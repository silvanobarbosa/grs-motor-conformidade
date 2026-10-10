package com.senior.grc.mocks;

/** Porta do Sign (ciencia e aprovacao): quem, qual versao, quando. Mock na demo. */
public interface SignPort {

    /** Registra a assinatura de ciencia de uma pessoa sobre a versao de uma politica. */
    boolean assinarCiencia(String pseudonimo, String politicaCodigo, int versao);
}
