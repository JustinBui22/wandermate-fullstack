# WanderMate Frontend

Expo Router client for the WanderMate trip-planning and collaboration API.

## Current stack

- Expo 56.0.16
- Expo Router 56.2.15 with typed routes
- React 19.2.3
- React Native 0.85.3
- TypeScript 6.0
- Axios
- Zustand
- Expo SecureStore
- React Hook Form and Zod
- Expo Image Picker, Clipboard, Linking, and DateTimePicker
- Vitest, Jest, React Native Testing Library, Maestro, EAS Workflows

## Features

- Login, registration/OTP, and forgot-password screens.
- SecureStore persistence for access, refresh, and session tokens.
- Automatic access-token refresh and one-time request retry.
- Local-session cleanup for unrecoverable authentication/session failures.
- Trip list filters and sorting.
- Trip create/detail/edit/delete screens.
- Destination and nested activity CRUD screens.
- Collaboration dashboard, invitations, join requests, share codes, members, and role controls.
- Permission-aware owner/editor/viewer UI.
- Profile editing, avatar upload, and light/dark/system theme preference.
- Persistent bottom-tab navigation.

## Project layout

```text
frontend/
├── app/                         Expo Router route files
├── assets/
├── src/
│   ├── api/                     Axios API modules and interceptors
│   ├── auth/                    Central local-session lifecycle
│   ├── components/              Shared UI, form, media, navigation and collaboration components
│   ├── constants/               API environment and theme constants
│   ├── features/                Feature-level presentation components
│   ├── hooks/
│   ├── stores/                  Zustand auth/theme stores and SecureStore token module
│   ├── theme/
│   ├── types/
│   └── utils/
├── .eas/workflows/
├── .maestro/
├── .env.example
├── app.json
├── eas.json
└── package.json
```

## Main routes

### Authentication

| Route | Purpose |
|---|---|
| `app/(auth)/login.tsx` | Login and maximum-session override handling |
| `app/(auth)/register.tsx` | Registration details, OTP send/cooldown, final registration |
| `app/(auth)/forgot-password.tsx` | Password-reset OTP flow |

### Tabs

| Route | Purpose |
|---|---|
| `app/(tabs)/index.tsx` | Home/dashboard entry |
| `app/(tabs)/trips.tsx` | Accessible trip list |
| `app/(tabs)/collaboration.tsx` | Collaboration summary and pending work |
| `app/(tabs)/profile.tsx` | Profile image/details and theme settings |

### Trip planning and collaboration

- `app/trips/create.tsx`
- `app/trips/[tripId]/index.tsx`
- `app/trips/[tripId]/edit.tsx`
- `app/trips/[tripId]/destinations/**`
- `app/trips/[tripId]/collaboration/**`
- `app/join-trip.tsx`

## Environment

Copy the template:

```bash
cp .env.example .env
```

Variables:

```text
EXPO_PUBLIC_APP_ENV
EXPO_PUBLIC_API_BASE_URL
```

`src/constants/env.ts` falls back to:

```text
https://wandermate-fullstack.onrender.com/Wandermate
```

Common Android-emulator values:

```text
# IntelliJ backend
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080/Wandermate

# Docker backend
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/Wandermate
```

A physical device cannot use `localhost` or `10.0.2.2`; use the development computer's LAN IP or the configured production URL.

## Install and run

```bash
npm ci
npm run start
```

Platform commands:

```bash
npm run android
npm run ios
npm run web
```

Tunnel mode:

```bash
npx expo start --tunnel -c
```

## Token and request lifecycle

1. Login returns `accessToken`, `refreshToken`, and `sessionToken`.
2. `tokenStore.ts` writes them to Expo SecureStore.
3. The Axios request interceptor adds `Authorization` and `Session-Token`.
4. When the backend reports an expired access token, the response interceptor calls `/api/v1/auth/refresh` with `Refresh-Token` and `Session-Token`.
5. The new access/refresh values are stored and the original request is retried once.
6. Unrecoverable `401`, token-verification failure, or explicit invalid-session responses clear local tokens through `sessionLifecycle.ts`.
7. A resource-permission `403` does not automatically log the user out.
8. Browser builds rely on the backend CORS policy allowing the frontend origin plus `Authorization`, `Session-Token`, and `Refresh-Token` headers.

## Role-aware UI

| Role | Frontend behavior |
|---|---|
| OWNER | Trip-plan editing plus member/request/share-code administration and trip deletion |
| EDITOR | Trip-plan editing, without owner-only collaboration administration |
| VIEWER | Read-only trip-plan view |

The backend still validates every protected action; hiding buttons is not the security boundary.

## Date/time behavior

The current backend request DTOs use `LocalDateTime` for trip, destination, and activity scheduling values. The frontend sends ISO date-time strings and uses shared formatting/date-picker utilities for display and input.

## Image upload

`ImageUploadPicker` sends multipart form data to:

```text
POST /api/v1/uploads/images
```

Fields:

```text
file
imageType=profile-images | trip-covers
```

The returned `imageUrl` and `publicId` are then included in profile or trip update/create requests.

## Validation and tests

TypeScript:

```bash
npm run typecheck
```

Unit and component tests:

```bash
npm test
```

Test scripts:

- `npm run test:unit` → Vitest.
- `npm run test:components` → Jest in-band.

The repository declares 10 test cases across four files:

- Axios authentication-response behavior.
- Central session-expiration behavior.
- Shared OTP auth controls/cooldown display.
- Shared `AppButton` press/disabled behavior.

## Maestro / EAS smoke test

Local flow after installing an E2E build:

```bash
maestro test .maestro/login-smoke.yml
```

The smoke flow launches `com.minhquan.wandermate` and verifies the login username, password, and submit controls. `.eas/workflows/e2e-test-android.yml` builds the `e2e-test` APK and runs that flow for pull requests.

## CI

`.github/workflows/frontend-ci.yml` uses Node.js 24 and runs:

```bash
npm ci
npm run typecheck
npm test
npm run test:components -- --coverage --coverageDirectory=coverage/components
npx expo config --type public --json
npx expo export --platform web --output-dir dist
```

The workflow uploads the component coverage/resolved Expo configuration and the static web export as separate artifacts.

Production dependency audit:

```bash
npm run audit:prod
```

The security workflow runs this as `npm audit --omit=dev --audit-level=high`, uploads the JSON report, and does not apply automatic or forced fixes. Dependabot groups Expo and React Native updates so framework-coupled packages can be reviewed together.

## Screenshots

Portfolio screenshots are stored in [`../docs/screenshots`](../docs/screenshots). See the root [screenshot checklist](../docs/SCREENSHOT_CHECKLIST.md) before publishing evidence.
