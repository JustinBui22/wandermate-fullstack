# API Guide

This guide summarizes the current WanderMate backend API.

## Base URLs

Local IntelliJ run:

```text
http://localhost:8080/The-Project
```

Docker run using host port `8082`:

```text
http://localhost:8082/The-Project
```

Production Render backend:

```text
https://wandermate-fullstack.onrender.com/The-Project
```

The Spring context path is:

```text
/The-Project
```

## Standard Response Shape

Most service/controller responses use a consistent response wrapper:

```json
{
  "code": "E000",
  "message": "Trip created successfully",
  "flow": "TRIP",
  "body": {}
}
```

| Field | Meaning |
|---|---|
| `code` | Business response code such as `E000`, `E016`, `E032`, `W001` |
| `message` | User-facing message, normally loaded from `error_codes` |
| `flow` | Logical flow/module such as `LOGIN`, `TOKEN`, `TRIP`, `DESTINATION` |
| `body` | Response data, warning details, or `null` |

## Authentication Headers

Protected API calls require:

```http
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

Refresh calls require:

```http
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

`/api/v1/auth/refresh` is public from access-token validation, but it is not anonymous: it still needs the refresh token and session token headers.

## Public Endpoints

These routes are treated as public by the security filter:

```text
POST /api/v1/users/register
POST /api/v1/users/register/verify
POST /api/v1/users/login
POST /api/v1/users/forgot-password
POST /api/v1/auth/refresh
POST /api/v1/otp/send
POST /api/v1/otp/verify
GET  /api/v1/users/check
GET  /api/v1/health
GET  /swagger-ui/**          # local/dev only
GET  /v3/api-docs/**         # local/dev only
```

Swagger/OpenAPI is disabled in the production profile.

## User APIs

Base path:

```text
/api/v1/users
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/register/verify` | Public | Validate registration details before OTP/register |
| `POST` | `/register` | Public | Register user after OTP verification |
| `POST` | `/login` | Public | Login and return access/refresh/session tokens |
| `POST` | `/forgot-password` | Public | Reset password after OTP flow |
| `POST` | `/logout` | Protected | Revoke current session and refresh tokens |
| `GET` | `/check?userInput=` | Public | Check username/email/phone availability |
| `GET` | `/me` | Protected | Get current user profile/settings |
| `PATCH` | `/me/profile` | Protected | Update display name, phone, DOB, profile image metadata |
| `PATCH` | `/me/settings` | Protected | Update user settings such as preferred theme |

### Login

```http
POST /api/v1/users/login
```

Request:

```json
{
  "username": "owner_user",
  "password": "Password123!",
  "overrideMaxSession": false
}
```

