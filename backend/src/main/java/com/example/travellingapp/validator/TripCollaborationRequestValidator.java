package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.SendTripInvitationDTO;
import com.example.travellingapp.dto.request.SendTripJoinRequestDTO;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.collaboration.TripCollaborationRequestRepository;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;

@Log4j2
@Component
public class TripCollaborationRequestValidator {

    private final TripMemberRepository tripMemberRepository;
    private final TripCollaborationRequestRepository requestRepository;

    public TripCollaborationRequestValidator(
            TripMemberRepository tripMemberRepository,
            TripCollaborationRequestRepository requestRepository
    ) {
        this.tripMemberRepository = tripMemberRepository;
        this.requestRepository = requestRepository;
    }

    public void validateInvitationRequest(
            Long tripId,
            SendTripInvitationDTO request
    ) {
        // Validate invitation input
        if (
                tripId == null
                        || request == null
                        || request.getUsername() == null
                        || request.getUsername().isBlank()
                        || request.getRole() == null
        ) {
            log.error("Invalid input to send trip invitation!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }
        // OWNER role should only be created automatically when the trip is created
        validateRequestedRole(request.getRole());
    }

    public void validateJoinRequest(
            Long tripId,
            SendTripJoinRequestDTO request
    ) {
        // Validate join request input
        if (
                tripId == null
                        || request == null
                        || request.getRole() == null
        ) {
            log.error("Invalid input to send trip join request!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        // OWNER role should not be requested
        validateRequestedRole(request.getRole());
    }

    public void validateRequestId(Long requestId) {
        // Validate request ID
        if (requestId == null) {
            log.error("Invalid request ID for trip collaboration request!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }
    }

    public void validateRequestedRole(TripEnum role) {
        // OWNER role cannot be invited or requested
        // Role must be EDITOR or VIEWER only
        if (role == null || role.getGroup() != TripEnum.Group.MEMBER_ROLE || role == TripEnum.OWNER) {
            log.error("Invalid role requested: {}", role);
            throw new BusinessException(TRIP_OWNER_ROLE_CANNOT_BE_CHANGED, TRIP_MEMBER.name());
        }
    }

    public void validateOwnerCannotInviteSelf(
            User owner,
            User invitedUser
    ) {
        // Owner cannot invite themselves
        if (isSameUser(owner, invitedUser)) {
            log.error("Owner {} cannot invite themselves to the trip!", owner.getUsername());
            throw new BusinessException(TRIP_CANNOT_INVITE_SELF, TRIP_MEMBER.name());
        }
    }

    public void validateOwnerCannotRequestToJoinOwnTrip(
            User owner,
            User requester
    ) {
        // Owner cannot request to join their own trip
        if (isSameUser(owner, requester)) {
            log.error("Owner {} cannot request to join their own trip!", owner.getUsername());
            throw new BusinessException(TRIP_OWNER_CANNOT_REQUEST_TO_JOIN_OWN_TRIP, TRIP_MEMBER.name());
        }
    }

    public void validateUserIsNotAlreadyMember(
            Long tripId,
            User user
    ) {
        // Validate trip and user before checking member
        if (tripId == null || user == null) {
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        // User should not be added again if they are already a trip member
        if (tripMemberRepository.existsByTrip_TripIdAndUser_UserId(
                tripId,
                user.getUserId()
        )) {
            log.error("User {} is already a member of trip {}!", user.getUsername(), tripId);
            throw new BusinessException(TRIP_MEMBER_ALREADY_EXISTS, TRIP_MEMBER.name());
        }
    }

    public void validateNoPendingRequestBetweenUsers(
            Long tripId,
            User firstUser,
            User secondUser
    ) {
        // Validate users before checking pending requests
        if (
                tripId == null
                        || firstUser == null
                        || secondUser == null
        ) {
            log.error("Invalid input to check pending requests between users!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        // Check if first user already sent a pending request to second user
        boolean firstToSecondPending = requestRepository
                .existsByTrip_TripIdAndRequester_UserIdAndTargetUser_UserIdAndStatus(
                        tripId,
                        firstUser.getUserId(),
                        secondUser.getUserId(),
                        TripEnum.PENDING
                );

        // Check if second user already sent a pending request to first user
        boolean secondToFirstPending = requestRepository
                .existsByTrip_TripIdAndRequester_UserIdAndTargetUser_UserIdAndStatus(
                        tripId,
                        secondUser.getUserId(),
                        firstUser.getUserId(),
                        TripEnum.PENDING
                );

        if (firstToSecondPending || secondToFirstPending) {
            log.error("There is already a pending collaboration request between users {} and {} for trip {}!",
                    firstUser.getUsername(), secondUser.getUsername(), tripId);
            throw new BusinessException(TRIP_COLLABORATION_REQUEST_ALREADY_EXISTS, TRIP_MEMBER.name());
        }
    }

    public void validateInvitationBelongsToCurrentUser(
            TripCollaborationRequestEntity invitation,
            String username
    ) {
        // Only the invited user can accept/reject this invitation
        if (
                invitation == null
                        || invitation.getTargetUser() == null
                        || username == null
                        || username.isBlank()
                        || !invitation.getTargetUser().getUsername().equals(username)
        ) {
            log.error("User {} is not authorized to accept/reject invitation {}!", username, invitation != null ? invitation.getRequestId() : null);
            throw new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name());
        }
    }

    public void validateJoinRequestBelongsToCurrentOwner(
            TripCollaborationRequestEntity joinRequest,
            String username
    ) {
        // Only the trip owner can accept/reject this join request
        if (
                joinRequest == null
                        || joinRequest.getTargetUser() == null
                        || username == null
                        || username.isBlank()
                        || !joinRequest.getTargetUser().getUsername().equals(username)
        ) {
            log.error("User {} is not authorized to accept/reject join request {}!", username, joinRequest != null ? joinRequest.getRequestId() : null);
            throw new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name());
        }
    }

    private boolean isSameUser(
            User firstUser,
            User secondUser
    ) {
        // Validate users before comparing user IDs
        if (firstUser == null || secondUser == null) {
            log.error("Invalid input to compare users!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }
        return firstUser.getUserId() == secondUser.getUserId();
    }
}