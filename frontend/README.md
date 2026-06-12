# WanderMate Frontend

Expo React Native frontend for the WanderMate travel planning application.

The app connects to the Spring Boot backend and supports authentication, token storage, trip management, destination management, activity management, and overlap-warning handling.

---

## Tech Stack

| Area | Technology |
|---|---|
| Framework | Expo React Native |
| Language | TypeScript |
| Routing | Expo Router |
| HTTP Client | Axios |
| State | Zustand |
| Secure Token Storage | Expo SecureStore |
| Forms / Validation | React Hook Form, Zod |

---

## Run Locally

```bash
npm install
npm run android
```

---

## Backend URL

Current local Android emulator backend URL:

```ts
export const API_BASE_URL = "http://10.0.2.2:8080/The-Project";
```

Use this when the backend is running locally through IntelliJ on host port `8080`.

For Android emulator connecting to Docker backend on host port `8082`, use:

```ts
export const API_BASE_URL = "http://10.0.2.2:8082/The-Project";
```

For browser/Postman on the host machine, use:

```text
http://localhost:8082/The-Project
```

---

## Auth Integration

The frontend stores these tokens in Expo SecureStore:

```text
accessToken
refreshToken
sessionToken
```

Protected API requests attach:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

If the backend returns an access-token-expired response, the Axios interceptor calls `/api/v1/auth/refresh` with:

```text
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

The refreshed access/refresh tokens are then saved again in SecureStore.

---

## OTP Status

Email OTP is the real working OTP path when backend email configuration is provided.

Phone/SMS OTP UI/backend types exist, but real SMS provider integration is not enabled yet. Treat SMS OTP as prepared logic only until a provider is added.
