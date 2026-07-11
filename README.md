# WanderMate Full Stack

WanderMate is a full-stack mobile travel-planning and collaboration app built with a Spring Boot backend and an Expo React Native frontend.

The app supports secure authentication, OTP registration, trip planning, destination and activity scheduling, role-based trip collaboration, share-code joining, profile settings, Cloudinary image uploads, dark/light/system theme support, automated backend tests, frontend type checking, Docker local setup, and Render backend deployment.

This repository is a portfolio project focused on backend API design, authentication/session security, relational data modelling, access-control rules, cloud media storage, automated testing, and production-style frontend/backend integration.

---

## Current Status

```text
Current phase: V4 portfolio proof
Code state: feature-complete enough for portfolio
Main remaining work: screenshots, demo video, README media, and clean public packaging
```

### Implemented

```text
✅ Spring Boot backend with controller/service/validator/mapper/repository layering
✅ Expo React Native frontend with Expo Router and TypeScript
✅ JWT access token + refresh token + session token authentication
✅ Refresh token rotation, session revocation, logout, and reuse detection
✅ Maximum active session handling
✅ Email OTP registration and forgot-password flow
✅ Trip CRUD with trip status recalculation
✅ Destination CRUD inside trips
✅ Activity CRUD inside destinations
✅ Trip and destination overlap warnings with allowOverlap confirmation
✅ Activity overlap blocking as a hard validation error
✅ Role-based trip collaboration: OWNER, EDITOR, VIEWER
✅ Direct invitation flow
✅ Join request flow
✅ Share-code join flow
✅ Owner-only join-request review
✅ Member role management
✅ Pending collaboration badges/lists
✅ Private overlap warnings for affected members
✅ Creator/editor attribution on destinations and activities
✅ Profile page with display name, phone, date of birth, theme, and avatar
✅ Cloudinary image upload for profile pictures and trip cover photos
✅ Cloudinary publicId storage for image cleanup
✅ Trip cover image support on create/edit, My Trips, and Trip Detail screens
✅ Light/Dark/System theme support
✅ Saved theme preference hydration after login/session restore
✅ Docker Compose local backend + MariaDB setup
✅ Sanitized local database init file
✅ Render backend deployment setup
✅ GitHub Actions backend CI/CD and frontend CI
✅ Swagger/OpenAPI for local development; disabled in production
```

### Not Implemented Yet

```text
⚠️ Cost sharing / budget split is planned for a future V5 feature
⚠️ Viewer suggestion workflow is planned later
⚠️ Real SMS provider integration is not enabled
⚠️ Push notifications are not enabled
⚠️ Demo video and screenshots still need to be added to the README
```

---

## Important Repo Hygiene Warning

Do not share a manually zipped project folder if it contains local/generated/private files.

Before publishing or sending the project, make sure these are not included:

```text
.git/
.idea/
backend/.env
frontend/.env
frontend/node_modules/
frontend/.expo/
backend/target/
backend/docker/init/full-init.sql
```

The real `.env` files and raw SQL dumps can contain database credentials, OAuth tokens, refresh tokens, Cloudinary secrets, personal emails, and demo user data. Keep only `.env.example` and the sanitized `backend/docker/init/init.sql` in the public repo.

Recommended clean export command:

```bash
git archive --format=zip --output=wandermate-clean.zip HEAD
```

---

## Repository Structure

```text
wandermate-fullstack/
├── backend/                 # Spring Boot API, auth, collaboration, Cloudinary upload, MariaDB, tests
├── frontend/                # Expo React Native app, auth, trips, collaboration, profile/theme/media UI
├── docs/                    # Portfolio screenshots/demo media folder
├── .github/workflows/       # Backend CI/CD and frontend CI workflows
├── .gitignore
└── README.md                # Full-stack project overview
```

Recommended portfolio media structure:

```text
docs/media/screenshots/
├── 01-login.png
├── 02-my-trips.png
├── 03-trip-detail-owner.png
├── 04-trip-cover-upload.png
├── 05-destinations-with-creator.png
├── 06-activity-attribution.png
├── 07-collaboration-menu-owner.png
├── 08-invite-member.png
├── 09-share-code.png
├── 10-join-requests.png
├── 11-members-role-management.png
├── 12-viewer-read-only.png
├── 13-profile-avatar-settings.png
├── 14-dark-mode.png
├── 15-render-health.png
└── 16-github-actions.png

docs/media/wandermate-demo.mp4
```

