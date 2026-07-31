# WanderMate — Full-Stack Trip Planning and Collaboration App

WanderMate is a mobile trip-planning and collaboration application built with a **Spring Boot 3.5.4 / Java 21** backend and an **Expo 56 / React Native 0.85** frontend.

Users can register with OTP verification, sign in with a session-aware token flow, create trips with destinations and activities, upload images, and collaborate through owner, editor, and viewer roles. Email OTP is operational; the phone-OTP path is retained as a demo-only simulation because no paid SMS gateway is configured.

## Project status

The planned backend security, database, CI/CD, logging, and dependency-scanning hardening work is complete. Remaining work is normal maintenance rather than another required implementation phase.

| Area | Current implementation |
|---|---|
| Authentication | Operational email OTP, demo-only simulated phone OTP, BCrypt passwords and generic account-recovery responses |
| Tokens | HS512 JWT access tokens, HMAC-hashed refresh tokens, BCrypt-hashed session tokens |
| Session control | Maximum active sessions, oldest-session replacement, logout/revocation, refresh-token reuse detection |
| Trip planning | Trip, destination, and nested activity CRUD with overlap/range validation |
| Collaboration | Invitations, join requests, share codes, role management, collaboration summaries |
| Authorization | Backend-enforced `OWNER`, `EDITOR`, and `VIEWER` permissions |
| Media | Authenticated Cloudinary upload for profile images and trip covers |
| Database | MariaDB, Flyway V1–V6, Hibernate schema validation |
| Automation | Backend/frontend CI, fresh MariaDB migration verification, tracked Render deployment, CodeQL, Gitleaks, npm audit, Dependabot |
| Tests | 487 backend tests and 13 frontend unit/component test cases in the current repository evidence |

> Phone OTP remains available in the UI/API for demonstration, but `SmsServiceImpl` only simulates a successful send and does not contact a real SMS gateway. A paid provider is intentionally not configured for this portfolio project; use email OTP for a real end-to-end demonstration.

## Technology stack

### Backend

- Java 21
- Spring Boot 3.5.4
- Spring Web, Security, Data JPA, Validation and Mail
- MariaDB 11
- H2 in MariaDB compatibility mode for fast test scenarios
- Flyway migrations
- JJWT 0.11.5
- Cloudinary Java SDK 2.4.0
- Springdoc OpenAPI 2.8.17
- JUnit 5, Mockito, Spring Boot Test and JaCoCo

### Frontend

- Expo `~56.0.17`
- Expo Router `~56.2.16`
- React 19.2.3
- React Native 0.85.3
- TypeScript 6
- Axios, Zustand and Expo SecureStore
- React Hook Form and Zod
- Vitest, Jest, React Native Testing Library and Maestro

## Repository layout

```text
wandermate-fullstack/
├── backend/                 Spring Boot API, Flyway migrations, Docker setup and backend docs
├── frontend/                Expo mobile/web client
├── docs/                    Demo, repository-audit and screenshot guidance
├── .github/workflows/       Build, deployment and security workflows
└── README.md                Project overview
```

## Architecture

```text
Expo Router screens
        ↓
Frontend API modules and Axios interceptors
        ↓
Spring controllers
        ↓
Services, validators and mappers
        ↓
JPA repositories
        ↓
MariaDB / Cloudinary / email provider
```

Protected requests use:

```http
Authorization: Bearer <access-token>
Session-Token: <session-token>
```

Refresh requests use:

```http
Refresh-Token: <refresh-token>
Session-Token: <session-token>
```

## Main features

### Authentication and sessions

- Registration details are pre-validated before an OTP request.
- Email OTP is delivered through the configured mail provider.
- Phone OTP is a demo-only simulated path and does not deliver a real SMS.
- Registration and password reset verify purpose-bound OTP records.
- OTP values are stored as purpose-bound HMAC hashes, not plaintext codes.
- Login accepts username, email, or supported phone-number format.
- Login returns access, refresh, and session tokens.
- The frontend stores tokens in Expo SecureStore.
- Axios refreshes an expired access token once and retries the original request.
- Refresh-token rows are locked during rotation.
- Refresh-token reuse revokes the compromised token family and session.
- Login and password-recovery responses avoid unnecessary account enumeration.

### Trip planning

- Trip CRUD and accessible-trip listing.
- Destination CRUD under trips.
- Activity CRUD under destinations.
- Date-range and overlap validation.
- Search and suggestion support for travel-related places.

### Collaboration

- Owner-created membership.
- Invitations and direct/share-code join requests.
- `OWNER`, `EDITOR`, and `VIEWER` roles.
- Pessimistic locking during share-code generation and redemption.
- Secure random share codes with collision handling and one-active-code-per-trip enforcement.
- Rate limiting for invalid share-code attempts.

### Images

- Authenticated multipart uploads.
- Profile-image and trip-cover categories.
- Five-megabyte request limit.
- PNG/JPEG validation.
- Per-user Cloudinary paths.
- Cleanup of replaced images.

## Date and time model

| Concept | Java/API model |
|---|---|
| Trip start/end | `LocalDate`, `yyyy-MM-dd` |
| Destination start/end | `LocalDate`, `yyyy-MM-dd` |
| Activity start/end | `LocalDateTime`, destination-local wall-clock value |
| Audit/security/expiry timestamps | UTC `Instant`, serialized with `Z` |

See [Date and time model](backend/docs/DATE_TIME_MODEL.md).

