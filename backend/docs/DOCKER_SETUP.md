# Docker Setup

## Services

`backend/docker-compose.yml` defines:

- `db`: MariaDB 11 with a named data volume;
- `backend`: the packaged Spring Boot service.

Default host mappings from `.env.example`:

```text
MariaDB  localhost:3307 → db:3306
Backend  localhost:8082 → backend:8080
```

## Configure

```bash
cd backend
cp .env.example .env
```

Replace placeholders, especially:

```text
DB_NAME
DB_USERNAME
DB_PASSWORD
DB_ROOT_PASSWORD
DB_URL
JWT_SECRET
REFRESH_TOKEN_HASH_SECRET
OTP_HASH_SECRET
```

Inside Docker, the datasource host is `db`, not `localhost`:

```text
jdbc:mariadb://db:3306/traveling_app
```

## Start

```bash
docker compose up --build
```

Check:

```text
http://localhost:8082/Wandermate/api/v1/health
```

## Database initialization

The Compose file does not mount `docker/init`. Flyway applies migrations after MariaDB becomes healthy. Hibernate then validates the schema.

## Use database container with IntelliJ backend

```bash
docker compose up -d db
```

Use:

```text
DB_URL=jdbc:mariadb://localhost:3307/traveling_app
```

Set the remaining variables in the IntelliJ Run Configuration.

## Stop

```bash
docker compose down
```

## Reset local database

This deletes the named volume and all local data:

```bash
docker compose down -v
docker compose up --build
```

Flyway reconstructs the database from V1 onward.

## Troubleshooting

### Backend cannot reach database

- In Docker use `db:3306`.
- From the host use `localhost:3307`.
- Check `docker compose ps` and database health.

### Flyway fails

- Read the first migration error in backend logs.
- Do not edit a migration already recorded in `flyway_schema_history`.
- Fix the migration with a new version or restore the database according to the recovery procedure.

### Hibernate validation fails

The database and Java entity mappings differ. Hibernate will not repair the schema. Add/fix a Flyway migration.
