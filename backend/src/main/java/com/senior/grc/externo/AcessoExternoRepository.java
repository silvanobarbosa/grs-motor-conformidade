package com.senior.grc.externo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AcessoExternoRepository extends JpaRepository<AcessoExterno, UUID> {
    List<AcessoExterno> findByTenantIdAndAuditorId(String tenantId, String auditorId);
}
