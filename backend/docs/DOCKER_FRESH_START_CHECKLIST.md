# Docker Fresh Start Checklist

Use this checklist to prove the backend can run from a clean local Docker setup.

---

## Before Running

Confirm these files exist:

```text
backend/Dockerfile
backend/docker-compose.yml
backend/.env.example
backend/docker/init/init.sql
```

Create local `.env`:

```powershell
cd backend
copy .env.example .env
```

Fill safe local values. Cloudinary image upload requires:

```env
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
CLOUDINARY_BASE_FOLDER=wandermate
```

---

## Fresh Reset

Warning: this deletes the Docker database volume.

```powershell
cd backend
docker compose down -v
docker compose up --build
```

---

## Verify

Check containers:

```powershell
docker compose ps
```

Expected:

```text
database container healthy
backend container running
```

Check health:

```text
http://localhost:8082/The-Project/api/v1/health
```

Check Swagger:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

---

## Manual Smoke Test

```text
1. Health endpoint returns UP.
2. Swagger opens locally.
3. Register/login works if seed config is complete.
4. Create trip works.
5. Create destination works.
6. Create activity works.
7. Upload profile image works if Cloudinary env vars are real.
8. Upload trip cover works if Cloudinary env vars are real.
```

---

## Troubleshooting

If the backend starts but flows fail, check that `docker/init/init.sql` includes safe seed rows for:

```text
configuration
error_codes
email_contents
sms_contents
cities
restaurants
accommodations
```

Schema-only SQL is not enough for all runtime flows.
