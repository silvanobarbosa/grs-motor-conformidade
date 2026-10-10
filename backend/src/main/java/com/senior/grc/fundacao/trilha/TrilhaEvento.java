package com.senior.grc.fundacao.trilha;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento da trilha de auditoria: registro SO DE INCLUSAO, encadeado por hash (CLAUDE.md, regra 3;
 * SPEC.md secoes 10 e 13). Nada e editado nem apagado — correcao e um novo evento. Cada evento
 * carrega o hash do anterior ({@code hashAnterior}) e o seu proprio ({@code hash}), formando uma
 * cadeia verificavel por tenant.
 *
 * A entidade e intencionalmente IMUTAVEL: so tem getters. A unica porta de escrita e o append do
 * {@link TrilhaService}; nao ha setters, nem o JPA reescreve linha existente (o servico nunca faz
 * merge de um evento ja gravado).
 */
@Entity
@Table(name = "trilha_evento")
public class TrilhaEvento {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    /** Sequencia por tenant (1, 2, 3...). Define a ordem da cadeia. */
    @Column(nullable = false, updatable = false)
    private long seq;

    @Column(nullable = false, updatable = false)
    private String tipo;

    @Column(name = "payload", nullable = false, updatable = false, columnDefinition = "text")
    private String payloadJson;

    @Column(name = "hash_anterior", nullable = false, updatable = false)
    private String hashAnterior;

    @Column(nullable = false, updatable = false, unique = true)
    private String hash;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected TrilhaEvento() {
        // JPA
    }

    TrilhaEvento(UUID id, String tenantId, long seq, String tipo, String payloadJson,
                 String hashAnterior, String hash, Instant criadoEm) {
        this.id = id;
        this.tenantId = tenantId;
        this.seq = seq;
        this.tipo = tipo;
        this.payloadJson = payloadJson;
        this.hashAnterior = hashAnterior;
        this.hash = hash;
        this.criadoEm = criadoEm;
    }

    /** Conteudo canonico que entra no hash: amarra tenant, ordem, tipo, dado e o elo anterior. */
    static String conteudoParaHash(String tenantId, long seq, String tipo, String payloadJson, String hashAnterior) {
        return tenantId + "|" + seq + "|" + tipo + "|" + payloadJson + "|" + hashAnterior;
    }

    public String hashRecalculado() {
        return Hashing.sha256(conteudoParaHash(tenantId, seq, tipo, payloadJson, hashAnterior));
    }

    public UUID getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public long getSeq() {
        return seq;
    }

    public String getTipo() {
        return tipo;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public String getHashAnterior() {
        return hashAnterior;
    }

    public String getHash() {
        return hash;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
