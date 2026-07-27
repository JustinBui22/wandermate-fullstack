package com.example.travellingapp.integration;

import com.example.travellingapp.entity.RefreshTokenEntity;
import com.example.travellingapp.entity.SessionTokenEntity;
import com.example.travellingapp.repository.RefreshTokenRepository;
import com.example.travellingapp.repository.SessionTokenRepository;
import com.example.travellingapp.security.data_security.DataSecurity;
import com.example.travellingapp.security.TokenSecretProvider;
import com.example.travellingapp.service.impl.RefreshTokenReuseServiceImpl;
import com.example.travellingapp.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TokenServiceImpl.class)
class TokenRevocationIntegrationTest {

    private static final String USERNAME = "JustinBo123";

    @Autowired
    private TokenServiceImpl tokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SessionTokenRepository sessionTokenRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private DataSecurity dataSecurity;

    @MockitoBean
    private TokenSecretProvider tokenSecretProvider;

    @MockitoBean
    private RefreshTokenReuseServiceImpl refreshTokenReuseServiceImpl;

    @Test
    void revokeAllActiveRefreshTokensForUser_revokesRefreshTokensAndDeletesOnlyThatUsersSessions() {
        RefreshTokenEntity token1 = activeRefreshToken(USERNAME, "session-1", "hash-1");
        RefreshTokenEntity token2 = activeRefreshToken(USERNAME, "session-2", "hash-2");
        refreshTokenRepository.saveAllAndFlush(List.of(token1, token2));

        sessionTokenRepository.saveAllAndFlush(List.of(
                session(USERNAME, "session-1", "encoded-session-1"),
                session(USERNAME, "session-2", "encoded-session-2"),
                session("OtherUser", "session-3", "encoded-session-3")
        ));

        tokenService.revokeAllActiveRefreshTokensForUser(USERNAME);
        refreshTokenRepository.flush();
        sessionTokenRepository.flush();

        List<RefreshTokenEntity> persistedTokens = refreshTokenRepository.findAll();
        assertThat(persistedTokens)
                .filteredOn(token -> USERNAME.equals(token.getUsername()))
                .allSatisfy(token -> {
                    assertThat(token.isRevoked()).isTrue();
                    assertThat(token.getModifiedDate()).isNotNull();
                    assertThat(token.getRevokedDate()).isNotNull();
                });

        assertThat(sessionTokenRepository.findAllByUsernameOrderByCreatedDateAsc(USERNAME))
                .isEmpty();
        assertThat(sessionTokenRepository.findAllByUsernameOrderByCreatedDateAsc("OtherUser"))
                .hasSize(1);
    }

    @Test
    void revokeAllActiveRefreshTokensForUser_deletesSessionsWhenNoRefreshTokensExist() {
        sessionTokenRepository.saveAllAndFlush(List.of(
                session(USERNAME, "session-1", "encoded-session-1"),
                session(USERNAME, "session-2", "encoded-session-2")
        ));

        tokenService.revokeAllActiveRefreshTokensForUser(USERNAME);
        sessionTokenRepository.flush();

        assertThat(refreshTokenRepository.findAllByUsernameAndIsRevokedFalse(USERNAME))
                .isEmpty();
        assertThat(sessionTokenRepository.findAllByUsernameOrderByCreatedDateAsc(USERNAME))
                .isEmpty();
    }

    private static RefreshTokenEntity activeRefreshToken(
            String username,
            String sessionId,
            String tokenHash
    ) {
        return new RefreshTokenEntity(
                false,
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.DAYS),
                username,
                tokenHash,
                sessionId,
                null
        );
    }

    private static SessionTokenEntity session(
            String username,
            String sessionId,
            String encodedToken
    ) {
        return new SessionTokenEntity(username, encodedToken, sessionId, Instant.now());
    }
}
