# Testing Guide

This document summarizes the backend test suite and how to run it.

---

## Test Style

The current backend tests are mainly service/controller tests using:

```text
JUnit 5
Mockito
AssertJ
Spring MockMvc
Maven Surefire
```

These tests focus on business logic and controller behaviour without needing a real database connection for most cases.

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

Run one method:

```powershell
.\mvnw -Dtest=DestinationServiceImplTest#updateDestination_shouldUpdateDestination_whenUserCanEditAndNoOverlapOrActivityConflictExists test
```

---

## Current Passing Test Suite

The uploaded project contains Surefire reports showing:

```text
373 tests
0 failures
0 errors
0 skipped
```

Main test areas:

```text
Controller tests
Service tests
Validator tests
```

Test classes include:

```text
ActivityControllerImplTest
DestinationControllerImplTest
HealthControllerImplTest
OtpControllerImplTest
TokenControllerImplTest
TripCollaborationControllerImplTest
TripControllerImplTest
TripMemberControllerImplTest
TripShareCodeControllerImplTest
UserControllerImplTest

ActivityServiceImplTest
DestinationServiceImplTest
EmailServiceImplTest
OtpServiceImplTest
SmsServiceImplTest
TokenServiceImplTest
TripAccessServiceImplTest
TripCollaborationRequestServiceImplTest
TripMemberServiceImplTest
TripOverlapWarningServiceImplTest
TripServiceImplTest
TripShareCodeServiceImplTest
UserServiceImplTest

TripCollaborationRequestValidatorTest
TripShareCodeValidatorTest
```

---

## What Is Covered

### User/Auth/OTP

```text
- Register validation
- Duplicate username/email/phone checks
- Password hashing flow
- Login success/failure paths
- Max session flow integration
- Forgot password with OTP
- Logout flow
- User check flow
- Profile/settings retrieval and updates
- Access token generation and validation
- Refresh token rotation and reuse detection
- Session token validation/revocation
- OTP send/verify retry, expiry, destination mismatch, and consume-on-success
```

### Trip/Destination/Activity

```text
- Create/list/detail/update/delete trips
- Create/list/detail/update/delete destinations
- Create/list/detail/update/delete activities
- Owner/member access checks
- Trip/destination overlap warning
- allowOverlap flow
- Trip date conflict with existing destinations
- Destination date conflict with existing activities
- Activity overlap hard error
- createdBy/modifiedBy attribution behaviour
```

### Collaboration and Share Codes

```text
- Owner/editor/viewer access checks
- Trip member listing
- Member role updates
- Member removal
- Invitation creation and accept/reject
- Join request creation and accept/reject
- Duplicate request prevention
- Request stale-status handling
- Private overlap warning behaviour
- Owner-only share-code generation/regeneration
- Share-code trip preview
- Join request by share code
- Share-code role validation
- Collaboration summary counts
```

### Controller/API Tests

```text
- HTTP status mapping
- Request binding
- Success response mapping
- Business exception response mapping
- Authenticated endpoint controller behaviour
- Collaboration/share-code endpoint behaviour
```

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

Acceptable options:

```text
Option A: Remove the generated contextLoads test if service/controller tests are the main proof.
Option B: Add application-test.properties and @ActiveProfiles("test").
Option C: Use Testcontainers later for a real integration test setup.
```

---

## Common Test Maintenance Rule

When a service method validates several conditions, tests should usually expect the earliest meaningful business error in the service order.

Recommended order for update flows:

```text
1. Validate input
2. Check current user/session
3. Check trip access
4. Check entity exists
5. Check date/business conflicts
6. Load current user for attribution if save will happen
7. Save and map response
```

Do not load attribution-only data too early if it can hide the real business error.

---

## Future Testing Improvements

```text
1. Add integration tests for key auth and trip flows
2. Add Testcontainers for MariaDB integration testing
3. Add frontend tests after frontend stabilizes
4. Add end-to-end demo smoke checklist for V4 portfolio proof
```
