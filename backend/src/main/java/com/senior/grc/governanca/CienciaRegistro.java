package com.senior.grc.governanca;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Ciencia de uma pessoa (pseudonimo) sobre a versao exata de uma politica (SPEC.md secao 10). */
@Entity
@Table(name = "ciencia_registro")
public class CienciaRegistro {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "politica_codigo", nullable = false)
    private String politicaCodigo;

    @Column(nullable = false)
    private int versao;

    @Column(nullable = false)
    private String pseudonimo;

    @Column(name = "ciente_em", nullable = false)
    private Instant cienteEm;

    protected CienciaRegistro() {
    }

    CienciaRegistro(UUID id, String tenantId, String politicaCodigo, int versao, String pseudonimo, Instant cienteEm) {
        this.id = id;
        this.tenantId = tenantId;
        this.politicaCodigo = politicaCodigo;
        this.versao = versao;
        this.pseudonimo = pseudonimo;
        this.cienteEm = cienteEm;
    }

    public UUID getId() {
        return id;
    }

    public String getPoliticaCodigo() {
        return politicaCodigo;
    }

    public int getVersao() {
        return versao;
    }

    public String getPseudonimo() {
        return pseudonimo;
    }

    public Instant getCienteEm() {
        return cienteEm;
    }
}
