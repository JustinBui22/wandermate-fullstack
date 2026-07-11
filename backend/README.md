# WanderMate Backend

Spring Boot backend for the WanderMate mobile travel-planning app.

The backend handles authentication, OTP verification, JWT access/refresh/session tokens, trip planning, destination and activity scheduling, collaboration permissions, share-code joining, Cloudinary image upload, local Docker database setup, automated tests, and Render deployment.

---

## Current Backend Status

```text
Current phase: V4 portfolio proof
Backend state: feature-complete and test-covered enough for portfolio use
Latest included test report: 399 tests, 0 failures, 0 errors, 0 skipped
```

Implemented:

```text
✅ Layered Spring Boot structure
✅ Controller/service/validator/mapper/repository separation
✅ MariaDB + JPA/Hibernate persistence
✅ JWT access token flow
✅ Refresh token rotation and reuse detection
✅ Session token validation
✅ Logout/session revocation
✅ Max active session handling
✅ Register with OTP verification
✅ Forgot-password OTP flow
✅ Trip CRUD
✅ Destination CRUD
✅ Activity CRUD
✅ Trip status recalculation from trip dates
✅ Trip/destination overlap warning flow
✅ Activity overlap blocking
✅ Role-based trip access: OWNER, EDITOR, VIEWER
✅ Direct trip invitations
✅ Join request flow
✅ Share-code join flow
✅ Collaboration summary/badge data
✅ Creator/editor attribution data
✅ Profile settings and theme preference storage
✅ Profile image and trip cover image fields
✅ Cloudinary image upload and old-image cleanup
✅ Swagger/OpenAPI local development docs
✅ Production profile disables Swagger/OpenAPI
✅ Docker Compose local backend + MariaDB
✅ Sanitized Docker database init file
✅ Render deployment workflow
```

Not implemented yet:

```text
⚠️ Cost sharing/budget split
⚠️ Viewer suggestion workflow
⚠️ Real SMS provider integration
⚠️ Flyway/Liquibase migrations
⚠️ Testcontainers integration tests
```

---

## Backend Tech Stack

| Area | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.x |
| Build | Maven Wrapper |
| API | Spring Web REST controllers |
| Security | Spring Security, JWT |
| Database | MariaDB |
| ORM | Spring Data JPA / Hibernate |
| Validation | Spring Validation + custom validators |
| Media | Cloudinary |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Tests | JUnit 5, Mockito, MockMvc, AssertJ |
| Deployment | Docker, Render |
| CI/CD | GitHub Actions |

---

## Backend Structure

```text
backend/
├── src/main/java/com/example/travellingapp/
│   ├── config/                 # Security, mail, Cloudinary, OpenAPI config
│   ├── controller/             # API interfaces
│   ├── controller/impl/        # REST controller implementations
│   ├── dto/request/            # Request DTOs
│   ├── dto/response/           # Response DTOs
│   ├── entity/                 # JPA entities
│   ├── entity/collaboration/   # Collaboration entities
│   ├── enums/                  # App enums
│   ├── exception_handler/      # Global exception handling
│   ├── mapper/                 # Entity/DTO mapping
│   ├── repository/             # Spring Data repositories
│   ├── security/               # Token filter, authenticated user, hashing, OAuth helper
│   ├── service/                # Service interfaces
│   ├── service/impl/           # Business logic
│   ├── util/                   # Shared utilities
│   └── validator/              # Input/business validators
├── src/main/resources/
│   ├── application.properties
│   └── application-prod.properties
├── src/test/java/              # Controller/service/validator tests
├── docker/init/init.sql        # Sanitized schema + reference data
├── docker-compose.yml
├── Dockerfile
├── .env.example
└── README.md
```

---

## Architecture Overview

The backend uses a layered architecture:

```text
Controller
→ Service
→ Validator / Access Service
→ Repository
→ MariaDB
```

Main design responsibilities:

```text
Controller: HTTP request/response handling
Service: business logic and orchestration
Validator: request and domain validation
Access service: role/permission checks
Mapper: entity-to-DTO conversion
Repository: database access
Entity: persistent domain model
DTO: API boundary model
```

Important access-control service:

```text
TripAccessService
├── getTripIfCanView
├── getTripIfCanEdit
├── getTripIfOwner
├── assertCanView
├── assertCanEdit
└── assertIsOwner
```

