# Docker Fresh-Start Checklist

Use this only for a disposable/local database. `down -v` permanently removes the Compose MariaDB volume.

## 1. Review configuration

```bash
cd backend
cp .env.example .env
```

Confirm:

- `DB_URL=jdbc:mariadb://db:3306/traveling_app`.
- `JWT_SECRET` has at least 64 UTF-8 bytes.
- `REFRESH_TOKEN_HASH_SECRET` has at least 32 UTF-8 bytes.
- Cloudinary/email placeholders are replaced only when those features are being tested.
- Real `.env` remains ignored by Git.

## 2. Stop existing services

```bash
docker compose down
```

## 3. Reset database volume when a clean schema/seed is required

```bash
docker compose down -v
```

Skip this step to preserve existing data.

## 4. Build and start

```bash
docker compose up --build
```

Wait for the database health check and backend startup.

## 5. Verify containers

```bash
docker compose ps
```

Expected host ports:

```text
MariaDB: 3307
Backend: 8082
```

## 6. Verify API

```bash
curl http://localhost:8082/Wandermate/api/v1/health
```

Open local Swagger when not using the production profile:

```text
http://localhost:8082/Wandermate/swagger-ui/index.html
```

## 7. Verify seed

Connect to `localhost:3307` with the `.env` database credentials and confirm the configuration/error/reference tables are populated while users/trips/tokens are empty.

## 8. Verify frontend configuration

```text
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/Wandermate
```

Restart Expo with cache clearing after changing `.env`:

```bash
npx expo start -c
```

## 9. Verify logs contain no secrets

```bash
docker compose logs backend
```

Do not capture or publish raw authorization, refresh, session or OTP values.
