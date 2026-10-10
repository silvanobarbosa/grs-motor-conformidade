package com.senior.grc.fundacao.evidencia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

/**
 * Evidencia no contrato unico de entrada (SPEC.md secoes 3 e 5): quem, o que, quando, de onde,
 * valido ate quando, com que confianca. Toda fonte — inclusive os mocks — entra por aqui; ninguem
 * grava direto no core (CLAUDE.md, regras 1 e 2).
 *
 * Alem do {@code fato} legivel, a evidencia carrega {@code tipo} e {@code atributos} estruturados,
 * que o motor de avaliacao cruza nas regras declarativas (ex.: tipo CRACHA com atributo status=ATIVO
 * contra tipo DESLIGAMENTO da mesma pessoa).
 *
 * @param pseudonimoPessoa pseudonimo (NUNCA o CPF real — regra 4); nulo quando nao e sobre pessoa
 * @param tipo             tipo estruturado (ex.: TREINAMENTO, CRACHA, DOCUMENTO_TERCEIRO, DESLIGAMENTO)
 * @param atributos        atributos estruturados (ex.: {"norma":"NR-35","status":"VALIDO"})
 * @param fato             o fato observavel, legivel
 * @param sistemaOrigem    de onde veio (ex.: "RH", "ACESSO", "TERCEIROS", "OCR")
 * @param confianca        confianca da fonte
 * @param coletadaEm       data da coleta (do relogio da demo)
 * @param validoAte        ate quando a evidencia vale (nulo = sem validade)
 * @param hashDocumento    hash do documento de origem, quando houver
 */
public record Evidencia(
        String pseudonimoPessoa,
        @NotBlank String tipo,
        Map<String, String> atributos,
        @NotBlank String fato,
        @NotBlank String sistemaOrigem,
        @NotNull ConfiancaFonte confianca,
        @NotNull Instant coletadaEm,
        Instant validoAte,
        String hashDocumento
) {
    public Evidencia {
        // Atributos sempre ordenados e imutaveis: o canonico (e o hash) ficam estaveis.
        atributos = atributos == null ? Map.of() : Map.copyOf(new TreeMap<>(atributos));
    }

    public String atributo(String chave) {
        return atributos.get(chave);
    }
}
