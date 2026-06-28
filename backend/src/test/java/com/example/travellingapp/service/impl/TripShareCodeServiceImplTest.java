package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.GenerateTripShareCodeRequest;
import com.example.travellingapp.dto.response.TripCollaborationRequestResponseDTO;
import com.example.travellingapp.dto.response.TripShareCodePreviewResponseDTO;
import com.example.travellingapp.dto.response.TripShareCodeResponseDTO;
import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import com.example.travellingapp.entity.collaboration.TripShareCodeAttemptEntity;
import com.example.travellingapp.entity.collaboration.TripShareCodeEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripCollaborationRequestMapper;
import com.example.travellingapp.mapper.TripShareCodeMapper;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripCollaborationRequestRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeAttemptRepository;
import com.example.travellingapp.repository.collaboration.TripShareCodeRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.validator.TripShareCodeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripShareCodeServiceImplTest {

    @Mock
    private TripShareCodeRepository tripShareCodeRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private TripShareCodeAttemptRepository tripShareCodeAttemptRepository;

    @Mock
    private TripCollaborationRequestRepository tripCollaborationRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private TripCollaborationRequestMapper tripCollaborationRequestMapper;

    @Mock
    private TripShareCodeMapper tripShareCodeMapper;

    @Mock
    private TripShareCodeValidator tripShareCodeValidator;

    private TripShareCodeServiceImpl service;

    private static final Long TRIP_ID = 1L;
    private static final long OWNER_USER_ID = 10L;
    private static final long REQUESTER_USER_ID = 20L;
    private static final Long SHARE_CODE_ID = 100L;
    private static final Long REQUEST_ID = 200L;

    private static final String OWNER_USERNAME = "owner";
    private static final String REQUESTER_USERNAME = "requester";
    private static final String RAW_CODE = "wm-abc12345";
    private static final String NORMALIZED_CODE = "WM-ABC12345";
    private static final String INVITE_LINK_PREFIX_VALUE = "wandermate://join-trip?code=";

    @BeforeEach
    void setUp() {
        service = new TripShareCodeServiceImpl(
                tripShareCodeRepository,
                configurationRepository,
                tripShareCodeAttemptRepository,
                tripCollaborationRequestRepository,
                userRepository,
                errorCodeRepository,
                authenticatedUserProvider,
                tripAccessService,
                tripCollaborationRequestMapper,
                tripShareCodeMapper,
                tripShareCodeValidator
        );
    }

    @Test
    void regenerateShareCode_shouldCreateNewShareCode_whenNoActiveCodeExists() {
        User owner = user(OWNER_USER_ID, OWNER_USERNAME);
        TripEntity trip = trip(owner);
        GenerateTripShareCodeRequest request = generateRequest(TripEnum.VIEWER);

        TripShareCodeEntity newShareCode = activeShareCode(trip, owner);
        TripShareCodeResponseDTO responseDTO = shareCodeResponseDTO();

        mockErrorCode(TRIP_SHARE_CODE_CREATED_SUCCESS, TRIP_MEMBER.name());
        mockInviteLinkPrefix();

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME)).thenReturn(trip);
        when(userRepository.findByUsernameAndActive(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(tripShareCodeValidator.resolveDefaultRole(request)).thenReturn(TripEnum.VIEWER);
        when(tripShareCodeRepository.findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                TRIP_ID,
                TripEnum.ACTIVE
        )).thenReturn(Optional.empty());
        when(tripShareCodeRepository.existsByCode(anyString())).thenReturn(false);
        when(tripShareCodeMapper.toNewShareCodeEntity(
                eq(trip),
                anyString(),
                eq(owner),
                eq(TripEnum.VIEWER),
                any(LocalDateTime.class),
                eq(24L)
        )).thenReturn(newShareCode);
        when(tripShareCodeRepository.save(newShareCode)).thenReturn(newShareCode);
        when(tripShareCodeMapper.toResponseDTO(newShareCode, INVITE_LINK_PREFIX_VALUE))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response =
                service.regenerateShareCode(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_SHARE_CODE_CREATED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(responseDTO);

        verify(tripShareCodeValidator).validateTripId(TRIP_ID);
        verify(tripAccessService).getTripIfOwner(TRIP_ID, OWNER_USERNAME);
        verify(tripShareCodeRepository).save(newShareCode);
        verify(tripShareCodeMapper).toResponseDTO(newShareCode, INVITE_LINK_PREFIX_VALUE);
    }

    @Test
    void regenerateShareCode_shouldRevokeOldActiveCode_whenCooldownPassed() {
        User owner = user(OWNER_USER_ID, OWNER_USERNAME);
        TripEntity trip = trip(owner);
        GenerateTripShareCodeRequest request = generateRequest(TripEnum.EDITOR);

        TripShareCodeEntity oldActiveCode = activeShareCode(trip, owner);
        oldActiveCode.setCreatedDate(LocalDateTime.now().minusMinutes(2));
        oldActiveCode.setExpiresAt(LocalDateTime.now().plusHours(1));

        TripShareCodeEntity newShareCode = activeShareCode(trip, owner);
        newShareCode.setCode("WM-NEW12345");

        TripShareCodeResponseDTO responseDTO = shareCodeResponseDTO();

        mockErrorCode(TRIP_SHARE_CODE_CREATED_SUCCESS, TRIP_MEMBER.name());
        mockInviteLinkPrefix();

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME)).thenReturn(trip);
        when(userRepository.findByUsernameAndActive(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(tripShareCodeValidator.resolveDefaultRole(request)).thenReturn(TripEnum.EDITOR);
        when(tripShareCodeRepository.findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                TRIP_ID,
                TripEnum.ACTIVE
        )).thenReturn(Optional.of(oldActiveCode));
        when(tripShareCodeRepository.existsByCode(anyString())).thenReturn(false);
        when(tripShareCodeMapper.toNewShareCodeEntity(
                eq(trip),
                anyString(),
                eq(owner),
                eq(TripEnum.EDITOR),
                any(LocalDateTime.class),
                eq(24L)
        )).thenReturn(newShareCode);
        when(tripShareCodeRepository.save(any(TripShareCodeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tripShareCodeMapper.toResponseDTO(newShareCode, INVITE_LINK_PREFIX_VALUE))
                .thenReturn(responseDTO);

        service.regenerateShareCode(TRIP_ID, request);

        assertThat(oldActiveCode.getCodeStatus()).isEqualTo(TripEnum.REVOKED);
        assertThat(oldActiveCode.getModifiedDate()).isNotNull();

        verify(tripShareCodeValidator).validateActiveCodeCanBeRegenerated(
                eq(oldActiveCode),
                any(LocalDateTime.class),
                eq(60L)
        );
        verify(tripShareCodeRepository, times(2)).save(any(TripShareCodeEntity.class));
    }

    @Test
    void regenerateShareCode_shouldMarkOldActiveCodeExpired_whenOldActiveCodeExpired() {
        User owner = user(OWNER_USER_ID, OWNER_USERNAME);
        TripEntity trip = trip(owner);
        GenerateTripShareCodeRequest request = generateRequest(TripEnum.VIEWER);

        TripShareCodeEntity oldActiveCode = activeShareCode(trip, owner);
        oldActiveCode.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        TripShareCodeEntity newShareCode = activeShareCode(trip, owner);
        newShareCode.setCode("WM-NEW12345");

        TripShareCodeResponseDTO responseDTO = shareCodeResponseDTO();

        mockErrorCode(TRIP_SHARE_CODE_CREATED_SUCCESS, TRIP_MEMBER.name());
        mockInviteLinkPrefix();

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME)).thenReturn(trip);
        when(userRepository.findByUsernameAndActive(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(tripShareCodeValidator.resolveDefaultRole(request)).thenReturn(TripEnum.VIEWER);
        when(tripShareCodeRepository.findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                TRIP_ID,
                TripEnum.ACTIVE
        )).thenReturn(Optional.of(oldActiveCode));
        when(tripShareCodeRepository.existsByCode(anyString())).thenReturn(false);
        when(tripShareCodeMapper.toNewShareCodeEntity(
                eq(trip),
                anyString(),
                eq(owner),
                eq(TripEnum.VIEWER),
                any(LocalDateTime.class),
                eq(24L)
        )).thenReturn(newShareCode);
        when(tripShareCodeRepository.save(any(TripShareCodeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tripShareCodeMapper.toResponseDTO(newShareCode, INVITE_LINK_PREFIX_VALUE))
                .thenReturn(responseDTO);

        service.regenerateShareCode(TRIP_ID, request);

        assertThat(oldActiveCode.getCodeStatus()).isEqualTo(TripEnum.EXPIRED);
        assertThat(oldActiveCode.getModifiedDate()).isNotNull();

        verify(tripShareCodeRepository, times(2)).save(any(TripShareCodeEntity.class));
    }

    @Test
    void regenerateShareCode_shouldThrow_whenValidatorBlocksCooldown() {
        User owner = user(OWNER_USER_ID, OWNER_USERNAME);
        TripEntity trip = trip(owner);
        GenerateTripShareCodeRequest request = generateRequest(TripEnum.VIEWER);
        TripShareCodeEntity activeCode = activeShareCode(trip, owner);

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME)).thenReturn(trip);
        when(userRepository.findByUsernameAndActive(OWNER_USERNAME)).thenReturn(Optional.of(owner));
        when(tripShareCodeValidator.resolveDefaultRole(request)).thenReturn(TripEnum.VIEWER);
        when(tripShareCodeRepository.findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                TRIP_ID,
                TripEnum.ACTIVE
        )).thenReturn(Optional.of(activeCode));

        doThrow(new BusinessException(
                TRIP_SHARE_CODE_GENERATE_TOO_SOON,
                TRIP_MEMBER.name()
        )).when(tripShareCodeValidator)
                .validateActiveCodeCanBeRegenerated(
                        eq(activeCode),
                        any(LocalDateTime.class),
                        eq(60L)
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.regenerateShareCode(TRIP_ID, request)
        );

        assertBusinessException(
                exception,
                TRIP_SHARE_CODE_GENERATE_TOO_SOON,
                TRIP_MEMBER.name()
        );

        verify(tripShareCodeRepository, never()).save(any(TripShareCodeEntity.class));
        verify(tripShareCodeMapper, never()).toNewShareCodeEntity(
                any(),
                anyString(),
                any(),
                any(),
                any(),
                anyLong()
        );
    }

    @Test
    void previewShareCode_shouldReturnPreview_whenCodeIsValid() {
        User owner = user(OWNER_USER_ID, OWNER_USERNAME);
        User requester = user(REQUESTER_USER_ID, REQUESTER_USERNAME);
        TripEntity trip = trip(owner);
        TripShareCodeEntity shareCode = activeShareCode(trip, owner);
        TripShareCodePreviewResponseDTO previewDTO = previewResponseDTO();

        mockErrorCode(TRIP_SHARE_CODE_RETRIEVED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(REQUESTER_USERNAME);
        when(userRepository.findByUsernameAndActive(REQUESTER_USERNAME)).thenReturn(Optional.of(requester));
        when(tripShareCodeAttemptRepository.findByUser_UserId(REQUESTER_USER_ID))
                .thenReturn(Optional.empty());
        when(tripShareCodeValidator.normalizeCode(RAW_CODE)).thenReturn(NORMALIZED_CODE);
        when(tripShareCodeRepository.findByCode(NORMALIZED_CODE)).thenReturn(Optional.of(shareCode));
        when(tripShareCodeMapper.toPreviewResponseDTO(shareCode)).thenReturn(previewDTO);

        CompleteResponse<Object> response = service.previewShareCode(RAW_CODE);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_SHARE_CODE_RETRIEVED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(previewDTO);

        verify(tripShareCodeValidator).validateShareCodeCanBeUsed(
                eq(shareCode),
                any(LocalDateTime.class)
        );
    }

    @Test
    void previewShareCode_shouldRegisterInvalidAttempt_whenCodeNotFound() {
        User requester = user(REQUESTER_USER_ID, REQUESTER_USERNAME);

        when(authenticatedUserProvider.getUsername()).thenReturn(REQUESTER_USERNAME);
        when(userRepository.findByUsernameAndActive(REQUESTER_USERNAME)).thenReturn(Optional.of(requester));
        when(tripShareCodeAttemptRepository.findByUser_UserId(REQUESTER_USER_ID))
                .thenReturn(Optional.empty());
        when(tripShareCodeValidator.normalizeCode(RAW_CODE)).thenReturn(NORMALIZED_CODE);
        when(tripShareCodeRepository.findByCode(NORMALIZED_CODE)).thenReturn(Optional.empty());
        when(tripShareCodeAttemptRepository.save(any(TripShareCodeAttemptEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.previewShareCode(RAW_CODE)
        );

        assertBusinessException(exception, TRIP_SHARE_CODE_NOT_FOUND, TRIP_MEMBER.name());

        ArgumentCaptor<TripShareCodeAttemptEntity> attemptCaptor =
                ArgumentCaptor.forClass(TripShareCodeAttemptEntity.class);

        verify(tripShareCodeAttemptRepository).save(attemptCaptor.capture());

        TripShareCodeAttemptEntity attempt = attemptCaptor.getValue();
        assertThat(attempt.getUser()).isEqualTo(requester);
        assertThat(attempt.getRetryCount()).isEqualTo(1);
        assertThat(attempt.getLastAttemptDate()).isNotNull();
        assertThat(attempt.getModifiedDate()).isNotNull();
    }

    @Test
    void previewShareCode_shouldMarkActiveCodeExpiredAndRegisterAttempt_whenValidatorThrowsExpired() {
        User owner = user(OWNER_USER_ID, OWNER_USERNAME);
        User requester = user(REQUESTER_USER_ID, REQUESTER_USERNAME);
        TripEntity trip = trip(owner);
        TripShareCodeEntity shareCode = activeShareCode(trip, owner);

        when(authenticatedUserProvider.getUsername()).thenReturn(REQUESTER_USERNAME);
        when(userRepository.findByUsernameAndActive(REQUESTER_USERNAME)).thenReturn(Optional.of(requester));
        when(tripShareCodeAttemptRepository.findByUser_UserId(REQUESTER_USER_ID))
                .thenReturn(Optional.empty());
        when(tripShareCodeValidator.normalizeCode(RAW_CODE)).thenReturn(NORMALIZED_CODE);
        when(tripShareCodeRepository.findByCode(NORMALIZED_CODE)).thenReturn(Optional.of(shareCode));
        when(tripShareCodeAttemptRepository.save(any(TripShareCodeAttemptEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new BusinessException(
                TRIP_SHARE_CODE_EXPIRED,
                TRIP_MEMBER.name()
        )).when(tripShareCodeValidator)
                .validateShareCodeCanBeUsed(
                        eq(shareCode),
                        any(LocalDateTime.class)
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.previewShareCode(RAW_CODE)
        );

        assertBusinessException(exception, TRIP_SHARE_CODE_EXPIRED, TRIP_MEMBER.name());

        assertThat(shareCode.getCodeStatus()).isEqualTo(TripEnum.EXPIRED);
        verify(tripShareCodeRepository).save(shareCode);
        verify(tripShareCodeAttemptRepository).save(any(TripShareCodeAttemptEntity.class));
    }

    @Test
    void previewShareCode_shouldThrowRestricted_whenUserAttemptIsRestricted() {
        User requester = user(REQUESTER_USER_ID, REQUESTER_USERNAME);
        TripShareCodeAttemptEntity attempt = new TripShareCodeAttemptEntity(
                requester,
                0,
                LocalDateTime.now()
        );

        when(authenticatedUserProvider.getUsername()).thenReturn(REQUESTER_USERNAME);
        when(userRepository.findByUsernameAndActive(REQUESTER_USERNAME)).thenReturn(Optional.of(requester));
        when(tripShareCodeAttemptRepository.findByUser_UserId(REQUESTER_USER_ID))
                .thenReturn(Optional.of(attempt));

        doThrow(new BusinessException(
                TRIP_SHARE_CODE_ATTEMPT_RESTRICTED,
                TRIP_MEMBER.name()
        )).when(tripShareCodeValidator)
                .validateAttemptIsNotRestricted(
                        eq(attempt),
                        any(LocalDateTime.class)
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.previewShareCode(RAW_CODE)
        );

        assertBusinessException(
                exception,
                TRIP_SHARE_CODE_ATTEMPT_RESTRICTED,
                TRIP_MEMBER.name()
        );

        verify(tripShareCodeRepository, never()).findByCode(anyString());
    }

    @Test
    void requestToJoinByShareCode_shouldCreateJoinRequestAndMarkCodeUsed_whenCodeIsValid() {
        User owner = user(OWNER_USER_ID, OWNER_USERNAME);
        User requester = user(REQUESTER_USER_ID, REQUESTER_USERNAME);
        TripEntity trip = trip(owner);
        TripShareCodeEntity shareCode = activeShareCode(trip, owner);

        TripCollaborationRequestEntity joinRequest = joinRequest(trip, requester, owner);
        TripCollaborationRequestEntity savedJoinRequest = joinRequest(trip, requester, owner);
        savedJoinRequest.setRequestId(REQUEST_ID);

        TripCollaborationRequestResponseDTO responseDTO =
                new TripCollaborationRequestResponseDTO();
        responseDTO.setRequestId(REQUEST_ID);

        mockErrorCode(TRIP_SHARE_CODE_JOIN_REQUEST_SENT_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(REQUESTER_USERNAME);
        when(userRepository.findByUsernameAndActive(REQUESTER_USERNAME)).thenReturn(Optional.of(requester));
        when(tripShareCodeAttemptRepository.findByUser_UserId(REQUESTER_USER_ID))
                .thenReturn(Optional.empty());
        when(tripShareCodeValidator.normalizeCode(RAW_CODE)).thenReturn(NORMALIZED_CODE);
        when(tripShareCodeRepository.findByCode(NORMALIZED_CODE)).thenReturn(Optional.of(shareCode));
        when(tripShareCodeMapper.toJoinRequestEntity(
                eq(shareCode),
                eq(requester),
                any(LocalDateTime.class)
        )).thenReturn(joinRequest);
        when(tripCollaborationRequestRepository.save(joinRequest)).thenReturn(savedJoinRequest);
        when(tripCollaborationRequestMapper.toResponseDTO(savedJoinRequest)).thenReturn(responseDTO);
        when(tripShareCodeRepository.save(shareCode)).thenReturn(shareCode);

        CompleteResponse<Object> response =
                service.requestToJoinByShareCode(RAW_CODE);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_SHARE_CODE_JOIN_REQUEST_SENT_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(responseDTO);

        verify(tripShareCodeValidator).validateRequesterCanRequestToJoinByShareCode(
                shareCode,
                requester
        );

        verify(tripCollaborationRequestRepository).save(joinRequest);

        assertThat(shareCode.getCodeStatus()).isEqualTo(TripEnum.USED);
        assertThat(shareCode.getUsedByUser()).isEqualTo(requester);
        assertThat(shareCode.getUsedDate()).isNotNull();
        assertThat(shareCode.getModifiedDate()).isNotNull();

        verify(tripShareCodeRepository).save(shareCode);
    }

    @Test
    void requestToJoinByShareCode_shouldNotMarkCodeUsed_whenJoinValidationFails() {
        User owner = user(OWNER_USER_ID, OWNER_USERNAME);
        User requester = user(REQUESTER_USER_ID, REQUESTER_USERNAME);
        TripEntity trip = trip(owner);
        TripShareCodeEntity shareCode = activeShareCode(trip, owner);

        when(authenticatedUserProvider.getUsername()).thenReturn(REQUESTER_USERNAME);
        when(userRepository.findByUsernameAndActive(REQUESTER_USERNAME)).thenReturn(Optional.of(requester));
        when(tripShareCodeAttemptRepository.findByUser_UserId(REQUESTER_USER_ID))
                .thenReturn(Optional.empty());
        when(tripShareCodeValidator.normalizeCode(RAW_CODE)).thenReturn(NORMALIZED_CODE);
        when(tripShareCodeRepository.findByCode(NORMALIZED_CODE)).thenReturn(Optional.of(shareCode));

        doThrow(new BusinessException(
                TRIP_COLLABORATION_REQUEST_ALREADY_EXISTS,
                TRIP_MEMBER.name()
        )).when(tripShareCodeValidator)
                .validateRequesterCanRequestToJoinByShareCode(
                        shareCode,
                        requester
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.requestToJoinByShareCode(RAW_CODE)
        );

        assertBusinessException(
                exception,
                TRIP_COLLABORATION_REQUEST_ALREADY_EXISTS,
                TRIP_MEMBER.name()
        );

        assertThat(shareCode.getCodeStatus()).isEqualTo(TripEnum.ACTIVE);
        assertThat(shareCode.getUsedByUser()).isNull();
        assertThat(shareCode.getUsedDate()).isNull();

        verify(tripCollaborationRequestRepository, never()).save(any());
        verify(tripShareCodeRepository, never()).save(shareCode);
    }

    @Test
    void getActiveShareCode_shouldReturnNull_whenNoActiveCodeExists() {
        mockErrorCode(TRIP_SHARE_CODE_RETRIEVED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripShareCodeRepository.findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                TRIP_ID,
                TripEnum.ACTIVE
        )).thenReturn(Optional.empty());

        CompleteResponse<Object> response = service.getActiveShareCode(TRIP_ID);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_SHARE_CODE_RETRIEVED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isNull();

        verify(tripShareCodeValidator).validateTripId(TRIP_ID);
        verify(tripAccessService).getTripIfOwner(TRIP_ID, OWNER_USERNAME);
    }

    @Test
    void getActiveShareCode_shouldExpireCodeAndReturnNull_whenActiveCodeIsExpired() {
        User owner = user(OWNER_USER_ID, OWNER_USERNAME);
        TripEntity trip = trip(owner);
        TripShareCodeEntity activeShareCode = activeShareCode(trip, owner);
        activeShareCode.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        mockErrorCode(TRIP_SHARE_CODE_RETRIEVED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripShareCodeRepository.findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                TRIP_ID,
                TripEnum.ACTIVE
        )).thenReturn(Optional.of(activeShareCode));
        when(tripShareCodeRepository.save(activeShareCode)).thenReturn(activeShareCode);

        CompleteResponse<Object> response = service.getActiveShareCode(TRIP_ID);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_SHARE_CODE_RETRIEVED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isNull();

        assertThat(activeShareCode.getCodeStatus()).isEqualTo(TripEnum.EXPIRED);
        assertThat(activeShareCode.getModifiedDate()).isNotNull();

        verify(tripShareCodeValidator).validateTripId(TRIP_ID);
        verify(tripAccessService).getTripIfOwner(TRIP_ID, OWNER_USERNAME);
        verify(tripShareCodeRepository).save(activeShareCode);
    }

    @Test
    void getActiveShareCode_shouldReturnActiveCode_whenCodeIsNotExpired() {
        User owner = user(OWNER_USER_ID, OWNER_USERNAME);
        TripEntity trip = trip(owner);
        TripShareCodeEntity activeShareCode = activeShareCode(trip, owner);
        TripShareCodeResponseDTO responseDTO = shareCodeResponseDTO();

        /*
         * Current service returns TRIP_SHARE_CODE_CREATED_SUCCESS here.
         * If you change the service to the cleaner TRIP_SHARE_CODE_RETRIEVED_SUCCESS,
         * change this mock and assertion to TRIP_SHARE_CODE_RETRIEVED_SUCCESS.
         */
        mockErrorCode(TRIP_SHARE_CODE_CREATED_SUCCESS, TRIP_MEMBER.name());
        mockInviteLinkPrefix();

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripShareCodeRepository.findFirstByTrip_TripIdAndCodeStatusOrderByCreatedDateDesc(
                TRIP_ID,
                TripEnum.ACTIVE
        )).thenReturn(Optional.of(activeShareCode));
        when(tripShareCodeMapper.toResponseDTO(activeShareCode, INVITE_LINK_PREFIX_VALUE))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = service.getActiveShareCode(TRIP_ID);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_SHARE_CODE_CREATED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(responseDTO);

        verify(tripShareCodeValidator).validateTripId(TRIP_ID);
        verify(tripAccessService).getTripIfOwner(TRIP_ID, OWNER_USERNAME);
        verify(tripShareCodeMapper).toResponseDTO(activeShareCode, INVITE_LINK_PREFIX_VALUE);
    }

    private GenerateTripShareCodeRequest generateRequest(TripEnum role) {
        GenerateTripShareCodeRequest request = new GenerateTripShareCodeRequest();
        request.setDefaultRole(role);
        return request;
    }

    private User user(long userId, String username) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setActive(true);
        return user;
    }

    private TripEntity trip(User owner) {
        TripEntity trip = new TripEntity();
        trip.setTripId(TRIP_ID);
        trip.setTripName("Adelaide Trip");
        trip.setDestination("Adelaide");
        trip.setStartDate(LocalDateTime.now().plusDays(10));
        trip.setEndDate(LocalDateTime.now().plusDays(15));
        trip.setCreatedDate(LocalDateTime.now());
        trip.setUser(owner);
        return trip;
    }

    private TripShareCodeEntity activeShareCode(
            TripEntity trip,
            User owner
    ) {
        TripShareCodeEntity shareCode = new TripShareCodeEntity();
        shareCode.setShareCodeId(SHARE_CODE_ID);
        shareCode.setTrip(trip);
        shareCode.setCode(NORMALIZED_CODE);
        shareCode.setCreatedByUser(owner);
        shareCode.setDefaultRole(TripEnum.VIEWER);
        shareCode.setCodeStatus(TripEnum.ACTIVE);
        shareCode.setCreatedDate(LocalDateTime.now().minusMinutes(2));
        shareCode.setExpiresAt(LocalDateTime.now().plusHours(1));
        return shareCode;
    }

    private TripCollaborationRequestEntity joinRequest(
            TripEntity trip,
            User requester,
            User owner
    ) {
        TripCollaborationRequestEntity request = new TripCollaborationRequestEntity();
        request.setTrip(trip);
        request.setRequester(requester);
        request.setTargetUser(owner);
        request.setRequestedRole(TripEnum.VIEWER);
        request.setRequestType(TripEnum.JOIN_REQUEST);
        request.setStatus(TripEnum.PENDING);
        request.setCreatedDate(LocalDateTime.now());
        return request;
    }

    private TripShareCodeResponseDTO shareCodeResponseDTO() {
        TripShareCodeResponseDTO dto = new TripShareCodeResponseDTO();
        dto.setTripId(TRIP_ID);
        dto.setTripName("Adelaide Trip");
        dto.setCode(NORMALIZED_CODE);
        dto.setInviteLink(INVITE_LINK_PREFIX_VALUE + NORMALIZED_CODE);
        dto.setDefaultRole(TripEnum.VIEWER);
        dto.setCodeStatus(TripEnum.ACTIVE);
        dto.setExpiresAt(LocalDateTime.now().plusHours(1));
        dto.setCreatedDate(LocalDateTime.now());
        return dto;
    }

    private TripShareCodePreviewResponseDTO previewResponseDTO() {
        TripShareCodePreviewResponseDTO dto = new TripShareCodePreviewResponseDTO();
        dto.setTripId(TRIP_ID);
        dto.setTripName("Adelaide Trip");
        dto.setDestination("Adelaide");
        dto.setOwnerUsername(OWNER_USERNAME);
        dto.setDefaultRole(TripEnum.VIEWER);
        dto.setExpiresAt(LocalDateTime.now().plusHours(1));
        return dto;
    }

    private void mockInviteLinkPrefix() {
        ConfigurationEntity configuration = new ConfigurationEntity();
        configuration.setConfigCode(INVITE_LINK_PREFIX.name());
        configuration.setConfigValue(INVITE_LINK_PREFIX_VALUE);

        when(configurationRepository.findByConfigCode(INVITE_LINK_PREFIX.name()))
                .thenReturn(Optional.of(configuration));
    }

    private void mockErrorCode(
            ErrorCodeEnum errorCodeEnum,
            String flow
    ) {
        ErrorCodeEntity entity = new ErrorCodeEntity();
        entity.setErrorCode(errorCodeEnum.getCode());
        entity.setErrorMessage(errorCodeEnum.getMessage());
        entity.setErrorEnum(errorCodeEnum.name());
        entity.setFlow(flow);
        entity.setCreatedDate(LocalDateTime.now());

        when(errorCodeRepository.findByErrorEnumAndFlow(
                errorCodeEnum.name(),
                flow
        )).thenReturn(Optional.of(entity));
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