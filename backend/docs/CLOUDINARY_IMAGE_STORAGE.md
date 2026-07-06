# Cloudinary Image Storage Guide

This document explains how WanderMate stores profile pictures and trip cover images using Cloudinary.

---

## Why Cloudinary

Uploaded images should not be stored in the Render backend filesystem because deployed service files can be replaced during redeploys/restarts. WanderMate stores actual image files in Cloudinary and stores only references in MariaDB.

Database fields:

```text
users.profile_image_url
users.profile_image_public_id

trips.cover_image_url
trips.cover_image_public_id
```

---

## Upload Flow

```text
Phone image
→ Expo frontend multipart upload
→ Spring Boot /api/v1/uploads/images
→ Cloudinary upload
→ backend returns imageUrl + publicId
→ frontend saves imageUrl + publicId on profile/trip
→ MariaDB stores URL + publicId
```

---

## Endpoint

```http
POST /api/v1/uploads/images
```

Required headers:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

Multipart fields:

```text
file
imageType
```

Allowed image types:

```text
profile-images
trip-covers
```

---

## Folder Strategy

Cloudinary folder structure:

```text
wandermate/profile-images/users/{userId}
wandermate/trip-covers/users/{userId}
```

`userId` is used instead of username because it is stable and avoids exposing usernames in folder names.

---

## Cleanup Strategy

The backend stores the old publicId before updating a profile/trip. After saving the new value:

```text
old publicId exists and old != new → delete old Cloudinary asset
old publicId empty → no cleanup
old == new → no cleanup
```

Cleanup cases:

```text
- Profile image replaced
- Profile image removed
- Trip cover replaced
- Trip cover removed
- Trip deleted
```

Deletion failures are logged as warnings so the main profile/trip update still succeeds.

---

## Known Limitation

If a user uploads an image but cancels before saving the form, the uploaded Cloudinary asset can remain orphaned.

Future solution options:

```text
- temporary_uploads table
- scheduled cleanup job
- Cloudinary folder cleanup for old unused assets
```

For the current portfolio version, this limitation is acceptable.
