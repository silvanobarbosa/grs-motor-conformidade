package com.senior.grc.fundacao.evidencia;

/**
 * Nivel de confianca da fonte da evidencia (SPEC.md secao 5 e trava 1 da secao 6).
 * Evidencia de BAIXA confianca nao leva o risco a critico sozinha — a trava vive no motor de
 * avaliacao (modulo posterior); aqui so classificamos.
 */
public enum ConfiancaFonte {
    ALTA,
    MEDIA,
    BAIXA
}
