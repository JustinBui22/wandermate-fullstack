package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.SendTripInvitationDTO;
import com.example.travellingapp.dto.request.SendTripJoinRequestDTO;
import com.example.travellingapp.dto.response.TripCollaborationActionResponseDTO;
import com.example.travellingapp.dto.response.TripCollaborationRequestResponseDTO;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripCollaborationRequestMapper;
import com.example.travellingapp.mapper.TripMemberMapper;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.TripRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripCollaborationRequestRepository;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.service.TripCollaborationRequestService;
import com.example.travellingapp.service.TripOverlapWarningService;
import com.example.travellingapp.validator.TripCollaborationRequestValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Service
@Log4j2
public class TripCollaborationRequestServiceImpl implements TripCollaborationRequestService {

    private final TripCollaborationRequestRepository requestRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TripAccessService tripAccessService;
    private final TripOverlapWarningService tripOverlapWarningService;
    private final TripCollaborationRequestMapper requestMapper;
    private final TripMemberMapper tripMemberMapper;
    private final TripCollaborationRequestValidator tripCollaborationRequestValidator;

    public TripCollaborationRequestServiceImpl(
            TripCollaborationRequestRepository requestRepository,
            TripMemberRepository tripMemberRepository,
            TripRepository tripRepository,
            UserRepository userRepository,
            ErrorCodeRepository errorCodeRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            TripAccessService tripAccessService,
            TripOverlapWarningService tripOverlapWarningService,
            TripCollaborationRequestMapper requestMapper,
            TripMemberMapper tripMemberMapper,
            TripCollaborationRequestValidator tripCollaborationRequestValidator) {
        this.requestRepository = requestRepository;
        this.tripMemberRepository = tripMemberRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.tripAccessService = tripAccessService;
        this.tripOverlapWarningService = tripOverlapWarningService;
        this.requestMapper = requestMapper;
        this.tripMemberMapper = tripMemberMapper;
        this.tripCollaborationRequestValidator = tripCollaborationRequestValidator;
    }

