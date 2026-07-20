# WanderMate Backend Documentation

This directory documents the backend implementation currently present in the repository.

| Document | Purpose |
|---|---|
| [API guide](API_GUIDE.md) | Endpoint inventory, headers, query parameters and request examples |
| [Architecture](ARCHITECTURE.md) | Packages, layers, data flow and transaction boundaries |
| [Authentication flow](AUTH_FLOW.md) | Registration, OTP, login, access/refresh/session tokens and logout |
| [Cloudinary image storage](CLOUDINARY_IMAGE_STORAGE.md) | Upload validation, folder/public-ID rules and replacement cleanup |
| [Database seed](DATABASE_SEED.md) | Docker schema/reference data and fresh-volume behavior |
| [Docker setup](DOCKER_SETUP.md) | Environment variables, ports and Compose commands |
| [Docker fresh-start checklist](DOCKER_FRESH_START_CHECKLIST.md) | Safe reset and verification sequence |
| [Frontend integration](FRONTEND_INTEGRATION.md) | API base URL, headers, response handling, dates, roles and uploads |
| [Operations](OPERATIONS.md) | Health, profiles, CI/CD, logs, backups and secrets |
| [Postman guide](POSTMAN_GUIDE.md) | Environment setup and representative request flows |
| [Production API docs](PRODUCTION_API_DOCS.md) | Local OpenAPI/Swagger and production exposure policy |
| [Roadmap](ROADMAP.md) | Verified limitations and prioritized next work |

## Source of truth

When documentation and code differ, use these files to verify current behavior:

- `controller/*.java` for routes and HTTP methods.
- `dto/request/**` for request fields.
- `service/impl/**` for permissions and business rules.
- `application.properties` and `application-prod.properties` for runtime configuration.
- `docker/init/init.sql` for the clean Docker schema/reference data.
- `src/test` and `target/surefire-reports` for the current test inventory/result evidence.

## Current version facts

- Context path: `/Wandermate`.
- Direct local port: `8080`.
- Docker host port: `8082` by default.
- JPA schema mode: `ddl-auto=update`.
- Database migrations: not configured.
- Production Swagger: disabled.
- Backend test evidence: 438 passing tests in the included Surefire reports.
