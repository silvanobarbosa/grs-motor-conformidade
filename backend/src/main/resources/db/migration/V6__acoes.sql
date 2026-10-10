-- Modulo 6: planos de acao / ocorrencias. SPEC.md secao 7. Dono por regra (cargo+local).

CREATE TABLE plano_acao (
    id           UUID         NOT NULL,
    tenant_id    VARCHAR(100) NOT NULL,
    tipo         VARCHAR(20)  NOT NULL,
    sujeito_id   VARCHAR(80)  NOT NULL,
    escopo       VARCHAR(10)  NOT NULL,
    causa        VARCHAR(60)  NOT NULL,
    status       VARCHAR(12)  NOT NULL,
    dono_cargo   VARCHAR(60),
    dono_local   VARCHAR(60),
    sem_dono     BOOLEAN      NOT NULL DEFAULT FALSE,
    descricao    TEXT,
    aberto_em    TIMESTAMP    NOT NULL,
    concluido_em TIMESTAMP,
    CONSTRAINT pk_plano_acao PRIMARY KEY (id)
);
CREATE INDEX ix_plano_tenant ON plano_acao (tenant_id);
CREATE INDEX ix_plano_sujeito_causa ON plano_acao (tenant_id, sujeito_id, causa, status);
