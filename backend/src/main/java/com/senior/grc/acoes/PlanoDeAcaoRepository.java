package com.senior.grc.acoes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanoDeAcaoRepository extends JpaRepository<PlanoDeAcao, UUID> {

    Optional<PlanoDeAcao> findByTenantIdAndSujeitoIdAndCausaAndStatus(
            String tenantId, String sujeitoId, String causa, PlanoDeAcao.Status status);

    List<PlanoDeAcao> findByTenantId(String tenantId);
}
