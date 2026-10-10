package com.senior.grc.acoes;

import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaService;
import com.senior.grc.governanca.GovernancaService;
import com.senior.grc.mocks.OcorrenciasPort;
import com.senior.grc.risco.Cenario;
import com.senior.grc.risco.NivelRisco;
import com.senior.grc.risco.RiscoMudou;
import com.senior.grc.risco.RiscoService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Acoes e donos (SPEC.md secao 7). Quando o risco residual sobe para um nivel acionavel, abre (ou
 * atualiza) UM plano por risco e causa, atribuindo o dono por regra (cargo + local). A conclusao
 * fecha o ciclo: volta como evidencia e dispara a reavaliacao.
 *
 * Reage ao evento {@link RiscoMudou} publicado pelo RiscoService — desacoplado, sem ciclo.
 */
@Service
public class AcoesService {

    private final PlanoDeAcaoRepository planos;
    private final AtribuicaoService atribuicao;
    private final OcorrenciasPort ocorrencias;
    private final RiscoService risco;
    private final TrilhaService trilha;
    private final Relogio relogio;
    private final GovernancaService governanca;

    public AcoesService(PlanoDeAcaoRepository planos, AtribuicaoService atribuicao, OcorrenciasPort ocorrencias,
                        RiscoService risco, TrilhaService trilha, Relogio relogio, GovernancaService governanca) {
        this.planos = planos;
        this.atribuicao = atribuicao;
        this.ocorrencias = ocorrencias;
        this.risco = risco;
        this.trilha = trilha;
        this.relogio = relogio;
        this.governanca = governanca;
    }

    private static boolean acionavel(NivelRisco nivel) {
        return nivel == NivelRisco.MEDIO || nivel == NivelRisco.ALTO || nivel == NivelRisco.CRITICO;
    }

    @EventListener
    @Transactional
    public void aoMudarRisco(RiscoMudou e) {
        if (!acionavel(e.para())) {
            return; // risco baixou/desapareceu: nao abre plano novo (fechamento e manual)
        }
        if (governanca.riscoAceito(e.tenantId(), e.sujeitoId(), e.cenarioDominante())) {
            return; // excecao humana: risco aceito com justificativa -> nao abre plano
        }
        String causa = e.cenarioDominante();
        var existente = planos.findByTenantIdAndSujeitoIdAndCausaAndStatus(
                e.tenantId(), e.sujeitoId(), causa, PlanoDeAcao.Status.ABERTO);

        String descricao = "Tratar risco " + e.para() + " do cenario " + causa + " (sujeito " + e.sujeitoId() + ").";
        if (existente.isPresent()) {
            existente.get().atualizarDescricao(descricao); // atualiza, nao duplica
            planos.save(existente.get());
            return;
        }

        AtribuicaoService.Dono dono = atribuicao.donoDe(e.escopo(), e.sujeitoId());
        PlanoDeAcao plano = new PlanoDeAcao(UUID.randomUUID(), e.tenantId(), PlanoDeAcao.Tipo.ACAO_TRATAMENTO,
                e.sujeitoId(), e.escopo(), causa, dono.cargo(), dono.local(), dono.semDono(), descricao, relogio.agora());
        planos.save(plano);
        ocorrencias.abrirPlano(e.sujeitoId(), e.escopo(), causa, descricao);
        trilha.registrar("PLANO_ABERTO",
                "{\"sujeito\":\"" + e.sujeitoId() + "\",\"causa\":\"" + causa + "\",\"dono\":\"" + dono.cargo()
                        + (dono.local() != null ? "@" + dono.local() : "") + "\",\"semDono\":" + dono.semDono() + "}");
    }

    /** Conclui um plano: marca concluido, devolve a conclusao como evidencia e reavalia o risco (R3). */
    @Transactional
    public void concluir(UUID planoId) {
        PlanoDeAcao plano = planos.findById(planoId)
                .orElseThrow(() -> new IllegalArgumentException("plano nao encontrado"));
        plano.concluir(relogio.agora());
        planos.save(plano);
        Cenario.Escopo escopo = plano.getEscopo();
        ocorrencias.concluirPlano(plano.getSujeitoId(), escopo, plano.getCausa());
        trilha.registrar("PLANO_CONCLUIDO", "{\"plano\":\"" + planoId + "\",\"sujeito\":\"" + plano.getSujeitoId() + "\"}");
        if (escopo == Cenario.Escopo.PESSOA) {
            risco.reavaliarPessoa(plano.getSujeitoId());
        } else {
            risco.reavaliarEmpresa(plano.getSujeitoId());
        }
    }

    @Transactional(readOnly = true)
    public List<PlanoDeAcao> painel() {
        return planos.findByTenantId(TenantContext.obrigatorio());
    }
}
