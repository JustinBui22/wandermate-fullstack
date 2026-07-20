# Database Seed

## File

```text
backend/docker/init/init.sql
```

MariaDB executes this file only when the named Docker volume is initialized for the first time.

## What it contains

- Table definitions and foreign keys.
- Configuration values used by validation, OTP, tokens, sessions, suggestions and invite links.
- Error-code/message rows used by the shared API response system.
- Email and SMS content templates.
- Empty runtime tables for users, trips, collaboration, OTPs and tokens.

It intentionally does not contain real users, real tokens, OTPs, production credentials or copied production data.

## Tables

```text
configuration
error_codes
email_contents
sms_contents
accommodations
cities
restaurants
users
trips
trip_destinations
destination_activities
trip_members
trip_collaboration_requests
trip_share_code_attempts
trip_share_codes
otp_check
refresh_token
session_token
```

## Important clean-seed configuration

| Code | Default |
|---|---|
| `ACCESS_TOKEN_EXPIRATION_TIME` | `300000` ms |
| `REFRESH_TOKEN_EXPIRATION_TIME` | `1` month |
| `MAX_ALLOWED_SESSIONS` | `3` |
| `OTP_EXPIRATION_TIME` | `300000` ms |
| `OTP_RETRY_COOLDOWN` | `60000` ms |
| `OTP_RESTRICTED_TIME` | `900000` ms |
| `MAX_RETRY_SEND_OTP` | `3` |
| `MAX_RETRY_VERIFY_OTP` | `3` |
| `MIN_SUGGEST_CHARACTER` | `2` |
| `INVITE_LINK_PREFIX` | `wandermate://join-trip?code=` |
| `EMAIL_OAUTH_REFRESH_ENABLED` | `false` |

The `NON_AUTHENTICATED_REQUEST` row is also operationally important because both `SecurityConfig` and `TokenFilter` read it at runtime.

## Fresh-volume behavior

Editing `init.sql` does not update an existing MariaDB volume. To rerun it locally:

```bash
docker compose down -v
docker compose up --build
```

This deletes all local database data in the named volume.

## JPA behavior after initialization

The base Spring configuration currently uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Hibernate can therefore adjust the schema at application startup. This is convenient for local development but is not a substitute for versioned migrations. Flyway/Liquibase is not configured in the current `pom.xml`.

## Safe maintenance

- Change reference data intentionally and review error-code uniqueness.
- Keep runtime/user rows out of the public seed.
- Do not add secrets to configuration rows.
- Test a clean start with `docker compose down -v` before relying on seed changes.
- Create a database backup before resetting any non-disposable environment.
