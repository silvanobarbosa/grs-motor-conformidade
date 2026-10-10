package com.senior.grc.cadastro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Pessoa no COFRE DE IDENTIDADE (SPEC.md secao 4; CLAUDE.md regra 4).
 *
 * O CPF real nunca sai do cofre: guardamos o {@code cpfHash} (para casar registros entre sistemas,
 * LGPD art. 46) e um {@code pseudonimo} estavel. Trilha, evidencias e indicadores guardam SO o
 * pseudonimo. Eliminacao (LGPD art. 18, VI) apaga a ligacao pseudonimo -> pessoa: zera nome e
 * cpfHash e marca {@code eliminada}; o pseudonimo continua existindo para a trilha permanecer
 * integra, mas a identidade fica irrecuperavel.
 */
@Entity
@Table(name = "pessoa")
public class Pessoa {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(nullable = false, updatable = false, unique = true)
    private String pseudonimo;

    /** Hash do CPF (tenant + cpf). Null apos eliminacao. Nunca o CPF em claro. */
    @Column(name = "cpf_hash")
    private String cpfHash;

    /** Nome exibivel. Null apos eliminacao. */
    @Column
    private String nome;

    @Column(name = "cargo_codigo")
    private String cargoCodigo;

    @Column(name = "local_codigo")
    private String localCodigo;

    @Column(nullable = false)
    private boolean eliminada;

    protected Pessoa() {
    }

    Pessoa(UUID id, String tenantId, String pseudonimo, String cpfHash, String nome,
           String cargoCodigo, String localCodigo) {
        this.id = id;
        this.tenantId = tenantId;
        this.pseudonimo = pseudonimo;
        this.cpfHash = cpfHash;
        this.nome = nome;
        this.cargoCodigo = cargoCodigo;
        this.localCodigo = localCodigo;
        this.eliminada = false;
    }

    /** Eliminacao LGPD: quebra a ligacao pseudonimo -> identidade. Irreversivel. */
    void eliminar() {
        this.cpfHash = null;
        this.nome = null;
        this.eliminada = true;
    }

    void moverPara(String cargoCodigo, String localCodigo) {
        this.cargoCodigo = cargoCodigo;
        this.localCodigo = localCodigo;
    }

    public UUID getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPseudonimo() {
        return pseudonimo;
    }

    public String getCpfHash() {
        return cpfHash;
    }

    public String getNome() {
        return nome;
    }

    public String getCargoCodigo() {
        return cargoCodigo;
    }

    public String getLocalCodigo() {
        return localCodigo;
    }

    public boolean isEliminada() {
        return eliminada;
    }
}
