package com.senior.grc.fundacao.evidencia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Evidencia no contrato unico de entrada (SPEC.md secoes 3 e 5): quem, o que, quando, de onde,
 * valido ate quando, com que confianca. Toda fonte — inclusive os mocks — entra por aqui; ninguem
 * grava direto no core (CLAUDE.md, regras 1 e 2).
 *
 * @param pseudonimoPessoa pseudonimo da pessoa (NUNCA o CPF real — regra 4; o CPF vive so no cofre
 *                         de identidade). Pode ser nulo quando a evidencia nao e sobre uma pessoa.
 * @param fato             o fato observavel (ex.: "treinamento NR-35 valido ate 2026-11-30")
 * @param sistemaOrigem    de onde veio (ex.: "RH", "ACESSO", "TERCEIROS", "OCR")
 * @param confianca        confianca da fonte
 * @param coletadaEm       data da coleta
 * @param validoAte        ate quando a evidencia vale (nulo = sem validade definida)
 * @param hashDocumento    hash do documento de origem, quando houver (ex.: ASO lido por OCR)
 */
public record Evidencia(
        String pseudonimoPessoa,
        @NotBlank String fato,
        @NotBlank String sistemaOrigem,
        @NotNull ConfiancaFonte confianca,
        @NotNull Instant coletadaEm,
        Instant validoAte,
        String hashDocumento
) {
}
