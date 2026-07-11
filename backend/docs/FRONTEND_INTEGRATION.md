# Frontend Integration Guide

This document explains how the Expo React Native frontend connects to the WanderMate backend.

## Backend Base URL

Local IntelliJ backend:

```env
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080/The-Project
```

Android emulator calling host machine:

```env
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080/The-Project
```

Docker backend:

```env
EXPO_PUBLIC_API_BASE_URL=http://localhost:8082/The-Project
```

Production Render backend:

```env
EXPO_PUBLIC_API_BASE_URL=https://wandermate-fullstack.onrender.com/The-Project
```

## Auth Headers

After login, the frontend stores:

```text
accessToken
refreshToken
sessionToken
```

Protected requests send:

```http
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

Refresh requests send:

```http
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

## Token Refresh

Expected frontend behaviour:

```text
1. Normal protected request uses accessToken + sessionToken.
2. If access token expires, Axios refresh flow calls /api/v1/auth/refresh.
3. New tokens are saved securely.
4. Original request is retried.
5. If refresh fails, frontend logs user out and clears tokens.
```

## Login Max Session Handling

If backend returns `MAX_SESSIONS_REACHED`, frontend should:

```text
1. Show confirmation Alert.
2. If user cancels, do nothing.
3. If user continues, retry login with overrideMaxSession=true.
```

React Native `Alert` callbacks should not be `async` directly. Use:

```ts
onPress: () => {
    void handleConfirmLoginOverride();
}
```

## Theme Hydration

Saved theme preference is returned by:

```text
GET /api/v1/users/me
```

The frontend should apply saved theme after:

```text
login success
session restore
profile/settings update
```

This prevents the app from staying in `SYSTEM` theme until the Profile screen is opened.

## Role-Based UI Rules

Frontend should hide actions based on current role, but backend remains the source of truth.

Recommended UI rules:

```ts
const isOwner = currentUserRole === "OWNER";
const isEditor = currentUserRole === "EDITOR";
const isViewer = currentUserRole === "VIEWER";

const canEditTrip = isOwner || isEditor;
const canEditContent = isOwner || isEditor;
const canDeleteTrip = isOwner;
const canManageCollaboration = isOwner;
```

Backend still enforces permissions even if frontend UI is bypassed.

## Image Upload Flow

Frontend flow:

```text
1. User selects image using Expo ImagePicker.
2. Frontend sends multipart upload to POST /api/v1/uploads/images.
3. Backend returns imageUrl and publicId.
4. Frontend saves those values in profile or trip update request.
```

Image type values:

```text
profile-images
trip-covers
```

## Collaboration Flow

Owner flow:

```text
1. Open trip detail.
2. Open collaboration menu.
3. Invite user, manage join requests, generate share code, manage members.
```

Invited user flow:

```text
1. Login as invited user.
2. View received invitation.
3. Accept or reject.
4. Access trip according to assigned role.
```

Share-code flow:

```text
1. Owner generates active share code.
2. Other user enters/pastes code in join-trip screen.
3. Frontend previews trip.
4. User submits join request.
5. Owner accepts/rejects request.
```

## Error and Warning Handling

Backend can return warning-style business codes such as:

```text
TRIP_OVERLAP_WARNING
DESTINATION_OVERLAP_WARNING
```

Frontend should show confirmation and resubmit with `allowOverlap=true` where supported.

## Production Notes

For production builds:

```text
EXPO_PUBLIC_API_BASE_URL should point to Render backend.
Do not hardcode localhost.
Do not expose backend secrets in frontend env.
Only EXPO_PUBLIC_* values are public-safe values.
```
