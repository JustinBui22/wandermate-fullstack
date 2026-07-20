# Cloudinary Image Storage

## Configuration

```text
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
CLOUDINARY_BASE_FOLDER=wandermate
```

Spring reads these through `application.properties`. The Cloudinary bean always sets secure HTTPS delivery.

## Upload endpoint

```http
POST /Wandermate/api/v1/uploads/images
Authorization: Bearer <access-token>
Session-Token: <session-token>
Content-Type: multipart/form-data
```

Fields:

```text
file=<image>
imageType=profile-images | trip-covers
```

## Validation

`ImageContentValidator` performs backend-side validation before upload. The current tests cover:

- missing/empty files;
- maximum allowed size;
- supported declared MIME types;
- signature/container checks;
- PNG/JPEG decoding;
- invalid/corrupt content.

Spring's multipart limits are both 5 MB:

```properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

## Folder and public-ID structure

```text
<base-folder>/<imageType>/users/<userId>/<generated-public-id>
```

Generated ID patterns:

```text
profile-<userId>-<uuid>
trip-cover-<userId>-<uuid>
```

The upload response contains the secure URL and public ID.

## Assigning an image to a user or trip

The frontend first uploads the image, then includes both returned values in the profile/trip request:

```json
{
  "profileImageUrl": "https://res.cloudinary.com/...",
  "profileImagePublicId": "wandermate/profile-images/users/12/profile-12-..."
}
```

or:

```json
{
  "coverImageUrl": "https://res.cloudinary.com/...",
  "coverImagePublicId": "wandermate/trip-covers/users/12/trip-cover-12-..."
}
```

`ImageReferenceValidator` checks HTTPS Cloudinary host/path, base folder, upload category, authenticated user ID and generated UUID-style suffix before a newly changed reference is accepted.

## Replacement and deletion behavior

- Profile update stores the new reference, then asks the Cloudinary client to delete the old public ID when it changed.
- Trip update does the same for the previous cover.
- Trip deletion removes the existing cover after deleting the trip.
- The upload controller currently has only the POST endpoint; abandoned uploads that are never assigned do not have a dedicated cleanup endpoint.

That last lifecycle gap is documented in the roadmap.

## Frontend integration

`ImageUploadPicker` uses `uploadApi.ts` and lets screens upload profile/trip images. API requests then persist the returned URL/public ID through user or trip endpoints.

## Operational checks

- Never commit Cloudinary credentials.
- Treat public IDs as identifiers, not secrets.
- Confirm the Render environment includes all Cloudinary values.
- Test both upload and replacement cleanup.
- Monitor for abandoned/unreferenced assets until a cleanup endpoint/job is implemented.
