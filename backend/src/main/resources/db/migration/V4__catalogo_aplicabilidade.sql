-- Modulo 4: catalogo regulatorio GLOBAL (sem tenant) e aplicabilidade. SPEC.md secao 9.

CREATE TABLE norma (
    codigo  VARCHAR(40)  NOT NULL,
    titulo  VARCHAR(300) NOT NULL,
    CONSTRAINT pk_norma PRIMARY KEY (codigo)
);

CREATE TABLE requisito_regulatorio (
    id              UUID         NOT NULL,
    norma_codigo    VARCHAR(40)  NOT NULL,
    item            VARCHAR(40)  NOT NULL,
    descricao       TEXT         NOT NULL,
    versao          INT          NOT NULL,
    vigencia_inicio TIMESTAMP    NOT NULL,
    vigencia_fim    TIMESTAMP,
    nao_validado    BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_requisito PRIMARY KEY (id)
);
CREATE INDEX ix_req_norma_item ON requisito_regulatorio (norma_codigo, item);

CREATE TABLE aplicabilidade_regra (
    id               UUID        NOT NULL,
    norma_codigo     VARCHAR(40) NOT NULL,
    cargo            VARCHAR(60),
    local_codigo     VARCHAR(60),
    resultado_padrao VARCHAR(20) NOT NULL,
    alta_criticidade BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_aplic_regra PRIMARY KEY (id)
);

CREATE TABLE aplicabilidade_confirmacao (
    id                  UUID         NOT NULL,
    tenant_id           VARCHAR(100) NOT NULL,
    norma_codigo        VARCHAR(40)  NOT NULL,
    cargo               VARCHAR(60)  NOT NULL,
    local_codigo        VARCHAR(60),
    resultado           VARCHAR(20)  NOT NULL,
    justificativa       TEXT,
    responsavel_tecnico VARCHAR(120),
    confirmado_por      VARCHAR(120) NOT NULL,
    confirmado_em       TIMESTAMP    NOT NULL,
    CONSTRAINT pk_aplic_conf PRIMARY KEY (id)
);
CREATE INDEX ix_aplic_conf_tenant ON aplicabilidade_confirmacao (tenant_id);
