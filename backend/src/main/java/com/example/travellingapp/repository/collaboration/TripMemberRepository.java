package com.example.travellingapp.repository.collaboration;

import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import com.example.travellingapp.enums.TripCollaborationEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TripMemberRepository extends JpaRepository<TripMemberEntity, Long> {

    Optional<TripMemberEntity> findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
            Long tripId,
            String username
    );

    Optional<TripMemberEntity> findByTripMemberIdAndTrip_TripId(
            Long tripMemberId,
            Long tripId
    );

    boolean existsByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
            Long tripId,
            String username
    );

    boolean existsByTrip_TripIdAndUser_UserId(
            Long tripId,
            long userId
    );

    List<TripMemberEntity> findAllByTrip_TripId(Long tripId);

    long countByTrip_TripIdAndRole(
            Long tripId,
            TripCollaborationEnum role
    );

    @Query("""
            SELECT tm.trip
            FROM TripMemberEntity tm
            WHERE tm.user.username = :username
            AND tm.user.isActive = true
            ORDER BY tm.trip.createdDate DESC
            """)
    List<TripEntity> findAccessibleTripsByUsername(@Param("username") String username);

    @Query("""
        SELECT tm.trip
        FROM TripMemberEntity tm
        WHERE tm.user.username = :username
        AND tm.user.isActive = true
        AND tm.trip.tripId <> :currentTripId
        AND tm.trip.startDate < :currentTripEndDate
        AND tm.trip.endDate > :currentTripStartDate
        ORDER BY tm.trip.startDate ASC
        """)
    List<TripEntity> findOverlappingTripsForMember(
            @Param("username") String username,
            @Param("currentTripId") Long currentTripId,
            @Param("currentTripStartDate") LocalDateTime currentTripStartDate,
            @Param("currentTripEndDate") LocalDateTime currentTripEndDate
    );
}
