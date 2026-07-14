# Roadmap

## Current V4 scope: complete

V4 focuses on portfolio proof and interview readiness.

Completed areas:

- Secure auth/session/OTP flow.
- Trip CRUD.
- Destination CRUD.
- Nested activity CRUD.
- Owner/editor/viewer collaboration.
- Invitations and join requests.
- Share-code joining.
- Member role management.
- Cloudinary profile/trip image upload.
- Frontend light/dark theme support.
- Persistent bottom tabs.
- Backend tests.
- Docs and screenshot proof.

## Do not add before portfolio submission

Avoid adding large new features before screenshots/demo/CV are done.

Do not add yet:

- Stripe payments.
- Real-time collaboration.
- Chat.
- Expense splitting.
- Large refactors.

## Suggested V5 features

- Trip cost planning and expense splitting.
- Viewer suggestion workflow.
- Push notifications.
- Email invitation links.
- Better activity maps/geolocation.
- E2E tests.
- Testcontainers for backend integration tests.
- Flyway or Liquibase migrations.
- Split broad enums into more focused enum types.

## Production hardening later

- Hash OTP codes in DB.
- Add explicit OTP purpose field.
- Add IP/device rate limiting.
- Add centralized audit logging.
- Strengthen Cloudinary upload validation.
- Add CI pipeline for backend tests and frontend typecheck.
