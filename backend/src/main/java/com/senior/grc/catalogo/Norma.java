package com.senior.grc.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Norma/fonte do catalogo global (ex.: NR-35). Conteudo de demonstracao, nao validado. */
@Entity
@Table(name = "norma")
public class Norma {

    @Id
    @Column(nullable = false, updatable = false)
    private String codigo;

    @Column(nullable = false)
    private String titulo;

    protected Norma() {
    }

    public Norma(String codigo, String titulo) {
        this.codigo = codigo;
        this.titulo = titulo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }
}
