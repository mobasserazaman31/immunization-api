UPDATE slot_templates
SET capacity = CASE 
    WHEN clinic_id IN (1, 2, 3, 4, 5, 6) THEN 1
    WHEN clinic_id IN (7, 8, 9, 10, 11, 12) THEN 2
END
WHERE clinic_id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);