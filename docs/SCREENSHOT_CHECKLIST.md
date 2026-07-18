# Screenshot Checklist

This checklist tracks the screenshot evidence that is currently included in the repository.
`Present` means the file exists at the documented, case-sensitive GitHub path; it does not
replace the final privacy and presentation review before publishing.

| File | Evidence | Status |
|---|---|---|
| [`01-login.png`](screenshots/01-login.png) | Login entry point | Present |
| [`02-register-otp.png`](screenshots/02-register-otp.png) | Registration and OTP verification | Present |
| [`03-my-trips.png`](screenshots/03-my-trips.png) | Trip list and dark theme | Present |
| [`04-trip-detail-owner.png`](screenshots/04-trip-detail-owner.png) | Owner trip view | Present |
| [`05-trip-cover-upload.png`](screenshots/05-trip-cover-upload.png) | Trip cover upload | Present |
| [`06-destinations-with-creator.png`](screenshots/06-destinations-with-creator.png) | Destination list and creator attribution | Present |
| [`07-destination-detail.png`](screenshots/07-destination-detail.png) | Destination details | Present |
| [`08-activity-detail.png`](screenshots/08-activity-detail.png) | Activity details | Present |
| [`09-collaboration-menu-owner.png`](screenshots/09-collaboration-menu-owner.png) | Owner collaboration menu | Present |
| [`10-invite-member.png`](screenshots/10-invite-member.png) | Member invitation and role selection | Present |
| [`11-share-code.png`](screenshots/11-share-code.png) | Trip share code | Present |
| [`12-join-requests.png`](screenshots/12-join-requests.png) | Join-request review | Present |
| [`13-members-role-management.png`](screenshots/13-members-role-management.png) | Member role management | Present |
| [`14-viewer-read-only.png`](screenshots/14-viewer-read-only.png) | Viewer read-only permissions | Present |
| [`15-profile-avatar-settings.png`](screenshots/15-profile-avatar-settings.png) | Profile, avatar and theme settings | Present |
| [`18-swagger-local.png`](screenshots/18-swagger-local.png) | Local Swagger/OpenAPI documentation | Present |
| [`19-backend-tests.png`](screenshots/19-backend-tests.png) | Backend test result | Present |
| [`20-frontend-typecheck.png`](screenshots/20-frontend-typecheck.png) | Frontend typecheck result | Present |
| [`21-github-repo.png`](screenshots/21-github-repo.png) | GitHub repository history | Present |
| [`22-cloudinary-upload-proof.png`](screenshots/22-cloudinary-upload-proof.png) | Cloudinary upload | Present |
| [`23-docker-running.png`](screenshots/23-docker-running.png) | Running Docker services | Present |
| [`24-api-postman-proof.png`](screenshots/24-api-postman-proof.png) | Authenticated API request | Present |
| [`25-render-logs.png`](screenshots/25-render-logs.png) | Render deployment logs | Present |
| [`26-database-schema.png`](screenshots/26-database-schema.png) | Database schema | Present |
| [`27-mobile-upload-proof.png`](screenshots/27-mobile-upload-proof.png) | Mobile image upload | Present |
| [`28-owner-editor-viewer-proof.png`](screenshots/28-owner-editor-viewer-proof.png) | Owner, editor and viewer authorization | Present |
| [`29-session-limit-proof.png`](screenshots/29-session-limit-proof.png) | Session-limit behavior | Present |
| [`30-logout-session-proof.png`](screenshots/30-logout-session-proof.png) | Session revocation after logout | Present |

## Intentionally absent numbers

- `16-dark-mode.png` is not included. Existing screenshots 03 and 15 already demonstrate
  dark theme and theme settings, so documentation links to those files instead.
- `17-render-health.png` is not included. The documentation links directly to the
  [production health endpoint](https://wandermate-fullstack.onrender.com/The-Project/api/v1/health)
  so reviewers can verify the current deployment state.

## Before publishing or presenting

- Re-capture any screen that contains debug controls, emulator overlays or temporary UI text.
- Crop unrelated browser, desktop and tool chrome where it does not add evidence.
- Hide email addresses, tokens, session identifiers, database credentials and personal data.
- Keep text legible at GitHub's default README width.
- Confirm that every screenshot still matches the current application behavior.
- Open each link from GitHub after pushing, because local files do not prove the remote branch
  contains the same filename and capitalization.
