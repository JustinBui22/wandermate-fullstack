package com.example.travellingapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PublicEndpointMatcherTest {

    private final PublicEndpointMatcher publicEndpointMatcher =
            new PublicEndpointMatcher();

    @Test
    void matches_shouldAllowConfiguredPublicMethodAndPath() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/Wandermate/api/v1/users/login"
        );
        request.setContextPath("/Wandermate");
        request.setServletPath("/api/v1/users/login");

        assertThat(publicEndpointMatcher.matches(request)).isTrue();
    }

    @Test
    void matches_shouldRejectDifferentMethodForPublicPath() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/Wandermate/api/v1/users/login"
        );
        request.setContextPath("/Wandermate");
        request.setServletPath("/api/v1/users/login");

        assertThat(publicEndpointMatcher.matches(request)).isFalse();
    }

    @Test
    void matches_shouldAllowSwaggerResource() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/Wandermate/swagger-ui/index.html"
        );
        request.setContextPath("/Wandermate");
        request.setServletPath("/swagger-ui/index.html");

        assertThat(publicEndpointMatcher.matches(request)).isTrue();
    }

    @Test
    void matches_shouldAllowCorsPreflightRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "OPTIONS",
                "/Wandermate/api/v1/trips"
        );
        request.setContextPath("/Wandermate");
        request.setServletPath("/api/v1/trips");

        assertThat(publicEndpointMatcher.matches(request)).isTrue();
    }
}