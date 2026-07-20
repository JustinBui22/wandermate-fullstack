# Database Seed and Sanitized Init Strategy

The backend uses MariaDB and Hibernate/JPA.

## Local schema strategy

The application currently uses Hibernate `ddl-auto=update` for local development and portfolio deployment. This is acceptable for the current V4 portfolio phase.

## Docker init SQL

Docker init files live under:

```text
backend/docker/init/
```

Use only sanitized seed data in public repositories.

## Safe seed data

Safe seed data can include:

- configuration values without secrets
- error code rows
- email/SMS template rows without real credentials
- static city/restaurant/accommodation demo data if sanitized

## Unsafe seed data

Do not commit or share:

- real users
- passwords or password hashes from real users
- OTP rows
- refresh token rows
- session token rows
- raw access tokens
- Cloudinary real private secrets
- Google OAuth refresh tokens/client secrets
- real trip/member runtime data

## Public sharing rule

A clean portfolio repo should contain:

```text
backend/docker/init/init.sql
```

Do not share raw full database dumps such as:

```text
full-init.sql
```

unless manually sanitized and reviewed.

After this security update, existing non-empty databases also need the generic
login message and the removed public check URL synchronized with the sanitized
seed. A completely fresh Docker database receives the correct values from
`init.sql`.

Changing `REFRESH_TOKEN_HASH_SECRET` means existing refresh-token hashes can no
longer be matched. Treat the change as a planned global sign-out.

## Database proof

![Database schema](../../docs/screenshots/26-database-schema.png)
