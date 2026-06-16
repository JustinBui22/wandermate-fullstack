package com.example.travellingapp.repository.collaboration;

import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.enums.TripCollaborationEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripCollaborationRequestRepository extends JpaRepository<TripCollaborationRequestEntity, Long> {

    boolean existsByTrip_TripIdAndRequester_UserIdAndTargetUser_UserIdAndStatus(
            Long tripId,
            long requesterUserId,
            long targetUserId,
            TripCollaborationEnum status
    );

    Optional<TripCollaborationRequestEntity> findByRequestIdAndRequestTypeAndStatus(
            Long requestId,
            TripCollaborationEnum requestType,
            TripCollaborationEnum status
    );

    List<TripCollaborationRequestEntity> findAllByTargetUser_UsernameAndRequestTypeAndStatusOrderByCreatedDateDesc(
            String username,
            TripCollaborationEnum requestType,
            TripCollaborationEnum status
    );

    List<TripCollaborationRequestEntity> findAllByTrip_TripIdAndTargetUser_UsernameAndRequestTypeAndStatusOrderByCreatedDateDesc(
            Long tripId,
            String targetUsername,
            TripCollaborationEnum requestType,
            TripCollaborationEnum status
    );
}