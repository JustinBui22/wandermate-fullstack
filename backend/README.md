# Travelling App Backend

Spring Boot backend for the WanderMate travel planning application. The backend provides authentication, token/session management, OTP verification, trip/destination/activity management, local Swagger API documentation, service/controller tests, production-safe deployment configuration, and Docker-based local setup.

This project is designed as a backend portfolio project with real-world backend concerns such as JWT authentication, refresh token rotation, session revocation, database-backed configuration, request validation, ownership checks, OTP consume-on-success behaviour, safe environment variable usage, production profile configuration, CI/CD, and a Dockerized demo environment.

---

## Tech Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | MariaDB |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security, JWT, refresh tokens, session tokens |
| API Documentation | Swagger / SpringDoc OpenAPI for local development; Markdown docs for production |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, Spring MockMvc, Maven Surefire |
| Containerization | Docker, Docker Compose |
| Deployment | Render |
| CI/CD | GitHub Actions + Render Deploy Hook |

---

## Current Status

```text
✅ User registration/login/logout implemented
✅ JWT access token authentication implemented
✅ Refresh token rotation, revocation, and reuse detection implemented
✅ Session token handling for active login sessions implemented
✅ Max active session enforcement implemented
✅ Email OTP implemented end-to-end when email OAuth/config is correctly set
✅ OTP is consumed/deleted after successful verification to prevent reuse
✅ Forgot-password password validation runs before OTP consumption
✅ Registration and forgot-password flows are transaction-protected
✅ Phone/SMS OTP service flow exists and is unit-tested with mocks
✅ Trip, destination, and activity CRUD implemented
✅ Trip/destination/activity date and time validation implemented
✅ Trip and destination overlap warnings implemented with allowOverlap support
✅ Activity overlap is blocked as a hard validation error
✅ Ownership checks protect user-specific data
✅ Standardized response wrapper and error code system implemented
✅ Swagger UI available for local/manual testing
✅ Swagger/OpenAPI disabled in production profile
✅ Docker Compose local backend + MariaDB setup available
✅ Production profile available for Render deployment
✅ Public health endpoint implemented
✅ Backend CI/CD runs tests before triggering Render deployment
✅ 216 backend tests are passing
⚠️ Real SMS provider integration is not enabled yet
⚠️ Public Docker demo values do not include real email/OAuth secrets
```

Important OTP note: email OTP is the real working OTP flow when email secrets/config are provided. Phone/SMS OTP is prepared at service level and covered with mocked tests, but the current `SmsServiceImpl` is a stub and does not send real SMS.

---

## Architecture Summary

```mermaid
flowchart TD
    Client[Frontend / Swagger / Postman] --> Security[Spring Security Filter Chain]
    Security --> TokenFilter[TokenFilter]
    TokenFilter --> Controller[Controller Layer]
    Controller --> Service[Service Layer]
    Service --> Validator[Validator Layer]
    Service --> Mapper[Mapper Layer]
    Service --> Repository[Repository Layer]
    Repository --> DB[(MariaDB)]
    Service --> Response[CompleteResponse / ResponseBody]
    Response --> Controller
    Controller --> Client
```

Layering:

```text
Controller → Service → Validator / Mapper → Repository → MariaDB
```

Core domain:

```text
User
  └── Trip
        └── Destination
              └── Activity
```

More details: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)

---

## API Base URLs

Local IntelliJ backend:

```text
http://localhost:8080/The-Project
```

Docker backend:

```text
http://localhost:8082/The-Project
```

Render backend:

```text
https://wandermate-fullstack.onrender.com/The-Project
```

Health check:

```text
/api/v1/health
```

Docker Swagger UI:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

Swagger UI is local-development only and is disabled in the production Spring profile.

---

## Docker Local Setup

### 1. Create `.env`

From the backend folder:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
copy .env.example .env
```

### 2. Run Docker

```bash
docker compose up --build
```

### 3. Open Swagger

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

### 4. Stop Docker

```bash
docker compose down
```

Reset database volume and re-import seed SQL:

```bash
docker compose down -v
docker compose up --build
```

More details: [`docs/DOCKER_SETUP.md`](docs/DOCKER_SETUP.md)

---

## Docker Port Mapping

| Service | Host Port | Container Port |
|---|---:|---:|
| Backend | 8082 | 8080 |
| MariaDB | 3307 | 3306 |

Inside Docker, the backend connects to MariaDB using:

```env
DB_URL=jdbc:mariadb://db:3306/traveling_app
```

From the host machine, database clients connect using:

```text
localhost:3307
```

---

## Environment Variables

The real `.env` file is ignored by Git and should contain private local values.

Safe example values are provided in `.env.example`:

```env
DB_NAME=traveling_app
DB_HOST_PORT=3307
DB_USERNAME=traveling_user
DB_PASSWORD=traveling_password
DB_ROOT_PASSWORD=root_password

BACKEND_HOST_PORT=8082
DB_URL=jdbc:mariadb://db:3306/traveling_app
SPRING_JPA_HIBERNATE_DDL_AUTO=update

