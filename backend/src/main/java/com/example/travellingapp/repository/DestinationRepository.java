package com.example.travellingapp.repository;

import com.example.travellingapp.entity.DestinationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    boolean existsByTrip_TripIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long tripId,
            LocalDate newEnd,
            LocalDate newStart
    );

    boolean existsByTrip_TripIdAndDestinationIdNotAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long tripId,
            Long destinationId,
            LocalDate newEnd,
            LocalDate newStart
    );

    boolean existsByTrip_TripIdAndStartDateBefore(
            Long tripId,
            LocalDate newTripStartDate
    );

    boolean existsByTrip_TripIdAndEndDateAfter(
            Long tripId,
            LocalDate newTripEndDate
    );
}