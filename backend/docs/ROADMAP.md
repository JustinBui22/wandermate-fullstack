# Backend Roadmap

This roadmap starts from the implementation currently present in the repository.

## Recently completed security work

- Replaced database-controlled path-only public routes with a code-owned, HTTP-method-specific `PublicEndpointMatcher`.
- Shared the matcher between `SecurityConfig` and `TokenFilter`.
- Added explicit configurable CORS handling for supported frontend origins and authentication headers.
- Standardized unexpected `TokenFilter` failures through the existing JSON response structure.
- Verified the backend with 443 passing tests, 0 failures, 0 errors and 0 skipped.
- Replaced UUID-substring share codes with longer `SecureRandom` codes using an unambiguous alphabet.
- Serialized active-code generation and redemption with pessimistic database locks.
- Changed state-sensitive share-code preview from GET to POST.
- Protected and rate-limited `/api/v1/users/check`, removed canonical-username disclosure, and standardized found/not-found responses. Public registration verification and OTP-send entry points are also source-rate-limited.
- Confirmed generic login and password-reset account-mismatch behaviour and documented the remaining registration-availability trade-off.
- Standardized the date model: trips/destinations use `LocalDate`, activities retain local wall-clock `LocalDateTime`, and audit/security/expiry fields use UTC `Instant`.
- Added Flyway V5 for calendar-date columns and frontend date-only parsing that avoids timezone day shifts.

## Priority 1 — Security and concurrency hardening

- Replace plaintext OTP persistence with purpose-bound HMAC hashes.
- Store OTP purpose with the record and verify it during registration/password reset.
- Add pessimistic/atomic locking to the main OTP lookup/consume path.
- Serialize refresh-token rotation by locking the token-hash row.
- Rework refresh-token reuse revocation so it does not depend on a second unlocked lookup.

## Priority 2 — Database lifecycle

- Add Flyway or Liquibase.
- Convert the current schema into a reviewed baseline migration.
- Set production Hibernate mode to `validate` rather than `update`.
- Add migrations for constraints and indexes instead of relying on startup mutation.
- Document backup/restore and rollback procedures for Render/MariaDB hosting.

## Priority 3 — Date model consistency

Completed in Phase 9:

- Trip and destination boundaries are calendar-only `LocalDate` values and allow same-day ranges.
- Activity schedules remain destination-local `LocalDateTime` wall-clock values.
- Audit, security, and expiry timestamps use UTC `Instant` values.
- Flyway V5 converts trip and destination columns to SQL `DATE` without editing earlier migrations.
- Frontend date-only parsing avoids JavaScript UTC day shifts.

Remaining follow-up:

- Add destination IANA timezone metadata if activities later need conversion into globally comparable instants.
- Add device-level E2E coverage around daylight-saving boundaries.

## Priority 4 — Image lifecycle

- Add authenticated deletion of unreferenced uploads.
- Clean up replaced/abandoned uploads when a form is cancelled or save fails.
- Add database uniqueness/pair constraints for URL/public-ID references.
- Add periodic orphan detection or Cloudinary asset retention policy.

## Priority 5 — Email/OTP operations

- Replace the custom `ScheduledExecutorService` with a Spring-managed scheduler/executor.
- Add explicit shutdown behavior and refresh-token rotation handling.
- Separate production email templates and test/dev delivery modes.
- Implement a real SMS provider or clearly disable phone OTP outside test/demo mode.

## Priority 6 — API and error behavior

- Add method-specific OpenAPI documentation for custom headers and response wrappers.
- Normalize validation/JSON/type-mismatch exceptions through `GlobalExceptionHandler`.
- Add pagination for trip/member/request lists.
- Introduce idempotency protection for sensitive POST/PATCH operations where useful.
- Review whether profile/account lookup responses leak account existence.

## Priority 7 — Test and quality expansion

- Add frontend integration tests for trip/destination/activity date payloads.
- Add component tests for role-aware actions and image replacement.
- Expand Maestro beyond login-screen smoke to login, trip create and collaboration flows.
- Add Testcontainers MariaDB tests for SQL/locking behavior that H2 cannot reproduce.
- Add coverage reporting and dependency/security scanning.

## Priority 8 — Product improvements

- Push/deep-link handling for collaboration invitations.
- Offline/poor-network UX and request retry policy.
- Trip timeline/calendar view.
- Activity ordering and richer itinerary metadata.
- Member audit trail and collaboration notifications.
