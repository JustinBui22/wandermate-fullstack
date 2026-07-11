# WanderMate Frontend

Expo React Native frontend for the WanderMate full-stack travel-planning app.

The app connects to the Spring Boot backend and supports authentication, OTP flows, trip/destination/activity planning, role-based collaboration, share-code joining, profile settings, Cloudinary image uploads, trip cover photos, creator/editor attribution, and Light/Dark/System theme support.

---

## Current Frontend Status

```text
Current phase: V4 portfolio proof
Frontend state: feature-complete enough for screenshots/demo
Latest TypeScript check: npm run typecheck passed
```

Implemented:

```text
✅ Login/register/forgot-password screens
✅ Email OTP registration flow
✅ Forgot-password OTP flow
✅ Token storage with Expo SecureStore
✅ Axios auth/session headers
✅ Automatic refresh-token flow
✅ Logout clears local session
✅ Home, trips, collaboration, profile tabs
✅ Trip create/detail/edit/delete
✅ Trip status display from backend
✅ Trip cover image upload/display/edit/remove
✅ Destination create/detail/edit/delete
✅ Activity create/detail/edit/delete
✅ Trip/destination overlap confirmation flow
✅ Activity overlap error display
✅ Collaboration invitations and join requests
✅ Share-code join flow
✅ Trip member/role management UI
✅ Pending collaboration badge/list UI
✅ Owner/editor/viewer role-based UI hiding
✅ Viewer read-only mode
✅ Profile display name/phone/dob/theme/avatar
✅ Phone image picker upload for profile pictures
✅ Phone image picker upload for trip cover photos
✅ Creator/editor attribution on destinations and activities
✅ Quick user card on attribution avatar press
✅ Light/Dark/System theme support
✅ Saved theme preference is applied after login/session restore
✅ Shared UI components and reusable date/time picker components
✅ Frontend TypeScript check passing
```

Not enabled yet:

```text
⚠️ Cost sharing/budget split
⚠️ Viewer suggestion workflow
⚠️ Push notifications
⚠️ Native app-store build/distribution
```

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
| UI foundation | Shared custom UI components + dynamic theme |
| CI | GitHub Actions TypeScript check |

---

## Run Locally

Install dependencies:

```bash
cd frontend
npm install
```

Start Expo:

```bash
npx expo start
```

Android:

```bash
npm run android
```

iOS:

```bash
npm run ios
```

Web:

```bash
npm run web
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

Template file:

```text
frontend/.env.example
```

Create local env file:

```bash
cp .env.example .env
```

Windows PowerShell:

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

Backend context path:

```text
/The-Project
```

Do not commit the real `frontend/.env` file.

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
│   ├── join-trip.tsx
│   └── trips/
│       ├── create.tsx
│       └── [tripId]/
│           ├── index.tsx
│           ├── edit.tsx
│           ├── collaboration/
│           │   ├── index.tsx
│           │   ├── invite.tsx
│           │   ├── members.tsx
│           │   ├── requests.tsx
│           │   └── share-code.tsx
│           └── destinations/
│               ├── create.tsx
│               └── [destinationId]/
│                   ├── index.tsx
│                   ├── edit.tsx
│                   └── activities/
│                       ├── create.tsx
│                       └── [activityId]/
│                           ├── index.tsx
│                           └── edit.tsx
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
│   ├── theme/
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
join-trip
trips/create
trips/[tripId]/index
trips/[tripId]/edit
trips/[tripId]/collaboration/index
trips/[tripId]/collaboration/invite
trips/[tripId]/collaboration/share-code
trips/[tripId]/collaboration/requests
trips/[tripId]/collaboration/members
trips/[tripId]/destinations/create
trips/[tripId]/destinations/[destinationId]/index
trips/[tripId]/destinations/[destinationId]/edit
trips/[tripId]/destinations/[destinationId]/activities/create
trips/[tripId]/destinations/[destinationId]/activities/[activityId]/index
trips/[tripId]/destinations/[destinationId]/activities/[activityId]/edit
```

---

## API Modules

| File | Purpose |
|---|---|
| `src/api/authApi.ts` | Login/register/logout/OTP/forgot password |
| `src/api/tripApi.ts` | Trip CRUD, cover image fields, search/suggest |
| `src/api/destinationApi.ts` | Destination CRUD |
| `src/api/activityApi.ts` | Activity CRUD |
| `src/api/tripCollaborationApi.ts` | Invitations, join requests, members, share-code flow |
| `src/api/uploadApi.ts` | Cloudinary-backed image upload endpoint |
| `src/api/userApi.ts` | Profile and settings APIs |
| `src/api/axiosClient.ts` | Shared Axios client and token refresh interceptor |
| `src/refreshApi.ts` | Refresh call without interceptor loop |

---

## Auth Integration

After login, the frontend stores:

```text
accessToken
refreshToken
sessionToken
username
```

Storage:

```text
Expo SecureStore
```

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

Auth store:

```text
src/stores/authStore.ts
```

Token store:

```text
src/stores/tokenStore.ts
```

Logout behavior:

```text
- Calls backend logout
- Clears local access/refresh/session tokens even if network logout fails
- Resets saved frontend theme preference to SYSTEM
```

