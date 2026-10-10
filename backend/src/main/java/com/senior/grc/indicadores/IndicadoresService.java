package com.senior.grc.indicadores;

import com.senior.grc.cadastro.CadastroService;
import com.senior.grc.cadastro.Pessoa;
import com.senior.grc.catalogo.AplicabilidadeService;
import com.senior.grc.catalogo.ResultadoAplicabilidade;
import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.evidencia.EvidenciaRegistro;
import com.senior.grc.fundacao.evidencia.EvidenciaRepository;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.risco.AvaliacaoRisco;
import com.senior.grc.risco.AvaliacaoRiscoRepository;
import com.senior.grc.risco.NivelRisco;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Indicadores (SPEC.md secao 11) calculados no core, com saida agregada (API aberta para BI
 * externo). Tres publicos: operacional (o que resolvo hoje), gestao (onde a exposicao cresce),
 * executivo (dentro do apetite). Obrigatorios: cobertura e atualidade. Todo numero abre ate a
 * evidencia (o operacional ja devolve as evidencias que o compoem).
 */
@Service
public class IndicadoresService {

    private final EvidenciaRepository evidencias;
    private final AvaliacaoRiscoRepository avaliacoes;
    private final CadastroService cadastro;
    private final AplicabilidadeService aplicabilidade;
    private final Relogio relogio;

    public IndicadoresService(EvidenciaRepository evidencias, AvaliacaoRiscoRepository avaliacoes,
                              CadastroService cadastro, AplicabilidadeService aplicabilidade, Relogio relogio) {
        this.evidencias = evidencias;
        this.avaliacoes = avaliacoes;
        this.cadastro = cadastro;
        this.aplicabilidade = aplicabilidade;
        this.relogio = relogio;
    }

    public record Vencimento(String tipo, String pseudonimo, String chave, Instant validoAte, String fonte) {
    }

    /** OPERACIONAL: evidencias vencendo nos proximos {@code dias} (default 15) — o que resolver hoje. */
    @Transactional(readOnly = true)
    public List<Vencimento> operacionalVencimentos(int dias) {
        String tenant = TenantContext.obrigatorio();
        Instant agora = relogio.agora();
        Instant limite = agora.plus(Duration.ofDays(dias));
        return evidencias.findByTenantId(tenant).stream()
                .filter(e -> e.getValidoAte() != null
                        && !e.getValidoAte().isBefore(agora) && !e.getValidoAte().isAfter(limite))
                .map(e -> new Vencimento(e.getTipo(), e.getPseudonimoPessoa(),
                        e.atributo("norma") != null ? e.atributo("norma") : e.atributo("documento"),
                        e.getValidoAte(), e.getSistemaOrigem()))
                .toList();
    }

    /** GESTAO: contagem do risco residual por unidade (local) e nivel — onde a exposicao cresce. */
    @Transactional(readOnly = true)
    public Map<String, Map<NivelRisco, Long>> gestaoResidualPorUnidade() {
        String tenant = TenantContext.obrigatorio();
        Map<String, Map<NivelRisco, Long>> out = new TreeMap<>();
        for (AvaliacaoRisco a : avaliacoes.findByTenantId(tenant)) {
            String unidade = cadastro.porPseudonimo(a.getSujeitoId())
                    .map(Pessoa::getLocalCodigo).orElse("(terceiros)");
            out.computeIfAbsent(unidade, k -> new LinkedHashMap<>())
                    .merge(a.getNivelResidual(), 1L, Long::sum);
        }
        return out;
    }

    /** EXECUTIVO: quantos sujeitos estao ACIMA do apetite (residual > apetite). */
    @Transactional(readOnly = true)
    public Map<String, Object> executivoVsApetite(NivelRisco apetite) {
        String tenant = TenantContext.obrigatorio();
        List<AvaliacaoRisco> todas = avaliacoes.findByTenantId(tenant);
        long acima = todas.stream().filter(a -> a.getNivelResidual().maiorQue(apetite)).count();
        return Map.of("apetite", apetite, "sujeitos", todas.size(), "acimaDoApetite", acima,
                "dentroDoApetite", todas.size() - acima);
    }

    /** ATUALIDADE: ultimo envio de cada fonte (sistema de origem). */
    @Transactional(readOnly = true)
    public Map<String, Instant> atualidadePorFonte() {
        String tenant = TenantContext.obrigatorio();
        Map<String, Instant> out = new TreeMap<>();
        for (EvidenciaRegistro e : evidencias.findByTenantId(tenant)) {
            out.merge(e.getSistemaOrigem(), e.getColetadaEm(),
                    (a, b) -> a.isAfter(b) ? a : b);
        }
        return out;
    }

    /** COBERTURA: % de requisitos obrigatorios aplicaveis com evidencia (treinamento) valida. */
    @Transactional(readOnly = true)
    public Map<String, Object> cobertura() {
        String tenant = TenantContext.obrigatorio();
        Instant agora = relogio.agora();
        long totalObrigatorios = 0;
        long cobertos = 0;
        for (Pessoa p : cadastro.listar()) {
            if (p.isEliminada()) {
                continue;
            }
            List<EvidenciaRegistro> evs = evidencias.findByTenantIdAndPseudonimoPessoa(tenant, p.getPseudonimo());
            for (AplicabilidadeService.Sugestao s : aplicabilidade.sugerirPara(p.getCargoCodigo(), p.getLocalCodigo())) {
                if (s.resultado() != ResultadoAplicabilidade.OBRIGATORIO) {
                    continue;
                }
                totalObrigatorios++;
                boolean temValida = evs.stream().anyMatch(e -> e.getTipo().equals("TREINAMENTO")
                        && s.normaCodigo().equals(e.atributo("norma"))
                        && (e.getValidoAte() == null || !e.getValidoAte().isBefore(agora)));
                if (temValida) {
                    cobertos++;
                }
            }
        }
        double pct = totalObrigatorios == 0 ? 100.0 : (100.0 * cobertos / totalObrigatorios);
        return Map.of("requisitosObrigatorios", totalObrigatorios, "comEvidenciaValida", cobertos,
                "coberturaPct", Math.round(pct * 10) / 10.0);
    }
}
