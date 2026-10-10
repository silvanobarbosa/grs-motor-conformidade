package com.senior.grc.fundacao.evidencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EvidenciaRepository extends JpaRepository<EvidenciaRegistro, UUID> {

    List<EvidenciaRegistro> findByTenantId(String tenantId);

    List<EvidenciaRegistro> findByTenantIdAndPseudonimoPessoa(String tenantId, String pseudonimoPessoa);

    List<EvidenciaRegistro> findByTenantIdAndSistemaOrigem(String tenantId, String sistemaOrigem);

    List<EvidenciaRegistro> findByTenantIdAndConferidaFalse(String tenantId);

    long countByTenantId(String tenantId);
}
