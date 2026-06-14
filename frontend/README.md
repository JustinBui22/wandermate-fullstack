# WanderMate Frontend

Expo React Native frontend for the WanderMate travel planning application. The app connects to the Spring Boot backend, supports authentication, token refresh, OTP flows, trip/destination/activity CRUD, and polished mobile UI screens.

The frontend V2.5 polish is complete: shared UI components, reusable date/time picker components, cleaner error messages, polished CRUD screens, and debug log cleanup are now in place.

---

## Tech Stack

| Area | Technology |
|---|---|
| Framework | Expo React Native 56 |
| Language | TypeScript |
| Routing | Expo Router |
| HTTP client | Axios |
| State | Zustand |
| Secure token storage | Expo SecureStore |
| Date/time picker | `@react-native-community/datetimepicker` |
| UI foundation | Shared custom components + theme constants |
| Config | Expo public environment variables |
| CI | GitHub Actions TypeScript check |

---

## Current Status

```text
✅ Login screen polished
✅ Register + email OTP flow implemented and polished
✅ Forgot-password flow implemented and polished
✅ Home tab polished
✅ Trips tab polished
✅ Trip create/detail/edit/delete screens implemented and polished
✅ Destination create/detail/edit/delete screens implemented and polished
✅ Activity create/detail/edit/delete screens implemented and polished
✅ Shared UI component foundation implemented
✅ Reusable date/time picker form components extracted
✅ Frontend API error message helpers polished
✅ Login stores accessToken, refreshToken, and sessionToken
✅ Axios attaches auth/session headers for protected requests
✅ Token refresh flow is integrated
✅ Logout clears stored tokens/session state
✅ Trip and destination overlap confirmation flow is handled
✅ Activity overlap validation error is displayed
✅ Frontend environment switching uses Expo public env variables
✅ Frontend TypeScript check runs in CI
✅ Frontend has been tested against the deployed Render backend
```

Not enabled yet:

```text
⚠️ Real SMS delivery is not enabled
⚠️ Screenshots/demo video are planned for V3
```

---

## Run Locally

From the frontend folder:

```bash
cd frontend
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

The backend has this context path:

```text
/The-Project
```

Example full API URL:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

Render free-tier services may sleep when inactive, so the first backend request can take around 40-60 seconds to wake up.

---

## App Structure

```text
frontend/
├── app/
│   ├── _layout.tsx
│   ├── (auth)/
│   │   ├── login.tsx
│   │   ├── register.tsx
│   │   └── forgot-password.tsx
│   ├── (tabs)/
│   │   ├── index.tsx
│   │   ├── trips.tsx
│   │   └── _layout.tsx
│   └── trips/
│       ├── create.tsx
│       └── [tripId]/...
├── src/
│   ├── api/
│   ├── components/
│   │   ├── forms/
│   │   └── ui/
│   ├── constants/
│   ├── stores/
│   ├── types/
│   └── utils/
└── package.json
```

---

## Main Routes

```text
(auth)/login
(auth)/register
(auth)/forgot-password
(tabs)/index
(tabs)/trips
trips/create
trips/[tripId]/index
trips/[tripId]/edit
trips/[tripId]/destinations/create
trips/[tripId]/destinations/[destinationId]/index
trips/[tripId]/destinations/[destinationId]/edit
trips/[tripId]/destinations/[destinationId]/activities/create
trips/[tripId]/destinations/[destinationId]/activities/[activityId]/index
trips/[tripId]/destinations/[destinationId]/activities/[activityId]/edit
```

---

## UI Component Foundation

Reusable UI components:

```text
src/components/ui/AppScreen.tsx
src/components/ui/AppButton.tsx
src/components/ui/AppInput.tsx
src/components/ui/AppCard.tsx
src/components/ui/ErrorMessage.tsx
src/components/ui/LoadingState.tsx
src/components/ui/EmptyState.tsx
```

Reusable form components:

```text
src/components/forms/DateTimeSection.tsx
src/components/forms/DateTimePickerCard.tsx
```

Theme constants:

```text
src/constants/theme.ts
```

The V2.5 cleanup extracted repeated date/time picker UI from trip, destination, and activity create/edit screens. The screens now keep business logic while shared components handle date/time presentation.

---

## Auth Integration

The frontend stores these values in Expo SecureStore:

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

The refreshed access/refresh tokens are saved again in SecureStore.

Logout attempts remote logout first, but local token cleanup still happens even if the network logout request fails.

---

## OTP Status

Email OTP is the real working OTP path when backend email configuration is provided.

Phone/SMS OTP UI/backend types exist, but real SMS provider integration is not enabled yet. Treat SMS OTP as prepared logic only until a provider is added.

Backend OTP behaviour includes:

```text
- OTP expiry validation
- OTP retry/block handling
- OTP destination matching by email/phone
- OTP consume-on-success to prevent reuse
```

Forgot-password behaviour expects the backend to validate password rules before consuming OTP.

---

## API Error Handling

Frontend API error helpers live in:

```text
src/utils/apiWarningUtils.ts
```

They provide:

```text
- getApiErrorCode()
- getApiErrorMessage()
- getApiErrorTitle()
- hasApiWarning()
```

Common backend error codes are mapped to user-friendly frontend messages, for example:

```text
E022 -> Too many active sessions
E046 -> Invalid activity time
E049 -> Trip date conflict
E050 -> Destination date conflict
E051 -> Activity time conflict
```

Overlap behaviour:

```text
- Trip overlap warning asks the user to continue or cancel
- Destination overlap warning asks the user to continue or cancel
- Activity overlap is displayed as a blocking error
```

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

## Manual Test Checklist

After frontend changes, run:

```bash
npm run typecheck
npx expo start --clear
```

Then test:

```text
Auth:
- Register + OTP
- Login
- Wrong password error
- Max session popup
- Forgot password
- Logout

Trip:
- Empty trips state
- Create trip
- Edit trip
- Delete trip
- Trip overlap confirmation

Destination:
- Empty destinations state
- Create destination
- Edit destination
- Delete destination
- Destination overlap confirmation

Activity:
- Empty activities state
- Create activity
- Edit activity
- Delete activity
- Activity overlap error

Session:
- App reopen keeps logged-in user
- Logout clears session and returns to login
```

---

## Frontend CI

Workflow file:

```text
.github/workflows/frontend-ci.yml
```

The workflow runs when frontend files or the frontend workflow change:

```text
npm ci
npm run typecheck
```

---

## V2.5 Completed Work

```text
✅ Shared UI components added
✅ Shared form components added
✅ Login/register/forgot-password polished
✅ Home/trips tabs polished
✅ Trip CRUD screens polished
✅ Destination CRUD screens polished
✅ Activity CRUD screens polished
✅ Date/time picker duplication removed
✅ API error messages improved
✅ Leftover debug/console logs removed from app/src code
✅ TypeScript check passing
```

---

## Current Next Phase

V2.5 frontend polish is complete. The next phase is V3 portfolio proof:

```text
1. Take screenshots of key screens
2. Record a 60-90 second app demo video
3. Update root README with screenshots/demo link
4. Add project summary to CV/GitHub portfolio
```

Suggested screenshots:

```text
- Login
- Register
- Home
- My Trips
- Create Trip
- Trip Detail
- Destination Detail
- Activity Detail
```

Suggested demo flow:

```text
Login → Create Trip → Add Destination → Add Activity → Edit/Delete → Logout
```
