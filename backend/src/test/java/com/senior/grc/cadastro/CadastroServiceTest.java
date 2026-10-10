package com.senior.grc.cadastro;

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
 * Cofre de identidade e LGPD (SPEC.md secao 4; roteiro R10 da DEMO.md).
 */
@SpringBootTest
@ActiveProfiles("test")
class CadastroServiceTest {

    @Autowired
    CadastroService cadastro;

    @Autowired
    TrilhaService trilha;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM pessoa");
        jdbc.update("DELETE FROM trilha_evento");
    }

    @Test
    void mesmo_cpf_resolve_sempre_para_a_mesma_pessoa() {
        TenantContext.definir("t1");
        Pessoa a = cadastro.resolverOuCriar("11111111111", "Fulano Ficticio", "ELETRICISTA", "PLANTA_NORTE");
        Pessoa b = cadastro.resolverOuCriar("11111111111", "Fulano Ficticio", "ELETRICISTA", "PLANTA_SUL");
        assertThat(a.getId()).isEqualTo(b.getId());
        assertThat(a.getPseudonimo()).isEqualTo(b.getPseudonimo());
        assertThat(b.getLocalCodigo()).isEqualTo("PLANTA_SUL"); // atualizado
    }

    @Test
    void cpf_nunca_aparece_na_trilha_so_pseudonimo() {
        TenantContext.definir("t1");
        String cpf = "22233344455";
        Pessoa p = cadastro.resolverOuCriar(cpf, "Beltrano", "VIGILANTE", "PORTARIA_NORTE");

        List<TrilhaEvento> eventos = trilha.listar("t1");
        assertThat(eventos).isNotEmpty();
        for (TrilhaEvento e : eventos) {
            assertThat(e.getPayloadJson()).doesNotContain(cpf);
            assertThat(e.getPayloadJson()).contains(p.getPseudonimo());
        }
    }

    @Test
    void resolucao_por_cpf_devolve_pseudonimo() {
        TenantContext.definir("t1");
        Pessoa p = cadastro.resolverOuCriar("99988877766", "Ciclano", "TST", "PLANTA_NORTE");
        assertThat(cadastro.pseudonimoPorCpf("99988877766")).contains(p.getPseudonimo());
        assertThat(cadastro.pseudonimoPorCpf("00000000000")).isEmpty();
    }

    @Test
    void eliminacao_lgpd_quebra_identidade_e_mantem_trilha_integra() {
        TenantContext.definir("t1");
        Pessoa p = cadastro.resolverOuCriar("55566677788", "Ex-Colaborador", "OPERADOR", "PRODUCAO_SUL");
        String pseudonimo = p.getPseudonimo();

        cadastro.eliminar(pseudonimo);

        Pessoa depois = cadastro.porPseudonimo(pseudonimo).orElseThrow();
        assertThat(depois.isEliminada()).isTrue();
        assertThat(depois.getNome()).isNull();
        assertThat(depois.getCpfHash()).isNull();
        // Identidade irrecuperavel: o CPF nao casa mais com ninguem.
        assertThat(cadastro.pseudonimoPorCpf("55566677788")).isEmpty();
        // Trilha continua integra (o pseudonimo sobrevive para os eventos historicos).
        assertThat(trilha.verificarIntegridade("t1").integra()).isTrue();
    }
}
