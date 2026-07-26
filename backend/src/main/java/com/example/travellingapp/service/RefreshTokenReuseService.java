package com.example.travellingapp.service;

import com.example.travellingapp.entity.RefreshTokenEntity;

public interface RefreshTokenReuseService {
    void revokeCompromisedSession(RefreshTokenEntity reusedToken);
}
