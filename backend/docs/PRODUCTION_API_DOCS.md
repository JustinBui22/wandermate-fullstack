# Production API Documentation

This document explains how API documentation is handled for production.

## Production Base URL

```text
https://wandermate-fullstack.onrender.com/The-Project
```

Health endpoint:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

## Swagger Policy

Swagger UI and OpenAPI docs are disabled in production:

```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

This means these production URLs should not expose docs:

```text
/The-Project/swagger-ui/index.html
/The-Project/v3/api-docs
```

## Why Disable Swagger in Production?

Reasons:

```text
- avoid exposing endpoint metadata publicly
- reduce attack surface
- keep production cleaner
- document public portfolio details through README/docs instead
```

## Where API Docs Live

For local development:

```text
http://localhost:8080/The-Project/swagger-ui/index.html
http://localhost:8082/The-Project/swagger-ui/index.html
```

For GitHub/portfolio:

```text
backend/docs/API_GUIDE.md
backend/docs/POSTMAN_GUIDE.md
backend/docs/AUTH_FLOW.md
```

## Production Verification

Before using the production URL in README/CV/demo:

```text
1. Open health endpoint.
2. Confirm successful response.
3. Confirm Swagger is not exposed in production.
4. Login with a demo account if demo DB is configured.
5. Test one protected endpoint.
```

## README Recommendation

In the root README, link production like this:

```markdown
Backend health: https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

Do not include secrets, raw tokens, or private test accounts in README.
