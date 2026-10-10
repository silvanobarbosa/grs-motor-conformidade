package com.senior.grc.permissoes;

import java.util.List;
import java.util.Set;

/**
 * Contexto de quem acessa: papeis (vindos da License), locais do escopo, e o que a ORIGEM herda.
 *
 * {@code origemNega}: locais onde o sistema de origem NEGA explicitamente (heranca so restringe).
 * {@code origemSincronizada}: se false, nao houve sincronizacao de permissoes com a origem — falha
 * segura: categorias sensiveis ficam bloqueadas (SPEC.md secao 12).
 */
public record ContextoAcesso(
        String usuarioId,
        Set<String> papeis,
        List<String> locaisPermitidos,
        Set<String> origemNega,
        boolean origemSincronizada
) {
    public boolean temPapel(String papel) {
        return papeis.contains(papel);
    }
}
