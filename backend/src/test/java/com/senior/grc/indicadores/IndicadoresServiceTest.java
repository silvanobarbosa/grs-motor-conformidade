package com.senior.grc.indicadores;

import com.senior.grc.catalogo.AplicabilidadeService;
import com.senior.grc.catalogo.CatalogoService;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.mocks.MockRhAdapter;
import com.senior.grc.mocks.MockTerceirosAdapter;
import com.senior.grc.risco.NivelRisco;
import com.senior.grc.risco.RiscoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Modulo 9: indicadores (operacional, gestao, executivo, atualidade, cobertura) — R8. */
@SpringBootTest
@ActiveProfiles("test")
class IndicadoresServiceTest {

    @Autowired IndicadoresService ind;
    @Autowired MockRhAdapter rh;
    @Autowired MockTerceirosAdapter terceiros;
    @Autowired RiscoService risco;
    @Autowired CatalogoService catalogo;
    @Autowired AplicabilidadeService aplicabilidade;
    @Autowired com.senior.grc.demo.RelogioDemo relogio;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM avaliacao_risco");
        jdbc.update("DELETE FROM evidencia_registro");
        jdbc.update("DELETE FROM pessoa");
        jdbc.update("DELETE FROM aplicabilidade_regra");
        jdbc.update("DELETE FROM requisito_regulatorio");
        jdbc.update("DELETE FROM norma");
        jdbc.update("DELETE FROM trilha_evento");
        relogio.reiniciar();
    }

    @Test
    void operacional_lista_vencimentos_proximos() {
        TenantContext.definir("t1");
        rh.admitir("90000000300", "A", "TECNICO_MANUTENCAO", "PLANTA_SUL",
                Map.of("NR-35", relogio.agora().plus(Duration.ofDays(10)))); // vence em 10 dias
        rh.admitir("90000000301", "B", "ELETRICISTA", "PLANTA_NORTE",
                Map.of("NR-10", relogio.agora().plus(Duration.ofDays(200)))); // longe

        var venc = ind.operacionalVencimentos(15);
        assertThat(venc).hasSize(1);
        assertThat(venc.get(0).chave()).isEqualTo("NR-35");
    }

    @Test
    void gestao_conta_residual_por_unidade() {
        TenantContext.definir("t1");
        String p = rh.admitir("90000000302", "C", "TECNICO_MANUTENCAO", "PLANTA_SUL",
                Map.of("NR-35", relogio.agora().minus(Duration.ofDays(1)))); // vencido -> ALTO
        risco.reavaliarPessoa(p);

        Map<String, Map<NivelRisco, Long>> g = ind.gestaoResidualPorUnidade();
        assertThat(g).containsKey("PLANTA_SUL");
        assertThat(g.get("PLANTA_SUL").get(NivelRisco.ALTO)).isEqualTo(1L);
    }

    @Test
    void atualidade_traz_o_ultimo_envio_por_fonte() {
        TenantContext.definir("t1");
        rh.admitir("90000000303", "D", "VIGILANTE", "PORTARIA_NORTE", null);
        terceiros.cadastrarEmpresa("TERC-X", "F", "obras");

        Map<String, java.time.Instant> at = ind.atualidadePorFonte();
        assertThat(at).containsKeys("RH", "TERCEIROS");
    }

    @Test
    void cobertura_mede_requisitos_obrigatorios_com_evidencia() {
        TenantContext.definir("t1");
        catalogo.semearGlobal();
        aplicabilidade.semearGlobalRegras();
        // Eletricista COM NR-10 valida + NR-1/NR-6 obrigatorias sem evidencia.
        rh.admitir("90000000304", "E", "ELETRICISTA", "PLANTA_NORTE",
                Map.of("NR-10", relogio.agora().plus(Duration.ofDays(365))));

        Map<String, Object> cob = ind.cobertura();
        // 3 obrigatorios (NR-1, NR-6, NR-10); 1 coberto (NR-10).
        assertThat(cob.get("requisitosObrigatorios")).isEqualTo(3L);
        assertThat(cob.get("comEvidenciaValida")).isEqualTo(1L);
    }
}
