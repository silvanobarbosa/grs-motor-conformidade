package com.senior.grc.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Usuario do SISTEMA (operadores do Motor GRC), gerido pelo admin master. Distinto das PESSOAS do
 * cliente (colaboradores): este e quem OPERA o produto. A senha e guardada so como hash com sal.
 *
 * Na arquitetura real a identidade vem da License; na demo este cadastro faz o papel dela, permitindo
 * ao admin master criar outros usuarios e papeis.
 */
@Entity
@Table(name = "usuario_sistema")
public class UsuarioSistema {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String login;

    @Column(nullable = false)
    private String nome;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(nullable = false)
    private String sal;

    /** Papeis separados por virgula (ex.: "ADMIN_MASTER" ou "GESTOR_RISCOS,TST"). */
    @Column(nullable = false)
    private String papeis;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected UsuarioSistema() {
    }

    UsuarioSistema(UUID id, String tenantId, String login, String nome, String senhaHash, String sal,
                   String papeis, Instant criadoEm) {
        this.id = id;
        this.tenantId = tenantId;
        this.login = login;
        this.nome = nome;
        this.senhaHash = senhaHash;
        this.sal = sal;
        this.papeis = papeis;
        this.ativo = true;
        this.criadoEm = criadoEm;
    }

    void definirAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public List<String> papeisLista() {
        return List.of(papeis.split(","));
    }

    public boolean temPapel(String papel) {
        return papeisLista().contains(papel);
    }

    public UUID getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getLogin() {
        return login;
    }

    public String getNome() {
        return nome;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getSal() {
        return sal;
    }

    public String getPapeis() {
        return papeis;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
