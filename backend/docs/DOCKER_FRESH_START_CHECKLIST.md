# Docker Fresh Start Checklist

Use this checklist when testing the backend with a fresh MariaDB container.

## Before starting

1. Copy `.env.example` to `.env`, set both independent token secrets.
2. Ensure Docker Desktop is running.
3. Ensure only sanitized SQL is in `backend/docker/init/`.
4. Do not include raw dumps with real runtime data.

## Fresh start commands

```bash
cd backend
docker compose down -v
docker compose up --build
```

## Verify containers

![Docker running](../../docs/screenshots/23-docker-running.png)

## Verify health

```bash
curl http://localhost:8082/Wandermate/api/v1/health
```

The backend container still listens on `8080`; Compose maps the default host
port `8082` to it. Use your configured `BACKEND_HOST_PORT` if different.

## Verify app flow

1. Send OTP.
2. Register or log in.
3. Create trip.
4. Upload image.
5. Create destination.
6. Create nested activity.
7. Invite member or generate share code.
8. Check permissions.

## Troubleshooting

- If DB init does not run, remove the Docker volume and restart.
- If the backend fails during startup, verify `JWT_SECRET` is at least 64 UTF-8
  bytes and `REFRESH_TOKEN_HASH_SECRET` is at least 32 UTF-8 bytes.
- If port is already used, change the host port in `.env`.
- If Cloudinary upload fails, check Cloudinary variables.
- If email OTP fails, check email/OAuth config.
- If Docker works but Expo cannot connect, verify frontend base URL and device networking.
