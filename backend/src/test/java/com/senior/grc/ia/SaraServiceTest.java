package com.senior.grc.ia;

import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.mocks.MockAcessoAdapter;
import com.senior.grc.mocks.MockRhAdapter;
import com.senior.grc.mocks.SaraPort;
import com.senior.grc.risco.NivelRisco;
import com.senior.grc.risco.RiscoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Modulo 12: Sara (IA) — triagem e explicacao; IA nunca decide; produto funciona desligada (R11). */
@SpringBootTest
@ActiveProfiles("test")
class SaraServiceTest {

    @Autowired SaraService sara;
    @Autowired MockRhAdapter rh;
    @Autowired MockAcessoAdapter acesso;
    @Autowired RiscoService risco;
    @Autowired com.senior.grc.demo.RelogioDemo relogio;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        sara.ligarIa(); // o estado de IA (desligada) e do singleton: religa antes de limpar o tenant
        TenantContext.limpar();
        jdbc.update("DELETE FROM avaliacao_risco");
        jdbc.update("DELETE FROM evidencia_registro");
        jdbc.update("DELETE FROM pessoa");
        jdbc.update("DELETE FROM trilha_evento");
        relogio.reiniciar();
    }

    @Test
    void triagem_extrai_local_e_pessoas_com_modelo_e_confianca() {
        TenantContext.definir("t1");
        Optional<SaraPort.Triagem> t = sara.triarOcorrencia(
                "Ocorrencia na PLANTA_NORTE em 2026-10-05 envolvendo P-ABC123 sem cracha.");
        assertThat(t).isPresent();
        assertThat(t.get().local()).isEqualTo("PLANTA_NORTE");
        assertThat(t.get().data()).isEqualTo("2026-10-05");
        assertThat(t.get().pessoas()).contains("P-ABC123");
        assertThat(t.get().modelo()).isNotBlank();
        assertThat(t.get().confianca()).isGreaterThan(0.0);
    }

    @Test
    void explicar_risco_reescreve_a_explicacao_sem_decidir() {
        TenantContext.definir("t1");
        String p = rh.admitir("90000000500", "Ficticio", "VIGILANTE", "PLANTA_NORTE", null);
        acesso.emitirCracha(p);
        rh.desligar(p);
        risco.reavaliarPessoa(p);

        Optional<String> exp = sara.explicarRisco(p);
        assertThat(exp).isPresent();
        assertThat(exp.get()).contains("CRACHA").contains("DESLIGAMENTO");
        // A IA nao mudou o risco: continua CRITICO (so explicou).
        assertThat(risco.doSujeito(p).orElseThrow().getNivelResidual()).isEqualTo(NivelRisco.CRITICO);
    }

    @Test
    void produto_funciona_com_ia_desligada() {
        TenantContext.definir("t1");
        sara.desligarIa();
        assertThat(sara.iaLigada()).isFalse();
        assertThat(sara.triarOcorrencia("qualquer relato na PLANTA_SUL")).isEmpty();
        assertThat(sara.explicarRisco("P-XYZ")).isEmpty();
        // O resto do produto segue: avaliar risco nao depende da IA.
        String p = rh.admitir("90000000501", "Ficticio", "VIGILANTE", "PLANTA_SUL", null);
        acesso.emitirCracha(p);
        rh.desligar(p);
        assertThat(risco.reavaliarPessoa(p).getNivelResidual()).isEqualTo(NivelRisco.CRITICO);
    }
}
