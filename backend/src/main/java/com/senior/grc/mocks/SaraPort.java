package com.senior.grc.mocks;

import java.util.List;

/**
 * Porta da Sara (gateway de IA da Senior). Na demo, respostas DETERMINISTICAS, com confianca e
 * registro de modelo/prompt. A IA sugere, extrai e explica — NUNCA decide (CLAUDE.md regra 8).
 */
public interface SaraPort {

    /** Triagem de um relato em texto livre: extrai local, data e pessoas (pseudonimos) — sugestao. */
    Triagem triar(String textoLivre);

    /** Explicacao em linguagem natural de um texto base (ex.: a explicacao da avaliacao de risco). */
    String explicar(String base);

    String modelo();

    record Triagem(String local, String data, List<String> pessoas, double confianca, String modelo, String prompt) {
    }
}
