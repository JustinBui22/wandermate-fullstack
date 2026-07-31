# Completed Hardening Roadmap and Maintenance

## Completed project work

- Centralized method-specific public endpoint matching and CORS.
- Standardized token-filter and Spring Security JSON errors.
- Added Flyway and moved schema ownership away from Hibernate mutation.
- Added purpose-bound HMAC OTP storage.
- Hardened refresh-token rotation with pessimistic locking and consistent reuse handling.
- Hardened share-code generation/redemption concurrency and changed preview to state-changing `POST` semantics.
- Added account-enumeration protections and rate limiting.
- Standardized date/time handling with `LocalDate`, local `LocalDateTime` and UTC `Instant`.
- Standardized common Spring MVC, multipart, authentication and authorization error responses.
- Sanitized production logging.
- Added backend `clean verify`, JaCoCo, fresh MariaDB Flyway/Hibernate verification and tracked Render health checks.
- Added Expo config/export validation.
- Added npm audit, Gitleaks, CodeQL and Dependabot dependency monitoring.
- Verified the included backend test evidence at 487 tests with no failures/errors.
- Documented phone OTP as a demo-only simulated path; email OTP remains the operational delivery flow.

## Project status

The required implementation and hardening roadmap is complete. Do not continue adding architecture solely to make the project look larger.

## Normal maintenance

- Review Dependabot and security findings.
- Keep screenshots and test evidence current and sanitized.
- Add a new Flyway migration for future database changes.
- Re-run backend/frontend/CI verification after dependency changes.
- Rotate secrets when exposure is suspected.

## Optional future enhancements

These are not required for portfolio completion:

- a complete Maestro business-flow E2E suite;
- MariaDB Testcontainers concurrency tests;
- Redis-backed distributed rate limiting for horizontal scaling;
- asynchronous queued email delivery;
- replace the simulated phone-OTP path with a paid SMS provider and real delivery/failure handling if it becomes a product requirement;
- broader frontend component coverage;
- scheduled cleanup of abandoned or unreferenced Cloudinary assets;
- offline-first mobile behavior.
