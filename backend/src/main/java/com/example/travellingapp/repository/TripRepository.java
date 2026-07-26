package com.example.travellingapp.repository;

import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {
    // Check duplicate trip name for the same user
    boolean existsByUser_UsernameAndTripNameIgnoreCase(String username, String tripName);

    List<TripEntity> findAllByUser(User user);

    Optional<TripEntity> findByTripIdAndUser_Username(Long id, String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT trip FROM TripEntity trip WHERE trip.tripId = :tripId")
    Optional<TripEntity> findByTripIdForUpdate(@Param("tripId") Long tripId);

    // Find possible already existed trip
    boolean existsByUser_UsernameAndTripNameIgnoreCaseAndTripIdNot(
            String username,
            String tripName,
            Long tripId
    );
    boolean existsByUser_UsernameAndStartDateLessThanAndEndDateGreaterThan(
            String username,
            LocalDateTime newEnd,
            LocalDateTime newStart
    );

    boolean existsByUser_UsernameAndTripIdNotAndStartDateLessThanAndEndDateGreaterThan(
            String username,
            Long tripId,
            LocalDateTime newEnd,
            LocalDateTime newStart
    );



}
