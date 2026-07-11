# Database Seed Strategy

This document explains how WanderMate should seed the local Docker database safely.

## Current Recommendation

Do not commit a full cloud/production database dump.

Use a clean local seed file only for reference data:

```text
backend/docker/init/init.sql
```

The local Docker database should contain:

```text
✅ schema or safe reference table creation required for local boot
✅ configuration seed rows
✅ error_codes seed rows
✅ email_contents seed rows
✅ sms_contents seed rows
```

It should not contain:

```text
❌ real users
❌ real emails
❌ hashed passwords from private accounts
❌ OTP rows
❌ refresh token rows
❌ session token rows
❌ trip/demo data
❌ Cloudinary private demo URLs
❌ Google OAuth access tokens
❌ Google OAuth refresh tokens
❌ Google client secrets
```

## Why Not Use Raw Dumps?

Raw MariaDB dumps often include:

```text
- real user records
- OAuth tokens
- hashed session/refresh tokens
- one-time passwords
- generated AUTO_INCREMENT values
- lock/disable-key commands
- old or dirty test/demo rows
```

That is risky for public GitHub and messy for portfolio review.

## Safe Seed Tables

The backend depends on configuration/error/template data for many flows.

Safe seed tables:

```text
configuration
error_codes
email_contents
sms_contents
```

Runtime tables should start empty:

```text
users
trips
trip_destinations
destination_activities
trip_members
trip_collaboration_requests
trip_share_codes
trip_share_code_attempts
otp_check
refresh_token
session_token
```

## Required Placeholder Values

Sensitive configuration rows must use placeholders in GitHub:

```text
SECRET_KEY_CONFIG=replace-with-a-strong-256-bit-secret-key-for-local-dev-only
EMAIL_ADDRESS_CONFIG=your-email@example.com
EMAIL_ACCESS_TOKEN_CONFIG=SET_BY_APPLICATION_RUNTIME_OR_ENV
EMAIL_CLIENT_ID=SET_IN_ENV
EMAIL_CLIENT_SECRET=SET_IN_ENV
EMAIL_REFRESH_TOKEN=SET_IN_ENV
```

Real secrets should come from:

```text
backend/.env locally
Docker environment variables
Render environment variables
```

## Important Cleanup

Do not keep both files in Docker init:

```text
backend/docker/init/init.sql
backend/docker/init/full-init.sql
```

Docker runs `.sql` files in the init folder. Keeping both can create old/stale schemas or duplicate data.

Recommended final folder:

```text
backend/docker/init/init.sql
```

Delete or archive outside the project:

```text
backend/docker/init/full-init.sql
```

## Fresh Start Test

After replacing the init SQL:

```bash
cd backend
docker compose down -v
docker compose up --build
```

Then test:

```text
http://localhost:8082/The-Project/api/v1/health
```

Expected result:

```text
backend starts successfully
MariaDB container starts successfully
health endpoint responds
no duplicate table/constraint SQL errors
```

## Future Improvement

For a more production-grade backend, replace `ddl-auto=update` and SQL dumps with migrations:

```text
Flyway
Liquibase
```

Suggested future migration structure:

```text
V1__create_auth_tables.sql
V2__create_trip_tables.sql
V3__create_collaboration_tables.sql
V4__add_image_fields.sql
V5__seed_reference_data.sql
```
