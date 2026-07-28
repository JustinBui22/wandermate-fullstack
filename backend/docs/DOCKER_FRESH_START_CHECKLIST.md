# Docker Fresh-Start Checklist

Use this only for a disposable/local database. `down -v` permanently removes the MariaDB volume.

## 1. Configure

```bash
cd backend
cp .env.example .env
```

Confirm:

- `DB_URL=jdbc:mariadb://db:3306/traveling_app`;
- `JWT_SECRET` has at least 64 UTF-8 bytes;
- `REFRESH_TOKEN_HASH_SECRET` and `OTP_HASH_SECRET` each have at least 32 UTF-8 bytes;
- real `.env` remains ignored by Git.

## 2. Stop services

```bash
docker compose down
```

## 3. Delete the local database volume when required

```bash
docker compose down -v
```

Skip this step to preserve data.

## 4. Build and start

```bash
docker compose up --build
```

MariaDB becomes healthy, then the backend runs Flyway V1 through the latest migration and validates the schema with Hibernate.

## 5. Check services

```bash
docker compose ps
```

Expected default host ports:

```text
MariaDB: 3307
Backend: 8082
```

## 6. Check health

```bash
curl http://localhost:8082/Wandermate/api/v1/health
```

Local Swagger outside the production profile:

```text
http://localhost:8082/Wandermate/swagger-ui/index.html
```

## 7. Check Flyway/reference data

Confirm `flyway_schema_history` contains successful versions and that required configuration, error and template tables are populated. New users, trips, tokens and OTPs should be absent on a clean database.

The legacy `docker/init/init.sql` is not mounted or executed.

## 8. Frontend URL

```text
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/Wandermate
```

Restart Expo after environment changes:

```bash
npx expo start -c
```

## 9. Review logs

```bash
docker compose logs backend
```

Do not publish tokens, OTPs, credentials, account data, session identifiers, share codes or Cloudinary references.
