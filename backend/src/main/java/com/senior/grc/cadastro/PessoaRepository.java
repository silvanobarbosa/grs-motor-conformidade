package com.senior.grc.cadastro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {

    Optional<Pessoa> findByTenantIdAndCpfHash(String tenantId, String cpfHash);

    Optional<Pessoa> findByTenantIdAndPseudonimo(String tenantId, String pseudonimo);

    List<Pessoa> findByTenantId(String tenantId);

    boolean existsByTenantIdAndCargoCodigoAndLocalCodigoAndEliminadaFalse(String tenantId, String cargoCodigo, String localCodigo);

    boolean existsByTenantIdAndCargoCodigoAndEliminadaFalse(String tenantId, String cargoCodigo);

    List<Pessoa> findByTenantIdAndCargoCodigoAndEliminadaFalse(String tenantId, String cargoCodigo);

    List<Pessoa> findByTenantIdAndCargoCodigoAndLocalCodigoAndEliminadaFalse(String tenantId, String cargoCodigo, String localCodigo);
}
