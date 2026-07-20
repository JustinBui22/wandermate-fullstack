# Backend Docs Index

This folder documents the WanderMate Spring Boot backend.

## Docs

- [API Guide](API_GUIDE.md)
- [Architecture](ARCHITECTURE.md)
- [Authentication Flow](AUTH_FLOW.md)
- [Cloudinary Image Storage](CLOUDINARY_IMAGE_STORAGE.md)
- [Database Seed](DATABASE_SEED.md)
- [Docker Fresh Start Checklist](DOCKER_FRESH_START_CHECKLIST.md)
- [Docker Setup](DOCKER_SETUP.md)
- [Frontend Integration](FRONTEND_INTEGRATION.md)
- [Operations](OPERATIONS.md)
- [Postman Guide](POSTMAN_GUIDE.md)
- [Production API Docs](PRODUCTION_API_DOCS.md)
- [Roadmap](ROADMAP.md)

## Current backend proof

The current backend suite defines:

```text
438 JUnit test methods
```

The screenshot below predates the newest security and transaction tests. Run
`./mvnw test` and replace it after a green local run.

![Backend tests](../../docs/screenshots/19-backend-tests.png)

## Main backend responsibilities

- Secure authentication and session management.
- OTP generation, send, cooldown, retry, verify and consume flow.
- Trip, destination and nested activity management.
- Owner/editor/viewer collaboration rules.
- Invitation, join request and share-code workflows.
- Cloudinary image upload for profile avatars and trip cover images.
- Production-friendly configuration for Render and Docker.
- Automated backend tests.