This keeps authorization logic reusable across trips, destinations, activities, members, requests, and share codes.

---

## Domain Model

```text
User
├── RefreshToken
├── SessionToken
├── OtpCheck
└── Trip
    ├── TripMember
    ├── TripCollaborationRequest
    ├── TripShareCode
    └── Destination
        └── Activity
```

Important tables:

```text
users
trips
trip_destinations
destination_activities
trip_members
trip_collaboration_requests
trip_share_codes
trip_share_code_attempts
refresh_token
session_token
otp_check
configuration
error_codes
email_contents
sms_contents
```

Image fields:

```text
users.profile_image_url
users.profile_image_public_id
trips.cover_image_url
trips.cover_image_public_id
```

---

## Permission Model

| Capability | OWNER | EDITOR | VIEWER |
|---|---:|---:|---:|
| View trip | ✅ | ✅ | ✅ |
| View destinations/activities | ✅ | ✅ | ✅ |
| Edit trip details | ✅ | ✅ | ❌ |
| Add/edit/delete destinations | ✅ | ✅ | ❌ |
| Add/edit/delete activities | ✅ | ✅ | ❌ |
| Delete trip | ✅ | ❌ | ❌ |
| Invite members | ✅ | ❌ | ❌ |
| Review join requests | ✅ | ❌ | ❌ |
| Create/revoke share code | ✅ | ❌ | ❌ |
| Change roles/remove members | ✅ | ❌ | ❌ |

Trip creator becomes OWNER automatically.

---

## API Base URLs

Local Maven:

```text
http://localhost:8080/The-Project
```

Local Docker:

```text
http://localhost:8082/The-Project
```

Render production:

```text
https://wandermate-fullstack.onrender.com/The-Project
```

Health check:

```text
GET /api/v1/health
```

Swagger UI local only:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

Swagger/OpenAPI is disabled in production through `application-prod.properties`.

---

## Main API Areas

```text
Auth/User:
POST /api/v1/users/register
POST /api/v1/users/register/verify
POST /api/v1/users/login
POST /api/v1/users/logout
POST /api/v1/users/forgot-password
GET  /api/v1/users/me
PUT  /api/v1/users/me
PUT  /api/v1/users/me/settings

Token:
POST /api/v1/auth/refresh

OTP:
POST /api/v1/otp/send
POST /api/v1/otp/verify

Trips:
POST   /api/v1/trips
GET    /api/v1/trips
GET    /api/v1/trips/{tripId}
PUT    /api/v1/trips/{tripId}
DELETE /api/v1/trips/{tripId}

Destinations:
POST   /api/v1/trips/{tripId}/destinations
GET    /api/v1/trips/{tripId}/destinations
GET    /api/v1/trips/{tripId}/destinations/{destinationId}
PUT    /api/v1/trips/{tripId}/destinations/{destinationId}
DELETE /api/v1/trips/{tripId}/destinations/{destinationId}

Activities:
POST   /api/v1/trips/{tripId}/destinations/{destinationId}/activities
GET    /api/v1/trips/{tripId}/destinations/{destinationId}/activities
GET    /api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}
PUT    /api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}
DELETE /api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}

Collaboration:
GET  /api/v1/trip-collaboration/summary
POST /api/v1/trips/{tripId}/collaboration/invitations
GET  /api/v1/trips/{tripId}/collaboration/requests
POST /api/v1/trips/{tripId}/collaboration/requests/{requestId}/accept
POST /api/v1/trips/{tripId}/collaboration/requests/{requestId}/reject
GET  /api/v1/trips/{tripId}/members
PUT  /api/v1/trips/{tripId}/members/{tripMemberId}
DELETE /api/v1/trips/{tripId}/members/{tripMemberId}

Share codes:
POST /api/v1/trips/{tripId}/share-codes
GET  /api/v1/trips/{tripId}/share-codes/active
POST /api/v1/trip-share-codes/preview
POST /api/v1/trip-share-codes/join

Uploads:
POST /api/v1/uploads/images
```

Check `backend/docs/API_GUIDE.md` and Swagger UI for exact request/response shapes.

---

## Authentication Flow

Login returns three tokens:

```text
accessToken
refreshToken
sessionToken
```

