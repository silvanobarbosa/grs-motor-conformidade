package com.senior.grc.risco;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoRiscoRepository extends JpaRepository<AvaliacaoRisco, UUID> {

    Optional<AvaliacaoRisco> findByTenantIdAndSujeitoId(String tenantId, String sujeitoId);

    List<AvaliacaoRisco> findByTenantId(String tenantId);
}
