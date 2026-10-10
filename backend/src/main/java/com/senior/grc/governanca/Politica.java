package com.senior.grc.governanca;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Politica interna como fonte de requisito (SPEC.md secao 10; ISO 37301 5.2). Ciclo: rascunho ->
 * aprovacao (BPM) -> publicacao com versao e vigencia (GED da o hash) -> ciencia (Sign). A ciencia
 * e exigida por REGRA (cargo + local), ligada a versao exata publicada.
 */
@Entity
@Table(name = "politica")
public class Politica {

    public enum Status { RASCUNHO, PUBLICADA }

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private int versao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    /** Hash da versao no GED (texto fica no GED; ao core chega ID + hash). */
    @Column(name = "hash_versao")
    private String hashVersao;

    @Column(name = "cargo_alvo")
    private String cargoAlvo;

    @Column(name = "local_alvo")
    private String localAlvo;

    @Column(name = "publicada_em")
    private Instant publicadaEm;

    protected Politica() {
    }

    Politica(UUID id, String tenantId, String codigo, String titulo, int versao, String cargoAlvo, String localAlvo) {
        this.id = id;
        this.tenantId = tenantId;
        this.codigo = codigo;
        this.titulo = titulo;
        this.versao = versao;
        this.status = Status.RASCUNHO;
        this.cargoAlvo = cargoAlvo;
        this.localAlvo = localAlvo;
    }

    void publicar(String hashVersao, Instant quando) {
        this.status = Status.PUBLICADA;
        this.hashVersao = hashVersao;
        this.publicadaEm = quando;
    }

    public UUID getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getVersao() {
        return versao;
    }

    public Status getStatus() {
        return status;
    }

    public String getHashVersao() {
        return hashVersao;
    }

    public String getCargoAlvo() {
        return cargoAlvo;
    }

    public String getLocalAlvo() {
        return localAlvo;
    }

    public Instant getPublicadaEm() {
        return publicadaEm;
    }
}
