-- Modulo 10: acesso externo (auditor/fiscal) temporario, so leitura, auditado. SPEC.md secao 10.

CREATE TABLE acesso_externo (
    id            UUID         NOT NULL,
    tenant_id     VARCHAR(100) NOT NULL,
    auditor_id    VARCHAR(120) NOT NULL,
    escopo        VARCHAR(200) NOT NULL,
    concedido_por VARCHAR(120) NOT NULL,
    concedido_em  TIMESTAMP    NOT NULL,
    valido_ate    TIMESTAMP    NOT NULL,
    revogado      BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_acesso_externo PRIMARY KEY (id)
);
CREATE INDEX ix_acesso_ext_tenant_auditor ON acesso_externo (tenant_id, auditor_id);
