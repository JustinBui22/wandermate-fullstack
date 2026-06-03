package com.example.travellingapp.security.data_security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {
    public AuthenticatedUser getAuthenticatedUser() {
        return (AuthenticatedUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public String getUsername() {
        return getAuthenticatedUser().getUsername();
    }

    public String getSessionId() {
        return getAuthenticatedUser().getSessionId();
    }
}
