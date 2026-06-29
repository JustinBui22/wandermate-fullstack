# WanderMate Full Stack

[![Backend CI/CD](https://github.com/JustinBui22/wandermate-fullstack/actions/workflows/backend-ci-cd.yml/badge.svg?branch=main)](https://github.com/JustinBui22/wandermate-fullstack/actions/workflows/backend-ci-cd.yml)
[![Frontend CI](https://github.com/JustinBui22/wandermate-fullstack/actions/workflows/frontend-ci.yml/badge.svg?branch=main)](https://github.com/JustinBui22/wandermate-fullstack/actions/workflows/frontend-ci.yml)

WanderMate is a full-stack mobile travel planning application built with a Spring Boot backend and an Expo React Native frontend. Users can register, verify with OTP, log in securely, manage trips, organise destinations, schedule activities, and collaborate on shared trip plans through role-based access, invitations, join requests, and private overlap warnings.

This repository is a portfolio project focused on production-style backend API design, secure auth/session handling, relational data modelling, collaboration permissions, Docker setup, CI/CD, cloud deployment, backend testing, and mobile frontend integration.

---

## Project Status

```text
Current phase: V3 collaboration complete
Next phase: V4 portfolio proof, screenshots, demo video, README media, and CV/GitHub write-up
```

### Completed

```text
✅ Backend CRUD for trips, destinations, and activities
✅ Role-based trip collaboration: OWNER, EDITOR, VIEWER
✅ Trip member access control through TripAccessService
✅ Invitation-based collaboration flow
✅ Owner can invite users to a trip
✅ Invited user can accept/reject invitation
✅ User can request to join a trip
✅ Owner can accept/reject join requests
✅ Members are only added after invitation/join request acceptance
✅ Private overlap warning visible only to the affected member
✅ Activity createdBy and modifiedBy tracking
✅ JWT access token + refresh token + session token auth flow
✅ Refresh token rotation, revocation, logout, and reuse detection
✅ Max active session handling with frontend confirmation flow
✅ Email OTP registration and forgot-password flow
✅ OTP expiry, retry/block handling, destination matching, and consume-on-success
✅ Forgot-password validates new password before OTP consumption
✅ Trip and destination overlap warning flow with allowOverlap confirmation
✅ Activity overlap blocked as a hard validation error
✅ Standardized backend response/error-code system
✅ Docker Compose local backend + MariaDB setup
✅ Render deployment with production Spring profile
✅ Public health check endpoint
✅ Swagger/OpenAPI available locally and disabled in production
✅ GitHub Actions backend CI/CD with Render deploy hook
✅ GitHub Actions frontend TypeScript CI
✅ Expo React Native frontend integrated with deployed backend
✅ Shared frontend UI component system
✅ Auth, home, trip, destination, and activity screens polished
✅ Reusable frontend date/time picker components extracted
✅ Frontend API error messages polished
✅ Frontend debug/console logs removed from app/src code
✅ Backend test suite passing with 373 tests
✅ Frontend TypeScript check passing
✅ Frontend collaboration tab completed
✅ Received invitations, owned-trip join requests, and sent join requests UI completed
✅ Per-trip collaboration screens completed
✅ Share-code preview and join-request flow completed
✅ Profile page, dynamic theme, and attribution UI completed
```

### Not enabled yet

```text
⚠️ Real SMS provider integration is not enabled
⚠️ Final screenshots/demo video are planned for V4
⚠️ Viewer suggestion workflow is not implemented yet
⚠️ Cost sharing is not implemented yet
⚠️ Final screenshots/demo video are planned after V3 frontend integration
```

Phone/SMS OTP should be treated as prepared logic only. The current backend SMS service is a stub/mocked service path and does not prove real SMS delivery. A real provider such as Twilio, AWS SNS, Vonage, or another SMS gateway would be required.

---

## Repository Structure

```text
wandermate-fullstack/
├── backend/                 # Spring Boot API, auth, collaboration, MariaDB, tests, Docker, docs
├── frontend/                # Expo React Native app, routing, API integration, UI components
├── .github/workflows/       # Backend CI/CD and frontend CI workflows
└── README.md                # Full-stack project overview
```

---

## Tech Stack

| Area               | Technology                                           |
| ------------------ | ---------------------------------------------------- |
| Backend language   | Java 21                                              |
| Backend framework  | Spring Boot 3.5.4                                    |
| Security           | Spring Security, JWT, refresh tokens, session tokens |
| Database           | MariaDB, Spring Data JPA / Hibernate                 |
| OTP / Email        | Spring Mail, OAuth/email configuration               |
| API docs           | SpringDoc OpenAPI / Swagger for local development    |
| Backend tests      | JUnit 5, Mockito, Spring MockMvc, Maven Surefire     |
| Frontend framework | Expo React Native 56                                 |
| Frontend language  | TypeScript                                           |
| Routing            | Expo Router                                          |
| State / storage    | Zustand, Expo SecureStore                            |
| HTTP client        | Axios with auth/refresh interceptors                 |
| Frontend UI        | Shared custom UI components + theme constants        |
| Containerization   | Docker, Docker Compose                               |
| Deployment         | Render                                               |
| CI/CD              | GitHub Actions + Render deploy hook                  |

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

Expected response:

```json
{
  "status": "UP",
  "service": "WanderMate backend"
}
```

Render free-tier services may sleep when inactive, so the first request can take around 40-60 seconds to wake up.

Swagger UI is available locally only:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

Swagger/OpenAPI is disabled in the production profile for safer deployment.

---

## Main Features

### Authentication and Sessions

```text
- Register
- Verify registration details before OTP
- Send email OTP
- Complete registration with OTP
- Login
- Access token authentication
- Refresh token rotation
- Session token tracking
- Max active session confirmation
- Logout/session revocation
- Forgot password with OTP
```

### Trip Planning Domain

```text
User
└── Trip
    ├── Trip Members
    │   ├── OWNER
    │   ├── EDITOR
    │   └── VIEWER
    ├── Destination
    │   └── Activity
    └── Collaboration Requests
        ├── Invitations
        └── Join Requests
```

Implemented behaviour:

```text
- Trip creator becomes OWNER automatically
- OWNER can view, edit, delete, and manage members
- EDITOR can view and modify trip plan content
- VIEWER can view shared trip content only
- OWNER can invite another user to a trip
- Invited user must accept before becoming a member
- User can request to join a trip
- OWNER must accept before requester becomes a member
- OWNER does not see other users' private trip overlap details
- Affected member can see their own overlap warning
- Trips require valid start/end dates
- Destinations must fit inside the parent trip date range
- Activities must fit inside the parent destination/trip range
- Trip overlap can trigger a warning and continue with allowOverlap=true
- Destination overlap can trigger a warning and continue with allowOverlap=true
- Activity overlap is blocked as a hard validation error
```

### Collaboration API Areas

```text
GET    /api/v1/trips/{tripId}/members
PATCH  /api/v1/trips/{tripId}/members/{tripMemberId}/role
DELETE /api/v1/trips/{tripId}/members/{tripMemberId}

POST   /api/v1/trips/{tripId}/invitations
GET    /api/v1/trips/invitations/received
PATCH  /api/v1/trips/invitations/{requestId}/accept
PATCH  /api/v1/trips/invitations/{requestId}/reject

POST   /api/v1/trips/{tripId}/join-requests
GET    /api/v1/trips/{tripId}/join-requests
PATCH  /api/v1/trips/join-requests/{requestId}/accept
PATCH  /api/v1/trips/join-requests/{requestId}/reject

GET    /api/v1/trips/{tripId}/my-overlap-warnings
```

Direct member creation through `POST /members` is not exposed at controller level. Collaboration should go through invitations or join requests.

### Frontend V3 Polish

```text
- Shared theme constants
- Shared AppScreen, AppButton, AppInput, AppCard components
- Shared LoadingState, EmptyState, ErrorMessage components
- Shared DateTimeSection and DateTimePickerCard form components
- Polished auth screens
- Polished home and tab navigation
- Polished trip/destination/activity CRUD screens
- Centralized frontend API warning/error helpers
- Removed leftover debug logs from app/src code
```

---

## Backend Quick Start with Docker

From the backend folder:

```bash
cd backend
cp .env.example .env
docker compose up --build
```

Windows PowerShell:

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

---

## Backend Local Development

From the backend folder:

```bash
cd backend
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
cd backend
.\mvnw spring-boot:run
```

Default local URL:

```text
http://localhost:8080/The-Project
```

Required local env values include:

```env
DB_URL=jdbc:mariadb://localhost:3307/traveling_app
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
```

Email OTP also requires valid email/OAuth configuration when testing real email delivery.

---

## Frontend Quick Start

From the frontend folder:

```bash
cd frontend
npm install
npx expo start
```

Android:

```bash
npm run android
```

TypeScript check:

```bash
npm run typecheck
```

After changing `.env`, restart Expo with cache clear:

```bash
npx expo start --clear
```

---

## Frontend Environment Configuration

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

For host-machine browser/Postman testing against Docker:

```text
http://localhost:8082/The-Project
```

---

## CI/CD

Backend CI/CD workflow:

```text
.github/workflows/backend-ci-cd.yml
```

Flow:

```text
Push / PR to main
→ GitHub Actions runs backend Maven tests
→ Push to main triggers Render deploy hook after tests pass
→ Render deploys backend
```

Render deploy hook secret:

```text
RENDER_DEPLOY_HOOK_URL
```

Frontend CI workflow:

```text
.github/workflows/frontend-ci.yml
```

Flow:

```text
Push / PR to main affecting frontend
→ npm ci
→ npm run typecheck
```

The workflows opt into GitHub Actions Node 24 execution for JavaScript-based actions:

```yaml
env:
  FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: true
```

---

## Testing Status

### Backend

Focused backend test suite status:

```text
373 passing tests
0 failures
0 errors
0 skipped
```

Coverage includes:

```text
- Auth login/logout/session behaviour
- Refresh token rotation, revocation, and reuse detection
- User registration validation
- OTP send/verify retry, expiry, destination mismatch, and consume-on-success
- Forgot-password password validation before OTP consumption
- Trip/destination/activity CRUD business rules
- Role-based collaboration access checks
- Trip member management
- Invitation and join request collaboration flow
- Private current-user overlap warning
- Activity createdBy/modifiedBy mapping
- Trip/destination overlap warning logic
- Activity overlap blocking
- Controller/API status mapping and edge cases
```

Run backend tests:

```bash
cd backend
./mvnw test
```

Windows PowerShell:

```powershell
cd backend
.\mvnw test
```

Note: a generated full Spring `contextLoads` test, if reintroduced, may require a dedicated test profile or DB environment variables because it starts the full application context. The main portfolio proof is the focused service/controller test suite.

### Frontend

Frontend TypeScript check:

```bash
cd frontend
npm run typecheck
```

Current V3 frontend collaboration and polish should be verified with npm run typecheck.

Manual regression checklist:

```text
- Register + OTP
- Login
- Max active session confirmation
- Forgot password
- Logout
- Create/edit/delete trip
- Create/edit/delete destination
- Create/edit/delete activity
- Trip/destination overlap confirmation
- Activity overlap error display
- Owner sends trip invitation
- Invited user accepts/rejects invitation
- User requests to join trip
- Owner accepts/rejects join request
- Member sees private overlap warning only for their own trips
- Viewer cannot modify trip content
- Editor can modify trip plan content
- App reopen while logged in
- Logout clears session and returns to login
```

---

## Documentation Index

| Document                               | Purpose                                                                   |
| -------------------------------------- | ------------------------------------------------------------------------- |
| `backend/README.md`                    | Backend-specific setup, architecture, tests, deployment, and API overview |
| `backend/docs/API_GUIDE.md`            | Endpoint list, headers, request examples, response shape                  |
| `backend/docs/AUTH_FLOW.md`            | Login, token refresh, logout, max session, and OTP flows                  |
| `backend/docs/ARCHITECTURE.md`         | Layers, domain model, ownership/collaboration checks, validation rules    |
| `backend/docs/DOCKER_SETUP.md`         | Docker Compose setup, ports, env variables, troubleshooting               |
| `backend/docs/DATABASE_SEED.md`        | Safe local seed data rules and seed file purpose                          |
| `backend/docs/POSTMAN_GUIDE.md`        | Manual API testing order and sample payloads                              |
| `backend/docs/TESTING.md`              | Test coverage and test commands                                           |
| `backend/docs/FRONTEND_INTEGRATION.md` | Frontend/backend URL, token, env switching, and error-handling notes      |
| `backend/docs/PRODUCTION_API_DOCS.md`  | Production-safe API documentation strategy                                |
| `backend/docs/OPERATIONS.md`           | Health check, production profile, logging, deployment troubleshooting     |
| `backend/docs/ROADMAP.md`              | Suggested V1/V2/V2.5/V3/V4 roadmap                                        |
| `frontend/README.md`                   | Frontend setup, app structure, auth integration, and V2.5 UI notes        |

---

## Environment and Secret Handling

The project uses `.env.example` files as templates. Real `.env` files should not be committed.

Important ignored files/folders:

```text
backend/.env
frontend/.env
backend/target/
frontend/node_modules/
frontend/.expo/
.idea/
```

Do not commit or share real `.env`, local DB dumps, OAuth refresh tokens, access tokens, or generated build artifacts.

---

## Roadmap

### V1 - MVP

```text
✅ Auth basics
✅ Trip/destination/activity CRUD
✅ Frontend/backend integration
```

### V2 - Backend professionalization

```text
✅ Refresh/session token handling
✅ OTP hardening
✅ Docker setup
✅ Render deployment
✅ CI/CD
✅ Production profile
✅ Health check
✅ Backend tests and docs
```

### V2.5 - Frontend polish

```text
✅ Shared UI foundation
✅ Polished mobile screens
✅ Reusable date/time picker components
✅ Error message cleanup
✅ Removed leftover debug logs
✅ TypeScript check passing
```

### V3 - Collaboration

```text
✅ Backend role-based collaboration
✅ Backend trip member access control
✅ Backend invitation and join request flow
✅ Backend share-code join flow
✅ Backend private member overlap warning
✅ Backend collaboration tests
✅ Frontend collaboration API/types
✅ Frontend member/invitation/join-request screens
✅ Role-based UI controls
✅ Private overlap warning UI
✅ Profile/theme/attribution UI
```

### V4 - Portfolio proof

```text
- Take screenshots
- Record 60-90 second demo video
- Add screenshots/demo link to README
- Add architecture diagram images if desired
- Add final project summary to CV/GitHub portfolio
```

### Future product features

```text
- Viewer activity/destination suggestion workflow
- Cost estimation and cost sharing
- AI itinerary suggestions
- Maps/geolocation integration
- Real SMS provider integration
- Push notifications
```

---

## Suggested Demo Flow

```text
1. Open deployed backend health endpoint
2. Open mobile app
3. Register with email OTP
4. Login
5. Create a trip
6. Add a destination
7. Add an activity
8. Invite another user to the trip
9. Accept invitation as the invited user
10. Show role-based access behaviour
11. Show private overlap warning
12. Show GitHub Actions backend/frontend CI
```

## V3 Completed Collaboration Summary

```text
- Trip roles: OWNER, EDITOR, VIEWER
- Owner invitation flow
- Received invitation list
- Join request flow
- Owned-trip join request list
- Sent join request list
- Share-code preview and join request
- Collaboration summary badge counts
- Member list and role management
- Private overlap warnings for affected members only
- Profile display name, preferred theme, and profile image URL
- Light/Dark/System theme support
- Destination/activity creator and last-editor attribution
```

Direct member creation is intentionally not exposed through the controller. New members are added through accepted invitations or accepted join requests.
