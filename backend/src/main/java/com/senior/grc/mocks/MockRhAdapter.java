package com.senior.grc.mocks;

import com.senior.grc.cadastro.CadastroService;
import com.senior.grc.cadastro.Pessoa;
import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.evidencia.ConfiancaFonte;
import com.senior.grc.fundacao.evidencia.Evidencia;
import com.senior.grc.fundacao.evidencia.PortaDeEntrada;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mock do RH. Toda informacao vira evidencia pela porta de entrada (contrato unico). O RH e fonte
 * de ALTA confianca (sistema de registro do cliente).
 */
@Component
public class MockRhAdapter implements RhPort {

    public static final String SISTEMA = "RH";

    private final PortaDeEntrada porta;
    private final CadastroService cadastro;
    private final Relogio relogio;

    public MockRhAdapter(PortaDeEntrada porta, CadastroService cadastro, Relogio relogio) {
        this.porta = porta;
        this.cadastro = cadastro;
        this.relogio = relogio;
    }

    @Override
    public String admitir(String cpf, String nome, String cargo, String local, Map<String, Instant> treinamentos) {
        Pessoa p = cadastro.resolverOuCriar(cpf, nome, cargo, local);
        String pseudonimo = p.getPseudonimo();
        porta.receber(new Evidencia(pseudonimo, "ADMISSAO",
                Map.of("cargo", cargo, "local", local, "status", "ATIVO"),
                "colaborador admitido como " + cargo + " em " + local, SISTEMA,
                ConfiancaFonte.ALTA, relogio.agora(), null, null));
        if (treinamentos != null) {
            treinamentos.forEach((norma, validoAte) -> registrarTreinamento(pseudonimo, norma, validoAte));
        }
        return pseudonimo;
    }

    @Override
    public void registrarTreinamento(String pseudonimo, String norma, Instant validoAte) {
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("norma", norma);
        attrs.put("status", "VALIDO");
        porta.receber(new Evidencia(pseudonimo, "TREINAMENTO", attrs,
                "treinamento " + norma + " valido ate " + validoAte, SISTEMA,
                ConfiancaFonte.ALTA, relogio.agora(), validoAte, null));
    }

    @Override
    public void desligar(String pseudonimo) {
        porta.receber(new Evidencia(pseudonimo, "DESLIGAMENTO", Map.of("status", "DESLIGADO"),
                "colaborador desligado", SISTEMA, ConfiancaFonte.ALTA, relogio.agora(), null, null));
    }

    @Override
    public void mudarFuncao(String pseudonimo, String novoCargo, String novoLocal) {
        cadastro.porPseudonimo(pseudonimo).ifPresent(p -> {
            porta.receber(new Evidencia(pseudonimo, "MUDANCA_FUNCAO",
                    Map.of("cargo", novoCargo, "local", novoLocal),
                    "mudanca de funcao para " + novoCargo + " em " + novoLocal, SISTEMA,
                    ConfiancaFonte.ALTA, relogio.agora(), null, null));
        });
    }
}
