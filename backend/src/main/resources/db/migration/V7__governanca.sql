-- Modulo 7: governanca. SPEC.md secao 10. Politica (GED), ciencia (Sign), excecao (BPM aceitar risco).

CREATE TABLE politica (
    id           UUID         NOT NULL,
    tenant_id    VARCHAR(100) NOT NULL,
    codigo       VARCHAR(60)  NOT NULL,
    titulo       VARCHAR(300) NOT NULL,
    versao       INT          NOT NULL,
    status       VARCHAR(12)  NOT NULL,
    hash_versao  VARCHAR(80),
    cargo_alvo   VARCHAR(60),
    local_alvo   VARCHAR(60),
    publicada_em TIMESTAMP,
    CONSTRAINT pk_politica PRIMARY KEY (id)
);
CREATE INDEX ix_politica_tenant_codigo ON politica (tenant_id, codigo);

CREATE TABLE ciencia_registro (
    id              UUID         NOT NULL,
    tenant_id       VARCHAR(100) NOT NULL,
    politica_codigo VARCHAR(60)  NOT NULL,
    versao          INT          NOT NULL,
    pseudonimo      VARCHAR(40)  NOT NULL,
    ciente_em       TIMESTAMP    NOT NULL,
    CONSTRAINT pk_ciencia PRIMARY KEY (id)
);
CREATE INDEX ix_ciencia_politica ON ciencia_registro (tenant_id, politica_codigo, versao);

CREATE TABLE excecao_risco (
    id            UUID         NOT NULL,
    tenant_id     VARCHAR(100) NOT NULL,
    sujeito_id    VARCHAR(80)  NOT NULL,
    causa         VARCHAR(60)  NOT NULL,
    justificativa TEXT         NOT NULL,
    aprovado_por  VARCHAR(120) NOT NULL,
    aceito_em     TIMESTAMP    NOT NULL,
    CONSTRAINT pk_excecao_risco PRIMARY KEY (id)
);
CREATE INDEX ix_excecao_sujeito_causa ON excecao_risco (tenant_id, sujeito_id, causa);
