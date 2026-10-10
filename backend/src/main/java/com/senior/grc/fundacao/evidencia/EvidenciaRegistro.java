package com.senior.grc.fundacao.evidencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-model da evidencia (SPEC.md secao 13, "Repositorio de evidencias"): projecao consultavel
 * do fato que entrou pela porta. Derivavel/reconstruivel a partir dos eventos da trilha — a trilha
 * e a fonte da verdade; esta tabela so acelera a consulta do motor de avaliacao e dos indicadores.
 *
 * {@code conferida}: evidencia de baixa confianca (ex.: OCR com campo duvidoso) entra como nao
 * conferida e NAO reduz risco ate um humano conferir (SPEC.md secoes 6 e 15; roteiro R4).
 */
@Entity
@Table(name = "evidencia_registro")
public class EvidenciaRegistro {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "evento_id", nullable = false, updatable = false)
    private UUID eventoId;

    @Column(name = "pseudonimo_pessoa")
    private String pseudonimoPessoa;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "atributos", nullable = false, columnDefinition = "text")
    private String atributosJson;

    @Column(nullable = false, columnDefinition = "text")
    private String fato;

    @Column(name = "sistema_origem", nullable = false)
    private String sistemaOrigem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfiancaFonte confianca;

    @Column(name = "coletada_em", nullable = false)
    private Instant coletadaEm;

    @Column(name = "valido_ate")
    private Instant validoAte;

    @Column(name = "hash_documento")
    private String hashDocumento;

    @Column(nullable = false)
    private boolean conferida;

    protected EvidenciaRegistro() {
    }

    EvidenciaRegistro(UUID id, String tenantId, UUID eventoId, Evidencia e, boolean conferida) {
        this.id = id;
        this.tenantId = tenantId;
        this.eventoId = eventoId;
        this.pseudonimoPessoa = e.pseudonimoPessoa();
        this.tipo = e.tipo();
        this.atributosJson = PortaDeEntrada.atributosJson(e);
        this.fato = e.fato();
        this.sistemaOrigem = e.sistemaOrigem();
        this.confianca = e.confianca();
        this.coletadaEm = e.coletadaEm();
        this.validoAte = e.validoAte();
        this.hashDocumento = e.hashDocumento();
        this.conferida = conferida;
    }

    public void conferir() {
        this.conferida = true;
    }

    public UUID getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public String getPseudonimoPessoa() {
        return pseudonimoPessoa;
    }

    public String getTipo() {
        return tipo;
    }

    public String getAtributosJson() {
        return atributosJson;
    }

    public java.util.Map<String, String> atributos() {
        return AtributosJson.ler(atributosJson);
    }

    public String atributo(String chave) {
        return atributos().get(chave);
    }

    public String getFato() {
        return fato;
    }

    public String getSistemaOrigem() {
        return sistemaOrigem;
    }

    public ConfiancaFonte getConfianca() {
        return confianca;
    }

    public Instant getColetadaEm() {
        return coletadaEm;
    }

    public Instant getValidoAte() {
        return validoAte;
    }

    public String getHashDocumento() {
        return hashDocumento;
    }

    public boolean isConferida() {
        return conferida;
    }
}
