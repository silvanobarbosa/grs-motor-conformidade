package com.senior.grc.risco;

/** Nivel de risco, do menor para o maior. {@code SEM_RISCO} = nenhum cenario disparou. */
public enum NivelRisco {
    SEM_RISCO,
    BAIXO,
    MEDIO,
    ALTO,
    CRITICO;

    public boolean maiorQue(NivelRisco outro) {
        return this.ordinal() > outro.ordinal();
    }

    /** Trava de confianca (SPEC.md 6): evidencia de baixa confianca nao leva a CRITICO sozinha. */
    public NivelRisco limitadoSemAltaConfianca() {
        return this == CRITICO ? ALTO : this;
    }
}
