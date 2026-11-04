ALTER TABLE appointments
    DROP COLUMN slot_id,
    ADD COLUMN clinic_id BIGINT NOT NULL REFERENCES clinics(id),
    ADD COLUMN start_time TIMESTAMP NOT NULL;
