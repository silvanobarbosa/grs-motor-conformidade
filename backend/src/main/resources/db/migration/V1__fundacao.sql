-- Modulo 1 (Fundacao): trilha de auditoria so-inclusao, encadeada por hash.
-- CLAUDE.md regra 3; SPEC.md secoes 10 e 13. Compativel com PostgreSQL (prod/Neon) e H2 (testes).

CREATE TABLE trilha_evento (
    id             UUID         NOT NULL,
    tenant_id      VARCHAR(100) NOT NULL,
    seq            BIGINT       NOT NULL,
    tipo           VARCHAR(100) NOT NULL,
    payload        TEXT         NOT NULL,
    hash_anterior  VARCHAR(80)  NOT NULL,
    hash           VARCHAR(80)  NOT NULL,
    criado_em      TIMESTAMP    NOT NULL,
    CONSTRAINT pk_trilha_evento PRIMARY KEY (id),
    CONSTRAINT uq_trilha_hash UNIQUE (hash),
    CONSTRAINT uq_trilha_tenant_seq UNIQUE (tenant_id, seq)
);

CREATE INDEX ix_trilha_tenant_seq ON trilha_evento (tenant_id, seq);
