# Date and Time Model

WanderMate distinguishes calendar dates, local scheduled times and absolute operational timestamps.

## Trips and destinations

Trip and destination boundaries represent calendar days, not exact instants.

```java
LocalDate startDate;
LocalDate endDate;
```

API format:

```json
{
  "startDate": "2027-04-01",
  "endDate": "2027-04-05"
}
```

Database type: `DATE`.

Same-day trips and destinations are valid. Inclusive overlap logic is used.

## Activities

Activity schedules represent the local wall-clock time at the destination.

```java
LocalDateTime startDateTime;
LocalDateTime endDateTime;
```

API format:

```json
{
  "startDateTime": "2027-04-03T09:00:00",
  "endDateTime": "2027-04-03T11:00:00"
}
```

The value is not silently converted according to the viewer's device timezone.

## Audit, security and expiry timestamps

Created/modified values and token, OTP, share-code and restriction expiries use:

```java
Instant
```

API example:

```text
2027-04-03T01:30:00Z
```

The backend configures UTC JDBC and Jackson handling:

```properties
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jackson.time-zone=UTC
```

## Application calendar zone

Trip status calculations that require the current calendar day use:

```properties
app.time.default-zone=${APP_DEFAULT_TIME_ZONE:Australia/Adelaide}
```

## Migration rule

Flyway V5 changed trip and destination columns from `DATETIME` to `DATE`. Do not edit V5. Any later temporal schema change must use a new migration.
