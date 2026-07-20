# Production API Documentation

## OpenAPI configuration

`OpenApiConfig` defines:

```text
Title: Traveling App API
Version: v1
Security scheme: HTTP Bearer JWT (`bearerAuth`)
```

Controller interfaces provide the current endpoint mappings.

## Local Swagger

Direct local backend:

```text
http://localhost:8080/Wandermate/swagger-ui/index.html
```

Docker backend:

```text
http://localhost:8082/Wandermate/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/Wandermate/v3/api-docs
```

## Production behavior

`application-prod.properties` contains:

```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

Therefore the Render production service is not intended to expose Swagger UI or `/v3/api-docs` while the prod profile is active.

## Authorizing locally

Swagger's Bearer scheme adds:

```http
Authorization: Bearer <access-token>
```

The current backend also requires:

```http
Session-Token: <session-token>
```

Swagger does not currently define that custom header as a global security scheme, so protected testing may be easier in Postman or by manually adding the header where supported.

Refresh also requires separate `Refresh-Token` and `Session-Token` headers.

## Production documentation policy

- Keep Swagger disabled publicly unless there is a deliberate access-control decision.
- Use `backend/docs/API_GUIDE.md` as the repository endpoint inventory.
- Do not publish example tokens or credentials.
- Keep API examples synchronized with controller interfaces and request DTOs.
- Treat the configured Render base URL as a deployment target, not proof of current uptime.
