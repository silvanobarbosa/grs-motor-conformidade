package com.senior.grc.demo;

import com.senior.grc.cadastro.CadastroService;
import com.senior.grc.fundacao.evidencia.EvidenciaRegistro;
import com.senior.grc.fundacao.evidencia.EvidenciaRepository;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.mocks.MockAcessoAdapter;
import com.senior.grc.mocks.MockRhAdapter;
import com.senior.grc.mocks.MockTerceirosAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Modulo 3: mocks emitem evidencia pela porta (contrato unico), simulador de tempo e seed do
 * cliente ficticio.
 */
@SpringBootTest
@ActiveProfiles("test")
class MocksETempoTest {

    @Autowired MockRhAdapter rh;
    @Autowired MockAcessoAdapter acesso;
    @Autowired MockTerceirosAdapter terceiros;
    @Autowired CadastroService cadastro;
    @Autowired EvidenciaRepository evidencias;
    @Autowired RelogioDemo relogio;
    @Autowired SeedDemo seed;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM evidencia_registro");
        jdbc.update("DELETE FROM pessoa");
        jdbc.update("DELETE FROM trilha_evento");
        relogio.reiniciar();
    }

    @Test
    void rh_admitir_cria_pessoa_no_cofre_e_evidencia_pela_porta() {
        TenantContext.definir("t1");
        String pseudonimo = rh.admitir("90000000001", "Ficticio", "ELETRICISTA", "PLANTA_NORTE",
                Map.of("NR-10", relogio.agora().plus(Duration.ofDays(365))));

        assertThat(cadastro.porPseudonimo(pseudonimo)).isPresent();
        List<EvidenciaRegistro> evs = evidencias.findByTenantIdAndPseudonimoPessoa("t1", pseudonimo);
        assertThat(evs).extracting(EvidenciaRegistro::getTipo).contains("ADMISSAO", "TREINAMENTO");
        EvidenciaRegistro treino = evs.stream().filter(e -> e.getTipo().equals("TREINAMENTO")).findFirst().orElseThrow();
        assertThat(treino.atributo("norma")).isEqualTo("NR-10");
        assertThat(treino.getValidoAte()).isNotNull();
    }

    @Test
    void desligar_e_cracha_geram_evidencias_de_tipo_certo() {
        TenantContext.definir("t1");
        String p = rh.admitir("90000000002", "Ficticio", "VIGILANTE", "PORTARIA_NORTE", null);
        acesso.emitirCracha(p);
        rh.desligar(p);

        List<EvidenciaRegistro> evs = evidencias.findByTenantIdAndPseudonimoPessoa("t1", p);
        assertThat(evs).extracting(EvidenciaRegistro::getTipo).contains("ADMISSAO", "CRACHA", "DESLIGAMENTO");
        EvidenciaRegistro cracha = evs.stream().filter(e -> e.getTipo().equals("CRACHA")).findFirst().orElseThrow();
        assertThat(cracha.atributo("status")).isEqualTo("ATIVO");
    }

    @Test
    void terceiro_com_documento_vencido_e_evidencia() {
        TenantContext.definir("t1");
        terceiros.cadastrarEmpresa("TERC-X", "Ficticia", "obras");
        terceiros.registrarDocumento("TERC-X", "ASO", relogio.agora().minus(Duration.ofDays(1)));

        List<EvidenciaRegistro> evs = evidencias.findByTenantIdAndSistemaOrigem("t1", "TERCEIROS");
        assertThat(evs).extracting(EvidenciaRegistro::getTipo).contains("EMPRESA_TERCEIRA", "DOCUMENTO_TERCEIRO");
        EvidenciaRegistro doc = evs.stream().filter(e -> e.getTipo().equals("DOCUMENTO_TERCEIRO")).findFirst().orElseThrow();
        assertThat(doc.getValidoAte()).isBefore(relogio.agora()); // vencido
    }

    @Test
    void relogio_avanca_em_dias() {
        var antes = relogio.agora();
        var depois = relogio.avancarDias(15);
        assertThat(Duration.between(antes, depois).toDays()).isEqualTo(15);
    }

    @Test
    void seed_popula_o_cliente_ficticio() {
        TenantContext.definir("t1");
        SeedDemo.Resumo r = seed.semear(18); // 2 por perfil
        assertThat(r.colaboradores()).isEqualTo(18);
        assertThat(r.terceiras()).isEqualTo(4);
        assertThat(cadastro.listar()).hasSize(18);
        // Um documento de terceiro vencido existe (TERC-OBRA, cenario 3).
        List<EvidenciaRegistro> docs = evidencias.findByTenantIdAndSistemaOrigem("t1", "TERCEIROS");
        assertThat(docs.stream().anyMatch(d -> d.getTipo().equals("DOCUMENTO_TERCEIRO")
                && d.getValidoAte() != null && d.getValidoAte().isBefore(relogio.agora()))).isTrue();
    }
}
