# API Guide

This document summarizes the main API areas in the WanderMate / Travelling App backend.

---

## Base URLs

Local IntelliJ run:

```text
http://localhost:8080/The-Project
```

Docker run using host port `8082`:

```text
http://localhost:8082/The-Project
```

Swagger UI for Docker:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

The application context path is currently:

```text
/The-Project
```

---

## Standard Response Shape

The backend returns a standardized response body:

```json
{
  "code": "E000",
  "message": "Operation message",
  "flow": "TRIP",
  "body": {}
}
```

Field meaning:

| Field | Meaning |
|---|---|
| `code` | Business/application code such as `E000`, `E016`, `W001` |
| `message` | Human-readable message from the error-code table |
| `flow` | Logical module such as `LOGIN`, `TOKEN`, `TRIP`, `OTP` |
| `body` | Response payload, error description, or `null` |

---

## Auth Headers

Protected APIs require both headers:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

Refresh endpoint requires:

```text
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

---

## Public APIs

These routes are public from the access-token filter perspective:

```text
POST /api/v1/users/register
POST /api/v1/users/login
POST /api/v1/auth/refresh
POST /api/v1/users/forgot-password
POST /api/v1/users/register/verify
POST /api/v1/otp/send
POST /api/v1/otp/verify
GET  /api/v1/users/check
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

Important note:

```text
/api/v1/auth/refresh is public from access-token validation,
but still requires Refresh-Token and Session-Token headers.
```

---

## User APIs

Base path:

```text
/api/v1/users
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/register/verify` | Public | Pre-check registration details before sending OTP |
| `POST` | `/register` | Public | Register user after OTP verification |
| `POST` | `/login` | Public | Login and receive access/refresh/session tokens |
| `POST` | `/forgot-password` | Public | Reset password after OTP verification |
| `POST` | `/logout` | Protected | Revoke current session and refresh tokens |
| `GET` | `/check?userInput=` | Public | Check if username/email/phone exists |

### Register details verification

```http
POST /api/v1/users/register/verify
```

Request:

```json
{
  "username": "JustinBo123",
  "password": "Password123",
  "email": "justin@example.com",
  "phoneNumber": "0412345678",
  "dob": "01/01/2000",
  "otp": ""
}
```

### Register

```http
POST /api/v1/users/register
```

Request:

```json
{
  "username": "JustinBo123",
  "password": "Password123",
  "email": "justin@example.com",
  "phoneNumber": "0412345678",
  "dob": "01/01/2000",
  "otp": "123456"
}
```

### Login

```http
POST /api/v1/users/login
```

Request:

```json
{
  "username": "JustinBo123",
  "password": "Password123",
  "overrideMaxSession": false
}
```

Successful response body contains:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "sessionToken": "..."
}
```

If max active sessions are reached, backend returns `MAX_SESSIONS_REACHED`. Frontend can ask the user whether to continue and retry login with:

```json
{
  "username": "JustinBo123",
  "password": "Password123",
  "overrideMaxSession": true
}
```

---

## OTP APIs

Base path:

```text
/api/v1/otp
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/send` | Public | Generate and send OTP |
| `POST` | `/verify` | Public | Verify OTP |

### Send email OTP

```http
POST /api/v1/otp/send
```

Request:

```json
{
  "userName": "JustinBo123",
  "otpVerificationMethod": "EMAIL_OTP",
  "email": "justin@example.com",
  "emailEnum": "EMAIL_OTP_REGISTER"
}
```

### Send phone OTP

```json
{
  "userName": "JustinBo123",
  "otpVerificationMethod": "PHONE_NUM_OTP",
  "phoneNumber": "0412345678",
  "smsEnum": "SMS_OTP_REGISTER"
}
```

Current limitation: phone/SMS OTP is service-level prepared logic only. The current `SmsServiceImpl` is a stub and does not send real SMS.

### Verify OTP

```http
POST /api/v1/otp/verify
```

Email request:

```json
{
  "userName": "JustinBo123",
  "otp": "123456",
  "email": "justin@example.com"
}
```

Phone request:

```json
{
  "userName": "JustinBo123",
  "otp": "123456",
  "phoneNumber": "0412345678"
}
```

---

## Auth / Token APIs

Base path:

```text
/api/v1/auth
```

| Method | Path | Headers | Purpose |
|---|---|---|---|
| `POST` | `/refresh` | `Refresh-Token`, `Session-Token` | Rotate refresh token and return a new access token |

Request body can be empty:

```json
{}
```

Headers:

```text
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

Successful response body:

```json
{
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token"
}
```

---

## Trip APIs

Base path:

```text
/api/v1/trips
```

