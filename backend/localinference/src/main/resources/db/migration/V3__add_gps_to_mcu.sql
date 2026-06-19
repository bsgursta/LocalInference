ALTER TABLE mcu
    ADD COLUMN last_lat        DOUBLE PRECISION,
    ADD COLUMN last_lng        DOUBLE PRECISION,
    ADD COLUMN last_gps_update TIMESTAMPTZ;
