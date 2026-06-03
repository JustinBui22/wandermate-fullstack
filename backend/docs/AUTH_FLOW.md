# Authentication and Token Flow

This document explains how authentication, access tokens, refresh tokens, sessions, and logout work in the backend.

---

## Main Concepts

| Concept | Purpose |
|---|---|
| Access token | Short-lived JWT used to access protected APIs |
| Refresh token | Longer-lived token used to request a new access token |
| Session token | Represents a login session and supports session validation/revocation |
| Token filter | Checks incoming protected requests before they reach controllers |
| Logout | Revokes session and active refresh tokens so the user cannot continue using that session |

---

## Login Response

A successful login returns:

```text
accessToken
refreshToken
sessionToken
```

Protected requests require:

```text
Authorization: Bearer <accessToken>
Session-Token: <sessionToken>
```

The refresh endpoint requires:

```text
Refresh-Token: <refreshToken>
Session-Token: <sessionToken>
```

---

## Login Flow

```mermaid
sequenceDiagram
    actor Client
    participant UserAPI as User API
    participant UserService
    participant TokenService
    participant UserRepo as UserRepository
    participant TokenDB as Token Tables

    Client->>UserAPI: POST /api/v1/users/login
    UserAPI->>UserService: login(request)
    UserService->>UserRepo: Find active user by username/email/phone
    UserRepo-->>UserService: User entity
    UserService->>UserService: Validate password
    UserService->>TokenService: Check max active sessions
    UserService->>UserService: Create sessionId
    UserService->>TokenService: Generate JWT access token with sessionId claim
    TokenService-->>UserService: accessToken
    UserService->>TokenService: Generate session token
    TokenService->>TokenDB: Save hashed session token
    TokenService-->>UserService: sessionToken
    UserService->>TokenService: Generate refresh token
    TokenService->>TokenDB: Save hashed refresh token
    TokenService-->>UserService: refreshToken
    UserService-->>UserAPI: accessToken + refreshToken + sessionToken
    UserAPI-->>Client: Login success response
```

---

## Protected Request Flow

```mermaid
flowchart TD
    A[Client sends protected request] --> B[TokenFilter]
    B --> C{Public URL?}
    C -- Yes --> Z[Skip token validation]
    C -- No --> D{Authorization Bearer token exists?}
    D -- No --> X[Reject as unauthorized]
    D -- Yes --> E[Validate JWT access token]
    E --> F{JWT valid and not expired?}
    F -- No --> X
    F -- Yes --> G[Extract username and sessionId from claims]
    G --> H{Session-Token header exists?}
    H -- No --> X
    H -- Yes --> I[Validate session token against database]
    I --> J{Session valid?}
    J -- No --> X
    J -- Yes --> K[Set AuthenticatedUser in SecurityContext]
    K --> L[Controller receives request]
    L --> M[Service checks ownership/business rules]
    M --> N[Return response]
```

---

## Refresh Token Flow

```mermaid
sequenceDiagram
    actor Client
    participant API as Auth API
    participant TokenService
    participant RefreshDB as RefreshToken Table
    participant SessionDB as SessionToken Table

    Client->>API: POST /api/v1/auth/refresh
    Note over Client,API: Headers: Refresh-Token + Session-Token
    API->>TokenService: refreshAccessToken(refreshToken, sessionToken)
    TokenService->>RefreshDB: Find refresh token by hash
    RefreshDB-->>TokenService: Refresh token entity

    alt Refresh token not found
        TokenService-->>API: REFRESH_TOKEN_INVALID
        API-->>Client: Refresh failed
    else Refresh token revoked
        TokenService->>RefreshDB: Mark reuseDetected = true
        TokenService->>RefreshDB: Revoke active refresh tokens for sessionId
        TokenService->>SessionDB: Revoke/delete session token
        TokenService-->>API: REFRESH_TOKEN_INVALID
        API-->>Client: Refresh failed
    else Refresh token expired
        TokenService->>RefreshDB: Revoke expired refresh token
        TokenService->>SessionDB: Revoke/delete session token
        TokenService-->>API: REFRESH_TOKEN_EXPIRED
        API-->>Client: Refresh expired
    else Refresh token active
        TokenService->>SessionDB: Validate session token
        alt Session token invalid
            TokenService-->>API: SESSION_TOKEN_INVALID
            API-->>Client: Refresh failed
        else Session token valid
            TokenService->>RefreshDB: Revoke old refresh token
            TokenService->>TokenService: Generate new access token
            TokenService->>TokenService: Generate new refresh token
            TokenService->>RefreshDB: Save new refresh token
            TokenService->>RefreshDB: Set replacedByTokenId on old token
            TokenService-->>API: New accessToken + refreshToken
            API-->>Client: Refresh success
        end
    end
```

---

## Logout Flow

Logout is a protected API, so the request must first pass the token filter.

```mermaid
sequenceDiagram
    actor Client
    participant TokenFilter
    participant UserAPI as User API
    participant UserService
    participant TokenService
    participant DB as Token Tables

    Client->>TokenFilter: POST /api/v1/users/logout
    Note over Client,TokenFilter: Authorization + Session-Token headers
    TokenFilter->>TokenFilter: Validate access token and session token
    TokenFilter->>UserAPI: Allow request
    UserAPI->>UserService: logout(sessionToken)
    UserService->>UserService: Get username/sessionId from AuthenticatedUserProvider
    UserService->>TokenService: Revoke session token by sessionId
    TokenService->>DB: Delete session token
    UserService->>TokenService: Revoke active refresh tokens by sessionId
    TokenService->>DB: Mark active refresh tokens revoked
    UserService->>UserService: Clear SecurityContext
    UserService-->>UserAPI: LOGOUT_SUCCESS
    UserAPI-->>Client: Logout success
```

---

## Why Refresh Token Revocation Matters

Refresh token revocation helps protect the system if a token is stolen or if the user logs out.

Important behaviours:

- Expired refresh tokens cannot be used.
- Revoked refresh tokens cannot be used.
- Reuse of a revoked refresh token is detected.
- Old refresh tokens are replaced when a new refresh token is issued.
- Logout prevents future refresh from the same session.

---

## Interview Explanation

A clear way to explain this project in an interview:

```text
The backend uses short-lived JWT access tokens for protected API requests, but it also requires a session token for session validation. When the access token expires, the client can use a refresh token with the session token to request a new access token. Refresh tokens and session tokens are stored as hashes in the database so they can be revoked during logout or replaced during refresh.
```
