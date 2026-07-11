# Cloudinary Image Storage

This document explains how WanderMate stores profile avatars and trip cover images.

## Purpose

Cloudinary is used so images work in local development, emulator/device testing, and production deployment.

Supported image categories:

```text
profile-images
trip-covers
```

## Environment Variables

Backend requires:

```env
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
CLOUDINARY_BASE_FOLDER=wandermate
```

These must be set locally in `backend/.env` or via Docker/Render environment variables.

Never commit real Cloudinary keys.

## Backend Properties

```properties
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME:}
cloudinary.api-key=${CLOUDINARY_API_KEY:}
cloudinary.api-secret=${CLOUDINARY_API_SECRET:}
cloudinary.base-folder=${CLOUDINARY_BASE_FOLDER:wandermate}
```

## Upload Endpoint

```http
POST /api/v1/uploads/images
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
Content-Type: multipart/form-data
```

Multipart fields:

```text
file=<image file>
imageType=profile-images | trip-covers
```

Expected response body:

```json
{
  "imageUrl": "https://res.cloudinary.com/.../wandermate/profile-images/...jpg",
  "publicId": "wandermate/profile-images/owner-user-uuid"
}
```

## Frontend Flow

Profile avatar:

```text
1. User selects image in Profile screen.
2. Frontend uploads image through /api/v1/uploads/images with imageType=profile-images.
3. Backend uploads to Cloudinary and returns imageUrl + publicId.
4. Frontend saves imageUrl + publicId through PATCH /api/v1/users/me/profile.
```

Trip cover:

```text
1. User selects cover image in Create/Edit Trip screen.
2. Frontend uploads image with imageType=trip-covers.
3. Backend returns imageUrl + publicId.
4. Frontend saves values through trip create/update flow.
```

## Database Fields

Users:

```text
profile_image_url
profile_image_public_id
```

Trips:

```text
cover_image_url
cover_image_public_id
```

`imageUrl` is used for display. `publicId` is used for cleanup/deletion.

## Cleanup Logic

When a profile image or trip cover changes:

```text
1. Backend compares old public ID with new public ID.
2. If old is blank, no cleanup is needed.
3. If old equals new, no cleanup is needed.
4. If old differs from new, backend tries to delete old image from Cloudinary.
5. If cleanup fails, backend logs the error but does not fail the main update.
```

This protects the user flow from failing only because an old Cloudinary image could not be deleted.

## Important Demo Note

Some old demo records may have:

```text
cover_image_url != null
cover_image_public_id = null
```

or:

```text
profile_image_url != null
profile_image_public_id = null
```

Those old images can display, but automatic cleanup cannot delete them because Cloudinary deletion needs the public ID.

For final screenshots/demo, re-upload:

```text
- one profile avatar
- one trip cover image
```

Then verify both URL and public ID are saved.

## Docker Compose Requirement

The backend service in `docker-compose.yml` must pass Cloudinary env vars:

```yaml
environment:
  CLOUDINARY_CLOUD_NAME: ${CLOUDINARY_CLOUD_NAME}
  CLOUDINARY_API_KEY: ${CLOUDINARY_API_KEY}
  CLOUDINARY_API_SECRET: ${CLOUDINARY_API_SECRET}
  CLOUDINARY_BASE_FOLDER: ${CLOUDINARY_BASE_FOLDER}
```

## Testing

Current test coverage should include:

```text
CloudinaryImageClientImplTest
ImageUploadServiceImplTest
ImageUploadControllerImplTest
UserServiceImpl profile image cleanup tests
TripServiceImpl trip cover cleanup tests
```

Recommended manual test:

```text
1. Login on real device/emulator.
2. Upload profile image.
3. Confirm it displays after app reload.
4. Upload a different profile image.
5. Confirm old public ID cleanup is attempted.
6. Repeat for trip cover image.
```
