# WanderMate Full Stack

WanderMate is a full-stack mobile travel planning application built with a Spring Boot backend and an Expo React Native frontend. Users can register/login, manage trips, organise destinations, and schedule activities with date/time validation.

This repository is designed as a portfolio project that demonstrates production-style backend API design, JWT authentication, refresh/session token handling, relational data modelling, Docker setup, and mobile frontend integration.

---

## Project Structure

```text
wandermate-fullstack/
├── backend/     # Spring Boot API, MariaDB, auth, tests, Docker setup
└── frontend/    # Expo React Native mobile app
```

---

## Tech Stack

| Area | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security |
| Database | MariaDB, Spring Data JPA / Hibernate |
| Auth | JWT access token, refresh token, session token |
| API Docs | Swagger / SpringDoc OpenAPI |
| Testing | JUnit 5, Mockito, Maven Surefire |
| Frontend | Expo React Native, TypeScript, Expo Router |
| State / Storage | Zustand, Expo SecureStore |
| Containerization | Docker, Docker Compose |

---

## Current Status

```text
✅ Backend CRUD for trips, destinations, and activities is implemented
✅ Ownership checks protect user-specific resources
✅ Trip, destination, and activity date/time validation is implemented
✅ Trip and destination overlap warnings support allowOverlap confirmation
✅ Activity overlap is blocked as a hard validation error
✅ JWT access token + refresh token + session token flow is implemented
✅ Refresh token rotation, revocation, and reuse detection are implemented
✅ Logout revokes the active session and related refresh tokens
✅ Email OTP flow is implemented and works when email OAuth/config is correctly set
✅ Phone/SMS OTP service flow exists and is covered by mocked unit tests
✅ Backend service-level tests are implemented and passing
✅ Docker Compose local backend + MariaDB setup is available
⚠️ Real SMS provider integration is not enabled yet
⚠️ Public Docker demo values do not include real email/OAuth secrets
```

Phone/SMS OTP should be described as prepared backend logic only. The current `SmsServiceImpl` is a stub that returns success, so it does not prove real SMS delivery. Real SMS would require a provider such as Twilio, AWS SNS, Vonage, or another SMS gateway.

---

## Backend Quick Start with Docker

From the backend folder:

```bash
cd backend
cp .env.example .env
docker compose up --build
```

On Windows PowerShell, manually copy `.env.example` and rename the copy to `.env`, or run:

```powershell
cd backend
copy .env.example .env
docker compose up --build
```

Docker backend URL:

```text
http://localhost:8082/The-Project
```

Swagger UI:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

MariaDB host connection:

```text
localhost:3307
```

Inside Docker, the backend connects to MariaDB by service name:

```env
DB_URL=jdbc:mariadb://db:3306/traveling_app
```

---

## Frontend Quick Start

From the frontend folder:

```bash
cd frontend
npm install
npm run android
```

Current frontend local backend setting:

```ts
export const API_BASE_URL = "http://10.0.2.2:8080/The-Project";
```

Use this when the backend is running locally from IntelliJ on port `8080` and the frontend is running on an Android emulator.

For Android emulator connecting to Docker backend, use:

```ts
export const API_BASE_URL = "http://10.0.2.2:8082/The-Project";
```

For browser/Postman connecting to Docker backend, use:

```text
http://localhost:8082/The-Project
```

---

## Main Demo Flow

```text
1. Register with email OTP
2. Login and receive accessToken, refreshToken, and sessionToken
3. Create a trip
4. Add destinations to the trip
5. Add activities to a destination
6. Trigger trip/destination overlap warning and retry with allowOverlap=true
7. Trigger activity overlap validation error
8. Refresh access token
9. Logout and verify session/refresh token revocation
```

---

## Key Documentation

| Document | Purpose |
|---|---|
| `backend/README.md` | Backend-specific setup, architecture, tests, and API overview |
| `backend/docs/API_GUIDE.md` | Endpoint list, headers, request examples, and response shape |
| `backend/docs/AUTH_FLOW.md` | Login, token refresh, logout, max session, and OTP flows |
| `backend/docs/ARCHITECTURE.md` | Layers, domain model, ownership checks, and validation rules |
| `backend/docs/DOCKER_SETUP.md` | Docker Compose setup, ports, env variables, troubleshooting |
| `backend/docs/DATABASE_SEED.md` | Safe local seed data rules and seed file purpose |
| `backend/docs/POSTMAN_GUIDE.md` | Manual API testing order and sample payloads |
| `backend/docs/TESTING.md` | Current test coverage and how to run tests |
| `backend/docs/FRONTEND_INTEGRATION.md` | Frontend/backend URL, token, and error-handling integration notes |
| `backend/docs/ROADMAP.md` | Suggested V1/V2/V3/V4 improvement plan |

---

## Test Status

The current backend service test reports show:

```text
ActivityServiceImplTest     25 passed
DestinationServiceImplTest  31 passed
EmailServiceImplTest         9 passed
OtpServiceImplTest          28 passed
SmsServiceImplTest           3 passed
TokenServiceImplTest        33 passed
TripServiceImplTest         39 passed
UserServiceImplTest         29 passed
```

Total service tests:

```text
197 passed, 0 failures, 0 errors
```

Note: if the default generated `TheProjectApplicationTests.contextLoads` test exists, it needs real DB environment variables or a dedicated test profile because it starts the full Spring ApplicationContext.

---

## Next Improvements

```text
1. Add GitHub Actions CI for backend tests
2. Add screenshots and short demo video/GIF
3. Polish frontend error states and loading states
4. Add collaborator roles: owner/editor/viewer
5. Add trip sharing/invite links
6. Add budgeting and cost sharing
7. Add real SMS provider integration only if phone OTP becomes required
```
