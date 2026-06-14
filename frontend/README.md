# WanderMate Frontend

Expo React Native frontend for the WanderMate travel planning application.

The app connects to the Spring Boot backend and supports authentication, token storage, OTP registration, forgot password, trip management, destination management, activity management, and overlap-warning handling.

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
| Config | Expo public environment variables |
| CI | GitHub Actions TypeScript check |

---

## Current Status

```text
✅ Authentication screens are implemented
✅ Register + email OTP flow is implemented
✅ Forgot-password flow is implemented
✅ Login stores accessToken, refreshToken, and sessionToken
✅ Axios attaches auth/session headers for protected requests
✅ Token refresh flow is integrated
✅ Logout clears stored tokens/session state
✅ Trip list/detail/create/edit screens are implemented
✅ Destination list/detail/create/edit screens are implemented
✅ Activity create/detail/edit screens are implemented
✅ Trip and destination overlap confirmation flow is handled
✅ Activity overlap validation error is displayed
✅ Frontend environment switching uses Expo public env variables
✅ Frontend TypeScript check runs in CI
✅ Frontend has been tested against the deployed Render backend
⚠️ SMS OTP UI/backend types exist, but real SMS delivery is not enabled
⚠️ Current V2.5 work is frontend UX polish and component cleanup
```

---

## Run Locally

From the frontend folder:

```bash
npm install
npx expo start
```

Android:

```bash
npm run android
```

TypeScript check:

```bash
npm run typecheck
```

After changing `.env`, restart Expo with cache clear:

```bash
npx expo start --clear
```

Windows PowerShell:

```powershell
npx expo start --clear
```

---

## Environment Configuration

The frontend API URL is configured with Expo public environment variables.

Create a local env file from the template:

```powershell
copy .env.example .env
```

Production Render backend:

```env
EXPO_PUBLIC_APP_ENV=production-render
EXPO_PUBLIC_API_BASE_URL=https://wandermate-fullstack.onrender.com/The-Project
```

Android emulator connecting to a local IntelliJ backend on port `8080`:

```env
EXPO_PUBLIC_APP_ENV=local-intellij
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080/The-Project
```

Android emulator connecting to the Docker backend on port `8082`:

```env
EXPO_PUBLIC_APP_ENV=local-docker
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/The-Project
```

For browser/Postman on the host machine, use:

```text
http://localhost:8082/The-Project
```

---

## Backend URL Notes

The backend has a context path:

```text
/The-Project
```

So frontend requests are built against a base URL such as:

```text
https://wandermate-fullstack.onrender.com/The-Project
```

Example full API URL:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

Render free-tier services may sleep when inactive, so the first backend request can take around 40–60 seconds to wake up.

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

If the backend returns an access-token-expired response, the Axios interceptor calls:

```text
POST /api/v1/auth/refresh
```

with:

```text
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

The refreshed access/refresh tokens are then saved again in SecureStore.

---

## OTP Status

Email OTP is the real working OTP path when backend email configuration is provided.

Phone/SMS OTP UI/backend types exist, but real SMS provider integration is not enabled yet. Treat SMS OTP as prepared logic only until a provider is added.

Backend OTP behaviour now includes:

```text
- OTP expiry validation
- OTP retry/block handling
- OTP destination matching by email/phone
- OTP consume-on-success to prevent reuse
```

Forgot-password behaviour expects the backend to validate password rules before consuming OTP.

---

## Main User Flows

```text
1. Register user details
2. Verify user details before sending OTP
3. Send email OTP
4. Submit OTP and complete registration
5. Login
6. Store tokens securely
7. View/create/edit/delete trips
8. Add destinations to trips
9. Add activities to destinations
10. Handle overlap warnings/errors
11. Refresh tokens automatically when needed
12. Logout and clear session
```

---

## V2.5 Frontend Polish Plan

The current project phase is frontend UX polish before screenshots and demo recording.

Recommended cleanup order:

```text
1. Centralize or remove development console logs
2. Extract reusable UI components
3. Standardize screen spacing, cards, buttons, inputs, and error text
4. Improve loading states
5. Improve empty states
6. Improve API error message display
7. Polish auth screens first
8. Polish trip/destination/activity screens second
```

Suggested reusable component structure:

```text
src/components/ui/AppScreen.tsx
src/components/ui/AppButton.tsx
src/components/ui/AppInput.tsx
src/components/ui/AppCard.tsx
src/components/ui/ErrorMessage.tsx
src/components/ui/LoadingState.tsx
src/components/ui/EmptyState.tsx
src/constants/theme.ts
src/utils/logger.ts
```

Suggested logger pattern:

```ts
export const logger = {
  debug: (...args: unknown[]) => {
    if (__DEV__) {
      console.log(...args);
    }
  },
  error: (...args: unknown[]) => {
    if (__DEV__) {
      console.error(...args);
    }
  },
};
```

Then replace direct development logs with `logger.debug()` or remove them if they are no longer useful.

---

## Testing and CI

Run local TypeScript check:

```bash
npm run typecheck
```

Frontend CI workflow:

```text
.github/workflows/frontend-ci.yml
```

The workflow installs frontend dependencies and runs the TypeScript check when frontend files are changed.

Suggested later improvement:

```text
- Add ESLint
- Add Prettier
- Add a small smoke-test layer for important auth/form utilities
```

---

## Portfolio Notes

Do not take final screenshots/demo video until V2.5 frontend polish is complete. This avoids redoing portfolio images after UI cleanup.

Screenshots and short demo video are planned for V3.
