package com.example.travellingapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AccountEnumerationRateLimitInterceptorTest {

    @Test
    void preHandle_shouldRateLimitByRemoteAddress() {
        AccountEnumerationRateLimiter rateLimiter =
                mock(AccountEnumerationRateLimiter.class);
        AccountEnumerationRateLimitInterceptor interceptor =
                new AccountEnumerationRateLimitInterceptor(rateLimiter);

        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/v1/users/register/verify"
        );
        request.setRemoteAddr("203.0.113.25");

        boolean allowed = interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                new Object()
        );

        assertThat(allowed).isTrue();
        verify(rateLimiter).checkPublicAccountRequestAllowed("203.0.113.25");
    }

    @Test
    void preHandle_shouldNotCountCorsPreflightRequests() {
        AccountEnumerationRateLimiter rateLimiter =
                mock(AccountEnumerationRateLimiter.class);
        AccountEnumerationRateLimitInterceptor interceptor =
                new AccountEnumerationRateLimitInterceptor(rateLimiter);

        MockHttpServletRequest request = new MockHttpServletRequest(
                "OPTIONS",
                "/api/v1/otp/send"
        );

        boolean allowed = interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                new Object()
        );

        assertThat(allowed).isTrue();
        verifyNoInteractions(rateLimiter);
    }

}
