package com.example.travellingapp.integration;

import com.example.travellingapp.entity.RefreshTokenEntity;
import com.example.travellingapp.entity.SessionTokenEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.RefreshTokenRepository;
import com.example.travellingapp.repository.SessionTokenRepository;
import com.example.travellingapp.security.data_security.DataSecurity;
import com.example.travellingapp.security.TokenSecretProvider;
import com.example.travellingapp.service.impl.RefreshTokenReuseServiceImpl;
import com.example.travellingapp.service.impl.TokenServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.travellingapp.enums.ErrorCodeEnum.REFRESH_TOKEN_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({
        TokenServiceImpl.class,
        RefreshTokenReuseServiceImpl.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class RefreshTokenReuseTransactionIntegrationTest {

    private static final String USERNAME = "JustinBo123";
    private static final String SESSION_ID = "session-1";

    @Autowired
    private TokenServiceImpl tokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SessionTokenRepository sessionTokenRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private DataSecurity dataSecurity;

    @MockitoBean
    private TokenSecretProvider tokenSecretProvider;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        sessionTokenRepository.deleteAll();
    }

    @Test
    void reusedRefreshToken_commitsReuseDetectionAndSessionRevocationBeforeErrorResponse() {
        RefreshTokenEntity reusedToken = refreshToken(
                true,
                "reused-hash",
                SESSION_ID
        );

        RefreshTokenEntity activeSuccessor = refreshToken(
                false,
                "successor-hash",
                SESSION_ID
        );

        RefreshTokenEntity otherSessionToken = refreshToken(
                false,
                "other-hash",
                "session-2"
        );

        refreshTokenRepository.saveAllAndFlush(
                List.of(
                        reusedToken,
                        activeSuccessor,
                        otherSessionToken
                )
        );

        sessionTokenRepository.saveAllAndFlush(
                List.of(
                        new SessionTokenEntity(
                                USERNAME,
                                "encoded-session-token",
                                SESSION_ID,
                                LocalDateTime.now()
                        ),
                        new SessionTokenEntity(
                                USERNAME,
                                "encoded-other-token",
                                "session-2",
                                LocalDateTime.now()
                        )
                )
        );

        when(dataSecurity.hashData("reused-refresh-token"))
                .thenReturn("reused-hash");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.refreshAccessToken(
                        "reused-refresh-token",
                        "incorrect-session-token"
                )
        );

        assertThat(exception.getErrorCodeEnum())
                .isEqualTo(REFRESH_TOKEN_INVALID);

        /*
         * Once reuse is confirmed, the client-provided session token must not
         * decide whether the security response is executed.
         */
        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        entityManager.clear();

        RefreshTokenEntity persistedReusedToken =
                refreshTokenRepository
                        .findByTokenHash("reused-hash")
                        .orElseThrow();

        RefreshTokenEntity persistedSuccessor =
                refreshTokenRepository
                        .findByTokenHash("successor-hash")
                        .orElseThrow();

        RefreshTokenEntity persistedOtherSessionToken =
                refreshTokenRepository
                        .findByTokenHash("other-hash")
                        .orElseThrow();

        assertThat(persistedReusedToken.isReuseDetected())
                .isTrue();

        assertThat(persistedSuccessor.isRevoked())
                .isTrue();

        assertThat(persistedSuccessor.getRevokedDate())
                .isNotNull();

        assertThat(
                sessionTokenRepository.findByUsernameAndSessionId(
                        USERNAME,
                        SESSION_ID
                )
        ).isEmpty();

        /*
         * An unrelated device/session belonging to the same user remains
         * active.
         */
        assertThat(persistedOtherSessionToken.isRevoked())
                .isFalse();

        assertThat(
                sessionTokenRepository.findByUsernameAndSessionId(
                        USERNAME,
                        "session-2"
                )
        ).isPresent();
    }

    @Test
    void reusedRefreshToken_withoutStoredSession_stillRevokesTheTokenFamily() {
        RefreshTokenEntity reusedToken = refreshToken(
                true,
                "reused-without-session-hash",
                SESSION_ID
        );

        RefreshTokenEntity activeSuccessor = refreshToken(
                false,
                "successor-without-session-hash",
                SESSION_ID
        );

        refreshTokenRepository.saveAllAndFlush(
                List.of(reusedToken, activeSuccessor)
        );

        when(dataSecurity.hashData(
                "reused-refresh-token-without-session"
        )).thenReturn("reused-without-session-hash");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenService.refreshAccessToken(
                        "reused-refresh-token-without-session",
                        "incorrect-session-token"
                )
        );

        assertThat(exception.getErrorCodeEnum())
                .isEqualTo(REFRESH_TOKEN_INVALID);

        entityManager.clear();

        RefreshTokenEntity persistedReusedToken =
                refreshTokenRepository
                        .findByTokenHash("reused-without-session-hash")
                        .orElseThrow();

        RefreshTokenEntity persistedSuccessor =
                refreshTokenRepository
                        .findByTokenHash("successor-without-session-hash")
                        .orElseThrow();

        assertThat(persistedReusedToken.isReuseDetected())
                .isTrue();

        assertThat(persistedSuccessor.isRevoked())
                .isTrue();

        assertThat(persistedSuccessor.getRevokedDate())
                .isNotNull();

        assertThat(
                sessionTokenRepository.findByUsernameAndSessionId(
                        USERNAME,
                        SESSION_ID
                )
        ).isEmpty();
    }

    private static RefreshTokenEntity refreshToken(
            boolean revoked,
            String hash,
            String sessionId
    ) {
        return new RefreshTokenEntity(
                revoked,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                USERNAME,
                hash,
                sessionId,
                null
        );
    }
}