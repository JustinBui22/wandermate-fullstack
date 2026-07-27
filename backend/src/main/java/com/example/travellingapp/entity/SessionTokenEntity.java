package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "session_token")
@Getter
@Setter
public class SessionTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "token", nullable = false, unique = true)
    private String sessionToken;

    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @Column(name = "modified_date")
    private Instant modifiedDate;

    public SessionTokenEntity(String username, String sessionToken, String sessionId, Instant createdDate) {
        this.username = username;
        this.sessionToken = sessionToken;
        this.sessionId = sessionId;
        this.createdDate = createdDate;
    }

    public SessionTokenEntity() {

    }
}