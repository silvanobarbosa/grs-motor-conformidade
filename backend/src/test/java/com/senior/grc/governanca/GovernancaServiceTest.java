package com.senior.grc.governanca;

import com.senior.grc.acoes.AcoesService;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.mocks.MockAcessoAdapter;
import com.senior.grc.mocks.MockRhAdapter;
import com.senior.grc.risco.RiscoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Modulo 7: governanca — politica publicada, ciencia por cargo+local, pendencias (R6), e aceitar
 * risco (excecao humana) travando a abertura de plano.
 */
@SpringBootTest
@ActiveProfiles("test")
class GovernancaServiceTest {

    @Autowired GovernancaService gov;
    @Autowired MockRhAdapter rh;
    @Autowired MockAcessoAdapter acesso;
    @Autowired RiscoService risco;
    @Autowired AcoesService acoes;
    @Autowired com.senior.grc.demo.RelogioDemo relogio;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM excecao_risco");
        jdbc.update("DELETE FROM ciencia_registro");
        jdbc.update("DELETE FROM politica");
        jdbc.update("DELETE FROM plano_acao");
        jdbc.update("DELETE FROM avaliacao_risco");
        jdbc.update("DELETE FROM evidencia_registro");
        jdbc.update("DELETE FROM pessoa");
        jdbc.update("DELETE FROM trilha_evento");
        relogio.reiniciar();
    }

    @Test
    void politica_publicada_gera_pendencias_de_ciencia_por_cargo_local() {
        TenantContext.definir("t1");
        String p1 = rh.admitir("90000000200", "A", "VIGILANTE", "PORTARIA_NORTE", null);
        String p2 = rh.admitir("90000000201", "B", "VIGILANTE", "PORTARIA_NORTE", null);

        gov.criarPolitica("POL-CRACHA", "Cracha de desligado bloqueado em 24h", "VIGILANTE", "PORTARIA_NORTE");
        gov.publicarPolitica("POL-CRACHA", "key-user");

        assertThat(gov.pendenciasCiencia("POL-CRACHA")).containsExactlyInAnyOrder(p1, p2);
        gov.registrarCiencia("POL-CRACHA", p1);
        assertThat(gov.pendenciasCiencia("POL-CRACHA")).containsExactly(p2);
    }

    @Test
    void ciencia_so_vale_para_politica_publicada() {
        TenantContext.definir("t1");
        gov.criarPolitica("POL-X", "Rascunho", "VIGILANTE", null);
        assertThrows(IllegalArgumentException.class, () -> gov.registrarCiencia("POL-X", "P-1"));
    }

    @Test
    void aceitar_risco_exige_justificativa() {
        TenantContext.definir("t1");
        assertThrows(IllegalArgumentException.class, () -> gov.aceitarRisco("P-1", "CRACHA_DESLIGADO", "  ", "key-user"));
    }

    @Test
    void risco_aceito_nao_abre_plano() {
        TenantContext.definir("t1");
        String p = rh.admitir("90000000202", "C", "VIGILANTE", "PLANTA_NORTE", null);
        acesso.emitirCracha(p);
        rh.desligar(p);
        // Aceita o risco ANTES de reavaliar -> excecao humana trava a abertura de plano.
        gov.aceitarRisco(p, "CRACHA_DESLIGADO", "risco assumido com controle compensatorio", "key-user");

        risco.reavaliarPessoa(p);
        List<?> planos = acoes.painel();
        assertThat(planos).isEmpty();
    }
}
