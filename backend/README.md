# Travelling App Backend

Spring Boot backend for the WanderMate travel planning application. The backend provides authentication, token/session management, OTP verification, trip/destination/activity management, Swagger API documentation, service-level tests, and Docker-based local setup.

This project is designed as a backend portfolio project with real-world backend concerns such as JWT authentication, refresh token handling, session revocation, database-backed configuration, request validation, ownership checks, safe environment variable usage, and a Dockerized demo environment.

---

## Tech Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | MariaDB |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security, JWT, refresh tokens, session tokens |
| API Documentation | Swagger / SpringDoc OpenAPI |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, Maven Surefire |
| Containerization | Docker, Docker Compose |

---

## Current Status

```text
✅ User registration/login/logout implemented
✅ JWT access token authentication implemented
✅ Refresh token rotation, revocation, and reuse detection implemented
✅ Session token handling for active login sessions implemented
✅ Max active session enforcement implemented
✅ Email OTP implemented end-to-end when email OAuth/config is correctly set
✅ Phone/SMS OTP service flow exists and is unit-tested with mocks
✅ Trip, destination, and activity CRUD implemented
✅ Trip/destination/activity date and time validation implemented
✅ Trip and destination overlap warnings implemented with allowOverlap support
✅ Activity overlap is blocked as a hard validation error
✅ Ownership checks protect user-specific data
✅ Standardized response wrapper and error code system implemented
✅ Swagger UI available for manual testing
✅ Docker Compose local backend + MariaDB setup available
✅ 197 service-level backend tests are passing
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

Docker Swagger UI:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

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
ActivityServiceImplTest     25 passed
DestinationServiceImplTest  31 passed
EmailServiceImplTest         9 passed
OtpServiceImplTest          28 passed
SmsServiceImplTest           3 passed
TokenServiceImplTest        33 passed
TripServiceImplTest         39 passed
UserServiceImplTest         29 passed

Total service tests: 197 passed
```

Note: if the default generated `TheProjectApplicationTests.contextLoads` test is present, it starts the full Spring context and requires DB env variables or a dedicated test profile. The main proof of backend business behaviour is the service-level test suite.

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
| [`docs/ROADMAP.md`](docs/ROADMAP.md) | Suggested V1/V2/V3/V4 improvement plan |
