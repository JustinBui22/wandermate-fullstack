# Backend Roadmap

This roadmap starts from the implementation currently present in the repository.

## Recently completed security work

- Replaced database-controlled path-only public routes with a code-owned, HTTP-method-specific `PublicEndpointMatcher`.
- Shared the matcher between `SecurityConfig` and `TokenFilter`.
- Added explicit configurable CORS handling for supported frontend origins and authentication headers.
- Standardized unexpected `TokenFilter` failures through the existing JSON response structure.
- Verified the backend with 443 passing tests, 0 failures, 0 errors and 0 skipped.

## Priority 1 — Security and concurrency hardening

- Replace plaintext OTP persistence with purpose-bound HMAC hashes.
- Store OTP purpose with the record and verify it during registration/password reset.
- Add pessimistic/atomic locking to the main OTP lookup/consume path.
- Serialize refresh-token rotation by locking the token-hash row.
- Rework refresh-token reuse revocation so it does not depend on a second unlocked lookup.
- Generate share codes with `SecureRandom` and a longer unambiguous alphabet.
- Lock active share-code generation/use to prevent concurrent duplicates or reuse.
- Change share-code preview from state-sensitive GET semantics if attempt accounting remains attached.
- Remove or redesign `/api/v1/users/check` to reduce account enumeration.

## Priority 2 — Database lifecycle

- Add Flyway or Liquibase.
- Convert the current schema into a reviewed baseline migration.
- Set production Hibernate mode to `validate` rather than `update`.
- Add migrations for constraints and indexes instead of relying on startup mutation.
- Document backup/restore and rollback procedures for Render/MariaDB hosting.

## Priority 3 — Date model consistency

- Decide whether trips/destinations are calendar dates or instants.
- Prefer `LocalDate` for all-day trip/destination boundaries if time-of-day is not meaningful.
- Keep activities as `LocalDateTime` and document timezone assumptions.
- Add end-to-end tests for Australian timezone/date conversion and same-day trips.

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
