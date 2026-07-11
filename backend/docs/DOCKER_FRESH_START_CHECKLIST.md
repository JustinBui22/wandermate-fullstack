# Docker Fresh Start Checklist

Use this checklist before claiming Docker setup is working.

## 1. Check Docker init folder

Expected:

```text
backend/docker/init/init.sql
```

Not expected:

```text
backend/docker/init/full-init.sql
raw dump files
old schema dumps
```

## 2. Check `.env`

`backend/.env` should exist locally only.

Required DB values:

```env
DB_URL=jdbc:mariadb://db:3306/TravellingApp
DB_USERNAME=app_user
DB_PASSWORD=app_password
MARIADB_DATABASE=TravellingApp
MARIADB_USER=app_user
MARIADB_PASSWORD=app_password
MARIADB_ROOT_PASSWORD=root_password
```

Required Cloudinary values if testing upload:

```env
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...
CLOUDINARY_BASE_FOLDER=wandermate
```

## 3. Reset containers and volumes

From backend folder:

```bash
docker compose down -v
docker compose up --build
```

## 4. Confirm containers

```bash
docker compose ps
```

Expected:

```text
backend service running
MariaDB service running
```

## 5. Test health endpoint

```text
http://localhost:8082/The-Project/api/v1/health
```

Expected:

```text
healthy/success response
```

## 6. Test Swagger locally

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

Expected:

```text
Swagger UI loads locally when production profile is not active
```

## 7. Test auth flow manually

Minimum manual flow:

```text
1. Register/verify user or use local test user created through API.
2. Login.
3. Confirm accessToken, refreshToken, sessionToken are returned.
4. Call GET /api/v1/users/me with Authorization and Session-Token.
5. Call POST /api/v1/auth/refresh with Refresh-Token and Session-Token.
6. Call POST /api/v1/users/logout.
```

## 8. Test core trip flow

```text
1. Create trip.
2. Get trip list.
3. Open trip detail.
4. Add destination.
5. Add activity.
6. Update trip date and confirm status recalculates.
7. Delete test trip if needed.
```

## 9. Test collaboration flow

```text
1. Login as owner.
2. Create trip.
3. Invite another user as VIEWER or EDITOR.
4. Login as invited user.
5. Accept invite.
6. Confirm role-based access.
7. Owner updates role or removes member.
```

## 10. Test image upload if Cloudinary env is set

```text
1. Upload profile image.
2. Save profile.
3. Confirm image URL/public ID are stored.
4. Upload trip cover image.
5. Save trip.
6. Confirm cover URL/public ID are stored.
```

## 11. Final result to record

For README/portfolio, capture proof of:

```text
Docker containers running
Health endpoint working
Frontend typecheck passing
Backend tests passing
```
