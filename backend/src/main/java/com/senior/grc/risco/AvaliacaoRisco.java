package com.senior.grc.risco;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Risco RESIDUAL de um sujeito (pessoa ou empresa), SEMPRE DERIVADO da avaliacao das evidencias
 * (CLAUDE.md regra 5; SPEC.md secao 6). Nunca editado a mao: o {@link RiscoService} recalcula e faz
 * upsert desta projecao. A explicacao ("qual evidencia, de qual fonte, em qual regra") acompanha o
 * nivel — toda mudanca de risco e explicavel (regra 6).
 */
@Entity
@Table(name = "avaliacao_risco")
public class AvaliacaoRisco {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "sujeito_id", nullable = false)
    private String sujeitoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Cenario.Escopo escopo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_residual", nullable = false)
    private NivelRisco nivelResidual;

    @Column(name = "cenario_dominante")
    private String cenarioDominante;

    @Column(columnDefinition = "text")
    private String explicacao;

    @Column(name = "calculado_em", nullable = false)
    private Instant calculadoEm;

    protected AvaliacaoRisco() {
    }

    AvaliacaoRisco(UUID id, String tenantId, String sujeitoId, Cenario.Escopo escopo) {
        this.id = id;
        this.tenantId = tenantId;
        this.sujeitoId = sujeitoId;
        this.escopo = escopo;
        this.nivelResidual = NivelRisco.SEM_RISCO;
    }

    void atualizar(NivelRisco nivel, String cenarioDominante, String explicacao, Instant quando) {
        this.nivelResidual = nivel;
        this.cenarioDominante = cenarioDominante;
        this.explicacao = explicacao;
        this.calculadoEm = quando;
    }

    public UUID getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getSujeitoId() {
        return sujeitoId;
    }

    public Cenario.Escopo getEscopo() {
        return escopo;
    }

    public NivelRisco getNivelResidual() {
        return nivelResidual;
    }

    public String getCenarioDominante() {
        return cenarioDominante;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public Instant getCalculadoEm() {
        return calculadoEm;
    }
}
