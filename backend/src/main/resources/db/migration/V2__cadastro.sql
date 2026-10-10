-- Modulo 2 (Cadastro canonico e cofre de identidade). SPEC.md secao 4.

CREATE TABLE pessoa (
    id            UUID         NOT NULL,
    tenant_id     VARCHAR(100) NOT NULL,
    pseudonimo    VARCHAR(40)  NOT NULL,
    cpf_hash      VARCHAR(80),
    nome          VARCHAR(200),
    cargo_codigo  VARCHAR(60),
    local_codigo  VARCHAR(60),
    eliminada     BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_pessoa PRIMARY KEY (id),
    CONSTRAINT uq_pessoa_pseudonimo UNIQUE (pseudonimo)
);

CREATE INDEX ix_pessoa_tenant_cpf ON pessoa (tenant_id, cpf_hash);
CREATE INDEX ix_pessoa_tenant ON pessoa (tenant_id);
