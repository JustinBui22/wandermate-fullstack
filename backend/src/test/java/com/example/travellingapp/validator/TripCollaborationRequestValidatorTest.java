package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.SendTripInvitationDTO;
import com.example.travellingapp.dto.request.SendTripJoinRequestDTO;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.collaboration.TripCollaborationRequestRepository;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripCollaborationRequestValidatorTest {

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private TripCollaborationRequestRepository requestRepository;

    private TripCollaborationRequestValidator validator;

    private static final Long TRIP_ID = 1L;

    @BeforeEach
    void setUp() {
        validator = new TripCollaborationRequestValidator(
                tripMemberRepository,
                requestRepository
        );
    }

    @Test
    void validateInvitationRequest_shouldPass_whenRoleIsEditor() {
        SendTripInvitationDTO request = new SendTripInvitationDTO();
        request.setUsername("FriendUser");
        request.setRole(TripEnum.EDITOR);

        assertDoesNotThrow(() -> validator.validateInvitationRequest(TRIP_ID, request));
    }

    @Test
    void validateInvitationRequest_shouldPass_whenRoleIsViewer() {
        SendTripInvitationDTO request = new SendTripInvitationDTO();
        request.setUsername("FriendUser");
        request.setRole(TripEnum.VIEWER);

        assertDoesNotThrow(() -> validator.validateInvitationRequest(TRIP_ID, request));
    }

    @Test
    void validateInvitationRequest_shouldThrowInvalidInput_whenUsernameIsBlank() {
        SendTripInvitationDTO request = new SendTripInvitationDTO();
        request.setUsername("   ");
        request.setRole(TripEnum.EDITOR);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateInvitationRequest(TRIP_ID, request)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
    }

    @Test
    void validateInvitationRequest_shouldThrowInvalidInput_whenRoleIsNull() {
        SendTripInvitationDTO request = new SendTripInvitationDTO();
        request.setUsername("FriendUser");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateInvitationRequest(TRIP_ID, request)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
    }

    @Test
    void validateInvitationRequest_shouldThrow_whenRoleIsOwner() {
        SendTripInvitationDTO request = new SendTripInvitationDTO();
        request.setUsername("FriendUser");
        request.setRole(TripEnum.OWNER);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateInvitationRequest(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_OWNER_ROLE_CANNOT_BE_CHANGED, TRIP_MEMBER.name());
    }

    @Test
    void validateInvitationRequest_shouldThrow_whenRoleIsPendingBecauseItIsNotMemberRole() {
        SendTripInvitationDTO request = new SendTripInvitationDTO();
        request.setUsername("FriendUser");
        request.setRole(TripEnum.PENDING);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateInvitationRequest(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_OWNER_ROLE_CANNOT_BE_CHANGED, TRIP_MEMBER.name());
    }

    @Test
    void validateJoinRequest_shouldPass_whenRoleIsViewer() {
        SendTripJoinRequestDTO request = new SendTripJoinRequestDTO();
        request.setRole(TripEnum.VIEWER);

        assertDoesNotThrow(() -> validator.validateJoinRequest(TRIP_ID, request));
    }

    @Test
    void validateJoinRequest_shouldThrowInvalidInput_whenRequestIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateJoinRequest(TRIP_ID, null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
    }

    @Test
    void validateRequestId_shouldThrowInvalidInput_whenRequestIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateRequestId(null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
    }

    @Test
    void validateOwnerCannotInviteSelf_shouldThrow_whenSameUser() {
        User owner = user(1L, "OwnerUser");
        User invitedUser = user(1L, "OwnerUser");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateOwnerCannotInviteSelf(owner, invitedUser)
        );

        assertBusinessException(exception, TRIP_CANNOT_INVITE_SELF, TRIP_MEMBER.name());
    }

    @Test
    void validateOwnerCannotRequestToJoinOwnTrip_shouldThrow_whenSameUser() {
        User owner = user(1L, "OwnerUser");
        User requester = user(1L, "OwnerUser");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateOwnerCannotRequestToJoinOwnTrip(owner, requester)
        );

        assertBusinessException(exception, TRIP_OWNER_CANNOT_REQUEST_TO_JOIN_OWN_TRIP, TRIP_MEMBER.name());
    }

    @Test
    void validateUserIsNotAlreadyMember_shouldThrow_whenUserAlreadyMember() {
        User user = user(2L, "FriendUser");

        when(tripMemberRepository.existsByTrip_TripIdAndUser_UserId(TRIP_ID, user.getUserId()))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateUserIsNotAlreadyMember(TRIP_ID, user)
        );

        assertBusinessException(exception, TRIP_MEMBER_ALREADY_EXISTS, TRIP_MEMBER.name());
    }

    @Test
    void validateNoPendingRequestBetweenUsers_shouldThrow_whenFirstUserAlreadySentPendingRequest() {
        User owner = user(1L, "OwnerUser");
        User invitedUser = user(2L, "FriendUser");

        when(requestRepository.existsByTrip_TripIdAndRequester_UserIdAndTargetUser_UserIdAndStatus(
                TRIP_ID,
                owner.getUserId(),
                invitedUser.getUserId(),
                TripEnum.PENDING
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateNoPendingRequestBetweenUsers(TRIP_ID, owner, invitedUser)
        );

        assertBusinessException(exception, TRIP_COLLABORATION_REQUEST_ALREADY_EXISTS, TRIP_MEMBER.name());
    }

    @Test
    void validateNoPendingRequestBetweenUsers_shouldThrow_whenSecondUserAlreadySentPendingRequest() {
        User owner = user(1L, "OwnerUser");
        User invitedUser = user(2L, "FriendUser");

        when(requestRepository.existsByTrip_TripIdAndRequester_UserIdAndTargetUser_UserIdAndStatus(
                TRIP_ID,
                owner.getUserId(),
                invitedUser.getUserId(),
                TripEnum.PENDING
        )).thenReturn(false);

        when(requestRepository.existsByTrip_TripIdAndRequester_UserIdAndTargetUser_UserIdAndStatus(
                TRIP_ID,
                invitedUser.getUserId(),
                owner.getUserId(),
                TripEnum.PENDING
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateNoPendingRequestBetweenUsers(TRIP_ID, owner, invitedUser)
        );

        assertBusinessException(exception, TRIP_COLLABORATION_REQUEST_ALREADY_EXISTS, TRIP_MEMBER.name());
    }

    @Test
    void validateInvitationBelongsToCurrentUser_shouldThrow_whenCurrentUserIsNotTargetUser() {
        TripCollaborationRequestEntity invitation = new TripCollaborationRequestEntity();
        invitation.setTargetUser(user(2L, "FriendUser"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateInvitationBelongsToCurrentUser(invitation, "OtherUser")
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());
    }

    @Test
    void validateJoinRequestBelongsToCurrentOwner_shouldThrow_whenCurrentUserIsNotTargetOwner() {
        TripCollaborationRequestEntity joinRequest = new TripCollaborationRequestEntity();
        joinRequest.setTargetUser(user(1L, "OwnerUser"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateJoinRequestBelongsToCurrentOwner(joinRequest, "OtherUser")
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());
    }

    private User user(long userId, String username) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setActive(true);
        return user;
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