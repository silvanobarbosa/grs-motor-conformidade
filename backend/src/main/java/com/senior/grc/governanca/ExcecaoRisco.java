package com.senior.grc.governanca;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Excecao humana: risco ACEITO por decisao (BPM), com justificativa registrada (ISO 31000 6.4.4;
 * SPEC.md secoes 6 e 10). Enquanto ativa, o modulo de acoes NAO abre novo plano para aquele
 * risco+causa — a aceitacao e consciente e rastreada.
 */
@Entity
@Table(name = "excecao_risco")
public class ExcecaoRisco {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "sujeito_id", nullable = false)
    private String sujeitoId;

    @Column(nullable = false)
    private String causa;

    @Column(nullable = false, columnDefinition = "text")
    private String justificativa;

    @Column(name = "aprovado_por", nullable = false)
    private String aprovadoPor;

    @Column(name = "aceito_em", nullable = false)
    private Instant aceitoEm;

    protected ExcecaoRisco() {
    }

    ExcecaoRisco(UUID id, String tenantId, String sujeitoId, String causa, String justificativa,
                 String aprovadoPor, Instant aceitoEm) {
        this.id = id;
        this.tenantId = tenantId;
        this.sujeitoId = sujeitoId;
        this.causa = causa;
        this.justificativa = justificativa;
        this.aprovadoPor = aprovadoPor;
        this.aceitoEm = aceitoEm;
    }

    public UUID getId() {
        return id;
    }

    public String getSujeitoId() {
        return sujeitoId;
    }

    public String getCausa() {
        return causa;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getAprovadoPor() {
        return aprovadoPor;
    }

    public Instant getAceitoEm() {
        return aceitoEm;
    }
}
