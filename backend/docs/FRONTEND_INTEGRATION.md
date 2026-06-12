# Frontend Integration Guide

This document explains how the Expo React Native frontend connects to the Spring Boot backend.

---

## Frontend Stack

| Area | Technology |
|---|---|
| App framework | Expo React Native |
| Language | TypeScript |
| Routing | Expo Router |
| HTTP client | Axios |
| Auth state | Zustand |
| Token storage | Expo SecureStore |
| Validation/forms | React Hook Form, Zod |

---

## Backend URL Setup

The frontend backend URL is currently configured in:

```text
frontend/src/constants/env.ts
```

Current local Android emulator setup:

```ts
export const API_BASE_URL = "http://10.0.2.2:8080/The-Project";
```

Use this when:

```text
Backend runs from IntelliJ/local Maven on host port 8080
Frontend runs on Android emulator
```

For Android emulator connecting to Docker backend:

```ts
export const API_BASE_URL = "http://10.0.2.2:8082/The-Project";
```

For browser/Postman on host machine:

```text
http://localhost:8082/The-Project
```

Important:

```text
Android emulator uses 10.0.2.2 to reach the host machine.
localhost inside the emulator means the emulator itself.
```

---

## Auth Token Storage

After login, frontend stores:

```text
accessToken
refreshToken
sessionToken
```

in Expo SecureStore.

Storage file:

```text
frontend/src/stores/tokenStore.ts
```

---

## Axios Request Interceptor

The Axios client is configured in:

```text
frontend/src/api/axiosClient.ts
```

For every request, it reads tokens from SecureStore and attaches:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

This matches the backend `TokenFilter` requirements.

---

## Access Token Refresh Flow

If the backend returns access-token-expired behaviour, the Axios response interceptor calls:

```text
POST /api/v1/auth/refresh
```

with headers:

```text
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

Refresh implementation:

```text
frontend/src/refreshApi.ts
```

The frontend saves the new access token and refresh token after a successful refresh.

---

## Logout Flow

Frontend logout calls:

```text
POST /api/v1/users/logout
```

Then it clears local tokens from SecureStore whether or not the API call fails.

This is good UX because the user is logged out locally even if the backend call fails due to network/session expiry.

---

## OTP Integration Status

Email OTP:

```text
Supported as the real working OTP path when backend email config is valid.
```

Phone/SMS OTP:

```text
Types and backend service branch exist, but real SMS provider is not enabled yet.
```

Frontend should not promise real SMS delivery until the backend integrates a provider.

---

## Trip/Destination Warning Flow

The backend uses soft warning codes for trip/destination overlaps:

```text
W001 TRIP_OVERLAP_WARNING
W002 DESTINATION_OVERLAP_WARNING
```

Frontend expected flow:

```text
1. User submits trip/destination with allowOverlap=false
2. Backend returns W001 or W002
3. Frontend shows confirmation popup
4. User confirms
5. Frontend retries the same request with allowOverlap=true
```

Utility file:

```text
frontend/src/utils/apiWarningUtils.ts
```

Activity overlap is not a warning. It is a hard error.

---

## Frontend API Modules

| File | Purpose |
|---|---|
| `src/api/authApi.ts` | Login, logout, register, OTP send, forgot password |
| `src/api/tripApi.ts` | Trip CRUD and search/suggest API calls |
| `src/api/destinationApi.ts` | Destination CRUD |
| `src/api/activityApi.ts` | Activity CRUD |
| `src/api/axiosClient.ts` | Shared Axios client and token refresh handling |
| `src/refreshApi.ts` | Refresh token call without interceptor loop |

---

## Date/Time Format

Backend expects Java `LocalDateTime` format:

```text
YYYY-MM-DDTHH:mm:ss
```

Example:

```text
2026-07-01T09:00:00
```

Frontend helper:

```text
frontend/src/utils/dateTimePickerUtils.ts
```

---

## Common Local URL Cases

| Scenario | Backend URL |
|---|---|
| Android emulator → IntelliJ backend | `http://10.0.2.2:8080/The-Project` |
| Android emulator → Docker backend | `http://10.0.2.2:8082/The-Project` |
| Browser/Postman → Docker backend | `http://localhost:8082/The-Project` |
| Browser/Postman → IntelliJ backend | `http://localhost:8080/The-Project` |

---

## Recommended Future Improvement

Instead of manually editing `env.ts`, move to Expo environment variables later, for example:

```text
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/The-Project
```

This would make switching between local/Docker/cloud cleaner.