EMAIL_OAUTH_REFRESH_ENABLED=false
EMAIL_CLIENT_ID=replace_me
EMAIL_CLIENT_SECRET=replace_me
EMAIL_REFRESH_TOKEN=replace_me
EMAIL_TOKEN_URL=https://oauth2.googleapis.com/token
EMAIL_ADDRESS_CONFIG=demo@example.com
```

For private local testing or deployment, real values should be provided through `.env` or the cloud provider environment variable settings.

Do not commit or share real `.env`, local DB dumps, OAuth refresh tokens, access tokens, or generated `target/` files.

---

## Production Profile

Render uses:

```text
SPRING_PROFILES_ACTIVE=prod
```

The production profile is used to reduce development-only behaviour in deployment:

```text
- Disable SQL debug output
- Reduce noisy debug logging
- Disable Swagger/OpenAPI UI in production
```

Production API documentation is handled through markdown docs instead of public Swagger:

```text
docs/PRODUCTION_API_DOCS.md
docs/API_GUIDE.md
docs/AUTH_FLOW.md
docs/POSTMAN_GUIDE.md
```

---

## Authentication Overview

Login returns:

```text
accessToken
refreshToken
sessionToken
```

Protected requests require:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

Refresh token endpoint requires:

```text
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

More details: [`docs/AUTH_FLOW.md`](docs/AUTH_FLOW.md)

---

## OTP and Password Reset Behaviour

OTP behaviour:

```text
- OTP is generated for email or prepared phone/SMS flows
- OTP destination is bound to the original email or phone number
- OTP has expiry validation
- Incorrect/expired OTP attempts increase retry count
- OTP record can be blocked after max retry attempts
- Successful OTP verification deletes the OTP record to prevent reuse
```

Password reset behaviour:

```text
1. Find user by username/email/phone
2. Validate new password pattern
3. Reject password if it matches the old password
4. Verify OTP only after password checks pass
5. Consume OTP on successful verification
6. Save encoded new password
```

`forgotPassword()` is transaction-protected so OTP consumption and password update stay consistent if a later database operation fails.

---

## Main API Areas

| Module | Purpose |
|---|---|
| Users | Register, verify registration details, login, forgot password, logout, check user |
| OTP | Send and verify OTP through email flow or prepared SMS flow |
| Auth | Refresh access token using refresh token + session token |
| Trips | Create, list, detail, update, delete trips; search/suggest travel data |
| Destinations | Create, list, detail, update, delete destinations under trips |
| Activities | Create, list, detail, update, delete activities under destinations |

More details: [`docs/API_GUIDE.md`](docs/API_GUIDE.md)

---

## Backend Tests

Run all backend tests from the backend folder:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw test
```

Current service test status:

```text
ActivityServiceImplTest      25 passed
DestinationServiceImplTest   31 passed
EmailServiceImplTest          9 passed
OtpServiceImplTest           30 passed
SmsServiceImplTest            3 passed
TokenServiceImplTest         33 passed
TripServiceImplTest          39 passed
UserServiceImplTest          35 passed

Total service tests: 205 passed
```

Controller/API tests:

```text
HealthControllerTest           1 passed
UserControllerImplTest         1 passed
TripControllerImplTest         2 passed
DestinationControllerImplTest  1 passed
ActivityControllerImplTest     1 passed
OtpControllerImplTest          3 passed
TokenControllerImplTest        2 passed

Total controller/API tests: 11 passed
```

Total backend tests:

```text
216 passed, 0 failures, 0 errors, 0 skipped
```

Current test coverage includes:

```text
- Auth login/logout/session behaviour
- Refresh token rotation, revocation, and reuse detection
- User registration validation
- OTP send/verify retry, expiry, destination mismatch, and consume-on-success behaviour
- Forgot-password password validation before OTP consumption
- Trip/destination/activity CRUD business rules
- Ownership checks
- Trip/destination overlap warnings
- Activity overlap blocking
- Controller/API status mapping and edge cases
```

Note: if the default generated `TheProjectApplicationTests.contextLoads` test is present, it starts the full Spring context and requires DB env variables or a dedicated test profile. The main proof of backend business behaviour is the service-level test suite plus focused controller/API tests.

More details: [`docs/TESTING.md`](docs/TESTING.md)

---

## Documentation Index

| Document | Purpose |
|---|---|
| [`docs/API_GUIDE.md`](docs/API_GUIDE.md) | Endpoints, request bodies, headers, response format |
| [`docs/AUTH_FLOW.md`](docs/AUTH_FLOW.md) | Login, refresh, logout, OTP, session/token logic |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Layers, domain model, ownership checks, validation rules |
| [`docs/DATABASE_SEED.md`](docs/DATABASE_SEED.md) | Seed file purpose and safe data rules |
| [`docs/DOCKER_SETUP.md`](docs/DOCKER_SETUP.md) | Docker setup and troubleshooting |
| [`docs/POSTMAN_GUIDE.md`](docs/POSTMAN_GUIDE.md) | Manual API testing flow |
| [`docs/TESTING.md`](docs/TESTING.md) | Test coverage and test commands |
| [`docs/FRONTEND_INTEGRATION.md`](docs/FRONTEND_INTEGRATION.md) | Frontend URL, token, refresh, and warning handling |
| [`docs/PRODUCTION_API_DOCS.md`](docs/PRODUCTION_API_DOCS.md) | Production-safe API documentation strategy |
| [`docs/OPERATIONS.md`](docs/OPERATIONS.md) | Health check, production profile, logging, deployment troubleshooting |
| [`docs/ROADMAP.md`](docs/ROADMAP.md) | Suggested V1/V2/V2.5/V3/V4 improvement plan |

---

## Current Next Phase

The backend is now in a strong V2 state. The next project phase is frontend UX polish before screenshots/demo material.

Suggested next work:

```text
1. Extract reusable frontend UI components
2. Centralize or remove development console logs
3. Improve loading, empty, and error states
4. Polish auth/trip/destination/activity screens
5. Take screenshots and create demo video later in V3
```
