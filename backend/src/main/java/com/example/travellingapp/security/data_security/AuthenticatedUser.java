package com.example.travellingapp.security.data_security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticatedUser {
    private String username;
    private String sessionId;

    public AuthenticatedUser(String username, String sessionId) {
        this.username = username;
        this.sessionId = sessionId;
    }
}
