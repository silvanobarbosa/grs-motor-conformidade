package com.senior.grc.fundacao;

import com.senior.grc.fundacao.evidencia.ConfiancaFonte;
import com.senior.grc.fundacao.evidencia.Evidencia;
import com.senior.grc.fundacao.evidencia.EvidenciaInvalidaException;
import com.senior.grc.fundacao.evidencia.PortaDeEntrada;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaEvento;
import com.senior.grc.fundacao.trilha.TrilhaService;
import org.junit.jupiter.api.AfterEach;
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
 * Contrato unico de entrada (CLAUDE.md regras 1 e 4): toda evidencia entra pela porta e vira evento
 * na trilha; dado pessoal cru (CPF) e recusado.
 */
@SpringBootTest
@ActiveProfiles("test")
class PortaDeEntradaTest {

    @Autowired
    PortaDeEntrada porta;

    @Autowired
    TrilhaService trilha;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM evidencia_registro");
        jdbc.update("DELETE FROM trilha_evento");
    }

    private Evidencia valida() {
        return new Evidencia("P-123", "TREINAMENTO", java.util.Map.of("norma", "NR-35"),
                "treinamento NR-35 valido ate 2026-11-30", "RH",
                ConfiancaFonte.ALTA, Instant.parse("2026-10-10T12:00:00Z"),
                Instant.parse("2026-11-30T00:00:00Z"), null);
    }

    @Test
    void evidencia_valida_vira_evento_na_trilha() {
        TenantContext.definir("t1");
        porta.receber(valida());

        List<TrilhaEvento> eventos = trilha.listar("t1");
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).getTipo()).isEqualTo(PortaDeEntrada.TIPO_EVENTO);
        assertThat(eventos.get(0).getPayloadJson()).contains("NR-35").contains("\"sistemaOrigem\":\"RH\"");
        assertThat(trilha.verificarIntegridade("t1").integra()).isTrue();
    }

    @Test
    void recusa_evidencia_sem_fato() {
        TenantContext.definir("t1");
        Evidencia semFato = new Evidencia("P-1", "TREINAMENTO", null, "  ", "RH", ConfiancaFonte.MEDIA, Instant.now(), null, null);
        assertThrows(EvidenciaInvalidaException.class, () -> porta.receber(semFato));
    }

    @Test
    void recusa_cpf_no_pseudonimo() {
        TenantContext.definir("t1");
        Evidencia comCpf = new Evidencia("123.456.789-00", "TESTE", null, "fato qualquer", "RH",
                ConfiancaFonte.ALTA, Instant.now(), null, null);
        assertThrows(EvidenciaInvalidaException.class, () -> porta.receber(comCpf));
    }

    @Test
    void recusa_cpf_no_fato() {
        TenantContext.definir("t1");
        Evidencia comCpf = new Evidencia("P-1", "TESTE", null, "pessoa 12345678900 sem treinamento", "RH",
                ConfiancaFonte.ALTA, Instant.now(), null, null);
        assertThrows(EvidenciaInvalidaException.class, () -> porta.receber(comCpf));
    }

    @Test
    void payload_canonico_e_deterministico() {
        String a = PortaDeEntrada.canonico(valida());
        String b = PortaDeEntrada.canonico(valida());
        assertThat(a).isEqualTo(b);
        assertThat(a).startsWith("{\"pseudonimoPessoa\":\"P-123\"");
    }
}
