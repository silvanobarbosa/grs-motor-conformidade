-- Modulo admin: usuarios do sistema (operadores), geridos pelo admin master.

CREATE TABLE usuario_sistema (
    id         UUID         NOT NULL,
    tenant_id  VARCHAR(100) NOT NULL,
    login      VARCHAR(80)  NOT NULL,
    nome       VARCHAR(200) NOT NULL,
    senha_hash VARCHAR(80)  NOT NULL,
    sal        VARCHAR(64)  NOT NULL,
    papeis     VARCHAR(300) NOT NULL,
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em  TIMESTAMP    NOT NULL,
    CONSTRAINT pk_usuario_sistema PRIMARY KEY (id),
    CONSTRAINT uq_usuario_login UNIQUE (tenant_id, login)
);
CREATE INDEX ix_usuario_tenant ON usuario_sistema (tenant_id);