---

## Tech Stack

| Area | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5.x, Maven |
| Security | Spring Security, JWT, refresh tokens, session tokens |
| Database | MariaDB, Spring Data JPA / Hibernate |
| Media storage | Cloudinary |
| Backend tests | JUnit 5, Mockito, MockMvc, AssertJ |
| Frontend | Expo React Native 56, TypeScript, Expo Router |
| Frontend state/storage | Zustand, Expo SecureStore |
| Frontend HTTP | Axios with auth/refresh interceptors |
| Image picking | Expo Image Picker |
| Deployment | Render |
| Containerization | Docker, Docker Compose |
| CI/CD | GitHub Actions |

---

## Live Backend

Production backend on Render:

```text
https://wandermate-fullstack.onrender.com/The-Project
```

Health check:

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

Render free-tier services may sleep when inactive, so the first request can take time to wake up.

Swagger UI is local-only:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

Swagger/OpenAPI is disabled in production.

---

## Main Product Features

### Authentication and Profile

```text
- Register with email OTP
- Login/logout
- Refresh token rotation
- Session token validation
- Max active session confirmation
- Forgot password with OTP
- Profile settings
- Display name / phone / date of birth update
- Light/Dark/System theme preference
- Saved theme hydration after login/session restore
- Profile image upload through phone image picker
```

### Trip Planning

```text
User
└── Trip
    ├── Trip cover image
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

Rules:

```text
- Trip creator becomes OWNER automatically
- OWNER can manage trip, members, invitations, and join requests
- EDITOR can view and modify trip content
- VIEWER can view shared trip content only
- Destinations must fit inside the trip date range
- Activities must fit inside the destination date/time range
- Trip/destination overlaps can be confirmed with allowOverlap=true
- Activity overlaps are blocked
- Trip status is recalculated from dates when trip dates change
```

### Collaboration

```text
- Invite another user to a trip
- Accept/reject received invitations
- Request to join another trip
- Join a trip through share code / invite link
- Owner accepts/rejects join requests
- Owner manages member roles
- Owner removes members except the owner
- Show pending invitation/join-request sections
- Show private overlap warnings only to the affected member
- Show creator/editor attribution on destinations and activities
```

### Role Matrix

| Action | OWNER | EDITOR | VIEWER |
|---|---:|---:|---:|
| View trip | ✅ | ✅ | ✅ |
| View destinations/activities | ✅ | ✅ | ✅ |
| Edit trip details | ✅ | ✅ | ❌ |
| Add/edit/delete destinations | ✅ | ✅ | ❌ |
| Add/edit/delete activities | ✅ | ✅ | ❌ |
| Delete trip | ✅ | ❌ | ❌ |
| Invite members | ✅ | ❌ | ❌ |
| Manage join requests | ✅ | ❌ | ❌ |
| Change roles/remove members | ✅ | ❌ | ❌ |

### Media Uploads

WanderMate uses Cloudinary for production-safe image storage.

```text
Phone image picker
→ Frontend sends multipart file to Spring Boot
→ Backend uploads image to Cloudinary
→ Cloudinary returns secureUrl + publicId
→ DB stores only secure URL + publicId
→ Replacing/removing an image deletes the old Cloudinary asset by publicId
```

Stored DB fields:

```text
users.profile_image_url
users.profile_image_public_id
trips.cover_image_url
trips.cover_image_public_id
```

---

## Backend Quick Start

### Docker backend + MariaDB

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

Docker health check:

```text
http://localhost:8082/The-Project/api/v1/health
```

### Local Maven backend

```bash
cd backend
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
cd backend
.\mvnw spring-boot:run
```

Local Maven URL:

```text
http://localhost:8080/The-Project
```

---

## Backend Environment Variables

Use `.env.example` as a template. Real `.env` files should not be committed.

Important variables:

```env
DB_URL=jdbc:mariadb://...
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password
SPRING_PROFILES_ACTIVE=dev
SPRING_JPA_HIBERNATE_DDL_AUTO=update

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

For IntelliJ local runs, put real values in the Run Configuration environment variables. For Docker, put real local values in `backend/.env`. For Render, put production values in the Render service Environment section.

