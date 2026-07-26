package com.example.travellingapp.service.impl;

import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.RefreshTokenEntity;
import com.example.travellingapp.entity.SessionTokenEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.RefreshTokenRepository;
import com.example.travellingapp.repository.SessionTokenRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.DataSecurity;
import com.example.travellingapp.security.TokenSecretProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.LOGIN;
import static com.example.travellingapp.enums.CommonEnum.TOKEN;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionTokenRepository sessionTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DataSecurity dataSecurity;

    @Mock
    private TokenSecretProvider tokenSecretProvider;

    @Mock
    private RefreshTokenReuseServiceImpl refreshTokenReuseServiceImpl;

    private TokenServiceImpl tokenService;
    private SecretKey testJwtSigningKey;

    private static final String SECRET =
            "test-jwt-secret-that-is-at-least-sixty-four-bytes-long-for-hs512-signing-key";

    private static final String REFRESH_HASH_SECRET =
            "test-refresh-token-hash-secret-at-least-thirty-two-bytes";

    @BeforeEach
    void setUp() {
        testJwtSigningKey = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        tokenService = new TokenServiceImpl(
                configurationRepository,
                errorCodeRepository,
                refreshTokenRepository,
                userRepository,
                sessionTokenRepository,
                passwordEncoder,
                dataSecurity,
                tokenSecretProvider,
                refreshTokenReuseServiceImpl
        );
    }

    // -------------------------------------------------------------------------
    // generateAccessToken()
    // -------------------------------------------------------------------------

    @Test
    void generateAccessToken_shouldReturnJwt_whenUserExists() {
        String username = "JustinBo123";
        String sessionId = "session-123";

        mockConfig("ACCESS_TOKEN_EXPIRATION_TIME", "300000");
        mockConfig("PHONE_VN_PATTERN", "^(0|\\+84)[0-9]{9,10}$");
        mockErrorCode(TOKEN_GENERATE_SUCCESS, TOKEN.name());
        mockJwtSigningKey();

        when(userRepository.findByUsernameAndActive(username))
                .thenReturn(Optional.of(activeUser(username)));

        CompleteResponse<Object> response =
                tokenService.generateAccessToken(username, sessionId);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TOKEN_GENERATE_SUCCESS.getCode());

        String token = response.getResponseBody().getBody().toString();

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateAccessToken_shouldThrowInputFormatInvalid_whenUsernameLooksLikePhoneNumber() {
        String username = "0412345678";

        mockConfig("ACCESS_TOKEN_EXPIRATION_TIME", "300000");
        mockConfig("PHONE_VN_PATTERN", "^(0|\\+84)[0-9]{9,10}$");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.generateAccessToken(
                        username,
                        "session-123"
                )
        );

        assertBusinessException(
                exception,
                INPUT_FORMAT_INVALID,
                TOKEN.name()
        );

        verify(userRepository, never())
                .findByUsernameAndActive(anyString());
    }

    @Test
    void generateAccessToken_shouldThrowUserNotFound_whenUserDoesNotExist() {
        String username = "MissingUser";

        mockConfig("ACCESS_TOKEN_EXPIRATION_TIME", "300000");
        mockConfig("PHONE_VN_PATTERN", "^(0|\\+84)[0-9]{9,10}$");

        when(userRepository.findByUsernameAndActive(username))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.generateAccessToken(
                        username,
                        "session-123"
                )
        );

        assertBusinessException(
                exception,
                USER_NOT_FOUND,
                COMMON.name()
        );
    }

    @Test
    void tokenSecretProvider_shouldRejectJwtSecretThatIsTooShort() {
        assertThrows(
                IllegalStateException.class,
                () -> new TokenSecretProvider(
                        "short-secret",
                        REFRESH_HASH_SECRET,
                        "test-otp-hash-secret-at-least-thirty-two-bytes"
                )
        );
    }

    @Test
    void tokenSecretProvider_shouldRejectRefreshHashSecretThatIsTooShort() {
        assertThrows(
                IllegalStateException.class,
                () -> new TokenSecretProvider(
                        SECRET,
                        "short-refresh-secret",
                        "test-otp-hash-secret-at-least-thirty-two-bytes"
                )
        );
    }

    // -------------------------------------------------------------------------
    // generateRefreshToken()
    // -------------------------------------------------------------------------

    @Test
    void generateRefreshToken_shouldSaveHashedRefreshTokenAndReturnRawRefreshToken() {
        mockRefreshTokenSaveAssignsId();

        String username = "JustinBo123";
        String sessionId = "session-123";

        mockConfig("REFRESH_TOKEN_EXPIRATION_TIME", "1");
        mockErrorCode(TOKEN_GENERATE_SUCCESS, TOKEN.name());

        when(dataSecurity.hashData(anyString()))
                .thenAnswer(invocation ->
                        "hashed-" + invocation.getArgument(0)
                );

        CompleteResponse<Object> response =
                tokenService.generateRefreshToken(username, sessionId);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TOKEN_GENERATE_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body =
                (Map<String, Object>) response.getResponseBody().getBody();

        assertThat(body.get("refreshToken"))
                .isInstanceOf(String.class);
        assertThat(body.get("refreshToken").toString())
                .isNotBlank();
        assertThat(body.get("refreshTokenId"))
                .isNotNull();

        ArgumentCaptor<RefreshTokenEntity> tokenCaptor =
                ArgumentCaptor.forClass(RefreshTokenEntity.class);

        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshTokenEntity savedToken = tokenCaptor.getValue();

        assertThat(savedToken.getUsername())
                .isEqualTo(username);
        assertThat(savedToken.getSessionId())
                .isEqualTo(sessionId);
        assertThat(savedToken.getTokenHash())
                .startsWith("hashed-");
        assertThat(savedToken.isRevoked())
                .isFalse();
        assertThat(savedToken.getExpiredDate())
                .isAfter(LocalDateTime.now());
    }

    @Test
    void generateRefreshToken_shouldThrowInternalServerError_whenHashingFails() {
        mockConfig("REFRESH_TOKEN_EXPIRATION_TIME", "1");

        when(dataSecurity.hashData(anyString()))
                .thenThrow(
                        new BusinessException(
                                INTERNAL_SERVER_ERROR,
                                TOKEN.name()
                        )
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.generateRefreshToken(
                        "JustinBo123",
                        "session-123"
                )
        );

        assertBusinessException(
                exception,
                INTERNAL_SERVER_ERROR,
                TOKEN.name()
        );

        verify(refreshTokenRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // generateSessionToken()
    // -------------------------------------------------------------------------

    @Test
    void generateSessionToken_shouldStoreEncodedSessionTokenAndReturnRawSessionToken() {
        String username = "JustinBo123";
        String sessionId = "session-123";

        mockErrorCode(TOKEN_GENERATE_SUCCESS, TOKEN.name());

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-session-token");

        CompleteResponse<Object> response =
                tokenService.generateSessionToken(username, sessionId);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TOKEN_GENERATE_SUCCESS.getCode());

        String rawSessionToken =
                response.getResponseBody().getBody().toString();

        assertThat(rawSessionToken).isNotBlank();

        ArgumentCaptor<SessionTokenEntity> sessionCaptor =
                ArgumentCaptor.forClass(SessionTokenEntity.class);

        verify(sessionTokenRepository)
                .save(sessionCaptor.capture());

        SessionTokenEntity savedSession = sessionCaptor.getValue();

        assertThat(savedSession.getUsername())
                .isEqualTo(username);
        assertThat(savedSession.getSessionId())
                .isEqualTo(sessionId);
        assertThat(savedSession.getSessionToken())
                .isEqualTo("encoded-session-token");
    }

    @Test
    void generateSessionToken_shouldThrowInternalServerError_whenSessionSaveFails() {
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-session-token");

        doThrow(new DataIntegrityViolationException("DB down"))
                .when(sessionTokenRepository)
                .save(any(SessionTokenEntity.class));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.generateSessionToken(
                        "JustinBo123",
                        "session-123"
                )
        );

        assertBusinessException(
                exception,
                INTERNAL_SERVER_ERROR,
                COMMON.name()
        );
    }

    // -------------------------------------------------------------------------
    // validateAccessToken()
    // -------------------------------------------------------------------------

    @Test
    void validateAccessToken_shouldReturnTokenVerifySuccess_whenJwtIsValidAndUserExists() {
        String username = "JustinBo123";
        String token = buildJwt(
                username,
                "session-123",
                300000
        );

        mockErrorCode(TOKEN_VERIFY_SUCCESS, TOKEN.name());

        when(userRepository.findByUsernameAndActive(username))
                .thenReturn(Optional.of(activeUser(username)));

        CompleteResponse<Object> response =
                tokenService.validateAccessToken(token);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TOKEN_VERIFY_SUCCESS.getCode());

        assertThat(response.getResponseBody().getBody())
                .isInstanceOf(Claims.class);

        Claims claims =
                (Claims) response.getResponseBody().getBody();

        assertThat(claims.getSubject())
                .isEqualTo(username);
        assertThat(claims.get("sessionId"))
                .isEqualTo("session-123");
    }

    @Test
    void validateAccessToken_shouldReturnUserNotFound_whenJwtValidButUserDoesNotExist() {
        String username = "MissingUser";
        String token = buildJwt(
                username,
                "session-123",
                300000
        );

        mockErrorCode(USER_NOT_FOUND, TOKEN.name());

        when(userRepository.findByUsernameAndActive(username))
                .thenReturn(Optional.empty());

        CompleteResponse<Object> response =
                tokenService.validateAccessToken(token);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(USER_NOT_FOUND.getCode());
    }

    @Test
    void validateAccessToken_shouldReturnTokenExpire_whenJwtIsExpired() {
        String token = buildJwt(
                "JustinBo123",
                "session-123",
                -1000
        );

        mockErrorCode(TOKEN_EXPIRE, TOKEN.name());

        CompleteResponse<Object> response =
                tokenService.validateAccessToken(token);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TOKEN_EXPIRE.getCode());

        verify(userRepository, never())
                .findByUsernameAndActive(anyString());
    }

    @Test
    void validateAccessToken_shouldReturnTokenVerifyFail_whenJwtIsInvalid() {
        mockErrorCode(TOKEN_VERIFY_FAIL, TOKEN.name());
        mockJwtSigningKey();

        CompleteResponse<Object> response =
                tokenService.validateAccessToken("invalid.jwt.token");

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TOKEN_VERIFY_FAIL.getCode());
    }

    // -------------------------------------------------------------------------
    // isSessionTokenInvalid()
    // -------------------------------------------------------------------------

    @Test
    void isSessionTokenInvalid_shouldReturnTrue_whenSessionDoesNotExist() {
        when(sessionTokenRepository.findByUsernameAndSessionId(
                "JustinBo123",
                "session-123"
        )).thenReturn(Optional.empty());

        boolean result = tokenService.isSessionTokenInvalid(
                "JustinBo123",
                "session-123",
                "raw-session-token"
        );

        assertThat(result).isTrue();
    }

    @Test
    void isSessionTokenInvalid_shouldReturnFalse_whenSessionTokenMatches() {
        SessionTokenEntity session = session(
                "JustinBo123",
                "session-123",
                "encoded-session-token"
        );

        when(sessionTokenRepository.findByUsernameAndSessionId(
                "JustinBo123",
                "session-123"
        )).thenReturn(Optional.of(session));

        when(passwordEncoder.matches(
                "raw-session-token",
                "encoded-session-token"
        )).thenReturn(true);

        boolean result = tokenService.isSessionTokenInvalid(
                "JustinBo123",
                "session-123",
                "raw-session-token"
        );

        assertThat(result).isFalse();
    }

    @Test
    void isSessionTokenInvalid_shouldReturnTrue_whenSessionTokenDoesNotMatch() {
        SessionTokenEntity session = session(
                "JustinBo123",
                "session-123",
                "encoded-session-token"
        );

        when(sessionTokenRepository.findByUsernameAndSessionId(
                "JustinBo123",
                "session-123"
        )).thenReturn(Optional.of(session));

        when(passwordEncoder.matches(
                "wrong-token",
                "encoded-session-token"
        )).thenReturn(false);

        boolean result = tokenService.isSessionTokenInvalid(
                "JustinBo123",
                "session-123",
                "wrong-token"
        );

        assertThat(result).isTrue();
    }

    // -------------------------------------------------------------------------
    // revokeSessionTokenBySessionId()
    // -------------------------------------------------------------------------

    @Test
    void revokeSessionTokenBySessionId_shouldDeleteSession_whenTokenMatches() {
        SessionTokenEntity session = session(
                "JustinBo123",
                "session-123",
                "encoded-session-token"
        );

        when(sessionTokenRepository.findByUsernameAndSessionId(
                "JustinBo123",
                "session-123"
        )).thenReturn(Optional.of(session));

        when(passwordEncoder.matches(
                "raw-session-token",
                "encoded-session-token"
        )).thenReturn(true);

        tokenService.revokeSessionTokenBySessionId(
                "JustinBo123",
                "session-123",
                "raw-session-token"
        );

        verify(sessionTokenRepository).delete(session);
    }

    @Test
    void revokeSessionTokenBySessionId_shouldThrowSessionTokenInvalid_whenSessionDoesNotExist() {
        when(sessionTokenRepository.findByUsernameAndSessionId(
                "JustinBo123",
                "session-123"
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.revokeSessionTokenBySessionId(
                        "JustinBo123",
                        "session-123",
                        "raw-session-token"
                )
        );

        assertBusinessException(
                exception,
                SESSION_TOKEN_INVALID,
                TOKEN.name()
        );

        verify(sessionTokenRepository, never()).delete(any());
    }

    @Test
    void revokeSessionTokenBySessionId_shouldThrowSessionTokenInvalid_whenTokenDoesNotMatch() {
        SessionTokenEntity session = session(
                "JustinBo123",
                "session-123",
                "encoded-session-token"
        );

        when(sessionTokenRepository.findByUsernameAndSessionId(
                "JustinBo123",
                "session-123"
        )).thenReturn(Optional.of(session));

        when(passwordEncoder.matches(
                "wrong-token",
                "encoded-session-token"
        )).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.revokeSessionTokenBySessionId(
                        "JustinBo123",
                        "session-123",
                        "wrong-token"
                )
        );

        assertBusinessException(
                exception,
                SESSION_TOKEN_INVALID,
                TOKEN.name()
        );

        verify(sessionTokenRepository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // revokeActiveRefreshTokensBySessionId()
    // -------------------------------------------------------------------------

    @Test
    void revokeActiveRefreshTokensBySessionId_shouldDoNothing_whenNoActiveRefreshTokensExist() {
        when(refreshTokenRepository
                .findAllBySessionIdAndIsRevokedFalse("session-123"))
                .thenReturn(List.of());

        tokenService.revokeActiveRefreshTokensBySessionId(
                "session-123"
        );

        verify(refreshTokenRepository, never())
                .saveAll(anyList());
    }

    @Test
    void revokeActiveRefreshTokensBySessionId_shouldRevokeAndSaveAllActiveRefreshTokens() {
        RefreshTokenEntity token1 = refreshToken(
                "JustinBo123",
                "session-123",
                false,
                LocalDateTime.now().plusDays(1)
        );

        RefreshTokenEntity token2 = refreshToken(
                "JustinBo123",
                "session-123",
                false,
                LocalDateTime.now().plusDays(1)
        );

        when(refreshTokenRepository
                .findAllBySessionIdAndIsRevokedFalse("session-123"))
                .thenReturn(List.of(token1, token2));

        tokenService.revokeActiveRefreshTokensBySessionId(
                "session-123"
        );

        assertThat(token1.isRevoked()).isTrue();
        assertThat(token2.isRevoked()).isTrue();
        assertThat(token1.getRevokedDate()).isNotNull();
        assertThat(token2.getRevokedDate()).isNotNull();

        verify(refreshTokenRepository)
                .saveAll(List.of(token1, token2));
    }

    // -------------------------------------------------------------------------
    // revokeAllActiveRefreshTokensForUser()
    // -------------------------------------------------------------------------

    @Test
    void revokeAllActiveRefreshTokensForUser_shouldRevokeRefreshTokensAndDeleteEverySession() {
        RefreshTokenEntity token1 = refreshToken(
                "JustinBo123",
                "session-1",
                false,
                LocalDateTime.now().plusDays(1)
        );

        RefreshTokenEntity token2 = refreshToken(
                "JustinBo123",
                "session-2",
                false,
                LocalDateTime.now().plusDays(1)
        );

        when(refreshTokenRepository
                .findAllByUsernameAndIsRevokedFalse("JustinBo123"))
                .thenReturn(List.of(token1, token2));

        tokenService.revokeAllActiveRefreshTokensForUser(
                "JustinBo123"
        );

        assertThat(token1.isRevoked()).isTrue();
        assertThat(token2.isRevoked()).isTrue();
        assertThat(token1.getModifiedDate()).isNotNull();
        assertThat(token2.getModifiedDate()).isNotNull();
        assertThat(token1.getRevokedDate()).isNotNull();
        assertThat(token2.getRevokedDate()).isNotNull();

        verify(refreshTokenRepository)
                .saveAll(List.of(token1, token2));

        verify(sessionTokenRepository)
                .deleteAllByUsername("JustinBo123");
    }

    @Test
    void revokeAllActiveRefreshTokensForUser_shouldDeleteSessions_whenNoRefreshTokensExist() {
        when(refreshTokenRepository
                .findAllByUsernameAndIsRevokedFalse("JustinBo123"))
                .thenReturn(List.of());

        tokenService.revokeAllActiveRefreshTokensForUser(
                "JustinBo123"
        );

        verify(refreshTokenRepository, never())
                .saveAll(anyList());

        verify(sessionTokenRepository)
                .deleteAllByUsername("JustinBo123");
    }

    // -------------------------------------------------------------------------
    // checkMaxActiveSessions()
    // -------------------------------------------------------------------------

    @Test
    void checkMaxActiveSessions_shouldDoNothing_whenActiveSessionsBelowMax() {
        mockConfig("MAX_ALLOWED_SESSIONS", "3");

        when(sessionTokenRepository
                .findAllByUsernameOrderByCreatedDateAsc("JustinBo123"))
                .thenReturn(List.of(
                        session(
                                "JustinBo123",
                                "session-1",
                                "encoded-1"
                        ),
                        session(
                                "JustinBo123",
                                "session-2",
                                "encoded-2"
                        )
                ));

        tokenService.checkMaxActiveSessions(
                "JustinBo123",
                false
        );

        verify(sessionTokenRepository, never()).delete(any());

        verify(refreshTokenRepository, never())
                .findAllBySessionIdAndIsRevokedFalse(anyString());
    }

    @Test
    void checkMaxActiveSessions_shouldThrowMaxSessionsReached_whenAtMaxAndOverrideFalse() {
        mockConfig("MAX_ALLOWED_SESSIONS", "2");

        when(sessionTokenRepository
                .findAllByUsernameOrderByCreatedDateAsc("JustinBo123"))
                .thenReturn(List.of(
                        session(
                                "JustinBo123",
                                "session-1",
                                "encoded-1"
                        ),
                        session(
                                "JustinBo123",
                                "session-2",
                                "encoded-2"
                        )
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.checkMaxActiveSessions(
                        "JustinBo123",
                        false
                )
        );

        assertBusinessException(
                exception,
                MAX_SESSIONS_REACHED,
                LOGIN.name()
        );

        verify(sessionTokenRepository, never()).delete(any());
    }

    @Test
    void checkMaxActiveSessions_shouldDeleteOldestSessionsUntilBelowMax_whenOverrideTrue() {
        mockConfig("MAX_ALLOWED_SESSIONS", "2");

        SessionTokenEntity oldest = session(
                "JustinBo123",
                "session-1",
                "encoded-1"
        );

        SessionTokenEntity newer = session(
                "JustinBo123",
                "session-2",
                "encoded-2"
        );

        RefreshTokenEntity oldRefreshToken = refreshToken(
                "JustinBo123",
                "session-1",
                false,
                LocalDateTime.now().plusDays(1)
        );

        when(sessionTokenRepository
                .findAllByUsernameOrderByCreatedDateAsc("JustinBo123"))
                .thenReturn(new ArrayList<>(List.of(oldest, newer)));

        when(refreshTokenRepository
                .findAllBySessionIdAndIsRevokedFalse("session-1"))
                .thenReturn(List.of(oldRefreshToken));

        tokenService.checkMaxActiveSessions(
                "JustinBo123",
                true
        );

        assertThat(oldRefreshToken.isRevoked()).isTrue();
        assertThat(oldRefreshToken.getRevokedDate()).isNotNull();

        verify(refreshTokenRepository)
                .saveAll(List.of(oldRefreshToken));

        verify(sessionTokenRepository).delete(oldest);
        verify(sessionTokenRepository, never()).delete(newer);
    }

    @Test
    void checkMaxActiveSessions_shouldThrowInvalidConfig_whenMaxAllowedSessionsIsZero() {
        mockConfig("MAX_ALLOWED_SESSIONS", "0");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.checkMaxActiveSessions(
                        "JustinBo123",
                        false
                )
        );

        assertBusinessException(
                exception,
                INVALID_CONFIG,
                COMMON.name()
        );

        verify(sessionTokenRepository, never())
                .findAllByUsernameOrderByCreatedDateAsc(anyString());
    }

    @Test
    void checkMaxActiveSessions_shouldThrowInputFormatInvalid_whenMaxAllowedSessionsIsNotNumber() {
        mockConfig("MAX_ALLOWED_SESSIONS", "abc");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.checkMaxActiveSessions(
                        "JustinBo123",
                        false
                )
        );

        assertBusinessException(
                exception,
                INPUT_FORMAT_INVALID,
                COMMON.name()
        );

        verify(sessionTokenRepository, never())
                .findAllByUsernameOrderByCreatedDateAsc(anyString());
    }

    // -------------------------------------------------------------------------
    // refreshAccessToken()
    // -------------------------------------------------------------------------

    @Test
    void refreshAccessToken_shouldThrowRefreshTokenInvalid_whenRefreshTokenIsBlank() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.refreshAccessToken(
                        "   ",
                        "session-token"
                )
        );

        assertBusinessException(
                exception,
                REFRESH_TOKEN_INVALID,
                TOKEN.name()
        );
    }

    @Test
    void refreshAccessToken_shouldThrowSessionTokenInvalid_whenSessionTokenIsBlank() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.refreshAccessToken(
                        "refresh-token",
                        "   "
                )
        );

        assertBusinessException(
                exception,
                SESSION_TOKEN_INVALID,
                TOKEN.name()
        );
    }

    @Test
    void refreshAccessToken_shouldThrowRefreshTokenInvalid_whenRefreshTokenHashNotFound() {
        when(dataSecurity.hashData("refresh-token"))
                .thenReturn("hashed-refresh-token");

        when(refreshTokenRepository
                .findByTokenHashForUpdate("hashed-refresh-token"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.refreshAccessToken(
                        "refresh-token",
                        "session-token"
                )
        );

        assertBusinessException(
                exception,
                REFRESH_TOKEN_INVALID,
                TOKEN.name()
        );
    }

    @Test
    void refreshAccessToken_shouldRevokeCompromisedSession_whenRevokedTokenIsPresented() {
        RefreshTokenEntity revokedToken = refreshToken(
                "JustinBo123",
                "session-123",
                true,
                LocalDateTime.now().plusDays(1)
        );

        when(dataSecurity.hashData("refresh-token"))
                .thenReturn("hashed-refresh-token");

        when(refreshTokenRepository
                .findByTokenHashForUpdate("hashed-refresh-token"))
                .thenReturn(Optional.of(revokedToken));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.refreshAccessToken(
                        "refresh-token",
                        "session-token"
                )
        );

        assertBusinessException(
                exception,
                REFRESH_TOKEN_INVALID,
                TOKEN.name()
        );

        verify(refreshTokenReuseServiceImpl)
                .revokeCompromisedSession(revokedToken);
    }

    @Test
    void refreshAccessToken_shouldReturnRefreshTokenExpired_whenRefreshTokenIsExpired() {
        RefreshTokenEntity expiredToken = refreshToken(
                "JustinBo123",
                "session-123",
                false,
                LocalDateTime.now().minusMinutes(1)
        );

        SessionTokenEntity session = session(
                "JustinBo123",
                "session-123",
                "encoded-session-token"
        );

        mockErrorCode(REFRESH_TOKEN_EXPIRED, TOKEN.name());

        when(dataSecurity.hashData("refresh-token"))
                .thenReturn("hashed-refresh-token");

        when(refreshTokenRepository
                .findByTokenHashForUpdate("hashed-refresh-token"))
                .thenReturn(Optional.of(expiredToken));

        when(sessionTokenRepository.findByUsernameAndSessionId(
                "JustinBo123",
                "session-123"
        )).thenReturn(Optional.of(session));

        when(passwordEncoder.matches(
                "session-token",
                "encoded-session-token"
        )).thenReturn(true);

        CompleteResponse<Object> response =
                tokenService.refreshAccessToken(
                        "refresh-token",
                        "session-token"
                );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(REFRESH_TOKEN_EXPIRED.getCode());

        assertThat(expiredToken.isRevoked()).isTrue();

        verify(refreshTokenRepository).save(expiredToken);
        verify(sessionTokenRepository).delete(session);
    }

    @Test
    void refreshAccessToken_shouldThrowSessionTokenInvalid_whenSessionTokenDoesNotMatch() {
        RefreshTokenEntity activeToken = refreshToken(
                "JustinBo123",
                "session-123",
                false,
                LocalDateTime.now().plusDays(1)
        );

        SessionTokenEntity session = session(
                "JustinBo123",
                "session-123",
                "encoded-session-token"
        );

        when(dataSecurity.hashData("refresh-token"))
                .thenReturn("hashed-refresh-token");

        when(refreshTokenRepository
                .findByTokenHashForUpdate("hashed-refresh-token"))
                .thenReturn(Optional.of(activeToken));

        when(sessionTokenRepository.findByUsernameAndSessionId(
                "JustinBo123",
                "session-123"
        )).thenReturn(Optional.of(session));

        when(passwordEncoder.matches(
                "wrong-session-token",
                "encoded-session-token"
        )).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.refreshAccessToken(
                        "refresh-token",
                        "wrong-session-token"
                )
        );

        assertBusinessException(
                exception,
                SESSION_TOKEN_INVALID,
                TOKEN.name()
        );

    }

    @Test
    void refreshAccessToken_shouldRotateRefreshTokenAndReturnNewAccessAndRefreshToken_whenValid() {
        mockRefreshTokenSaveAssignsId();

        String username = "JustinBo123";
        String sessionId = "session-123";
        String oldRefreshTokenRaw = "old-refresh-token";
        String sessionTokenRaw = "session-token";

        RefreshTokenEntity oldRefreshToken = refreshToken(
                username,
                sessionId,
                false,
                LocalDateTime.now().plusDays(1)
        );

        SessionTokenEntity session = session(
                username,
                sessionId,
                "encoded-session-token"
        );

        mockConfig(
                "PHONE_VN_PATTERN",
                "^(0|\\+84)[0-9]{9,10}$"
        );
        mockConfig(
                "ACCESS_TOKEN_EXPIRATION_TIME",
                "300000"
        );
        mockConfig(
                "REFRESH_TOKEN_EXPIRATION_TIME",
                "1"
        );

        mockErrorCode(TOKEN_GENERATE_SUCCESS, TOKEN.name());
        mockJwtSigningKey();

        when(dataSecurity.hashData(anyString()))
                .thenAnswer(invocation ->
                        "hashed-" + invocation.getArgument(0)
                );

        when(refreshTokenRepository.findByTokenHashForUpdate(
                "hashed-" + oldRefreshTokenRaw
        )).thenReturn(Optional.of(oldRefreshToken));

        when(sessionTokenRepository.findByUsernameAndSessionId(
                username,
                sessionId
        )).thenReturn(Optional.of(session));

        when(passwordEncoder.matches(
                sessionTokenRaw,
                "encoded-session-token"
        )).thenReturn(true);

        when(userRepository.findByUsernameAndActive(username))
                .thenReturn(Optional.of(activeUser(username)));

        CompleteResponse<Object> response =
                tokenService.refreshAccessToken(
                        oldRefreshTokenRaw,
                        sessionTokenRaw
                );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TOKEN_GENERATE_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body =
                (Map<String, String>) response.getResponseBody().getBody();

        assertThat(body.get("accessToken")).isNotBlank();
        assertThat(body.get("refreshToken")).isNotBlank();
        assertThat(body.get("refreshToken"))
                .isNotEqualTo(oldRefreshTokenRaw);

        assertThat(oldRefreshToken.isRevoked()).isTrue();
        assertThat(oldRefreshToken.getReplacedByTokenId())
                .isNotNull();

        verify(refreshTokenRepository, atLeastOnce())
                .save(oldRefreshToken);

        verify(refreshTokenRepository, atLeast(2))
                .save(any(RefreshTokenEntity.class));
    }

    @Test
    void refreshAccessToken_shouldPropagateInternalError_whenRefreshHashingFails() {
        when(dataSecurity.hashData("refresh-token"))
                .thenThrow(
                        new BusinessException(
                                INTERNAL_SERVER_ERROR,
                                TOKEN.name()
                        )
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.refreshAccessToken(
                        "refresh-token",
                        "session-token"
                )
        );

        assertBusinessException(
                exception,
                INTERNAL_SERVER_ERROR,
                TOKEN.name()
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User activeUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setEmail(username.toLowerCase() + "@example.com");
        user.setPhoneNumber("0412345678");
        user.setActive(true);
        return user;
    }

    private SessionTokenEntity session(
            String username,
            String sessionId,
            String encodedToken
    ) {
        SessionTokenEntity session = new SessionTokenEntity();
        session.setUsername(username);
        session.setSessionId(sessionId);
        session.setSessionToken(encodedToken);
        session.setCreatedDate(LocalDateTime.now());
        return session;
    }

    private RefreshTokenEntity refreshToken(
            String username,
            String sessionId,
            boolean revoked,
            LocalDateTime expiredDate
    ) {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setTokenId(UUID.randomUUID());
        token.setUsername(username);
        token.setSessionId(sessionId);
        token.setTokenHash("hashed-refresh-token");
        token.setRevoked(revoked);
        token.setCreatedDate(LocalDateTime.now().minusHours(1));
        token.setExpiredDate(expiredDate);
        return token;
    }

    private String buildJwt(
            String username,
            String sessionId,
            long expiryOffsetMillis
    ) {
        mockJwtSigningKey();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .claim("sessionId", sessionId)
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expiryOffsetMillis
                        )
                )
                .signWith(testJwtSigningKey)
                .compact();
    }

    private void mockJwtSigningKey() {
        when(tokenSecretProvider.getJwtSigningKey())
                .thenReturn(testJwtSigningKey);
    }

    private void mockConfig(
            String configCode,
            String configValue
    ) {
        ConfigurationEntity entity = new ConfigurationEntity();
        entity.setConfigCode(configCode);
        entity.setConfigValue(configValue);
        entity.setCreatedDate(LocalDateTime.now());

        when(configurationRepository.findByConfigCode(configCode))
                .thenReturn(Optional.of(entity));
    }

    private void mockErrorCode(
            ErrorCodeEnum errorCodeEnum,
            String flow
    ) {
        ErrorCodeEntity entity = new ErrorCodeEntity();
        entity.setErrorCode(errorCodeEnum.getCode());
        entity.setErrorMessage(errorCodeEnum.getMessage());
        entity.setErrorEnum(errorCodeEnum.name());
        entity.setFlow(flow);
        entity.setCreatedDate(LocalDateTime.now());

        when(errorCodeRepository.findByErrorEnumAndFlow(
                errorCodeEnum.name(),
                flow
        )).thenReturn(Optional.of(entity));
    }

    private void assertBusinessException(
            BusinessException exception,
            ErrorCodeEnum expectedErrorCode,
            String expectedFlow
    ) {
        assertThat(exception.getErrorCodeEnum())
                .isEqualTo(expectedErrorCode);
        assertThat(exception.getFlow())
                .isEqualTo(expectedFlow);
    }

    private void mockRefreshTokenSaveAssignsId() {
        when(refreshTokenRepository.save(
                any(RefreshTokenEntity.class)
        )).thenAnswer(invocation -> {
            RefreshTokenEntity token =
                    invocation.getArgument(0);

            if (token.getTokenId() == null) {
                token.setTokenId(UUID.randomUUID());
            }

            return token;
        });
    }
}
