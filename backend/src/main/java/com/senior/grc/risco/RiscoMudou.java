package com.senior.grc.risco;

/**
 * Evento de dominio: o risco residual de um sujeito mudou de nivel. Publicado pelo RiscoService e
 * consumido pelo modulo de acoes (abre/atualiza plano) — desacopla risco de acoes, sem dependencia
 * circular. Carrega o tenant porque o listener roda no mesmo thread/transacao.
 */
public record RiscoMudou(String tenantId, String sujeitoId, Cenario.Escopo escopo,
                         NivelRisco de, NivelRisco para, String cenarioDominante) {
}
