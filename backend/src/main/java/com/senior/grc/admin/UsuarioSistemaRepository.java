package com.senior.grc.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, UUID> {

    Optional<UsuarioSistema> findByTenantIdAndLogin(String tenantId, String login);

    List<UsuarioSistema> findByTenantId(String tenantId);

    long countByTenantId(String tenantId);
}
