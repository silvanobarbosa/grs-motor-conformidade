package com.senior.grc.adapters.license;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mock da License para a demo: usuarios e papeis ficticios do cliente "Metalurgica Exemplo S.A."
 * (DEMO.md secao 2). Login simulado — a senha da demo e a mesma para todos. Nada real.
 *
 * Em producao, trocar por um adaptador que fala com a License da Senior, SEM mudar o core (regra 2).
 */
@Component
public class MockLicenseAdapter implements LicensePort {

    public static final String TENANT_DEMO = "metalurgica-exemplo";
    private static final String SENHA_DEMO = "Demo@2026";

    private static final List<UsuarioLicenca> USUARIOS_DEMO = List.of(
            new UsuarioLicenca(TENANT_DEMO, "U-001", "Key User (Gestor de Riscos da Matriz)", List.of("KEY_USER", "GESTOR_DE_RISCOS")),
            new UsuarioLicenca(TENANT_DEMO, "U-002", "Gestor de Seguranca Patrimonial", List.of("GESTOR_SEGURANCA")),
            new UsuarioLicenca(TENANT_DEMO, "U-003", "Gestor de Planta - Norte", List.of("GESTOR_PLANTA")),
            new UsuarioLicenca(TENANT_DEMO, "U-004", "Tecnico de Seguranca do Trabalho", List.of("TST")),
            new UsuarioLicenca(TENANT_DEMO, "U-005", "Analista de RH", List.of("RH")),
            new UsuarioLicenca(TENANT_DEMO, "U-006", "Auditor Externo (acesso temporario)", List.of("AUDITOR_EXTERNO"))
    );

    private final Map<String, List<UsuarioLicenca>> porTenant = Map.of(TENANT_DEMO, USUARIOS_DEMO);

    @Override
    public Optional<UsuarioLicenca> autenticar(String tenantId, String usuario, String senha) {
        if (!SENHA_DEMO.equals(senha)) {
            return Optional.empty();
        }
        return usuarios(tenantId).stream()
                .filter(u -> u.id().equalsIgnoreCase(usuario) || u.nome().equalsIgnoreCase(usuario))
                .findFirst();
    }

    @Override
    public List<UsuarioLicenca> usuarios(String tenantId) {
        return porTenant.getOrDefault(tenantId, List.of());
    }
}
