Here is a system design breakdown, REST API surface, and edge-case analysis for your **45-minute slot scheduling platform**.

---

## 1. Domain Model & RBAC Refinement

### Core Entities & Key Fields

* **Users & Roles (`Patient`, `Doctor`, `Receptionist`):**
* `User`: `id`, `email`, `password_hash`, `role` (`ROLE_PATIENT`, `ROLE_RECEPTIONIST`, `ROLE_DOCTOR`), `timezone` (crucial for cross-day/DST boundaries).
* `Patient`: `id`, `user_id`, `primary_doctor_id` (nullable or required on selection), `phone`, `preferred_contact_channel`.
* `Doctor`: `id`, `user_id`, `specialty`, `max_working_days_per_week = 3`, `default_slot_minutes = 45`.
* `Receptionist`: `id`, `user_id`, `clinic_zone`.


* **Schedule Rules (`ScheduleTemplates`, `ScheduleOverrides`):**
* `ScheduleTemplate`: `id`, `doctor_id`, `day_of_week` (`MON`–`FRI`, enforced to at most **3 distinct days per doctor**), `start_time`, `end_time`, `buffer_minutes` (optional), `effective_from`, `effective_until` (nullable), `version`.
* `ScheduleOverride`: `id`, `doctor_id`, `override_date` (or `[start_at, end_at]` window), `override_type` (`BLOCKED` / `VACATION` / `MODIFIED_HOURS`), `new_start_time`, `new_end_time`, `reason`.


* **Inventory & Bookings (`AppointmentSlots`, `Appointments`):**
* `AppointmentSlot`: `id`, `doctor_id`, `start_at` (UTC/timestamptz), `end_at` (`start_at + 45m`), `status` (`OPEN`, `BOOKED`, `BLOCKED`, `CANCELLED`), `source_template_id`, `source_override_id`, `version` (for optimistic locking).
* `Appointment`: `id`, `slot_id` (unique while active), `patient_id`, `doctor_id`, `status` (`CONFIRMED`, `CANCELLED_BY_PATIENT`, `CANCELLED_BY_OVERRIDE`, `COMPLETED`, `NO_SHOW`), `booked_at`, `cancelled_at`, `cancellation_reason`.


* **Recall / Recovery (`RescheduleOutreachQueue`):**
* `RescheduleOutreachQueue`: `id`, `original_appointment_id`, `patient_id`, `doctor_id`, `original_slot_start_at`, `status` (`PENDING`, `CONTACTED`, `RESCHEDULED`, `EXHAUSTED`, `DECLINED`), `priority_score`, `contact_attempts` (JSONB array or child table of `attempt_at`, `channel`, `outcome`, `notes`, `logged_by_user_id`), `created_at`, `updated_at`.



### RBAC Notes

* **Hierarchy check:** Notice your description says `ROLE_RECEPTIONIST` can do *"All things a patient can do plus..."*. In real clinics, receptionists book/cancel *on behalf of* patients (`patient_id` passed in payload) rather than booking under their own receptionist identity—make sure your authorization layer supports acting on behalf of a `patient_id` while enforcing the same slot/notice rules (or allowing receptionist overrides with audit logging).
* **`ROLE_DOCTOR`:** Read-only access to their own daily/weekly slots, booked appointments, and basic patient demographics/visit notes (scoped to `doctor_id` derived from the authenticated principal, never trusting client-supplied `doctor_id`).

---

## 2. Core Workflows

### Workflow 1: 60-Day Rolling Slot Materialization Job

* **Cadence:** Runs daily (e.g., `01:00 UTC` / clinic local midnight) plus on-demand trigger when a template is created/updated.
* **Window:** Materializes slots for `[today, today + 60 days]`.
* **Logic:**
1. Fetch active `ScheduleTemplate` rows for each doctor that intersect `[today, today + 60 days]`.
2. For each matching weekday within the window, slice `[start_time, end_time]` into contiguous **45-minute** intervals (discarding trailing remainder `< 45` minutes).
3. Apply any active `ScheduleOverride` for that date (`BLOCKED` removes/blocks overlapping slots; `MODIFIED_HOURS` replaces/clips slots to the new window).
4. **Idempotent upsert:** Insert missing `OPEN` slots using a unique constraint on `(doctor_id, start_at)` so re-runs never duplicate slots.



