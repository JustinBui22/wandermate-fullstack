# Frontend Integration

## Base URL

The frontend reads:

```text
EXPO_PUBLIC_API_BASE_URL
```

`src/constants/env.ts` falls back to the configured Render URL.

| Frontend runtime | Backend base URL example |
|---|---|
| Android emulator + IntelliJ | `http://10.0.2.2:8080/Wandermate` |
| Android emulator + Docker | `http://10.0.2.2:8082/Wandermate` |
| Physical device + local backend | `http://<computer-lan-ip>:8080/Wandermate` or `:8082` |
| Production | `https://wandermate-fullstack.onrender.com/Wandermate` |

## Token storage

`tokenStore.ts` stores these values in Expo SecureStore:

```text
accessToken
refreshToken
sessionToken
username
```

## Axios headers

`axiosClient.ts` automatically adds:

```http
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

`refreshApi.ts` adds:

```http
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

## Refresh/error behavior

- Access-token expiry (`E016` or matching expiry text) triggers refresh once.
- Concurrent expired requests share one refresh promise.
- The original request is retried with the new access token.
- Failed refresh clears the local session.
- `401`, explicit invalid-session `E023`, or token-verification `E015` clears local authentication.
- A normal permission-only `403` remains a resource authorization error and does not log the user out.

## Response body

API modules normally return `response.data.body` from the backend's shared wrapper. Screens should use `apiWarningUtils` for consistent error code/message extraction.

## Date/time payloads

The backend uses two scheduling formats:

- trip `startDate` / `endDate`: ISO calendar dates (`yyyy-MM-dd`);
- destination `startDate` / `endDate`: ISO calendar dates (`yyyy-MM-dd`);
- activity `startDateTime` / `endDateTime`: ISO local date-times without an offset (`yyyy-MM-dd'T'HH:mm:ss`).

Calendar-only dates must be parsed and formatted from their numeric year/month/day components so JavaScript does not reinterpret them as UTC and shift the displayed day. Activity values are destination-local wall-clock values and must not be automatically timezone-shifted.

Example:

```text
2027-04-05T08:00:00
```

Frontend date utilities should not send display-formatted values.

## Roles

Backend roles:

```text
OWNER
EDITOR
VIEWER
```

Frontend helpers map them to:

- view access for all three;
- editing for owner/editor;
- trip/member administration for owner only.

Screens may fetch the trip's `currentUserRole` and/or member list. The backend remains authoritative.

## Image upload

```http
POST /api/v1/uploads/images
Content-Type: multipart/form-data
```

```text
file=<selected image>
imageType=profile-images | trip-covers
```

Persist both returned values in the subsequent request:

```json
{
  "coverImageUrl": "...",
  "coverImagePublicId": "..."
}
```

The current backend does not provide an endpoint for deleting an uploaded image that the user abandons before saving.

## Collaboration deep link

The clean seed uses:

```text
wandermate://join-trip?code=
```

The Expo app scheme is `wandermate`, and `app/join-trip.tsx` handles the join path.

## Development checks

```bash
cd frontend
npm run typecheck
npm test
```

After changing frontend `.env`, restart Metro/Expo with cache clearing.
