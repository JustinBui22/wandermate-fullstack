# Authentication and Session Flow

## Registration

```text
POST /api/v1/users/register/verify
        ↓ validate username, email, optional phone, DOB and password
POST /api/v1/otp/send
        ↓ send EMAIL_OTP, or select demo PHONE_NUM_OTP
        ↓ store purpose-bound OTP hash and expiry
POST /api/v1/users/register
        ↓ verify/consume OTP
        ↓ create BCrypt-password user
```

Email OTP is the operational delivery path. Phone OTP remains in the frontend and API for demonstration, but `SmsServiceImpl` only returns a simulated success and does not contact a real SMS gateway because a paid provider is not configured. The phone path is not production-ready.

## OTP security

- Six-digit codes are generated with `SecureRandom`.
- The plaintext code is delivered by email and is not stored.
- The database stores an HMAC derived from username, purpose and code.
- Registration and password-reset OTPs are purpose-bound.
- Cooldown, resend limits, verification retry limits, expiry and temporary restrictions are enforced.
- A successful verification consumes the OTP record state.

## Login

```text
POST /api/v1/users/login
        ↓ resolve username/email/supported phone format
        ↓ run BCrypt comparison
        ↓ enforce maximum active sessions
        ↓ generate session ID
        ↓ issue access, refresh and session tokens
```

Missing-user and incorrect-password paths return the same invalid-credentials response. The missing-user path performs a dummy BCrypt comparison to reduce timing differences.

## Tokens

### Access token

- HS512 JWT.
- Contains username, authorities and session ID.
- Short lived.
- Sent in `Authorization: Bearer ...`.

### Refresh token

- Random opaque token returned to the client.
- Only an HMAC hash is persisted.
- Rotation locks the hash row with `PESSIMISTIC_WRITE`.
- The old token is revoked and linked to its replacement.
- Confirmed reuse revokes the token family and related session.

### Session token

- Random opaque value returned to the client.
- BCrypt hash persisted in `session_token`.
- Sent in `Session-Token` for protected and refresh requests.

## Refresh

```text
POST /api/v1/auth/refresh
Refresh-Token: <refresh-token>
Session-Token: <session-token>
        ↓ lock refresh row
        ↓ validate token/session/expiry/revocation
        ↓ revoke old refresh token
        ↓ issue new access and refresh tokens
```

Only one concurrent refresh request can rotate a given active token successfully.

## Password reset

```text
POST /api/v1/otp/send
purpose=PASSWORD_RESET
EMAIL_OTP + registered email
        or demo PHONE_NUM_OTP + registered phone
        ↓ generic response for unmatched details
POST /api/v1/users/forgot-password
        ↓ verify password-reset OTP
        ↓ reject reuse of old password
        ↓ update BCrypt password
        ↓ revoke all sessions/refresh tokens for the account
```

The flow avoids confirming whether an account exists. A valid request still performs synchronous email delivery, so perfect timing equality would require asynchronous queued delivery.

## Logout

```text
POST /api/v1/users/logout
Authorization: Bearer <access-token>
Session-Token: <session-token>
        ↓ delete current session
        ↓ revoke active refresh tokens for session ID
        ↓ frontend clears SecureStore state
```
