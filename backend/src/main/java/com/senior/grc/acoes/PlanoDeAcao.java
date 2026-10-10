package com.senior.grc.acoes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.senior.grc.risco.Cenario;

import java.time.Instant;
import java.util.UUID;

/**
 * Plano de acao / ocorrencia (SPEC.md secao 7). Tipos distintos com indicadores separados:
 * OCORRENCIA (fato ocorrido) e ACAO_TRATAMENTO (preventiva). Um plano por RISCO e CAUSA: enquanto
 * aberto, e atualizado, nao duplicado.
 *
 * O dono e por REGRA (cargo + local), nunca por nome (SPEC.md 7; ISO 31000 5.4.3) — assim a
 * reatribuicao e automatica: quem ocupa o cargo+local naquele momento e o dono. {@code semDono}
 * marca o risco cujo cargo+local nao resolve para ninguem -> vai ao gestor de riscos, destacado.
 */
@Entity
@Table(name = "plano_acao")
public class PlanoDeAcao {

    public enum Tipo { OCORRENCIA, ACAO_TRATAMENTO }

    public enum Status { ABERTO, CONCLUIDO }

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Column(name = "sujeito_id", nullable = false)
    private String sujeitoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Cenario.Escopo escopo;

    /** Causa = cenario que originou (um plano por risco e causa). */
    @Column(nullable = false)
    private String causa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "dono_cargo")
    private String donoCargo;

    @Column(name = "dono_local")
    private String donoLocal;

    @Column(name = "sem_dono", nullable = false)
    private boolean semDono;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "aberto_em", nullable = false)
    private Instant abertoEm;

    @Column(name = "concluido_em")
    private Instant concluidoEm;

    protected PlanoDeAcao() {
    }

    PlanoDeAcao(UUID id, String tenantId, Tipo tipo, String sujeitoId, Cenario.Escopo escopo, String causa,
                String donoCargo, String donoLocal, boolean semDono, String descricao, Instant abertoEm) {
        this.id = id;
        this.tenantId = tenantId;
        this.tipo = tipo;
        this.sujeitoId = sujeitoId;
        this.escopo = escopo;
        this.causa = causa;
        this.status = Status.ABERTO;
        this.donoCargo = donoCargo;
        this.donoLocal = donoLocal;
        this.semDono = semDono;
        this.descricao = descricao;
        this.abertoEm = abertoEm;
    }

    void atualizarDescricao(String descricao) {
        this.descricao = descricao;
    }

    void concluir(Instant quando) {
        this.status = Status.CONCLUIDO;
        this.concluidoEm = quando;
    }

    public UUID getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getSujeitoId() {
        return sujeitoId;
    }

    public Cenario.Escopo getEscopo() {
        return escopo;
    }

    public String getCausa() {
        return causa;
    }

    public Status getStatus() {
        return status;
    }

    public String getDonoCargo() {
        return donoCargo;
    }

    public String getDonoLocal() {
        return donoLocal;
    }

    public boolean isSemDono() {
        return semDono;
    }

    public String getDescricao() {
        return descricao;
    }

    public Instant getAbertoEm() {
        return abertoEm;
    }

    public Instant getConcluidoEm() {
        return concluidoEm;
    }
}
