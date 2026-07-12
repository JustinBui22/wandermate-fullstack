package com.example.travellingapp.repository;

import com.example.travellingapp.entity.OtpCheckEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OtpCheckRepository extends JpaRepository<OtpCheckEntity, Integer> {
    @Query("SELECT o FROM OtpCheckEntity o WHERE o.username = :username AND o.isBlock = :isBlock")
    Optional<OtpCheckEntity> findByUsernameAndBlock(String username, boolean isBlock);


    Optional<OtpCheckEntity> findByUsername(String username);

    Optional<OtpCheckEntity> findByEmailIgnoreCase(String email);
}
