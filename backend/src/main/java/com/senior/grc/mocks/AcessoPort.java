package com.senior.grc.mocks;

/**
 * Porta do controle de acesso (crachas, areas, registros de entrada). Mock na demo.
 * Alimenta cenarios como "cracha ativo de desligado" e "acesso a area restrita sem treinamento".
 */
public interface AcessoPort {

    void emitirCracha(String pseudonimo);

    void bloquearCracha(String pseudonimo);

    void registrarEntrada(String pseudonimo, String area);
}
