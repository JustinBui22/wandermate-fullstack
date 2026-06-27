package com.example.travellingapp.repository.collaboration;

import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.repository.collaboration.projection.TripPendingJoinRequestCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripCollaborationRequestRepository extends JpaRepository<TripCollaborationRequestEntity, Long> {

    boolean existsByTrip_TripIdAndRequester_UserIdAndTargetUser_UserIdAndStatus(
            Long tripId,
            long requesterUserId,
            long targetUserId,
            TripEnum status
    );

    Optional<TripCollaborationRequestEntity> findByRequestIdAndRequestTypeAndStatus(
            Long requestId,
            TripEnum requestType,
            TripEnum status
    );

    List<TripCollaborationRequestEntity> findAllByTargetUser_UsernameAndRequestTypeAndStatusOrderByCreatedDateDesc(
            String username,
            TripEnum requestType,
            TripEnum status
    );

    List<TripCollaborationRequestEntity> findAllByTrip_TripIdAndTargetUser_UsernameAndRequestTypeAndStatusOrderByCreatedDateDesc(
            Long tripId,
            String targetUsername,
            TripEnum requestType,
            TripEnum status
    );

    // For projection

    long countByTargetUser_UsernameAndRequestTypeAndStatus(
            String username,
            TripEnum requestType,
            TripEnum status
    );

    long countByTrip_User_UsernameAndRequestTypeAndStatus(
            String username,
            TripEnum requestType,
            TripEnum status
    );

    @Query("""
        SELECT request.trip.tripId AS tripId,
               COUNT(request.requestId) AS pendingCount
        FROM TripCollaborationRequestEntity request
        WHERE request.trip.user.username = :username
          AND request.requestType = :requestType
          AND request.status = :status
        GROUP BY request.trip.tripId
        """)
    List<TripPendingJoinRequestCountProjection> countPendingJoinRequestsByOwnedTrip(
            @Param("username") String username,
            @Param("requestType") TripEnum requestType,
            @Param("status") TripEnum status
    );
}