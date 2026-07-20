# Authentication, OTP and Session Flow

WanderMate uses a custom authentication flow built around password hashing, OTP verification, JWT access tokens, refresh tokens and session tokens.

## Main concepts

| Concept | Purpose |
|---|---|
| Password hash | Stores password securely instead of plain text |
| OTP | Verifies registration and forgot-password flows |
| Access token | Short-lived Bearer token for protected APIs |
| Refresh token | Used to obtain a new access token |
| Session token | Tracks active login session and supports logout/revocation |
| OTP purpose | Separates registration requests from password-reset requests |

## Register flow

1. User enters username, email, phone and password.
2. Backend checks active user uniqueness.
3. User requests OTP.
4. Backend validates destination, retry, block and cooldown rules.
5. Backend sends OTP.
6. User verifies OTP.
7. Backend consumes/deletes verified OTP record.
8. User completes registration.

Screenshot:

![Register OTP](../../docs/screenshots/02-register-otp.png)

## OTP resend flow

1. Existing OTP row is found by username, email or phone depending on request.
2. If blocked and restriction has not expired, request fails.
3. If blocked but restriction expired, retry state resets.
4. If send retry limit is reached, row becomes blocked.
5. If cooldown has not expired, request fails with cooldown error.
6. If checks pass, a new OTP is sent and saved.

The frontend shows a resend timer so users cannot spam OTP requests.

## Failed OTP accounting

An invalid or expired OTP ends the normal service flow by throwing a runtime
`BusinessException`. If its retry-count update were part of the same database
transaction, Spring would roll the update back together with the failed
request. An attacker could then submit unlimited bad values while the database
counter remained unchanged.

`OtpFailureAccountingService` records the retry count in a separate
`REQUIRES_NEW` transaction with a pessimistic row lock. That inner transaction
commits before the outer request returns its error. The lock prevents concurrent
requests from silently overwriting one another. An H2 integration test proves
the count/block state survives the outer rollback.

## Password-reset privacy

1. The frontend requests an OTP with `purpose: PASSWORD_RESET`.
2. The backend returns the same OTP-sent envelope for matching and non-matching
   account details, but sends nothing for a non-match.
3. Weak-password validation is independent of account existence.
4. The current-password comparison occurs only after successful OTP
   verification, preventing it from becoming a password oracle.
5. A successful reset changes the password and revokes every refresh/session
   record in one transaction.

## Login flow

1. User submits username/password.
2. Backend validates credentials.
3. Backend issues access token, refresh token and session token.
4. Frontend stores tokens securely.
5. Frontend sends access token in `Authorization: Bearer` header.

Screenshot:

![Login](../../docs/screenshots/01-login.png)

## Refresh flow

1. Frontend calls `/api/v1/auth/refresh`.
2. Request includes refresh token and session token.
3. Backend validates the session and refresh token.
4. Backend returns a fresh access token and refresh token.

Refresh tokens are stored as keyed HMAC hashes using
`REFRESH_TOKEN_HASH_SECRET`; the JWT signing key uses a different
`JWT_SECRET`. If a rotated/revoked refresh token is reused, a separate
`REQUIRES_NEW` security service records reuse, revokes its token family, and
deletes the session before the normal request throws. This prevents the outer
rollback from undoing the response to a suspected stolen token.

## Logout flow

1. Frontend calls logout with access token and session token.
2. Backend revokes the current session.
3. Frontend clears stored tokens.
4. Revoked session cannot refresh or continue authenticated requests.

Screenshot:

![Logout session proof](../../docs/screenshots/30-logout-session-proof.png)

## Session limit proof

![Session limit proof](../../docs/screenshots/29-session-limit-proof.png)

## Security notes

- Do not store real tokens in docs or screenshots.
- Do not export Postman environments after login unless tokens are cleared.
- OTP and share-code failure counters commit independently from the request that
  returns an error.
- Missing authentication is returned using the same JSON response envelope as
  other token failures.
- Login uses a generic invalid-credentials response and a dummy password hash
  when the account is missing to reduce response/timing differences.
- OTP values should be hashed in a future production-hardening step.
- IP/device rate limiting should complement account-based counters.

## Share-code failed-attempt accounting

Missing or expired share codes also lead to runtime exceptions. Their attempt
counter therefore uses `TripShareCodeSecurityEventService` with
`REQUIRES_NEW`. It pessimistically locks the user, increments or restricts the
attempt row, and marks an expired active code as expired before the outer join
request fails. Integration tests verify the attempt/restriction survives the
outer rollback.
