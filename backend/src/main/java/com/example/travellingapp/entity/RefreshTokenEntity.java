package com.example.travellingapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "token_id")
    private UUID tokenId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "expired_date", nullable = false)
    private LocalDateTime expiredDate;

    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "revoked_date")
    private LocalDateTime revokedDate;

    @Column(name = "replaced_by_token_id")
    private UUID replacedByTokenId;

    @Column(name = "reuse_detected", nullable = false)
    private boolean reuseDetected;

    public RefreshTokenEntity(boolean isRevoked, LocalDateTime createdDate, LocalDateTime expiredDate, String username, String tokenHash, String sessionId, UUID replacedByTokenId) {
        this.isRevoked = isRevoked;
        this.createdDate = createdDate;
        this.expiredDate = expiredDate;
        this.username = username;
        this.tokenHash = tokenHash;
        this.sessionId = sessionId;
        this.replacedByTokenId = replacedByTokenId;
    }

    public RefreshTokenEntity() {
    }
}
