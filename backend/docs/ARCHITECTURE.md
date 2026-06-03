# Backend Architecture

This document explains the backend structure of the Travelling App backend.

---

## High-Level Architecture

```mermaid
flowchart TD
    Client[Client / Swagger / Postman / Frontend] --> Security[Spring Security Filter Chain]
    Security --> TokenFilter[TokenFilter]
    TokenFilter --> Controller[Controller Layer]
    Controller --> Service[Service Layer]
    Service --> Validator[Validator Layer]
    Service --> Mapper[Mapper Layer]
    Service --> Repository[Repository Layer]
    Repository --> DB[(MariaDB)]

    Service --> Response[CompleteResponse / ResponseBody]
    Response --> Controller
    Controller --> Client
```

---

## Layer Responsibilities

### Controller Layer

The controller layer handles HTTP requests and responses.

Responsibilities:

- Receive API requests
- Bind request body, headers, path variables, and query parameters
- Call service methods
- Return standardized responses

The controller should not contain complex business logic.

---

### Security Layer

The security layer protects authenticated routes before requests reach controllers.

Responsibilities:

- Skip token validation for public URLs
- Extract and validate Bearer access tokens
- Validate `Session-Token` for protected requests
- Populate `SecurityContext` with authenticated user information
- Reject invalid or expired authentication attempts

---

### Service Layer

The service layer contains business logic.

Responsibilities:

- Authentication flow
- Token generation and validation
- Refresh token rotation/revocation
- Trip and activity business rules
- OTP flow
- Ownership checks
- Calling validators, mappers, and repositories

This is the main layer where application behaviour is coordinated.

---

### Validator Layer

The validator layer keeps validation logic separate from service logic.

Responsibilities:

- Validate required fields
- Validate date/time rules
- Validate OTP request type
- Validate trip/activity rules
- Validate business conditions before service actions continue

---

### Mapper Layer

The mapper layer converts between entities and DTOs.

Responsibilities:

- Convert entity to response DTO
- Keep response formatting outside service logic
- Avoid exposing internal entity structure directly

---

### Repository Layer

The repository layer handles database access.

Responsibilities:

- Query entities
- Save/update/delete records
- Provide ownership-based lookup methods

Example ownership-based query pattern:

```text
findByTripIdAndUser_Username(...)
findByActivityIdAndTrip_TripIdAndTrip_User_Username(...)
```

This helps ensure a user can only access their own resources.

---

## Activity Creation Flow Example

```mermaid
sequenceDiagram
    actor Client
    participant Security as TokenFilter
    participant Controller
    participant Service as ActivityService
    participant Validator as ActivityValidator
    participant Repo as Repository
    participant DB as MariaDB
    participant Mapper as ActivityMapper

    Client->>Security: POST /api/v1/trips/{tripId}/activities
    Security->>Security: Validate access token + session token
    Security->>Controller: Allow request
    Controller->>Service: createActivity(tripId, dto)
    Service->>Validator: validateCreateInput(tripId, dto)
    Validator-->>Service: normalized activity name
    Service->>Repo: find trip by tripId + authenticated username
    Repo->>DB: query trip ownership
    DB-->>Repo: trip entity
    Repo-->>Service: trip entity
    Service->>Validator: validate activity inside trip date range
    Service->>Repo: check overlapping activity in same trip
    Repo->>DB: existsByTrip_TripIdAndStartDateTimeLessThanAndEndDateTimeGreaterThan
    DB-->>Repo: overlap true/false
    Repo-->>Service: overlap result
    Service->>Repo: save activity
    Repo->>DB: insert activity
    DB-->>Repo: saved entity
    Repo-->>Service: saved entity
    Service->>Mapper: toResponseDTO(activity)
    Mapper-->>Service: ActivityResponseDTO
    Service-->>Controller: CompleteResponse
    Controller-->>Client: API response
```

---

## Why This Structure Is Useful

This architecture makes the backend easier to maintain because each layer has a clear responsibility.

Benefits:

- Easier to debug
- Easier to test
- Cleaner service classes
- Better separation of concerns
- More professional backend structure for a portfolio project
