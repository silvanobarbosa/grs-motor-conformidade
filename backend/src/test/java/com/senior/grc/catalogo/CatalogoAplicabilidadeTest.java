package com.senior.grc.catalogo;

import com.senior.grc.fundacao.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Modulo 4: catalogo versionado (consulta por data) e aplicabilidade (obrigatorio / requer
 * confirmacao / nao aplicavel com justificativa).
 */
@SpringBootTest
@ActiveProfiles("test")
class CatalogoAplicabilidadeTest {

    @Autowired CatalogoService catalogo;
    @Autowired AplicabilidadeService aplicabilidade;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        catalogo.semearGlobal();
        aplicabilidade.semearGlobalRegras();
    }

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM aplicabilidade_confirmacao");
        jdbc.update("DELETE FROM aplicabilidade_regra");
        jdbc.update("DELETE FROM requisito_regulatorio");
        jdbc.update("DELETE FROM norma");
        jdbc.update("DELETE FROM trilha_evento");
    }

    @Test
    void consulta_por_data_devolve_a_versao_vigente() {
        var em2025 = catalogo.vigenteEm("NR-35", "35.1", Instant.parse("2025-03-01T00:00:00Z")).orElseThrow();
        var em2026 = catalogo.vigenteEm("NR-35", "35.1", Instant.parse("2026-07-01T00:00:00Z")).orElseThrow();
        assertThat(em2025.getVersao()).isEqualTo(1);
        assertThat(em2026.getVersao()).isEqualTo(2);
        assertThat(em2025.isNaoValidado()).isTrue(); // conteudo de demonstracao
    }

    @Test
    void eletricista_tem_nr10_obrigatoria() {
        TenantContext.definir("t1");
        List<AplicabilidadeService.Sugestao> s = aplicabilidade.sugerirPara("ELETRICISTA", "PLANTA_NORTE");
        assertThat(s).anyMatch(x -> x.normaCodigo().equals("NR-10") && x.resultado() == ResultadoAplicabilidade.OBRIGATORIO);
        assertThat(s).anyMatch(x -> x.normaCodigo().equals("NR-1"));
        assertThat(s).anyMatch(x -> x.normaCodigo().equals("NR-6"));
    }

    @Test
    void nao_aplicavel_exige_justificativa() {
        TenantContext.definir("t1");
        assertThrows(IllegalArgumentException.class, () ->
                aplicabilidade.confirmar("NR-10", "ELETRICISTA", "PLANTA_NORTE",
                        ResultadoAplicabilidade.NAO_APLICAVEL, "  ", "Eng. Fulano", "key-user"));
    }

    @Test
    void nao_aplicavel_de_alta_criticidade_exige_responsavel_tecnico() {
        TenantContext.definir("t1");
        assertThrows(IllegalArgumentException.class, () ->
                aplicabilidade.confirmar("NR-10", "ELETRICISTA", "PLANTA_NORTE",
                        ResultadoAplicabilidade.NAO_APLICAVEL, "justificativa valida", null, "key-user"));
    }

    @Test
    void confirmacao_do_tenant_sobrepoe_o_padrao_global() {
        TenantContext.definir("t1");
        aplicabilidade.confirmar("NR-33", "OPERADOR_PRODUCAO", "PLANTA_NORTE",
                ResultadoAplicabilidade.NAO_APLICAVEL, "nao ha espaco confinado nesta area", "Eng. Ciclano", "key-user");
        assertThat(aplicabilidade.resultadoDe("NR-33", "OPERADOR_PRODUCAO", "PLANTA_NORTE"))
                .contains(ResultadoAplicabilidade.NAO_APLICAVEL);
    }
}