Successful response body includes:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "sessionToken": "..."
}
```

If the account reaches the configured session limit, the backend returns `MAX_SESSIONS_REACHED`. The frontend can ask the user for confirmation and retry with:

```json
{
  "username": "owner_user",
  "password": "Password123!",
  "overrideMaxSession": true
}
```

## OTP APIs

Base path:

```text
/api/v1/otp
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/send` | Public | Generate and send OTP |
| `POST` | `/verify` | Public | Verify OTP |

OTP behaviour includes retry limits, expiry time, restricted time after too many failures, and method-specific validation for email/phone matching.

## Token APIs

Base path:

```text
/api/v1/auth
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/refresh` | Refresh/session headers | Rotate refresh token and return new access token |

The refresh flow uses hashed refresh tokens in the database and detects refresh-token reuse.

## Trip APIs

Base path:

```text
/api/v1/trips
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/` | Protected | Create trip |
| `GET` | `/` | Protected | List trips current user owns or collaborates on |
| `GET` | `/{tripId}` | Protected | Get trip detail if user can view |
| `PUT` | `/{tripId}` | Owner/Editor | Update trip if user can edit |
| `DELETE` | `/{tripId}` | Owner | Delete trip |
| `GET` | `/search/cities` | Protected | Search city data |
| `GET` | `/search/restaurants` | Protected | Search restaurant data |
| `GET` | `/search/accommodations` | Protected | Search accommodation data |
| `GET` | `/suggest/cities` | Protected | Suggest city data |
| `GET` | `/suggest/restaurants` | Protected | Suggest restaurant data |
| `GET` | `/suggest/accommodations` | Protected | Suggest accommodation data |

Trip status is date-driven:

```text
end date before now              -> FINISHED
start date <= now <= end date    -> ONGOING
start date after now             -> PLANNING
```

Updating a finished trip so that the end date is in the future should recalculate the status back to `ONGOING` or `PLANNING`, depending on the date range.

## Destination APIs

Base path:

```text
/api/v1/trips/{tripId}/destinations
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/` | Owner/Editor | Create destination |
| `GET` | `/` | Viewer+ | List trip destinations |
| `GET` | `/{destinationId}` | Viewer+ | Get destination detail |
| `PUT` | `/{destinationId}` | Owner/Editor | Update destination |
| `DELETE` | `/{destinationId}` | Owner/Editor | Delete destination |

Destination dates must stay inside the trip date range. Updating a destination cannot exclude existing activities inside it.

## Activity APIs

Base path:

```text
/api/v1/trips/{tripId}/destinations/{destinationId}/activities
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/` | Owner/Editor | Create activity |
| `GET` | `/` | Viewer+ | List activities for a destination |
| `GET` | `/{activityId}` | Viewer+ | Get activity detail |
| `PUT` | `/{activityId}` | Owner/Editor | Update activity |
| `DELETE` | `/{activityId}` | Owner/Editor | Delete activity |

Activity times must stay inside the destination date range. Activity overlaps are treated as hard conflicts.

## Collaboration APIs

Base path:

```text
/api/v1/trips
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/{tripId}/invitations` | Owner | Invite a user to a trip |
| `GET` | `/invitations/received` | Protected | List received invitations |
| `PATCH` | `/invitations/{requestId}/accept` | Protected | Accept invitation |
| `PATCH` | `/invitations/{requestId}/reject` | Protected | Reject invitation |
| `POST` | `/{tripId}/join-requests` | Protected | Request to join a trip |
| `GET` | `/{tripId}/join-requests` | Owner | List join requests for a trip |
| `GET` | `/join-requests/owned` | Protected | List join requests for owned trips |
| `GET` | `/join-requests/sent` | Protected | List join requests sent by current user |
| `PATCH` | `/join-requests/{requestId}/accept` | Owner | Accept join request |
| `PATCH` | `/join-requests/{requestId}/reject` | Owner | Reject join request |
| `GET` | `/{tripId}/my-overlap-warnings` | Protected | Get private overlap warnings for current user |

Roles:

```text
OWNER  -> full trip/collaboration management
EDITOR -> can update trip planning content
VIEWER -> read-only access
```

## Member APIs

Base path:

```text
/api/v1/trips/{tripId}/members
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `GET` | `/` | Viewer+ | List trip members |
| `PATCH` | `/{tripMemberId}/role` | Owner | Change member role |
| `DELETE` | `/{tripMemberId}` | Owner | Remove member |

Owners cannot be removed and owner role cannot be assigned manually from member management.

## Share-Code APIs

Base path:

```text
/api/v1/trips
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/{tripId}/share-codes/regenerate` | Owner | Create/revoke/regenerate active share code |
| `GET` | `/share-codes/{code}` | Protected | Preview trip from share code |
| `POST` | `/share-codes/{code}/join-requests` | Protected | Send join request using share code |
| `GET` | `/{tripId}/share-codes/active` | Owner | Get active share code for a trip |

Share-code statuses:

```text
ACTIVE
USED
EXPIRED
REVOKED
```

## Upload APIs

Base path:

```text
/api/v1/uploads
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/images` | Protected | Upload profile image or trip cover image to Cloudinary |

Multipart fields:

```text
file=<image file>
imageType=profile-images | trip-covers
```

Response body includes image URL and Cloudinary public ID. The frontend stores those values in profile/trip update APIs.

## Collaboration Summary API

Base path:

```text
/api/v1/collaboration
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `GET` | `/summary` | Protected | Return pending invitation/join-request counts for badges |

## Health API

```http
GET /api/v1/health
```

Used for Render/Docker/local deployment verification.
