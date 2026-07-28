# Backend Architecture

## Request flow

```text
HTTP request
    ↓
CORS / Spring Security / TokenFilter
    ↓
Controller interface and implementation
    ↓
Service interface and implementation
    ↓
Validator / mapper / security helper
    ↓
Spring Data repository
    ↓
MariaDB
```

External integrations are isolated behind service/client classes for Cloudinary and email delivery.

## Layers

### Controllers

Controllers map HTTP methods, paths and validated DTOs. Controller implementations delegate to services and return the existing `ResponseBody<Object>` envelope.

### Services

Services own business rules and transaction boundaries, including:

- authentication/session lifecycle;
- refresh-token rotation and reuse handling;
- trip/destination/activity range validation;
- collaboration permissions;
- share-code generation and redemption locking;
- image-reference validation and cleanup.

### Validators and mappers

Validators reject invalid input and authorization/business-rule violations. Mappers separate persistence entities from API DTOs.

### Repositories

Spring Data JPA repositories provide ordinary queries and explicit pessimistic-lock queries for concurrency-sensitive flows.

## Security architecture

- `PublicEndpointMatcher` owns the method-specific public-route list.
- `TokenFilter` and Spring Security use the same matcher.
- Protected requests require access and session tokens.
- `JsonAuthenticationEntryPoint` returns consistent 401 JSON.
- `JsonAccessDeniedHandler` returns consistent 403 JSON.
- `GlobalExceptionHandler` maps controller/framework exceptions into the shared response structure.
- Production logs omit tokens, OTPs, account destinations, session identifiers, share codes and Cloudinary references.

## Authentication persistence

```text
access token     signed HS512 JWT, not stored as plaintext database state
refresh token    HMAC-SHA256 hash stored in refresh_token
session token    BCrypt hash stored in session_token
OTP              purpose-bound HMAC hash stored in otp_check
```

Refresh rotation locks the token-hash row. Share-code regeneration locks the trip row, and redemption locks the share-code row.

## Data model

Main tables include:

```text
users
trips
trip_destinations
trip_activities
trip_members
trip_collaboration_requests
trip_share_codes
trip_share_code_attempts
refresh_token
session_token
otp_check
configuration
error_codes
email_contents
sms_contents
```

## Database schema ownership

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.jpa.hibernate.ddl-auto=validate
```

Flyway is the sole schema-change mechanism. Hibernate validates the schema after Flyway completes and does not add or alter columns.

Current migrations:

```text
V1  initial schema/reference data
V2  purpose-bound OTP hash storage
V3  share-code active-row constraint
V4  account-enumeration rate-limit error
V5  trip/destination calendar DATE columns
V6  standardized framework exception errors
```

The old `docker/init/init.sql` is not mounted by Docker Compose and is retained only as legacy reference material.

## Time model

- Trips and destinations: `LocalDate` / SQL `DATE`.
- Activities: local wall-clock `LocalDateTime` / SQL `DATETIME`.
- Audit, expiry and security timestamps: UTC `Instant` persisted through UTC JDBC handling.

## Deployment architecture

```text
GitHub Actions
    ↓ clean verify + security checks
Fresh MariaDB service
    ↓ Flyway + Hibernate validation
Packaged Spring Boot JAR
    ↓ tracked Render deployment
Production health endpoint
```
