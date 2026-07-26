package com.example.travellingapp.service.impl;

import com.example.travellingapp.entity.RefreshTokenEntity;
import com.example.travellingapp.entity.SessionTokenEntity;
import com.example.travellingapp.repository.RefreshTokenRepository;
import com.example.travellingapp.repository.SessionTokenRepository;
import com.example.travellingapp.service.RefreshTokenReuseService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Service
public class RefreshTokenReuseServiceImpl implements RefreshTokenReuseService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SessionTokenRepository sessionTokenRepository;

    public RefreshTokenReuseServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            SessionTokenRepository sessionTokenRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.sessionTokenRepository = sessionTokenRepository;
    }

    @Transactional
    public void revokeCompromisedSession(RefreshTokenEntity reusedToken) {
        String username = reusedToken.getUsername();
        String sessionId = reusedToken.getSessionId();

        SessionTokenEntity session = sessionTokenRepository
                .findByUsernameAndSessionId(username, sessionId)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();

        reusedToken.setReuseDetected(true);
        reusedToken.setModifiedDate(now);
        refreshTokenRepository.save(reusedToken);

        List<RefreshTokenEntity> activeTokens =
                refreshTokenRepository
                        .findAllBySessionIdAndIsRevokedFalse(sessionId);

        for (RefreshTokenEntity activeToken : activeTokens) {
            activeToken.setRevoked(true);
            activeToken.setModifiedDate(now);
            activeToken.setRevokedDate(now);
        }

        if (!activeTokens.isEmpty()) {
            refreshTokenRepository.saveAll(activeTokens);
        }
        if (session != null) {
            sessionTokenRepository.delete(session);
        }
        log.warn(
                "Refresh-token reuse detected: revoked token family and session for user {} and sessionId {}",
                username,
                sessionId
        );
    }
}