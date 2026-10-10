package com.senior.grc.mocks;

import com.senior.grc.fundacao.trilha.Hashing;
import org.springframework.stereotype.Component;

/** Mock do GED: o texto fica "no GED"; devolve um hash deterministico da versao. */
@Component
public class MockGedAdapter implements GedPort {
    @Override
    public String hashDaVersao(String politicaCodigo, int versao) {
        return Hashing.sha256("ged|" + politicaCodigo + "|v" + versao);
    }
}
