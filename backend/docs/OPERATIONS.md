# Operations Guide

This document covers local operations, production deployment checks, and maintenance tasks for WanderMate backend.

## Environments

| Environment | Backend URL | Notes |
|---|---|---|
| Local IntelliJ | `http://localhost:8080/The-Project` | Uses local env vars |
| Local Docker | `http://localhost:8082/The-Project` | Uses Docker Compose env vars |
| Production Render | `https://wandermate-fullstack.onrender.com/The-Project` | Production profile, Swagger disabled |

## Health Check

Use this endpoint to verify the backend is running:

```text
GET /api/v1/health
```

Local Docker:

```text
http://localhost:8082/The-Project/api/v1/health
```

Production:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

## Production Environment Variables

Production requires:

```env
PORT=8080
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mariadb://...
DB_USERNAME=...
DB_PASSWORD=...
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...
CLOUDINARY_BASE_FOLDER=wandermate
```

Secrets must be configured through Render environment settings, not committed to GitHub.

## Production Profile Behaviour

`application-prod.properties` disables Swagger/OpenAPI:

```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
spring.jpa.show-sql=false
logging.level.org.springframework.security=INFO
```

This is good for production safety.

## Deployment Checklist

Before deployment:

```text
1. Run backend tests locally.
2. Run frontend typecheck.
3. Confirm .env files are not committed.
4. Confirm raw SQL dumps are not committed.
5. Confirm Cloudinary env vars are set in Render.
6. Confirm production DB credentials are set in Render.
7. Confirm production health endpoint works.
```

Backend test command:

```bash
cd backend
./mvnw test
```

Frontend typecheck:

```bash
cd frontend
npm run typecheck
```

## Local DB Reset

Docker clean reset:

```bash
cd backend
docker compose down -v
docker compose up --build
```

Only do this locally. It removes Docker volumes.

## Log Checks

Useful things to check in logs:

```text
Spring Boot startup success
MariaDB connection success
Hibernate schema update success
Cloudinary env loaded
Token/security filter warnings
Unhandled exceptions
```

## Image Storage Operations

Cloudinary image cleanup depends on `publicId` fields:

```text
users.profile_image_public_id
trips.cover_image_public_id
```

If old rows have image URL but no public ID, they can display but cannot be automatically deleted from Cloudinary.

## Public Sharing Rules

Never share:

```text
backend/.env
frontend/.env
raw cloud DB dump
backend/target
frontend/node_modules
.git folder in manual zip
.idea folder
.expo folder
```

Use:

```bash
git archive --format=zip --output=wandermate-clean.zip HEAD
```

or publish the clean GitHub repo.

## Incident Notes

If secrets were accidentally included in a shared zip or raw dump:

```text
1. Remove the file from public access immediately.
2. Rotate/revoke Google OAuth refresh token.
3. Rotate Google client secret if exposed.
4. Rotate DB password if exposed.
5. Rotate Cloudinary API secret if exposed.
6. Check Git history if the secret was committed.
```

## Portfolio Proof Operations

Before recording the final demo:

```text
1. Re-upload profile avatar.
2. Re-upload trip cover image.
3. Confirm public IDs are saved.
4. Use clean demo users.
5. Clear old session clutter if needed.
6. Confirm owner/editor/viewer flows.
7. Confirm Render health endpoint.
8. Capture backend test result and frontend typecheck proof.
```
