# Project Roadmap

This document describes the suggested roadmap for turning WanderMate into a stronger portfolio project.

---

## Current Position

The project is already beyond basic CRUD.

Current strengths:

```text
✅ Spring Boot layered backend
✅ Auth with JWT access token, refresh token, and session token
✅ Refresh token rotation/reuse detection
✅ Logout/session revocation
✅ Email OTP flow
✅ Trip → Destination → Activity hierarchy
✅ Ownership checks
✅ Overlap and date/time validation
✅ Docker Compose local backend + MariaDB
✅ React Native frontend integration
✅ 197 passing service-level backend tests
```

Current limitations:

```text
⚠️ Real SMS provider integration is not enabled
⚠️ Public Docker demo does not include real email/OAuth secrets
⚠️ Frontend environment switching is still manual
⚠️ No GitHub Actions CI yet
⚠️ No collaborator/share-trip feature yet
```

---

## V1 — Stable Portfolio MVP

Goal:

```text
A clean, working full-stack travel planner with authentication, CRUD, Docker, tests, and documentation.
```

V1 includes:

- Register/login/logout
- Email OTP
- JWT access token
- Refresh token
- Session token
- Max active session handling
- Trip CRUD
- Destination CRUD
- Activity CRUD
- Ownership checks
- Overlap/date validation
- Docker Compose setup
- Swagger docs
- README/docs
- Service-level tests

Status:

```text
Mostly complete. Current focus should be cleanup, docs, GitHub polish, and demo proof.
```

---

## V2 — Professional Backend Polish

Goal:

```text
Make the backend look reliable, clean, and employer-ready.
```

Recommended tasks:

```text
1. Add GitHub Actions CI for backend tests
2. Keep root .gitignore clean and ensure .env/target/node_modules are not tracked
3. Add screenshots and demo flow to README
4. Add a Postman collection export
5. Add a short demo video/GIF
6. Add production profile settings
7. Disable/restrict Swagger in production
8. Add integration tests or Testcontainers later
9. Clean remaining debug logs if needed
10. Add cloud/local environment documentation
```

Why V2 matters:

```text
Employers trust clean testing, CI, docs, Docker, and deployment more than unfinished extra features.
```

---

## V3 — Collaboration Features

Goal:

```text
Turn the app from a personal planner into a shared trip planning product.
```

Recommended features:

```text
1. Trip collaborators
2. Owner/editor/viewer roles
3. Invite link or invite by username/email
4. Shared-with-me screen
5. Permission checks for every trip/destination/activity operation
6. Audit fields for who created/modified an item
```

Why V3 is valuable:

```text
It demonstrates authorization, relationship modelling, product thinking, and multi-user backend design.
```

Suggested role model:

```text
OWNER  → full access, delete trip, invite/remove collaborators
EDITOR → create/update destinations and activities
VIEWER → read-only access
```

---

## V4 — Smart Planning and Cost Features

Goal:

```text
Make the app feel more like a real travel product.
```

Recommended features:

```text
1. Trip budget
2. Activity cost estimate
3. Cost sharing between collaborators
4. Paid-by / split-between fields
5. Summary per person
6. AI itinerary suggestions
7. Place/map API integration
8. Smart conflict suggestions
```

Why V4 should come later:

```text
These features are useful, but only after the core app is stable and professionally presented.
```

---

## Recommended Next Order

```text
1. Commit Docker Compose + .env.example fixes
2. Commit README/docs updates
3. Confirm Docker Swagger URL works
4. Confirm service tests pass
5. Add GitHub Actions CI
6. Add screenshots/demo GIF
7. Polish GitHub repository description and pinned repo
8. Add project to CV
9. Start V3 collaboration design
```

---

## CV Positioning

Recommended CV wording:

```text
Built a production-style full-stack travel planning app using Spring Boot, MariaDB, JWT authentication, refresh/session token management, Docker, and Expo React Native. Implemented ownership checks, OTP-based registration flow, trip/destination/activity CRUD, overlap validation, standardized error responses, and 190+ service-level backend tests.
```

Avoid describing it as only a student app. Present it as a production-style portfolio system built to practise backend engineering patterns.
