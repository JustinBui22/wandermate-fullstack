# WanderMate Frontend

Expo Router client for the WanderMate trip-planning and collaboration API.

## Stack

- Expo `~56.0.17`
- Expo Router `~56.2.16` with typed routes
- React 19.2.3
- React Native 0.85.3
- TypeScript 6
- Axios
- Zustand
- Expo SecureStore
- React Hook Form and Zod
- Expo Image Picker, Clipboard, Linking and DateTimePicker
- Vitest, Jest, React Native Testing Library, Maestro and EAS Workflows

## Features

- Login, email-OTP registration/password reset and a demo-only phone-OTP option.
- Phone OTP remains visible, but no real SMS is sent because the backend uses a simulated provider stub for this portfolio project.
- SecureStore persistence for access, refresh and session tokens.
- Automatic access-token refresh and one-time request retry.
- Local-session cleanup for unrecoverable authentication/session failures.
- Trip list filtering and sorting.
- Trip, destination and nested activity CRUD.
- Collaboration dashboard, invitations, join requests, share codes, members and role controls.
- Permission-aware owner/editor/viewer UI.
- Profile editing, avatar upload and light/dark/system theme preference.
- Persistent bottom-tab navigation.

## Layout

```text
frontend/
├── app/                         Expo Router route files
├── assets/
├── src/
│   ├── api/                     Axios API modules and interceptors
│   ├── auth/                    Local-session lifecycle
│   ├── components/              Shared UI and feature components
│   ├── constants/               Environment and theme constants
│   ├── features/
│   ├── hooks/
│   ├── stores/                  Zustand and SecureStore token storage
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
| `app/(auth)/register.tsx` | Registration details, OTP method selection, cooldown and account creation |
| `app/(auth)/forgot-password.tsx` | Generic account recovery by email OTP or demo phone OTP |

### Tabs

| Route | Purpose |
|---|---|
| `app/(tabs)/index.tsx` | Home/dashboard |
| `app/(tabs)/trips.tsx` | Accessible trip list |
| `app/(tabs)/collaboration.tsx` | Collaboration summary and pending work |
| `app/(tabs)/profile.tsx` | Profile, avatar and theme settings |

### Trip and collaboration routes

- `app/trips/create.tsx`
- `app/trips/[tripId]/index.tsx`
- `app/trips/[tripId]/edit.tsx`
- `app/trips/[tripId]/destinations/**`
- `app/trips/[tripId]/collaboration/**`
- `app/join-trip.tsx`

## Environment

```bash
cp .env.example .env
```

Variables:

```text
EXPO_PUBLIC_APP_ENV
EXPO_PUBLIC_API_BASE_URL
```

Default production fallback:

```text
https://wandermate-fullstack.onrender.com/Wandermate
```

Android emulator:

```text
# IntelliJ backend
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080/Wandermate

# Docker backend
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/Wandermate
```

A physical device must use the computer's LAN IP or a reachable hosted URL.

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

## Authentication behavior

Login stores:

```text
accessToken
refreshToken
sessionToken
```

Protected requests attach:

```http
Authorization: Bearer <access-token>
Session-Token: <session-token>
```

The Axios client:

1. attaches the current access/session tokens;
2. attempts one refresh after an access-token authentication failure;
3. updates stored access/refresh tokens;
4. retries the original request once;
5. clears the local session only for unrecoverable authentication/session errors.

Registration and password recovery support `EMAIL_OTP` and `PHONE_NUM_OTP` requests. Email OTP is fully operational. The phone option is demo-only: the current backend simulates a successful SMS send but does not deliver a real message because no paid SMS gateway is configured.

## Collaboration behavior

The frontend reflects the backend permission model:

- `OWNER`: plan editing and collaboration administration.
- `EDITOR`: plan editing without owner-only administration.
- `VIEWER`: read-only access.

Backend authorization remains authoritative.

Share-code preview uses:

```text
POST /api/v1/trips/share-codes/preview
```

because preview attempts update security counters and are not safe GET operations.

## Date handling

- Trip and destination dates are date-only `yyyy-MM-dd` strings.
- Date-only values are parsed using local calendar components, not `new Date("yyyy-MM-dd")`, to prevent UTC day shifts.
- Activity values remain local date-time strings without automatic viewer-timezone conversion.
- Audit and expiry timestamps returned by the backend are UTC instants.

## Verification

```bash
npm ci
npm run typecheck
npm test
npx expo config --type public --json
npx expo export --platform web --output-dir dist
```

`npm test` runs Vitest and Jest. The repository currently declares 13 test cases covering:

- Axios authentication/error behavior;
- session lifecycle;
- date-only formatting/parsing;
- OTP UI controls;
- shared button behavior.

Production dependency audit:

```bash
npm run audit:prod
```

This runs `npm audit --omit=dev --audit-level=high` and does not force dependency upgrades.

## E2E smoke flow

```bash
maestro test .maestro/login-smoke.yml
```

The existing Maestro flow is intentionally a small login-screen smoke test rather than a complete business-flow suite.

## Build output

Generated directories are not source files and should not be committed:

```text
node_modules/
dist/
coverage/
.expo/
```
