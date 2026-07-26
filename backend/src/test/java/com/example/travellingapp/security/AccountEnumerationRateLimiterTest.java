package com.example.travellingapp.security;

import com.example.travellingapp.exception_handler.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.ACCOUNT_ENUMERATION_RATE_LIMITED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountEnumerationRateLimiterTest {

    @Test
    void checkAuthenticatedLookupAllowed_shouldRejectRequestsAfterConfiguredLimit() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-07-26T12:00:00Z")
        );
        AccountEnumerationRateLimiter rateLimiter = rateLimiter(clock);

        assertDoesNotThrow(() ->
                rateLimiter.checkAuthenticatedLookupAllowed("JustinBo123")
        );
        assertDoesNotThrow(() ->
                rateLimiter.checkAuthenticatedLookupAllowed("justinbo123")
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rateLimiter.checkAuthenticatedLookupAllowed("JUSTINBO123")
        );

        assertThat(exception.getErrorCodeEnum())
                .isEqualTo(ACCOUNT_ENUMERATION_RATE_LIMITED);
        assertThat(exception.getFlow()).isEqualTo(COMMON.name());
    }

    @Test
    void checkPublicAccountRequestAllowed_shouldUseSeparatePublicPolicy() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-07-26T12:00:00Z")
        );
        AccountEnumerationRateLimiter rateLimiter = rateLimiter(clock);

        assertDoesNotThrow(() ->
                rateLimiter.checkPublicAccountRequestAllowed("127.0.0.1")
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> rateLimiter.checkPublicAccountRequestAllowed("127.0.0.1")
        );

        assertThat(exception.getErrorCodeEnum())
                .isEqualTo(ACCOUNT_ENUMERATION_RATE_LIMITED);
    }

    @Test
    void checkAuthenticatedLookupAllowed_shouldResetLimitAfterWindowExpires() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-07-26T12:00:00Z")
        );
        AccountEnumerationRateLimiter rateLimiter = rateLimiter(clock);

        assertDoesNotThrow(() ->
                rateLimiter.checkAuthenticatedLookupAllowed("JustinBo123")
        );
        assertDoesNotThrow(() ->
                rateLimiter.checkAuthenticatedLookupAllowed("JustinBo123")
        );
        assertThrows(
                BusinessException.class,
                () -> rateLimiter.checkAuthenticatedLookupAllowed("JustinBo123")
        );

        clock.advance(Duration.ofSeconds(60));

        assertDoesNotThrow(() ->
                rateLimiter.checkAuthenticatedLookupAllowed("JustinBo123")
        );
    }

    private AccountEnumerationRateLimiter rateLimiter(MutableClock clock) {
        return new AccountEnumerationRateLimiter(
                new AccountEnumerationRateLimiter.RateLimitPolicy(
                        2,
                        Duration.ofSeconds(60)
                ),
                new AccountEnumerationRateLimiter.RateLimitPolicy(
                        1,
                        Duration.ofSeconds(60)
                ),
                clock
        );
    }

    private static final class MutableClock extends Clock {
        private Instant currentInstant;

        private MutableClock(Instant currentInstant) {
            this.currentInstant = currentInstant;
        }

        private void advance(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}
