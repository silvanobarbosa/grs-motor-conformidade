package com.senior.grc.risco;

import com.senior.grc.fundacao.evidencia.EvidenciaRegistro;

import java.util.List;
import java.util.Optional;

/**
 * Motor de regras (SPEC.md secao 13). Na demo e o {@link AvaliadorSimples}; em producao, o motor da
 * Senior — trocavel sem mudar o resto do core (CLAUDE.md regra 2 / stack).
 *
 * Avalia um cenario contra o estado de evidencia de um sujeito. Se o cenario DISPARA, devolve o
 * gatilho: as evidencias que o acionaram (para a explicacao "qual evidencia, de qual fonte, em qual
 * regra") e se o disparo se apoia SO em evidencia de baixa confianca (trava: nao vai a CRITICO).
 */
public interface RuleEnginePort {

    Optional<Gatilho> avaliar(Cenario cenario, EstadoDeEvidencia estado);

    record Gatilho(List<EvidenciaRegistro> evidencias, String explicacao, boolean apenasBaixaConfianca) {
    }
}
