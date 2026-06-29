# Project Roadmap

This document describes the roadmap for turning WanderMate into a stronger full-stack portfolio project.

---

## Current Position

The project has reached V3 collaboration-complete status.

Current strengths:

```text
✅ Spring Boot layered backend
✅ Auth with JWT access token, refresh token, and session token
✅ Refresh token rotation/reuse detection
✅ Logout/session revocation
✅ Email OTP flow
✅ Trip → Destination → Activity hierarchy
✅ Role-based collaboration with OWNER/EDITOR/VIEWER
✅ Invitation and join-request workflows
✅ Share-code join workflow
✅ Collaboration summary badge counts
✅ Profile/settings API
✅ Creator/editor attribution
✅ Private overlap warnings
✅ Docker Compose local backend + MariaDB
✅ React Native frontend integration
✅ Dynamic frontend theme and profile UI
✅ Collaboration screens in the mobile app
✅ GitHub Actions CI workflows
✅ Render deployment profile
✅ Uploaded Surefire reports show 373 passing backend tests
```

Current limitations:

```text
⚠️ Real SMS provider integration is not enabled
⚠️ Public Docker demo does not include real email/OAuth secrets
⚠️ Real profile image upload/storage is not implemented
⚠️ Viewer suggestion workflow is not implemented yet
⚠️ Cost sharing is not implemented yet
⚠️ Final screenshots/demo video are still needed for portfolio proof
```

---

## V1 — Stable Portfolio MVP

Status:

```text
✅ Complete
```

Included auth, OTP, trip/destination/activity CRUD, ownership checks, overlap validation, Docker, Swagger, README/docs, and backend tests.

---

## V2 — Professional Backend Polish

Status:

```text
✅ Complete
```

Included CI/CD, production profile, Render deployment, health check, Swagger disabled in production, Docker docs, API docs, and stronger tests.

---

## V2.5 — Frontend Polish

Status:

```text
✅ Complete
```

Included shared UI components, reusable date/time picker components, polished auth/trip/destination/activity screens, cleaner API errors, and Expo public env configuration.

---

## V3 — Collaboration Features

Status:

```text
✅ Complete
```

Included:

```text
✅ Trip collaborators
✅ Owner/editor/viewer roles
✅ Trip member management
✅ Invite user flow
✅ Received invitation list
✅ Join request flow
✅ Owned-trip join request list
✅ Sent join request list
✅ Share-code preview and join request
✅ Collaboration summary badge counts
✅ Permission checks for trip/destination/activity operations
✅ Private overlap warning for affected member only
✅ Profile display name, theme, and avatar URL
✅ Creator/editor attribution for destinations and activities
✅ Frontend collaboration tab and per-trip collaboration screens
```

Why V3 is valuable:

```text
It demonstrates authorization, relationship modelling, product thinking, and multi-user backend/frontend design.
```

---

## V4 — Portfolio Proof

Goal:

```text
Make the project easy for employers to understand in 30-90 seconds.
```

Recommended tasks:

```text
1. Take screenshots of key mobile screens
2. Record a 60-90 second demo video
3. Add screenshots/demo link to root README
4. Add a short architecture diagram image if desired
5. Add final project summary to CV/GitHub portfolio
6. Pin the repo and update the GitHub repository description
7. Add a concise demo flow section near the top of README
```

Suggested screenshots:

```text
- Login
- My Trips
- Trip Detail
- Collaboration tab
- Invite member
- Join request list
- Share-code screen
- Profile/theme screen
- Destination detail with attribution
- Activity detail with attribution
```

Suggested demo flow:

```text
Login → Create Trip → Add Destination → Add Activity → Open Collaboration → Invite User → Accept Invitation → Show Role Access → Show Attribution → Show Profile Theme
```

---

## V5 — Smart Planning and Cost Features

Goal:

```text
Make the app feel more like a real travel product after the portfolio proof is ready.
```

Recommended features:

```text
1. Trip budget
2. Activity cost estimate
3. Cost sharing between collaborators
4. Paid-by / split-between fields
5. Summary per person
6. Viewer destination/activity suggestion workflow
7. AI itinerary suggestions
8. Place/map API integration
9. Smart conflict suggestions
```

---

## Recommended Next Order

```text
1. Fix any final local backend/frontend test issues
2. Commit V3 docs update
3. Run backend tests and frontend typecheck locally
4. Capture screenshots
5. Record demo video
6. Add README media section
7. Update CV project bullet points
8. Start V5 only after portfolio proof is finished
```

---

## CV Positioning

Recommended CV wording:

```text
Built a production-style full-stack travel planning app using Spring Boot, MariaDB, JWT authentication, refresh/session token management, Docker, and Expo React Native. Implemented role-based trip collaboration with owner/editor/viewer permissions, invitation and join-request workflows, share-code joining, OTP registration, trip/destination/activity CRUD, overlap validation, creator/editor attribution, standardized API responses, and 370+ backend tests.
```
