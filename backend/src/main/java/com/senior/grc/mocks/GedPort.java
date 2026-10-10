package com.senior.grc.mocks;

/** Porta do GED (texto e versoes das politicas). Ao core chega ID + hash da versao (SPEC.md 10). */
public interface GedPort {
    String hashDaVersao(String politicaCodigo, int versao);
}
