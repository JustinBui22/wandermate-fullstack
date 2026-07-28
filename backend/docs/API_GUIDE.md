# API Guide

## Base URLs

```text
Direct local: http://localhost:8080/Wandermate
Docker host:  http://localhost:8082/Wandermate
Configured production: https://wandermate-fullstack.onrender.com/Wandermate
```

All paths below are relative to the base URL.

## Response format

Most application endpoints return the shared response body generated through `CompleteResponse`:

```json
{
  "code": "E000",
  "message": "Operation completed successfully",
  "flow": "TRIP",
  "body": {}
}
```

The health endpoint returns a plain map instead of the shared wrapper.

## Error response consistency

Application, validation, request parsing, Spring MVC, multipart, authentication and authorization failures use the same shared response envelope:

```json
{
  "code": "E001",
  "message": "Invalid input provided",
  "flow": "COMMON",
  "body": "Invalid request body, enum value, date or date-time format"
}
```

Common framework-level mappings include:

| Error category | HTTP status | Application error |
|---|---:|---|
| Missing/invalid parameter, header, JSON or multipart body | 400 | `INVALID_INPUT` |
| Missing or invalid authentication | 401 | existing token error |
| Authenticated but forbidden | 403 | `ACCESS_DENIED` |
| Unknown API resource | 404 | `RESOURCE_NOT_FOUND` |
| Unsupported HTTP method | 405 | `REQUEST_METHOD_NOT_SUPPORTED` |
| Uploaded file/request exceeds the configured limit | 413 | `PAYLOAD_TOO_LARGE` |
| Unsupported request content type | 415 | `MEDIA_TYPE_NOT_SUPPORTED` |
| Unexpected server failure | 500 | `INTERNAL_SERVER_ERROR` |

Validation continues to return the first useful validation message in `body`, preserving the current frontend parsing contract. Internal exception messages and stack traces are logged server-side and are not returned to the client.

## Authentication headers

Protected request:

```http
Authorization: Bearer <access-token>
Session-Token: <session-token>
```

Refresh request:

```http
Refresh-Token: <refresh-token>
Session-Token: <session-token>
```

The access JWT identifies the username/session ID. `TokenFilter` also checks the presented session token against the stored BCrypt hash before populating Spring Security's context.

## Public routes

