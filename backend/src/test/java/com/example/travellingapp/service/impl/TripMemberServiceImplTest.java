package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.AddTripMemberDTO;
import com.example.travellingapp.dto.request.update.UpdateTripMemberRoleDTO;
import com.example.travellingapp.dto.response.TripMemberResponseDTO;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripMemberMapper;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.validator.TripMemberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripMemberServiceImplTest {

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private TripMemberMapper tripMemberMapper;

    @Mock
    private TripMemberValidator tripMemberValidator;

    private TripMemberServiceImpl tripMemberService;

    private static final Long TRIP_ID = 1L;
    private static final Long TRIP_MEMBER_ID = 10L;
    private static final Long OWNER_MEMBER_ID = 11L;
    private static final String OWNER_USERNAME = "JustinBo123";
    private static final String TARGET_USERNAME = "FriendUser";

    @BeforeEach
    void setUp() {
        tripMemberService = new TripMemberServiceImpl(
                tripMemberRepository,
                userRepository,
                errorCodeRepository,
                authenticatedUserProvider,
                tripAccessService,
                tripMemberMapper,
                tripMemberValidator
        );
    }

    // -------------------------------------------------------------------------
    // getTripMembers()
    // -------------------------------------------------------------------------

    @Test
    void getTripMembers_shouldReturnMembers_whenCurrentUserCanViewTrip() {
        TripMemberEntity ownerMember = tripMember(OWNER_MEMBER_ID, ownerUser(), TripEnum.OWNER);
        TripMemberEntity editorMember = tripMember(TRIP_MEMBER_ID, targetUser(), TripEnum.EDITOR);

        TripMemberResponseDTO ownerResponse = mock(TripMemberResponseDTO.class);
        TripMemberResponseDTO editorResponse = mock(TripMemberResponseDTO.class);

        mockErrorCode(TRIP_MEMBERS_RETRIEVED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripMemberRepository.findAllByTrip_TripId(TRIP_ID))
                .thenReturn(List.of(ownerMember, editorMember));
        when(tripMemberMapper.toResponseDTO(ownerMember))
                .thenReturn(ownerResponse);
        when(tripMemberMapper.toResponseDTO(editorMember))
                .thenReturn(editorResponse);

        CompleteResponse<Object> response = tripMemberService.getTripMembers(TRIP_ID);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_MEMBERS_RETRIEVED_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        List<TripMemberResponseDTO> body =
                (List<TripMemberResponseDTO>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(ownerResponse, editorResponse);

        verify(tripMemberValidator).validateTripId(TRIP_ID);
        verify(tripAccessService).assertCanView(TRIP_ID, OWNER_USERNAME);
    }

    @Test
    void getTripMembers_shouldThrowInvalidInput_whenTripIdIsNull() {
        doThrow(new BusinessException(INVALID_INPUT, COMMON.name()))
                .when(tripMemberValidator)
                .validateTripId(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.getTripMembers(null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripAccessService, never()).assertCanView(anyLong(), anyString());
        verify(tripMemberRepository, never()).findAllByTrip_TripId(anyLong());
    }

    @Test
    void getTripMembers_shouldThrowAccessDenied_whenCurrentUserCannotViewTrip() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);

        doThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()))
                .when(tripAccessService)
                .assertCanView(TRIP_ID, OWNER_USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.getTripMembers(TRIP_ID)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(tripMemberRepository, never()).findAllByTrip_TripId(anyLong());
    }

    // -------------------------------------------------------------------------
    // addTripMember()
    // -------------------------------------------------------------------------

    @Test
    void addTripMember_shouldAddEditorMember_whenCurrentUserIsOwner() {
        AddTripMemberDTO request = addRequest("EDITOR");
        TripEntity trip = trip();
        User targetUser = targetUser();
        TripMemberResponseDTO responseDTO = mock(TripMemberResponseDTO.class);

        mockErrorCode(TRIP_MEMBER_ADDED_SUCCESS, TRIP_MEMBER.name());

        when(tripMemberValidator.validateAddTripMemberInput(TRIP_ID, request))
                .thenReturn(TARGET_USERNAME);
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME))
                .thenReturn(trip);
        when(userRepository.findByUsernameAndActive(TARGET_USERNAME))
                .thenReturn(Optional.of(targetUser));
        when(tripMemberRepository.existsByTrip_TripIdAndUser_UserId(TRIP_ID, targetUser.getUserId()))
                .thenReturn(false);
        when(tripMemberMapper.toResponseDTO(any(TripMemberEntity.class)))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = tripMemberService.addTripMember(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_MEMBER_ADDED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);

        ArgumentCaptor<TripMemberEntity> memberCaptor =
                ArgumentCaptor.forClass(TripMemberEntity.class);

        verify(tripMemberRepository).save(memberCaptor.capture());

        TripMemberEntity savedMember = memberCaptor.getValue();

        assertThat(savedMember.getTrip()).isEqualTo(trip);
        assertThat(savedMember.getUser()).isEqualTo(targetUser);
        assertThat(savedMember.getRole()).isEqualTo(TripEnum.EDITOR);
        assertThat(savedMember.getCreatedDate()).isNotNull();
    }

    @Test
    void addTripMember_shouldAddViewerMember_whenCurrentUserIsOwner() {
        AddTripMemberDTO request = addRequest("VIEWER");
        TripEntity trip = trip();
        User targetUser = targetUser();

        mockErrorCode(TRIP_MEMBER_ADDED_SUCCESS, TRIP_MEMBER.name());

        when(tripMemberValidator.validateAddTripMemberInput(TRIP_ID, request))
                .thenReturn(TARGET_USERNAME);
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME))
                .thenReturn(trip);
        when(userRepository.findByUsernameAndActive(TARGET_USERNAME))
                .thenReturn(Optional.of(targetUser));
        when(tripMemberRepository.existsByTrip_TripIdAndUser_UserId(TRIP_ID, targetUser.getUserId()))
                .thenReturn(false);
        when(tripMemberMapper.toResponseDTO(any(TripMemberEntity.class)))
                .thenReturn(mock(TripMemberResponseDTO.class));

        CompleteResponse<Object> response = tripMemberService.addTripMember(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_MEMBER_ADDED_SUCCESS.getCode());

        ArgumentCaptor<TripMemberEntity> memberCaptor =
                ArgumentCaptor.forClass(TripMemberEntity.class);

        verify(tripMemberRepository).save(memberCaptor.capture());

        assertThat(memberCaptor.getValue().getRole()).isEqualTo(TripEnum.VIEWER);
    }

    @Test
    void addTripMember_shouldThrowInvalidInput_whenRoleIsOwner() {
        AddTripMemberDTO request = addRequest("OWNER");

        when(tripMemberValidator.validateAddTripMemberInput(TRIP_ID, request))
                .thenThrow(new BusinessException(INVALID_INPUT, TRIP_MEMBER.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.addTripMember(TRIP_ID, request)
        );

        assertBusinessException(exception, INVALID_INPUT, TRIP_MEMBER.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    @Test
    void addTripMember_shouldThrowUserNotFound_whenTargetUserDoesNotExist() {
        AddTripMemberDTO request = addRequest("EDITOR");

        when(tripMemberValidator.validateAddTripMemberInput(TRIP_ID, request))
                .thenReturn(TARGET_USERNAME);
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME))
                .thenReturn(trip());
        when(userRepository.findByUsernameAndActive(TARGET_USERNAME))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.addTripMember(TRIP_ID, request)
        );

        assertBusinessException(exception, USER_NOT_FOUND, COMMON.name());

        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    @Test
    void addTripMember_shouldThrowTripMemberAlreadyExists_whenTargetUserAlreadyMember() {
        AddTripMemberDTO request = addRequest("EDITOR");
        User targetUser = targetUser();

        when(tripMemberValidator.validateAddTripMemberInput(TRIP_ID, request))
                .thenReturn(TARGET_USERNAME);
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME))
                .thenReturn(trip());
        when(userRepository.findByUsernameAndActive(TARGET_USERNAME))
                .thenReturn(Optional.of(targetUser));
        when(tripMemberRepository.existsByTrip_TripIdAndUser_UserId(TRIP_ID, targetUser.getUserId()))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.addTripMember(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_MEMBER_ALREADY_EXISTS, TRIP_MEMBER.name());

        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    @Test
    void addTripMember_shouldThrowAccessDenied_whenCurrentUserIsNotOwner() {
        AddTripMemberDTO request = addRequest("EDITOR");

        when(tripMemberValidator.validateAddTripMemberInput(TRIP_ID, request))
                .thenReturn(TARGET_USERNAME);
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME))
                .thenThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.addTripMember(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(userRepository, never()).findByUsernameAndActive(anyString());
        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    // -------------------------------------------------------------------------
    // updateTripMemberRole()
    // -------------------------------------------------------------------------

    @Test
    void updateTripMemberRole_shouldUpdateRole_whenCurrentUserIsOwner() {
        UpdateTripMemberRoleDTO request = updateRequest("VIEWER");
        TripMemberEntity member = tripMember(TRIP_MEMBER_ID, targetUser(), TripEnum.EDITOR);
        TripMemberResponseDTO responseDTO = mock(TripMemberResponseDTO.class);

        mockErrorCode(TRIP_MEMBER_ROLE_UPDATED_SUCCESS, TRIP_MEMBER.name());

        when(tripMemberValidator.validateUpdateTripMemberRoleInput(TRIP_ID, TRIP_MEMBER_ID, request))
                .thenReturn(TripEnum.VIEWER);
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripMemberRepository.findByTripMemberIdAndTrip_TripId(TRIP_MEMBER_ID, TRIP_ID))
                .thenReturn(Optional.of(member));
        when(tripMemberMapper.toResponseDTO(member))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = tripMemberService.updateTripMemberRole(
                TRIP_ID,
                TRIP_MEMBER_ID,
                request
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_MEMBER_ROLE_UPDATED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);

        assertThat(member.getRole()).isEqualTo(TripEnum.VIEWER);
        assertThat(member.getModifiedDate()).isNotNull();

        verify(tripAccessService).assertIsOwner(TRIP_ID, OWNER_USERNAME);
        verify(tripMemberRepository).save(member);
    }

    @Test
    void updateTripMemberRole_shouldThrowOwnerRoleCannotBeChanged_whenTripMemberIsOwner() {
        UpdateTripMemberRoleDTO request = updateRequest("VIEWER");
        TripMemberEntity ownerMember = tripMember(OWNER_MEMBER_ID, ownerUser(), TripEnum.OWNER);

        when(tripMemberValidator.validateUpdateTripMemberRoleInput(TRIP_ID, OWNER_MEMBER_ID, request))
                .thenReturn(TripEnum.VIEWER);
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripMemberRepository.findByTripMemberIdAndTrip_TripId(OWNER_MEMBER_ID, TRIP_ID))
                .thenReturn(Optional.of(ownerMember));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.updateTripMemberRole(TRIP_ID, OWNER_MEMBER_ID, request)
        );

        assertBusinessException(exception, TRIP_OWNER_ROLE_CANNOT_BE_CHANGED, TRIP_MEMBER.name());

        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    @Test
    void updateTripMemberRole_shouldThrowTripMemberNotFound_whenTripMemberDoesNotExist() {
        UpdateTripMemberRoleDTO request = updateRequest("VIEWER");

        when(tripMemberValidator.validateUpdateTripMemberRoleInput(TRIP_ID, TRIP_MEMBER_ID, request))
                .thenReturn(TripEnum.VIEWER);
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripMemberRepository.findByTripMemberIdAndTrip_TripId(TRIP_MEMBER_ID, TRIP_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.updateTripMemberRole(TRIP_ID, TRIP_MEMBER_ID, request)
        );

        assertBusinessException(exception, TRIP_MEMBER_NOT_FOUND, TRIP_MEMBER.name());

        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    @Test
    void updateTripMemberRole_shouldThrowInvalidInput_whenNewRoleIsOwner() {
        UpdateTripMemberRoleDTO request = updateRequest("OWNER");

        when(tripMemberValidator.validateUpdateTripMemberRoleInput(TRIP_ID, TRIP_MEMBER_ID, request))
                .thenThrow(new BusinessException(INVALID_INPUT, TRIP_MEMBER.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.updateTripMemberRole(TRIP_ID, TRIP_MEMBER_ID, request)
        );

        assertBusinessException(exception, INVALID_INPUT, TRIP_MEMBER.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    @Test
    void updateTripMemberRole_shouldThrowAccessDenied_whenCurrentUserIsNotOwner() {
        UpdateTripMemberRoleDTO request = updateRequest("VIEWER");

        when(tripMemberValidator.validateUpdateTripMemberRoleInput(TRIP_ID, TRIP_MEMBER_ID, request))
                .thenReturn(TripEnum.VIEWER);
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        doThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()))
                .when(tripAccessService)
                .assertIsOwner(TRIP_ID, OWNER_USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.updateTripMemberRole(TRIP_ID, TRIP_MEMBER_ID, request)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(tripMemberRepository, never()).findByTripMemberIdAndTrip_TripId(anyLong(), anyLong());
        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    // -------------------------------------------------------------------------
    // removeTripMember()
    // -------------------------------------------------------------------------

    @Test
    void removeTripMember_shouldRemoveMember_whenCurrentUserIsOwner() {
        TripMemberEntity member = tripMember(TRIP_MEMBER_ID, targetUser(), TripEnum.VIEWER);

        mockErrorCode(TRIP_MEMBER_REMOVED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripMemberRepository.findByTripMemberIdAndTrip_TripId(TRIP_MEMBER_ID, TRIP_ID))
                .thenReturn(Optional.of(member));

        CompleteResponse<Object> response = tripMemberService.removeTripMember(
                TRIP_ID,
                TRIP_MEMBER_ID
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_MEMBER_REMOVED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isNull();

        verify(tripMemberValidator).validateRemoveTripMemberInput(TRIP_ID, TRIP_MEMBER_ID);
        verify(tripAccessService).assertIsOwner(TRIP_ID, OWNER_USERNAME);
        verify(tripMemberRepository).delete(member);
    }

    @Test
    void removeTripMember_shouldThrowOwnerCannotBeRemoved_whenTripMemberIsOwner() {
        TripMemberEntity ownerMember = tripMember(OWNER_MEMBER_ID, ownerUser(), TripEnum.OWNER);

        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripMemberRepository.findByTripMemberIdAndTrip_TripId(OWNER_MEMBER_ID, TRIP_ID))
                .thenReturn(Optional.of(ownerMember));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.removeTripMember(TRIP_ID, OWNER_MEMBER_ID)
        );

        assertBusinessException(exception, TRIP_OWNER_CANNOT_BE_REMOVED, TRIP_MEMBER.name());

        verify(tripMemberRepository, never()).delete(any(TripMemberEntity.class));
    }

    @Test
    void removeTripMember_shouldThrowTripMemberNotFound_whenTripMemberDoesNotExist() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        when(tripMemberRepository.findByTripMemberIdAndTrip_TripId(TRIP_MEMBER_ID, TRIP_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.removeTripMember(TRIP_ID, TRIP_MEMBER_ID)
        );

        assertBusinessException(exception, TRIP_MEMBER_NOT_FOUND, TRIP_MEMBER.name());

        verify(tripMemberRepository, never()).delete(any(TripMemberEntity.class));
    }

    @Test
    void removeTripMember_shouldThrowAccessDenied_whenCurrentUserIsNotOwner() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(OWNER_USERNAME);
        doThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()))
                .when(tripAccessService)
                .assertIsOwner(TRIP_ID, OWNER_USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.removeTripMember(TRIP_ID, TRIP_MEMBER_ID)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(tripMemberRepository, never()).findByTripMemberIdAndTrip_TripId(anyLong(), anyLong());
        verify(tripMemberRepository, never()).delete(any(TripMemberEntity.class));
    }

    @Test
    void removeTripMember_shouldThrowInvalidInput_whenTripMemberIdIsNull() {
        doThrow(new BusinessException(INVALID_INPUT, COMMON.name()))
                .when(tripMemberValidator)
                .validateRemoveTripMemberInput(TRIP_ID, null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripMemberService.removeTripMember(TRIP_ID, null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripMemberRepository, never()).delete(any(TripMemberEntity.class));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private AddTripMemberDTO addRequest(String role) {
        AddTripMemberDTO request = new AddTripMemberDTO();
        request.setUsername(TARGET_USERNAME);
        request.setRole(role);
        return request;
    }

    private UpdateTripMemberRoleDTO updateRequest(String role) {
        UpdateTripMemberRoleDTO request = new UpdateTripMemberRoleDTO();
        request.setRole(role);
        return request;
    }

    private TripEntity trip() {
        TripEntity trip = new TripEntity();
        trip.setTripId(TRIP_ID);
        trip.setTripName("Adelaide Trip");
        trip.setDestination("Adelaide");
        trip.setStartDate(LocalDate.of(2026, 7, 10));
        trip.setEndDate(LocalDate.of(2026, 7, 15));
        trip.setCreatedDate(Instant.now());
        trip.setUser(ownerUser());
        return trip;
    }

    private User ownerUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername(OWNER_USERNAME);
        user.setEmail("justin@example.com");
        user.setActive(true);
        return user;
    }

    private User targetUser() {
        User user = new User();
        user.setUserId(2L);
        user.setUsername(TARGET_USERNAME);
        user.setEmail("friend@example.com");
        user.setActive(true);
        return user;
    }

    private TripMemberEntity tripMember(
            Long tripMemberId,
            User user,
            TripEnum role
    ) {
        TripMemberEntity tripMember = new TripMemberEntity();
        tripMember.setTripMemberId(tripMemberId);
        tripMember.setTrip(trip());
        tripMember.setUser(user);
        tripMember.setRole(role);
        tripMember.setCreatedDate(Instant.now());
        return tripMember;
    }

    private void mockErrorCode(ErrorCodeEnum errorCodeEnum, String flow) {
        ErrorCodeEntity entity = new ErrorCodeEntity();
        entity.setErrorCode(errorCodeEnum.getCode());
        entity.setErrorMessage(errorCodeEnum.getMessage());
        entity.setErrorEnum(errorCodeEnum.name());
        entity.setFlow(flow);
        entity.setCreatedDate(Instant.now());

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