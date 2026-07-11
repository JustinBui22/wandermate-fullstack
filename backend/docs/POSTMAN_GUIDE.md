# Postman Guide

This guide gives a practical Postman flow for manually testing the WanderMate backend.

## Environment Variables

Create a Postman environment with:

```text
baseUrl=http://localhost:8080/The-Project
accessToken=
refreshToken=
sessionToken=
tripId=
destinationId=
activityId=
shareCode=
requestId=
tripMemberId=
```

For Docker:

```text
baseUrl=http://localhost:8082/The-Project
```

For production:

```text
baseUrl=https://wandermate-fullstack.onrender.com/The-Project
```

## Common Headers

For protected requests:

```http
Authorization: Bearer {{accessToken}}
Session-Token: {{sessionToken}}
Content-Type: application/json
```

For refresh:

```http
Refresh-Token: {{refreshToken}}
Session-Token: {{sessionToken}}
Content-Type: application/json
```

## 1. Health Check

```http
GET {{baseUrl}}/api/v1/health
```

Expected: success/healthy response.

## 2. Register Verify

```http
POST {{baseUrl}}/api/v1/users/register/verify
```

Body:

```json
{
  "username": "owner_user",
  "password": "Password123!",
  "email": "owner@example.com",
  "phoneNumber": "0412345678",
  "dob": "1999-12-25"
}
```

## 3. Send OTP

```http
POST {{baseUrl}}/api/v1/otp/send
```

Body shape depends on the current DTO and OTP method. Use Swagger locally to confirm exact fields.

## 4. Register

```http
POST {{baseUrl}}/api/v1/users/register
```

Body:

```json
{
  "username": "owner_user",
  "password": "Password123!",
  "email": "owner@example.com",
  "phoneNumber": "0412345678",
  "dob": "1999-12-25",
  "otp": "123456"
}
```

## 5. Login

```http
POST {{baseUrl}}/api/v1/users/login
```

Body:

```json
{
  "username": "owner_user",
  "password": "Password123!",
  "overrideMaxSession": false
}
```

After response, save:

```text
accessToken
refreshToken
sessionToken
```

## 6. Current Profile

```http
GET {{baseUrl}}/api/v1/users/me
```

Headers:

```http
Authorization: Bearer {{accessToken}}
Session-Token: {{sessionToken}}
```

## 7. Create Trip

```http
POST {{baseUrl}}/api/v1/trips
```

Body example:

```json
{
  "tripName": "Japan Food Trip",
  "destination": "Japan",
  "startDate": "2026-08-01T09:00:00",
  "endDate": "2026-08-10T18:00:00",
  "allowOverlap": false
}
```

Save returned `tripId`.

## 8. Create Destination

```http
POST {{baseUrl}}/api/v1/trips/{{tripId}}/destinations
```

Body example:

```json
{
  "destinationName": "Tokyo",
  "startDate": "2026-08-01T09:00:00",
  "endDate": "2026-08-04T18:00:00",
  "notes": "Food and city walks"
}
```

Save `destinationId`.

## 9. Create Activity

```http
POST {{baseUrl}}/api/v1/trips/{{tripId}}/destinations/{{destinationId}}/activities
```

Body example:

```json
{
  "activityName": "Sushi night",
  "description": "Dinner near Shibuya",
  "location": "Shibuya",
  "startDateTime": "2026-08-01T19:00:00",
  "endDateTime": "2026-08-01T21:00:00"
}
```

Save `activityId`.

## 10. Invite User

```http
POST {{baseUrl}}/api/v1/trips/{{tripId}}/invitations
```

Body example:

```json
{
  "targetUsername": "viewer_user",
  "requestedRole": "VIEWER"
}
```

## 11. Share Code

Generate/regenerate active share code:

```http
POST {{baseUrl}}/api/v1/trips/{{tripId}}/share-codes/regenerate
```

Preview share code:

```http
GET {{baseUrl}}/api/v1/trips/share-codes/{{shareCode}}
```

Send join request by share code:

```http
POST {{baseUrl}}/api/v1/trips/share-codes/{{shareCode}}/join-requests
```

## 12. Refresh Token

```http
POST {{baseUrl}}/api/v1/auth/refresh
```

Headers:

```http
Refresh-Token: {{refreshToken}}
Session-Token: {{sessionToken}}
```

Update saved tokens from response.

## 13. Logout

```http
POST {{baseUrl}}/api/v1/users/logout
```

Headers:

```http
Authorization: Bearer {{accessToken}}
Session-Token: {{sessionToken}}
```

After logout, protected calls with old tokens should fail.
