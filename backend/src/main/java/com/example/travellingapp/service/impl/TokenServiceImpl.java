package com.example.travellingapp.service.impl;

import com.example.travellingapp.entity.RefreshTokenEntity;
import com.example.travellingapp.entity.SessionTokenEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.*;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.DataSecurity;
import com.example.travellingapp.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.travellingapp.util.Common.getConfigValue;
import static com.example.travellingapp.util.DataConverter.convertStringToInt;
import static com.example.travellingapp.validator.CommonInputValidator.validatePhoneForm;
import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;
import static com.example.travellingapp.util.DataConverter.convertStringToLong;

@Log4j2
@Service
public class TokenServiceImpl implements TokenService {
    private final ConfigurationRepository configurationRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSecurity dataSecurity;
    private static final String REFRESH_TOKEN_FIELD = "refreshToken";

    public TokenServiceImpl(ConfigurationRepository configurationRepository, ErrorCodeRepository errorCodeRepository, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, SessionTokenRepository sessionTokenRepository, PasswordEncoder passwordEncoder, DataSecurity dataSecurity) {
        this.configurationRepository = configurationRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.sessionTokenRepository = sessionTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataSecurity = dataSecurity;
    }

    // Generate a Bearer Token based on the username
    public CompleteResponse<Object> generateAccessToken(String username, String sessionId) {
        try {
            log.info("Start generating access token!");
            long expirationTime = convertStringToLong(getConfigValue(ACCESS_TOKEN_EXPIRATION_TIME.name(), configurationRepository, "300000L"));
            // Backup mechanism to check if username is phone number
            if (validatePhoneForm(username, configurationRepository.findByConfigCode(PHONE_VN_PATTERN.name()))) {
                log.error("Input format is invalid for token generation!");
                throw new BusinessException(INPUT_FORMAT_INVALID, TOKEN.name());
            }
            User user = userRepository.findByUsernameAndActive(username, true).orElseGet(() -> {
                log.error("There is user as {}", username);
                throw new BusinessException(USER_NOT_FOUND, COMMON.name());
            });
            String token = Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .claim("roles", user.getAuthorities())
                    .claim("sessionId", sessionId)
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                    .signWith(getSecretKey()) // Specify the signing algorithm
                    .compact();
            ErrorCodeEnum errorCodeEnum = Optional.of(token).filter(t -> !t.isEmpty()) // Check if token is not empty
                    .map(t -> TOKEN_GENERATE_SUCCESS).orElseGet(() -> {
                        log.error("There is an error generating access token!");
                        return TOKEN_GENERATE_FAIL;
                    });
            return getCompleteResponse(errorCodeRepository, errorCodeEnum, TOKEN.name(), token);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Jwt token generated failed!");
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    public CompleteResponse<Object> generateRefreshToken(String username, String sessionId) {
        log.info("Start generating refresh token for user {}!", username);
        try {
            String refreshToken = UUID.randomUUID().toString();
            int expirationTime = convertStringToInt(getConfigValue(REFRESH_TOKEN_EXPIRATION_TIME.name(), configurationRepository, "1"));
            RefreshTokenEntity entity = new RefreshTokenEntity(false, LocalDateTime.now(),
                    LocalDateTime.now().plusMonths(expirationTime), username, dataSecurity.hashData(refreshToken), sessionId, null);
            refreshTokenRepository.save(entity);
            ErrorCodeEnum errorCodeEnum = Optional.of(refreshToken).filter(t -> !t.isEmpty()) // Check if token is not empty
                    .map(t -> TOKEN_GENERATE_SUCCESS).orElseGet(() -> {
                        log.error("There is an error generating refresh token!");
                        return TOKEN_GENERATE_FAIL;
                    });
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put(REFRESH_TOKEN_FIELD, refreshToken);
            tokenMap.put("refreshTokenId", entity.getTokenId());
            return getCompleteResponse(errorCodeRepository, errorCodeEnum, TOKEN.name(), tokenMap);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Refresh token generated failed!");
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    public void revokeActiveRefreshTokensBySessionId(String sessionId) {
        List<RefreshTokenEntity> tokenList = refreshTokenRepository.findAllBySessionIdAndIsRevokedFalse(sessionId);
        if (tokenList.isEmpty()) {
            log.warn("No active refresh tokens with sessionId {} to revoke", sessionId);
            return;
        }
        for (RefreshTokenEntity token : tokenList) {
            token.setRevoked(true);
            token.setModifiedDate(LocalDateTime.now());
            token.setRevokedDate(LocalDateTime.now());
        }
        refreshTokenRepository.saveAll(tokenList);
        log.info("Active refresh tokens with sessionId {} revoked successfully", sessionId);
    }

    private void revokeActiveRefreshTokenByTokenId(UUID tokenId) {
        Optional<RefreshTokenEntity> tokenOptional = refreshTokenRepository.findByTokenId(tokenId);
        if (tokenOptional.isPresent()) {
            RefreshTokenEntity token = tokenOptional.get();
            String username = token.getUsername();
            token.setRevoked(true);
            token.setModifiedDate(LocalDateTime.now());
            refreshTokenRepository.save(token);
            log.info("Refresh token revoked for user {} successfully!", username);

        } else {
            log.error("No existing refresh token with token Id {} to be revoked", tokenId);
            throw new BusinessException(SESSION_TOKEN_INVALID, TOKEN.name());
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    @Override
    public CompleteResponse<Object> refreshAccessToken(String refreshToken, String sessionToken) {
        log.info("Start refreshing access token!");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(REFRESH_TOKEN_INVALID, TOKEN.name());
        }

        if (sessionToken == null || sessionToken.isBlank()) {
            throw new BusinessException(SESSION_TOKEN_INVALID, TOKEN.name());
        }
        try {
            // Validate if the token's user exists
            Optional<RefreshTokenEntity> refreshTokenOptional = refreshTokenRepository.findByTokenHash(dataSecurity.hashData(refreshToken));
            if (refreshTokenOptional.isEmpty()) {
                log.error("There is no valid refresh token!");
                throw new BusinessException(REFRESH_TOKEN_INVALID, TOKEN.name());
            }
            RefreshTokenEntity refreshTokenEntity = refreshTokenOptional.get();
            String userName = refreshTokenEntity.getUsername();
            String sessionId = refreshTokenEntity.getSessionId();
            UUID tokenId = refreshTokenEntity.getTokenId();
            // Check if the refresh token is revoked and reused
            if (refreshTokenEntity.isRevoked()) {
                log.error("Refresh token reuse detected for user {} and sessionId {}", userName, sessionId);
                refreshTokenEntity.setReuseDetected(true);
                refreshTokenEntity.setModifiedDate(LocalDateTime.now());
                refreshTokenRepository.save(refreshTokenEntity);
                revokeActiveRefreshTokensBySessionId(sessionId);
                revokeSessionTokenBySessionId(userName, sessionId, sessionToken);
                throw new BusinessException(REFRESH_TOKEN_INVALID, TOKEN.name());
            }
            // If refresh token expired
            if (refreshTokenEntity.getExpiredDate().isBefore(LocalDateTime.now())) {
                log.error("The refresh token for user {} expired!", userName);
                // Revoke refresh token
                revokeActiveRefreshTokenByTokenId(tokenId);
                // Revoke session token
                revokeSessionTokenBySessionId(userName, sessionId, sessionToken);
                return getCompleteResponse(errorCodeRepository, REFRESH_TOKEN_EXPIRED, TOKEN.name(), null);
            }
            if (isSessionTokenInvalid(userName, sessionId, sessionToken)) {
                log.error("Invalid session token for user {}", userName);
                throw new BusinessException(SESSION_TOKEN_INVALID, TOKEN.name());
            }
            // Revoke refresh token
            revokeActiveRefreshTokenByTokenId(tokenId);

            // Get new access token
            CompleteResponse<Object> newAccessTokenResponse = generateAccessToken(userName, sessionId);
            String newAccessToken = newAccessTokenResponse
                    .getResponseBody()
                    .getBody()
                    .toString();
            // Get new refresh token
            CompleteResponse<Object> newRefreshTokenResponse = generateRefreshToken(userName, sessionId);
            Map<String, Object> newRefreshTokenMap = (Map<String, Object>) newRefreshTokenResponse
                    .getResponseBody()
                    .getBody();
            String newRefreshToken = newRefreshTokenMap.get(REFRESH_TOKEN_FIELD).toString();
            // Update id of new refresh token in the revoked one
            refreshTokenEntity.setReplacedByTokenId((UUID) newRefreshTokenMap.get("refreshTokenId"));
            refreshTokenRepository.save(refreshTokenEntity);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("accessToken", newAccessToken);
            tokenMap.put(REFRESH_TOKEN_FIELD, newRefreshToken);
            return getCompleteResponse(
                    errorCodeRepository,
                    TOKEN_GENERATE_SUCCESS,
                    TOKEN.name(),
                    tokenMap);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There is an error in refreshing access token!", e);
            return getCompleteResponse(errorCodeRepository, TOKEN_VERIFY_FAIL, TOKEN.name(), null);
        }
    }

    // Validate the token and extract the phone number
    public CompleteResponse<Object> validateAccessToken(String accessToken) {
        log.info("Start validating access token!");
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(getSecretKey()).build().parseClaimsJws(accessToken) // This validates the token
                    .getBody();
            String username = claims.getSubject();
            // Validate if the token's user exists
            log.info("Start checking if user {} is registered!", username);
            Optional<User> userOptional = userRepository.findByUsernameAndActive(username, true);
            if (userOptional.isEmpty()) {
                log.error("There is no user as {}", username);
                return getCompleteResponse(errorCodeRepository, USER_NOT_FOUND, TOKEN.name(), null);
            }
            log.info("The token is valid for user {}", username);
            return getCompleteResponse(errorCodeRepository, TOKEN_VERIFY_SUCCESS, TOKEN.name(), claims);
        } catch (ExpiredJwtException e) {
            String username = e.getClaims() != null
                    ? e.getClaims().getSubject()
                    : "Unknown";
            log.error("Access token expires for user {}!", username);
            return getCompleteResponse(errorCodeRepository, TOKEN_EXPIRE, TOKEN.name(), null);
        } catch (Exception e) {
            log.error("There is an error in validating access token!", e);
            return getCompleteResponse(errorCodeRepository, TOKEN_VERIFY_FAIL, TOKEN.name(), null);
        }
    }

    // Method to get the SECRET key dynamically
    private SecretKey getSecretKey() {
        String secret = getConfigValue(SECRET_KEY_CONFIG, configurationRepository, TOKEN.name());
        // Ensure the key is at least 256 bits for HS512
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            log.error("Secret key must be at least 256 bits (32 bytes) for HS512!");
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private void storeSessionToken(String userName, String token, String sessionId) {
        try {
            SessionTokenEntity newToken = new SessionTokenEntity(userName, passwordEncoder.encode(token), sessionId, LocalDateTime.now());
            sessionTokenRepository.save(newToken);
            log.info("Session token for user {} stored successfully!", userName);
        } catch (Exception e) {
            log.error("Session token stored failed!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    public CompleteResponse<Object> generateSessionToken(String userName, String sessionId) {
        try {
            String sessionToken = UUID.randomUUID().toString();
            storeSessionToken(userName, sessionToken, sessionId);
            return getCompleteResponse(errorCodeRepository, TOKEN_GENERATE_SUCCESS, TOKEN.name(), sessionToken);
        } catch (Exception e) {
            log.error("Session token generated failed!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    public boolean isSessionTokenInvalid(String username, String sessionId, String sessionToken) {
        Optional<SessionTokenEntity> sessionOptional =
                sessionTokenRepository.findByUsernameAndSessionId(username, sessionId);

        if (sessionOptional.isEmpty()) {
            log.info("No session found for user {} and sessionId {}", username, sessionId);
            return true;
        }
        SessionTokenEntity session = sessionOptional.get();
        // Check if the token session is correct
        if (passwordEncoder.matches(sessionToken, session.getSessionToken())) {
            log.info("Session token is valid for user {} and sessionId {}", username, sessionId);
            return false;
        }
        log.info("Session token is invalid for user {} and sessionId {}", username, sessionId);
        return true;
    }

    // Two related DB operations, need to succeed or fail together, so use @Transactional to ensure data consistency
    @Transactional
    public void checkMaxActiveSessions(String username, boolean overrideMaxSession) {
        try {
            int maxSessionConfig = Integer.parseInt(getConfigValue(MAX_ALLOWED_SESSIONS.name(), configurationRepository, "3"));
            if (maxSessionConfig <= 0) {
                log.error("Invalid max allowed sessions config: {}", maxSessionConfig);
                throw new BusinessException(INVALID_CONFIG, COMMON.name());
            }
            List<SessionTokenEntity> activeSessionList = sessionTokenRepository.findAllByUsernameOrderByCreatedDateAsc(username);

            // Check if the user has exceeded maxed number of active sessions
            if (activeSessionList.size() < maxSessionConfig) {
                return;
            }
            if (!overrideMaxSession) {
                log.info("Max allowed active sessions reached for user {}", username);
                throw new BusinessException(MAX_SESSIONS_REACHED, LOGIN.name());
            }

            // If overrideMaxSession is true, revoke the oldest active session until the number of active sessions is less than max allowed sessions
            while (activeSessionList.size() >= maxSessionConfig) {
                //SessionTokenEntity oldestSession = activeSessionList.removeFirst();
                SessionTokenEntity oldestSession = activeSessionList.remove(0);
                log.info(
                        "User {} chose to override max sessions. Revoking oldest sessionId {}.",
                        username,
                        oldestSession.getSessionId()
                );
                revokeActiveRefreshTokensBySessionId(oldestSession.getSessionId());
                sessionTokenRepository.delete(oldestSession);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (NumberFormatException e) {
            log.error("Invalid max allowed sessions configuration value!", e);
            throw new BusinessException(INPUT_FORMAT_INVALID, COMMON.name());
        } catch (Exception e) {
            log.error("Checking max active sessions failed for user {}!", username, e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    public void revokeSessionTokenBySessionId(String username, String sessionId, String sessionToken) {
        SessionTokenEntity token = sessionTokenRepository
                .findByUsernameAndSessionId(username, sessionId)
                .orElseThrow(
                        () -> new BusinessException(SESSION_TOKEN_INVALID, TOKEN.name()));

        if (!passwordEncoder.matches(sessionToken, token.getSessionToken())) {
            log.error("Session token does not match for user {} and sessionId {}", username, sessionId);
            throw new BusinessException(SESSION_TOKEN_INVALID, TOKEN.name());
        }
        sessionTokenRepository.delete(token);
        log.info("Session token revoked for user {} successfully!", username);
    }
}
