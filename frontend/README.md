# WanderMate Frontend

React Native Expo frontend for the WanderMate trip planning and collaboration app.

The frontend provides authentication screens, trip planning UI, destination/activity screens, collaboration screens, image upload, profile/settings, theme support and persistent bottom navigation.

## Status

| Area | Status |
|---|---|
| Auth screens | Login, register/OTP, forgot password |
| Token integration | access token, refresh token, session token storage/refresh/logout |
| Trips | List, create, detail, edit, cover upload |
| Destinations | List, create, detail, edit, delete |
| Activities | Nested under destinations |
| Collaboration | Invite, share code, requests, members, roles |
| Profile | Avatar upload, profile info, theme settings |
| Theme | Light/dark/system theme support |
| Navigation | Expo Router plus persistent bottom tabs |
| TypeScript | `npm run typecheck` proof included |

## Stack

- Expo Router
- React Native
- TypeScript
- Axios
- Expo SecureStore
- Expo Image Picker
- Expo Clipboard
- Expo Vector Icons
- React Hook Form
- Zod-style validation flow
- Zustand-style stores

## Run

```bash
cd frontend
npm install
npm run start
```

For Android:

```bash
npm run android
```

For iOS:

```bash
npm run ios
```

For web:

```bash
npm run web
```

If a real phone cannot load Expo assets over LAN, use tunnel mode:

```bash
npx expo start --tunnel -c
```

## Environment

Frontend API base URL is configured under:

```text
src/constants/env.ts
```

Make sure it points to the backend base URL including `/The-Project`.

## Main folders

| Folder | Purpose |
|---|---|
| `app/` | Expo Router screens and routes |
| `src/api/` | API clients |
| `src/components/` | reusable UI/collaboration/media components |
| `src/stores/` | auth/theme/token stores |
| `src/types/` | TypeScript API/domain types |
| `src/utils/` | formatting, token, role and helper utilities |
| `src/theme/` | app theme configuration |
| `src/constants/` | env and app constants |

## Key screens

| Screen | Purpose |
|---|---|
| `(auth)/login.tsx` | Login |
| `(auth)/register.tsx` | Register and OTP cooldown UI |
| `(auth)/forgot-password.tsx` | Forgot-password OTP flow |
| `(tabs)/trips.tsx` | My Trips list |
| `trips/[tripId]/index.tsx` | Trip detail |
| `trips/[tripId]/destinations/create.tsx` | Create destination |
| `trips/[tripId]/destinations/[destinationId]/index.tsx` | Destination detail |
| `trips/[tripId]/destinations/[destinationId]/activities/[activityId]/index.tsx` | Activity detail |
| `trips/[tripId]/collaboration/*` | Collaboration menu, invite, share code, requests, members |
| `(tabs)/profile.tsx` | Profile/avatar/theme settings |

## Screenshots

### My Trips

![My Trips](../docs/screenshots/03-my-trips.png)

### Trip detail as owner

![Trip detail owner](../docs/screenshots/04-trip-detail-owner.png)

### Invite member

![Invite member](../docs/screenshots/10-invite-member.png)

### Profile settings

![Profile settings](../docs/screenshots/15-profile-avatar-settings.png)

### Dark mode

![Dark mode](../docs/screenshots/16-dark-mode.png)

## Typecheck

```bash
npm run typecheck
```

Proof:

![Frontend typecheck](../docs/screenshots/20-frontend-typecheck.png)

## Permission-aware UI

The frontend hides or disables actions based on the current user's trip role.

| Role | UI behaviour |
|---|---|
| OWNER | Can manage trip, members, roles, requests and content |
| EDITOR | Can edit trip content where allowed |
| VIEWER | Read-only view, no destructive/edit actions |

Proof:

![Viewer read-only](../docs/screenshots/14-viewer-read-only.png)

## Image upload

Image upload is handled through shared media components and backend upload API.

Proof:

![Mobile upload proof](../docs/screenshots/27-mobile-upload-proof.png)