Protected API requests use:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

Refresh requests use:

```text
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

Security behavior:

```text
- Access tokens are short-lived
- Refresh tokens are stored as hashes
- Refresh tokens rotate when used
- Reused refresh tokens are detected
- Logout revokes the current session
- Max active sessions are enforced
- Frontend can request override to remove the oldest session
```

---

## Trip Date and Status Rules

Trip status is date-driven:

```text
end date before now = FINISHED
start date <= now <= end date = ONGOING
start date after now = PLANNING
```

If a previously finished trip is edited and its end date is moved into the future, the backend should recalculate it to `ONGOING` when the current date falls within the new date range.

Validation rules:

```text
- Trip start must be before trip end
- Destination start/end must stay inside trip range
- Activity start/end must stay inside destination range
- Trip/destination overlap can return warning and be confirmed with allowOverlap=true
- Activity overlap is blocked as a conflict
```

---

## Cloudinary Image Flow

Upload flow:

```text
Frontend image picker
→ multipart request to backend
→ ImageUploadService validates image type/size
→ CloudinaryImageClient uploads file
→ Backend returns imageUrl + publicId
→ Frontend sends imageUrl + publicId in profile/trip update
→ Backend saves both fields
```

Cleanup flow:

```text
- Replacing profile image deletes old profile image by publicId
- Removing profile image deletes old profile image by publicId
- Replacing trip cover deletes old trip cover by publicId
- Removing trip cover deletes old trip cover by publicId
- Deleting trip deletes old trip cover by publicId
```

Important implementation note:

```text
If an old image has URL but no publicId, the app can display it but cannot delete it from Cloudinary automatically.
```

---

## Local Setup: Docker

Create env file:

```bash
cd backend
cp .env.example .env
```

Windows PowerShell:

```powershell
cd backend
copy .env.example .env
```

Start backend + MariaDB:

```bash
docker compose up --build
```

Fresh start:

```bash
docker compose down -v
docker compose up --build
```

Health check:

```text
http://localhost:8082/The-Project/api/v1/health
```

Database port mapping:

```text
host 3307 → container 3306
```

Important: Docker init files under `backend/docker/init/` are executed by MariaDB on first database creation. Keep only the sanitized `init.sql`. Delete local/raw dumps such as `full-init.sql` before sharing or running a fresh Docker start.

---

## Local Setup: Maven / IntelliJ

Run from command line:

```bash
cd backend
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
cd backend
.\mvnw spring-boot:run
```

For IntelliJ, configure environment variables in the Run Configuration:

```env
DB_URL=jdbc:mariadb://localhost:3307/traveling_app
DB_USERNAME=traveling_user
DB_PASSWORD=traveling_password
SPRING_PROFILES_ACTIVE=dev
CLOUDINARY_CLOUD_NAME=replace_me
CLOUDINARY_API_KEY=replace_me
CLOUDINARY_API_SECRET=replace_me
CLOUDINARY_BASE_FOLDER=wandermate
```

---

## Environment Variables

Template file:

```text
backend/.env.example
```

Required/important values:

```env
DB_NAME=traveling_app
DB_HOST_PORT=3307
DB_USERNAME=traveling_user
DB_PASSWORD=traveling_password
DB_ROOT_PASSWORD=root_password
BACKEND_HOST_PORT=8082
DB_URL=jdbc:mariadb://db:3306/traveling_app
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_PROFILES_ACTIVE=dev

EMAIL_OAUTH_REFRESH_ENABLED=false
EMAIL_CLIENT_ID=replace_me
EMAIL_CLIENT_SECRET=replace_me
EMAIL_REFRESH_TOKEN=replace_me
EMAIL_TOKEN_URL=https://oauth2.googleapis.com/token
EMAIL_ADDRESS_CONFIG=demo@example.com

