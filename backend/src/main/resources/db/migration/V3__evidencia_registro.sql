-- Read-model de evidencia (projecao consultavel do que entrou pela porta). SPEC.md secao 13.
-- A trilha e a fonte da verdade; esta tabela e reconstruivel a partir dos eventos.

CREATE TABLE evidencia_registro (
    id                UUID         NOT NULL,
    tenant_id         VARCHAR(100) NOT NULL,
    evento_id         UUID         NOT NULL,
    pseudonimo_pessoa VARCHAR(40),
    tipo              VARCHAR(60)  NOT NULL,
    atributos         TEXT         NOT NULL,
    fato              TEXT         NOT NULL,
    sistema_origem    VARCHAR(60)  NOT NULL,
    confianca         VARCHAR(10)  NOT NULL,
    coletada_em       TIMESTAMP    NOT NULL,
    valido_ate        TIMESTAMP,
    hash_documento    VARCHAR(80),
    conferida         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_evidencia_registro PRIMARY KEY (id)
);

CREATE INDEX ix_evid_tenant ON evidencia_registro (tenant_id);
CREATE INDEX ix_evid_tenant_pessoa ON evidencia_registro (tenant_id, pseudonimo_pessoa);
CREATE INDEX ix_evid_tenant_sistema ON evidencia_registro (tenant_id, sistema_origem);
