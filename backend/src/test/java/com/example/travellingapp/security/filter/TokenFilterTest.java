package com.example.travellingapp.security.filter;

import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.security.JsonAuthenticationEntryPoint;
import com.example.travellingapp.security.PublicEndpointMatcher;
import com.example.travellingapp.service.impl.TokenServiceImpl;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TokenFilterTest {

    @Test
    void doFilterInternal_shouldDelegateMissingBearerTokenToJsonEntryPoint()
            throws Exception {
        JsonAuthenticationEntryPoint authenticationEntryPoint =
                mock(JsonAuthenticationEntryPoint.class);
        FilterChain filterChain = mock(FilterChain.class);

        TokenFilter filter = new TokenFilter(
                mock(TokenServiceImpl.class),
                mock(ErrorCodeRepository.class),
                authenticationEntryPoint,
                new PublicEndpointMatcher()
        );

        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/Wandermate/api/v1/trips"
        );
        request.setContextPath("/Wandermate");
        request.setServletPath("/api/v1/trips");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationEntryPoint).commence(
                any(),
                any(),
                any(AuthenticationException.class)
        );
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_shouldSkipAuthenticationForConfiguredPublicUrl()
            throws Exception {
        JsonAuthenticationEntryPoint authenticationEntryPoint =
                mock(JsonAuthenticationEntryPoint.class);
        FilterChain filterChain = mock(FilterChain.class);

        TokenFilter filter = new TokenFilter(
                mock(TokenServiceImpl.class),
                mock(ErrorCodeRepository.class),
                authenticationEntryPoint,
                new PublicEndpointMatcher()
        );

        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/Wandermate/api/v1/health"
        );
        request.setContextPath("/Wandermate");
        request.setServletPath("/api/v1/health");

        filter.doFilterInternal(
                request,
                new MockHttpServletResponse(),
                filterChain
        );

        verify(filterChain).doFilter(any(), any());
        verify(authenticationEntryPoint, never()).commence(any(), any(), any());
    }

    @Test
    void doFilterInternal_shouldRequireAuthenticationForDifferentHttpMethod()
            throws Exception {
        JsonAuthenticationEntryPoint authenticationEntryPoint =
                mock(JsonAuthenticationEntryPoint.class);
        FilterChain filterChain = mock(FilterChain.class);

        TokenFilter filter = new TokenFilter(
                mock(TokenServiceImpl.class),
                mock(ErrorCodeRepository.class),
                authenticationEntryPoint,
                new PublicEndpointMatcher()
        );

        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/Wandermate/api/v1/health"
        );
        request.setContextPath("/Wandermate");
        request.setServletPath("/api/v1/health");

        filter.doFilterInternal(
                request,
                new MockHttpServletResponse(),
                filterChain
        );

        verify(authenticationEntryPoint).commence(
                any(),
                any(),
                any(AuthenticationException.class)
        );
        verify(filterChain, never()).doFilter(any(), any());
    }
}