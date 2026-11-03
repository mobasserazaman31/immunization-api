
-- INSERT INTO clinics (name, address, city, province, active)
-- VALUES
--     ('Demo Community Clinic', '100 Main Street',
--      'Ottawa', 'Ontario', TRUE),
--     ('Demo Health Centre', '200 Park Avenue',
--      'Ottawa', 'Ontario', TRUE);

-- INSERT INTO vaccination_services
--     (name, description, duration_minutes, active)
-- VALUES
--     ('Routine vaccination',
--      'Appointment for a routine childhood or adolescent vaccination.',
--      15, TRUE),
--     ('Vaccination history assessment',
--      'Appointment to review a child''s vaccination history.',
--      30, TRUE);

-- -- Demo slots on November 15, 2026, in Ontario local time.
-- INSERT INTO appointment_slots
--     (clinic_id, start_time, end_time, active)
-- VALUES
--     (1, TIMESTAMPTZ '2026-11-15 09:00:00-05',
--         TIMESTAMPTZ '2026-11-15 09:15:00-05', TRUE),
--     (1, TIMESTAMPTZ '2026-11-15 09:30:00-05',
--         TIMESTAMPTZ '2026-11-15 09:45:00-05', TRUE),
--     (1, TIMESTAMPTZ '2026-11-15 10:00:00-05',
--         TIMESTAMPTZ '2026-11-15 10:15:00-05', TRUE),
--     (2, TIMESTAMPTZ '2026-11-15 09:00:00-05',
--         TIMESTAMPTZ '2026-11-15 09:15:00-05', TRUE),
--     (2, TIMESTAMPTZ '2026-11-15 09:30:00-05',
--         TIMESTAMPTZ '2026-11-15 09:45:00-05', TRUE);

