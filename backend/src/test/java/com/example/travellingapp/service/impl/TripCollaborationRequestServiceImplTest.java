package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.SendTripInvitationDTO;
import com.example.travellingapp.dto.request.SendTripJoinRequestDTO;
import com.example.travellingapp.dto.response.MyTripOverlapWarningDTO;
import com.example.travellingapp.dto.response.TripCollaborationActionResponseDTO;
import com.example.travellingapp.dto.response.TripCollaborationRequestResponseDTO;
import com.example.travellingapp.dto.response.TripMemberResponseDTO;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.TripCollaborationEnum;
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
import com.example.travellingapp.service.TripOverlapWarningService;
import com.example.travellingapp.validator.TripCollaborationRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripCollaborationRequestServiceImplTest {

    @Mock
    private TripCollaborationRequestRepository requestRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private TripOverlapWarningService tripOverlapWarningService;

    @Mock
    private TripCollaborationRequestMapper requestMapper;

    @Mock
    private TripMemberMapper tripMemberMapper;

    @Mock
    private TripCollaborationRequestValidator tripCollaborationRequestValidator;

    private TripCollaborationRequestServiceImpl service;

    private static final Long TRIP_ID = 1L;
    private static final Long REQUEST_ID = 10L;
    private static final String OWNER_USERNAME = "OwnerUser";
    private static final String FRIEND_USERNAME = "FriendUser";

    @BeforeEach
    void setUp() {
        service = new TripCollaborationRequestServiceImpl(
                requestRepository,
                tripMemberRepository,
                tripRepository,
                userRepository,
                errorCodeRepository,
                authenticatedUserProvider,
                tripAccessService,
                tripOverlapWarningService,
                requestMapper,
                tripMemberMapper,
                tripCollaborationRequestValidator
        );
    }

    @Test
    void sendInvitation_shouldCreatePendingInvitation_whenInputIsValid() {
        SendTripInvitationDTO request = sendInvitationRequest();
        User owner = owner();
        User invitedUser = friend();
        TripEntity trip = trip(owner);
        TripCollaborationRequestEntity savedInvitation = invitation(trip, owner, invitedUser);

        TripCollaborationRequestResponseDTO responseDTO = mock(TripCollaborationRequestResponseDTO.class);

        mockErrorCode(TRIP_INVITATION_SENT_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME)).thenReturn(trip);
        when(userRepository.findByUsernameAndActive(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(userRepository.findByUsernameAndActive(FRIEND_USERNAME)).thenReturn(Optional.of(invitedUser));
        when(requestRepository.save(any(TripCollaborationRequestEntity.class))).thenReturn(savedInvitation);
        when(requestMapper.toResponseDTO(savedInvitation)).thenReturn(responseDTO);

        CompleteResponse<Object> response = service.sendInvitation(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode()).isEqualTo(TRIP_INVITATION_SENT_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(responseDTO);

        ArgumentCaptor<TripCollaborationRequestEntity> requestCaptor =
                ArgumentCaptor.forClass(TripCollaborationRequestEntity.class);

        verify(requestRepository).save(requestCaptor.capture());

        TripCollaborationRequestEntity captured = requestCaptor.getValue();
        assertThat(captured.getTrip()).isEqualTo(trip);
        assertThat(captured.getRequester()).isEqualTo(owner);
        assertThat(captured.getTargetUser()).isEqualTo(invitedUser);
        assertThat(captured.getRequestedRole()).isEqualTo(TripCollaborationEnum.EDITOR);
        assertThat(captured.getRequestType()).isEqualTo(TripCollaborationEnum.INVITATION);
        assertThat(captured.getStatus()).isEqualTo(TripCollaborationEnum.PENDING);

        verify(tripCollaborationRequestValidator).validateInvitationRequest(TRIP_ID, request);
        verify(tripCollaborationRequestValidator).validateOwnerCannotInviteSelf(owner, invitedUser);
        verify(tripCollaborationRequestValidator).validateUserIsNotAlreadyMember(TRIP_ID, invitedUser);
        verify(tripCollaborationRequestValidator).validateNoPendingRequestBetweenUsers(TRIP_ID, owner, invitedUser);
    }

    @Test
    void sendInvitation_shouldRethrowBusinessException_whenCurrentUserIsNotOwner() {
        SendTripInvitationDTO request = sendInvitationRequest();

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME))
                .thenThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.sendInvitation(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(requestRepository, never()).save(any());
    }

    @Test
    void sendInvitation_shouldThrowUserNotFound_whenInvitedUserDoesNotExist() {
        SendTripInvitationDTO request = sendInvitationRequest();
        User owner = owner();
        TripEntity trip = trip(owner);

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME)).thenReturn(trip);
        when(userRepository.findByUsernameAndActive(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(userRepository.findByUsernameAndActive(FRIEND_USERNAME)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.sendInvitation(TRIP_ID, request)
        );

        assertBusinessException(exception, USER_NOT_FOUND, COMMON.name());

        verify(requestRepository, never()).save(any());
    }

    @Test
    void getMyPendingInvitations_shouldReturnPendingInvitations() {
        User owner = owner();
        User friend = friend();
        TripEntity trip = trip(owner);
        TripCollaborationRequestEntity invitation = invitation(trip, owner, friend);
        TripCollaborationRequestResponseDTO responseDTO = mock(TripCollaborationRequestResponseDTO.class);

        mockErrorCode(TRIP_INVITATIONS_RETRIEVED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(FRIEND_USERNAME);
        when(requestRepository.findAllByTargetUser_UsernameAndRequestTypeAndStatusOrderByCreatedDateDesc(
                FRIEND_USERNAME,
                TripCollaborationEnum.INVITATION,
                TripCollaborationEnum.PENDING
        )).thenReturn(List.of(invitation));
        when(requestMapper.toResponseDTO(invitation)).thenReturn(responseDTO);

        CompleteResponse<Object> response = service.getMyPendingInvitations();

        assertThat(response.getResponseBody().getCode()).isEqualTo(TRIP_INVITATIONS_RETRIEVED_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        List<TripCollaborationRequestResponseDTO> body =
                (List<TripCollaborationRequestResponseDTO>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(responseDTO);
    }

    @Test
    void acceptInvitation_shouldCreateMemberAndAcceptRequest() {
        User owner = owner();
        User friend = friend();
        TripEntity trip = trip(owner);
        TripCollaborationRequestEntity invitation = invitation(trip, owner, friend);

        TripMemberEntity savedMember = member(trip, friend, TripCollaborationEnum.EDITOR);
        TripCollaborationRequestResponseDTO requestResponseDTO = mock(TripCollaborationRequestResponseDTO.class);
        TripMemberResponseDTO memberResponseDTO = mock(TripMemberResponseDTO.class);
        MyTripOverlapWarningDTO warningDTO = mock(MyTripOverlapWarningDTO.class);

        mockErrorCode(TRIP_INVITATION_ACCEPTED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(FRIEND_USERNAME);
        when(requestRepository.findByRequestIdAndRequestTypeAndStatus(
                REQUEST_ID,
                TripCollaborationEnum.INVITATION,
                TripCollaborationEnum.PENDING
        )).thenReturn(Optional.of(invitation));
        when(tripMemberRepository.save(any(TripMemberEntity.class))).thenReturn(savedMember);
        when(requestMapper.toResponseDTO(invitation)).thenReturn(requestResponseDTO);
        when(tripMemberMapper.toResponseDTO(savedMember)).thenReturn(memberResponseDTO);
        when(tripOverlapWarningService.buildWarningsForUser(trip, FRIEND_USERNAME))
                .thenReturn(List.of(warningDTO));

        CompleteResponse<Object> response = service.acceptInvitation(REQUEST_ID);

        assertThat(response.getResponseBody().getCode()).isEqualTo(TRIP_INVITATION_ACCEPTED_SUCCESS.getCode());

        TripCollaborationActionResponseDTO body =
                (TripCollaborationActionResponseDTO) response.getResponseBody().getBody();

        assertThat(body.getRequest()).isEqualTo(requestResponseDTO);
        assertThat(body.getMember()).isEqualTo(memberResponseDTO);
        assertThat(body.getOverlapWarnings()).containsExactly(warningDTO);

        assertThat(invitation.getStatus()).isEqualTo(TripCollaborationEnum.ACCEPTED);
        assertThat(invitation.getRespondedDate()).isNotNull();

        verify(tripCollaborationRequestValidator)
                .validateInvitationBelongsToCurrentUser(invitation, FRIEND_USERNAME);
        verify(requestRepository).save(invitation);
    }

    @Test
    void acceptInvitation_shouldThrowRequestNotFound_whenInvitationDoesNotExist() {
        when(authenticatedUserProvider.getUsername()).thenReturn(FRIEND_USERNAME);
        when(requestRepository.findByRequestIdAndRequestTypeAndStatus(
                REQUEST_ID,
                TripCollaborationEnum.INVITATION,
                TripCollaborationEnum.PENDING
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.acceptInvitation(REQUEST_ID)
        );

        assertBusinessException(exception, TRIP_COLLABORATION_REQUEST_NOT_FOUND, TRIP_MEMBER.name());

        verify(tripMemberRepository, never()).save(any());
    }

    @Test
    void rejectInvitation_shouldMarkInvitationRejected() {
        User owner = owner();
        User friend = friend();
        TripEntity trip = trip(owner);
        TripCollaborationRequestEntity invitation = invitation(trip, owner, friend);

        TripCollaborationRequestResponseDTO responseDTO = mock(TripCollaborationRequestResponseDTO.class);

        mockErrorCode(TRIP_INVITATION_REJECTED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(FRIEND_USERNAME);
        when(requestRepository.findByRequestIdAndRequestTypeAndStatus(
                REQUEST_ID,
                TripCollaborationEnum.INVITATION,
                TripCollaborationEnum.PENDING
        )).thenReturn(Optional.of(invitation));
        when(requestMapper.toResponseDTO(invitation)).thenReturn(responseDTO);

        CompleteResponse<Object> response = service.rejectInvitation(REQUEST_ID);

        assertThat(response.getResponseBody().getCode()).isEqualTo(TRIP_INVITATION_REJECTED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(responseDTO);
        assertThat(invitation.getStatus()).isEqualTo(TripCollaborationEnum.REJECTED);

        verify(requestRepository).save(invitation);
    }

    @Test
    void requestToJoinTrip_shouldCreatePendingJoinRequest_whenInputIsValid() {
        SendTripJoinRequestDTO request = sendJoinRequest();
        User owner = owner();
        User requester = friend();
        TripEntity trip = trip(owner);

        TripCollaborationRequestEntity savedJoinRequest = joinRequest(trip, requester, owner);
        TripCollaborationRequestResponseDTO responseDTO = mock(TripCollaborationRequestResponseDTO.class);

        mockErrorCode(TRIP_JOIN_REQUEST_SENT_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(FRIEND_USERNAME);
        when(userRepository.findByUsernameAndActive(FRIEND_USERNAME)).thenReturn(Optional.of(requester));
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
        when(requestRepository.save(any(TripCollaborationRequestEntity.class))).thenReturn(savedJoinRequest);
        when(requestMapper.toResponseDTO(savedJoinRequest)).thenReturn(responseDTO);

        CompleteResponse<Object> response = service.requestToJoinTrip(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode()).isEqualTo(TRIP_JOIN_REQUEST_SENT_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(responseDTO);

        ArgumentCaptor<TripCollaborationRequestEntity> requestCaptor =
                ArgumentCaptor.forClass(TripCollaborationRequestEntity.class);

        verify(requestRepository).save(requestCaptor.capture());

        TripCollaborationRequestEntity captured = requestCaptor.getValue();
        assertThat(captured.getRequester()).isEqualTo(requester);
        assertThat(captured.getTargetUser()).isEqualTo(owner);
        assertThat(captured.getRequestType()).isEqualTo(TripCollaborationEnum.JOIN_REQUEST);
        assertThat(captured.getStatus()).isEqualTo(TripCollaborationEnum.PENDING);
    }

    @Test
    void requestToJoinTrip_shouldThrowTripNotFound_whenTripDoesNotExist() {
        SendTripJoinRequestDTO request = sendJoinRequest();
        User requester = friend();

        when(authenticatedUserProvider.getUsername()).thenReturn(FRIEND_USERNAME);
        when(userRepository.findByUsernameAndActive(FRIEND_USERNAME)).thenReturn(Optional.of(requester));
        when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.requestToJoinTrip(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_NOT_FOUND, TRIP.name());

        verify(requestRepository, never()).save(any());
    }

    @Test
    void getPendingJoinRequests_shouldReturnRequestsForOwner() {
        User owner = owner();
        User requester = friend();
        TripEntity trip = trip(owner);
        TripCollaborationRequestEntity joinRequest = joinRequest(trip, requester, owner);
        TripCollaborationRequestResponseDTO responseDTO = mock(TripCollaborationRequestResponseDTO.class);

        mockErrorCode(TRIP_JOIN_REQUESTS_RETRIEVED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME)).thenReturn(trip);
        when(requestRepository.findAllByTrip_TripIdAndTargetUser_UsernameAndRequestTypeAndStatusOrderByCreatedDateDesc(
                TRIP_ID,
                OWNER_USERNAME,
                TripCollaborationEnum.JOIN_REQUEST,
                TripCollaborationEnum.PENDING
        )).thenReturn(List.of(joinRequest));
        when(requestMapper.toResponseDTO(joinRequest)).thenReturn(responseDTO);

        CompleteResponse<Object> response = service.getPendingJoinRequests(TRIP_ID);

        assertThat(response.getResponseBody().getCode()).isEqualTo(TRIP_JOIN_REQUESTS_RETRIEVED_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        List<TripCollaborationRequestResponseDTO> body =
                (List<TripCollaborationRequestResponseDTO>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(responseDTO);
    }

    @Test
    void acceptJoinRequest_shouldCreateMemberAndAcceptRequest() {
        User owner = owner();
        User requester = friend();
        TripEntity trip = trip(owner);
        TripCollaborationRequestEntity joinRequest = joinRequest(trip, requester, owner);

        TripMemberEntity savedMember = member(trip, requester, TripCollaborationEnum.VIEWER);
        TripCollaborationRequestResponseDTO requestResponseDTO = mock(TripCollaborationRequestResponseDTO.class);
        TripMemberResponseDTO memberResponseDTO = mock(TripMemberResponseDTO.class);

        mockErrorCode(TRIP_JOIN_REQUEST_ACCEPTED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(requestRepository.findByRequestIdAndRequestTypeAndStatus(
                REQUEST_ID,
                TripCollaborationEnum.JOIN_REQUEST,
                TripCollaborationEnum.PENDING
        )).thenReturn(Optional.of(joinRequest));
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME)).thenReturn(trip);
        when(tripMemberRepository.save(any(TripMemberEntity.class))).thenReturn(savedMember);
        when(requestMapper.toResponseDTO(joinRequest)).thenReturn(requestResponseDTO);
        when(tripMemberMapper.toResponseDTO(savedMember)).thenReturn(memberResponseDTO);

        CompleteResponse<Object> response = service.acceptJoinRequest(REQUEST_ID);

        assertThat(response.getResponseBody().getCode()).isEqualTo(TRIP_JOIN_REQUEST_ACCEPTED_SUCCESS.getCode());

        TripCollaborationActionResponseDTO body =
                (TripCollaborationActionResponseDTO) response.getResponseBody().getBody();

        assertThat(body.getRequest()).isEqualTo(requestResponseDTO);
        assertThat(body.getMember()).isEqualTo(memberResponseDTO);
        assertThat(body.getOverlapWarnings()).isEmpty();

        assertThat(joinRequest.getStatus()).isEqualTo(TripCollaborationEnum.ACCEPTED);

        verify(tripCollaborationRequestValidator)
                .validateJoinRequestBelongsToCurrentOwner(joinRequest, OWNER_USERNAME);
        verify(tripAccessService).getTripIfOwner(TRIP_ID, OWNER_USERNAME);
        verify(requestRepository).save(joinRequest);
    }

    @Test
    void rejectJoinRequest_shouldMarkJoinRequestRejected() {
        User owner = owner();
        User requester = friend();
        TripEntity trip = trip(owner);
        TripCollaborationRequestEntity joinRequest = joinRequest(trip, requester, owner);

        TripCollaborationRequestResponseDTO responseDTO = mock(TripCollaborationRequestResponseDTO.class);

        mockErrorCode(TRIP_JOIN_REQUEST_REJECTED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(requestRepository.findByRequestIdAndRequestTypeAndStatus(
                REQUEST_ID,
                TripCollaborationEnum.JOIN_REQUEST,
                TripCollaborationEnum.PENDING
        )).thenReturn(Optional.of(joinRequest));
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME)).thenReturn(trip);
        when(requestMapper.toResponseDTO(joinRequest)).thenReturn(responseDTO);

        CompleteResponse<Object> response = service.rejectJoinRequest(REQUEST_ID);

        assertThat(response.getResponseBody().getCode()).isEqualTo(TRIP_JOIN_REQUEST_REJECTED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(responseDTO);
        assertThat(joinRequest.getStatus()).isEqualTo(TripCollaborationEnum.REJECTED);

        verify(requestRepository).save(joinRequest);
    }

    private SendTripInvitationDTO sendInvitationRequest() {
        SendTripInvitationDTO request = new SendTripInvitationDTO();
        request.setUsername(FRIEND_USERNAME);
        request.setRole(TripCollaborationEnum.EDITOR);
        return request;
    }

    private SendTripJoinRequestDTO sendJoinRequest() {
        SendTripJoinRequestDTO request = new SendTripJoinRequestDTO();
        request.setRole(TripCollaborationEnum.VIEWER);
        return request;
    }

    private User owner() {
        return user(1L, OWNER_USERNAME);
    }

    private User friend() {
        return user(2L, FRIEND_USERNAME);
    }

    private User user(long userId, String username) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(username.toLowerCase() + "@example.com");
        user.setActive(true);
        return user;
    }

    private TripEntity trip(User owner) {
        TripEntity trip = new TripEntity();
        trip.setTripId(TRIP_ID);
        trip.setTripName("Adelaide Trip");
        trip.setDestination("Adelaide");
        trip.setStartDate(LocalDateTime.of(2026, 7, 10, 9, 0));
        trip.setEndDate(LocalDateTime.of(2026, 7, 15, 18, 0));
        trip.setUser(owner);
        return trip;
    }

    private TripCollaborationRequestEntity invitation(
            TripEntity trip,
            User owner,
            User invitedUser
    ) {
        TripCollaborationRequestEntity request = new TripCollaborationRequestEntity();
        request.setRequestId(REQUEST_ID);
        request.setTrip(trip);
        request.setRequester(owner);
        request.setTargetUser(invitedUser);
        request.setRequestedRole(TripCollaborationEnum.EDITOR);
        request.setRequestType(TripCollaborationEnum.INVITATION);
        request.setStatus(TripCollaborationEnum.PENDING);
        request.setCreatedDate(LocalDateTime.now());
        return request;
    }

    private TripCollaborationRequestEntity joinRequest(
            TripEntity trip,
            User requester,
            User owner
    ) {
        TripCollaborationRequestEntity request = new TripCollaborationRequestEntity();
        request.setRequestId(REQUEST_ID);
        request.setTrip(trip);
        request.setRequester(requester);
        request.setTargetUser(owner);
        request.setRequestedRole(TripCollaborationEnum.VIEWER);
        request.setRequestType(TripCollaborationEnum.JOIN_REQUEST);
        request.setStatus(TripCollaborationEnum.PENDING);
        request.setCreatedDate(LocalDateTime.now());
        return request;
    }

    private TripMemberEntity member(
            TripEntity trip,
            User user,
            TripCollaborationEnum role
    ) {
        TripMemberEntity member = new TripMemberEntity();
        member.setTripMemberId(3L);
        member.setTrip(trip);
        member.setUser(user);
        member.setRole(role);
        member.setCreatedDate(LocalDateTime.now());
        return member;
    }

    private void mockErrorCode(ErrorCodeEnum errorCodeEnum, String flow) {
        ErrorCodeEntity entity = new ErrorCodeEntity();
        entity.setErrorCode(errorCodeEnum.getCode());
        entity.setErrorMessage(errorCodeEnum.getMessage());
        entity.setErrorEnum(errorCodeEnum.name());
        entity.setFlow(flow);
        entity.setCreatedDate(LocalDateTime.now());

        when(errorCodeRepository.findByErrorEnumAndFlow(errorCodeEnum.name(), flow))
                .thenReturn(Optional.of(entity));
    }

    private void assertBusinessException(
            BusinessException exception,
            ErrorCodeEnum expectedErrorCode,
            String expectedFlow
    ) {
        assertThat(exception.getErrorCodeEnum()).isEqualTo(expectedErrorCode);
        assertThat(exception.getFlow()).isEqualTo(expectedFlow);
    }
}