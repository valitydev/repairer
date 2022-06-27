CREATE SCHEMA IF NOT EXISTS rp;

CREATE TYPE rp.status AS ENUM ('failed', 'repaired');

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
    in_progress     BOOLEAN                     NOT NULL DEFAULT FALSE,
    CONSTRAINT machine_pkey PRIMARY KEY (id),
    CONSTRAINT machine_ukey UNIQUE (machine_id, namespace, status, created_at)
);

create index machine_id_idx on machine (machine_id, namespace);
create index machine_created_at_idx on machine (created_at);
create index machine_status_idx on machine (status);
create index machine_provider_id_idx on machine (provider_id);
create index machine_error_message_idx on machine (error_message);