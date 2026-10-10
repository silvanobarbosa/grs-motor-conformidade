package com.senior.grc.demo;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Relogio da demonstracao: comeca numa data fixa e pode ser avancado em dias pela tela de
 * administracao. Assim um roteiro mostra "o treinamento vence" e o risco sobe ao vivo (R2).
 */
@Component
public class RelogioDemo implements Relogio {

    /** Data de partida da demo (ficticia, estavel). */
    public static final Instant INICIO = Instant.parse("2026-10-01T09:00:00Z");

    private final AtomicReference<Instant> agora = new AtomicReference<>(INICIO);

    @Override
    public Instant agora() {
        return agora.get();
    }

    public Instant avancarDias(long dias) {
        return agora.updateAndGet(a -> a.plus(Duration.ofDays(dias)));
    }

    public void reiniciar() {
        agora.set(INICIO);
    }
}
