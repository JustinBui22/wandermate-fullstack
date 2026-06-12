# Testing Guide

This document summarizes the backend test suite and how to run it.

---

## Test Style

The current backend tests are mainly service-level unit tests using:

```text
JUnit 5
Mockito
AssertJ
Maven Surefire
```

These tests focus on business logic without needing a real database connection.

---

## Run Tests

From the backend folder:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw test
```

Run one test class:

```powershell
.\mvnw -Dtest=TripServiceImplTest test
```

---

## Current Passing Service Test Suite

Current surefire reports show:

| Test Class | Tests | Failures | Errors |
|---|---:|---:|---:|
| `ActivityServiceImplTest` | 25 | 0 | 0 |
| `DestinationServiceImplTest` | 31 | 0 | 0 |
| `EmailServiceImplTest` | 9 | 0 | 0 |
| `OtpServiceImplTest` | 28 | 0 | 0 |
| `SmsServiceImplTest` | 3 | 0 | 0 |
| `TokenServiceImplTest` | 33 | 0 | 0 |
| `TripServiceImplTest` | 39 | 0 | 0 |
| `UserServiceImplTest` | 29 | 0 | 0 |

Total service tests:

```text
197 passed
0 failures
0 errors
```

---

## What Is Covered

### User Service

- Register validation
- Duplicate username/email/phone checks
- Password hashing flow
- Login success/failure paths
- Max session flow integration
- Forgot password with OTP
- Logout flow
- User check flow
- Business exception handling

### Token Service

- Access token generation
- JWT validation
- Expired/invalid token handling
- Refresh token generation and hashing
- Refresh token rotation
- Refresh token reuse detection
- Session token generation/validation/revocation
- Max active session handling

### OTP Service

- Email OTP send flow
- Phone OTP service branch with mocked `SmsServiceImpl`
- OTP retry/send limits
- OTP verification retry limits
- OTP expiry
- OTP destination mismatch checks
- Blocking and restriction reset logic

### Trip Service

- Create/list/detail/update/delete trips
- Ownership checks
- Duplicate trip name checks
- Trip overlap warning
- `allowOverlap` flow
- Trip date conflict with existing destinations
- Search/suggest methods

### Destination Service

- Create/list/detail/update/delete destinations
- Ownership checks through parent trip
- Destination inside trip range
- Destination overlap warning
- `allowOverlap` flow
- Destination date conflict with existing activities

### Activity Service

- Create/list/detail/update/delete activities
- Ownership checks through destination/trip
- Activity inside destination range
- Activity time overlap hard error
- Invalid/missing activity time checks

---

## Default Spring Boot Context Test Note

A generated test such as:

```java
@SpringBootTest
class TheProjectApplicationTests {
    @Test
    void contextLoads() {}
}
```

starts the full Spring application context. Because this project uses:

```properties
spring.datasource.url=${DB_URL}
```

that test requires real DB environment variables or a dedicated test profile.

If `DB_URL` is missing, the test can fail with:

```text
Driver org.mariadb.jdbc.Driver claims to not accept jdbcUrl, ${DB_URL}
```

Acceptable options:

```text
Option A: Remove the generated contextLoads test if service tests are the main proof.
Option B: Add application-test.properties and @ActiveProfiles("test").
Option C: Use Testcontainers later for a real integration test setup.
```

For the current portfolio stage, service-level tests are the strongest proof of business logic.

---

## Mockito Notes

When mocking repository `save(...)`, remember that Mockito does not behave like JPA.

Example: JPA may generate UUID/ID values after save, but a mocked repository will not.

Tests that need generated IDs should simulate it:

```java
when(refreshTokenRepository.save(any(RefreshTokenEntity.class)))
        .thenAnswer(invocation -> {
            RefreshTokenEntity token = invocation.getArgument(0);
            if (token.getTokenId() == null) {
                token.setTokenId(UUID.randomUUID());
            }
            return token;
        });
```

---

## Future Testing Improvements

Recommended next testing improvements:

```text
1. Add GitHub Actions CI to run backend tests on every push/PR
2. Add integration tests for key auth and trip flows
3. Add Testcontainers for MariaDB integration testing
4. Add controller tests with MockMvc
5. Add frontend tests later after frontend stabilizes
```
