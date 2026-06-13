# WanderMate Full Stack

[![Backend CI/CD](https://github.com/JustinBui22/wandermate-fullstack/actions/workflows/backend-ci-cd.yml/badge.svg?branch=main)](https://github.com/JustinBui22/wandermate-fullstack/actions/workflows/backend-ci-cd.yml)

WanderMate is a full-stack mobile travel planning application built with a Spring Boot backend and an Expo React Native frontend. Users can register and log in, manage trips, organise destinations, and schedule activities with date/time validation.

This repository is designed as a portfolio project that demonstrates production-style backend API design, JWT authentication, refresh/session token handling, OTP verification, relational data modelling, Docker setup, CI/CD deployment, production profile configuration, health checks, production-safe documentation, operations notes, and mobile frontend integration.

---

## Project Structure

```text
wandermate-fullstack/
├── backend/     # Spring Boot API, MariaDB, auth, tests, Docker setup, docs
└── frontend/    # Expo React Native mobile app
```

---

## Tech Stack

| Area | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring Security |
| Database | MariaDB, Spring Data JPA / Hibernate |
| Auth | JWT access token, refresh token, session token |
| OTP | Email OTP implemented; SMS OTP prepared with mocked service tests |
| API Docs | Swagger / SpringDoc OpenAPI for local development; Markdown docs for production strategy |
| Testing | JUnit 5, Mockito, Spring MockMvc, Maven Surefire |
| Frontend | Expo React Native, TypeScript, Expo Router |
| State / Storage | Zustand, Expo SecureStore |
| Frontend Config | Expo public environment variables |
| Containerization | Docker, Docker Compose |
| Deployment | Render |
| CI/CD | GitHub Actions + Render Deploy Hook |

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
✅ Email OTP flow is implemented and verified end-to-end
✅ Phone/SMS OTP service flow exists and is covered by mocked unit tests
✅ Backend service-level tests and controller/API tests are implemented and passing
✅ Controller/API edge-case tests are implemented and passing
✅ Docker Compose local backend + MariaDB setup is available
✅ GitHub Actions CI/CD runs backend tests before triggering Render deployment
✅ Frontend CI runs TypeScript checks
✅ Backend is deployed on Render
✅ Production Spring profile is enabled on Render
✅ Public health check endpoint is available
✅ Swagger/OpenAPI is disabled in production
✅ Production API documentation strategy is documented
✅ Operations, health check, and logging notes are documented
✅ Frontend environment switching is configured with Expo public env variables
✅ Frontend has been tested against the deployed Render backend
⚠️ Real SMS provider integration is not enabled yet
⚠️ Public Docker demo values do not include real email/OAuth secrets
```

Phone/SMS OTP should be described as prepared backend logic only. The current `SmsServiceImpl` is a stub/mocked service flow and does not prove real SMS delivery. Real SMS would require a provider such as Twilio, AWS SNS, Vonage, or another SMS gateway.

---

## Live Backend

Production backend on Render:

```text
https://wandermate-fullstack.onrender.com/The-Project
```

Health check endpoint:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

Expected health response:

```json
{
  "status": "UP",
  "service": "WanderMate backend"
}
```

Swagger UI is available for local development, but it is disabled in the production profile for safer deployment.

Local Swagger UI:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

Render free-tier services may sleep when inactive, so the first request can take around 40–60 seconds to wake up.

---

## CI/CD

Backend CI/CD is configured with GitHub Actions.

On pull requests and pushes to `main`, the backend workflow runs the backend Maven test suite. On successful pushes to `main`, GitHub Actions triggers a Render deployment through a protected Render deploy hook secret.

Backend CI/CD flow:

```text
Push to main
→ GitHub Actions runs backend tests
→ Tests pass
→ Render deploy hook is triggered
→ Backend is deployed
```

Backend workflow file:

```text
.github/workflows/backend-ci-cd.yml
```

The Render deploy hook is stored securely in GitHub Actions secrets as:

```text
RENDER_DEPLOY_HOOK_URL
```

Frontend CI is also configured.

On pull requests and pushes that affect the frontend, the frontend workflow installs dependencies and runs TypeScript checks.

Frontend workflow file:

```text
.github/workflows/frontend-ci.yml
```

The workflows opt into GitHub Actions Node 24 execution for JavaScript-based actions:

```yaml
env:
  FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: true
```

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

Health check:

```text
http://localhost:8082/The-Project/api/v1/health
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

From the host machine, database tools connect using:

```text
localhost:3307
```

---

## Backend Local Development

From the backend folder:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd backend
.\mvnw spring-boot:run
```

The backend uses the context path:

```text
/The-Project
```

Default local backend URL when running from IntelliJ or Maven:

```text
http://localhost:8080/The-Project
```

Health check:

```text
http://localhost:8080/The-Project/api/v1/health
```

Required environment variables for local backend runs include:

```env
DB_URL=jdbc:mariadb://localhost:3307/traveling_app
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
```

Email OTP also requires valid email/OAuth configuration when testing real email delivery.

---

## Production Profile

The backend supports a production Spring profile.

Render uses:

```text
SPRING_PROFILES_ACTIVE=prod
```

The production profile is used to reduce development-only behaviour in deployment, including:

```text
- Disable SQL debug output
- Reduce noisy debug logging
- Disable Swagger/OpenAPI UI in production
```

Production health should be checked through:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

Production API documentation is handled through markdown docs instead of public Swagger:

```text
backend/docs/PRODUCTION_API_DOCS.md
backend/docs/API_GUIDE.md
backend/docs/AUTH_FLOW.md
backend/docs/POSTMAN_GUIDE.md
```

---

## Frontend Quick Start

From the frontend folder:

```bash
cd frontend
npm install
npx expo start
```

Frontend TypeScript check:

```bash
npm run typecheck
```

The frontend API URL is configured with Expo public environment variables.

Create a local frontend env file from the template:

```powershell
copy frontend\.env.example frontend\.env
```

Production Render backend:

```env
EXPO_PUBLIC_APP_ENV=production-render
EXPO_PUBLIC_API_BASE_URL=https://wandermate-fullstack.onrender.com/The-Project
```

Android emulator connecting to a local IntelliJ backend on port `8080`:

```env
EXPO_PUBLIC_APP_ENV=local-intellij
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080/The-Project
```

Android emulator connecting to the Docker backend on port `8082`:

```env
EXPO_PUBLIC_APP_ENV=local-docker
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/The-Project
```

After changing frontend `.env`, restart Expo with cache clear:

```powershell
cd frontend
npx expo start --clear
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
10. Open GitHub Actions and show backend tests/deploy workflow
11. Open Frontend CI and show TypeScript check
12. Open Render health endpoint
13. Explain that Swagger is disabled in production but available locally
```

The frontend and backend have been tested successfully with the frontend pointing to the deployed Render backend.

---

## Key Backend Features

### Authentication and Session Management

```text
- User registration
- Login
- Password hashing
- JWT access token generation
- Refresh token generation and rotation
- Session token storage
- Logout/session revocation
- Refresh token reuse detection
- Max active session handling
```

### OTP

```text
- Email OTP for registration/verification
- OTP retry limit
- OTP block/restriction time
- OTP expiry validation
- SMS/phone OTP service path prepared but not connected to a real SMS provider
```

### Trip Planning Domain

```text
User
└── Trip
    ├── Destination
    └── Activity
```

Implemented domain behaviour:

```text
- Users can only access their own trips
- Trips have start/end date validation
- Destinations must fit inside trip date range
- Activities must fit inside destination/trip date range
- Trip and destination overlap warnings can be bypassed with allowOverlap=true
- Activity overlap is blocked as a hard validation error
```

---

## Test Status

The current backend test suite includes both service-layer business logic tests and controller/API mapping tests.

Service tests:

```text
ActivityServiceImplTest      25 passed
DestinationServiceImplTest   31 passed
EmailServiceImplTest          9 passed
OtpServiceImplTest           28 passed
SmsServiceImplTest            3 passed
TokenServiceImplTest         33 passed
TripServiceImplTest          39 passed
UserServiceImplTest          29 passed
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
```

Total backend tests:

```text
208 passed, 0 failures, 0 errors, 0 skipped
```

Run backend tests from the backend folder:

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw test
```

Run frontend TypeScript checks from the frontend folder:

```bash
npm run typecheck
```

Note: if the default generated `TheProjectApplicationTests.contextLoads` test exists, it needs real DB environment variables or a dedicated test profile because it starts the full Spring ApplicationContext. The main portfolio test coverage is currently service-level unit testing plus focused controller tests.

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
| `backend/docs/FRONTEND_INTEGRATION.md` | Frontend/backend URL, token, env switching, and error-handling notes |
| `backend/docs/PRODUCTION_API_DOCS.md` | Explains why Swagger is local-only and how production API docs are handled |
| `backend/docs/OPERATIONS.md` | Health check, production profile, logging, and deployment troubleshooting notes |
| `backend/docs/ROADMAP.md` | Suggested V1/V2/V3/V4 improvement plan |
| `frontend/README.md` | Frontend setup and integration notes |

---

## Environment and Secret Handling

The project uses `.env.example` files as templates. Real `.env` files are ignored and should never be committed.

Important ignored files/folders include:

```text
backend/.env
frontend/.env
backend/target/
frontend/node_modules/
frontend/.expo/
.idea/
```

For local Docker setup, copy:

```text
backend/.env.example → backend/.env
```

For frontend local environment switching, copy:

```text
frontend/.env.example → frontend/.env
```

For production deployment, environment variables should be configured directly in Render.

---

## Deployment Notes

The backend is deployed on Render.

The deployment expects required environment variables to be configured in Render, including database and email/OAuth configuration.

Required Render environment variables include:

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL
DB_USERNAME
DB_PASSWORD
EMAIL_OAUTH_REFRESH_ENABLED
EMAIL_CLIENT_ID
EMAIL_CLIENT_SECRET
EMAIL_REFRESH_TOKEN
EMAIL_TOKEN_URL
EMAIL_ADDRESS_CONFIG
```

The frontend currently points to the deployed backend through:

```env
EXPO_PUBLIC_API_BASE_URL=https://wandermate-fullstack.onrender.com/The-Project
```

This allows the mobile app to test against the deployed backend instead of a local backend.

---

## Roadmap

### V1 — Portfolio MVP Polish

```text
✅ Backend CRUD
✅ Auth/session/refresh token flow
✅ Email OTP
✅ Frontend integration
✅ Docker setup
✅ Backend tests
✅ Controller/API tests
✅ Health endpoint
✅ Production Spring profile
✅ Render deployment
✅ CI/CD backend test and deploy workflow
```

### V2 — Backend and Portfolio Professionalism

```text
✅ Frontend CI checks
✅ Frontend environment switching
✅ Production-safe API documentation strategy
✅ Monitoring/logging notes
✅ Controller/API edge-case tests
⬜ Screenshots and short demo GIF/video
```

### V3 — Collaboration Features

```text
- Trip collaborators
- Owner/editor/viewer roles
- Invite links
- Shared trips screen
- Permission checks
```

### V4 — Advanced Product Features

```text
- Cost estimation
- Cost sharing
- Budget tracking
- AI itinerary suggestions
- Map/place API integration
- Real SMS provider integration if phone OTP becomes required
```

---

## Suggested Portfolio Demo Script

```text
1. Open the mobile app
2. Register using email OTP
3. Login
4. Create a trip
5. Add a destination
6. Add activities
7. Demonstrate overlap validation
8. Logout
9. Show GitHub Actions backend test/deploy workflow
10. Show GitHub Actions frontend TypeScript check
11. Show Render health endpoint
12. Explain that Swagger is disabled in production but available locally
```

This demonstrates both product functionality and backend engineering practices.

---

## Project Summary

WanderMate is a production-style full-stack travel planning app built to demonstrate backend engineering, authentication, database modelling, validation, testing, Docker deployment, CI/CD, production configuration, production-safe documentation, operations notes, and frontend integration.

The backend is the strongest part of the project, with service-layer tests, focused controller and API edge-case tests, token/session handling, OTP logic, ownership validation, Docker deployment, Render deployment, production profile, health endpoint, and GitHub Actions CI/CD. The frontend is functional, type-checked in CI, environment-configurable, and connected to the deployed backend, making the project suitable for a junior backend or full-stack portfolio.
