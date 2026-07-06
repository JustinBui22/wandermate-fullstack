# Operations, Health Check, and Logging Notes

This document explains how the WanderMate backend is checked, monitored, and debugged during local development and Render deployment.

---

## Health Check

The backend exposes a public health check endpoint:

```text
GET /api/v1/health
```

Local Maven/IntelliJ backend:

```text
http://localhost:8080/The-Project/api/v1/health
```

Local Docker backend:

```text
http://localhost:8082/The-Project/api/v1/health
```

Production Render backend:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

Expected response:

```json
{
  "status": "UP",
  "service": "WanderMate backend"
}
```

This endpoint is intentionally public so deployment status can be checked without Swagger or authentication.

---

## Production Profile

Render uses the production Spring profile:

```text
SPRING_PROFILES_ACTIVE=prod
```

Expected Render startup log:

```text
The following 1 profile is active: "prod"
```

The production profile is used to reduce development-only behaviour in deployment.

Production profile goals:

```text
- Disable Swagger/OpenAPI in production
- Reduce debug logging
- Disable SQL query logging
- Keep production logs readable
```

---

## Swagger and API Documentation

Swagger/OpenAPI is useful for local development and manual testing.

Local Swagger UI:

```text
http://localhost:8080/The-Project/swagger-ui/index.html
```

Local Docker Swagger UI:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

Swagger is disabled in production through the `prod` profile.

Production API documentation is maintained through markdown docs:

```text
backend/docs/API_GUIDE.md
backend/docs/AUTH_FLOW.md
backend/docs/POSTMAN_GUIDE.md
backend/docs/PRODUCTION_API_DOCS.md
```

Production health should be checked through:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

---

## Logging Strategy

Development logging can be more verbose to help debug security, SQL, and application behaviour.

Production logging should avoid excessive detail.

Production logging goals:

```text
- Keep logs readable
- Avoid exposing sensitive data
- Avoid logging passwords, OTP values, access tokens, refresh tokens, or session tokens
- Keep security logs at INFO instead of DEBUG
- Keep SQL output disabled
```

Sensitive values that should never be logged:

```text
- Passwords
- OTP codes
- JWT access tokens
- Refresh tokens
- Session tokens
- Email OAuth secrets
- Database passwords
- Personal user information
```

---

## Deployment Checks

After every deployment, verify:

```text
1. GitHub Actions backend tests passed
2. Render deployment completed successfully
3. Render logs show active profile: prod
4. Health endpoint returns status UP
5. Frontend can connect to the Render backend
6. Login/register flow still works from the frontend
```

Useful production check URL:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

---

## CI/CD Checks

The backend deployment flow is:

```text
Push to main
→ GitHub Actions runs backend tests
→ Tests pass
→ Render deploy hook is triggered
→ Render deploys backend
```

Before trusting a deployment, check:

```text
- GitHub Actions backend workflow passed
- Render deployment is live
- Health endpoint returns UP
```

The Render deploy hook is stored in GitHub Actions secrets as:

```text
RENDER_DEPLOY_HOOK_URL
```

---

## Common Production Issues

### Render service sleeps

Render free-tier services may sleep when inactive.

Symptoms:

```text
- First request is slow
- Frontend appears to hang briefly
- Health endpoint takes longer than usual
```

Fix:

```text
Wait for the service to wake up and retry the request.
```

---

### Database connection issue

Symptoms:

```text
- Backend fails to start
- Render logs show datasource or connection errors
- Login/register fails because backend cannot access the database
```

