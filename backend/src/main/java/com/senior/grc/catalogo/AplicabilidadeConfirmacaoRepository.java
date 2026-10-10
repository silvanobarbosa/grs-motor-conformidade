package com.senior.grc.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AplicabilidadeConfirmacaoRepository extends JpaRepository<AplicabilidadeConfirmacao, UUID> {

    Optional<AplicabilidadeConfirmacao> findByTenantIdAndNormaCodigoAndCargoAndLocalCodigo(
            String tenantId, String normaCodigo, String cargo, String localCodigo);

    List<AplicabilidadeConfirmacao> findByTenantId(String tenantId);
}
