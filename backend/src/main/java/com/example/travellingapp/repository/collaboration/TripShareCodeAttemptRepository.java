package com.example.travellingapp.repository.collaboration;

import com.example.travellingapp.entity.collaboration.TripShareCodeAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripShareCodeAttemptRepository extends JpaRepository<TripShareCodeAttemptEntity, Long> {

    Optional<TripShareCodeAttemptEntity> findByUser_UserId(Long userId);

    Optional<TripShareCodeAttemptEntity> findByUser_Username(String username);
}