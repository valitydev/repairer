CREATE SCHEMA IF NOT EXISTS rp;

CREATE TYPE rp.status AS ENUM ('failed', 'in_progress', 'repaired');

CREATE TABLE rp.machine
(
    id              BIGSERIAL                   NOT NULL,
    machine_id      CHARACTER VARYING           NOT NULL,
    namespace       CHARACTER VARYING           NOT NULL,
    status          rp.status                   NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    provider_id     CHARACTER VARYING,
    error_message   CHARACTER VARYING,
    current         BOOLEAN                     NOT NULL DEFAULT TRUE,
    CONSTRAINT machine_pkey PRIMARY KEY (id)
);