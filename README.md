# Immunization API

## Hybrid rolling-slot model: change list

The app is moving toward a persisted, rolling window of concrete slots instead of calculating availability dynamically on each booking request. This is the recommended model for routine immunization and similar recurring schedules.

### What still needs to change

1. Make the startup job bootstrap-only
   - [x] The startup path should seed the initial rolling window once.
   - [x] It should not be used to re-generate the full future window repeatedly.
   - The goal is:
     - [x] startup: seed the current window
     - [x] daily job: add only the next day
     - [x] no full-window rebuild during normal operation

2. Remove full-window regeneration from the normal flow
   - Current generation entry points include:
     - `populateRollingWindow()`
     - `generateNewRollingDay()`
     - `regenerateDate(...)`
   - For production stability, keep only:
     - [x] initial bootstrap
     - [x] forward-only daily generation
     - [x] explicit admin-triggered single-date regeneration when required

3. Treat templates and overrides as the source of truth, not the slot table
   - Concrete slot rows should be materialized from:
     - active clinic template
     - valid date window
     - override for that specific date
   - The slot table should be the bookable inventory, not the place where the business rules are recalculated each time.

4. Enforce `validFrom` and `validTill` as required business rules
   - `validFrom` must not be null.
   - `validTill` must not be null.
   - `validTill` must be greater than or equal to `validFrom`.
   - Templates outside their validity window should not generate slots.

5. Keep slot generation idempotent
   - The app should not create duplicate slot rows for the same clinic, date, and time.
   - The current insert-if-absent pattern is the right approach.
   - Every generation path should go through the same slot creation logic to avoid overlap and duplication.

6. Handle overrides explicitly
   - For date-level overrides, the rules should be explicit:
     - closed date => no slots generated
     - reduced capacity => only that slot capacity changes
     - changed time window => regenerate only that date
   - Override logic must also reconcile bookings when capacity drops or the day is closed.

7. Define the policy for removed or invalidated slots
   - This remains a business decision.
   - Options include:
     - deactivate slot rows instead of deleting them
     - delete slot rows and cancel associated booked appointments
     - leave history but mark slots inactive
   - The chosen policy should be deterministic and easy to support.

8. Keep booking logic based on slot stock
   - Booking should rely on concrete slot capacity and booked count.
   - It should not re-aggregate the template logic each time.
   - This reduces race conditions and keeps availability aligned with the materialized data.

9. Keep the daily job narrow and predictable
   - Best pattern:
     - bootstrap window = current date + 7 days
     - daily job = generate exactly one new future date
     - never regenerate the whole window unless an admin explicitly requests it
   - This is the standard airline-style scheduling pattern: recurring template + materialized future dates.

10. Add clear admin-only control for templates and overrides
   - Template creation should require admin permissions.
   - Override creation/update should also require admin control or explicit ownership rules.
   - The API should clearly separate:
     - public slot browsing
     - authenticated booking
     - admin schedule management

### Recommended final pattern

The best model for routine immunization or airline-like scheduling is:

- Templates define the recurring weekly pattern.
- Validity windows restrict when the template is active.
- Overrides change a specific date.
- Concrete slot rows are generated and stored for a rolling future window.
- A daily job extends the window by one more day.
- Booking logic reads the persisted slot capacity, not the template.

This is the cleanest version of the hybrid model the app is converging toward.
