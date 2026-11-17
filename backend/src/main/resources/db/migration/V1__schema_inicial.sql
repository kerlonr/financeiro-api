CREATE TABLE usuario (
    id                BIGSERIAL PRIMARY KEY,
    nome              VARCHAR(80)  NOT NULL,
    email             VARCHAR(160) NOT NULL UNIQUE,
    senha_hash        VARCHAR(72)  NOT NULL,
    tentativas_falhas INTEGER      NOT NULL DEFAULT 0,
    bloqueado_ate     TIMESTAMPTZ,
    criado_em         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Refresh tokens: guardamos só o hash SHA-256, nunca o token em si.
-- "familia" agrupa os tokens gerados a partir do mesmo login (rotação).
CREATE TABLE sessao (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    familia     UUID        NOT NULL,
    expira_em   TIMESTAMPTZ NOT NULL,
    revogada_em TIMESTAMPTZ,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX sessao_familia_idx ON sessao (familia);

CREATE TABLE conta (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    nome          VARCHAR(60)   NOT NULL,
    saldo_inicial NUMERIC(14, 2) NOT NULL DEFAULT 0,
    UNIQUE (usuario_id, nome),
    UNIQUE (id, usuario_id)
);

CREATE TABLE categoria (
    id         BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    nome       VARCHAR(60) NOT NULL,
    UNIQUE (usuario_id, nome),
    UNIQUE (id, usuario_id)
);

-- Uma transferência gera duas transações (SAIDA na origem e ENTRADA no destino)
-- ligadas pelo mesmo "transferencia_id". Transações comuns sempre têm categoria;
-- transações de transferência nunca têm.
-- As chaves estrangeiras compostas (id + usuario_id) garantem, no próprio banco, que a conta
-- e a categoria pertencem ao mesmo usuário da transação.
CREATE TABLE transacao (
    id               BIGSERIAL PRIMARY KEY,
    usuario_id       BIGINT         NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    conta_id         BIGINT         NOT NULL,
    categoria_id     BIGINT,
    tipo             VARCHAR(7)     NOT NULL CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    valor            NUMERIC(14, 2) NOT NULL CHECK (valor > 0),
    descricao        VARCHAR(120)   NOT NULL,
    data             DATE           NOT NULL,
    transferencia_id UUID,
    CHECK ((transferencia_id IS NULL) = (categoria_id IS NOT NULL)),
    FOREIGN KEY (conta_id, usuario_id) REFERENCES conta (id, usuario_id),
    FOREIGN KEY (categoria_id, usuario_id) REFERENCES categoria (id, usuario_id)
);

CREATE INDEX transacao_usuario_data_idx ON transacao (usuario_id, data DESC);
CREATE INDEX transacao_conta_idx ON transacao (conta_id);
CREATE INDEX transacao_categoria_idx ON transacao (categoria_id);
CREATE INDEX transacao_transferencia_idx ON transacao (transferencia_id);
