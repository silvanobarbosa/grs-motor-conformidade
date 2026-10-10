package com.senior.grc.governanca;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Tres repositorios top-level da governanca.
interface PoliticaRepository extends JpaRepository<Politica, UUID> {
    Optional<Politica> findByTenantIdAndCodigo(String tenantId, String codigo);

    List<Politica> findByTenantId(String tenantId);
}

interface CienciaRepository extends JpaRepository<CienciaRegistro, UUID> {
    List<CienciaRegistro> findByTenantIdAndPoliticaCodigoAndVersao(String tenantId, String politicaCodigo, int versao);
}

interface ExcecaoRiscoRepository extends JpaRepository<ExcecaoRisco, UUID> {
    Optional<ExcecaoRisco> findByTenantIdAndSujeitoIdAndCausa(String tenantId, String sujeitoId, String causa);
}
