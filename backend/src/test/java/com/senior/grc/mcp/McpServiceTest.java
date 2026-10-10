package com.senior.grc.mcp;

import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.mocks.MockAcessoAdapter;
import com.senior.grc.mocks.MockRhAdapter;
import com.senior.grc.risco.Cenario;
import com.senior.grc.risco.RiscoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Modulo 13: servidor MCP — desligado por padrao, leitura, escrita com confirmacao, proibidos (R12). */
@SpringBootTest
@ActiveProfiles("test")
class McpServiceTest {

    @Autowired McpService mcp;
    @Autowired MockRhAdapter rh;
    @Autowired MockAcessoAdapter acesso;
    @Autowired RiscoService risco;
    @Autowired com.senior.grc.demo.RelogioDemo relogio;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        mcp.desligar();
        TenantContext.limpar();
        jdbc.update("DELETE FROM plano_acao");
        jdbc.update("DELETE FROM avaliacao_risco");
        jdbc.update("DELETE FROM evidencia_registro");
        jdbc.update("DELETE FROM pessoa");
        jdbc.update("DELETE FROM trilha_evento");
        relogio.reiniciar();
    }

    @Test
    void mcp_desligado_por_padrao_nega_tudo() {
        TenantContext.definir("t1");
        assertThat(mcp.estaLigado()).isFalse();
        assertThat(mcp.consultarRisco("P-1").ok()).isFalse();
    }

    @Test
    void consultar_risco_funciona_com_mcp_ligado() {
        TenantContext.definir("t1");
        String p = rh.admitir("90000000600", "F", "VIGILANTE", "PLANTA_NORTE", null);
        acesso.emitirCracha(p);
        rh.desligar(p);
        risco.reavaliarPessoa(p);

        mcp.ligar();
        McpService.Resultado r = mcp.consultarRisco(p);
        assertThat(r.ok()).isTrue();
        assertThat(r.dado().toString()).contains("CRITICO");
    }

    @Test
    void abrir_plano_exige_confirmacao() {
        TenantContext.definir("t1");
        String p = rh.admitir("90000000601", "F", "VIGILANTE", "PLANTA_NORTE", null);
        mcp.ligar();
        assertThat(mcp.abrirPlano(p, Cenario.Escopo.PESSOA, "CRACHA_DESLIGADO", false).ok()).isFalse();
        assertThat(mcp.abrirPlano(p, Cenario.Escopo.PESSOA, "CRACHA_DESLIGADO", true).ok()).isTrue();
    }

    @Test
    void acoes_proibidas_via_mcp_sao_sempre_negadas() {
        TenantContext.definir("t1");
        mcp.ligar();
        assertThat(mcp.aceitarRisco().ok()).isFalse();
        assertThat(mcp.marcarNaoAplicavel().ok()).isFalse();
        assertThat(mcp.revelarIdentidade().ok()).isFalse();
        assertThat(mcp.exportarEmMassa().ok()).isFalse();
    }

    @Test
    void toda_chamada_entra_na_trilha() {
        TenantContext.definir("t1");
        mcp.ligar();
        mcp.consultarRisco("P-1");
        Integer n = jdbc.queryForObject("SELECT count(*) FROM trilha_evento WHERE tipo = 'MCP_CHAMADA'", Integer.class);
        assertThat(n).isGreaterThanOrEqualTo(1);
    }
}