Check Render environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
CLOUDINARY_BASE_FOLDER
```

Also confirm that the database host allows connections from Render.

---

### Email OTP issue

Symptoms:

```text
- Registration OTP is not sent
- OTP endpoint returns email-related error
- Render logs show email/OAuth configuration issue
```

Check Render environment variables:

```text
EMAIL_OAUTH_REFRESH_ENABLED
EMAIL_CLIENT_ID
EMAIL_CLIENT_SECRET
EMAIL_REFRESH_TOKEN
EMAIL_TOKEN_URL
EMAIL_ADDRESS_CONFIG
```

Also confirm that the email/OAuth refresh token is still valid.

---

### Frontend cannot connect to backend

Symptoms:

```text
- Network request failed
- Login/register fails from the mobile app
- Backend works in Postman but not in emulator
```

Check frontend API base URL:

```text
EXPO_PUBLIC_API_BASE_URL
```

For production demo:

```text
https://wandermate-fullstack.onrender.com/The-Project
```

For Android emulator + local IntelliJ backend:

```text
http://10.0.2.2:8080/The-Project
```

For Android emulator + Docker backend:

```text
http://10.0.2.2:8082/The-Project
```

After changing `.env`, restart Expo with cache clear:

```powershell
npx expo start --clear
```

---

### Swagger not available in production

This is expected.

Swagger is disabled in production by the `prod` profile.

Use the health endpoint and markdown docs instead:

```text
https://wandermate-fullstack.onrender.com/The-Project/api/v1/health
```

---

## Local Troubleshooting Commands

Check Docker containers:

```powershell
docker compose ps
```

Rebuild Docker backend and database:

```powershell
cd backend
docker compose down -v
docker compose up --build
```

Run backend tests:

```powershell
cd backend
.\mvnw test
```

Run frontend TypeScript check:

```powershell
cd frontend
npm run typecheck
```

Start Expo with cache clear:

```powershell
cd frontend
npx expo start --clear
```

---

## Production Safety Rules

Do not commit:

```text
backend/.env
frontend/.env
backend/target/
frontend/node_modules/
frontend/.expo/
.idea/
```

Do not log:

```text
- Passwords
- OTP codes
- Access tokens
- Refresh tokens
- Session tokens
- Email OAuth secrets
- Database passwords
```

Do not expose publicly:

```text
- Real production `.env` values
- Render deploy hook URL
- Database credentials
- OAuth credentials
```

---

## Future Monitoring Improvements

Future production monitoring could include:

```text
- Spring Boot Actuator
- Structured JSON logging
- Request correlation IDs
- Centralized log storage
- Error tracking
- Uptime monitoring
- Alerting for failed health checks
```

These are not required for the current portfolio version, but they are good future improvements.

---

## Operations Summary

For the current WanderMate portfolio version:

```text
Health check: /api/v1/health
Production profile: prod
Swagger in production: disabled
Production deployment: Render
CI/CD: GitHub Actions + Render deploy hook
Production docs: markdown docs + Postman guide
```

This setup is simple, safe, and appropriate for a junior backend/full-stack portfolio project.

## V3 Deployment Checks

After deploying V3, verify:

```text
1. /api/v1/health returns UP
2. Login returns accessToken, refreshToken, and sessionToken
3. /api/v1/users/me returns profile fields
4. /api/v1/collaboration/summary returns badge counts
5. Owner can invite another user
6. User can request to join through trip/share code
7. Owner can accept/reject join request
8. Destination/activity attribution fields appear in API responses
9. Frontend can switch Light/Dark/System theme
```

If profile or attribution fails, check that the production database has the V3 columns.

---

## Cloudinary Production Checks

Render stores the backend process, but Cloudinary stores user-uploaded images.

Required Render environment variables:

```env
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
CLOUDINARY_BASE_FOLDER=wandermate
```

After deployment, verify:

```text
1. Upload profile image from mobile app.
2. The returned imageUrl starts with https://res.cloudinary.com/.
3. users.profile_image_public_id is saved in DB.
4. Replace the profile image and confirm the old Cloudinary asset is deleted.
5. Create a trip cover image and confirm trips.cover_image_public_id is saved.
6. Replace/remove trip cover and confirm old Cloudinary asset is deleted.
```

Operational note:

```text
If a user uploads an image but cancels the form before saving, that uploaded image may remain as an orphan Cloudinary asset. A future cleanup job or temporary-upload table can handle abandoned uploads.
```

---

## V4 Portfolio Operations Checklist

Before recording screenshots/demo:

```text
1. Backend tests pass.
2. Frontend typecheck passes.
3. Docker fresh-start works locally.
4. Render backend health check is UP.
5. Cloudinary upload works in local/dev environment.
6. Demo accounts are created.
7. No real secrets are committed.
8. GitHub Actions workflows are passing.
```
