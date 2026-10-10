package com.senior.grc.e2e;

import com.senior.grc.acoes.AcoesService;
import com.senior.grc.acoes.PlanoDeAcao;
import com.senior.grc.catalogo.AplicabilidadeService;
import com.senior.grc.catalogo.CatalogoService;
import com.senior.grc.catalogo.ResultadoAplicabilidade;
import com.senior.grc.externo.DossieService;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaService;
import com.senior.grc.governanca.GovernancaService;
import com.senior.grc.ia.SaraService;
import com.senior.grc.indicadores.IndicadoresService;
import com.senior.grc.mcp.McpService;
import com.senior.grc.mocks.MockAcessoAdapter;
import com.senior.grc.mocks.MockOcrAdapter;
import com.senior.grc.mocks.MockRhAdapter;
import com.senior.grc.ocr.ConferenciaService;
import com.senior.grc.cadastro.CadastroService;
import com.senior.grc.risco.Cenario;
import com.senior.grc.risco.NivelRisco;
import com.senior.grc.risco.RiscoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Roteiros R1 a R12 da demonstracao, ponta a ponta (DEMO.md secoes 5 e 7). Alem de rodar cada
 * roteiro, provam os criterios de pronto: trilha nao pode ser alterada; risco residual e derivado e
 * explicavel; permissoes do core nao sao ampliadas pela origem; MCP nao executa acao proibida.
 */
@SpringBootTest
@ActiveProfiles("test")
class RoteirosE2ETest {

    @Autowired MockRhAdapter rh;
    @Autowired MockAcessoAdapter acesso;
    @Autowired MockOcrAdapter ocr;
    @Autowired RiscoService risco;
    @Autowired AcoesService acoes;
    @Autowired ConferenciaService conferencia;
    @Autowired CatalogoService catalogo;
    @Autowired AplicabilidadeService aplicabilidade;
    @Autowired GovernancaService gov;
    @Autowired IndicadoresService indicadores;
    @Autowired DossieService dossie;
    @Autowired SaraService sara;
    @Autowired McpService mcp;
    @Autowired CadastroService cadastro;
    @Autowired TrilhaService trilha;
    @Autowired com.senior.grc.demo.RelogioDemo relogio;
    @Autowired JdbcTemplate jdbc;

    private static final String T = "metalurgica-exemplo";

    @BeforeEach
    void preparar() {
        TenantContext.definir(T);
        catalogo.semearGlobal();
        aplicabilidade.semearGlobalRegras();
    }

    @AfterEach
    void limpa() {
        sara.ligarIa();
        mcp.desligar();
        for (String t : List.of("excecao_risco", "ciencia_registro", "politica", "acesso_externo",
                "plano_acao", "avaliacao_risco", "aplicabilidade_confirmacao", "aplicabilidade_regra",
                "requisito_regulatorio", "norma", "evidencia_registro", "pessoa", "trilha_evento")) {
            jdbc.update("DELETE FROM " + t);
        }
        TenantContext.limpar();
        relogio.reiniciar();
    }

    @Test
    void R1_onboarding_e_aplicabilidade() {
        var sugestoes = aplicabilidade.sugerirPara("ELETRICISTA", "PLANTA_NORTE");
        assertThat(sugestoes).anyMatch(s -> s.normaCodigo().equals("NR-10") && s.resultado() == ResultadoAplicabilidade.OBRIGATORIO);
        // Marcar NR-33 nao aplicavel na Planta Norte exige responsavel tecnico.
        assertThrows(IllegalArgumentException.class, () -> aplicabilidade.confirmar(
                "NR-33", "OPERADOR_PRODUCAO", "PLANTA_NORTE", ResultadoAplicabilidade.NAO_APLICAVEL,
                "sem espaco confinado", null, "key-user"));
    }

    @Test
    void R2_evidencia_muda_o_risco() {
        String p = rh.admitir("90000001001", "F", "TECNICO_MANUTENCAO", "PLANTA_SUL",
                Map.of("NR-35", relogio.agora().plus(Duration.ofDays(5))));
        rh.admitir("90000001000", "Gestor", "GESTOR_PLANTA", "PLANTA_SUL", null);
        assertThat(risco.reavaliarPessoa(p).getNivelResidual()).isEqualTo(NivelRisco.MEDIO);
        relogio.avancarDias(10);
        var r = risco.reavaliarPessoa(p);
        assertThat(r.getNivelResidual()).isEqualTo(NivelRisco.ALTO);
        assertThat(r.getExplicacao()).isNotBlank();
        // Plano aberto automaticamente, com dono por regra (gestor da planta).
        assertThat(acoes.painel()).anyMatch(pl -> pl.getSujeitoId().equals(p) && pl.getDonoCargo().equals("GESTOR_PLANTA"));
    }

    @Test
    void R3_ciclo_fechado() {
        String p = rh.admitir("90000001002", "F", "VIGILANTE", "PLANTA_NORTE", null);
        acesso.emitirCracha(p);
        rh.desligar(p);
        risco.reavaliarPessoa(p);
        PlanoDeAcao plano = acoes.painel().get(0);
        acoes.concluir(plano.getId());
        assertThat(acoes.painel().get(0).getStatus()).isEqualTo(PlanoDeAcao.Status.CONCLUIDO);
        Integer n = jdbc.queryForObject("SELECT count(*) FROM evidencia_registro WHERE tipo='ACAO_CONCLUIDA'", Integer.class);
        assertThat(n).isEqualTo(1);
    }

