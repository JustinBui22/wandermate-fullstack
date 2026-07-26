package com.example.travellingapp.repository;

import com.example.travellingapp.entity.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByUsername(String userName);

    Optional<RefreshTokenEntity> findByTokenHash(String refreshToken);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.tokenHash = :tokenHash")
    Optional<RefreshTokenEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.sessionId = :sessionId AND r.isRevoked = false")
    List<RefreshTokenEntity> findAllBySessionIdAndIsRevokedFalse(@Param("sessionId") String sessionId);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.username = :username AND r.isRevoked = false")
    List<RefreshTokenEntity> findAllByUsernameAndIsRevokedFalse(String username);
}