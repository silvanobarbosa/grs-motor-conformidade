package com.senior.grc.externo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Acesso externo (auditor, fiscal) — SPEC.md secao 10: concedido pelo key user, com escopo,
 * temporario, SO LEITURA, pseudonimizado e auditado. Expira em {@code validoAte}.
 */
@Entity
@Table(name = "acesso_externo")
public class AcessoExterno {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "auditor_id", nullable = false)
    private String auditorId;

    /** Escopo do acesso (ex.: "NR-35" ou "NR-35|PLANTA_SUL"). */
    @Column(nullable = false)
    private String escopo;

    @Column(name = "concedido_por", nullable = false)
    private String concedidoPor;

    @Column(name = "concedido_em", nullable = false)
    private Instant concedidoEm;

    @Column(name = "valido_ate", nullable = false)
    private Instant validoAte;

    @Column(nullable = false)
    private boolean revogado;

    protected AcessoExterno() {
    }

    AcessoExterno(UUID id, String tenantId, String auditorId, String escopo, String concedidoPor,
                  Instant concedidoEm, Instant validoAte) {
        this.id = id;
        this.tenantId = tenantId;
        this.auditorId = auditorId;
        this.escopo = escopo;
        this.concedidoPor = concedidoPor;
        this.concedidoEm = concedidoEm;
        this.validoAte = validoAte;
        this.revogado = false;
    }

    void revogar() {
        this.revogado = true;
    }

    public boolean ativoEm(Instant agora) {
        return !revogado && agora.isBefore(validoAte);
    }

    public UUID getId() {
        return id;
    }

    public String getAuditorId() {
        return auditorId;
    }

    public String getEscopo() {
        return escopo;
    }

    public Instant getValidoAte() {
        return validoAte;
    }

    public boolean isRevogado() {
        return revogado;
    }
}
