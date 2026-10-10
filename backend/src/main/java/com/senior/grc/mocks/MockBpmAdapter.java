package com.senior.grc.mocks;

import org.springframework.stereotype.Component;

/**
 * Mock do BPM: aprova de imediato (fluxo simulado), devolvendo a decisao. Decisoes que exigem
 * justificativa (aceitar risco, marcar nao aplicavel) sao validadas por quem chama.
 */
@Component
public class MockBpmAdapter implements BpmPort {
    @Override
    public Decisao decidir(TipoDecisao tipo, String aprovador, String justificativa, String alvo) {
        return new Decisao(tipo, aprovador, justificativa, alvo);
    }
}