All trip APIs require:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/trips` | List authenticated user's trips |
| `GET` | `/api/v1/trips/{tripId}` | Get trip detail |
| `POST` | `/api/v1/trips` | Create trip |
| `PUT` | `/api/v1/trips/{tripId}` | Update trip |
| `DELETE` | `/api/v1/trips/{tripId}` | Delete trip |
| `GET` | `/api/v1/trips/search/cities?keyword=` | Search cities |
| `GET` | `/api/v1/trips/search/restaurants?keyword=` | Search restaurants |
| `GET` | `/api/v1/trips/search/accommodations?keyword=` | Search accommodations |
| `GET` | `/api/v1/trips/suggest/cities?keyword=` | Suggest cities |
| `GET` | `/api/v1/trips/suggest/restaurants?keyword=` | Suggest restaurants |
| `GET` | `/api/v1/trips/suggest/accommodations?keyword=` | Suggest accommodations |

Create/update request:

```json
{
  "tripName": "Japan Trip",
  "destination": "Japan",
  "startDate": "2026-07-01T09:00:00",
  "endDate": "2026-07-10T18:00:00",
  "allowOverlap": false,
  "coverImageUrl": "https://res.cloudinary.com/demo/image/upload/v123/cover.jpg",
  "coverImagePublicId": "wandermate/trip-covers/users/1/trip-cover-1-abc"
}
```

Rules:

- Trip name is unique per user.
- Trip start time must be before end time.
- Trip start date cannot be in the past.
- Trip updates must still include all existing destinations.
- Overlap with another trip returns `TRIP_OVERLAP_WARNING` unless `allowOverlap=true`.

---

## Destination APIs

Base path:

```text
/api/v1/trips/{tripId}/destinations
```

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/trips/{tripId}/destinations` | List destinations for trip |
| `GET` | `/api/v1/trips/{tripId}/destinations/{destinationId}` | Get destination detail |
| `POST` | `/api/v1/trips/{tripId}/destinations` | Create destination |
| `PUT` | `/api/v1/trips/{tripId}/destinations/{destinationId}` | Update destination |
| `DELETE` | `/api/v1/trips/{tripId}/destinations/{destinationId}` | Delete destination |

Create/update request:

```json
{
  "destinationName": "Tokyo",
  "startDate": "2026-07-01T09:00:00",
  "endDate": "2026-07-05T18:00:00",
  "destinationOrder": 1,
  "notes": "First stop",
  "allowOverlap": false
}
```

Rules:

- Destination dates must stay inside the parent trip range.
- Destination updates must still include existing activities.
- Overlap with another destination in the same trip returns `DESTINATION_OVERLAP_WARNING` unless `allowOverlap=true`.
- Deleting a destination deletes activities under that destination by cascade/database relationship.

---

## Activity APIs

Base path:

