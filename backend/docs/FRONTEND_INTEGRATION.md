# Frontend Integration

The React Native Expo frontend talks to the backend through Axios API clients.

## Frontend API files

| File | Purpose |
|---|---|
| `src/api/axiosClient.ts` | Shared Axios instance and auth handling |
| `src/api/authApi.ts` | login/register/logout/OTP auth calls |
| `src/api/userApi.ts` | profile and settings |
| `src/api/tripApi.ts` | trips/search/suggestions |
| `src/api/destinationApi.ts` | destinations |
| `src/api/activityApi.ts` | nested activities |
| `src/api/tripCollaborationApi.ts` | invitations, join requests, share codes, members |
| `src/api/uploadApi.ts` | image upload |

## Token flow

1. Login stores access, refresh and session tokens.
2. Protected API calls use Bearer access token.
3. Refresh flow uses refresh token and session token.
4. Logout clears frontend tokens and asks backend to revoke session.

## Theme flow

Frontend loads the user's profile/settings after login/session restore and applies the preferred theme. Screens use `useAppTheme()` and shared UI components.

## Navigation flow

Expo Router handles nested screens. Persistent bottom tabs allow users to jump back to Home, Trips, Collaboration and Profile from deep nested screens.

## Upload flow

1. User picks image using Expo Image Picker.
2. Frontend sends multipart upload to backend.
3. Backend uploads image to Cloudinary.
4. Frontend receives image URL/public ID.
5. User profile/trip update saves image metadata.

## Screenshots

![Mobile upload proof](../../docs/screenshots/27-mobile-upload-proof.png)

![Frontend typecheck](../../docs/screenshots/20-frontend-typecheck.png)
