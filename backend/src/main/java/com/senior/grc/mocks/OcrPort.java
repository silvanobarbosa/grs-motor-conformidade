package com.senior.grc.mocks;

import java.time.Instant;

/**
 * Porta do OCR de mercado (o mesmo da gestao de terceiros). Uma leitura, uma evidencia; retorno com
 * confianca, hash do documento e versao do motor. Baixa confianca vai para conferencia (SPEC.md 15).
 */
public interface OcrPort {

    /**
     * Le um documento de terceiro. {@code campoDuvidoso=true} simula um campo com baixa confianca:
     * a evidencia entra NAO conferida e nao reduz o risco ate a conferencia humana.
     */
    void lerDocumentoTerceiro(String empresaCodigo, String tipoDocumento, Instant validoAte,
                              String hashDocumento, boolean campoDuvidoso);
}
