package com.senior.grc.fundacao;

import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaEvento;
import com.senior.grc.fundacao.trilha.TrilhaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regras inegociaveis da trilha (CLAUDE.md regra 3; criterios da DEMO.md secao 7):
 * - so inclusao, encadeada por hash;
 * - sequencia por tenant;
 * - adulteracao e detectada;
 * - cadeias de tenants nao se misturam.
 */
@SpringBootTest
@ActiveProfiles("test")
class TrilhaServiceTest {

    @Autowired
    TrilhaService trilha;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM trilha_evento");
    }

    @Test
    void encadeia_eventos_por_hash_com_sequencia() {
        TenantContext.definir("t1");
        TrilhaEvento e1 = trilha.registrar("TESTE", "{\"n\":1}");
        TrilhaEvento e2 = trilha.registrar("TESTE", "{\"n\":2}");
        TrilhaEvento e3 = trilha.registrar("TESTE", "{\"n\":3}");

        assertThat(e1.getSeq()).isEqualTo(1);
        assertThat(e2.getSeq()).isEqualTo(2);
        assertThat(e3.getSeq()).isEqualTo(3);
        assertThat(e1.getHashAnterior()).isEqualTo(TrilhaService.GENESIS);
        assertThat(e2.getHashAnterior()).isEqualTo(e1.getHash());
        assertThat(e3.getHashAnterior()).isEqualTo(e2.getHash());
        assertThat(e1.getHash()).isEqualTo(e1.hashRecalculado());
    }

    @Test
    void cadeia_integra_passa_na_verificacao() {
        TenantContext.definir("t1");
        trilha.registrar("TESTE", "{\"n\":1}");
        trilha.registrar("TESTE", "{\"n\":2}");

        TrilhaService.ResultadoIntegridade r = trilha.verificarIntegridade("t1");
        assertThat(r.integra()).isTrue();
        assertThat(r.eventos()).isEqualTo(2);
    }

    @Test
    void adulteracao_do_payload_e_detectada() {
        TenantContext.definir("t1");
        trilha.registrar("TESTE", "{\"n\":1}");
        TrilhaEvento e2 = trilha.registrar("TESTE", "{\"n\":2}");

        // Simula adulteracao direta no banco (a entidade nao tem setter; so SQL cru consegue).
        jdbc.update("UPDATE trilha_evento SET payload = ? WHERE id = ?", "{\"n\":999}", e2.getId().toString());

        TrilhaService.ResultadoIntegridade r = trilha.verificarIntegridade("t1");
        assertThat(r.integra()).isFalse();
        assertThat(r.seqProblema()).isEqualTo(2);
        assertThat(r.motivo()).contains("hash nao confere");
    }

    @Test
    void cadeias_de_tenants_nao_se_misturam() {
        TenantContext.definir("t1");
        trilha.registrar("TESTE", "{\"a\":1}");
        TenantContext.definir("t2");
        trilha.registrar("TESTE", "{\"b\":1}");
        trilha.registrar("TESTE", "{\"b\":2}");

        List<TrilhaEvento> t1 = trilha.listar("t1");
        List<TrilhaEvento> t2 = trilha.listar("t2");
        assertThat(t1).hasSize(1);
        assertThat(t2).hasSize(2);
        // Cada tenant comeca sua propria sequencia em 1, a partir do GENESIS.
        assertThat(t1.get(0).getSeq()).isEqualTo(1);
        assertThat(t2.get(0).getSeq()).isEqualTo(1);
        assertThat(t2.get(0).getHashAnterior()).isEqualTo(TrilhaService.GENESIS);
        assertThat(trilha.verificarIntegridade("t1").integra()).isTrue();
        assertThat(trilha.verificarIntegridade("t2").integra()).isTrue();
    }

    @Test
    void sem_tenant_nao_registra() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> trilha.registrar("TESTE", "{}"));
    }
}