CLOUDINARY_CLOUD_NAME=replace_me
CLOUDINARY_API_KEY=replace_me
CLOUDINARY_API_SECRET=replace_me
CLOUDINARY_BASE_FOLDER=wandermate
```

Security rule:

```text
Commit .env.example only.
Never commit backend/.env, raw SQL dumps, OAuth tokens, refresh tokens, DB passwords, Cloudinary secrets, or real email credentials.
```

---

## Database Initialization

Current local Docker strategy:

```text
backend/docker/init/init.sql
```

This file should:

```text
✅ create the current local schema
✅ seed configuration/error/email/SMS reference data
✅ avoid real users/trips/demo data
✅ avoid OTP rows
✅ avoid refresh/session tokens
✅ avoid OAuth tokens or real credentials
```

This file should not include:

```text
❌ backend/docker/init/full-init.sql
❌ raw cloud database dumps
❌ users
❌ trips
❌ refresh_token rows
❌ session_token rows
❌ otp_check rows
❌ real Gmail OAuth tokens
❌ Cloudinary demo image URLs
```

---

## Testing

Run backend tests:

```bash
cd backend
./mvnw test
```

Latest included Surefire reports show:

```text
399 tests
0 failures
0 errors
0 skipped
```

Covered areas:

```text
- User registration/login/profile/settings
- OTP send/verify flows
- Token generation/refresh/logout/session revocation
- Trip CRUD and validation
- Trip status recalculation
- Destination CRUD and validation
- Activity CRUD and overlap validation
- Collaboration invitations
- Join requests
- Trip members and role management
- Share-code generation/preview/join/revoke behavior
- Collaboration summary
- Access-control service
- Image upload service/controller
- Cloudinary client cleanup behavior
- Controllers, services, validators
```

---

## Deployment

Render deployment:

```text
Root directory: backend
Build command: ./mvnw clean package -DskipTests
Start command: java -jar target/*.jar
Production profile: prod
```

Required Render environment variables:

```env
DB_URL=jdbc:mariadb://...
DB_USERNAME=...
DB_PASSWORD=...
SPRING_PROFILES_ACTIVE=prod
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...
CLOUDINARY_BASE_FOLDER=wandermate
EMAIL_OAUTH_REFRESH_ENABLED=false
EMAIL_CLIENT_ID=...
EMAIL_CLIENT_SECRET=...
EMAIL_REFRESH_TOKEN=...
EMAIL_TOKEN_URL=https://oauth2.googleapis.com/token
EMAIL_ADDRESS_CONFIG=...
```

Production security:

```text
- Swagger/OpenAPI disabled
- SQL logging disabled
- Security logs reduced
- Secrets stored as environment variables
```

---

## CI/CD

Backend workflow:

```text
.github/workflows/backend-ci-cd.yml
```

Workflow behavior:

```text
- Runs on pull request to main when backend changes
- Runs on push to main when backend changes
- Sets up Java 21
- Runs ./mvnw -B test
- Triggers Render deploy hook after successful push to main
```

---

## Backend Documentation Index

Recommended reading order:

```text
1. backend/README.md
2. backend/docs/ARCHITECTURE.md
3. backend/docs/API_GUIDE.md
4. backend/docs/AUTH_FLOW.md
5. backend/docs/CLOUDINARY_IMAGE_STORAGE.md
6. backend/docs/DOCKER_SETUP.md
7. backend/docs/DOCKER_FRESH_START_CHECKLIST.md
8. backend/docs/DATABASE_SEED.md
9. backend/docs/POSTMAN_GUIDE.md
10. backend/docs/TESTING.md
11. backend/docs/OPERATIONS.md
12. backend/docs/PRODUCTION_API_DOCS.md
13. backend/docs/ROADMAP.md
14. backend/docs/V4_SCREENSHOT_DEMO_GUIDE.md
```

---

## Known Technical Debt / Future Improvements

```text
- Split the broad TripEnum into smaller enums: TripRoleEnum, TripStatusEnum, RequestStatusEnum, ShareCodeStatusEnum, SortEnum
- Replace ddl-auto=update with Flyway/Liquibase migrations
- Add Testcontainers database integration tests
- Add end-to-end API test collection for critical flows
- Add real SMS provider if phone OTP is required
- Add budget/cost sharing in V5
- Add viewer suggestion workflow
```

---

## Portfolio Readiness Checklist

```text
✅ Backend features are strong enough
✅ Backend tests are strong enough
✅ Auth/session/collaboration logic is explainable
✅ Cloudinary image flow is portfolio-worthy
⚠️ Clean shareable package still matters
⚠️ Delete full-init.sql and private .env files before sharing
⚠️ Add screenshots and demo video before using this repo heavily for job applications
```
