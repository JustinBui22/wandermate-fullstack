# Screenshot Checklist

A screenshot is publishable only after privacy and accuracy review. File existence alone is not approval.

| File | Evidence | Review status |
|---|---|---|
| `01-login.png` | Login screen | Recheck current UI |
| `02-register-otp.png` | Registration OTP method selection | If phone OTP is shown, label it demo-only and do not imply a real SMS was delivered |
| `03-my-trips.png` | Trip list/theme | Review |
| `04-trip-detail-owner.png` | Owner trip detail | Review |
| `05-trip-cover-upload.png` | Cover upload | Review Cloudinary details |
| `06-destinations-with-creator.png` | Destination list | Review account data |
| `07-destination-detail.png` | Destination detail | Review |
| `08-activity-detail.png` | Activity detail | Review |
| `09-collaboration-menu-owner.png` | Collaboration menu | Review |
| `10-invite-member.png` | Invitation flow | Redact usernames/emails |
| `11-share-code.png` | Share-code flow | Redact or use expired demo code |
| `12-join-requests.png` | Join requests | Redact account data |
| `13-members-role-management.png` | Role management | Redact account data |
| `14-viewer-read-only.png` | Viewer permissions | Review |
| `15-profile-avatar-settings.png` | Profile/theme | Use fake demo data |
| `18-swagger-local.png` | Local OpenAPI | Review request examples |
| `19-backend-tests.png` | Backend tests | Re-capture after final test run |
| `20-frontend-typecheck.png` | Frontend typecheck | Re-capture after final changes |
| `21-github-repo.png` | Repository history | Review private browser information |
| `22-cloudinary-upload-proof.png` | Cloudinary upload | Redact URLs/public IDs |
| `23-docker-running.png` | Docker services | Redact environment/host details |
| `24-api-postman-proof.png` | API request | Must not show access/refresh/session tokens |
| `25-render-logs.png` | Render logs | Re-capture after log sanitization; redact identifiers |
| `26-database-schema.png` | Database schema | No credentials or runtime data |
| `27-mobile-upload-proof.png` | Mobile upload | Review account data |
| `28-owner-editor-viewer-proof.png` | Role authorization | Use demo users |
| `29-session-limit-proof.png` | Session limit | Redact session IDs/tokens |
| `30-logout-session-proof.png` | Logout revocation | Redact email, DOB, username and sessions |

## Health evidence

Use the live endpoint instead of a static screenshot:

```text
https://wandermate-fullstack.onrender.com/Wandermate/api/v1/health
```

## Before publishing

- Use dedicated fake demo accounts.
- Hide tokens, OTPs, passwords, session IDs and authorization headers.
- Hide email addresses, phone numbers, DOBs and personal names.
- Hide database credentials, internal connection strings and provider secrets.
- Hide Cloudinary secure URLs/public IDs when they identify private resources.
- Do not publish raw share codes that are still active.
- Phone-OTP screenshots must state that delivery is simulated because no paid SMS provider is configured.
- Re-capture screens that show pre-sanitization logs.
- Keep text legible and crop unrelated desktop/browser chrome.
- Open every link from GitHub after pushing.
