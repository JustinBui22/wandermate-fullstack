# Production API Docs

Production deployment uses the same context path:

```text
/Wandermate
```

Render base URL:

```text
https://wandermate-fullstack.onrender.com/Wandermate
```

## Health check

[Open the production health endpoint](https://wandermate-fullstack.onrender.com/Wandermate/api/v1/health).
The free Render service may take about a minute to wake after inactivity.

## Render logs proof

![Render logs](../../docs/screenshots/25-render-logs.png)

## Swagger/OpenAPI

Swagger/OpenAPI is useful locally for development and endpoint inspection.

Local Swagger proof:

![Swagger local](../../docs/screenshots/18-swagger-local.png)

In production profile, Swagger may be disabled for security/simplicity depending on configuration.

## Production testing checklist

1. Health endpoint returns success.
2. Register/login works against production DB/config.
3. OTP email works.
4. Access token works on protected endpoint.
5. Refresh token works.
6. Logout revokes session.
7. Cloudinary image upload works.
8. Trip/destination/activity CRUD works.
9. Collaboration roles work.
10. No secrets are exposed in logs or docs.

## Environment warning

Production secrets must live in Render environment variables, not in committed files.

Required security variables include:

```text
JWT_SECRET
REFRESH_TOKEN_HASH_SECRET
```

Use independent random values. The JWT value must contain at least 64 UTF-8
bytes and the refresh-token HMAC value at least 32. Rotating them invalidates
active tokens; a refresh-HMAC rotation signs out existing sessions.
