package com.senior.grc.mocks;

import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.evidencia.ConfiancaFonte;
import com.senior.grc.fundacao.evidencia.Evidencia;
import com.senior.grc.fundacao.evidencia.PortaDeEntrada;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mock do OCR: le o documento e emite UMA evidencia pela porta, com o hash do documento e a versao
 * do motor. Campo duvidoso -> confianca BAIXA -> a evidencia entra nao conferida (R4).
 */
@Component
public class MockOcrAdapter implements OcrPort {

    public static final String SISTEMA = "OCR";
    public static final String VERSAO_MOTOR = "ocr-demo-1.0";

    private final PortaDeEntrada porta;
    private final Relogio relogio;

    public MockOcrAdapter(PortaDeEntrada porta, Relogio relogio) {
        this.porta = porta;
        this.relogio = relogio;
    }

    @Override
    public void lerDocumentoTerceiro(String empresaCodigo, String tipoDocumento, Instant validoAte,
                                     String hashDocumento, boolean campoDuvidoso) {
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("empresa", empresaCodigo);
        attrs.put("documento", tipoDocumento);
        attrs.put("status", "VALIDO");
        attrs.put("motorOcr", VERSAO_MOTOR);
        ConfiancaFonte confianca = campoDuvidoso ? ConfiancaFonte.BAIXA : ConfiancaFonte.ALTA;
        porta.receber(new Evidencia(null, "DOCUMENTO_TERCEIRO", attrs,
                "documento " + tipoDocumento + " da empresa " + empresaCodigo + " lido por OCR",
                SISTEMA, confianca, relogio.agora(), validoAte, hashDocumento));
    }
}
