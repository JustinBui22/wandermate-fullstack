package com.example.travellingapp.service;

import com.example.travellingapp.response_template.CompleteResponse;

public interface TokenService {
    CompleteResponse<Object> generateAccessToken(String username, String sessionId);

    CompleteResponse<Object> refreshAccessToken(String refreshToken, String sessionToken);

    CompleteResponse<Object> generateSessionToken(String username, String sessionId);

    CompleteResponse<Object> generateRefreshToken(String username, String sessionId);

    void revokeSessionTokenBySessionId(String username, String sessionId, String sessionToken);

    boolean isSessionTokenInvalid(String username, String sessionId, String sessionToken);

    void checkMaxActiveSessions(String username, boolean overrideMaxSession);

    void revokeActiveRefreshTokensBySessionId(String sessionId);
}
