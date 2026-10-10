package com.senior.grc.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Confirmacao/ajuste de aplicabilidade POR TENANT (SPEC.md secao 9): o key user confirma o
 * resultado, ou marca como nao aplicavel COM justificativa. Nao aplicavel de alta criticidade exige
 * responsavel tecnico. Revisao anual registrada.
 */
@Entity
@Table(name = "aplicabilidade_confirmacao")
public class AplicabilidadeConfirmacao {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "norma_codigo", nullable = false)
    private String normaCodigo;

    @Column(nullable = false)
    private String cargo;

    @Column(name = "local_codigo")
    private String localCodigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoAplicabilidade resultado;

    @Column(columnDefinition = "text")
    private String justificativa;

    @Column(name = "responsavel_tecnico")
    private String responsavelTecnico;

    @Column(name = "confirmado_por", nullable = false)
    private String confirmadoPor;

    @Column(name = "confirmado_em", nullable = false)
    private Instant confirmadoEm;

    protected AplicabilidadeConfirmacao() {
    }

    public AplicabilidadeConfirmacao(UUID id, String tenantId, String normaCodigo, String cargo, String localCodigo,
                                     ResultadoAplicabilidade resultado, String justificativa,
                                     String responsavelTecnico, String confirmadoPor, Instant confirmadoEm) {
        this.id = id;
        this.tenantId = tenantId;
        this.normaCodigo = normaCodigo;
        this.cargo = cargo;
        this.localCodigo = localCodigo;
        this.resultado = resultado;
        this.justificativa = justificativa;
        this.responsavelTecnico = responsavelTecnico;
        this.confirmadoPor = confirmadoPor;
        this.confirmadoEm = confirmadoEm;
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

    public ResultadoAplicabilidade getResultado() {
        return resultado;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getResponsavelTecnico() {
        return responsavelTecnico;
    }

    public String getConfirmadoPor() {
        return confirmadoPor;
    }

    public Instant getConfirmadoEm() {
        return confirmadoEm;
    }
}