    @Test
    void R4_terceiro_e_ocr() {
        ocr.lerDocumentoTerceiro("TERC-OBRA", "ASO", relogio.agora().plus(Duration.ofDays(90)), "hash-x", true);
        assertThat(risco.reavaliarEmpresa("TERC-OBRA").getNivelResidual()).isEqualTo(NivelRisco.ALTO);
        conferencia.conferir(conferencia.pendentes().get(0).getId(), "tst");
        assertThat(risco.reavaliarEmpresa("TERC-OBRA").getNivelResidual()).isEqualTo(NivelRisco.SEM_RISCO);
    }

    @Test
    void R5_aplicabilidade_continua() {
        rh.admitir("90000001003", "F", "ELETRICISTA", "PLANTA_NORTE", null);
        assertThat(aplicabilidade.sugerirPara("ELETRICISTA", "PLANTA_NORTE"))
                .anyMatch(s -> s.normaCodigo().equals("NR-10"));
    }

    @Test
    void R6_governanca() {
        String p1 = rh.admitir("90000001004", "A", "VIGILANTE", "PORTARIA_NORTE", null);
        gov.criarPolitica("POL-CRACHA", "Cracha de desligado bloqueado em 24h", "VIGILANTE", "PORTARIA_NORTE");
        gov.publicarPolitica("POL-CRACHA", "key-user");
        assertThat(gov.pendenciasCiencia("POL-CRACHA")).contains(p1);
        gov.registrarCiencia("POL-CRACHA", p1);
        assertThat(gov.pendenciasCiencia("POL-CRACHA")).doesNotContain(p1);
    }

    @Test
    void R7_desligamento() {
        String p = rh.admitir("90000001005", "F", "GESTOR_SEGURANCA_PATRIMONIAL", "PLANTA_NORTE", null);
        acesso.emitirCracha(p);
        rh.desligar(p);
        assertThat(risco.reavaliarPessoa(p).getNivelResidual()).isEqualTo(NivelRisco.CRITICO);
    }

    @Test
    void R8_dashboards() {
        String p = rh.admitir("90000001006", "F", "TECNICO_MANUTENCAO", "PLANTA_SUL",
                Map.of("NR-35", relogio.agora().minus(Duration.ofDays(1))));
        risco.reavaliarPessoa(p);
        assertThat(indicadores.gestaoResidualPorUnidade()).containsKey("PLANTA_SUL");
        assertThat(indicadores.atualidadePorFonte()).containsKey("RH");
        assertThat(indicadores.cobertura()).containsKey("coberturaPct");
        assertThat(indicadores.executivoVsApetite(NivelRisco.MEDIO)).containsKey("acimaDoApetite");
    }

    @Test
    void R9_auditoria_externa_e_dossie() {
        String p = rh.admitir("90000001007", "F", "TECNICO_MANUTENCAO", "PLANTA_SUL",
                Map.of("NR-35", relogio.agora().plus(Duration.ofDays(100))));
        dossie.conceder("auditor-1", "NR-35", 5, "key-user");
        DossieService.Dossie d = dossie.gerar("auditor-1", "NR-35");
        assertThat(d.trilhaIntegra()).isTrue();
        assertThat(d.conteudo().get(0).pseudonimo()).isEqualTo(p);
        assertThat(d.conteudo().toString()).doesNotContain("90000001007");
    }

    @Test
    void R10_lgpd_eliminacao() {
        String p = rh.admitir("90000001008", "F", "OPERADOR_PRODUCAO", "PLANTA_SUL", null);
        cadastro.eliminar(p);
        assertThat(cadastro.pseudonimoPorCpf("90000001008")).isEmpty(); // identidade irrecuperavel
        assertThat(trilha.verificarIntegridade(T).integra()).isTrue(); // trilha intacta
    }

    @Test
    void R11_ia_sara() {
        var t = sara.triarOcorrencia("Ocorrencia na PLANTA_SUL em 2026-10-05.");
        assertThat(t).isPresent();
        assertThat(t.get().local()).isEqualTo("PLANTA_SUL");
        sara.desligarIa();
        assertThat(sara.triarOcorrencia("qualquer")).isEmpty(); // produto funciona com IA desligada
    }

    @Test
    void R12_mcp() {
        String p = rh.admitir("90000001009", "F", "VIGILANTE", "PLANTA_NORTE", null);
        acesso.emitirCracha(p);
        rh.desligar(p);
        risco.reavaliarPessoa(p);

        assertThat(mcp.consultarRisco(p).ok()).isFalse(); // desligado por padrao
        mcp.ligar();
        assertThat(mcp.consultarRisco(p).ok()).isTrue();
        assertThat(mcp.abrirPlano(p, Cenario.Escopo.PESSOA, "CRACHA_DESLIGADO", false).ok()).isFalse();
        assertThat(mcp.aceitarRisco().ok()).isFalse(); // acao proibida via MCP
    }

    @Test
    void criterio_trilha_imutavel_detecta_adulteracao() {
        String p = rh.admitir("90000001010", "F", "VIGILANTE", "PLANTA_NORTE", null);
        var ev = jdbc.queryForObject("SELECT id FROM trilha_evento WHERE tenant_id=? ORDER BY seq LIMIT 1", String.class, T);
        jdbc.update("UPDATE trilha_evento SET payload='{\"adulterado\":true}' WHERE id=?", ev);
        assertThat(trilha.verificarIntegridade(T).integra()).isFalse();
        assertThat(cadastro.porPseudonimo(p)).isPresent();
    }
}
