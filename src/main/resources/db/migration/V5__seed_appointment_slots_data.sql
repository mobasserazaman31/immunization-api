
-- ==============================================================================
-- Flyway Migration Script: Seed Appointment Slots
-- Schedule: Weekdays (Mon-Fri), 10:00 to 16:00
-- Cadence: 30-minute slot duration + 10-minute break (40 minutes per interval)
-- ==============================================================================

DO $$
DECLARE
    clinic_record RECORD;
    target_date   DATE;
    slot_start    TIMESTAMPTZ;
    slot_end      TIMESTAMPTZ;

    -- Configuration parameters
    start_date   DATE := CURRENT_DATE;
    end_date     DATE := CURRENT_DATE + INTERVAL '7 days';
    day_end_time TIME := '16:00:00';

BEGIN
    -- Iterate through each active clinic
    FOR clinic_record IN
        SELECT id
        FROM clinics
        WHERE active = TRUE
    LOOP
        target_date := start_date;

        -- Iterate through each day in the specified range
        WHILE target_date <= end_date LOOP

            -- Filter for weekdays only (1 = Monday, 5 = Friday in ISODOW)
            IF EXTRACT(ISODOW FROM target_date) BETWEEN 1 AND 5 THEN

                -- Initialize first slot at 10:00 AM
                slot_start := target_date + TIME '10:00:00';
                slot_end   := slot_start + INTERVAL '30 minutes';

                -- Generate slots throughout the day
                WHILE (slot_end::TIME <= day_end_time)
                  AND (slot_start::DATE = target_date)
                LOOP
                    INSERT INTO appointment_slots (
                        clinic_id,
                        start_time,
                        end_time,
                        active
                    )
                    VALUES (
                        clinic_record.id,
                        slot_start,
                        slot_end,
                        TRUE
                    )
                    ON CONFLICT (clinic_id, start_time) DO NOTHING;

                    -- Advance to next slot (30 min appointment + 10 min break = 40 min interval)
                    slot_start := slot_start + INTERVAL '40 minutes';
                    slot_end   := slot_start + INTERVAL '30 minutes';

                END LOOP;
            END IF;

            target_date := target_date + INTERVAL '1 day';

        END LOOP;
    END LOOP;
END $$;
