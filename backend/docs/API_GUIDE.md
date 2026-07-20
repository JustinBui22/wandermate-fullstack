# API Guide

Base path:

```text
/Wandermate
```

Local base URL:

```text
http://localhost:8080/Wandermate
```

Render base URL:

```text
https://wandermate-fullstack.onrender.com/Wandermate
```

## Auth headers

Protected endpoints use:

```http
Authorization: Bearer <accessToken>
```

Refresh/logout flows may also use:

```http
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

## Public endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/health` | Backend health check |
| POST | `/api/v1/users/register/verify` | Validate registration details before registration |
| POST | `/api/v1/users/register` | Register a user after OTP verification |
| POST | `/api/v1/users/login` | Login and receive tokens |
| POST | `/api/v1/users/forgot-password` | Reset password with OTP |
| POST | `/api/v1/otp/send` | Send email/phone OTP |
| POST | `/api/v1/otp/verify` | Verify OTP |
| POST | `/api/v1/auth/refresh` | Refresh access token |

The removed `/api/v1/users/check` endpoint must not be restored: returning a
different response for an existing account makes account enumeration easier.
Share-code preview and join endpoints are authenticated and are listed below.

For a password-reset OTP request, add:

```json
{
  "purpose": "PASSWORD_RESET"
}
```

The registration default is `REGISTRATION`. Password-reset OTP requests return
the same success envelope whether or not the supplied account/destination
matches; the backend sends mail/SMS only for a valid match.

## Protected user endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/users/logout` | Logout and revoke session |
| GET | `/api/v1/users/me` | Get current user profile |
| PATCH | `/api/v1/users/me/profile` | Update current user profile |
| PATCH | `/api/v1/users/me/settings` | Update theme/settings |

## Upload endpoint

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/uploads/images` | Multipart image upload for profile/trip cover |

Required form-data:

```text
file: image file
imageType: profile-images or trip-covers
```

The backend enforces a 5 MB limit, compares MIME and file signatures, decodes
PNG/JPEG images, and validates WebP/HEIF container signatures before upload.

## Trip endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/trips` | Create trip |
| GET | `/api/v1/trips` | Get current user's accessible trips |
| GET | `/api/v1/trips/{tripId}` | Get trip detail |
| PUT | `/api/v1/trips/{tripId}` | Update trip |
| DELETE | `/api/v1/trips/{tripId}` | Delete trip |
| GET | `/api/v1/trips/search/cities` | Search cities |
| GET | `/api/v1/trips/search/restaurants` | Search restaurants |
| GET | `/api/v1/trips/search/accommodations` | Search accommodations |
| GET | `/api/v1/trips/suggest/cities` | Suggest cities |
| GET | `/api/v1/trips/suggest/restaurants` | Suggest restaurants |
| GET | `/api/v1/trips/suggest/accommodations` | Suggest accommodations |

## Destination endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/trips/{tripId}/destinations` | Create destination |
| GET | `/api/v1/trips/{tripId}/destinations` | Get destinations for trip |
| GET | `/api/v1/trips/{tripId}/destinations/{destinationId}` | Get destination detail |
| PUT | `/api/v1/trips/{tripId}/destinations/{destinationId}` | Update destination |
| DELETE | `/api/v1/trips/{tripId}/destinations/{destinationId}` | Delete destination |

## Nested activity endpoints

Activities belong to a destination, not directly to a trip.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities` | Create activity |
| GET | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities` | Get activities under destination |
| GET | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}` | Get activity detail |
| PUT | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}` | Update activity |
| DELETE | `/api/v1/trips/{tripId}/destinations/{destinationId}/activities/{activityId}` | Delete activity |

## Collaboration endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/collaboration/summary` | Get collaboration dashboard summary |
| POST | `/api/v1/trips/{tripId}/invitations` | Invite member to trip |
| GET | `/api/v1/trips/invitations/received` | Get received invitations |
| PATCH | `/api/v1/trips/invitations/{requestId}/accept` | Accept invitation |
| PATCH | `/api/v1/trips/invitations/{requestId}/reject` | Reject invitation |
| POST | `/api/v1/trips/{tripId}/join-requests` | Send join request to trip |
| GET | `/api/v1/trips/{tripId}/join-requests` | Get trip join requests |
| GET | `/api/v1/trips/join-requests/owned` | Get join requests for trips the user owns |
| GET | `/api/v1/trips/join-requests/sent` | Get current user's sent join requests |
| PATCH | `/api/v1/trips/join-requests/{requestId}/accept` | Accept join request |
| PATCH | `/api/v1/trips/join-requests/{requestId}/reject` | Reject join request |
| GET | `/api/v1/trips/{tripId}/my-overlap-warnings` | Get overlap warnings for current user |

## Share code endpoints

All endpoints in this section require a valid access token and session token.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/trips/{tripId}/share-codes/regenerate` | Generate/regenerate active share code |
| GET | `/api/v1/trips/share-codes/{code}` | Preview share code |
| POST | `/api/v1/trips/share-codes/{code}/join-requests` | Join-request using share code |
| GET | `/api/v1/trips/{tripId}/share-codes/active` | Get active share code for trip |

## Member endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/trips/{tripId}/members` | List trip members |
| PATCH | `/api/v1/trips/{tripId}/members/{tripMemberId}/role` | Update member role |
| DELETE | `/api/v1/trips/{tripId}/members/{tripMemberId}` | Remove member |
