package com.senior.grc.mocks;

import com.senior.grc.risco.Cenario;

/**
 * Porta do gestor de ocorrencias (execucao de planos). Mock na demo. A CONCLUSAO de um plano volta
 * ao core como EVIDENCIA, pela porta de entrada (ciclo fechado — SPEC.md secao 7, roteiro R3).
 */
public interface OcorrenciasPort {

    void abrirPlano(String sujeitoId, Cenario.Escopo escopo, String causa, String descricao);

    void concluirPlano(String sujeitoId, Cenario.Escopo escopo, String causa);
}
