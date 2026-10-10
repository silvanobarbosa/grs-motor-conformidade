package com.senior.grc.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Regra de aplicabilidade GLOBAL e versionada (SPEC.md secao 9): por cargo e/ou local, qual norma
 * se aplica e com que resultado padrao. Ex.: ELETRICISTA -> NR-10 OBRIGATORIO. {@code localCodigo}
 * nulo = vale para qualquer local. {@code altaCriticidade}: marcar como NAO_APLICAVEL exige
 * responsavel tecnico.
 */
@Entity
@Table(name = "aplicabilidade_regra")
public class AplicabilidadeRegra {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "norma_codigo", nullable = false)
    private String normaCodigo;

    @Column
    private String cargo;

    @Column(name = "local_codigo")
    private String localCodigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado_padrao", nullable = false)
    private ResultadoAplicabilidade resultadoPadrao;

    @Column(name = "alta_criticidade", nullable = false)
    private boolean altaCriticidade;

    protected AplicabilidadeRegra() {
    }

    public AplicabilidadeRegra(UUID id, String normaCodigo, String cargo, String localCodigo,
                               ResultadoAplicabilidade resultadoPadrao, boolean altaCriticidade) {
        this.id = id;
        this.normaCodigo = normaCodigo;
        this.cargo = cargo;
        this.localCodigo = localCodigo;
        this.resultadoPadrao = resultadoPadrao;
        this.altaCriticidade = altaCriticidade;
    }

    public boolean casaCom(String cargo, String localCodigo) {
        boolean cargoOk = this.cargo == null || this.cargo.equals(cargo);
        boolean localOk = this.localCodigo == null || this.localCodigo.equals(localCodigo);
        return cargoOk && localOk;
    }

    public UUID getId() {
        return id;
    }

    public String getNormaCodigo() {
        return normaCodigo;
    }

    public String getCargo() {
        return cargo;
    }

    public String getLocalCodigo() {
        return localCodigo;
    }

    public ResultadoAplicabilidade getResultadoPadrao() {
        return resultadoPadrao;
    }

    public boolean isAltaCriticidade() {
        return altaCriticidade;
    }
}
