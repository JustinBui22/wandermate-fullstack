# Production API Documentation Strategy

WanderMate uses Swagger/OpenAPI during local development, but Swagger UI and OpenAPI JSON are disabled in the production Spring profile.

This keeps the production backend safer while still allowing developers to test and understand the API locally.

---

## Why Swagger Is Disabled in Production

Swagger is very useful during development because it exposes:

```text
- Available endpoints
- Request body models
- Response shapes
- Authentication requirements
- Controller structure
```

However, exposing Swagger publicly in production can reveal too much information about the API surface.

For this project, production should expose only:

```text
- Application API endpoints
- Public health check endpoint
```

Swagger should remain available locally for development and manual testing.

---

## Production Profile

The production profile is enabled on Render with:

```text
SPRING_PROFILES_ACTIVE=prod
```

The production profile disables Swagger/OpenAPI using:

```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

It also reduces development-only logging such as SQL output and debug logs.

---

## Local Swagger Access

Swagger is available when the backend is running locally without the `prod` profile.

Local Maven/IntelliJ backend:

```text
http://localhost:8080/The-Project/swagger-ui/index.html
```

Local Docker backend:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

These local Swagger URLs are intended for development, debugging, and manual API testing.

---

## Production API Access

Production backend on Render:

```text
https://wandermate-fullstack.onrender.com/The-Project
```

Production Swagger UI is disabled.

Instead of using Swagger in production, check deployment health through:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

Expected response:

```json
{
  "status": "UP",
  "service": "WanderMate backend"
}
```

Render free-tier services may sleep when inactive, so the first request can take around 40–60 seconds to wake up.

---

## Production Documentation Sources

Since Swagger is disabled in production, production API behaviour is documented through markdown files and manual test guides.

Recommended documentation files:

```text
backend/docs/API_GUIDE.md
backend/docs/AUTH_FLOW.md
backend/docs/POSTMAN_GUIDE.md
backend/docs/FRONTEND_INTEGRATION.md
backend/docs/OPERATIONS.md
```

Purpose of each document:

| Document | Purpose |
|---|---|
| `API_GUIDE.md` | Endpoint list, headers, request examples, and response shape |
| `AUTH_FLOW.md` | Login, token refresh, logout, max session, and OTP flows |
| `POSTMAN_GUIDE.md` | Manual API testing order and sample payloads |
| `FRONTEND_INTEGRATION.md` | Frontend/backend URL, token storage, and error-handling notes |
| `OPERATIONS.md` | Health check, production profile, logging, and troubleshooting notes |

---

## Recommended Manual API Testing Order

Use Postman or a similar API client for production/manual testing.

Recommended order:

```text
1. Health check
2. Register user
3. Send email OTP
4. Verify email OTP
5. Login
6. Create trip
7. Create destination
8. Create activity
9. Test trip/destination overlap handling
10. Test activity overlap validation
11. Refresh access token
12. Logout
```

---

## Public Endpoint Strategy

Only endpoints required for unauthenticated access should be public.

Examples of public endpoints:

```text
/api/v1/health
/api/v1/users/register
/api/v1/users/login
/api/v1/users/check
/api/v1/otp/send
/api/v1/otp/verify
/api/v1/auth/refresh
```

All user-specific trip, destination, and activity endpoints should require authentication.

Protected endpoint examples:

```text
/api/v1/trips
/api/v1/trips/{tripId}
/api/v1/trips/{tripId}/destinations
/api/v1/trips/{tripId}/destinations/{destinationId}
/api/v1/trips/{tripId}/destinations/{destinationId}/activities
```

---

## Authentication Documentation

Authenticated requests require:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

Refresh token requests require:

```text
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

The token flow is documented in:

```text
backend/docs/AUTH_FLOW.md
```

---

## Frontend Integration

The frontend should not depend on Swagger being available in production.

The frontend uses the configured API base URL:

```env
EXPO_PUBLIC_API_BASE_URL=https://wandermate-fullstack.onrender.com/The-Project
```

Frontend environment switching is documented in:

```text
backend/docs/FRONTEND_INTEGRATION.md
```

---

## Security Notes

Production API documentation should avoid exposing sensitive details.

Do not publish:

```text
- Real database credentials
- Real email/OAuth credentials
- JWT secrets
- Refresh tokens
- Session tokens
- OTP values
- Full production `.env` files
```

Safe to publish:

```text
- Endpoint paths
- Example request bodies with fake values
- Example response shapes
- Local setup instructions
- Docker setup instructions
- Public health endpoint
```

---

## Future Improvements

Possible future improvements:

```text
- Export OpenAPI JSON during CI and publish it as a private artifact
- Protect Swagger behind authentication
- Restrict Swagger access by IP address
- Host API docs privately
- Add Spring Boot Actuator for richer health checks
- Add uptime monitoring for the health endpoint
```

---

## Production Rule

Do not enable Swagger/OpenAPI publicly in production unless it is protected by authentication, IP restrictions, or private network access.

For the current portfolio version, the safest approach is:

```text
Local development: Swagger enabled
Production Render: Swagger disabled
Production health: /api/v1/health
Production docs: markdown + Postman guide
```
