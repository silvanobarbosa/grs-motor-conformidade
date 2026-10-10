package com.senior.grc.mcp;

import com.senior.grc.acoes.AcoesService;
import com.senior.grc.catalogo.CatalogoService;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaService;
import com.senior.grc.indicadores.IndicadoresService;
import com.senior.grc.risco.AvaliacaoRisco;
import com.senior.grc.risco.Cenario;
import com.senior.grc.risco.RiscoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servidor MCP (SPEC.md secao 14): canal de exibicao para a IA do cliente, com as MESMAS permissoes,
 * trilha e travas. Aqui mora a LOGICA e as guardas do servidor (resources, tools de leitura, tools
 * de escrita com confirmacao). O transporte MCP (JSON-RPC, OAuth 2.1) pluga por cima deste servico.
 *
 * Guardas inegociaveis:
 *  - MCP DESLIGADO por padrao (flag por tenant);
 *  - toda chamada entra na trilha;
 *  - escrita exige confirmacao (step-up);
 *  - NUNCA via MCP: aceitar risco, marcar nao aplicavel, alterar regras, revelar identidade,
 *    exportar em massa — sempre negado;
 *  - pseudonimizado: so trafega pseudonimo, nunca CPF.
 */
@Service
public class McpService {

    private final RiscoService risco;
    private final AcoesService acoes;
    private final CatalogoService catalogo;
    private final IndicadoresService indicadores;
    private final TrilhaService trilha;

    /** Tenants com MCP LIGADO (padrao: desligado). */
    private final Set<String> ligado = ConcurrentHashMap.newKeySet();

    public McpService(RiscoService risco, AcoesService acoes, CatalogoService catalogo,
                      IndicadoresService indicadores, TrilhaService trilha) {
        this.risco = risco;
        this.acoes = acoes;
        this.catalogo = catalogo;
        this.indicadores = indicadores;
        this.trilha = trilha;
    }

    public record Resultado(boolean ok, Object dado, String motivo) {
        static Resultado ok(Object dado) {
            return new Resultado(true, dado, null);
        }

        static Resultado negado(String motivo) {
            return new Resultado(false, null, motivo);
        }
    }

    public void ligar() {
        ligado.add(TenantContext.obrigatorio());
    }

    public void desligar() {
        ligado.remove(TenantContext.obrigatorio());
    }

    public boolean estaLigado() {
        return ligado.contains(TenantContext.obrigatorio());
    }

    private Resultado guarda(String tool) {
        String tenant = TenantContext.obrigatorio();
        trilha.registrar("MCP_CHAMADA", "{\"tool\":\"" + tool + "\",\"tenant\":\"" + tenant + "\"}");
        if (!estaLigado()) {
            return Resultado.negado("MCP desligado para este cliente (desligado por padrao)");
        }
        return null;
    }

    // ---------- Resources ----------
    public Resultado recursos() {
        Resultado g = guarda("recursos");
        if (g != null) {
            return g;
        }
        return Resultado.ok(List.of("risco-por-unidade", "requisitos-aplicaveis", "indicadores", "catalogo-regulatorio"));
    }

    // ---------- Tools de leitura ----------
    @Transactional
    public Resultado consultarRisco(String sujeitoId) {
        Resultado g = guarda("consultar_risco");
        if (g != null) {
            return g;
        }
        AvaliacaoRisco a = risco.doSujeito(sujeitoId).orElse(null);
        if (a == null) {
            return Resultado.ok(Map.of("sujeito", sujeitoId, "nivel", "SEM_RISCO"));
        }
        return Resultado.ok(Map.of("sujeito", a.getSujeitoId(), "nivel", a.getNivelResidual(),
                "cenario", String.valueOf(a.getCenarioDominante()), "explicacao", String.valueOf(a.getExplicacao())));
    }

    @Transactional
    public Resultado listarPendencias() {
        Resultado g = guarda("listar_pendencias");
        if (g != null) {
            return g;
        }
        return Resultado.ok(indicadores.operacionalVencimentos(15));
    }

    @Transactional
    public Resultado buscarRequisito(String norma, String item, Instant data) {
        Resultado g = guarda("buscar_requisito");
        if (g != null) {
            return g;
        }
        var req = data != null ? catalogo.vigenteEm(norma, item, data) : catalogo.vigenteAgora(norma, item);
        return req.<Resultado>map(r -> Resultado.ok(Map.of("norma", r.getNormaCodigo(), "item", r.getItem(),
                        "versao", r.getVersao(), "descricao", r.getDescricao(), "naoValidado", r.isNaoValidado())))
                .orElse(Resultado.ok(Map.of("encontrado", false)));
    }

    // ---------- Tools de escrita (com confirmacao / step-up) ----------
    @Transactional
    public Resultado abrirPlano(String sujeitoId, Cenario.Escopo escopo, String causa, boolean confirmado) {
        Resultado g = guarda("abrir_plano");
        if (g != null) {
            return g;
        }
        if (!confirmado) {
            return Resultado.negado("escrita via MCP exige confirmacao (step-up): reenvie com confirmado=true");
        }
        var plano = acoes.abrirPlanoManual(sujeitoId, escopo, causa, "Plano aberto via MCP (confirmado)");
        return Resultado.ok(Map.of("plano", plano.getId(), "status", plano.getStatus()));
    }

    // ---------- Acoes PROIBIDAS via MCP ----------
    public Resultado aceitarRisco() {
        guarda("aceitar_risco");
        return Resultado.negado("aceitar aceitacao de risco NAO pode ser feito via MCP");
    }

    public Resultado marcarNaoAplicavel() {
        guarda("marcar_nao_aplicavel");
        return Resultado.negado("marcar nao aplicavel NAO pode ser feito via MCP");
    }

    public Resultado revelarIdentidade() {
        guarda("revelar_identidade");
        return Resultado.negado("revelar identidade NAO pode ser feito via MCP (so pseudonimo)");
    }

    public Resultado exportarEmMassa() {
        guarda("exportar_em_massa");
        return Resultado.negado("exportacao em massa NAO pode ser feita via MCP");
    }
}
