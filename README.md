# WanderMate Full Stack

WanderMate is a full-stack mobile travel planning app built with a Spring Boot backend and an Expo React Native frontend. It supports secure authentication, OTP registration, trip planning, destination/activity scheduling, role-based trip collaboration, profile settings, Cloudinary image uploads, and mobile-first UI polish.

This repository is a portfolio project focused on backend API design, authentication/session security, relational data modelling, collaboration permissions, cloud deployment, automated testing, and production-style frontend/backend integration.

---

## Current Status

```text
Current phase: V3 complete / V4 portfolio proof preparation
```

### Implemented

```text
✅ Spring Boot backend with layered controller/service/validator/mapper/repository design
✅ Expo React Native frontend with Expo Router and TypeScript
✅ JWT access token + refresh token + session token auth flow
✅ Refresh token rotation, session revocation, logout, and reuse detection
✅ Max active session handling
✅ Email OTP registration and forgot-password flow
✅ Trip, destination, and activity CRUD
✅ Trip/destination overlap warnings with allowOverlap confirmation
✅ Activity overlap blocking as a hard validation error
✅ Role-based trip collaboration: OWNER, EDITOR, VIEWER
✅ Invitation flow and join-request flow
✅ Share-code join flow
✅ Pending collaboration badges/lists
✅ Private overlap warnings for affected members
✅ Creator/editor attribution on destinations and activities
✅ Profile page with display name, theme, and profile image
✅ Cloudinary image upload for profile pictures and trip cover photos
✅ Cloudinary publicId storage for cleanup of replaced/removed images
✅ Trip cover image on create/edit, My Trips, and Trip Detail screens
✅ Light/Dark/System theme support
✅ Docker Compose local backend + MariaDB setup
✅ Render deployment setup
✅ GitHub Actions backend/frontend CI
✅ Swagger/OpenAPI for local development; disabled in production
```

### Not implemented yet

```text
⚠️ Cost sharing/budget split is planned for a future feature, not V3
⚠️ Viewer suggestion workflow is planned later
⚠️ Real SMS provider integration is not enabled
⚠️ Demo video/screenshots are next in V4 portfolio proof
```

---

## Repository Structure

```text
wandermate-fullstack/
├── backend/                 # Spring Boot API, auth, collaboration, Cloudinary upload, MariaDB, tests
├── frontend/                # Expo React Native app, auth, trips, collaboration, profile/theme/media UI
├── .github/workflows/       # Backend CI/CD and frontend CI workflows
└── README.md                # Full-stack project overview
```

---

## Tech Stack

| Area | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5.x, Maven |
| Security | Spring Security, JWT, refresh tokens, session tokens |
| Database | MariaDB, Spring Data JPA / Hibernate |
| Media storage | Cloudinary image upload/storage |
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

Render free-tier services may sleep when inactive, so the first request can take around 40–60 seconds to wake up.

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
- Display name
- Light/Dark/System theme preference
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
- Destinations must fit inside trip date range
- Activities must fit inside destination date/time range
- Trip/destination overlaps can be confirmed with allowOverlap=true
- Activity overlaps are blocked
```

### Collaboration

```text
- Invite another user to a trip
- Accept/reject received invitations
- Request to join another trip
- Owner accepts/rejects join requests
- Show pending invitation/join request sections
- Show private overlap warnings only to the affected member
- Show avatar-only creator/editor attribution on destinations and activities
```

### Media Uploads

WanderMate uses Cloudinary for production-safe image storage.

```text
Phone image picker
→ Frontend sends multipart file to Spring Boot
→ Backend uploads image to Cloudinary
→ Cloudinary returns secureUrl + publicId
→ DB stores only secure URL + publicId
→ Replacing/removing image deletes the old Cloudinary asset by publicId
```

Stored DB fields:

```text
users.profile_image_url
users.profile_image_public_id
trips.cover_image_url
trips.cover_image_public_id
```

Cloudinary folder style:

```text
wandermate/profile-images/users/{userId}/...
wandermate/trip-covers/users/{userId}/...
```

---

## Backend Quick Start

Docker backend + MariaDB:

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

Local Maven backend:

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
JWT_SECRET_KEY=your_jwt_secret
SPRING_PROFILES_ACTIVE=dev

EMAIL_OAUTH_REFRESH_ENABLED=true
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

For IntelliJ local runs, put real values in the Run Configuration environment variables. For Docker, put them in `backend/.env`. For Render, put them in the Render service Environment section.

Do not hardcode real DB/email/Cloudinary secrets in `application.properties`.

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

## Database Migration Notes

For Cloudinary Option B, ensure these columns exist:

```sql
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS profile_image_public_id varchar(500) NULL;

ALTER TABLE trips
    ADD COLUMN IF NOT EXISTS cover_image_url varchar(500) NULL,
    ADD COLUMN IF NOT EXISTS cover_image_public_id varchar(500) NULL;
```

Existing V3 columns should also include:

```text
users.display_name
users.preferred_theme
users.profile_image_url
trip_destinations.created_by_user_id
trip_destinations.modified_by_user_id
destination_activities.created_by_user_id
destination_activities.modified_by_user_id
```

---

## Testing

Backend tests:

```bash
cd backend
./mvnw test
```

Frontend typecheck:

```bash
cd frontend
npm run typecheck
```

Important manual tests after media changes:

```text
1. Upload profile picture
2. Change profile picture and confirm old Cloudinary image is deleted
3. Remove profile picture and confirm old Cloudinary image is deleted
4. Create trip with cover image
5. Edit trip cover image and confirm old Cloudinary image is deleted
6. Remove trip cover image
7. Delete trip with cover image and confirm Cloudinary cleanup
8. Check My Trips and Trip Detail cover display
9. Check destination/activity avatar attribution
10. Check dark mode screens
```

---

## Documentation Index

| Document | Purpose |
|---|---|
| `backend/README.md` | Backend setup, architecture, auth, Cloudinary, tests, deployment |
| `frontend/README.md` | Frontend setup, routes, auth/media integration, manual tests |
| `backend/docs/API_GUIDE.md` | Endpoint examples and response shapes |
| `backend/docs/AUTH_FLOW.md` | Auth/session/OTP flow details |
| `backend/docs/ARCHITECTURE.md` | Backend layering and domain model |
| `backend/docs/POSTMAN_GUIDE.md` | Manual backend API testing flow |
| `backend/docs/TESTING.md` | Backend test coverage notes |

---

## Roadmap

### V3 Complete

```text
✅ Collaboration roles
✅ Invitations and join requests
✅ Share-code join flow
✅ Profile/theme/avatar support
✅ Cloudinary image uploads
✅ Trip cover photos
✅ Creator/editor attribution
✅ Dark mode polish
```

### V4 Portfolio Proof

```text
1. Take screenshots
2. Record demo video
3. Add media to README
4. Polish GitHub repo description
5. Prepare CV bullets
6. Prepare interview explanation
```

### Future Product Features

```text
- Cost sharing and trip budget split
- Viewer suggestion workflow
- AI itinerary suggestions
- Maps/geolocation integration
- Push notifications
- Real SMS provider integration
```

Cost sharing is a good future feature, but it is not needed before V4 portfolio proof.
