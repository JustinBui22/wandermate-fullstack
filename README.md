# WanderMate — Full-Stack Trip Planning and Collaboration App

WanderMate is a mobile trip-planning application with a **Spring Boot 3.5.4** backend and an **Expo 56 / React Native 0.85** frontend. Users can register with OTP verification, sign in with a session-aware token flow, create trips with nested destinations and activities, upload profile/trip images, and collaborate through owner, editor, and viewer roles.

This README describes the implementation contained in this repository. Planned hardening work is listed separately in the roadmap rather than presented as completed functionality.

## Current implementation

| Area | Implemented |
|---|---|
| Authentication | Registration, login, forgot password, email/phone OTP flow, BCrypt passwords |
| Tokens | HS512 JWT access token, HMAC-SHA256-hashed refresh tokens, BCrypt-hashed session tokens |
| Session control | Maximum active sessions, optional oldest-session replacement, logout/revocation |
| Trip planning | Trip CRUD, destination CRUD, nested activity CRUD, overlap/range validation |
| Collaboration | Invitations, direct join requests, share-code join requests, member roles, summary badges |
| Authorization | Backend-enforced `OWNER`, `EDITOR`, and `VIEWER` trip permissions |
| Media | Authenticated Cloudinary upload for profile images and trip covers |
| Mobile app | Expo Router screens, SecureStore token persistence, automatic access-token refresh, themes |
| Automation | Backend and frontend GitHub Actions workflows; Render deploy hook after backend tests |
| Tests in repository | 443 backend tests passing; 10 frontend unit/component test cases declared |

## Technology stack

### Backend

- Java 21
- Spring Boot 3.5.4
- Spring Web, Spring Security, Spring Data JPA, Bean Validation
- MariaDB 11 for local Docker/runtime data
- H2 in MariaDB compatibility mode for integration tests
- JJWT 0.11.5
- Cloudinary Java SDK 2.4.0
- Spring Mail with Gmail OAuth2 configuration support
- Springdoc OpenAPI 2.8.17
- Maven Wrapper and Docker multi-stage builds
- JUnit 5, Mockito, Spring Boot Test

### Frontend

- Expo 56.0.16 and Expo Router 56.2.15
- React 19.2.3 and React Native 0.85.3
- TypeScript 6.0
- Axios
- Zustand
- Expo SecureStore
- React Hook Form and Zod
- Expo Image Picker, Clipboard, Linking, and DateTimePicker
- Vitest, Jest, React Native Testing Library, and Maestro

## Repository layout

```text
wandermate-fullstack/
├── backend/                 Spring Boot API, Docker setup, seed data and backend docs
├── frontend/                Expo mobile/web client
├── docs/                    Portfolio/demo/screenshot documentation
├── .github/workflows/       Backend CI/CD and frontend CI
└── README.md                Project overview and setup
```

## Architecture overview

```text
Expo Router screens
        ↓
Frontend API modules / Axios interceptors
        ↓
Spring controller interfaces + implementations
        ↓
Service interfaces + implementations
        ↓
Validators / mappers / repositories
        ↓
MariaDB and Cloudinary
```

Protected API requests normally carry both:

```http
Authorization: Bearer <access-token>
Session-Token: <session-token>
```

Access-token refresh uses:

```http
Refresh-Token: <refresh-token>
Session-Token: <session-token>
```

## Main features

### Authentication and sessions

- Registration details are validated before registration.
- OTPs can be sent by email or phone according to request configuration.
- Registration and forgot-password requests verify an OTP before changing account data.
- Login accepts username, email, or Vietnamese phone-number form through backend lookup logic.
- Successful login returns an access token, refresh token, and session token.
- The frontend stores all three tokens in Expo SecureStore.
- Axios attaches access/session headers, refreshes an expired access token once, and retries the original request.
- A normal permission-only `403` does not clear the local session; explicit invalid-session/token responses do.
- Refresh-token values are HMAC-SHA256 hashed before persistence.
- Session-token values are BCrypt hashed before persistence.
- Refresh-token reuse detection revokes the related active token family and session in the locked refresh transaction.

