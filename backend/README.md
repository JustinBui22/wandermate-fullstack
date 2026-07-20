# WanderMate Backend

Spring Boot backend for the WanderMate full-stack trip planning and collaboration app.

The backend provides authentication, OTP verification, JWT access/refresh/session token flows, trip planning APIs, nested destination/activity APIs, role-based collaboration, Cloudinary image upload, Docker support, production deployment configuration, and automated tests.

## Status

| Area | Status |
|---|---|
| Java/Spring backend | Complete for V4 |
| Auth/session system | Complete for portfolio scope |
| OTP flow | Complete for registration and forgot-password flows |
| Trip/destination/activity API | Complete for V4 |
| Collaboration | Owner/editor/viewer, invitations, join requests, share codes, members |
| Uploads | Cloudinary image upload for avatar and trip cover |
| Tests | 438 JUnit test methods, including H2 transaction integration tests |
| Deployment proof | Render health/log screenshots included |

## Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA / Hibernate
- MariaDB
- JWT via JJWT
- Cloudinary SDK
- Spring Mail / Google OAuth helper for email sending
- Docker / Docker Compose
- JUnit 5 / Mockito
- Swagger / OpenAPI

## Base URL

The backend uses this context path:

```text
/Wandermate
```

Local base URL:

```text
http://localhost:8080/Wandermate
```

Render base URL:

```text
https://wandermate-fullstack.onrender.com/Wandermate
```

## API groups

| Group | Purpose |
|---|---|
| `/api/v1/health` | Health check |
| `/api/v1/users` | Register, login, logout, profile and settings |
| `/api/v1/otp` | Send and verify OTP |
| `/api/v1/auth` | Refresh access token |
| `/api/v1/uploads` | Multipart image upload |
| `/api/v1/trips` | Trip CRUD, search, suggestions and collaboration actions |
| `/api/v1/trips/{tripId}/destinations` | Destination CRUD |
| `/api/v1/trips/{tripId}/destinations/{destinationId}/activities` | Activity CRUD under a destination |
| `/api/v1/trips/{tripId}/members` | Member role management |
| `/api/v1/collaboration/summary` | Collaboration dashboard summary |

## Authentication summary

The backend uses a production-style custom auth flow:

1. User registers or logs in with username/password.
2. Passwords are hashed.
3. OTP supports registration and forgot-password flows.
4. Login returns an access token, refresh token and session token.
5. Frontend sends the access token as Bearer token.
6. Frontend sends refresh/session token headers when refreshing.
7. Logout revokes the current session.
8. Protected resources enforce ownership/collaboration access rules.

## Collaboration roles

| Role | Permission summary |
|---|---|
| OWNER | Full trip control, invite, share code, accept/reject requests, manage roles, remove members, edit/delete trip content |
| EDITOR | Can view and edit trip content where allowed |
| VIEWER | Read-only trip access |

## Local run

```bash
cd backend
./mvnw spring-boot:run
```

Required environment variables are read from the runtime environment or `.env` during local setup. Do not commit real `.env` files.

## Docker run

```bash
cd backend
docker compose up --build
```

See:

- [Docker setup](docs/DOCKER_SETUP.md)
- [Fresh start checklist](docs/DOCKER_FRESH_START_CHECKLIST.md)

## Tests

```bash
cd backend
./mvnw test
```

The suite currently defines:

```text
438 JUnit test methods
```

Run `./mvnw test` and replace the test screenshot after a successful local run; the included screenshot shows an older suite count.

Screenshot proof:

![Backend tests](../docs/screenshots/19-backend-tests.png)

## Documentation

- [Backend docs index](docs/README.md)
- [API guide](docs/API_GUIDE.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Auth flow](docs/AUTH_FLOW.md)
- [Cloudinary image storage](docs/CLOUDINARY_IMAGE_STORAGE.md)
- [Database seed](docs/DATABASE_SEED.md)
- [Docker setup](docs/DOCKER_SETUP.md)
- [Operations](docs/OPERATIONS.md)
- [Postman guide](docs/POSTMAN_GUIDE.md)
- [Production API docs](docs/PRODUCTION_API_DOCS.md)
- [Roadmap](docs/ROADMAP.md)

## Security warning

Never commit or share:

- `.env` files
- access/refresh/session tokens
- Google OAuth refresh tokens/client secrets
- Cloudinary API secrets
- DB credentials
- raw database dumps with runtime data
