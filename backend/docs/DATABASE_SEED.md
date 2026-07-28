# Database Migrations and Reference Data

## Source of truth

The active database is created and upgraded by Flyway migrations under:

```text
backend/src/main/resources/db/migration
```

V1 creates the initial schema and reference rows. Later migrations evolve that schema.

Reference data includes:

- configuration values;
- application error definitions;
- email templates;
- legacy SMS template rows;
- other static lookup content required by services.

Runtime users, trips, tokens, OTPs and collaboration data are not public seed data.

## Docker startup

The current `docker-compose.yml` mounts only the MariaDB data volume. It does not mount `docker/init` into `/docker-entrypoint-initdb.d`.

On an empty volume:

1. MariaDB starts with an empty application database.
2. Spring Boot connects.
3. Flyway applies V1 through the latest migration.
4. Hibernate validates the resulting schema.
5. The application becomes healthy.

## Legacy `docker/init/init.sql`

`backend/docker/init/init.sql` is old V1-era reference material. It is not the current migration mechanism and must not be run against an existing database.

Do not edit it to introduce schema changes. Do not mount it alongside Flyway because that would create competing schema owners.

## Clean local rebuild

This permanently deletes local database data:

```bash
docker compose down -v
docker compose up --build
```

Flyway rebuilds the schema from versioned migrations.

## Existing database

Flyway checks `flyway_schema_history` and applies only missing migrations. Hibernate uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

It reports mismatches and does not fix them.

## Rules

- Never edit an applied migration.
- Add a new version for every schema/reference-data change.
- Back up production before deploying a migration.
- Do not manually insert Flyway history rows except as part of a deliberate baseline/recovery procedure.