Do not hardcode real DB/email/Cloudinary secrets in `application.properties`, SQL files, or README files.

---

## Frontend Quick Start

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

After changing frontend `.env`, restart Expo:

```bash
npx expo start --clear
```

---

## Frontend Environment Variables

Production Render backend:

```env
EXPO_PUBLIC_APP_ENV=production-render
EXPO_PUBLIC_API_BASE_URL=https://wandermate-fullstack.onrender.com/The-Project
```

Android emulator to local IntelliJ backend:

```env
EXPO_PUBLIC_APP_ENV=local-intellij
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080/The-Project
```

Android emulator to Docker backend:

```env
EXPO_PUBLIC_APP_ENV=local-docker
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/The-Project
```

---

## Testing

Backend tests:

```bash
cd backend
./mvnw test
```

Latest included Surefire reports in the project show:

```text
399 tests
0 failures
0 errors
0 skipped
```

Frontend typecheck:

```bash
cd frontend
npm run typecheck
```

Latest frontend check:

```text
npm run typecheck passed
```

Important manual tests before recording the demo:

```text
1. Register + OTP
2. Login
3. Max active session confirmation
4. Create trip with cover image
5. Edit trip dates and confirm status recalculation
6. Edit/remove trip cover image
7. Create/edit/delete destination
8. Create/edit/delete activity
9. Activity overlap blocking
10. Invite member
11. Accept/reject invitation
12. Send join request
13. Accept/reject join request
14. Generate/share invite code
15. Join through invite code
16. Owner/editor/viewer UI permissions
17. Viewer read-only trip screen
18. Profile avatar upload/change/remove
19. Light/Dark/System theme switching
20. App reopen/session restore keeps saved theme
```

---

## Deployment

Backend deployment is configured for Render.

```text
Render service root: backend
Production context path: /The-Project
Production health endpoint: /api/v1/health
Swagger/OpenAPI: disabled in production
```

CI/CD workflows:

```text
.github/workflows/backend-ci-cd.yml
.github/workflows/frontend-ci.yml
```

Backend workflow:

```text
- Set up Java 21
- Run ./mvnw -B test
- Trigger Render deploy hook on push to main
```

Frontend workflow:

```text
- Set up Node.js 24
- Run npm ci
- Run npm run typecheck
```

---

## Documentation Index

| Document | Purpose |
|---|---|
| `README.md` | Full-stack overview, setup, features, testing, deployment, roadmap |
| `backend/README.md` | Backend-focused setup, architecture, auth, database, Cloudinary, tests |
| `frontend/README.md` | Frontend-focused setup, routes, auth/media/theme integration, manual tests |
| `backend/docs/API_GUIDE.md` | Endpoint examples and response shapes |
| `backend/docs/AUTH_FLOW.md` | Auth/session/OTP flow details |
| `backend/docs/ARCHITECTURE.md` | Backend layering and domain model |
| `backend/docs/CLOUDINARY_IMAGE_STORAGE.md` | Cloudinary upload and cleanup flow |
| `backend/docs/DATABASE_SEED.md` | Local Docker database seed/init notes |
| `backend/docs/DOCKER_SETUP.md` | Docker setup guide |
| `backend/docs/DOCKER_FRESH_START_CHECKLIST.md` | Fresh Docker verification checklist |
| `backend/docs/POSTMAN_GUIDE.md` | Manual backend API testing flow |
| `backend/docs/TESTING.md` | Backend test coverage notes |
| `backend/docs/V4_SCREENSHOT_DEMO_GUIDE.md` | Screenshot/demo capture checklist |

---

## Roadmap

### V4 Portfolio Proof

```text
1. Delete local/private files from shareable package
2. Take screenshots
3. Record demo video
4. Add media section to README
5. Polish GitHub repo description
6. Prepare CV bullets
7. Prepare interview explanation notes
```

### Future Product Features

```text
- Cost sharing and trip budget split
- Viewer suggestion workflow
- AI itinerary suggestions
- Maps/geolocation integration
- Push notifications
- Real SMS provider integration
- Flyway/Liquibase migrations
- Testcontainers integration tests
```

Cost sharing is a good future feature, but it is not needed before V4 portfolio proof.
