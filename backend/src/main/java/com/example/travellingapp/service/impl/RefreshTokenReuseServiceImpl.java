package com.example.travellingapp.service.impl;

import com.example.travellingapp.entity.RefreshTokenEntity;
import com.example.travellingapp.entity.SessionTokenEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.RefreshTokenRepository;
import com.example.travellingapp.repository.SessionTokenRepository;
import com.example.travellingapp.service.RefreshTokenReuseService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.example.travellingapp.enums.CommonEnum.TOKEN;
import static com.example.travellingapp.enums.ErrorCodeEnum.REFRESH_TOKEN_INVALID;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeCompromisedSession(
            UUID reusedTokenId,
            String presentedSessionToken
    ) {
        RefreshTokenEntity reusedToken = refreshTokenRepository
                .findByTokenId(reusedTokenId)
                .orElseThrow(() ->
                        new BusinessException(
                                REFRESH_TOKEN_INVALID,
                                TOKEN.name()
                        )
                );
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