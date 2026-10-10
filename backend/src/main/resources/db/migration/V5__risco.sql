-- Modulo 5: risco residual derivado. SPEC.md secao 6. A avaliacao e sempre recalculada; esta
-- tabela e a projecao corrente por sujeito (a mudanca vira evento na trilha).

CREATE TABLE avaliacao_risco (
    id                UUID         NOT NULL,
    tenant_id         VARCHAR(100) NOT NULL,
    sujeito_id        VARCHAR(80)  NOT NULL,
    escopo            VARCHAR(10)  NOT NULL,
    nivel_residual    VARCHAR(12)  NOT NULL,
    cenario_dominante VARCHAR(60),
    explicacao        TEXT,
    calculado_em      TIMESTAMP    NOT NULL,
    CONSTRAINT pk_avaliacao_risco PRIMARY KEY (id),
    CONSTRAINT uq_avaliacao_sujeito UNIQUE (tenant_id, sujeito_id)
);
CREATE INDEX ix_avaliacao_tenant ON avaliacao_risco (tenant_id);
