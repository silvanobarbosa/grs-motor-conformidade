package com.senior.grc.mocks;

/**
 * Porta do BPM (fluxos de aprovacao): aprovar politica, aceitar risco, marcar nao aplicavel. Ao core
 * chega a DECISAO (aprovador, justificativa, quando). Mock na demo.
 */
public interface BpmPort {

    enum TipoDecisao { APROVAR_POLITICA, ACEITAR_RISCO, MARCAR_NAO_APLICAVEL }

    Decisao decidir(TipoDecisao tipo, String aprovador, String justificativa, String alvo);

    record Decisao(TipoDecisao tipo, String aprovador, String justificativa, String alvo) {
    }
}
