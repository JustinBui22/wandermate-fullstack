# Backend Operations

## Health check

```text
GET /Wandermate/api/v1/health
```

Examples:

```text
http://localhost:8080/Wandermate/api/v1/health
http://localhost:8082/Wandermate/api/v1/health
https://wandermate-fullstack.onrender.com/Wandermate/api/v1/health
```

The production URL is the configured deployment target; availability still depends on Render service state and environment configuration.

## Runtime profiles

Base configuration is development-oriented:

```text
Hibernate ddl-auto=update
SQL output enabled
Spring Security/project DEBUG logging
Swagger enabled
```

Production profile:

```text
SPRING_PROFILES_ACTIVE=prod
```

`application-prod.properties` disables SQL output and OpenAPI endpoints and lowers logs to `INFO`.

## Required production environment

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
REFRESH_TOKEN_HASH_SECRET
SPRING_PROFILES_ACTIVE=prod
```

Feature environment:

```text
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
CLOUDINARY_BASE_FOLDER
EMAIL_OAUTH_REFRESH_ENABLED
EMAIL_CLIENT_ID
EMAIL_CLIENT_SECRET
EMAIL_REFRESH_TOKEN
EMAIL_TOKEN_URL
EMAIL_ADDRESS_CONFIG
```

Render environment variables must be configured in Render; a local `.env` file is not deployed automatically.

## CI/CD

`.github/workflows/backend-ci-cd.yml`:

1. checks out backend-related pushes/PRs;
2. installs Temurin Java 21;
3. runs `./mvnw -B test`;
4. on a successful push to `main`, calls `RENDER_DEPLOY_HOOK_URL`.

The deploy-hook value is a GitHub Actions secret and must never appear in source or logs.

## Logs

Local Docker:

```bash
docker compose logs -f backend
docker compose logs -f db
```

Do not log or publish:

- authorization headers;
- raw access/refresh/session tokens or session identifiers;
- OTP values or hashes;
- passwords or sensitive request DTOs;
- email OAuth credentials or token responses;
- trip share codes;
- account email addresses or phone numbers;
- database secrets, Cloudinary secrets, secure URLs or public IDs.

The production profile disables request-detail, Hibernate SQL/bind-value and Apache HTTP client wire/header logging. The base profile currently uses DEBUG logging and `spring.jpa.show-sql=true`, so production must activate the prod profile.

See [Production logging](PRODUCTION_LOGGING.md) for the enforced policy and test guardrail.

## Database backup and restore

Before destructive changes or volume deletion, use MariaDB backup tooling appropriate to the environment. For the local container, a representative pattern is:

```bash
docker exec traveling-app-db mariadb-dump \
  -u root -p traveling_app > traveling_app_backup.sql
```

Do not commit backups. Validate credentials/database names against your `.env` before running commands.

## Docker reset

```bash
docker compose down -v
docker compose up --build
```

This reruns `docker/init/init.sql` but permanently deletes the local named volume.

## Operational risks in the current version

- `ddl-auto=update` changes schema without versioned migration history.
- Public paths depend on a database row; a missing/incorrect seed can change authentication behavior.
- The production profile disables Swagger, so API docs are a local-development tool.
- The email OAuth helper creates a custom scheduled executor and does not currently expose explicit shutdown management.
- The upload API has no abandoned-image cleanup operation.
- OTP values are currently stored in the OTP table rather than as purpose-bound hashes.

Track remediation in [ROADMAP.md](ROADMAP.md).

## Release checklist

- Run backend tests.
- Confirm Render secrets and prod profile.
- Verify health endpoint after deployment.
- Verify login, refresh, protected request, logout and password reset.
- Verify owner/editor/viewer authorization with separate accounts.
- Verify Cloudinary upload and replacement cleanup.
- Review logs for secrets and excessive DEBUG/SQL output.
- Back up data before schema-affecting deployment.
