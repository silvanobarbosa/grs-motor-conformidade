package com.senior.grc.mocks;

import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.evidencia.ConfiancaFonte;
import com.senior.grc.fundacao.evidencia.Evidencia;
import com.senior.grc.fundacao.evidencia.PortaDeEntrada;
import com.senior.grc.risco.Cenario;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mock do gestor de ocorrencias. Abrir e so um aceite (o sistema externo recebeu o plano). Concluir
 * emite a evidencia ACAO_CONCLUIDA pela porta de entrada — a conclusao volta ao core como evidencia
 * (ciclo fechado, R3).
 */
@Component
public class MockOcorrenciasAdapter implements OcorrenciasPort {

    public static final String SISTEMA = "OCORRENCIAS";

    private final PortaDeEntrada porta;
    private final Relogio relogio;

    public MockOcorrenciasAdapter(PortaDeEntrada porta, Relogio relogio) {
        this.porta = porta;
        this.relogio = relogio;
    }

    @Override
    public void abrirPlano(String sujeitoId, Cenario.Escopo escopo, String causa, String descricao) {
        // No-op de integracao: o gestor de ocorrencias recebeu o plano. (Sem evidencia: abrir plano
        // nao e um fato de risco; a abertura e rastreada na trilha pelo modulo de acoes.)
    }

    @Override
    public void concluirPlano(String sujeitoId, Cenario.Escopo escopo, String causa) {
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("causa", causa);
        String pseudonimo = escopo == Cenario.Escopo.PESSOA ? sujeitoId : null;
        if (escopo == Cenario.Escopo.EMPRESA) {
            attrs.put("empresa", sujeitoId);
        }
        porta.receber(new Evidencia(pseudonimo, "ACAO_CONCLUIDA", attrs,
                "plano de acao concluido para a causa " + causa, SISTEMA,
                ConfiancaFonte.ALTA, relogio.agora(), null, null));
    }
}
