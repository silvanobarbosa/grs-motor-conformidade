package com.senior.grc.risco;

import com.senior.grc.cadastro.CadastroService;
import com.senior.grc.cadastro.Pessoa;
import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.evidencia.EvidenciaRegistro;
import com.senior.grc.fundacao.evidencia.EvidenciaRepository;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Motor de avaliacao de risco residual (SPEC.md secao 6). Recalcula o residual de um sujeito a partir
 * das evidencias, aplicando as travas:
 *  - confianca: disparo apoiado SO em evidencia de baixa confianca nao vai a CRITICO;
 *  - oscilacao: so registra evento de mudanca quando o nivel realmente muda;
 *  - explicacao: toda mudanca guarda qual evidencia, de qual fonte, em qual regra.
 *
 * O residual nunca e editado a mao — e sempre o resultado deste calculo (CLAUDE.md regra 5).
 */
@Service
public class RiscoService {

    private final EvidenciaRepository evidencias;
    private final AvaliacaoRiscoRepository avaliacoes;
    private final CadastroService cadastro;
    private final CenarioBiblioteca biblioteca;
    private final RuleEnginePort motor;
    private final TrilhaService trilha;
    private final Relogio relogio;

    public RiscoService(EvidenciaRepository evidencias, AvaliacaoRiscoRepository avaliacoes, CadastroService cadastro,
                        CenarioBiblioteca biblioteca, RuleEnginePort motor, TrilhaService trilha, Relogio relogio) {
        this.evidencias = evidencias;
        this.avaliacoes = avaliacoes;
        this.cadastro = cadastro;
        this.biblioteca = biblioteca;
        this.motor = motor;
        this.trilha = trilha;
        this.relogio = relogio;
    }

    @Transactional
    public AvaliacaoRisco reavaliarPessoa(String pseudonimo) {
        String tenant = TenantContext.obrigatorio();
        List<EvidenciaRegistro> evs = evidencias.findByTenantIdAndPseudonimoPessoa(tenant, pseudonimo);
        return avaliar(tenant, pseudonimo, Cenario.Escopo.PESSOA, new EstadoDeEvidencia(evs, relogio.agora()));
    }

    @Transactional
    public AvaliacaoRisco reavaliarEmpresa(String empresaCodigo) {
        String tenant = TenantContext.obrigatorio();
        List<EvidenciaRegistro> evs = evidencias.findByTenantIdAndSistemaOrigem(tenant, "TERCEIROS").stream()
                .filter(e -> empresaCodigo.equals(e.atributo("empresa")))
                .toList();
        return avaliar(tenant, empresaCodigo, Cenario.Escopo.EMPRESA, new EstadoDeEvidencia(evs, relogio.agora()));
    }

    /** Reavalia todas as pessoas e empresas do tenant (usado apos avancar o relogio). */
    @Transactional
    public int reavaliarTudo() {
        String tenant = TenantContext.obrigatorio();
        int n = 0;
        for (Pessoa p : cadastro.listar()) {
            reavaliarPessoa(p.getPseudonimo());
            n++;
        }
        for (String empresa : evidencias.findByTenantIdAndSistemaOrigem(tenant, "TERCEIROS").stream()
                .map(e -> e.atributo("empresa")).filter(java.util.Objects::nonNull).distinct().toList()) {
            reavaliarEmpresa(empresa);
            n++;
        }
        return n;
    }

    private AvaliacaoRisco avaliar(String tenant, String sujeitoId, Cenario.Escopo escopo, EstadoDeEvidencia estado) {
        NivelRisco residual = NivelRisco.SEM_RISCO;
        String cenarioDominante = null;
        String explicacao = "Sem cenario de risco ativo.";

        for (Cenario c : biblioteca.doEscopo(escopo)) {
            Optional<RuleEnginePort.Gatilho> g = motor.avaliar(c, estado);
            if (g.isEmpty()) {
                continue;
            }
            NivelRisco nivel = c.nivel();
            if (g.get().apenasBaixaConfianca()) {
                nivel = nivel.limitadoSemAltaConfianca(); // trava de confianca
            }
            if (nivel.maiorQue(residual)) {
                residual = nivel;
                cenarioDominante = c.codigo();
                explicacao = g.get().explicacao();
            }
        }

        AvaliacaoRisco atual = avaliacoes.findByTenantIdAndSujeitoId(tenant, sujeitoId)
                .orElseGet(() -> new AvaliacaoRisco(UUID.randomUUID(), tenant, sujeitoId, escopo));
        NivelRisco anterior = atual.getNivelResidual();

        atual.atualizar(residual, cenarioDominante, explicacao, relogio.agora());
        avaliacoes.save(atual);

        // Trava de oscilacao: so registra o evento de mudanca quando o nivel realmente muda.
        if (anterior != residual) {
            trilha.registrar("RISCO_AVALIADO",
                    "{\"sujeito\":\"" + sujeitoId + "\",\"de\":\"" + anterior + "\",\"para\":\"" + residual
                            + "\",\"cenario\":\"" + cenarioDominante + "\"}");
        }
        return atual;
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoRisco> painel() {
        return avaliacoes.findByTenantId(TenantContext.obrigatorio());
    }

    @Transactional(readOnly = true)
    public Optional<AvaliacaoRisco> doSujeito(String sujeitoId) {
        return avaliacoes.findByTenantIdAndSujeitoId(TenantContext.obrigatorio(), sujeitoId);
    }
}
