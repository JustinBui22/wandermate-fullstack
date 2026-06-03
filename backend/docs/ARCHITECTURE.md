# Backend Architecture

This document explains the backend structure of the WanderMate / Travelling App backend.

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

## Core Domain Model

```text
User
  └── Trip
        └── Destination
              └── Activity
```

The backend is structured around nested itinerary data.

A user owns trips. Each trip can contain multiple destinations. Each destination can contain multiple activities.

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
- Trip, destination, and activity business rules
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
- Validate trip, destination, and activity rules
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

Example ownership-based query patterns:

```text
findByTripIdAndUser_Username(...)
findByDestinationIdAndTrip_TripIdAndTrip_User_Username(...)
findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(...)
```

This helps ensure a user can only access their own resources.

---

## Trip Creation Flow

```mermaid
sequenceDiagram
    actor Client
    participant Security as TokenFilter
    participant Controller
    participant Service as TripService
    participant Validator as TripValidator
    participant Repo as Repository
    participant DB as MariaDB
    participant Mapper as TripMapper

    Client->>Security: POST /api/v1/trips
    Security->>Security: Validate access token + session token
    Security->>Controller: Allow request
    Controller->>Service: createTrip(dto)
    Service->>Validator: validateCreateInput(dto)
    Validator-->>Service: normalized trip name
    Service->>Repo: find authenticated user
    Repo->>DB: query user
    DB-->>Repo: user entity
    Repo-->>Service: user entity
    Service->>Repo: check duplicate trip name for user
    Service->>Repo: check trip overlap for user
    alt Overlap exists and allowOverlap is false
        Service-->>Controller: TRIP_OVERLAP_WARNING
        Controller-->>Client: Warning response
    else No overlap or allowOverlap is true
        Service->>Repo: save trip
        Repo->>DB: insert trip
        DB-->>Repo: saved trip
        Repo-->>Service: saved trip
        Service->>Mapper: toResponseDTO(trip)
        Mapper-->>Service: TripResponseDTO
        Service-->>Controller: CompleteResponse
        Controller-->>Client: API response
    end
```

---

## Destination Creation Flow

```mermaid
sequenceDiagram
    actor Client
    participant Security as TokenFilter
    participant Controller
    participant Service as DestinationService
    participant Validator as DestinationValidator
    participant Repo as Repository
    participant DB as MariaDB
    participant Mapper as DestinationMapper

    Client->>Security: POST /api/v1/trips/{tripId}/destinations
    Security->>Security: Validate access token + session token
    Security->>Controller: Allow request
    Controller->>Service: createDestination(tripId, dto)
    Service->>Repo: find trip by tripId + authenticated username
    Repo->>DB: query trip ownership
    DB-->>Repo: trip entity
    Repo-->>Service: trip entity
    Service->>Validator: validate destination date inside trip date range
    Service->>Repo: check destination overlap inside same trip
    alt Overlap exists and allowOverlap is false
        Service-->>Controller: DESTINATION_OVERLAP_WARNING
        Controller-->>Client: Warning response
    else No overlap or allowOverlap is true
        Service->>Repo: save destination
        Repo->>DB: insert destination
        Service->>Mapper: toResponseDTO(destination)
        Service-->>Controller: CompleteResponse
        Controller-->>Client: API response
    end
```

---

## Activity Creation Flow

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

    Client->>Security: POST /api/v1/trips/{tripId}/destinations/{destinationId}/activities
    Security->>Security: Validate access token + session token
    Security->>Controller: Allow request
    Controller->>Service: createActivity(tripId, destinationId, dto)
    Service->>Validator: validateCreateInput(destinationId, dto)
    Validator-->>Service: normalized activity name
    Service->>Repo: find destination by destinationId + tripId + authenticated username
    Repo->>DB: query destination ownership
    DB-->>Repo: destination entity
    Repo-->>Service: destination entity
    Service->>Validator: validate activity inside destination date range
    Service->>Repo: check overlapping activity in same trip
    Repo->>DB: existsByDestination_Trip_TripIdAndStartDateTimeLessThanAndEndDateTimeGreaterThan
    DB-->>Repo: overlap true/false
    Repo-->>Service: overlap result
    alt Overlap exists
        Service-->>Controller: ACTIVITY_OVERLAP_ERROR
        Controller-->>Client: Error response
    else No overlap
        Service->>Repo: save activity
        Repo->>DB: insert activity
        DB-->>Repo: saved entity
        Repo-->>Service: saved entity
        Service->>Mapper: toResponseDTO(activity)
        Mapper-->>Service: ActivityResponseDTO
        Service-->>Controller: CompleteResponse
        Controller-->>Client: API response
    end
```

---

## Important Validation Rules

### Trip Rules

- User cannot have duplicate trip names.
- Trip overlap is a soft warning.
- Trip update cannot shrink the trip date range so that existing destinations fall outside it.
- Deleting a trip deletes related destinations and activities.

### Destination Rules

- Destination must belong to the authenticated user's trip.
- Destination date range must stay inside the trip date range.
- Destination overlap inside the same trip is a soft warning.
- Deleting a destination deletes related activities.

### Activity Rules

- Activity must belong to a destination.
- Activity time must stay inside the destination date range.
- Activity overlap is a hard error.
- Back-to-back activities are allowed.

---

## Why This Structure Is Useful

This architecture makes the backend easier to maintain because each layer has a clear responsibility.

Benefits:

- Easier to debug
- Easier to test
- Cleaner service classes
- Better separation of concerns
- More professional backend structure for a portfolio project
