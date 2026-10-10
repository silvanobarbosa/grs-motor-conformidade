package com.senior.grc.mocks;

import java.time.Instant;
import java.util.Map;

/**
 * Porta do RH (SPEC.md secao 15; DEMO.md): pessoas, cargos, estrutura, treinamentos, admissoes e
 * desligamentos. Na demo, {@link MockRhAdapter}; em producao, o RH da Senior — sem mudar o core.
 * Tudo que o RH "sabe" entra no core como EVIDENCIA, pela porta de entrada (nunca direto no banco).
 */
public interface RhPort {

    /** Admite um colaborador: registra no cofre e emite evidencia de admissao + treinamentos. */
    String admitir(String cpf, String nome, String cargo, String local, Map<String, Instant> treinamentos);

    void registrarTreinamento(String pseudonimo, String norma, Instant validoAte);

    void desligar(String pseudonimo);

    void mudarFuncao(String pseudonimo, String novoCargo, String novoLocal);
}
