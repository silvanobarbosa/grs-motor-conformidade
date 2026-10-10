package com.senior.grc.externo;

import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.mocks.MockRhAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Modulo 10: acesso externo temporario + dossie pseudonimizado com integridade (R9). */
@SpringBootTest
@ActiveProfiles("test")
class DossieServiceTest {

    @Autowired DossieService dossie;
    @Autowired MockRhAdapter rh;
    @Autowired com.senior.grc.demo.RelogioDemo relogio;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM acesso_externo");
        jdbc.update("DELETE FROM evidencia_registro");
        jdbc.update("DELETE FROM pessoa");
        jdbc.update("DELETE FROM trilha_evento");
        relogio.reiniciar();
    }

    @Test
    void acesso_temporario_vale_so_dentro_do_prazo() {
        TenantContext.definir("t1");
        dossie.conceder("auditor-1", "NR-35", 2, "key-user");
        assertThat(dossie.acessoValido("auditor-1")).isTrue();
        relogio.avancarDias(3);
        assertThat(dossie.acessoValido("auditor-1")).isFalse();
    }

    @Test
    void dossie_exige_acesso_valido() {
        TenantContext.definir("t1");
        assertThrows(IllegalStateException.class, () -> dossie.gerar("auditor-sem-acesso", "NR-35"));
    }

    @Test
    void dossie_e_pseudonimizado_e_prova_integridade() {
        TenantContext.definir("t1");
        String cpf = "90000000400";
        String p = rh.admitir(cpf, "Ficticio", "TECNICO_MANUTENCAO", "PLANTA_SUL",
                Map.of("NR-35", relogio.agora().plus(Duration.ofDays(100))));
        dossie.conceder("auditor-1", "NR-35", 5, "key-user");

        DossieService.Dossie d = dossie.gerar("auditor-1", "NR-35");
        assertThat(d.itens()).isEqualTo(1);
        assertThat(d.trilhaIntegra()).isTrue();
        assertThat(d.hashDossie()).isNotBlank();
        // Pseudonimizado: so o pseudonimo aparece, nunca o CPF.
        assertThat(d.conteudo().get(0).pseudonimo()).isEqualTo(p);
        assertThat(d.conteudo().toString()).doesNotContain(cpf);
    }
}
