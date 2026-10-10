package com.senior.grc.demo;

import java.time.Instant;

/**
 * Relogio do sistema. Na demo e um relogio que pode ser AVANCADO (simulador de tempo, DEMO.md
 * secao 1), para mostrar vencimentos e mudancas de risco ao vivo. Em producao, um relogio real.
 * O motor de avaliacao e os mocks tomam "agora" sempre daqui — nunca de {@code Instant.now()} solto.
 */
public interface Relogio {
    Instant agora();
}
