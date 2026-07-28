# Frontend Integration

## Base URL

The frontend reads:

```text
EXPO_PUBLIC_API_BASE_URL
```

`src/constants/env.ts` falls back to the Render base URL.

| Frontend runtime | Backend base URL example |
|---|---|
| Android emulator + IntelliJ | `http://10.0.2.2:8080/Wandermate` |
| Android emulator + Docker | `http://10.0.2.2:8082/Wandermate` |
| Physical device + local backend | `http://<computer-lan-ip>:8080/Wandermate` or `:8082` |
| Production | `https://wandermate-fullstack.onrender.com/Wandermate` |

## Authentication requests

Registration and password recovery support email OTP and phone OTP. Email is the operational delivery path:

```json
{
  "userName": "sampleuser",
  "otpVerificationMethod": "EMAIL_OTP",
  "email": "sample@example.com",
  "emailEnum": "EMAIL_OTP_REGISTER",
  "purpose": "REGISTRATION"
}
```

Password recovery uses `purpose: "PASSWORD_RESET"`. The phone option sends this request shape:

```json
{
  "userName": "sampleuser",
  "otpVerificationMethod": "PHONE_NUM_OTP",
  "phoneNumber": "+61400000000",
  "smsEnum": "SMS_OTP_REGISTER",
  "purpose": "REGISTRATION"
}
```

The phone flow is demo-only: the backend SMS service simulates success but does not send a real message because no paid SMS provider is configured. Use email OTP when demonstrating a complete working flow.

## Token storage

`tokenStore.ts` stores:

```text
accessToken
refreshToken
sessionToken
username
```

Protected requests attach:

```http
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

Refresh attaches:

```http
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

## Refresh/error behavior

- Access-token expiry triggers one refresh attempt.
- Concurrent expired requests share one refresh promise.
- The original request is retried with the new access token.
- Failed refresh or explicit invalid-session/token errors clear local authentication.
- Ordinary permission-only `403` responses do not log the user out.
- API modules return the shared backend response body's `body` value.
- Screens use `apiWarningUtils` for code/message extraction.

## Date/time payloads

- Trip `startDate` / `endDate`: `yyyy-MM-dd`.
- Destination `startDate` / `endDate`: `yyyy-MM-dd`.
- Activity `startDateTime` / `endDateTime`: local `yyyy-MM-dd'T'HH:mm:ss`.
- Audit/expiry timestamps: UTC values with `Z`.

Date-only values must be parsed from numeric components rather than `new Date("yyyy-MM-dd")`, which JavaScript treats as UTC and may display on another calendar day.

Activity values are destination-local wall-clock values and must not be automatically viewer-timezone shifted.

## Roles

```text
OWNER
EDITOR
VIEWER
```

- All three can view accessible content.
- Owner/editor can edit plan content.
- Owner alone manages trip deletion, invitations, requests, share codes and member roles.

The backend is authoritative even when the frontend hides unavailable actions.

## Image upload

```http
POST /api/v1/uploads/images
Content-Type: multipart/form-data
```

```text
file=<selected image>
imageType=profile-images | trip-covers
```

Persist both returned Cloudinary values in the subsequent profile/trip update.

## Share-code deep link

```text
wandermate://join-trip?code=<code>
```

`app/join-trip.tsx` handles the route. Preview uses `POST /api/v1/trips/share-codes/preview` because attempts update security state.

## Verification

```bash
cd frontend
npm ci
npm run typecheck
npm test
npx expo config --type public --json
npx expo export --platform web --output-dir dist
```
