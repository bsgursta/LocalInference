CREATE TABLE mcu (
    id                  UUID    PRIMARY KEY,
    recovery_key        UUID    NOT NULL,
    last_connected      TIMESTAMPTZ,
    last_known_ip       INET
);