## Database ownership

Flyway is the only schema-change mechanism.

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.jpa.hibernate.ddl-auto=validate
```

- Flyway applies migrations that are not present in `flyway_schema_history`.
- Hibernate validates the migrated schema and does not repair it.
- Do not edit V1–V6 after they have been applied.
- Future schema changes must use a new migration such as `V7__description.sql`.
- `backend/docker/init/init.sql` is legacy reference material and is not mounted by the current Docker Compose configuration.

## URLs

The backend context path is `/Wandermate`.

| Environment | Base URL |
|---|---|
| IntelliJ/Maven | `http://localhost:8080/Wandermate` |
| Docker Compose | `http://localhost:8082/Wandermate` |
| Render | `https://wandermate-fullstack.onrender.com/Wandermate` |

Health endpoint:

```text
https://wandermate-fullstack.onrender.com/Wandermate/api/v1/health
```

Local Swagger UI:

```text
http://localhost:8080/Wandermate/swagger-ui/index.html
```

Swagger and OpenAPI JSON are disabled in the production profile.

## Environment configuration

A `.env` file is plain text until a tool loads it.

| Runtime | Variable loading |
|---|---|
| Docker Compose | Reads `backend/.env` beside `docker-compose.yml` |
| IntelliJ | Run Configuration environment variables or an env-file plugin |
| Maven/direct Java | Shell environment variables |
| Render | Service environment variables |
| Expo | `EXPO_PUBLIC_` variables from the frontend environment |

Required backend values:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
REFRESH_TOKEN_HASH_SECRET
OTP_HASH_SECRET
```

Optional values cover Cloudinary, email OAuth, CORS, rate limits, timezone and port configuration. Use the committed `.env.example` templates and never commit real `.env` files.

## Local development

### Docker backend and MariaDB

```bash
cd backend
cp .env.example .env
# Replace placeholders in .env
docker compose up --build
```

Flyway creates the schema on an empty database. Resetting the volume deletes all local database data:

```bash
docker compose down -v
docker compose up --build
```

### MariaDB in Docker, backend in IntelliJ

```bash
cd backend
docker compose up -d db
```

Use a host-accessible datasource:

```text
DB_URL=jdbc:mariadb://localhost:3307/traveling_app
```

Configure the required secrets in the IntelliJ Run Configuration and run `TheProjectApplication`.

### Frontend

```bash
cd frontend
cp .env.example .env
npm ci
npm run start
```

Android emulator with IntelliJ backend:

```text
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080/Wandermate
```

Android emulator with Docker backend:

```text
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/Wandermate
```

## Verification

### Backend

```bash
cd backend
./mvnw clean verify
```

Current included Surefire evidence:

```text
487 tests, 0 failures, 0 errors, 0 skipped
```

### Frontend

```bash
cd frontend
npm ci
npm run typecheck
npm test
npx expo config --type public --json
npx expo export --platform web --output-dir dist
```

The repository currently declares 13 frontend test cases across Axios authentication behavior, session lifecycle, date-only utilities, auth controls, and shared UI components.

## CI/CD and security checks

- Backend CI runs `clean verify`, uploads JAR/test/coverage artifacts, and starts the packaged application against an empty MariaDB service.
- The MariaDB job proves Flyway V1–V6 can build a fresh schema and Hibernate can validate it.
- Main-branch backend deployment tracks the exact Render deployment and polls the health endpoint.
- Frontend CI runs clean installation, type checking, tests, component coverage, Expo config resolution and static web export.
- Security CI warns on high npm production findings, fails on critical findings, and scans Git history with Gitleaks.
- CodeQL analyzes Java and JavaScript/TypeScript.
- Dependabot monitors npm and Maven dependencies and opens reviewable npm, Maven, GitHub Actions and Docker update PRs.

## Documentation

### Portfolio and review

- [Demo script](docs/DEMO_SCRIPT.md)
- [Project file audit](docs/PROJECT_FILE_AUDIT.md)
- [Screenshot checklist](docs/SCREENSHOT_CHECKLIST.md)

### Backend

- [Backend README](backend/README.md)
- [Backend documentation index](../Downloads/wandermate-updated-docs(2)/backend/docs/README.md)
- [API guide](backend/docs/API_GUIDE.md)
- [Architecture](backend/docs/ARCHITECTURE.md)
- [Authentication flow](backend/docs/AUTH_FLOW.md)
- [Date and time model](backend/docs/DATE_TIME_MODEL.md)
- [Docker setup](backend/docs/DOCKER_SETUP.md)
- [Operations](backend/docs/OPERATIONS.md)
- [CI/CD](../Downloads/wandermate-updated-docs(2)/backend/docs/CI_CD.md)
- [Production logging](backend/docs/PRODUCTION_LOGGING.md)
- [Security scanning](../Downloads/wandermate-updated-docs(2)/backend/docs/SECURITY_SCANNING.md)
- [Roadmap and maintenance](../Downloads/wandermate-updated-docs(2)/backend/docs/ROADMAP.md)

### Frontend

- [Frontend README](../Downloads/wandermate-updated-docs(2)/frontend/README.md)

## Sharing the repository

Do not zip the working directory because it may contain ignored secrets and generated files. Create a source-only archive from Git:

```bash
git archive --format=zip --output=wandermate-source.zip HEAD
```

Review screenshots before publishing. Never expose tokens, OTPs, session IDs, credentials, personal account data, database connection details, or Cloudinary secrets.
