package com.senior.grc.fundacao.evidencia;

/** Evidencia recusada na porta de entrada (dado faltando, ou dado pessoal cru). */
public class EvidenciaInvalidaException extends RuntimeException {
    public EvidenciaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
