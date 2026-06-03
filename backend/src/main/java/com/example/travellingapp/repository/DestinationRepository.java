package com.example.travellingapp.repository;

import com.example.travellingapp.entity.DestinationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<DestinationEntity, Long> {

    List<DestinationEntity> findByTrip_TripIdOrderByDestinationOrderAsc(Long tripId);

    Optional<DestinationEntity> findByDestinationIdAndTrip_TripId(Long destinationId, Long tripId);

    Optional<DestinationEntity> findByDestinationIdAndTrip_TripIdAndTrip_User_Username(
            Long destinationId,
            Long tripId,
            String username
    );

    boolean existsByTrip_TripIdAndStartDateLessThanAndEndDateGreaterThan(
            Long tripId,
            LocalDateTime newEnd,
            LocalDateTime newStart
    );

    boolean existsByTrip_TripIdAndDestinationIdNotAndStartDateLessThanAndEndDateGreaterThan(
            Long tripId,
            Long destinationId,
            LocalDateTime newEnd,
            LocalDateTime newStart
    );

    boolean existsByTrip_TripIdAndStartDateBefore(
            Long tripId,
            LocalDateTime newTripStartDate
    );

    boolean existsByTrip_TripIdAndEndDateAfter(
            Long tripId,
            LocalDateTime newTripEndDate
    );
}
