
-- Flyway migration to seed routine childhood vaccination services
INSERT INTO vaccination_services (name, description, duration_minutes, active) VALUES
-- 2 Months Old
('2-Month Routine Immunizations', 'Includes DTaP-IPV-Hib (Diphtheria, tetanus, pertussis, polio, Hib) needle, Pneu-C (Pneumococcal conjugate) needle, and Rot-1 (Rotavirus) oral drops.', 15, TRUE),

-- 4 Months Old
('4-Month Routine Immunizations', 'Includes DTaP-IPV-Hib (Diphtheria, tetanus, pertussis, polio, Hib) needle, Pneu-C (Pneumococcal conjugate) needle, and Rot-1 (Rotavirus) oral drops.', 15, TRUE),

-- 6 Months Old
('6-Month Routine Immunizations', 'Includes DTaP-IPV-Hib (Diphtheria, tetanus, pertussis, polio, Hib) needle. Note: Annual fall Influenza (Flu) vaccine may also be given if eligible.', 15, TRUE),

-- 12 Months Old
('12-Month Routine Immunizations', 'Includes MMR (Measles, mumps, rubella) needle, Men-C-C (Meningococcal conjugate C) needle, and Pneu-C (Pneumococcal conjugate) needle.', 15, TRUE),

-- 15 Months Old
('15-Month Routine Immunization', 'Includes Varicella (Chickenpox) needle.', 15, TRUE),

-- 18 Months Old
('18-Month Routine Immunization', 'Includes DTaP-IPV-Hib (Diphtheria, tetanus, pertussis, polio, Hib) booster needle.', 15, TRUE),

-- 4 to 6 Years Old
('4 to 6 Years Old School Entry Immunizations', 'Includes Tdap-IPV booster (Tetanus, diphtheria, pertussis, polio) needle and MMRV (Measles, mumps, rubella, varicella second dose) needle.', 15, TRUE),

-- Grade 7
('Grade 7 School Immunizations', 'Includes Meningococcal conjugate ACYW-135, Hepatitis B (HB), and Human Papillomavirus (HPV) vaccines.', 15, TRUE),

-- 14 to 16 Years Old
('14 to 16 Years Old Booster Immunization', 'Includes Tdap (Tetanus, diphtheria, pertussis booster) needle.', 15, TRUE);

