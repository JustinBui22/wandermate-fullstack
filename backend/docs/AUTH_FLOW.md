# Authentication, OTP and Session Flow

## Stored credentials and tokens

| Item | Client receives | Database stores |
|---|---|---|
| Password | Never returned | BCrypt hash in `users.password` |
| Access token | JWT | Not persisted as an access-token row |
| Refresh token | UUID string | HMAC-SHA256 hash plus session/rotation metadata |
| Session token | UUID string | BCrypt hash plus username/session ID |
| OTP | Six-digit value sent to destination | Current OTP value and retry/expiry metadata in `otp_check` |

The last row describes the current implementation. OTP hashing and explicit purpose binding in storage are roadmap items.

## Registration flow

```text
POST /users/register/verify
        ↓ validate username/password/email/phone/DOB availability
POST /otp/send (purpose REGISTRATION)
        ↓ send email or phone OTP; store current OTP record
POST /users/register
        ↓ verify OTP and destination
        ↓ delete consumed OTP record
        ↓ BCrypt password and create user
```

Registration DTO date format is `DD/MM/YYYY`.

## OTP send behavior

`OtpServiceImpl`:

- validates request method and destination format;
- checks whether registration destinations are already used;
- handles password-reset requests without sending when account/destination does not match;
- reuses an OTP-check row by username/email/phone where available;
- enforces cooldown and maximum send attempts;
- creates a six-digit OTP with `SecureRandom`;
- records expiry, retry counters and restriction state.

Clean-seed defaults:

| Setting | Value |
|---|---:|
| OTP expiration | 300,000 ms (5 minutes) |
| Resend cooldown | 60,000 ms |
| Max send attempts | 3 |
| Max verification attempts | 3 |
| Restriction duration | 900,000 ms (15 minutes) |

## OTP verification behavior

- The username must have an active, non-blocked OTP row.
- The email/phone in the verification request must match the destination stored in that row.
- Expired or incorrect values call `OtpFailureAccountingService`.
- That service uses `REQUIRES_NEW` and a pessimistic lock so the failed-attempt update commits even when verification fails.
- A successful OTP is deleted to prevent direct reuse.

`OtpPurpose` currently influences send/recovery behavior but is not stored on `OtpCheckEntity`; therefore the current database record is not purpose-bound.

## Login flow

```text
POST /users/login
        ↓ resolve username/email/phone
        ↓ BCrypt password check
        ↓ enforce maximum active sessions
        ↓ create session ID
        ↓ issue access JWT
        ↓ create and persist hashed session token
        ↓ create and persist hashed refresh token
        ↓ return all three raw tokens once
```

The clean seed allows three active sessions. When `overrideMaxSession=false`, login fails at the limit. When true, the oldest sessions are revoked until a slot is available.

## Protected request flow

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
        ↓
TokenFilter validates JWT signature/expiry/user
        ↓
reads username and sessionId claims
        ↓
finds matching session row and BCrypt-checks Session-Token
        ↓
populates SecurityContext with AuthenticatedUser
        ↓
service authorization runs
```

Missing Bearer authentication is handled by `JsonAuthenticationEntryPoint`. Business token/session failures use the shared API response structure.

## Access token

- Signed with the environment-provided JWT secret.
- HS512 minimum secret length enforced: 64 UTF-8 bytes.
- Contains subject, authorities, session ID, issue time and expiry.
- Clean-seed expiry: 300,000 ms (5 minutes).

## Refresh flow

```text
POST /api/v1/auth/refresh
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
        ↓
HMAC-hash presented refresh token and find stored row
        ↓
reject/revoke expired or reused token/session
        ↓
validate session token
        ↓
revoke old refresh token
        ↓
issue new access token and refresh token
        ↓
record replacement token ID
```

Refresh-token hash key minimum: 32 UTF-8 bytes. Clean-seed refresh expiry is one month.

## Refresh-token reuse

When a revoked refresh token is presented:

1. `RefreshTokenReuseServiceImpl` starts a `REQUIRES_NEW` transaction.
2. It marks reuse on the presented token record.
3. It revokes active refresh tokens with the same session ID.
4. It removes the related session-token row.
5. The caller returns `REFRESH_TOKEN_INVALID`.

Because the revocation transaction is independent, its changes survive the caller's failure.

## Frontend lifecycle

- SecureStore holds the three tokens and username.
- Axios adds access/session headers.
- Access-token expiry triggers one shared refresh promise and one retry of the original request.
- A normal permission `403` is preserved as a resource error.
- Invalid-session/token responses clear SecureStore and reset auth/theme state.
- App startup attempts refresh before marking a stored session authenticated.

## Logout and password reset

Logout deletes the current session row and revokes active refresh tokens for that session. Forgot password verifies OTP, updates the BCrypt password, and revokes all active refresh/session records for the user.

## Current security follow-up

See [ROADMAP.md](ROADMAP.md) for:

- hashing and purpose-binding OTP records;
- removing account-enumeration behavior from `/users/check`;
- method-specific code-owned public-route policy;
- serialized refresh/share-code rotation;
- managed scheduler lifecycle.
