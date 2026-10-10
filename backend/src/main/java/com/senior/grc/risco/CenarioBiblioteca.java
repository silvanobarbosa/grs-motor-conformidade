package com.senior.grc.risco;

import com.senior.grc.risco.Cenario.Condicao;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Biblioteca inicial de cenarios de seguranca patrimonial (DEMO.md secao 4). Na demo e uma lista
 * fixa; em producao viram configuracao editavel por cliente (editor em camadas). Cada cenario e
 * uma regra declarativa que o {@link RuleEnginePort} interpreta.
 */
@Component
public class CenarioBiblioteca {

    private static final List<Cenario> CENARIOS = List.of(
            // 1. Cracha ativo de colaborador desligado.
            new Cenario("CRACHA_DESLIGADO", "Cracha ativo de colaborador desligado",
                    NivelRisco.CRITICO, Cenario.Escopo.PESSOA,
                    List.of(Condicao.statusIgual("CRACHA", "ATIVO"), Condicao.existe("DESLIGAMENTO"))),

            // 2. Treinamento obrigatorio vencido (ex.: NR-35 venceu -> risco sobe; roteiro R2).
            new Cenario("TREINAMENTO_VENCIDO", "Treinamento obrigatorio vencido",
                    NivelRisco.ALTO, Cenario.Escopo.PESSOA,
                    List.of(Condicao.vencida("TREINAMENTO", "norma"))),

            // 3. Treinamento obrigatorio vencendo em ate 15 dias.
            new Cenario("TREINAMENTO_VENCENDO", "Treinamento obrigatorio vencendo em ate 15 dias",
                    NivelRisco.MEDIO, Cenario.Escopo.PESSOA,
                    List.of(Condicao.venceEmDias("TREINAMENTO", "norma", 15))),

            // 4. Terceiro com documentacao vencida.
            new Cenario("DOC_TERCEIRO_VENCIDO", "Terceiro com documentacao vencida",
                    NivelRisco.ALTO, Cenario.Escopo.EMPRESA,
                    List.of(Condicao.vencida("DOCUMENTO_TERCEIRO", "documento"))),

            // 5. Documento de terceiro lido por OCR com baixa confianca, ainda nao conferido (R4):
            // nao reduz o risco ate a conferencia humana.
            new Cenario("DOC_TERCEIRO_NAO_CONFERIDO", "Documento de terceiro pendente de conferencia",
                    NivelRisco.ALTO, Cenario.Escopo.EMPRESA,
                    List.of(Condicao.naoConferida("DOCUMENTO_TERCEIRO", "documento")))
    );

    public List<Cenario> todos() {
        return CENARIOS;
    }

    public List<Cenario> doEscopo(Cenario.Escopo escopo) {
        return CENARIOS.stream().filter(c -> c.escopo() == escopo).toList();
    }
}
