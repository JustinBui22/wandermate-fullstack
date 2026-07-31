# Postman Guide

## Environment variables

Create a private Postman environment:

```text
baseUrl=http://localhost:8080/Wandermate
accessToken=
refreshToken=
sessionToken=
tripId=
destinationId=
activityId=
requestId=
tripMemberId=
shareCode=
```

For Docker, use:

```text
baseUrl=http://localhost:8082/Wandermate
```

Never commit/export real token values into the repository.

## Shared headers

Protected requests:

```http
Authorization: Bearer {{accessToken}}
Session-Token: {{sessionToken}}
```

Refresh:

```http
Refresh-Token: {{refreshToken}}
Session-Token: {{sessionToken}}
```

## Suggested flow

### 1. Health

```http
GET {{baseUrl}}/api/v1/health
```

### 2. Validate registration details

```http
POST {{baseUrl}}/api/v1/users/register/verify
Content-Type: application/json
```

Use the registration body from [API_GUIDE.md](API_GUIDE.md), with `otp` set to an empty value where the frontend pre-validation flow does so.

### 3. Send registration OTP

```http
POST {{baseUrl}}/api/v1/otp/send
```

```json
{
  "userName": "sampleuser",
  "email": "sample@example.com",
  "otpVerificationMethod": "EMAIL_OTP",
  "emailEnum": "EMAIL_OTP_REGISTER",
  "purpose": "REGISTRATION"
}
```

For a phone request, use `PHONE_NUM_OTP`, `phoneNumber` and `SMS_OTP_REGISTER`. That path is demo-only in the current repository: the backend simulates success but does not send a real SMS because no paid gateway is configured. Use email OTP for actual manual verification.

### 4. Register

```http
POST {{baseUrl}}/api/v1/users/register
```

Include the received OTP.

### 5. Login

```http
POST {{baseUrl}}/api/v1/users/login
```

```json
{
  "username": "sampleuser",
  "password": "Password1!",
  "overrideMaxSession": false
}
```

Store the three returned values in the Postman environment.

### 6. Protected profile

```http
GET {{baseUrl}}/api/v1/users/me
Authorization: Bearer {{accessToken}}
Session-Token: {{sessionToken}}
```

### 7. Refresh

```http
POST {{baseUrl}}/api/v1/auth/refresh
Refresh-Token: {{refreshToken}}
Session-Token: {{sessionToken}}
```

Replace both access and refresh values after every successful rotation.

### 8. Create trip

```http
POST {{baseUrl}}/api/v1/trips
Authorization: Bearer {{accessToken}}
Session-Token: {{sessionToken}}
```

Use date-only `yyyy-MM-dd` values for trip start and end dates.

### 9. Destination and activity

Create a destination under `{{tripId}}`, then an activity under `{{destinationId}}`. Verify editor/viewer behavior with separate accounts.

### 10. Collaboration

- Invite a second user.
- Accept as the second user.
- Generate a share code as owner.
- Preview it with authenticated headers.
- Submit a join request and accept/reject as owner.
- Test role update and member removal.

### 11. Logout

```http
POST {{baseUrl}}/api/v1/users/logout
Authorization: Bearer {{accessToken}}
Session-Token: {{sessionToken}}
```

Then verify the old session can no longer call a protected endpoint.

## Test-script example

Store login tokens without printing them:

```javascript
const body = pm.response.json().body;
pm.environment.set("accessToken", body.accessToken);
pm.environment.set("refreshToken", body.refreshToken);
pm.environment.set("sessionToken", body.sessionToken);
```

## Evidence safety

Before taking screenshots:

- hide Postman environment quick-look values;
- blur authorization/session headers;
- do not show OTPs, credentials or personal email/phone data;
- prefer showing status, endpoint path and sanitized response fields.
