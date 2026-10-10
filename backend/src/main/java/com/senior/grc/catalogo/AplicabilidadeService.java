package com.senior.grc.catalogo;

import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Aplicabilidade das normas (SPEC.md secao 9).
 *
 * - Regras GLOBAIS por cargo/local dizem o resultado padrao (obrigatorio / requer confirmacao /
 *   nao aplicavel). Na duvida (sem regra), REQUER_CONFIRMACAO.
 * - O key user CONFIRMA por tenant, ou marca NAO_APLICAVEL com justificativa (obrigatoria); nao
 *   aplicavel de alta criticidade exige responsavel tecnico.
 * - Cada confirmacao deixa rastro na trilha.
 */
@Service
public class AplicabilidadeService {

    private final AplicabilidadeRegraRepository regras;
    private final AplicabilidadeConfirmacaoRepository confirmacoes;
    private final TrilhaService trilha;
    private final Relogio relogio;

    public AplicabilidadeService(AplicabilidadeRegraRepository regras,
                                 AplicabilidadeConfirmacaoRepository confirmacoes,
                                 TrilhaService trilha, Relogio relogio) {
        this.regras = regras;
        this.confirmacoes = confirmacoes;
        this.trilha = trilha;
        this.relogio = relogio;
    }

    public record Sugestao(String normaCodigo, ResultadoAplicabilidade resultado, boolean altaCriticidade) {
    }

    @Transactional
    public void semearGlobalRegras() {
        if (regras.count() > 0) {
            return;
        }
        // Todos: NR-1 e NR-6 obrigatorias.
        regra("NR-1", null, null, ResultadoAplicabilidade.OBRIGATORIO, false);
        regra("NR-6", null, null, ResultadoAplicabilidade.OBRIGATORIO, false);
        // Eletricista: NR-10 obrigatoria.
        regra("NR-10", "ELETRICISTA", null, ResultadoAplicabilidade.OBRIGATORIO, true);
        // Tecnico de manutencao (altura/predial): NR-35 obrigatoria.
        regra("NR-35", "TECNICO_MANUTENCAO", null, ResultadoAplicabilidade.OBRIGATORIO, true);
        // Espaco confinado so na Planta Norte: requer confirmacao para operador de producao la.
        regra("NR-33", "OPERADOR_PRODUCAO", "PLANTA_NORTE", ResultadoAplicabilidade.REQUER_CONFIRMACAO, true);
    }

    /** Sugere as normas para um cargo+local, aplicando a confirmacao do tenant quando houver. */
    @Transactional(readOnly = true)
    public List<Sugestao> sugerirPara(String cargo, String localCodigo) {
        String tenant = TenantContext.obrigatorio();
        List<Sugestao> out = new ArrayList<>();
        for (AplicabilidadeRegra r : regras.findAll()) {
            if (!r.casaCom(cargo, localCodigo)) {
                continue;
            }
            ResultadoAplicabilidade resultado = confirmacoes
                    .findByTenantIdAndNormaCodigoAndCargoAndLocalCodigo(tenant, r.getNormaCodigo(), cargo, localCodigo)
                    .map(AplicabilidadeConfirmacao::getResultado)
                    .orElse(r.getResultadoPadrao());
            out.add(new Sugestao(r.getNormaCodigo(), resultado, r.isAltaCriticidade()));
        }
        return out;
    }

    /** Resultado efetivo de uma norma para cargo+local (confirmacao do tenant > padrao global). */
    @Transactional(readOnly = true)
    public Optional<ResultadoAplicabilidade> resultadoDe(String normaCodigo, String cargo, String localCodigo) {
        return sugerirPara(cargo, localCodigo).stream()
                .filter(s -> s.normaCodigo().equals(normaCodigo))
                .map(Sugestao::resultado)
                .findFirst();
    }

    @Transactional
    public void confirmar(String normaCodigo, String cargo, String localCodigo, ResultadoAplicabilidade resultado,
                          String justificativa, String responsavelTecnico, String confirmadoPor) {
        String tenant = TenantContext.obrigatorio();
        boolean altaCriticidade = regras.findAll().stream()
                .anyMatch(r -> r.getNormaCodigo().equals(normaCodigo) && r.casaCom(cargo, localCodigo) && r.isAltaCriticidade());

        if (resultado == ResultadoAplicabilidade.NAO_APLICAVEL) {
            if (justificativa == null || justificativa.isBlank()) {
                throw new IllegalArgumentException("nao aplicavel exige justificativa (SPEC.md 9)");
            }
            if (altaCriticidade && (responsavelTecnico == null || responsavelTecnico.isBlank())) {
                throw new IllegalArgumentException("nao aplicavel de alta criticidade exige responsavel tecnico");
            }
        }

        confirmacoes.save(new AplicabilidadeConfirmacao(UUID.randomUUID(), tenant, normaCodigo, cargo, localCodigo,
                resultado, justificativa, responsavelTecnico, confirmadoPor, relogio.agora()));
        trilha.registrar("APLICABILIDADE_CONFIRMADA",
                "{\"norma\":\"" + normaCodigo + "\",\"cargo\":\"" + cargo + "\",\"local\":\"" + localCodigo
                        + "\",\"resultado\":\"" + resultado + "\",\"por\":\"" + confirmadoPor + "\"}");
    }

    private void regra(String norma, String cargo, String local, ResultadoAplicabilidade res, boolean alta) {
        regras.save(new AplicabilidadeRegra(UUID.randomUUID(), norma, cargo, local, res, alta));
    }
}
