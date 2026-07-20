package com.example.travellingapp.service;

public interface RefreshTokenReuseService {
    void revokeCompromisedSession(
            java.util.UUID reusedTokenId,
            String sessionToken
    );
}