```text
/api/v1/trips/{tripId}/destinations/{destinationId}/activities
```

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities` | List activities for destination |
| `GET` | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}` | Get activity detail |
| `POST` | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities` | Create activity |
| `PUT` | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}` | Update activity |
| `DELETE` | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}` | Delete activity |

Create/update request:

```json
{
  "activityName": "Shibuya Crossing",
  "location": "Shibuya",
  "description": "Walk around and take photos",
  "startDateTime": "2026-07-01T10:00:00",
  "endDateTime": "2026-07-01T12:00:00"
}
```

Rules:

- Activity start time must be before end time.
- Activity time must stay inside destination range.
- Activity overlap is a hard error.
- There is no `allowOverlap` override for activities.

Overlap rule:

```text
newStart < existingEnd AND newEnd > existingStart
```

Back-to-back is allowed:

```text
Existing: 10:00 - 12:00
New:      12:00 - 13:00
Result:   allowed
```

Overlap is blocked:

```text
Existing: 10:00 - 12:00
New:      11:00 - 13:00
Result:   blocked
```

---

## Important Error / Warning Codes

| Code | Enum | Meaning |
|---|---|---|
| `E000` | Success enums | Operation successful |
| `E016` | `TOKEN_EXPIRE` | Access token expired |
| `E022` | `MAX_SESSIONS_REACHED` | Login session limit reached |
| `E023` | `SESSION_TOKEN_INVALID` | Session token invalid/missing |
| `E030` | `REFRESH_TOKEN_INVALID` | Refresh token invalid/reused |
| `E031` | `REFRESH_TOKEN_EXPIRED` | Refresh token expired |
| `W001` | `TRIP_OVERLAP_WARNING` | Trip overlaps existing trip |
| `W002` | `DESTINATION_OVERLAP_WARNING` | Destination overlaps existing destination |
| `E049` | `TRIP_DATE_CONFLICT_WITH_EXISTING_DESTINATION` | Trip update would exclude existing destination |
| `E050` | `DESTINATION_DATE_CONFLICT_WITH_EXISTING_ACTIVITY` | Destination update would exclude existing activity |
| `E051` | `ACTIVITY_TIME_CONFLICT_WITH_EXISTING_ACTIVITY` | Activity overlaps existing activity |
| `E060` | `OTP_METHOD_MISSING` | OTP method missing |
| `E061` | `EMAIL_ENUM_MISSING` | Email enum missing for email OTP |
| `E062` | `SMS_ENUM_MISSING` | SMS enum missing for phone OTP |

---

## Frontend Warning Handling

Trip and destination overlap warnings are intended for frontend confirmation dialogs:

```text
1. User submits request with allowOverlap=false
2. Backend returns W001 or W002
3. Frontend shows confirmation modal
4. User confirms
5. Frontend retries same request with allowOverlap=true
```

Activity overlap is not a warning. It is blocked directly.

## V3 Profile, Collaboration, and Share-Code APIs

### Current profile/settings

```text
GET    /api/v1/users/me
PATCH  /api/v1/users/me/profile
PATCH  /api/v1/users/me/settings
```

Update profile request:

```json
{
  "displayName": "Justin Bui",
  "phoneNumber": "0412345678",
  "dob": "01/01/2000",
  "profileImageUrl": "https://res.cloudinary.com/demo/image/upload/v123/avatar.jpg",
  "profileImagePublicId": "wandermate/profile-images/users/1/profile-1-abc"
}
```

Update settings request:

```json
{
  "preferredTheme": "SYSTEM"
}
```

Allowed theme values:

```text
LIGHT
DARK
SYSTEM
```

### Collaboration summary

```text
GET /api/v1/collaboration/summary
```

Response body example:

```json
{
  "pendingInvitationCount": 2,
  "pendingOwnedTripJoinRequestCount": 3,
  "totalPendingActionCount": 5,
  "tripPendingJoinRequestCounts": {
    "10": 2,
    "11": 1
  }
}
```

### Join request list endpoints

```text
GET /api/v1/trips/join-requests/owned
GET /api/v1/trips/join-requests/sent
```

`owned` returns pending join requests for trips owned by the current user. `sent` returns join requests created by the current user.

### Share-code endpoints

```text
POST /api/v1/trips/{tripId}/share-codes/regenerate
GET  /api/v1/trips/share-codes/{code}
POST /api/v1/trips/share-codes/{code}/join-requests
GET  /api/v1/trips/{tripId}/share-codes/active
```

Generate/regenerate request:

```json
{
  "defaultRole": "VIEWER"
}
```

Join by share code request:

```json
{
  "role": "VIEWER"
}
```

Allowed share-code roles:

```text
EDITOR
VIEWER
```

### Attribution response fields

Destination and activity responses can include:

```text
createdByUserId
createdByUsername
createdByDisplayName
createdByProfileImageUrl
modifiedByUserId
modifiedByUsername
modifiedByDisplayName
modifiedByProfileImageUrl
```

---

## Image Upload API

Base path:

```text
/api/v1/uploads
```

The upload endpoint is protected and uses the same auth/session headers as other logged-in APIs:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/images` | Protected | Upload a profile image or trip cover image to Cloudinary |

Request type:

```text
multipart/form-data
```

Form fields:

| Field | Type | Required | Notes |
|---|---|---:|---|
| `file` | file | Yes | Image file, max 5MB |
| `imageType` | string | Yes | `profile-images` or `trip-covers` |

Example response body:

```json
{
  "imageUrl": "https://res.cloudinary.com/demo/image/upload/v123/wandermate/profile-images/users/1/profile-1-abc.jpg",
  "publicId": "wandermate/profile-images/users/1/profile-1-abc",
  "fileName": "wandermate/profile-images/users/1/profile-1-abc",
  "imageType": "profile-images"
}
```

Important behaviour:

```text
- The backend uploads image bytes to Cloudinary.
- The backend returns Cloudinary secure URL + publicId.
- The frontend saves imageUrl + publicId in profile/trip update requests.
- The database stores URLs/public IDs only, not binary image files.
- Replacing/removing a profile image or trip cover deletes the old Cloudinary asset by publicId.
```

Allowed `imageType` values:

```text
profile-images
trip-covers
```

---

## Cloudinary Image Fields

Profile responses/requests can include:

```text
profileImageUrl
profileImagePublicId
```

Trip responses/create/update requests can include:

```text
coverImageUrl
coverImagePublicId
```

Destination/activity attribution fields use the creator/editor profile image URL for avatar display:

```text
createdByProfileImageUrl
modifiedByProfileImageUrl
```

The `publicId` fields are not displayed to users. They are stored so the backend can delete the old Cloudinary asset when a user replaces or removes an image.
