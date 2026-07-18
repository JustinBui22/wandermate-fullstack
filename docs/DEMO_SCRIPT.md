# WanderMate Demo Script

This script is designed for a focused 6–8 minute mentor or recruiter demonstration.

## Pre-demo setup

- Start the backend and database, or wake the Render service in advance.
- Start Expo and open the mobile app before screen sharing.
- Prepare owner, editor and viewer demo accounts with non-sensitive sample data.
- Keep the repository, test output and fallback screenshots open in separate tabs.
- Clear tokens, credentials, email addresses and personal information from visible tools.

## 1. Problem and solution — 30 seconds

“WanderMate is a collaborative mobile travel planner. It helps a group organize trips,
destinations and activities while preventing schedule conflicts and enforcing clear owner,
editor and viewer permissions.”

## 2. Architecture — 45 seconds

Briefly explain the request flow:

1. React Native and Expo send typed requests to the REST API.
2. Spring Boot validates input, authenticates the session and enforces authorization.
3. MariaDB stores application data, while Cloudinary stores uploaded images.
4. Docker supports repeatable local setup, and GitHub Actions validates the project.

Use the [architecture documentation](../backend/docs/ARCHITECTURE.md) if a deeper explanation
is requested.

## 3. Authentication — 60 seconds

- Show registration and email OTP verification.
- Log in and explain short-lived access tokens, refresh-token rotation and server-managed sessions.
- State that secrets are not stored in the repository and protected routes are enforced by Spring Security.

Fallback evidence: [registration and OTP](screenshots/02-register-otp.png),
[login](screenshots/01-login.png) and [logout revocation](screenshots/30-logout-session-proof.png).

## 4. Core trip planning — 90 seconds

- Open My Trips and select a trip.
- Show destinations and nested activities.
- Create or edit an activity and explain server-side validation and schedule-conflict detection.
- Upload a trip cover to demonstrate the Cloudinary integration.

Fallback evidence: [trip detail](screenshots/04-trip-detail-owner.png),
[activity detail](screenshots/08-activity-detail.png) and
[trip cover upload](screenshots/05-trip-cover-upload.png).

## 5. Collaboration and authorization — 90 seconds

- As the owner, invite a member or generate a share code.
- Show join requests and change a member's role.
- Switch to a viewer account and attempt a restricted action.
- Explain that permissions are enforced by the backend, not only hidden in the interface.

Fallback evidence: [invite member](screenshots/10-invite-member.png),
[role management](screenshots/13-members-role-management.png),
[viewer read-only state](screenshots/14-viewer-read-only.png) and
[authorization proof](screenshots/28-owner-editor-viewer-proof.png).

## 6. Engineering quality — 60 seconds

- Show the automated backend test result and frontend typecheck.
- Mention Docker-based setup, the sanitized database seed and CI validation.
- Open the [live health endpoint](https://wandermate-fullstack.onrender.com/The-Project/api/v1/health)
  and note that the free service may take about a minute to wake.

Fallback evidence: [backend tests](screenshots/19-backend-tests.png),
[frontend typecheck](screenshots/20-frontend-typecheck.png) and
[Render logs](screenshots/25-render-logs.png).

## 7. Close — 30 seconds

Summarize the strongest engineering decisions: backend-enforced role permissions, session-aware
authentication, schedule validation, automated tests and repeatable deployment. Then acknowledge
one realistic improvement, such as replacing schema auto-update with versioned Flyway migrations,
adding end-to-end tests, or improving offline mobile behavior.

## Technical-question structure

For follow-up questions:

1. Clarify the scenario.
2. State assumptions.
3. Explain the request flow.
4. Cover security and edge cases.
5. Discuss testing and complexity.
6. Acknowledge the trade-off or next improvement.
