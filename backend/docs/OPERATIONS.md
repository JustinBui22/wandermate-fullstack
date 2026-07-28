# Backend Operations

## Runtime baseline

```text
Spring profile: prod on Render
Schema management: Flyway
Hibernate mode: validate
Context path: /Wandermate
Health path: /Wandermate/api/v1/health
```

## Startup order

1. Connect to MariaDB.
2. Validate Flyway history/checksums.
3. Apply pending migrations.
4. Build JPA entity manager and validate schema.
5. Start the HTTP server.
6. Return `UP` from the health endpoint.

A successful startup means pending migrations completed and Hibernate accepted the final schema. It does not mean Hibernate repaired anything.

## Health checks

Local direct:

```text
http://localhost:8080/Wandermate/api/v1/health
```

Docker:

```text
http://localhost:8082/Wandermate/api/v1/health
```

Render:

```text
https://wandermate-fullstack.onrender.com/Wandermate/api/v1/health
```

## Flyway checks

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

All entries should report success. Do not rerun V1–V6 manually against an already migrated database.

## Production logging

Production disables:

- SQL and bind-value logs;
- Spring request-detail logs;
- HTTP client header/wire dumps;
- debug security/project logs;
- Swagger/OpenAPI endpoints.

Logs must not contain tokens, OTPs, passwords, authorization headers, account destinations, session IDs, share codes, Cloudinary references or complete sensitive DTOs.

See [Production logging](PRODUCTION_LOGGING.md).

## Local database reset

This deletes all local data:

```bash
docker compose down -v
docker compose up --build
```

Flyway rebuilds the schema. The legacy `docker/init/init.sql` is not executed.

## Backup before migration

Before a production deployment containing a new migration:

1. create and verify a database backup;
2. review migration SQL and destructive operations;
3. deploy through CI;
4. confirm the tracked Render deployment becomes live;
5. confirm the health endpoint;
6. inspect `flyway_schema_history` if startup fails.

See [Database backup and recovery](DATABASE_BACKUP_AND_RECOVERY.md).

## Common failure categories

### Database connection failure

Verify `DB_URL`, username, password, network reachability and provider availability.

### Flyway checksum mismatch

An applied migration was modified. Restore the committed original migration; introduce changes through a new version.

### Schema validation failure

The entity model and migrated schema disagree. Add or correct a Flyway migration.

### OTP delivery

For email OTP failures, verify the email OAuth/environment configuration.

Phone OTP is retained only as a portfolio demonstration path. `SmsServiceImpl` currently simulates a successful send and no real message is delivered because a paid SMS provider is not configured. Do not diagnose the absence of a phone message as an infrastructure outage, and do not enable the path for production until a real provider and delivery-failure handling are implemented.

### Render deployment accepted but unhealthy

Use the tracked deployment status and health-polling workflow logs. Check environment variables, database connectivity, Flyway and Spring startup output.
