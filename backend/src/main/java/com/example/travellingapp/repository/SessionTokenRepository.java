package com.example.travellingapp.repository;

import com.example.travellingapp.entity.SessionTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionTokenRepository extends JpaRepository<SessionTokenEntity, Long> {
    Optional<SessionTokenEntity> findByUsernameAndSessionId(String username, String sessionId);

    void deleteAllByUsername(String username);
    List<SessionTokenEntity> findAllByUsernameOrderByCreatedDateAsc(String username);
}
