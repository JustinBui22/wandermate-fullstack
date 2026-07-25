package com.example.travellingapp.security.filter;

import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.security.JsonAuthenticationEntryPoint;
import com.example.travellingapp.security.PublicEndpointMatcher;
import com.example.travellingapp.security.data_security.AuthenticatedUser;
import com.example.travellingapp.service.impl.TokenServiceImpl;
import com.example.travellingapp.response_template.CompleteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.filter.OncePerRequestFilter;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Log4j2
@Component
public class TokenFilter extends OncePerRequestFilter {

    private final TokenServiceImpl tokenServiceImpl;
    private final ErrorCodeRepository errorCodeRepository;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final PublicEndpointMatcher publicEndpointMatcher;
    private static final String ROLES_CLAIM = "roles";

    public TokenFilter(TokenServiceImpl tokenServiceImpl, ErrorCodeRepository errorCodeRepository, JsonAuthenticationEntryPoint authenticationEntryPoint, PublicEndpointMatcher publicEndpointMatcher) {
        this.tokenServiceImpl = tokenServiceImpl;
        this.errorCodeRepository = errorCodeRepository;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.publicEndpointMatcher = publicEndpointMatcher;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Skip token validation for non-required-authenticated URLs
        if (publicEndpointMatcher.matches(request)) {
            log.info("Skipping token validation for public URL: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        // Token validation for required-authenticated URLs
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                CompleteResponse<Object> validateTokenResponse = tokenServiceImpl.validateAccessToken(token);
                String responseCode = validateTokenResponse.getResponseBody().getCode();
                if (responseCode.equals(TOKEN_VERIFY_SUCCESS.getCode())) {
                    handleSuccessfulTokenValidation(request, response, filterChain, validateTokenResponse);
                } else {
                    log.warn("Token validation failed for reason: {}", responseCode);
                    handleFailTokenValidation(responseCode);
                }
            } catch (BusinessException e) {
                log.error("Business exception occurred: {}", e.getMessage(), e);
                handleBusinessException(response, e);
            } catch (Exception e) {
                log.error("There has been an error in {}!", this.getClass(), e);
                handleBusinessException(response, new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name()));
            }
        } else {
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new InsufficientAuthenticationException("Bearer access token is required")
            );
        }
    }

    private void handleSuccessfulTokenValidation(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, CompleteResponse<Object> validateTokenResponse)
            throws ServletException, IOException {
        // Populate SecurityContext with authenticated user
        Claims claims = (Claims) validateTokenResponse.getResponseBody().getBody();
        String userName = claims.getSubject();
        String sessionId = claims.get("sessionId", String.class);
        // Validate session token globally (fallback mechanism)
        String sessionToken = request.getHeader("Session-Token");
        if (sessionToken == null || sessionToken.isBlank() || sessionId == null || sessionId.isBlank() || tokenServiceImpl.isSessionTokenInvalid(userName, sessionId, sessionToken)) {
            log.error("Invalid session token for user: {}", userName);
            throw new BusinessException(SESSION_TOKEN_INVALID, TOKEN.name());
        }
        // All checks passed => populate SecurityContext
        // Extract the roles stored as Strings (authorities) from the claims
        List<GrantedAuthority> authorities = ((List<?>) claims.get(ROLES_CLAIM)).stream()
                .map(role -> new SimpleGrantedAuthority((String) role))
                .collect(Collectors.toList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(userName, sessionId), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);  // Allow the request to proceed
    }

    private void handleFailTokenValidation(String responseCode) {
        if (responseCode.equals(USER_NOT_FOUND.getCode())) {
            throw new BusinessException(USER_NOT_FOUND, TOKEN.name());
        } else if (responseCode.equals(TOKEN_EXPIRE.getCode())) {
            throw new BusinessException(TOKEN_EXPIRE, TOKEN.name());
        } else {
            throw new BusinessException(TOKEN_VERIFY_FAIL, TOKEN.name());
        }
    }

    private void handleBusinessException(HttpServletResponse response, BusinessException ex) throws IOException {
        CompleteResponse<Object> result = getCompleteResponse(errorCodeRepository, ex.getErrorCodeEnum(), ex.getFlow(), null);
        response.setStatus(HttpStatusCode.valueOf(result.getHttpCode()).value());
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result.getResponseBody()));
    }
}