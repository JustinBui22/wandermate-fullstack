# WanderMate Backend Documentation

This directory documents the current backend implementation. The code and Flyway migrations remain the source of truth when documentation and implementation differ.

| Document | Purpose |
|---|---|
| [API guide](API_GUIDE.md) | Endpoints, headers, request shapes and response behavior |
| [Architecture](ARCHITECTURE.md) | Layers, security boundaries, persistence and external services |
| [Authentication flow](AUTH_FLOW.md) | Email OTP, demo phone OTP, login, sessions, refresh rotation and logout |
| [Date and time model](DATE_TIME_MODEL.md) | Calendar dates, local activity time and UTC operational timestamps |
| [Docker setup](DOCKER_SETUP.md) | Local MariaDB/backend containers and Flyway startup |
| [Docker fresh-start checklist](DOCKER_FRESH_START_CHECKLIST.md) | Rebuild an empty local database safely |
| [Operations](OPERATIONS.md) | Runtime checks, schema validation, logs and common recovery actions |
| [Database backup and recovery](DATABASE_BACKUP_AND_RECOVERY.md) | Backup/restore procedure and Flyway checks |
| [Database reference data](DATABASE_SEED.md) | V1 reference rows and the status of legacy `init.sql` |
| [Frontend integration](FRONTEND_INTEGRATION.md) | Mobile headers, token lifecycle, dates and API handling |
| [Postman guide](POSTMAN_GUIDE.md) | Manual API verification |
| [Production API docs](PRODUCTION_API_DOCS.md) | Production Swagger/OpenAPI policy |
| [Cloudinary image storage](CLOUDINARY_IMAGE_STORAGE.md) | Upload validation, paths and cleanup |
| [CI/CD](CI_CD.md) | Build, MariaDB migration and Render health verification |
| [Production logging](PRODUCTION_LOGGING.md) | Sensitive-data logging rules |
| [Security scanning](SECURITY_SCANNING.md) | npm, OWASP Dependency-Check, Gitleaks, CodeQL and Dependabot |
| [Roadmap and maintenance](ROADMAP.md) | Completed hardening work and optional future maintenance |

## Current baseline

- Java 21 / Spring Boot 3.5.4.
- MariaDB runtime database.
- Flyway V1–V6 owns schema changes.
- Hibernate uses `ddl-auto=validate`.
- Trip/destination ranges use `LocalDate`.
- Activity schedules use local `LocalDateTime` values.
- Audit/security/expiry values use UTC `Instant`.
- Email OTP is operational for registration and password recovery.
- Phone OTP remains as a demo-only simulated path; no real SMS gateway is configured.
- Phone number remains valid account/profile data.
- Current included Surefire evidence: 487 tests, 0 failures, 0 errors and 0 skipped.
