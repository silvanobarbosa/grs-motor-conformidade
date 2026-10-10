package com.senior.grc.catalogo;

/**
 * Resultado da aplicabilidade de uma norma (SPEC.md secao 9): obrigatorio, requer confirmacao do
 * key user, ou nao aplicavel (sempre com justificativa). Na duvida, REQUER_CONFIRMACAO.
 */
public enum ResultadoAplicabilidade {
    OBRIGATORIO,
    REQUER_CONFIRMACAO,
    NAO_APLICAVEL
}
