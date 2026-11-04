# Immunization API

## Scheduling model

For this public immunization clinic system, the preferred approach is to generate slot availability on demand from recurring templates and date-specific overrides rather than storing a full slot table in the database.

### Why this approach is preferred

The scheduling rules are rule-based rather than slot-inventory-based:

- clinics have recurring weekly templates
- some dates are overridden by special rules
- capacity is managed as a bucket per time slot
- appointment availability is calculated from templates, overrides, and existing appointments

This matches the operational reality of a public clinic much better than creating and maintaining a large table of generated slot rows in advance.

### Current design

The system uses these sources of truth:

- Clinic
- VaccinationService
- SlotTemplate
- SlotOverride
- Appointment

Availability is computed as follows:

1. Check for a date-specific override for the clinic.
2. If no override exists, use the weekly template for that day.
3. Build the candidate slot start times from the template or override window.
4. Count existing appointments for each start time.
5. Compare the count to the configured capacity.
6. Only return available slots to the user.

### Why not persist every generated slot?

Persisting every slot in the database is useful only when the system needs:

- explicit per-slot records for reporting
- manual slot administration
- historical auditing of generated slots
- a fixed calendar inventory that must be edited individually

This project does not currently require that complexity.

### Decision

For a public immunization clinic with recurring schedules and capacity-based booking, the dynamic generation approach is the better fit.

A dedicated slot table should only be introduced later if the business grows to require slot-level management, auditing, or manual intervention.
