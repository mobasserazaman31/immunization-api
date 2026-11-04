ALTER TABLE slot_templates
    ADD COLUMN capacity INTEGER NOT NULL
        CHECK (capacity > 0)
        DEFAULT 1;

ALTER TABLE slot_overrides
    ADD COLUMN capacity INTEGER NOT NULL
        CHECK (capacity > 0)
        DEFAULT 1,
    ADD COLUMN reason VARCHAR(250) NOT NULL DEFAULT 'Closed for the day';