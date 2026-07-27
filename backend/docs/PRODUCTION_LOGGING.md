# Production Logging Policy

The production profile is designed to keep logs useful without exposing authentication secrets, one-time codes, private account data or cloud-storage references.

## Values that must never be logged

Application log statements must not include:

- `Authorization` header values;
- raw access, refresh or session tokens;
- session identifiers used by the token family;
- OTP values or stored OTP hashes;
- passwords or password-bearing request DTOs;
- email OAuth access tokens, refresh tokens, client secrets or request bodies;
- trip share codes;
- email addresses or phone numbers from OTP/account requests;
- Cloudinary secrets, secure asset URLs, public IDs or upload folders;
- complete request DTOs or entity objects that may contain user-provided data.

Security and account events should be logged as outcomes, error codes, resource IDs or statuses rather than raw credentials and personal values.

## Production profile controls

`application-prod.properties` applies the following controls:

```properties
spring.jpa.show-sql=false
spring.mvc.log-request-details=false
logging.level.org.hibernate.SQL=OFF
logging.level.org.hibernate.orm.jdbc.bind=OFF
logging.level.org.apache.hc.client5.http.headers=OFF
logging.level.org.apache.hc.client5.http.wire=OFF
logging.level.org.springframework.security=INFO
logging.level.com.example=INFO
```

These settings prevent request-detail logging, SQL value binding logs and Apache HTTP client header/wire dumps in production.

## Exception logging

Sensitive authentication, OTP, OAuth, email, share-code and Cloudinary paths log only a controlled event description and the exception class where useful. They do not log exception messages or request values because third-party exception messages can contain remote response details, recipients, SQL values or request data.

Unexpected API exceptions return the generic API error structure and log only the exception class.

## Automated guardrail

`ProductionLoggingSecurityTest` scans production Java log calls for known sensitive variables and request DTO names. It also verifies that the production profile keeps request, SQL binding and HTTP wire logging disabled.

Run:

```bash
cd backend
./mvnw test
```

A new sensitive log statement should cause the test suite to fail before deployment.
