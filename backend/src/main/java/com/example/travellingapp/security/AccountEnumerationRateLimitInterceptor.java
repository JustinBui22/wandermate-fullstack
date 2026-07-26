package com.example.travellingapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccountEnumerationRateLimitInterceptor implements HandlerInterceptor {

    private final AccountEnumerationRateLimiter accountEnumerationRateLimiter;

    public AccountEnumerationRateLimitInterceptor(
            AccountEnumerationRateLimiter accountEnumerationRateLimiter
    ) {
        this.accountEnumerationRateLimiter = accountEnumerationRateLimiter;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }

        String clientAddress = request.getRemoteAddr();
        accountEnumerationRateLimiter.checkPublicAccountRequestAllowed(
                clientAddress == null || clientAddress.isBlank()
                        ? "unknown"
                        : clientAddress
        );
        return true;
    }
}