Session restore behavior:

```text
- Loads tokens from SecureStore
- Refreshes access token
- Calls profile API
- Applies saved theme preference from backend profile
- Marks user authenticated if restore succeeds
```

---

## Theme System

Theme files:

```text
src/theme/appTheme.ts
src/stores/themeStore.ts
src/hooks/useAppTheme.ts
src/constants/theme.ts
```

Supported preferences:

```text
LIGHT
DARK
SYSTEM
```

Important behavior:

```text
- User can save theme preference from Profile
- Saved preference is stored in backend profile
- Auth store hydrates saved theme after login
- Auth store hydrates saved theme after session restore
- Logout resets local preference to SYSTEM
```

Screens recently converted to dynamic theme:

```text
(auth)/login
(auth)/register
(auth)/forgot-password
join-trip
trips/[tripId]/collaboration/index
trips/[tripId]/collaboration/invite
trips/[tripId]/collaboration/share-code
trips/[tripId]/collaboration/requests
```

For future polish, keep moving any remaining static style colors to `useAppTheme()` where the screen is visibly affected in dark mode.

---

## Role-Based UI

Role helper:

```text
src/utils/tripRoleUtils.ts
```

Frontend UI rules:

```text
OWNER:
- Can edit trip
- Can delete trip
- Can add/edit/delete destinations and activities
- Can invite members
- Can manage join requests
- Can create/revoke share codes
- Can update roles and remove members

EDITOR:
- Can view trip
- Can edit trip content
- Can add/edit/delete destinations and activities
- Cannot delete trip
- Cannot manage members/requests/share codes

VIEWER:
- Can view trip, destinations, and activities
- Cannot edit content
- Sees read-only mode messaging
```

The backend is the real security layer. The frontend only hides actions to improve UX.

---

## Image Upload Flow

Image picker component:

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

When the user removes an image, the frontend sends empty strings so the backend can clear DB fields and delete the old Cloudinary asset.

---

## Collaboration UI

Implemented areas:

```text
- Pending invitations received by me
- Join requests for my trips
- My sent join requests
- Trip collaboration menu
- Invite member screen
- Share-code screen
- Join-request review screen
- Members/role management screen
- Role badge
- Owner-only controls
- Accept/reject actions
- Share-code join screen
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

Display helper:

```text
src/utils/dateFormat.ts
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
- Reopen app and restore session

Theme:
- Save LIGHT preference
- Save DARK preference
- Save SYSTEM preference
- Confirm login/session restore applies saved preference
- Confirm auth, join-trip, and collaboration screens look correct in dark mode

Profile:
- Upload profile picture
- Change profile picture
- Remove profile picture
- Change display name
- Change phone/date of birth

Trips:
- Create trip with cover image
- Edit trip cover image
- Remove trip cover image
- Delete trip
- My Trips cover image display
- Trip Detail cover hero display
- Trip overlap confirmation
- Finished trip changed to future date becomes ongoing when backend returns updated status

Destinations:
- Create destination
- Edit destination
- Delete destination
- Destination overlap confirmation
- Creator/editor attribution shows

Activities:
- Create activity
- Edit activity
- Delete activity
- Activity overlap error
- Creator/editor attribution shows

Collaboration:
- Send invitation
- Accept/reject invitation
- Send join request
- Accept/reject join request
- Generate invite code
- Join with invite code
- Role-based UI behavior
- Viewer read-only behavior
- Pending badge/list behavior
- Private overlap warning

Session:
- Access-token refresh works
- Logout clears session
- Max active session confirmation works
```

---

## Screenshot Checklist

Recommended screenshot folder:

```text
docs/media/screenshots/
```

Recommended screenshot names:

```text
01-login.png
02-my-trips.png
03-trip-detail-owner.png
04-trip-cover-upload.png
05-destinations-with-creator.png
06-activity-attribution.png
07-collaboration-menu-owner.png
08-invite-member.png
09-share-code.png
10-join-requests.png
11-members-role-management.png
12-viewer-read-only.png
13-profile-avatar-settings.png
14-dark-mode.png
15-render-health.png
16-github-actions.png
```

For README, use the strongest 6–8 screenshots, not all of them.

---

## Frontend CI

Workflow:

```text
.github/workflows/frontend-ci.yml
```

Commands:

```text
npm ci
npm run typecheck
```

Current local check:

```text
npm run typecheck passed
```

---

## Known Technical Debt / Future Improvements

```text
- Remove or avoid any obsolete legacy routes if they are no longer used
- Add ESLint if stricter callback/promise rules are desired
- Add unit/component tests for frontend helpers and stores
- Add end-to-end tests for auth and collaboration flows
- Add native builds later if publishing to app stores
- Add push notifications later
- Add cost sharing/budget split in V5
```

---

## Portfolio Readiness Checklist

```text
✅ Frontend feature set is strong enough
✅ TypeScript check passes
✅ Auth/session integration is good
✅ Image upload integration is good
✅ Role-based UX is good
✅ Dark mode is much cleaner after latest fixes
⚠️ Need screenshots
⚠️ Need demo video
⚠️ Need README media section
⚠️ Need clean public repo/package without .env, node_modules, .expo, or local build outputs
```
