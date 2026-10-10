package com.senior.grc.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Requisito regulatorio NO NIVEL DO ITEM da norma (SPEC.md secao 9), VERSIONADO por vigencia:
 * responde "cumpria a regra vigente naquela data?". Catalogo GLOBAL e somente leitura (CLAUDE.md
 * regra 10): nao tem tenant; o conteudo e mantido pela Senior.
 *
 * {@code naoValidado}: conteudo de DEMONSTRACAO, requisito resumido com palavras proprias e
 * referencia ao item — nao e texto integral de norma nem tem validacao juridica (CLAUDE.md "Dados").
 */
@Entity
@Table(name = "requisito_regulatorio")
public class RequisitoRegulatorio {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "norma_codigo", nullable = false)
    private String normaCodigo;

    @Column(nullable = false)
    private String item;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false)
    private int versao;

    @Column(name = "vigencia_inicio", nullable = false)
    private Instant vigenciaInicio;

    /** Nulo = vigente ate hoje (versao corrente). */
    @Column(name = "vigencia_fim")
    private Instant vigenciaFim;

    @Column(name = "nao_validado", nullable = false)
    private boolean naoValidado;

    protected RequisitoRegulatorio() {
    }

    public RequisitoRegulatorio(UUID id, String normaCodigo, String item, String descricao,
                                int versao, Instant vigenciaInicio, Instant vigenciaFim) {
        this.id = id;
        this.normaCodigo = normaCodigo;
        this.item = item;
        this.descricao = descricao;
        this.versao = versao;
        this.vigenciaInicio = vigenciaInicio;
        this.vigenciaFim = vigenciaFim;
        this.naoValidado = true;
    }

    public boolean vigenteEm(Instant data) {
        boolean depoisDoInicio = !data.isBefore(vigenciaInicio);
        boolean antesDoFim = vigenciaFim == null || data.isBefore(vigenciaFim);
        return depoisDoInicio && antesDoFim;
    }

    public UUID getId() {
        return id;
    }

    public String getNormaCodigo() {
        return normaCodigo;
    }

    public String getItem() {
        return item;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getVersao() {
        return versao;
    }

    public Instant getVigenciaInicio() {
        return vigenciaInicio;
    }

    public Instant getVigenciaFim() {
        return vigenciaFim;
    }

    public boolean isNaoValidado() {
        return naoValidado;
    }
}
