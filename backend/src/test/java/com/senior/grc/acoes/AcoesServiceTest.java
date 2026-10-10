package com.senior.grc.acoes;

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

/**
 * Modulo 6: mudanca de risco abre plano (um por risco+causa, sem duplicar), dono por regra
 * (cargo+local, com fallback no gestor de riscos quando sem dono), e ciclo fechado (R3, R7).
 */
@SpringBootTest
@ActiveProfiles("test")
class AcoesServiceTest {

    @Autowired MockRhAdapter rh;
    @Autowired MockAcessoAdapter acesso;
    @Autowired RiscoService risco;
    @Autowired AcoesService acoes;
    @Autowired com.senior.grc.demo.RelogioDemo relogio;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM plano_acao");
        jdbc.update("DELETE FROM avaliacao_risco");
        jdbc.update("DELETE FROM evidencia_registro");
        jdbc.update("DELETE FROM pessoa");
        jdbc.update("DELETE FROM trilha_evento");
        relogio.reiniciar();
    }

    private String crachaDesligado(String cpf, String local) {
        String p = rh.admitir(cpf, "Ficticio", "VIGILANTE", local, null);
        acesso.emitirCracha(p);
        rh.desligar(p);
        risco.reavaliarPessoa(p); // dispara RiscoMudou -> abre plano
        return p;
    }

    @Test
    void risco_que_sobe_abre_plano_com_dono_por_regra() {
        TenantContext.definir("t1");
        rh.admitir("90000000100", "Gestor", "GESTOR_PLANTA", "PLANTA_NORTE", null);
        String p = crachaDesligado("90000000101", "PLANTA_NORTE");

        List<PlanoDeAcao> ps = acoes.painel();
        assertThat(ps).hasSize(1);
        PlanoDeAcao plano = ps.get(0);
        assertThat(plano.getSujeitoId()).isEqualTo(p);
        assertThat(plano.getCausa()).isEqualTo("CRACHA_DESLIGADO");
        assertThat(plano.getDonoCargo()).isEqualTo("GESTOR_PLANTA");
        assertThat(plano.getDonoLocal()).isEqualTo("PLANTA_NORTE");
        assertThat(plano.isSemDono()).isFalse();
    }

    @Test
    void risco_sem_gestor_vai_ao_gestor_de_riscos_marcado_sem_dono() {
        TenantContext.definir("t1");
        crachaDesligado("90000000102", "PLANTA_SUL"); // sem GESTOR_PLANTA nem GESTOR_RISCOS

        PlanoDeAcao plano = acoes.painel().get(0);
        assertThat(plano.getDonoCargo()).isEqualTo("GESTOR_RISCOS");
        assertThat(plano.isSemDono()).isTrue();
    }

    @Test
    void nao_duplica_plano_para_o_mesmo_risco_e_causa() {
        TenantContext.definir("t1");
        String p = crachaDesligado("90000000103", "PLANTA_NORTE");
        // Reavaliar de novo nao muda o nivel (oscilacao) -> nenhum plano novo.
        risco.reavaliarPessoa(p);
        risco.reavaliarPessoa(p);
        assertThat(acoes.painel()).hasSize(1);
    }

    @Test
    void concluir_plano_fecha_o_ciclo() {
        TenantContext.definir("t1");
        crachaDesligado("90000000104", "PLANTA_NORTE");
        PlanoDeAcao plano = acoes.painel().get(0);

        acoes.concluir(plano.getId());

        PlanoDeAcao depois = acoes.painel().get(0);
        assertThat(depois.getStatus()).isEqualTo(PlanoDeAcao.Status.CONCLUIDO);
        // A conclusao voltou como evidencia ACAO_CONCLUIDA (ciclo fechado).
        Integer n = jdbc.queryForObject(
                "SELECT count(*) FROM evidencia_registro WHERE tipo = 'ACAO_CONCLUIDA'", Integer.class);
        assertThat(n).isEqualTo(1);
    }
}