### Workflow 2: Override Creation and Cascade

1. Receptionist creates/updates a `ScheduleOverride` for `doctor_id` on a date/window.
2. Transaction starts:
* Locate all `AppointmentSlot` rows for that `doctor_id` within the affected window.
* For `OPEN` slots: mark status `BLOCKED` and link `source_override_id`.
* For `BOOKED` slots: transition the `Appointment` to `CANCELLED_BY_OVERRIDE`, free/block the `AppointmentSlot`, and insert a row into `RescheduleOutreachQueue` with `status = PENDING`.


3. Trigger async notification/task to alert receptionist queue and optionally notify the affected patient.

### Workflow 3: Patient Booking and Cancellation Guardrails

* **Booking:**
1. Verify patient is authenticated (`ROLE_PATIENT`), `slot.status == OPEN`, `slot.start_at > now()`, and `slot.start_at <= now() + 60 days` (and matches primary doctor if restricted to primary doctor).
2. Enforce **one active appointment per patient per day / overlapping times** (recommended guardrail).
3. Execute inside a serializable transaction or atomic conditional update:
```sql
UPDATE appointment_slots
SET status = 'BOOKED', version = version + 1
WHERE id = :slot_id AND status = 'OPEN';

```


If `rowcount == 0`, return `409 Conflict` (slot just taken).
4. Insert `Appointment` (`CONFIRMED`).


* **Cancellation (24-Hour Notice Period):**
* Compute `hours_until_start = (slot.start_at - now()) / 3600`.
* If `hours_until_start < 24`: reject with `422 Unprocessable Entity` or `409 Conflict` (`CANCELLATION_WINDOW_EXPIRED`), explaining that cancellations require at least 24 hours' advance notice (or direct them to contact the clinic).
* If `hours_until_start >= 24`: mark `Appointment` `CANCELLED_BY_PATIENT` and return `AppointmentSlot` to `OPEN` (assuming no override blocks it).



---

## 3. Suggested REST API Surface

Use consistent base path `/api/v1`, JWT/session auth with RBAC guards, and `Idempotency-Key` headers on mutations.

### Auth & Primary Doctor (`ROLE_PATIENT`)

* `POST /api/v1/auth/register` — Register a patient account.
* `GET /api/v1/patients/me` — Fetch patient profile & assigned primary doctor.
* `PUT /api/v1/patients/me/primary-doctor` — Select or change primary doctor (`{ "doctor_id": "uuid" }`).
* `GET /api/v1/doctors` — List doctors and their working weekdays.

### Slots & Appointments

* `GET /api/v1/doctors/{doctor_id}/slots?from=2026-11-21&to=2026-12-20`
* Enforces `to - from <= 60 days` from current date (`ROLE_PATIENT` restricted to their `primary_doctor_id`; `ROLE_RECEPTIONIST` can query any doctor).
* Returns `OPEN` 45-minute slots (`id`, `start_at`, `end_at`, `status`).


* `POST /api/v1/appointments` — Book an appointment (`{ "slot_id": "uuid", "patient_id": "optional-for-receptionist", "reason": "..." }`).
* `GET /api/v1/appointments` — List appointments (`ROLE_PATIENT` sees own; `ROLE_RECEPTIONIST` can filter by `doctor_id`, `patient_id`, `status`, `date`).
* `POST /api/v1/appointments/{appointment_id}/cancel` — Cancel an appointment (enforces 24h rule for `ROLE_PATIENT`).

### Doctor Schedule View (`ROLE_DOCTOR`)

* `GET /api/v1/doctors/me/schedule?view=week&start_date=2026-11-23` (`view=day|week`)
* Returns the doctor's 45-minute slots, booked appointments, patient names/IDs, and active overrides for that range.



### Templates, Overrides & Outreach Queue (`ROLE_RECEPTIONIST`)

