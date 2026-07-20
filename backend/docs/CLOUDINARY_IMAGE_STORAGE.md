# Cloudinary Image Storage

WanderMate uses Cloudinary for uploaded user avatars and trip cover images.

## Upload endpoint

```http
POST /api/v1/uploads/images
Content-Type: multipart/form-data
Authorization: Bearer <accessToken>
```

Form-data fields:

```text
file: image file
imageType: profile-images or trip-covers
```

## Response data

The upload response includes the public image URL and Cloudinary public ID. The frontend stores/uses the image URL for rendering. The backend stores public IDs where needed so older Cloudinary images can be deleted or replaced safely.

## Image types

| Value | Used by |
|---|---|
| `profile-images` | User profile avatar upload |
| `trip-covers` | Trip cover image upload |

## Server-side validation

The service does not trust the client MIME header alone. It enforces the 5 MB
limit, checks the declared MIME against PNG/JPEG/WebP/HEIF signatures, rejects
unknown or mismatched content, decodes PNG/JPEG files with ImageIO, and caps
decoded PNG/JPEG images at 20 million pixels. WebP/HEIF receive container
signature validation locally and are decoded by Cloudinary.

When an uploaded image is assigned to a profile or trip, the backend also
validates that the HTTPS URL and public ID match the configured Cloudinary
account, the requested image type, the authenticated uploader's server-created
user folder, and the generated UUID filename pattern. Unchanged existing image
references and explicit image removal remain supported.

## Screenshots

### Trip cover upload

![Trip cover upload](../../docs/screenshots/05-trip-cover-upload.png)

### Profile avatar upload

![Profile avatar settings](../../docs/screenshots/15-profile-avatar-settings.png)

### Cloudinary proof

![Cloudinary upload proof](../../docs/screenshots/22-cloudinary-upload-proof.png)

## Environment variables

```text
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
CLOUDINARY_BASE_FOLDER
```

Do not commit real Cloudinary secrets.

## Future hardening

- Add upload audit logging.
- Add a server-side WebP/HEIF decoder if validation must not depend on the
  Cloudinary decode step.
