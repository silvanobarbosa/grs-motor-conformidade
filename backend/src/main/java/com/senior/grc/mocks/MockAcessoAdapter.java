package com.senior.grc.mocks;

import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.evidencia.ConfiancaFonte;
import com.senior.grc.fundacao.evidencia.Evidencia;
import com.senior.grc.fundacao.evidencia.PortaDeEntrada;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Mock do controle de acesso. Emite evidencia de cracha e de entrada pela porta de entrada. */
@Component
public class MockAcessoAdapter implements AcessoPort {

    public static final String SISTEMA = "ACESSO";

    private final PortaDeEntrada porta;
    private final Relogio relogio;

    public MockAcessoAdapter(PortaDeEntrada porta, Relogio relogio) {
        this.porta = porta;
        this.relogio = relogio;
    }

    @Override
    public void emitirCracha(String pseudonimo) {
        porta.receber(new Evidencia(pseudonimo, "CRACHA", Map.of("status", "ATIVO"),
                "cracha emitido/ativo", SISTEMA, ConfiancaFonte.ALTA, relogio.agora(), null, null));
    }

    @Override
    public void bloquearCracha(String pseudonimo) {
        porta.receber(new Evidencia(pseudonimo, "CRACHA", Map.of("status", "BLOQUEADO"),
                "cracha bloqueado", SISTEMA, ConfiancaFonte.ALTA, relogio.agora(), null, null));
    }

    @Override
    public void registrarEntrada(String pseudonimo, String area) {
        porta.receber(new Evidencia(pseudonimo, "ACESSO", Map.of("area", area),
                "entrada registrada na area " + area, SISTEMA, ConfiancaFonte.ALTA, relogio.agora(), null, null));
    }
}
