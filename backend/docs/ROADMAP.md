# WanderMate Roadmap

This roadmap summarizes what has been built and what should come next.

## Current Phase

```text
V4 portfolio proof stage
```

The main product features are complete enough. The priority is now:

```text
screenshots
demo video
README media
CV/LinkedIn project proof
clean public repo
```

## V1 — Backend Foundation

Status:

```text
Complete
```

Included:

```text
Spring Boot project setup
MariaDB persistence
JPA entities/repositories
standard response wrapper
configuration/error code tables
basic user/auth flow
trip CRUD foundation
```

## V2 — Security and Trip Planning

Status:

```text
Complete
```

Included:

```text
JWT access token
refresh token storage/rotation
session token validation
logout/session revocation
OTP registration and forgot password
trip/destination/activity CRUD
date validation
overlap warning/error rules
backend service/controller tests
```

## V3 — Collaboration and Image Polish

Status:

```text
Complete
```

Included:

```text
owner/editor/viewer trip roles
trip members
invite member flow
received invitations
join request flow
owned/sent request lists
share-code generation and join request
collaboration summary badge counts
role-based backend access control
role-based frontend UI hiding
viewer read-only mode
creator/editor attribution
profile avatar upload
trip cover upload
Cloudinary cleanup by public ID
profile theme preference
```

## V4 — Portfolio Proof

Status:

```text
In progress
```

Goal:

```text
Make the project understandable and impressive in 30-90 seconds.
```

Tasks:

```text
1. Clean repo/shareable zip.
2. Delete old raw SQL dumps and local/private files.
3. Run backend tests.
4. Run frontend typecheck.
5. Take mobile screenshots.
6. Record 60-90 second demo video.
7. Add screenshots/demo link to README.
8. Update CV and LinkedIn project entry.
```

Suggested screenshots:

```text
01-login.png
02-my-trips.png
03-trip-detail-owner.png
04-trip-cover-upload.png
05-destinations-with-creator.png
06-activity-attribution.png
07-collaboration-menu-owner.png
08-invite-member.png
09-share-code.png
10-join-requests.png
11-members-role-management.png
12-viewer-read-only.png
13-profile-avatar-settings.png
14-dark-mode.png
15-render-health.png
16-github-actions.png
```

## V5 — Future Product Features

Do not start until V4 proof is finished.

Recommended future features:

```text
trip budget estimate
activity cost estimate
expense splitting between trip members
paid-by / split-between fields
per-person settlement summary
viewer suggestion workflow
AI itinerary suggestions
map/place API integration
smart conflict suggestions
calendar export
push notifications
```

## Technical Improvements Later

These are good interview discussion points, but not urgent before screenshots:

```text
split broad TripEnum into smaller enums
add @Transactional to multi-write service methods
replace ddl-auto=update with Flyway/Liquibase
add Testcontainers integration tests
add frontend E2E tests
add CI/CD screenshots to README
```

## CV Positioning

Recommended project bullet:

```text
Built WanderMate, a production-style full-stack travel planning app using Spring Boot, MariaDB, JWT auth, refresh/session tokens, Docker, Render, Cloudinary, and Expo React Native. Implemented owner/editor/viewer collaboration, invitation and join-request workflows, share-code joining, trip/destination/activity CRUD, image uploads, creator attribution, role-based access control, and 399 backend tests.
```
