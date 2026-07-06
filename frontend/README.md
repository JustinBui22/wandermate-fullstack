# WanderMate Frontend

Expo React Native frontend for the WanderMate travel planning app. The app connects to the Spring Boot backend and supports authentication, OTP flows, trip/destination/activity planning, role-based collaboration, profile settings, Cloudinary image uploads, trip cover photos, avatar attribution, and light/dark/system theming.

---

## Tech Stack

| Area | Technology |
|---|---|
| Framework | Expo React Native 56 |
| Language | TypeScript |
| Routing | Expo Router |
| HTTP client | Axios |
| State | Zustand |
| Secure storage | Expo SecureStore |
| Forms/validation | React Hook Form, Zod |
| Date/time picker | `@react-native-community/datetimepicker` |
| Image picker | `expo-image-picker` |
| UI foundation | Shared custom components + dynamic theme |
| CI | GitHub Actions TypeScript check |

---

## Current Status

```text
✅ Login/register/forgot-password screens
✅ Email OTP registration flow
✅ Token storage with SecureStore
✅ Axios auth/session headers
✅ Automatic refresh-token flow
✅ Logout clears local session
✅ Home, trips, collaboration, profile tabs
✅ Trip create/detail/edit/delete
✅ Trip cover image upload/display/edit/remove
✅ Destination create/detail/edit/delete
✅ Activity create/detail/edit/delete
✅ Trip/destination overlap confirmation flow
✅ Activity overlap error display
✅ Collaboration invitations and join requests
✅ Share-code join flow
✅ Members/roles UI
✅ Pending collaboration badge/list UI
✅ Profile display name/theme/avatar
✅ Phone image picker upload for profile pictures
✅ Phone image picker upload for trip cover photos
✅ Avatar-only creator/editor attribution
✅ Quick user card on attribution avatar press
✅ Light/Dark/System theme support
✅ Shared UI components and reusable date/time picker components
✅ Frontend TypeScript check passing
```

Not enabled yet:

```text
⚠️ Cost sharing/budget split
⚠️ Viewer suggestion workflow
⚠️ Push notifications
```

---

## Run Locally

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

---

## Environment Configuration

Create a local env file from template:

```powershell
copy .env.example .env
```

Production Render backend:

```env
EXPO_PUBLIC_APP_ENV=production-render
EXPO_PUBLIC_API_BASE_URL=https://wandermate-fullstack.onrender.com/The-Project
```

Android emulator to local IntelliJ backend:

```env
EXPO_PUBLIC_APP_ENV=local-intellij
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080/The-Project
```

Android emulator to Docker backend:

```env
EXPO_PUBLIC_APP_ENV=local-docker
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/The-Project
```

The backend context path is:

```text
/The-Project
```

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
│   │   ├── collaboration.tsx
│   │   ├── profile.tsx
│   │   └── _layout.tsx
│   └── trips/
│       ├── create.tsx
│       └── [tripId]/...
├── src/
│   ├── api/
│   ├── components/
│   │   ├── collaboration/
│   │   ├── forms/
│   │   ├── media/
│   │   └── ui/
│   ├── constants/
│   ├── hooks/
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
(tabs)/collaboration
(tabs)/profile
trips/create
trips/[tripId]/index
trips/[tripId]/edit
trips/[tripId]/members
trips/[tripId]/collaboration/members
trips/[tripId]/collaboration/requests
trips/[tripId]/destinations/create
trips/[tripId]/destinations/[destinationId]/index
trips/[tripId]/destinations/[destinationId]/edit
trips/[tripId]/destinations/[destinationId]/activities/create
trips/[tripId]/destinations/[destinationId]/activities/[activityId]/index
trips/[tripId]/destinations/[destinationId]/activities/[activityId]/edit
```

---

## Auth Integration

After login, the frontend stores:

```text
accessToken
refreshToken
sessionToken
```

in Expo SecureStore.

Protected API requests attach:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

Refresh requests use:

```text
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

Logout calls the backend, then clears local tokens even if network logout fails.

---

## Image Upload Flow

The frontend uses `expo-image-picker` through:

```text
src/components/media/ImageUploadPicker.tsx
```

