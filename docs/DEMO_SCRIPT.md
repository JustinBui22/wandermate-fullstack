# WanderMate Demo Script

Target duration: five to seven minutes.

## 1. Introduction

Explain that WanderMate is an Expo mobile client backed by Spring Boot and MariaDB. It combines trip planning, nested itinerary management and role-based collaboration with production-oriented authentication and CI/CD controls.

## 2. Architecture

Show the repository layout and summarize:

```text
Expo Router → Axios modules → Spring controllers → services/validators → JPA → MariaDB
```

Mention Flyway migrations, Hibernate schema validation, Cloudinary image storage and Render deployment.

## 3. Authentication

- Show registration with email OTP as the fully operational flow.
- Explain that OTP plaintext is emailed but only a purpose-bound HMAC is persisted.
- Log in and explain access, refresh and session tokens.
- Mention locked refresh rotation and reuse detection.
- Show or mention the phone OTP option only as a demo UI/API path. Explain clearly that the current backend simulates SMS success and sends no real message because a paid provider is not configured.

Fallback evidence: [registration](screenshots/02-register-otp.png) and [login](screenshots/01-login.png).

## 4. Trip planning

- Open My Trips.
- Open a trip with destinations and nested activities.
- Create or edit an item.
- Explain calendar-only trip/destination dates and local activity times.
- Upload a trip cover.

Fallback evidence: [trip detail](screenshots/04-trip-detail-owner.png), [activity detail](screenshots/08-activity-detail.png) and [trip cover](screenshots/05-trip-cover-upload.png).

## 5. Collaboration and authorization

- Generate a share code or invite a member.
- Show requests and role changes.
- Switch to a viewer and demonstrate a restricted action.
- Explain that backend services enforce permissions.
- Mention database locking for share-code generation and redemption.

Fallback evidence: [invite member](screenshots/10-invite-member.png), [role management](screenshots/13-members-role-management.png), [viewer state](screenshots/14-viewer-read-only.png) and [authorization proof](screenshots/28-owner-editor-viewer-proof.png).

## 6. Engineering quality

- Show `./mvnw clean verify` and frontend checks.
- Explain that CI starts an empty MariaDB service and proves Flyway can build the schema from V1–V6.
- Mention CodeQL, Gitleaks, npm audit, OWASP Dependency-Check and Dependabot.
- Open the [live health endpoint](https://wandermate-fullstack.onrender.com/Wandermate/api/v1/health).

Fallback evidence: [backend tests](screenshots/19-backend-tests.png), [frontend typecheck](screenshots/20-frontend-typecheck.png) and [Docker services](screenshots/23-docker-running.png).

## 7. Close

Summarize the strongest decisions:

- backend-enforced permissions;
- session-aware authentication and refresh reuse handling;
- versioned database migrations;
- concurrency-safe share codes;
- deliberate date/time model;
- consistent errors and production-safe logging;
- automated build, deployment and security checks.

Acknowledge only optional future work, such as broader mobile E2E coverage, asynchronous email delivery or replacing the simulated phone-OTP path with a real paid SMS provider if product requirements justify it.

## Technical-question structure

1. Clarify the scenario.
2. State assumptions.
3. Explain the request/data flow.
4. Cover security and edge cases.
5. Explain tests and operational verification.
6. State the trade-off.
