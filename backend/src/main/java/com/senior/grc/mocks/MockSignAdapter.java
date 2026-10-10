package com.senior.grc.mocks;

import org.springframework.stereotype.Component;

/** Mock do Sign: a assinatura de ciencia "acontece" e e confirmada. */
@Component
public class MockSignAdapter implements SignPort {
    @Override
    public boolean assinarCiencia(String pseudonimo, String politicaCodigo, int versao) {
        return true;
    }
}
