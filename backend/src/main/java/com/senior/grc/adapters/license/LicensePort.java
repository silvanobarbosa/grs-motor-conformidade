package com.senior.grc.adapters.license;

import java.util.List;
import java.util.Optional;

/**
 * Porta da License (identidade, autenticacao e permissoes funcionais — SPEC.md secao 12).
 * O core cuida de escopo de dados, heranca e segregacao; a License diz QUEM e com QUAIS papeis.
 * Na demo ha uma implementacao mock ({@link MockLicenseAdapter}); em producao, a License real.
 */
public interface LicensePort {

    /** Login simulado: devolve o usuario se as credenciais baterem. */
    Optional<UsuarioLicenca> autenticar(String tenantId, String usuario, String senha);

    List<UsuarioLicenca> usuarios(String tenantId);

    record UsuarioLicenca(String tenantId, String id, String nome, List<String> papeis) {
    }
}