    @Transactional
    @Override
    public CompleteResponse<Object> sendInvitation(Long tripId, SendTripInvitationDTO request) {
        try {
            // Validate invitation input
            tripCollaborationRequestValidator.validateInvitationRequest(tripId, request);

            // Get current owner username
            String ownerUsername = authenticatedUserProvider.getUsername();

            // Only trip owner can send invitation
            TripEntity trip = tripAccessService.getTripIfOwner(tripId, ownerUsername);

            // Get owner user record
            User owner = userRepository.findByUsernameAndActive(ownerUsername)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Get invited user record
            User invitedUser = userRepository.findByUsernameAndActive(request.getUsername()).orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Owner cannot invite themselves
            tripCollaborationRequestValidator.validateOwnerCannotInviteSelf(owner, invitedUser);

            // Invited user must not already be a trip member
            tripCollaborationRequestValidator.validateUserIsNotAlreadyMember(tripId, invitedUser);

            // Avoid duplicate pending invitation or join request between the same users
            tripCollaborationRequestValidator.validateNoPendingRequestBetweenUsers(tripId, owner, invitedUser);

            // Create pending invitation only, not member yet
            TripCollaborationRequestEntity invitation = new TripCollaborationRequestEntity(
                    trip,
                    owner,
                    invitedUser,
                    request.getRole(),
                    TripEnum.INVITATION,
                    TripEnum.PENDING,
                    Instant.now()
            );

            TripCollaborationRequestEntity savedInvitation = requestRepository.save(invitation);
            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_INVITATION_SENT_SUCCESS,
                    TRIP_MEMBER.name(),
                    requestMapper.toResponseDTO(savedInvitation)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while sending trip invitation", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getMyPendingInvitations() {
        try {
            // Get current logged-in user
            String username = authenticatedUserProvider.getUsername();

            // Get all pending invitations sent to current user
            List<?> invitations = requestRepository
                    .findAllByTargetUser_UsernameAndRequestTypeAndStatusOrderByCreatedDateDesc(
                            username,
                            TripEnum.INVITATION,
                            TripEnum.PENDING
                    )
                    .stream()
                    .map(requestMapper::toResponseDTO)
                    .toList();

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_INVITATIONS_RETRIEVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    invitations
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting current user's pending invitations", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Transactional
    @Override
    public CompleteResponse<Object> acceptInvitation(Long requestId) {
        try {
            // Validate request ID
            tripCollaborationRequestValidator.validateRequestId(requestId);

            // Get current logged-in user
            String username = authenticatedUserProvider.getUsername();

            // Find pending invitation
            TripCollaborationRequestEntity invitation = requestRepository
                    .findByRequestIdAndRequestTypeAndStatus(
                            requestId,
                            TripEnum.INVITATION,
                            TripEnum.PENDING
                    )
                    .orElseThrow(() -> new BusinessException(TRIP_COLLABORATION_REQUEST_NOT_FOUND, TRIP_MEMBER.name()));

            // Only the invited user can accept this invitation
            tripCollaborationRequestValidator.validateInvitationBelongsToCurrentUser(
                    invitation,
                    username
            );

            // Make sure user is not already a member
            tripCollaborationRequestValidator.validateUserIsNotAlreadyMember(
                    invitation.getTrip().getTripId(),
                    invitation.getTargetUser()
            );

            // Create real trip member after invitation is accepted
            TripMemberEntity member = new TripMemberEntity(
                    invitation.getTrip(),
                    invitation.getTargetUser(),
                    invitation.getRequestedRole(),
                    Instant.now()
            );

            // Save new trip member
            TripMemberEntity savedMember = tripMemberRepository.save(member);

            // Mark invitation as accepted
            markRequestAsResponded(invitation, TripEnum.ACCEPTED);

            // Return request detail, new member detail and private overlap warnings
            TripCollaborationActionResponseDTO responseDTO =
                    new TripCollaborationActionResponseDTO(
                            requestMapper.toResponseDTO(invitation),
                            tripMemberMapper.toResponseDTO(savedMember),
                            tripOverlapWarningService.buildWarningsForUser(
                                    invitation.getTrip(),
                                    username
                            )
                    );

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_INVITATION_ACCEPTED_SUCCESS,
                    TRIP_MEMBER.name(),
                    responseDTO
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while accepting trip invitation", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Transactional
    @Override
    public CompleteResponse<Object> rejectInvitation(Long requestId) {
        try {
            // Validate request ID
            tripCollaborationRequestValidator.validateRequestId(requestId);

            // Get current logged-in user
            String username = authenticatedUserProvider.getUsername();

            // Find pending invitation
            TripCollaborationRequestEntity invitation = requestRepository
                    .findByRequestIdAndRequestTypeAndStatus(
                            requestId,
                            TripEnum.INVITATION,
                            TripEnum.PENDING
                    )
                    .orElseThrow(() -> new BusinessException(TRIP_COLLABORATION_REQUEST_NOT_FOUND, TRIP_MEMBER.name()));

            // Only the invited user can reject this invitation
            tripCollaborationRequestValidator.validateInvitationBelongsToCurrentUser(invitation, username);

            // Mark invitation as rejected
            markRequestAsResponded(invitation, TripEnum.REJECTED);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_INVITATION_REJECTED_SUCCESS,
                    TRIP_MEMBER.name(),
                    requestMapper.toResponseDTO(invitation)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while rejecting trip invitation", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Transactional
    @Override
    public CompleteResponse<Object> requestToJoinTrip(Long tripId, SendTripJoinRequestDTO request) {
        try {
            // Validate join request input
            tripCollaborationRequestValidator.validateJoinRequest(tripId, request);

            // Get current logged-in user
            String username = authenticatedUserProvider.getUsername();

            // Get requester user record
            User requester = userRepository.findByUsernameAndActive(username)
                    .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));

            // Get trip that user wants to join
            TripEntity trip = tripRepository.findById(tripId).orElseThrow(() -> new BusinessException(TRIP_NOT_FOUND, TRIP.name()));

            // Trip owner will receive this join request
            User owner = trip.getUser();

            // Owner cannot request to join their own trip
            tripCollaborationRequestValidator.validateOwnerCannotRequestToJoinOwnTrip(owner, requester);

            // Requester must not already be a member
            tripCollaborationRequestValidator.validateUserIsNotAlreadyMember(tripId, requester);

            // Avoid duplicate pending invitation or join request between the same users
            tripCollaborationRequestValidator.validateNoPendingRequestBetweenUsers(tripId, requester, owner);

            // Create pending join request only, not member yet
            TripCollaborationRequestEntity joinRequest = new TripCollaborationRequestEntity(
                    trip,
                    requester,
                    owner,
                    request.getRole(),
                    TripEnum.JOIN_REQUEST,
                    TripEnum.PENDING,
                    Instant.now()
            );

            TripCollaborationRequestEntity savedJoinRequest = requestRepository.save(joinRequest);

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_JOIN_REQUEST_SENT_SUCCESS,
                    TRIP_MEMBER.name(),
                    requestMapper.toResponseDTO(savedJoinRequest)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while requesting to join trip", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getPendingJoinRequests(Long tripId) {
        try {
            // Validate trip ID
            if (tripId == null) {
                log.error("Trip ID is null");
                throw new BusinessException(INVALID_INPUT, COMMON.name());
            }

            // Get current logged-in owner
            String username = authenticatedUserProvider.getUsername();

            // Only trip owner can view pending join requests
            tripAccessService.getTripIfOwner(tripId, username);

            // Get all pending join requests for this trip owner
            List<?> joinRequests = requestRepository
                    .findAllByTrip_TripIdAndTargetUser_UsernameAndRequestTypeAndStatusOrderByCreatedDateDesc(
                            tripId,
                            username,
                            TripEnum.JOIN_REQUEST,
                            TripEnum.PENDING
                    )
                    .stream()
                    .map(requestMapper::toResponseDTO)
                    .toList();

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_JOIN_REQUESTS_RETRIEVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    joinRequests
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting pending join requests", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getPendingJoinRequestsForMyTrips() {
        try {
            String username = authenticatedUserProvider.getUsername();
            List<TripCollaborationRequestResponseDTO> joinRequests = requestRepository
                    .findAllByTrip_User_UsernameAndRequestTypeAndStatusOrderByCreatedDateDesc(
                            username,
                            TripEnum.JOIN_REQUEST,
                            TripEnum.PENDING
                    )
                    .stream()
                    .map(requestMapper::toResponseDTO)
                    .toList();

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_JOIN_REQUESTS_RETRIEVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    joinRequests
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting pending join requests for current user's trips", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Override
    public CompleteResponse<Object> getMySentPendingJoinRequests() {
        try {
            String username = authenticatedUserProvider.getUsername();
            List<TripCollaborationRequestResponseDTO> joinRequests = requestRepository
                    .findAllByRequester_UsernameAndRequestTypeAndStatusOrderByCreatedDateDesc(
                            username,
                            TripEnum.JOIN_REQUEST,
                            TripEnum.PENDING
                    )
                    .stream()
                    .map(requestMapper::toResponseDTO)
                    .toList();
            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_JOIN_REQUESTS_RETRIEVED_SUCCESS,
                    TRIP_MEMBER.name(),
                    joinRequests
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while getting current user's sent pending join requests", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Transactional
    @Override
    public CompleteResponse<Object> acceptJoinRequest(Long requestId) {
        try {
            // Get pending join request and check current user is trip owner
            TripCollaborationRequestEntity joinRequest = getPendingJoinRequestForCurrentOwner(requestId);

            // Requester must not already be a member
            tripCollaborationRequestValidator.validateUserIsNotAlreadyMember(
                    joinRequest.getTrip().getTripId(),
                    joinRequest.getRequester()
            );

            // Create real trip member after owner accepts join request
            TripMemberEntity member = new TripMemberEntity(
                    joinRequest.getTrip(),
                    joinRequest.getRequester(),
                    joinRequest.getRequestedRole(),
                    Instant.now()
            );

            // Save new trip member
            TripMemberEntity savedMember = tripMemberRepository.save(member);

            // Mark join request as accepted
            markRequestAsResponded(
                    joinRequest,
                    TripEnum.ACCEPTED
            );

            // Do not show requester overlap warnings to owner
            TripCollaborationActionResponseDTO responseDTO =
                    new TripCollaborationActionResponseDTO(
                            requestMapper.toResponseDTO(joinRequest),
                            tripMemberMapper.toResponseDTO(savedMember),
                            List.of()
                    );

            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_JOIN_REQUEST_ACCEPTED_SUCCESS,
                    TRIP_MEMBER.name(),
                    responseDTO
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while accepting join request", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Transactional
    @Override
    public CompleteResponse<Object> rejectJoinRequest(Long requestId) {
        try {
            // Get pending join request and check current user is trip owner
            TripCollaborationRequestEntity joinRequest = getPendingJoinRequestForCurrentOwner(requestId);

            // Mark join request as rejected
            markRequestAsResponded(joinRequest, TripEnum.REJECTED);
            return getCompleteResponse(
                    errorCodeRepository,
                    TRIP_JOIN_REQUEST_REJECTED_SUCCESS,
                    TRIP_MEMBER.name(),
                    requestMapper.toResponseDTO(joinRequest)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while rejecting join request", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    private void markRequestAsResponded(TripCollaborationRequestEntity request, TripEnum status) {
        // Update pending request after user/owner responds
        request.setStatus(status);
        request.setModifiedDate(Instant.now());
        request.setRespondedDate(Instant.now());
        requestRepository.save(request);
    }

    private TripCollaborationRequestEntity getPendingJoinRequestForCurrentOwner(Long requestId) {
        // Validate request ID
        tripCollaborationRequestValidator.validateRequestId(requestId);

        // Get current logged-in owner
        String username = authenticatedUserProvider.getUsername();

        // Find pending join request
        TripCollaborationRequestEntity joinRequest = requestRepository
                .findByRequestIdAndRequestTypeAndStatus(
                        requestId,
                        TripEnum.JOIN_REQUEST,
                        TripEnum.PENDING
                )
                .orElseThrow(() -> new BusinessException(TRIP_COLLABORATION_REQUEST_NOT_FOUND, TRIP_MEMBER.name()));

        // Only the trip owner can accept/reject this join request
        tripCollaborationRequestValidator.validateJoinRequestBelongsToCurrentOwner(joinRequest, username);

        // Double check current user is still trip owner
        tripAccessService.getTripIfOwner(joinRequest.getTrip().getTripId(), username);
        return joinRequest;
    }
}