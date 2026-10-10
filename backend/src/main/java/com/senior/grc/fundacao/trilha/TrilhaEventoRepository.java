package com.senior.grc.fundacao.trilha;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio da trilha. So leitura e insercao sao usadas pelo core; nao ha metodo de edicao nem
 * delecao exposto no servico. (O {@code delete*} herdado do JpaRepository existe mas nao e chamado
 * em lugar nenhum do codigo — a regra de imutabilidade e garantida pelo servico e pelos testes.)
 */
public interface TrilhaEventoRepository extends JpaRepository<TrilhaEvento, UUID> {

    Optional<TrilhaEvento> findFirstByTenantIdOrderBySeqDesc(String tenantId);

    List<TrilhaEvento> findByTenantIdOrderBySeqAsc(String tenantId);

    long countByTenantId(String tenantId);
}
