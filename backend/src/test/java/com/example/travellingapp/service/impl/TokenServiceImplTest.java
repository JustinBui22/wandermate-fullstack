package com.example.travellingapp.service.impl;

import com.example.travellingapp.entity.RefreshTokenEntity;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.RefreshTokenRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenServiceImplTest {

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Setup mock request with a dummy session token
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Session-Token", "dummy-session-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void testValidateRefreshToken_Success() {
        String username = "testuser";
        String refreshToken = "valid-refresh-token";

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUsername(username);
        entity.setTokenHash(refreshToken);
        entity.setRevoked(false);
        entity.setExpiredDate(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByUsername(username)).thenReturn(Optional.of(entity));

        CompleteResponse<Object> response = tokenService.refreshAccessToken(refreshToken, "dummy-session-token");

        assertEquals(TOKEN_VERIFY_SUCCESS.getCode(), response.getResponseBody().getCode());
    }

    @Test
    void testValidateRefreshToken_TokenExpired() {
        String username = "testuser";
        String refreshToken = "valid-refresh-token";

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUsername(username);
        entity.setTokenHash(refreshToken);
        entity.setRevoked(false);
        entity.setExpiredDate(LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByUsername(username)).thenReturn(Optional.of(entity));

        CompleteResponse<Object> response = tokenService.refreshAccessToken(refreshToken, "dummy-session-token");

        assertEquals(REFRESH_TOKEN_EXPIRED.getCode(), response.getResponseBody().getCode());
    }

    @Test
    void testValidateRefreshToken_TokenRevoked() {
        String username = "testuser";
        String refreshToken = "valid-refresh-token";

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUsername(username);
        entity.setTokenHash(refreshToken);
        entity.setRevoked(true);
        entity.setExpiredDate(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByUsername(username)).thenReturn(Optional.of(entity));

        CompleteResponse<Object> response = tokenService.refreshAccessToken(refreshToken, "dummy-session-token");

        assertEquals(REFRESH_TOKEN_INVALID.getCode(), response.getResponseBody().getCode());
    }

    @Test
    void testValidateRefreshToken_TokenMismatch() {
        String username = "testuser";
        String refreshToken = "wrong-token";

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUsername(username);
        entity.setTokenHash("actual-token");
        entity.setRevoked(false);
        entity.setExpiredDate(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByUsername(username)).thenReturn(Optional.of(entity));

        CompleteResponse<Object> response = tokenService.refreshAccessToken(refreshToken,"dummy-session-token");

        assertEquals(REFRESH_TOKEN_INVALID.getCode(), response.getResponseBody().getCode());
    }

    @Test
    void testValidateRefreshToken_UserNotFound() {
        String username = "nonexistent";
        String refreshToken = "some-token";

        when(refreshTokenRepository.findByUsername(username)).thenReturn(Optional.empty());

        CompleteResponse<Object> response = tokenService.refreshAccessToken(refreshToken, "dummy-session-token");

        assertEquals(REFRESH_TOKEN_INVALID.getCode(), response.getResponseBody().getCode());
    }
}