### Trip planning

- Create, list, view, update, and delete trips.
- Filter trips by ownership/status and sort by name/created/modified date.
- Search or suggest cities, restaurants, and accommodations.
- Create destinations under a trip.
- Create activities under a destination.
- Validate trip, destination, and activity ranges and overlap rules.
- Store trip and destination boundaries as calendar-only `LocalDate` values.
- Keep activity schedules as destination-local `LocalDateTime` wall-clock values.
- Store audit, security, and expiry timestamps as UTC `Instant` values.

### Collaboration

- The trip creator is inserted as the `OWNER` member.
- `OWNER` can invite members, manage join requests, generate share codes, update roles, and remove members.
- `EDITOR` can view and edit trip-plan content where the service uses `assertCanEdit`.
- `VIEWER` can view accessible content but cannot perform edit or owner-only actions.
- Users can accept/reject invitations and submit direct or share-code join requests.
- The collaboration summary endpoint provides pending-work counts for the mobile dashboard.
- Invalid share-code attempts are counted and can trigger a temporary restriction.

### Image upload

- Authenticated multipart upload endpoint.
- Supported upload categories: `profile-images` and `trip-covers`.
- Five-megabyte Spring multipart limit.
- Backend content validation checks declared type, signature/container markers, and PNG/JPEG decoding.
- Cloudinary public IDs are generated under a per-user folder.
- Profile/trip updates validate that newly assigned Cloudinary references match the authenticated uploader pattern.
- Replaced profile and trip-cover images are deleted through the Cloudinary client after a successful update.

## Backend URLs

The backend context path is `/Wandermate`.

| Environment | Base URL |
|---|---|
| IntelliJ/Maven default | `http://localhost:8080/Wandermate` |
| Docker Compose host | `http://localhost:8082/Wandermate` |
| Configured Render service | `https://wandermate-fullstack.onrender.com/Wandermate` |

Local Swagger UI:

```text
http://localhost:8080/Wandermate/swagger-ui/index.html
```

The production profile disables Swagger UI and `/v3/api-docs`.

## Environment configuration

A `.env` file does not execute by itself. It is used only by a tool that loads it.

| Runtime | How variables are loaded |
|---|---|
| Docker Compose | Automatically reads `backend/.env` beside `docker-compose.yml` |
| IntelliJ direct run | Add variables to the Run Configuration or configure an env-file loader |
| Maven direct run | Export/set variables in the shell before starting Spring Boot |
| Render | Configure/import variables in the Render service environment |
| Expo | Expo loads `frontend/.env` variables prefixed with `EXPO_PUBLIC_` |

Backend-required runtime values include:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET                    # at least 64 UTF-8 bytes
REFRESH_TOKEN_HASH_SECRET     # at least 32 UTF-8 bytes
```

Cloudinary and email variables are required only for the corresponding functionality. Copy the provided templates; never commit real `.env` files.

## Local development

### Option A — Docker Compose backend and database

```bash
cd backend
cp .env.example .env
# Replace placeholders and secrets in .env
docker compose up --build
```

Backend: `http://localhost:8082/Wandermate`

### Option B — MariaDB in Docker, backend in IntelliJ

1. Start the database service:

   ```bash
   cd backend
   docker compose up -d db
   ```

2. Configure the IntelliJ Spring Boot run environment. Use a host-accessible database URL:

   ```text
   DB_URL=jdbc:mariadb://localhost:3307/traveling_app
   DB_USERNAME=traveling_user
   DB_PASSWORD=traveling_password
   JWT_SECRET=<64-or-more-byte-secret>
   REFRESH_TOKEN_HASH_SECRET=<32-or-more-byte-secret>
   ```

3. Run `TheProjectApplication`.

### Frontend

```bash
cd frontend
cp .env.example .env
npm ci
npm run start
```

For an Android emulator with the IntelliJ backend:

```text
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080/Wandermate
```