Public access is defined in code by `PublicEndpointMatcher`. The matcher is shared by `SecurityConfig` and `TokenFilter`, so both layers use the same HTTP-method-specific policy.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/health` | Health check |
| POST | `/api/v1/users/register/verify` | Validate registration data before OTP/final registration |
| POST | `/api/v1/users/register` | Register after OTP verification |
| POST | `/api/v1/users/login` | Login and receive three tokens |
| POST | `/api/v1/users/forgot-password` | Reset password after OTP verification |
| POST | `/api/v1/otp/send` | Send OTP |
| POST | `/api/v1/otp/verify` | Verify/consume OTP |
| POST | `/api/v1/auth/refresh` | Rotate refresh token and issue a new access token |
| GET | `/swagger-ui/**` | Local Swagger UI assets |
| GET | `/swagger-ui.html` | Local Swagger entry point |
| GET | `/v3/api-docs/**` | Local OpenAPI JSON/YAML |
| OPTIONS | `/**` | Browser CORS preflight |

A path is not made public for every HTTP method. For example, `POST /api/v1/users/login` is public, while another method on the same path is not automatically permitted.

## User and authentication endpoints

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| POST | `/api/v1/users/register/verify` | No | Validate username/email/phone/password/date fields |
| POST | `/api/v1/otp/send` | No | Send email OTP or invoke the demo-only simulated phone-OTP path |
| POST | `/api/v1/otp/verify` | No | Verify OTP directly |
| POST | `/api/v1/users/register` | No | Verify OTP and create account |
| POST | `/api/v1/users/login` | No | Login by username/email/phone |
| POST | `/api/v1/auth/refresh` | Refresh/session headers | Rotate refresh token |
| POST | `/api/v1/users/forgot-password` | No | Verify OTP, change password, revoke active sessions/tokens |
| POST | `/api/v1/users/logout` | Yes | Revoke current session and its active refresh tokens |
| GET | `/api/v1/users/check` | Yes | Rate-limited account lookup returning only `{ "exists": true|false }` |
| GET | `/api/v1/users/me` | Yes | Current profile |
| PATCH | `/api/v1/users/me/profile` | Yes | Update display name, phone, DOB and profile image reference |
| PATCH | `/api/v1/users/me/settings` | Yes | Update preferred theme |


### Account-enumeration behaviour

`GET /api/v1/users/check` is not public. It requires the normal access-token and session-token headers, is limited per authenticated account, and returns the same `E000` response envelope whether the supplied username/email/phone exists or not. The body contains only an `exists` boolean; it never returns the matched canonical username, active-status details, or authentication-provider details.

The default authenticated lookup limit is 20 requests per 60 seconds and can be configured through `ACCOUNT_LOOKUP_MAX_REQUESTS` and `ACCOUNT_LOOKUP_WINDOW_SECONDS`. Public registration-verification and OTP-send requests share a default limit of 10 requests per 60 seconds per remote address, configured through `PUBLIC_ACCOUNT_MAX_REQUESTS` and `PUBLIC_ACCOUNT_WINDOW_SECONDS`. The limiter is in-memory and per application instance, which is sufficient for the current single-instance deployment but should be replaced with a shared Redis/database-backed limiter before horizontal scaling.

Registration verification intentionally still returns field-specific username/email/phone availability errors so the registration form can guide the user. This is an accepted UX/security trade-off. Login uses the same invalid-credentials response for missing users and incorrect passwords, including a dummy BCrypt comparison for the missing-user path. Password-reset OTP requests return the same success response and send nothing when the account or supplied email does not match. Exact latency equality is not guaranteed because a valid request still performs synchronous email delivery; eliminating that residual timing signal would require queued asynchronous delivery.

### Login request

```json
{
  "username": "user-or-email-or-phone",
  "password": "Password1!",
  "overrideMaxSession": false
}
```

Successful body:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "sessionToken": "..."
}
```

### Send OTP request

Email example:

```json
{
  "userName": "sampleuser",
  "email": "sample@example.com",
  "otpVerificationMethod": "EMAIL_OTP",
  "emailEnum": "EMAIL_OTP_REGISTER",
  "purpose": "REGISTRATION"
}
```

`purpose` defaults to `REGISTRATION`; password recovery uses `PASSWORD_RESET`. Email OTP is the operational end-to-end path. The API also accepts `PHONE_NUM_OTP`, but the current `SmsServiceImpl` only simulates a successful send and does not call a real SMS gateway because a paid provider is not configured.

Demo-only phone request shape:

```json
{
  "userName": "sampleuser",
  "phoneNumber": "+61400000000",
  "otpVerificationMethod": "PHONE_NUM_OTP",
  "smsEnum": "SMS_OTP_REGISTER",
  "purpose": "REGISTRATION"
}
```

Do not use the phone path as evidence of real delivery until `SmsServiceImpl` is integrated with an SMS provider.

### Register request

```json
{
  "username": "sampleuser",
  "password": "Password1!",
  "email": "sample@example.com",
  "phoneNumber": null,
  "dob": "20/07/1999",
  "referredCode": null,
  "otp": "123456"
}
```

### Refresh request

```http
POST /api/v1/auth/refresh
Refresh-Token: <current-refresh-token>
Session-Token: <current-session-token>
```

## Upload endpoint

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| POST | `/api/v1/uploads/images` | Yes | Upload a profile image or trip cover |

Multipart fields:

```text
file=<image>
imageType=profile-images | trip-covers
```

The backend returns an image URL and Cloudinary public ID. It does not currently expose a standalone unused-image DELETE endpoint.

## Trip endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/trips` | Create trip and owner membership |
| GET | `/api/v1/trips` | List accessible trips |
| GET | `/api/v1/trips/{tripId}` | Get accessible trip detail |
| PUT | `/api/v1/trips/{tripId}` | Update as owner/editor |
| DELETE | `/api/v1/trips/{tripId}` | Delete as owner |
| GET | `/api/v1/trips/search/cities?keyword=...` | City contains search |
| GET | `/api/v1/trips/search/restaurants?keyword=...` | Restaurant contains search |
| GET | `/api/v1/trips/search/accommodations?keyword=...` | Accommodation contains search |
| GET | `/api/v1/trips/suggest/cities?keyword=...` | Prefix suggestions |
| GET | `/api/v1/trips/suggest/restaurants?keyword=...` | Prefix suggestions |
| GET | `/api/v1/trips/suggest/accommodations?keyword=...` | Prefix suggestions |

Trip-list query parameters:

```text
ownership=ALL | CREATED | JOINED
status=ALL | PLANNING | ONGOING | FINISHED
sort=MODIFIED_DATE_DESC | MODIFIED_DATE_ASC | CREATED_DATE_DESC | CREATED_DATE_ASC | NAME_ASC | NAME_DESC
```

Create/update example:

```json
{
  "tripName": "Japan 2027",
  "destination": "Tokyo",
  "startDate": "2027-04-01",
  "endDate": "2027-04-10",
  "allowOverlap": false,
  "tripStatus": "PLANNING",
  "coverImageUrl": null,
  "coverImagePublicId": null
}
```

Trip and destination boundaries are calendar-only ISO dates (`yyyy-MM-dd`). They must not be converted through the device timezone. Same-day ranges are valid. Audit and expiry timestamps are RFC 3339 UTC instants such as `2027-04-01T05:30:00Z`.

## Destination endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/trips/{tripId}/destinations` | Create as owner/editor |
| GET | `/api/v1/trips/{tripId}/destinations` | List for any member |
| GET | `/api/v1/trips/{tripId}/destinations/{destinationId}` | Get detail |
| PUT | `/api/v1/trips/{tripId}/destinations/{destinationId}` | Update as owner/editor |
| DELETE | `/api/v1/trips/{tripId}/destinations/{destinationId}` | Delete as owner/editor |

```json
{
  "destinationName": "Kyoto",
  "startDate": "2027-04-04",
  "endDate": "2027-04-07",
  "destinationOrder": 2,
  "notes": "Train from Tokyo",
  "allowOverlap": false
}
```

## Activity endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities` | Create as owner/editor |
| GET | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities` | List for any member |
| GET | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}` | Get detail |
| PUT | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}` | Update as owner/editor |
| DELETE | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}` | Delete as owner/editor |

```json
{
  "activityName": "Fushimi Inari",
  "location": "Kyoto",
  "description": "Morning visit",
  "startDateTime": "2027-04-05T08:00:00",
  "endDateTime": "2027-04-05T11:00:00"
}
```

Activity schedule fields intentionally remain ISO local date-times without an offset. They represent the local wall-clock time at the destination and must not be automatically shifted through the viewer's device timezone.

## Collaboration request endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/collaboration/summary` | Pending collaboration counts |
| POST | `/api/v1/trips/{tripId}/invitations` | Owner invites username as editor/viewer |
| GET | `/api/v1/trips/invitations/received` | Current user's pending invitations |
| PATCH | `/api/v1/trips/invitations/{requestId}/accept` | Accept invitation |
| PATCH | `/api/v1/trips/invitations/{requestId}/reject` | Reject invitation |
| POST | `/api/v1/trips/{tripId}/join-requests` | Request to join known trip ID |
| GET | `/api/v1/trips/{tripId}/join-requests` | Owner's pending requests for one trip |
| GET | `/api/v1/trips/join-requests/owned` | Pending requests across owned trips |
| GET | `/api/v1/trips/join-requests/sent` | Current user's sent pending requests |
| PATCH | `/api/v1/trips/join-requests/{requestId}/accept` | Owner accepts |
| PATCH | `/api/v1/trips/join-requests/{requestId}/reject` | Owner rejects |
| GET | `/api/v1/trips/{tripId}/my-overlap-warnings` | Member-specific trip overlap warnings |

Invitation body:

```json
{
  "username": "anotheruser",
  "role": "EDITOR"
}
```

Direct join-request body:

```json
{
  "role": "VIEWER"
}
```

## Share-code endpoints

All current share-code endpoints require valid access and session tokens.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/trips/{tripId}/share-codes/regenerate` | Owner generates/replaces current code |
| POST | `/api/v1/trips/share-codes/preview` | Authenticated preview and attempt accounting |
| POST | `/api/v1/trips/share-codes/{code}/join-requests` | Submit join request using code |
| GET | `/api/v1/trips/{tripId}/share-codes/active` | Owner retrieves active code |

Optional generate body:

```json
{
  "defaultRole": "VIEWER"
}
```

Preview body:

```json
{
  "code": "WM-ABCDEFGHJKLM"
}
```

## Member endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/trips/{tripId}/members` | List members; any trip member |
| PATCH | `/api/v1/trips/{tripId}/members/{tripMemberId}/role` | Owner changes editor/viewer role |
| DELETE | `/api/v1/trips/{tripId}/members/{tripMemberId}` | Owner removes non-owner member |

Role body:

```json
{
  "role": "EDITOR"
}
```
