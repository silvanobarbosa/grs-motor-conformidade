package com.senior.grc.ocr;

import com.senior.grc.fundacao.evidencia.EvidenciaRegistro;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.mocks.MockOcrAdapter;
import com.senior.grc.risco.NivelRisco;
import com.senior.grc.risco.RiscoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Modulo 11: OCR mock + conferencia (R4). Documento valido lido com baixa confianca NAO reduz o
 * risco ate a conferencia; apos conferir, a evidencia vale e o risco cai.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConferenciaServiceTest {

    @Autowired MockOcrAdapter ocr;
    @Autowired ConferenciaService conferencia;
    @Autowired RiscoService risco;
    @Autowired com.senior.grc.demo.RelogioDemo relogio;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM avaliacao_risco");
        jdbc.update("DELETE FROM evidencia_registro");
        jdbc.update("DELETE FROM trilha_evento");
        relogio.reiniciar();
    }

    @Test
    void documento_valido_mas_nao_conferido_mantem_risco_ate_conferencia() {
        TenantContext.definir("t1");
        // ASO valido por 100 dias, porem lido com campo duvidoso -> baixa confianca -> nao conferido.
        ocr.lerDocumentoTerceiro("TERC-OBRA", "ASO", relogio.agora().plus(Duration.ofDays(100)),
                "hash-doc-abc", true);

        // Antes de conferir: pendente, e o risco do terceiro e ALTO (nao conferido).
        List<EvidenciaRegistro> pend = conferencia.pendentes();
        assertThat(pend).hasSize(1);
        assertThat(risco.reavaliarEmpresa("TERC-OBRA").getNivelResidual()).isEqualTo(NivelRisco.ALTO);

        // Confere -> a evidencia vale -> o risco cai.
        conferencia.conferir(pend.get(0).getId(), "tst");
        assertThat(conferencia.pendentes()).isEmpty();
        assertThat(risco.reavaliarEmpresa("TERC-OBRA").getNivelResidual()).isEqualTo(NivelRisco.SEM_RISCO);
    }

    @Test
    void documento_lido_com_alta_confianca_ja_vale() {
        TenantContext.definir("t1");
        ocr.lerDocumentoTerceiro("TERC-VIG", "CND", relogio.agora().plus(Duration.ofDays(100)),
                "hash-doc-def", false);
        assertThat(conferencia.pendentes()).isEmpty();
        assertThat(risco.reavaliarEmpresa("TERC-VIG").getNivelResidual()).isEqualTo(NivelRisco.SEM_RISCO);
    }
}
