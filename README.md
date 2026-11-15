# Immunization API Endpoint Flows

## Security

The security filter permits requests globally, while selected controller methods add method-level authorization. Endpoints marked **ADMIN** require the `ADMIN` role from the Keycloak token. The user profile endpoint requires an authenticated token. Other endpoints have no controller-level role requirement.

## Appointments

| Method and endpoint | Access | Actions |
| --- | --- | --- |
| `POST /api/appointments` | Public | Validates the clinic, vaccination service, and requested slot. Locks the slot, checks availability, increments its booked count, updates its status, creates an appointment and booking reference, and returns the appointment. |
| `GET /api/appointments` | Public | Returns all appointments. |
| `POST /api/appointments/cancel?bookingReference={uuid}&dateOfBirth={date}` | Public | Finds the appointment using its booking reference and date of birth. If it is still booked, marks it cancelled, decrements the slot's booked count, updates slot availability, and returns `204`. Returns `404` if no appointment matches. |

## Clinics

| Method and endpoint | Access | Actions |
| --- | --- | --- |
| `GET /api/clinics` | Public | Returns all clinics. |
| `POST /api/clinics` | **ADMIN** | Validates the request, creates a clinic, and returns it with `201`. |

## Vaccination Services

| Method and endpoint | Access | Actions |
| --- | --- | --- |
| `GET /api/vaccination-services` | Public | Returns all vaccination services. |
| `POST /api/vaccination-services` | **ADMIN** | Validates the request, creates a vaccination service, and returns it with `201`. |

## Appointment Slots and Templates

| Method and endpoint | Access | Actions |
| --- | --- | --- |
| `GET /api/appointment-slots?clinicId={id}` | Public | Returns active, available slots for the clinic within the current seven-day window, grouped by date. |
| `GET /api/appointment-slots/templates?clinicId={id}` | Public | Returns active, non-expired templates for the clinic. |
| `POST /api/appointment-slots/templates` | **ADMIN** | Validates the template, rejects same-priority overlaps and a first applicable date of today, then saves it. It regenerates matching dates that are already materialized and fall within both the template's validity range and the current seven-day window. Reconciliation may cancel bookings that no longer fit; those cancellations are logged. The `201` response includes `affectedBookingCount`. |
| `PATCH /api/appointment-slots/templates/{templateId}/deactivate` | **ADMIN** | Marks the template inactive, then regenerates its matching dates in the current window and validity period. Any bookings that no longer fit are cancelled and logged. Returns `204`. |

## Slot Overrides

| Method and endpoint | Access | Actions |
| --- | --- | --- |
| `GET /api/overrides` | Public | Returns all overrides, including inactive overrides. |
| `POST /api/overrides` | **ADMIN** | Requires a future date. Creates or updates the override for the clinic and date, marks it active, then regenerates that date. Reconciliation may cancel and log bookings that no longer fit. Returns the saved override with `201`. |
| `DELETE /api/overrides/{overrideId}` | **ADMIN** | Marks the override inactive, then regenerates the date using the remaining active override or recurring template. Bookings that no longer fit are cancelled and logged. Returns `204`; cancelling an already inactive override is a no-op. |

## User Profile

| Method and endpoint | Access | Actions |
| --- | --- | --- |
| `GET /` | Authenticated | Reads the authenticated user's subject and `preferred_username` claims, logs them, and returns the profile. |

## Scheduled Slot Generation

The endpoint flows above work with materialized slot rows. At application startup, a one-time initializer populates the initial rolling window. A daily job generates the new edge date of the seven-day window. Template and override mutations can also regenerate dates immediately as described above.
