package com.example.travellingapp.repository.collaboration;

import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import com.example.travellingapp.enums.TripMemberRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            TripMemberRoleEnum role
    );

    @Query("""
            SELECT tm.trip
            FROM TripMemberEntity tm
            WHERE tm.user.username = :username
            AND tm.user.isActive = true
            ORDER BY tm.trip.createdDate DESC
            """)
    List<TripEntity> findAccessibleTripsByUsername(@Param("username") String username);
}
