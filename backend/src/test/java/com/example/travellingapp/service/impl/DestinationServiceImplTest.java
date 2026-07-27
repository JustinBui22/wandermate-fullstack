package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.CreateDestinationDTO;
import com.example.travellingapp.dto.request.update.UpdateDestinationDTO;
import com.example.travellingapp.dto.response.DestinationResponseDTO;
import com.example.travellingapp.entity.DestinationEntity;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.DestinationMapper;
import com.example.travellingapp.repository.ActivityRepository;
import com.example.travellingapp.repository.DestinationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.validator.DestinationValidator;
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
import static com.example.travellingapp.enums.CommonEnum.DESTINATION;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DestinationServiceImplTest {

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private DestinationValidator destinationValidator;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private DestinationMapper destinationMapper;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private UserRepository userRepository;

    private DestinationServiceImpl destinationService;

    private static final Long TRIP_ID = 1L;
    private static final Long DESTINATION_ID = 10L;
    private static final String USERNAME = "JustinBo123";

    @BeforeEach
    void setUp() {
        destinationService = new DestinationServiceImpl(
                destinationRepository,
                errorCodeRepository,
                destinationValidator,
                authenticatedUserProvider,
                destinationMapper,
                activityRepository,
                tripAccessService,
                userRepository
        );
    }

    // -------------------------------------------------------------------------
    // createDestination()
    // -------------------------------------------------------------------------

    @Test
    void createDestination_shouldCreateDestination_whenInputIsValidAndNoOverlapExists() {
        CreateDestinationDTO request = validCreateRequest();
        TripEntity trip = trip();
        User currentUser = user();
        DestinationResponseDTO responseDTO = mock(DestinationResponseDTO.class);

        mockErrorCode(DESTINATION_CREATED_SUCCESS, DESTINATION.name());

        when(destinationValidator.validateCreateInput(TRIP_ID, request))
                .thenReturn("Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(currentUser));
        when(destinationRepository.existsByTrip_TripIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                TRIP_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(false);
        when(destinationMapper.toResponseDTO(any(DestinationEntity.class)))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = destinationService.createDestination(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(DESTINATION_CREATED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);

        ArgumentCaptor<DestinationEntity> destinationCaptor =
                ArgumentCaptor.forClass(DestinationEntity.class);

        verify(destinationRepository).save(destinationCaptor.capture());

        DestinationEntity savedDestination = destinationCaptor.getValue();

        assertThat(savedDestination.getDestinationName()).isEqualTo("Adelaide");
        assertThat(savedDestination.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(savedDestination.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(savedDestination.getDestinationOrder()).isEqualTo(request.getDestinationOrder());
        assertThat(savedDestination.getNotes()).isEqualTo(request.getNotes());
        assertThat(savedDestination.getTrip()).isEqualTo(trip);
        assertThat(savedDestination.getCreatedBy()).isEqualTo(currentUser);
        assertThat(savedDestination.getCreatedDate()).isNotNull();

        verify(destinationValidator).validateDestinationInsideTrip(
                request.getStartDate(),
                request.getEndDate(),
                trip
        );
    }

    @Test
    void createDestination_shouldRethrowBusinessException_whenValidatorRejectsInput() {
        CreateDestinationDTO request = validCreateRequest();

        when(destinationValidator.validateCreateInput(TRIP_ID, request))
                .thenThrow(new BusinessException(DESTINATION_TIME_INVALID, DESTINATION.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.createDestination(TRIP_ID, request)
        );

        assertBusinessException(exception, DESTINATION_TIME_INVALID, DESTINATION.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripAccessService, never()).getTripIfCanEdit(anyLong(), anyString());
        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void createDestination_shouldThrowAccessDenied_whenUserCannotEditTrip() {
        CreateDestinationDTO request = validCreateRequest();

        when(destinationValidator.validateCreateInput(TRIP_ID, request))
                .thenReturn("Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.createDestination(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(destinationValidator, never()).validateDestinationInsideTrip(any(), any(), any());
        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void createDestination_shouldRethrowBusinessException_whenDestinationIsOutsideTripRange() {
        CreateDestinationDTO request = validCreateRequest();
        TripEntity trip = trip();

        when(destinationValidator.validateCreateInput(TRIP_ID, request))
                .thenReturn("Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip);
        lenient().when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(user()));

        doThrow(new BusinessException(DESTINATION_DATE_OUTSIDE_TRIP_RANGE, DESTINATION.name()))
                .when(destinationValidator)
                .validateDestinationInsideTrip(
                        request.getStartDate(),
                        request.getEndDate(),
                        trip
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.createDestination(TRIP_ID, request)
        );

        assertBusinessException(exception, DESTINATION_DATE_OUTSIDE_TRIP_RANGE, DESTINATION.name());

        verify(destinationRepository, never())
                .existsByTrip_TripIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(anyLong(), any(), any());
        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void createDestination_shouldThrowOverlapWarning_whenOverlapExistsAndAllowOverlapIsFalse() {
        CreateDestinationDTO request = validCreateRequest();
        request.setAllowOverlap(false);

        TripEntity trip = trip();

        when(destinationValidator.validateCreateInput(TRIP_ID, request))
                .thenReturn("Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip);
        lenient().when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(user()));
        when(destinationRepository.existsByTrip_TripIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                TRIP_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.createDestination(TRIP_ID, request)
        );

        assertBusinessException(exception, DESTINATION_OVERLAP_WARNING, DESTINATION.name());

        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void createDestination_shouldCreateDestination_whenOverlapExistsButAllowOverlapIsTrue() {
        CreateDestinationDTO request = validCreateRequest();
        request.setAllowOverlap(true);

        TripEntity trip = trip();
        DestinationResponseDTO responseDTO = mock(DestinationResponseDTO.class);

        mockErrorCode(DESTINATION_CREATED_SUCCESS, DESTINATION.name());

        when(destinationValidator.validateCreateInput(TRIP_ID, request))
                .thenReturn("Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip);
        lenient().when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(user()));
        when(destinationRepository.existsByTrip_TripIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                TRIP_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(true);
        when(destinationMapper.toResponseDTO(any(DestinationEntity.class)))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = destinationService.createDestination(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(DESTINATION_CREATED_SUCCESS.getCode());

        verify(destinationRepository).save(any(DestinationEntity.class));
    }

    @Test
    void createDestination_shouldWrapUnexpectedExceptionAsInternalServerError() {
        CreateDestinationDTO request = validCreateRequest();

        when(destinationValidator.validateCreateInput(TRIP_ID, request))
                .thenThrow(new RuntimeException("Unexpected validator failure"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.createDestination(TRIP_ID, request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    // -------------------------------------------------------------------------
    // getDestinationsByTrip()
    // -------------------------------------------------------------------------

    @Test
    void getDestinationsByTrip_shouldReturnDestinationList_whenUserCanViewTrip() {
        DestinationEntity destination1 = destination("Adelaide", 1);
        DestinationEntity destination2 = destination("Melbourne", 2);

        DestinationResponseDTO response1 = mock(DestinationResponseDTO.class);
        DestinationResponseDTO response2 = mock(DestinationResponseDTO.class);

        mockErrorCode(DESTINATION_RETRIEVED_SUCCESS, DESTINATION.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(destinationRepository.findByTrip_TripIdOrderByDestinationOrderAsc(TRIP_ID))
                .thenReturn(List.of(destination1, destination2));
        when(destinationMapper.toResponseDTO(destination1))
                .thenReturn(response1);
        when(destinationMapper.toResponseDTO(destination2))
                .thenReturn(response2);

        CompleteResponse<Object> response = destinationService.getDestinationsByTrip(TRIP_ID);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(DESTINATION_RETRIEVED_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        List<DestinationResponseDTO> body =
                (List<DestinationResponseDTO>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(response1, response2);

        verify(tripAccessService).assertCanView(TRIP_ID, USERNAME);
    }

    @Test
    void getDestinationsByTrip_shouldThrowInvalidInput_whenTripIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.getDestinationsByTrip(null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripAccessService, never()).assertCanView(anyLong(), anyString());
        verify(destinationRepository, never()).findByTrip_TripIdOrderByDestinationOrderAsc(anyLong());
    }

    @Test
    void getDestinationsByTrip_shouldThrowAccessDenied_whenUserCannotViewTrip() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);

        doThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()))
                .when(tripAccessService)
                .assertCanView(TRIP_ID, USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.getDestinationsByTrip(TRIP_ID)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(destinationRepository, never()).findByTrip_TripIdOrderByDestinationOrderAsc(anyLong());
    }

    @Test
    void getDestinationsByTrip_shouldWrapUnexpectedExceptionAsInternalServerError() {
        when(authenticatedUserProvider.getUsername())
                .thenThrow(new RuntimeException("Security context unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.getDestinationsByTrip(TRIP_ID)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());
    }

    // -------------------------------------------------------------------------
    // getDestinationById()
    // -------------------------------------------------------------------------

    @Test
    void getDestinationById_shouldReturnDestination_whenUserCanViewTripAndDestinationExists() {
        DestinationEntity destination = destination("Adelaide", 1);
        DestinationResponseDTO responseDTO = mock(DestinationResponseDTO.class);

        mockErrorCode(DESTINATION_RETRIEVED_SUCCESS, DESTINATION.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(destinationRepository.findByDestinationIdAndTrip_TripId(
                DESTINATION_ID,
                TRIP_ID
        )).thenReturn(Optional.of(destination));
        when(destinationMapper.toResponseDTO(destination))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = destinationService.getDestinationById(
                TRIP_ID,
                DESTINATION_ID
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(DESTINATION_RETRIEVED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);

        verify(tripAccessService).assertCanView(TRIP_ID, USERNAME);
    }

    @Test
    void getDestinationById_shouldThrowInvalidInput_whenTripIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.getDestinationById(null, DESTINATION_ID)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripAccessService, never()).assertCanView(anyLong(), anyString());
    }

    @Test
    void getDestinationById_shouldThrowInvalidInput_whenDestinationIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.getDestinationById(TRIP_ID, null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripAccessService, never()).assertCanView(anyLong(), anyString());
    }

    @Test
    void getDestinationById_shouldThrowAccessDenied_whenUserCannotViewTrip() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);

        doThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()))
                .when(tripAccessService)
                .assertCanView(TRIP_ID, USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.getDestinationById(TRIP_ID, DESTINATION_ID)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(destinationRepository, never()).findByDestinationIdAndTrip_TripId(anyLong(), anyLong());
    }

    @Test
    void getDestinationById_shouldThrowDestinationNotFound_whenDestinationDoesNotExist() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(destinationRepository.findByDestinationIdAndTrip_TripId(
                DESTINATION_ID,
                TRIP_ID
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.getDestinationById(TRIP_ID, DESTINATION_ID)
        );

        assertBusinessException(exception, DESTINATION_NOT_FOUND, DESTINATION.name());

        verify(tripAccessService).assertCanView(TRIP_ID, USERNAME);
    }

    @Test
    void getDestinationById_shouldWrapUnexpectedExceptionAsInternalServerError() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(destinationRepository.findByDestinationIdAndTrip_TripId(
                DESTINATION_ID,
                TRIP_ID
        )).thenThrow(new RuntimeException("Database unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.getDestinationById(TRIP_ID, DESTINATION_ID)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());
    }

    // -------------------------------------------------------------------------
    // updateDestination()
    // -------------------------------------------------------------------------

    @Test
    void updateDestination_shouldUpdateDestination_whenUserCanEditAndNoOverlapOrActivityConflictExists() {
        UpdateDestinationDTO request = validUpdateRequest();

        TripEntity trip = trip();
        User currentUser = user();
        DestinationEntity existingDestination = destination("Old Adelaide", 1);
        DestinationResponseDTO responseDTO = mock(DestinationResponseDTO.class);

        mockErrorCode(DESTINATION_UPDATED_SUCCESS, DESTINATION.name());

        when(destinationValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Updated Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip);
        lenient().when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(currentUser));
        when(destinationRepository.findByDestinationIdAndTrip_TripId(DESTINATION_ID, TRIP_ID))
                .thenReturn(Optional.of(existingDestination));
        when(activityRepository.existsByDestination_DestinationIdAndStartDateTimeBefore(
                DESTINATION_ID,
                request.getStartDate()
        )).thenReturn(false);
        when(activityRepository.existsByDestination_DestinationIdAndEndDateTimeAfter(
                DESTINATION_ID,
                request.getEndDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndDestinationIdNotAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                TRIP_ID,
                DESTINATION_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(false);
        when(destinationMapper.toResponseDTO(existingDestination))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = destinationService.updateDestination(
                TRIP_ID,
                DESTINATION_ID,
                request
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(DESTINATION_UPDATED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);

        assertThat(existingDestination.getDestinationName()).isEqualTo("Updated Adelaide");
        assertThat(existingDestination.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(existingDestination.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(existingDestination.getDestinationOrder()).isEqualTo(request.getDestinationOrder());
        assertThat(existingDestination.getNotes()).isEqualTo(request.getNotes());
        assertThat(existingDestination.getModifiedBy()).isEqualTo(currentUser);
        assertThat(existingDestination.getModifiedDate()).isNotNull();

        verify(destinationValidator).validateDestinationInsideTrip(
                request.getStartDate(),
                request.getEndDate(),
                trip
        );
        verify(destinationRepository).save(existingDestination);
    }

    @Test
    void updateDestination_shouldRethrowBusinessException_whenValidatorRejectsInput() {
        UpdateDestinationDTO request = validUpdateRequest();

        when(destinationValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, request))
                .thenThrow(new BusinessException(DESTINATION_TIME_INVALID, DESTINATION.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.updateDestination(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(exception, DESTINATION_TIME_INVALID, DESTINATION.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripAccessService, never()).getTripIfCanEdit(anyLong(), anyString());
        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void updateDestination_shouldThrowAccessDenied_whenUserCannotEditTrip() {
        UpdateDestinationDTO request = validUpdateRequest();

        when(destinationValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Updated Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.updateDestination(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(destinationRepository, never()).findByDestinationIdAndTrip_TripId(anyLong(), anyLong());
        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void updateDestination_shouldThrowDestinationNotFound_whenDestinationDoesNotExistInTrip() {
        UpdateDestinationDTO request = validUpdateRequest();

        when(destinationValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Updated Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip());
        when(destinationRepository.findByDestinationIdAndTrip_TripId(DESTINATION_ID, TRIP_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.updateDestination(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(exception, DESTINATION_NOT_FOUND, DESTINATION.name());

        verify(destinationValidator, never()).validateDestinationInsideTrip(any(), any(), any());
        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void updateDestination_shouldRethrowBusinessException_whenUpdatedDestinationIsOutsideTripRange() {
        UpdateDestinationDTO request = validUpdateRequest();
        TripEntity trip = trip();

        when(destinationValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Updated Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip);
        when(destinationRepository.findByDestinationIdAndTrip_TripId(DESTINATION_ID, TRIP_ID))
                .thenReturn(Optional.of(destination("Adelaide", 1)));

        doThrow(new BusinessException(DESTINATION_DATE_OUTSIDE_TRIP_RANGE, DESTINATION.name()))
                .when(destinationValidator)
                .validateDestinationInsideTrip(
                        request.getStartDate(),
                        request.getEndDate(),
                        trip
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.updateDestination(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(exception, DESTINATION_DATE_OUTSIDE_TRIP_RANGE, DESTINATION.name());

        verify(activityRepository, never())
                .existsByDestination_DestinationIdAndStartDateTimeBefore(anyLong(), any());
        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void updateDestination_shouldThrowDestinationDateConflict_whenExistingActivityStartsBeforeNewStartDate() {
        UpdateDestinationDTO request = validUpdateRequest();

        when(destinationValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Updated Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip());
        when(destinationRepository.findByDestinationIdAndTrip_TripId(DESTINATION_ID, TRIP_ID))
                .thenReturn(Optional.of(destination("Adelaide", 1)));
        when(activityRepository.existsByDestination_DestinationIdAndStartDateTimeBefore(
                DESTINATION_ID,
                request.getStartDate()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.updateDestination(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(
                exception,
                DESTINATION_DATE_CONFLICT_WITH_EXISTING_ACTIVITY,
                DESTINATION.name()
        );

        verify(activityRepository, never())
                .existsByDestination_DestinationIdAndEndDateTimeAfter(anyLong(), any());
        verify(destinationRepository, never())
                .existsByTrip_TripIdAndDestinationIdNotAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        anyLong(),
                        anyLong(),
                        any(),
                        any()
                );
        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void updateDestination_shouldThrowDestinationDateConflict_whenExistingActivityEndsAfterNewEndDate() {
        UpdateDestinationDTO request = validUpdateRequest();

        when(destinationValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Updated Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip());
        when(destinationRepository.findByDestinationIdAndTrip_TripId(DESTINATION_ID, TRIP_ID))
                .thenReturn(Optional.of(destination("Adelaide", 1)));
        when(activityRepository.existsByDestination_DestinationIdAndStartDateTimeBefore(
                DESTINATION_ID,
                request.getStartDate()
        )).thenReturn(false);
        when(activityRepository.existsByDestination_DestinationIdAndEndDateTimeAfter(
                DESTINATION_ID,
                request.getEndDate()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.updateDestination(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(
                exception,
                DESTINATION_DATE_CONFLICT_WITH_EXISTING_ACTIVITY,
                DESTINATION.name()
        );

        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void updateDestination_shouldThrowOverlapWarning_whenOverlapExistsAndAllowOverlapIsFalse() {
        UpdateDestinationDTO request = validUpdateRequest();
        request.setAllowOverlap(false);

        when(destinationValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Updated Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip());
        when(destinationRepository.findByDestinationIdAndTrip_TripId(DESTINATION_ID, TRIP_ID))
                .thenReturn(Optional.of(destination("Adelaide", 1)));
        when(activityRepository.existsByDestination_DestinationIdAndStartDateTimeBefore(
                DESTINATION_ID,
                request.getStartDate()
        )).thenReturn(false);
        when(activityRepository.existsByDestination_DestinationIdAndEndDateTimeAfter(
                DESTINATION_ID,
                request.getEndDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndDestinationIdNotAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                TRIP_ID,
                DESTINATION_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.updateDestination(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(exception, DESTINATION_OVERLAP_WARNING, DESTINATION.name());

        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    @Test
    void updateDestination_shouldUpdateDestination_whenOverlapExistsButAllowOverlapIsTrue() {
        UpdateDestinationDTO request = validUpdateRequest();
        request.setAllowOverlap(true);

        User currentUser = user();
        DestinationEntity existingDestination = destination("Adelaide", 1);
        DestinationResponseDTO responseDTO = mock(DestinationResponseDTO.class);

        mockErrorCode(DESTINATION_UPDATED_SUCCESS, DESTINATION.name());

        when(destinationValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Updated Adelaide");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip());
        lenient().when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(currentUser));
        when(destinationRepository.findByDestinationIdAndTrip_TripId(DESTINATION_ID, TRIP_ID))
                .thenReturn(Optional.of(existingDestination));
        when(activityRepository.existsByDestination_DestinationIdAndStartDateTimeBefore(
                DESTINATION_ID,
                request.getStartDate()
        )).thenReturn(false);
        when(activityRepository.existsByDestination_DestinationIdAndEndDateTimeAfter(
                DESTINATION_ID,
                request.getEndDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndDestinationIdNotAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                TRIP_ID,
                DESTINATION_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(true);
        when(destinationMapper.toResponseDTO(existingDestination))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = destinationService.updateDestination(
                TRIP_ID,
                DESTINATION_ID,
                request
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(DESTINATION_UPDATED_SUCCESS.getCode());
        assertThat(existingDestination.getModifiedBy()).isEqualTo(currentUser);

        verify(destinationRepository).save(existingDestination);
    }

    @Test
    void updateDestination_shouldWrapUnexpectedExceptionAsInternalServerError() {
        UpdateDestinationDTO request = validUpdateRequest();

        when(destinationValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, request))
                .thenThrow(new RuntimeException("Unexpected validator failure"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.updateDestination(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(destinationRepository, never()).save(any(DestinationEntity.class));
    }

    // -------------------------------------------------------------------------
    // deleteDestination()
    // -------------------------------------------------------------------------

    @Test
    void deleteDestination_shouldDeleteDestination_whenUserCanEditAndDestinationExists() {
        DestinationEntity destination = destination("Adelaide", 1);

        mockErrorCode(DESTINATION_DELETED_SUCCESS, DESTINATION.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(destinationRepository.findByDestinationIdAndTrip_TripId(
                DESTINATION_ID,
                TRIP_ID
        )).thenReturn(Optional.of(destination));

        CompleteResponse<Object> response = destinationService.deleteDestination(
                TRIP_ID,
                DESTINATION_ID
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(DESTINATION_DELETED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isNull();

        verify(tripAccessService).assertCanEdit(TRIP_ID, USERNAME);
        verify(destinationRepository).delete(destination);
    }

    @Test
    void deleteDestination_shouldThrowInvalidInput_whenTripIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.deleteDestination(null, DESTINATION_ID)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripAccessService, never()).assertCanEdit(anyLong(), anyString());
        verify(destinationRepository, never()).delete(any(DestinationEntity.class));
    }

    @Test
    void deleteDestination_shouldThrowInvalidInput_whenDestinationIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.deleteDestination(TRIP_ID, null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripAccessService, never()).assertCanEdit(anyLong(), anyString());
        verify(destinationRepository, never()).delete(any(DestinationEntity.class));
    }

    @Test
    void deleteDestination_shouldThrowAccessDenied_whenUserCannotEditTrip() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);

        doThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()))
                .when(tripAccessService)
                .assertCanEdit(TRIP_ID, USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.deleteDestination(TRIP_ID, DESTINATION_ID)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(destinationRepository, never()).findByDestinationIdAndTrip_TripId(anyLong(), anyLong());
        verify(destinationRepository, never()).delete(any(DestinationEntity.class));
    }

    @Test
    void deleteDestination_shouldThrowDestinationNotFound_whenDestinationDoesNotExist() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(destinationRepository.findByDestinationIdAndTrip_TripId(
                DESTINATION_ID,
                TRIP_ID
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.deleteDestination(TRIP_ID, DESTINATION_ID)
        );

        assertBusinessException(exception, DESTINATION_NOT_FOUND, DESTINATION.name());

        verify(tripAccessService).assertCanEdit(TRIP_ID, USERNAME);
        verify(destinationRepository, never()).delete(any(DestinationEntity.class));
    }

    @Test
    void deleteDestination_shouldWrapUnexpectedExceptionAsInternalServerError() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(destinationRepository.findByDestinationIdAndTrip_TripId(
                DESTINATION_ID,
                TRIP_ID
        )).thenThrow(new RuntimeException("Database unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> destinationService.deleteDestination(TRIP_ID, DESTINATION_ID)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(destinationRepository, never()).delete(any(DestinationEntity.class));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateDestinationDTO validCreateRequest() {
        CreateDestinationDTO request = new CreateDestinationDTO();
        request.setDestinationName("Adelaide");
        request.setStartDate(LocalDate.of(2026, 7, 10));
        request.setEndDate(LocalDate.of(2026, 7, 12));
        request.setDestinationOrder(1);
        request.setNotes("Stay in Adelaide CBD");
        request.setAllowOverlap(false);
        return request;
    }

    private UpdateDestinationDTO validUpdateRequest() {
        UpdateDestinationDTO request = new UpdateDestinationDTO();
        request.setDestinationName("Updated Adelaide");
        request.setStartDate(LocalDate.of(2026, 7, 10));
        request.setEndDate(LocalDate.of(2026, 7, 13));
        request.setDestinationOrder(2);
        request.setNotes("Updated notes");
        request.setAllowOverlap(false);
        return request;
    }

    private User user() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername(USERNAME);
        user.setEmail("justin@example.com");
        user.setActive(true);
        user.setDisplayName("Justin");
        user.setProfileImageUrl("https://example.com/avatar.png");
        return user;
    }

    private TripEntity trip() {
        TripEntity trip = new TripEntity();
        trip.setTripId(TRIP_ID);
        trip.setTripName("South Australia Trip");
        trip.setDestination("Adelaide");
        trip.setStartDate(LocalDate.of(2026, 7, 1));
        trip.setEndDate(LocalDate.of(2026, 7, 20));
        trip.setCreatedDate(Instant.now());
        return trip;
    }

    private DestinationEntity destination(String name, int order) {
        DestinationEntity destination = new DestinationEntity();
        destination.setDestinationId(DESTINATION_ID);
        destination.setDestinationName(name);
        destination.setStartDate(LocalDate.of(2026, 7, 10));
        destination.setEndDate(LocalDate.of(2026, 7, 12));
        destination.setDestinationOrder(order);
        destination.setNotes("Notes");
        destination.setCreatedDate(Instant.now());
        destination.setTrip(trip());
        return destination;
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