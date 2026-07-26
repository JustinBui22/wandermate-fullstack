package com.example.travellingapp.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublicEndpointMatcher implements RequestMatcher {

    private final List<RequestMatcher> publicEndpointMatchers = List.of(
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/users/register"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/users/register/verify"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/users/login"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/users/forgot-password"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/auth/refresh"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/otp/send"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.POST, "/api/v1/otp/verify"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.GET, "/api/v1/health"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.GET, "/swagger-ui/**"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.GET, "/swagger-ui.html"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.GET, "/v3/api-docs/**"),
            PathPatternRequestMatcher.withDefaults()
                    .matcher(HttpMethod.OPTIONS, "/**")
    );

    @Override
    public boolean matches(HttpServletRequest request) {
        return publicEndpointMatchers.stream()
                .anyMatch(requestMatcher -> requestMatcher.matches(request));
    }
}