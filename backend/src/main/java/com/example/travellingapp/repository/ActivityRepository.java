package com.example.travellingapp.repository;

import com.example.travellingapp.entity.ActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {

    List<ActivityEntity> findAllByDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
            Long destinationId,
            Long tripId,
            String username
    );

    Optional<ActivityEntity> findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
            Long activityId,
            Long destinationId,
            Long tripId,
            String username
    );

    boolean existsByDestination_Trip_TripIdAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            Long tripId,
            LocalDateTime newEnd,
            LocalDateTime newStart
    );

    List<ActivityEntity> findAllByDestination_DestinationIdAndDestination_Trip_TripId(
            Long destinationId,
            Long tripId
    );

    Optional<ActivityEntity> findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripId(
            Long activityId,
            Long destinationId,
            Long tripId
    );

    boolean existsByDestination_Trip_TripIdAndActivityIdNotAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            Long tripId,
            Long activityId,
            LocalDateTime newEnd,
            LocalDateTime newStart
    );

    boolean existsByDestination_DestinationIdAndStartDateTimeBefore(
            Long destinationId,
            LocalDate startDateTime
    );

    boolean existsByDestination_DestinationIdAndEndDateTimeAfter(
            Long destinationId,
            LocalDate endDateTime
    );
}