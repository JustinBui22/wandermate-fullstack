# Testing Guide

This document summarizes the WanderMate backend test suite and how to run it.

## Test Result Snapshot

The latest uploaded project contains Maven Surefire reports showing:

```text
399 tests
0 failures
0 errors
0 skipped
```

This is strong backend test proof for a graduate/junior full-stack portfolio project.

## Run All Backend Tests

From backend folder:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw test
```

## Run One Test Class

```powershell
.\mvnw -Dtest=TripServiceImplTest test
```

## Run One Test Method

```powershell
.\mvnw -Dtest=TripServiceImplTest#updateTrip_shouldChangeFinishedTripToOngoing_whenEndDateIsExtendedIntoFuture test
```

## Test Stack

```text
JUnit 5
Mockito
AssertJ
Spring MockMvc
Maven Surefire
```

Most tests are unit/service/controller tests. They do not require a real database for the main coverage.

## Main Test Areas

### Auth/User/OTP

Covered areas:

```text
registration validation
duplicate username/email/phone checks
password hashing
login success/failure
max-session reached flow
max-session override flow
access token generation/validation
refresh token rotation
refresh token reuse detection
session token validation
logout/session revocation
forgot password with OTP
OTP send/verify retry and expiry rules
current profile retrieval
profile update
settings/theme update
```

### Trip/Destination/Activity

Covered areas:

```text
trip create/list/detail/update/delete
destination create/list/detail/update/delete
activity create/list/detail/update/delete
trip status recalculation
trip overlap warning and allowOverlap flow
trip date conflict with destinations
destination date conflict with activities
activity overlap hard error
createdBy/modifiedBy attribution
Cloudinary public ID metadata updates
image cleanup helper behaviour
```

### Collaboration

Covered areas:

```text
owner/editor/viewer access checks
trip member listing
member role update
member removal
owner cannot be removed
owner role cannot be manually assigned
invitation creation
invitation accept/reject
join request creation
join request accept/reject
duplicate request prevention
private overlap warning behaviour
collaboration summary count behaviour
```

### Share Codes

Covered areas:

```text
owner-only share-code generation/regeneration
active share-code retrieval
share-code preview
join request through share code
share-code expired/inactive/used/revoked validation
invalid share-code attempt restriction
```

### Image Upload

Covered areas:

```text
image upload service validation
image upload controller response mapping
Cloudinary client upload/delete behaviour
profile image cleanup
trip cover cleanup
cleanup failure does not fail main update
```

## Key Test Classes

Controller tests:

```text
ActivityControllerImplTest
DestinationControllerImplTest
HealthControllerImplTest
ImageUploadControllerImplTest
OtpControllerImplTest
TokenControllerImplTest
TripCollaborationControllerImplTest
TripControllerImplTest
TripMemberControllerImplTest
TripShareCodeControllerImplTest
UserControllerImplTest
```

Service tests:

```text
ActivityServiceImplTest
CloudinaryImageClientImplTest
DestinationServiceImplTest
EmailServiceImplTest
ImageUploadServiceImplTest
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
```

Validator tests:

```text
TripCollaborationRequestValidatorTest
TripShareCodeValidatorTest
```

## Frontend Typecheck

Frontend test command:

```bash
cd frontend
npm run typecheck
```

Latest replacement-file checks passed after the alert-callback and theme fixes.

## Context Load Test Note

A generated test like this:

```java
@SpringBootTest
class TheProjectApplicationTests {
    @Test
    void contextLoads() {}
}
```

can fail if no test database/profile is configured, because the app uses environment-based datasource properties:

```properties
spring.datasource.url=${DB_URL}
```

Acceptable options:

```text
Option A: remove generated contextLoads test if service/controller tests are the main proof
Option B: add application-test.properties and @ActiveProfiles("test")
Option C: add Testcontainers later for integration tests
```

## Recommended Manual Test Pass Before Demo

```text
1. Register or login as owner.
2. Create trip.
3. Upload trip cover.
4. Add destination.
5. Add activity.
6. Invite editor/viewer.
7. Accept invitation from another account.
8. Confirm viewer read-only mode.
9. Generate share code.
10. Submit/accept join request.
11. Upload profile avatar.
12. Toggle dark/light mode.
13. Test logout and session restore.
14. Test Render health endpoint.
```

## Future Testing Improvements

Not required for V4, but good later:

```text
Testcontainers for MariaDB integration tests
frontend component tests
frontend E2E tests with Detox/Maestro
CI badges/screenshots in README
coverage report
```
