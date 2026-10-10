package com.senior.grc.risco;

import java.util.List;

/**
 * Cenario de risco como REGRA DECLARATIVA (SPEC.md secao 8): evidencias cruzadas + condicao +
 * criticidade. O avaliador interpreta as condicoes (todas verdadeiras = cenario dispara). O escopo
 * diz se o sujeito avaliado e uma PESSOA ou uma EMPRESA terceira.
 */
public record Cenario(String codigo, String nome, NivelRisco nivel, Escopo escopo, List<Condicao> condicoes) {

    public enum Escopo { PESSOA, EMPRESA }

    /** Modo de cada condicao sobre o estado de evidencia do sujeito. */
    public enum Modo {
        /** Existe ao menos uma evidencia do tipo. */
        EXISTE,
        /** A evidencia mais recente do tipo tem atributo "status" igual ao valor. */
        STATUS_IGUAL,
        /** Agrupando por {@code chave}, a evidencia mais recente de algum grupo esta VENCIDA (validoAte < agora). */
        VENCIDA,
        /** Agrupando por {@code chave}, a mais recente de algum grupo vence em ate {@code valor} dias. */
        VENCE_EM_DIAS
    }

    /**
     * @param tipo  tipo da evidencia (ex.: CRACHA, DESLIGAMENTO, TREINAMENTO, DOCUMENTO_TERCEIRO)
     * @param modo  como avaliar
     * @param chave atributo de agrupamento/comparacao (ex.: "norma", "documento", "status")
     * @param valor valor de comparacao (status igual, ou nº de dias)
     */
    public record Condicao(String tipo, Modo modo, String chave, String valor) {
        public static Condicao existe(String tipo) {
            return new Condicao(tipo, Modo.EXISTE, null, null);
        }

        public static Condicao statusIgual(String tipo, String status) {
            return new Condicao(tipo, Modo.STATUS_IGUAL, "status", status);
        }

        public static Condicao vencida(String tipo, String chaveGrupo) {
            return new Condicao(tipo, Modo.VENCIDA, chaveGrupo, null);
        }

        public static Condicao venceEmDias(String tipo, String chaveGrupo, int dias) {
            return new Condicao(tipo, Modo.VENCE_EM_DIAS, chaveGrupo, String.valueOf(dias));
        }
    }
}
