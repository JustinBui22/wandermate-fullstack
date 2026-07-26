package com.example.travellingapp.security;

import com.example.travellingapp.exception_handler.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.ACCOUNT_ENUMERATION_RATE_LIMITED;

@Component
public class AccountEnumerationRateLimiter {

    private static final int MAX_TRACKED_KEYS_BEFORE_CLEANUP = 10000;
    private static final String AUTHENTICATED_LOOKUP_PREFIX = "lookup:";
    private static final String PUBLIC_ACCOUNT_REQUEST_PREFIX = "public:";

    private final RateLimitPolicy authenticatedLookupPolicy;
    private final RateLimitPolicy publicAccountRequestPolicy;
    private final Clock clock;
    private final ConcurrentHashMap<String, RequestWindow> requestWindows =
            new ConcurrentHashMap<>();

    @Autowired
    public AccountEnumerationRateLimiter(
            @Value("${app.security.account-enumeration.lookup.max-requests:20}") int lookupMaxRequests,
            @Value("${app.security.account-enumeration.lookup.window-seconds:60}") long lookupWindowSeconds,
            @Value("${app.security.account-enumeration.public.max-requests:10}") int publicMaxRequests,
            @Value("${app.security.account-enumeration.public.window-seconds:60}") long publicWindowSeconds
    ) {
        this(
                new RateLimitPolicy(lookupMaxRequests, Duration.ofSeconds(lookupWindowSeconds)),
                new RateLimitPolicy(publicMaxRequests, Duration.ofSeconds(publicWindowSeconds)),
                Clock.systemUTC()
        );
    }

    AccountEnumerationRateLimiter(
            RateLimitPolicy authenticatedLookupPolicy,
            RateLimitPolicy publicAccountRequestPolicy,
            Clock clock
    ) {
        this.authenticatedLookupPolicy = authenticatedLookupPolicy;
        this.publicAccountRequestPolicy = publicAccountRequestPolicy;
        this.clock = clock;
    }

    public void checkAuthenticatedLookupAllowed(String username) {
        checkAllowed(
                AUTHENTICATED_LOOKUP_PREFIX + username.toLowerCase(Locale.ROOT),
                authenticatedLookupPolicy
        );
    }

    public void checkPublicAccountRequestAllowed(String clientAddress) {
        checkAllowed(
                PUBLIC_ACCOUNT_REQUEST_PREFIX + clientAddress,
                publicAccountRequestPolicy
        );
    }

    private void checkAllowed(String key, RateLimitPolicy policy) {
        Instant now = clock.instant();
        AtomicBoolean allowed = new AtomicBoolean(false);

        requestWindows.compute(key, (requestKey, currentWindow) -> {
            if (currentWindow == null
                    || !now.isBefore(currentWindow.startedAt().plus(policy.windowDuration()))) {
                allowed.set(true);
                return new RequestWindow(now, 1, policy.windowDuration());
            }

            if (currentWindow.requestCount() >= policy.maxRequests()) {
                return currentWindow;
            }

            allowed.set(true);
            return new RequestWindow(
                    currentWindow.startedAt(),
                    currentWindow.requestCount() + 1,
                    policy.windowDuration()
            );
        });

        cleanExpiredWindowsIfRequired(now);

        if (!allowed.get()) {
            throw new BusinessException(ACCOUNT_ENUMERATION_RATE_LIMITED, COMMON.name());
        }
    }

    private void cleanExpiredWindowsIfRequired(Instant now) {
        if (requestWindows.size() <= MAX_TRACKED_KEYS_BEFORE_CLEANUP) {
            return;
        }

        requestWindows.entrySet().removeIf(entry ->
                !now.isBefore(
                        entry.getValue().startedAt().plus(entry.getValue().windowDuration())
                )
        );
    }

    record RateLimitPolicy(int maxRequests, Duration windowDuration) {
        RateLimitPolicy {
            if (maxRequests <= 0) {
                throw new IllegalArgumentException("Rate-limit max requests must be greater than zero");
            }
            if (windowDuration.isZero() || windowDuration.isNegative()) {
                throw new IllegalArgumentException("Rate-limit window must be greater than zero");
            }
        }
    }

    private record RequestWindow(
            Instant startedAt,
            int requestCount,
            Duration windowDuration
    ) {
    }
}
