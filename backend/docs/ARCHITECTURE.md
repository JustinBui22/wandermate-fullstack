# Backend Architecture

## Request flow

```text
HTTP request
  ↓
Spring Security / TokenFilter
  ↓
Controller interface mapping
  ↓
Controller implementation
  ↓
Service interface / implementation
  ↓
Validator + mapper + repository
  ↓
MariaDB / Cloudinary / email provider
```

## Package responsibilities

| Package | Responsibility |
|---|---|
| `config` | Security, mail, Cloudinary, OpenAPI and scheduling beans |
| `controller` | HTTP contracts and mappings |
| `controller.impl` | Converts service `CompleteResponse` values to `ResponseEntity` |
| `dto.request` | Validated incoming payloads |
| `dto.response` | API response models |
| `entity` | JPA entities and relationships |
| `repository` | Spring Data JPA access and custom queries |
| `service` | Business-service contracts |
| `service.impl` | Orchestration, authorization and transactions |
| `validator` | Input and business-rule checks |
| `mapper` | Entity/DTO conversion |
| `security` | JWT/session filter, entry point, authenticated principal and secret providers |
| `response_template` | Shared code/message/flow/body response construction |
| `exception_handler` | Structured business-exception handling |
| `util` | Configuration lookup, user lookup and conversion helpers |

## Domain model

```text
User
 ├─ SessionToken
 ├─ RefreshToken
 ├─ OtpCheck
 └─ TripMember ── Trip
                   ├─ Destination ── Activity
                   ├─ CollaborationRequest
                   └─ ShareCode
```

The clean Docker schema contains 18 tables:

```text
configuration, error_codes, email_contents, sms_contents,
accommodations, cities, restaurants, users, trips,
trip_destinations, destination_activities, trip_members,
trip_collaboration_requests, trip_share_code_attempts,
trip_share_codes, otp_check, refresh_token, session_token
```


## Controller/service split

Each API group has an interface containing Spring mapping annotations and a concrete `@RestController` implementation. Services follow the same interface/implementation split. This keeps endpoint contracts visible while allowing unit tests to mock service contracts.

## Response and exception design

Services generally return `CompleteResponse<Object>`. `CompleteResponse` loads configured error/message metadata and creates the shared response body. Business rules throw `BusinessException(ErrorCodeEnum, flow)`, which `GlobalExceptionHandler` converts to the configured HTTP status and body.

## Authentication architecture

- `PublicEndpointMatcher` owns the HTTP-method-specific public-route policy.
- `SecurityConfig` and `TokenFilter` both use the same matcher, preventing their public/protected decisions from drifting apart.
- `TokenFilter` skips matching public requests, validates a Bearer JWT and session token for protected requests, and inserts `AuthenticatedUser` into the SecurityContext.
- `SecurityConfig` applies the configured CORS origin policy and allows the frontend authentication headers.
- `AuthenticatedUserProvider` gives services the current username/session ID.
- `TripAccessService` centralizes view/edit/owner checks.
- Access JWTs are signed with an environment-provided HS512 key.
- Refresh tokens are random UUID strings; only an HMAC-SHA256 hash is stored.
- Session tokens are random UUID strings; only a BCrypt hash is stored.

## Transaction boundaries

Normal write services use `@Transactional` where multiple records must remain consistent. Three security-event services use `REQUIRES_NEW` so counters/revocation can commit even if the caller then returns a failure:

- failed OTP verification accounting;
- refresh-token reuse revocation;
- invalid share-code attempt accounting.

The OTP failure and share-code attempt services use pessimistic entity locking while updating counters. The main token/share-code lookup paths do not consistently use pessimistic locks in this version; concurrency hardening remains roadmap work.

## Authorization model

`TripAccessService` implements:

```text
Can view: OWNER, EDITOR, VIEWER
Can edit: OWNER, EDITOR
Owner only: OWNER
```

Trip creation also creates the owner membership. Nested destination/activity services call the centralized access checks before querying or changing child records.

## Persistence strategy

The current application uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

The Docker seed creates the initial schema/reference rows on a fresh MariaDB volume. There are no versioned Flyway/Liquibase migrations in the current project.

## External services

- Cloudinary stores profile/trip images.
- Gmail OAuth2 settings can be read from environment variables with database/default fallback.
- `GoogleOAuthHelper` schedules access-token refresh using its own single-thread scheduled executor.
- Render deployment is triggered through a GitHub Actions deploy-hook secret after backend tests pass.