For an Android emulator with Docker:

```text
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/Wandermate
```

For a physical device, use the development computer's LAN IP or the configured production URL. Tunnel mode can help when Expo asset delivery over LAN fails:

```bash
npx expo start --tunnel -c
```

## Verification

### Backend

```bash
cd backend
./mvnw test
```

The included Surefire reports record:

```text
443 tests, 0 failures, 0 errors, 0 skipped
```

### Frontend

```bash
cd frontend
npm run typecheck
npm test
```

`npm test` runs Vitest followed by Jest. The repository currently declares 10 frontend test cases across API authentication behavior, session lifecycle, auth controls, and the shared button component.

### End-to-end smoke flow

```bash
cd frontend
maestro test .maestro/login-smoke.yml
```

An EAS workflow builds the `e2e-test` Android profile and runs the Maestro login-screen smoke flow on pull requests.

## CI/CD

- `backend-ci-cd.yml` runs backend tests for backend-related pushes/PRs to `main`.
- A successful backend push to `main` triggers the configured Render deploy hook.
- `frontend-ci.yml` runs `npm ci`, TypeScript validation, and frontend tests.
- The production Spring profile disables SQL/debug logging and OpenAPI endpoints.
- Production logging omits authentication secrets, OTP/account destinations, share codes, Cloudinary asset references, request details, SQL bind values and HTTP wire/header dumps.

## Current implementation notes

The following facts are important when evaluating or extending this version:

- Flyway owns schema changes and JPA validates the production schema with `spring.jpa.hibernate.ddl-auto=validate`.
- The Docker SQL seed runs only when MariaDB initializes a new named volume.
- Public endpoints are defined in code through a shared, HTTP-method-specific `PublicEndpointMatcher` used by both Spring Security and `TokenFilter`.
- Account lookup requires authentication, returns only a generic `exists` boolean, and is rate-limited per authenticated account. Public registration verification and OTP-send entry points are rate-limited by source address. Login and password-reset account mismatches use generic responses.
- CORS origins are configured through `CORS_ALLOWED_ORIGINS`; allowed request headers include `Authorization`, `Session-Token`, and `Refresh-Token`.
- OTP records store purpose-bound HMAC hashes rather than reusable plaintext OTP values.
- Share-code preview is an authenticated `POST /api/v1/trips/share-codes/preview` endpoint because preview attempts update rate-limit state.
- Share codes use `SecureRandom`, a `WM-` prefix, and a twelve-character unambiguous alphabet. Generation and redemption use pessimistic locking.
- Trip/destination dates use `yyyy-MM-dd`; activity schedules remain timezone-free local wall-clock values; audit/expiry timestamps serialize as UTC instants.
- Google email OAuth refresh currently creates its own scheduled executor; lifecycle management is a roadmap improvement.

## Screenshots and portfolio evidence

Screenshots are under [`docs/screenshots`](docs/screenshots). The evidence checklist is maintained in [`docs/SCREENSHOT_CHECKLIST.md`](docs/SCREENSHOT_CHECKLIST.md).

Do not publish screenshots containing access tokens, refresh tokens, session tokens, OTPs, credentials, or personal data.

## Documentation

### Root/portfolio

- [Demo script](docs/DEMO_SCRIPT.md)
- [Project file audit](docs/PROJECT_FILE_AUDIT.md)
- [Screenshot checklist](docs/SCREENSHOT_CHECKLIST.md)

### Backend

- [Backend README](../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/README.md)
- [Backend documentation index](../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/README.md)
- [API guide](../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/API_GUIDE.md)
- [Architecture](../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/ARCHITECTURE.md)
- [Authentication flow](../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/AUTH_FLOW.md)
- [Docker setup](backend/docs/DOCKER_SETUP.md)
- [Operations](backend/docs/OPERATIONS.md)
- [Production logging](backend/docs/PRODUCTION_LOGGING.md)
- [Roadmap](../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/ROADMAP.md)

### Frontend

- [Frontend README](../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/frontend/README.md)