* `GET /api/v1/doctors/{doctor_id}/schedule-templates` — List active templates.
* `POST /api/v1/doctors/{doctor_id}/schedule-templates` — Create template (validates **at most 3 weekdays** per doctor).
* `PATCH /api/v1/schedule-templates/{template_id}` — Update start/end time, days, or effective dates.
* `POST /api/v1/doctors/{doctor_id}/schedule-overrides` — Create override and cascade cancellations + queue entries.
* `PATCH /api/v1/schedule-overrides/{override_id}` — Modify or cancel an override.
* `GET /api/v1/reschedule-queue?status=PENDING&doctor_id=...` — View appointments cancelled due to overrides.
* `POST /api/v1/reschedule-queue/{queue_id}/contact-attempts` — Log a call/SMS/email attempt (`{ "channel": "PHONE", "outcome": "NO_ANSWER", "notes": "Left voicemail" }`).
* `PATCH /api/v1/reschedule-queue/{queue_id}` — Update queue status (`RESCHEDULED`, `EXHAUSTED`, `DECLINED`).

---

## 4. Edge Cases You May Have Missed

### 1. Updating a Doctor's Schedule Template ("What happens if...")

* **Future `OPEN` slots already materialized:** Suppose a doctor changes Tuesday from `09:00–12:00` to `13:00–16:00`. Previously generated `OPEN` slots from `09:00–12:00` must be deactivated/deleted (`CANCELLED` / removed) so patients cannot book ghost hours, and new `OPEN` slots for `13:00–16:00` must be generated.
* **Future `BOOKED` slots outside the new template window:** A patient already holds a `09:30` slot, and the template moves to `13:00–16:00`. **Do not silently delete or orphan the appointment.** Flag it for receptionist review / move to `RescheduleOutreachQueue` (or notify receptionist) before dropping the slot.
* **Non-45-minute template durations:** If a template is `09:00–10:10` (70 mins), one 45-min slot fits (`09:00–09:45`) and 25 mins remains. Decide explicitly whether partial intervals are dropped (standard) or padded.
* **Exceeding 3 weekdays:** If a doctor already has templates on Mon, Wed, Fri and a receptionist tries to add Tue, reject with `422` unless the existing template is deactivated/replaced.

### 2. Race Conditions & Double Booking

* Two patients clicking the same 45-minute slot simultaneously can cause double bookings without locking. Use **atomic conditional updates (`status = 'OPEN'`)** or **database exclusion constraints / unique indexes** on `(doctor_id, start_at)` + transaction isolation.

### 3. Timezones & Daylight Saving Time (DST)

* Store all `AppointmentSlot.start_at` and `end_at` in **UTC** (`timestamptz`), while storing `ScheduleTemplate` times in the **clinic/doctor local timezone** (e.g., `America/Toronto`).
* On a **23-hour DST spring-forward day** or **25-hour fall-back day**, local wall-clock times (`09:00–17:00`) convert cleanly to UTC *per date* only if you resolve local time + clinic TZ to UTC during slot materialization—never assume a day is 24 hours or add `86400s` blindly.

### 4. Override Precedence & Overlapping Overrides

* If a doctor has a week-long `VACATION` override (`2026-12-01` to `2026-12-05`) and someone creates a `MODIFIED_HOURS` override for `2026-12-03`, define clear precedence (e.g., `BLOCKED`/`VACATION` wins over `MODIFIED_HOURS`, or reject overlapping overrides with `409 Conflict`).
* If an override is **deleted/cancelled** shortly after creation, decide whether previously `OPEN` slots become `OPEN` again—**never** automatically un-cancel `CANCELLED_BY_OVERRIDE` appointments without confirming with the patient, as they may have already made other plans.

### 5. Changing Primary Doctor with Existing Appointments

* If a `ROLE_PATIENT` switches `primary_doctor_id` from Dr. A to Dr. B, decide whether existing upcoming appointments with Dr. A stay booked (usually yes, so they aren't lost) or require confirmation.

### 6. 24-Hour Cancellation Boundary & Clock Skew

* Evaluate `now()` on the **server in UTC**, not on the client device clock.
* Decide whether "24 hours notice" is strictly greater than 24h (`slot_start - now > 24h`) or greater than or equal to (`>= 24h`), and keep that consistent across API validation and UI display.

### 7. Reschedule Outreach Queue Lifecycle

* What if a patient in the `RescheduleOutreachQueue` books a *new* slot on their own via the patient portal? Automatically mark their pending queue item as `RESCHEDULED` so the receptionist doesn't call someone who already rebooked.
* Track `attempt_count` and timestamps so high-priority or repeatedly missed patients surface clearly.