Upload API:

```text
src/api/uploadApi.ts
```

Flow:

```text
User picks image from phone
→ frontend sends multipart request to /api/v1/uploads/images
→ backend uploads image to Cloudinary
→ backend returns imageUrl + publicId
→ frontend stores both in profile/trip payload
→ backend saves both in MariaDB
```

Upload response shape:

```ts
type ImageUploadResponse = {
  imageUrl: string;
  publicId: string;
  fileName: string;
  imageType: "profile-images" | "trip-covers";
};
```

Profile image fields:

```text
profileImageUrl
profileImagePublicId
```

Trip cover fields:

```text
coverImageUrl
coverImagePublicId
```

When the user removes an image, the frontend sends empty strings so the backend can clear the DB fields and delete the old Cloudinary asset.

---

## UI Foundation

Shared UI components:

```text
src/components/ui/AppScreen.tsx
src/components/ui/AppButton.tsx
src/components/ui/AppInput.tsx
src/components/ui/AppCard.tsx
src/components/ui/ErrorMessage.tsx
src/components/ui/LoadingState.tsx
src/components/ui/EmptyState.tsx
```

Shared form/media components:

```text
src/components/forms/DateTimeSection.tsx
src/components/forms/DateTimePickerCard.tsx
src/components/media/ImageUploadPicker.tsx
```

Theme:

```text
src/theme/appTheme.ts
src/stores/themeStore.ts
src/hooks/useAppTheme.ts
src/constants/theme.ts
```

The app supports:

```text
Light theme
Dark theme
System theme
```

---

## Collaboration UI

Implemented collaboration areas:

```text
- Pending invitations received by me
- Join requests for my trips
- My sent join requests
- Trip member list
- Role badge
- Owner role controls
- Accept/reject actions
- Share-code join flow
- Private overlap warning cards
```

Attribution UI:

```text
- Destination creator/editor avatar
- Activity creator/editor avatar
- Avatar-only display on cards
- Quick user card on avatar press
```

---

## API Modules

| File | Purpose |
|---|---|
| `src/api/authApi.ts` | Login/register/logout/OTP/forgot password |
| `src/api/tripApi.ts` | Trip CRUD, cover fields, search/suggest |
| `src/api/destinationApi.ts` | Destination CRUD |
| `src/api/activityApi.ts` | Activity CRUD |
| `src/api/tripCollaborationApi.ts` | Invitations, join requests, members, share-code flow |
| `src/api/uploadApi.ts` | Cloudinary-backed image upload endpoint |
| `src/api/axiosClient.ts` | Shared Axios client and token refresh interceptor |
| `src/refreshApi.ts` | Refresh call without interceptor loop |

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

Date/time helper:

```text
src/utils/dateTimePickerUtils.ts
```

---

## Manual Test Checklist

Run first:

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

Profile:
- Upload profile picture
- Change profile picture
- Remove profile picture
- Change display name
- Change Light/Dark/System theme

Trips:
- Create trip with cover image
- Edit trip cover image
- Remove trip cover image
- Delete trip
- My Trips cover image display
- Trip Detail cover hero display
- Trip overlap confirmation

Destinations:
- Create destination
- Edit destination
- Delete destination
- Destination overlap confirmation
- Creator avatar shows

Activities:
- Create activity
- Edit activity
- Delete activity
- Activity overlap error
- Creator avatar shows

Collaboration:
- Send invitation
- Accept/reject invitation
- Send join request
- Accept/reject join request
- Role-based UI behavior
- Pending badge/list behavior
- Private overlap warning

Session:
- App reopen keeps logged-in state
- Token refresh works
- Logout clears session
```

---

## Frontend CI

Workflow:

```text
.github/workflows/frontend-ci.yml
```

Command:

```text
npm ci
npm run typecheck
```

---

## Next Phase

V3 is feature-complete enough. Next should be V4 portfolio proof:

```text
1. Final bug pass
2. Screenshots
3. Demo video
4. README media section
5. CV/GitHub project bullets
6. Interview explanation notes
```

Cost sharing is a good future feature, but it is not required before portfolio proof.
