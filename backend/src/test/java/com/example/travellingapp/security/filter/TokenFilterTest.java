package com.example.travellingapp.security.filter;

import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.security.JsonAuthenticationEntryPoint;
import com.example.travellingapp.service.impl.TokenServiceImpl;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.NON_AUTHENTICATED_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenFilterTest {

    @Test
    void doFilterInternal_shouldDelegateMissingBearerTokenToJsonEntryPoint()
            throws Exception {
        ConfigurationRepository configurationRepository =
                mock(ConfigurationRepository.class);
        JsonAuthenticationEntryPoint authenticationEntryPoint =
                mock(JsonAuthenticationEntryPoint.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(configurationRepository.findByConfigCode(
                NON_AUTHENTICATED_REQUEST.name()
        )).thenReturn(Optional.empty());

        TokenFilter filter = new TokenFilter(
                mock(TokenServiceImpl.class),
                configurationRepository,
                mock(ErrorCodeRepository.class),
                authenticationEntryPoint
        );

        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/Wandermate/api/v1/trips"
        );
        request.setContextPath("/Wandermate");
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
        ConfigurationRepository configurationRepository =
                mock(ConfigurationRepository.class);
        JsonAuthenticationEntryPoint authenticationEntryPoint =
                mock(JsonAuthenticationEntryPoint.class);
        FilterChain filterChain = mock(FilterChain.class);

        ConfigurationEntity publicUrls = new ConfigurationEntity();
        publicUrls.setConfigCode(NON_AUTHENTICATED_REQUEST.name());
        publicUrls.setConfigValue("/api/v1/health");
        when(configurationRepository.findByConfigCode(
                NON_AUTHENTICATED_REQUEST.name()
        )).thenReturn(Optional.of(publicUrls));

        TokenFilter filter = new TokenFilter(
                mock(TokenServiceImpl.class),
                configurationRepository,
                mock(ErrorCodeRepository.class),
                authenticationEntryPoint
        );

        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/Wandermate/api/v1/health"
        );
        request.setContextPath("/Wandermate");

        filter.doFilterInternal(
                request,
                new MockHttpServletResponse(),
                filterChain
        );

        verify(filterChain).doFilter(any(), any());
        verify(authenticationEntryPoint, never()).commence(any(), any(), any());
    }
}
