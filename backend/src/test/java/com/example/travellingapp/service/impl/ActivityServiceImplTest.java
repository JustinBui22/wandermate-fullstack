package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.CreateActivityDTO;
import com.example.travellingapp.dto.request.update.UpdateActivityDTO;
import com.example.travellingapp.dto.response.ActivityResponseDTO;
import com.example.travellingapp.entity.ActivityEntity;
import com.example.travellingapp.entity.DestinationEntity;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.ActivityMapper;
import com.example.travellingapp.repository.ActivityRepository;
import com.example.travellingapp.repository.DestinationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.validator.ActivityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.ACTIVITY;
import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.DESTINATION;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceImplTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private ActivityValidator activityValidator;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private UserRepository userRepository;

    private ActivityServiceImpl activityService;

    private static final Long TRIP_ID = 1L;
    private static final Long DESTINATION_ID = 10L;
    private static final Long ACTIVITY_ID = 100L;
    private static final String USERNAME = "JustinBo123";

    @BeforeEach
    void setUp() {
        activityService = new ActivityServiceImpl(
                activityRepository,
                destinationRepository,
                errorCodeRepository,
                activityValidator,
                authenticatedUserProvider,
                activityMapper,
                userRepository
        );
    }

    // -------------------------------------------------------------------------
    // createActivity()
    // -------------------------------------------------------------------------

    @Test
    void createActivity_shouldCreateActivity_whenInputIsValidAndNoOverlapExists() {
        CreateActivityDTO request = validCreateRequest();
        DestinationEntity destination = destination();
        User user = activeUser();

        ActivityResponseDTO responseDTO = mock(ActivityResponseDTO.class);

        mockErrorCode(ACTIVITY_CREATED_SUCCESS, ACTIVITY.name());

        when(activityValidator.validateCreateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Visit Museum");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(user));
        when(destinationRepository.findByDestinationIdAndTrip_TripIdAndTrip_User_Username(
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.of(destination));
        when(activityRepository.existsByDestination_Trip_TripIdAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                TRIP_ID,
                request.getEndDateTime(),
                request.getStartDateTime()
        )).thenReturn(false);
        when(activityMapper.toResponseDTO(any(ActivityEntity.class)))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = activityService.createActivity(
                TRIP_ID,
                DESTINATION_ID,
                request
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(ACTIVITY_CREATED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);

        ArgumentCaptor<ActivityEntity> activityCaptor =
                ArgumentCaptor.forClass(ActivityEntity.class);

        verify(activityRepository).save(activityCaptor.capture());

        ActivityEntity savedActivity = activityCaptor.getValue();

        assertThat(savedActivity.getActivityName()).isEqualTo("Visit Museum");
        assertThat(savedActivity.getLocation()).isEqualTo("Adelaide Museum");
        assertThat(savedActivity.getDescription()).isEqualTo("Explore the museum");
        assertThat(savedActivity.getStartDateTime()).isEqualTo(request.getStartDateTime());
        assertThat(savedActivity.getEndDateTime()).isEqualTo(request.getEndDateTime());
        assertThat(savedActivity.getDestination()).isEqualTo(destination);
        assertThat(savedActivity.getCreatedBy()).isEqualTo(user);
        assertThat(savedActivity.getCreatedBy().getUsername()).isEqualTo(USERNAME);
        assertThat(savedActivity.getCreatedDate()).isNotNull();

        verify(activityValidator).validateActivityInsideDestination(
                request.getStartDateTime(),
                request.getEndDateTime(),
                destination
        );
    }

    @Test
    void createActivity_shouldRethrowBusinessException_whenValidatorRejectsInput() {
        CreateActivityDTO request = validCreateRequest();

        when(activityValidator.validateCreateInput(TRIP_ID, DESTINATION_ID, request))
                .thenThrow(new BusinessException(ACTIVITY_TIME_INVALID, ACTIVITY.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.createActivity(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(exception, ACTIVITY_TIME_INVALID, ACTIVITY.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(userRepository, never()).findByUsernameAndActive(anyString());
        verify(destinationRepository, never())
                .findByDestinationIdAndTrip_TripIdAndTrip_User_Username(anyLong(), anyLong(), anyString());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void createActivity_shouldThrowDestinationNotFound_whenDestinationDoesNotBelongToUserTrip() {
        CreateActivityDTO request = validCreateRequest();

        when(activityValidator.validateCreateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Visit Museum");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(activeUser()));
        when(destinationRepository.findByDestinationIdAndTrip_TripIdAndTrip_User_Username(
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.createActivity(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(exception, DESTINATION_NOT_FOUND, DESTINATION.name());

        verify(activityValidator, never()).validateActivityInsideDestination(any(), any(), any());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void createActivity_shouldRethrowBusinessException_whenActivityIsOutsideDestinationRange() {
        CreateActivityDTO request = validCreateRequest();
        DestinationEntity destination = destination();

        when(activityValidator.validateCreateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Visit Museum");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(activeUser()));
        when(destinationRepository.findByDestinationIdAndTrip_TripIdAndTrip_User_Username(
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.of(destination));
        doThrow(new BusinessException(ACTIVITY_OUTSIDE_DESTINATION_RANGE, ACTIVITY.name()))
                .when(activityValidator)
                .validateActivityInsideDestination(
                        request.getStartDateTime(),
                        request.getEndDateTime(),
                        destination
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.createActivity(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(exception, ACTIVITY_OUTSIDE_DESTINATION_RANGE, ACTIVITY.name());

        verify(activityRepository, never())
                .existsByDestination_Trip_TripIdAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        anyLong(),
                        any(),
                        any()
                );
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void createActivity_shouldThrowActivityTimeConflict_whenOverlappingActivityExists() {
        CreateActivityDTO request = validCreateRequest();
        DestinationEntity destination = destination();

        when(activityValidator.validateCreateInput(TRIP_ID, DESTINATION_ID, request))
                .thenReturn("Visit Museum");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(activeUser()));
        when(destinationRepository.findByDestinationIdAndTrip_TripIdAndTrip_User_Username(
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.of(destination));
        when(activityRepository.existsByDestination_Trip_TripIdAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                TRIP_ID,
                request.getEndDateTime(),
                request.getStartDateTime()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.createActivity(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(
                exception,
                ACTIVITY_TIME_CONFLICT_WITH_EXISTING_ACTIVITY,
                ACTIVITY.name()
        );

        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void createActivity_shouldWrapUnexpectedExceptionAsInternalServerError() {
        CreateActivityDTO request = validCreateRequest();

        when(activityValidator.validateCreateInput(TRIP_ID, DESTINATION_ID, request))
                .thenThrow(new RuntimeException("Unexpected validation failure"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.createActivity(TRIP_ID, DESTINATION_ID, request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    // -------------------------------------------------------------------------
    // getActivitiesByDestination()
    // -------------------------------------------------------------------------

    @Test
    void getActivitiesByDestination_shouldReturnActivities_whenDestinationExists() {
        DestinationEntity destination = destination();

        ActivityEntity activity1 = activity("Museum");
        ActivityEntity activity2 = activity("Dinner");

        ActivityResponseDTO response1 = mock(ActivityResponseDTO.class);
        ActivityResponseDTO response2 = mock(ActivityResponseDTO.class);

        mockErrorCode(ACTIVITY_RETRIEVED_SUCCESS, ACTIVITY.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(destinationRepository.findByDestinationIdAndTrip_TripIdAndTrip_User_Username(
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.of(destination));
        when(activityRepository.findAllByDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(List.of(activity1, activity2));
        when(activityMapper.toResponseDTO(activity1)).thenReturn(response1);
        when(activityMapper.toResponseDTO(activity2)).thenReturn(response2);

        CompleteResponse<Object> response = activityService.getActivitiesByDestination(
                TRIP_ID,
                DESTINATION_ID
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(ACTIVITY_RETRIEVED_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        List<ActivityResponseDTO> body =
                (List<ActivityResponseDTO>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(response1, response2);
    }

    @Test
    void getActivitiesByDestination_shouldThrowInvalidInput_whenTripIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.getActivitiesByDestination(null, DESTINATION_ID)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(activityRepository, never())
                .findAllByDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                        anyLong(),
                        anyLong(),
                        anyString()
                );
    }

    @Test
    void getActivitiesByDestination_shouldThrowInvalidInput_whenDestinationIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.getActivitiesByDestination(TRIP_ID, null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
    }

    @Test
    void getActivitiesByDestination_shouldThrowDestinationNotFound_whenDestinationDoesNotExist() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(destinationRepository.findByDestinationIdAndTrip_TripIdAndTrip_User_Username(
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.getActivitiesByDestination(TRIP_ID, DESTINATION_ID)
        );

        assertBusinessException(exception, DESTINATION_NOT_FOUND, DESTINATION.name());

        verify(activityRepository, never())
                .findAllByDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                        anyLong(),
                        anyLong(),
                        anyString()
                );
    }

    @Test
    void getActivitiesByDestination_shouldWrapUnexpectedExceptionAsInternalServerError() {
        when(authenticatedUserProvider.getUsername())
                .thenThrow(new RuntimeException("Security context unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.getActivitiesByDestination(TRIP_ID, DESTINATION_ID)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());
    }

    // -------------------------------------------------------------------------
    // getActivityById()
    // -------------------------------------------------------------------------

    @Test
    void getActivityById_shouldReturnActivity_whenActivityExistsAndBelongsToUser() {
        ActivityEntity activity = activity("Museum");
        ActivityResponseDTO responseDTO = mock(ActivityResponseDTO.class);

        mockErrorCode(ACTIVITY_RETRIEVED_SUCCESS, ACTIVITY.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(activityRepository.findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                ACTIVITY_ID,
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.of(activity));
        when(activityMapper.toResponseDTO(activity))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = activityService.getActivityById(
                TRIP_ID,
                DESTINATION_ID,
                ACTIVITY_ID
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(ACTIVITY_RETRIEVED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);
    }

    @Test
    void getActivityById_shouldThrowInvalidInput_whenAnyIdIsNull() {
        BusinessException exception1 = assertThrows(
                BusinessException.class,
                () -> activityService.getActivityById(null, DESTINATION_ID, ACTIVITY_ID)
        );

        BusinessException exception2 = assertThrows(
                BusinessException.class,
                () -> activityService.getActivityById(TRIP_ID, null, ACTIVITY_ID)
        );

        BusinessException exception3 = assertThrows(
                BusinessException.class,
                () -> activityService.getActivityById(TRIP_ID, DESTINATION_ID, null)
        );

        assertBusinessException(exception1, INVALID_INPUT, COMMON.name());
        assertBusinessException(exception2, INVALID_INPUT, COMMON.name());
        assertBusinessException(exception3, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
    }

    @Test
    void getActivityById_shouldThrowActivityNotFound_whenActivityDoesNotExist() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(activityRepository.findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                ACTIVITY_ID,
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.getActivityById(TRIP_ID, DESTINATION_ID, ACTIVITY_ID)
        );

        assertBusinessException(exception, ACTIVITY_NOT_FOUND, ACTIVITY.name());
    }

    @Test
    void getActivityById_shouldWrapUnexpectedExceptionAsInternalServerError() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(activityRepository.findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                ACTIVITY_ID,
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenThrow(new RuntimeException("Database unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.getActivityById(TRIP_ID, DESTINATION_ID, ACTIVITY_ID)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());
    }

    // -------------------------------------------------------------------------
    // updateActivity()
    // -------------------------------------------------------------------------

    @Test
    void updateActivity_shouldUpdateActivity_whenInputIsValidAndNoOverlapExists() {
        UpdateActivityDTO request = validUpdateRequest();

        DestinationEntity destination = destination();
        ActivityEntity existingActivity = activity("Old Name");
        existingActivity.setDestination(destination);

        ActivityResponseDTO responseDTO = mock(ActivityResponseDTO.class);

        mockErrorCode(ACTIVITY_UPDATED_SUCCESS, ACTIVITY.name());

        when(activityValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request))
                .thenReturn("Updated Museum");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(activityRepository.findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                ACTIVITY_ID,
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.of(existingActivity));
        when(activityRepository.existsByDestination_Trip_TripIdAndActivityIdNotAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                TRIP_ID,
                ACTIVITY_ID,
                request.getEndDateTime(),
                request.getStartDateTime()
        )).thenReturn(false);
        when(activityMapper.toResponseDTO(existingActivity))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = activityService.updateActivity(
                TRIP_ID,
                DESTINATION_ID,
                ACTIVITY_ID,
                request
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(ACTIVITY_UPDATED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);

        assertThat(existingActivity.getActivityName()).isEqualTo("Updated Museum");
        assertThat(existingActivity.getLocation()).isEqualTo("Adelaide Zoo");
        assertThat(existingActivity.getDescription()).isEqualTo("Updated description");
        assertThat(existingActivity.getStartDateTime()).isEqualTo(request.getStartDateTime());
        assertThat(existingActivity.getEndDateTime()).isEqualTo(request.getEndDateTime());
        assertThat(existingActivity.getModifiedDate()).isNotNull();

        verify(activityValidator).validateActivityInsideDestination(
                request.getStartDateTime(),
                request.getEndDateTime(),
                destination
        );
        verify(activityRepository).save(existingActivity);
    }

    @Test
    void updateActivity_shouldRethrowBusinessException_whenValidatorRejectsInput() {
        UpdateActivityDTO request = validUpdateRequest();

        when(activityValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request))
                .thenThrow(new BusinessException(ACTIVITY_TIME_INVALID, ACTIVITY.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.updateActivity(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request)
        );

        assertBusinessException(exception, ACTIVITY_TIME_INVALID, ACTIVITY.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void updateActivity_shouldThrowActivityNotFound_whenActivityDoesNotExist() {
        UpdateActivityDTO request = validUpdateRequest();

        when(activityValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request))
                .thenReturn("Updated Museum");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(activityRepository.findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                ACTIVITY_ID,
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.updateActivity(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request)
        );

        assertBusinessException(exception, ACTIVITY_NOT_FOUND, ACTIVITY.name());

        verify(activityValidator, never()).validateActivityInsideDestination(any(), any(), any());
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void updateActivity_shouldRethrowBusinessException_whenUpdatedActivityIsOutsideDestinationRange() {
        UpdateActivityDTO request = validUpdateRequest();

        DestinationEntity destination = destination();
        ActivityEntity existingActivity = activity("Old Name");
        existingActivity.setDestination(destination);

        when(activityValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request))
                .thenReturn("Updated Museum");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(activityRepository.findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                ACTIVITY_ID,
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.of(existingActivity));
        doThrow(new BusinessException(ACTIVITY_OUTSIDE_DESTINATION_RANGE, ACTIVITY.name()))
                .when(activityValidator)
                .validateActivityInsideDestination(
                        request.getStartDateTime(),
                        request.getEndDateTime(),
                        destination
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.updateActivity(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request)
        );

        assertBusinessException(exception, ACTIVITY_OUTSIDE_DESTINATION_RANGE, ACTIVITY.name());

        verify(activityRepository, never())
                .existsByDestination_Trip_TripIdAndActivityIdNotAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        anyLong(),
                        anyLong(),
                        any(),
                        any()
                );
        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void updateActivity_shouldThrowActivityTimeConflict_whenUpdatedTimeOverlapsAnotherActivity() {
        UpdateActivityDTO request = validUpdateRequest();

        DestinationEntity destination = destination();
        ActivityEntity existingActivity = activity("Old Name");
        existingActivity.setDestination(destination);

        when(activityValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request))
                .thenReturn("Updated Museum");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(activityRepository.findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                ACTIVITY_ID,
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.of(existingActivity));
        when(activityRepository.existsByDestination_Trip_TripIdAndActivityIdNotAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                TRIP_ID,
                ACTIVITY_ID,
                request.getEndDateTime(),
                request.getStartDateTime()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.updateActivity(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request)
        );

        assertBusinessException(
                exception,
                ACTIVITY_TIME_CONFLICT_WITH_EXISTING_ACTIVITY,
                ACTIVITY.name()
        );

        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    @Test
    void updateActivity_shouldWrapUnexpectedExceptionAsInternalServerError() {
        UpdateActivityDTO request = validUpdateRequest();

        when(activityValidator.validateUpdateInput(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request))
                .thenThrow(new RuntimeException("Unexpected validation failure"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.updateActivity(TRIP_ID, DESTINATION_ID, ACTIVITY_ID, request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(activityRepository, never()).save(any(ActivityEntity.class));
    }

    // -------------------------------------------------------------------------
    // deleteActivity()
    // -------------------------------------------------------------------------

    @Test
    void deleteActivity_shouldDeleteActivity_whenActivityExistsAndBelongsToUser() {
        ActivityEntity activity = activity("Museum");

        mockErrorCode(ACTIVITY_DELETED_SUCCESS, ACTIVITY.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(activityRepository.findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                ACTIVITY_ID,
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.of(activity));

        CompleteResponse<Object> response = activityService.deleteActivity(
                TRIP_ID,
                DESTINATION_ID,
                ACTIVITY_ID
        );

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(ACTIVITY_DELETED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isNull();

        verify(activityRepository).delete(activity);
    }

    @Test
    void deleteActivity_shouldThrowInvalidInput_whenAnyIdIsNull() {
        BusinessException exception1 = assertThrows(
                BusinessException.class,
                () -> activityService.deleteActivity(null, DESTINATION_ID, ACTIVITY_ID)
        );

        BusinessException exception2 = assertThrows(
                BusinessException.class,
                () -> activityService.deleteActivity(TRIP_ID, null, ACTIVITY_ID)
        );

        BusinessException exception3 = assertThrows(
                BusinessException.class,
                () -> activityService.deleteActivity(TRIP_ID, DESTINATION_ID, null)
        );

        assertBusinessException(exception1, INVALID_INPUT, COMMON.name());
        assertBusinessException(exception2, INVALID_INPUT, COMMON.name());
        assertBusinessException(exception3, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(activityRepository, never()).delete(any(ActivityEntity.class));
    }

    @Test
    void deleteActivity_shouldThrowActivityNotFound_whenActivityDoesNotExist() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(activityRepository.findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                ACTIVITY_ID,
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.deleteActivity(TRIP_ID, DESTINATION_ID, ACTIVITY_ID)
        );

        assertBusinessException(exception, ACTIVITY_NOT_FOUND, ACTIVITY.name());

        verify(activityRepository, never()).delete(any(ActivityEntity.class));
    }

    @Test
    void deleteActivity_shouldWrapUnexpectedExceptionAsInternalServerError() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(activityRepository.findByActivityIdAndDestination_DestinationIdAndDestination_Trip_TripIdAndDestination_Trip_User_Username(
                ACTIVITY_ID,
                DESTINATION_ID,
                TRIP_ID,
                USERNAME
        )).thenThrow(new RuntimeException("Database unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> activityService.deleteActivity(TRIP_ID, DESTINATION_ID, ACTIVITY_ID)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(activityRepository, never()).delete(any(ActivityEntity.class));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateActivityDTO validCreateRequest() {
        CreateActivityDTO request = new CreateActivityDTO();
        request.setActivityName("Visit Museum");
        request.setLocation("Adelaide Museum");
        request.setDescription("Explore the museum");
        request.setStartDateTime(LocalDateTime.of(2026, 7, 10, 10, 0));
        request.setEndDateTime(LocalDateTime.of(2026, 7, 10, 12, 0));
        return request;
    }

    private UpdateActivityDTO validUpdateRequest() {
        UpdateActivityDTO request = new UpdateActivityDTO();
        request.setActivityName("Updated Museum");
        request.setLocation("Adelaide Zoo");
        request.setDescription("Updated description");
        request.setStartDateTime(LocalDateTime.of(2026, 7, 10, 13, 0));
        request.setEndDateTime(LocalDateTime.of(2026, 7, 10, 15, 0));
        return request;
    }

    private DestinationEntity destination() {
        DestinationEntity destination = new DestinationEntity();
        destination.setDestinationId(DESTINATION_ID);
        destination.setDestinationName("Adelaide");
        destination.setStartDate(LocalDateTime.of(2026, 7, 10, 0, 0));
        destination.setEndDate(LocalDateTime.of(2026, 7, 12, 23, 59));
        return destination;
    }

    private ActivityEntity activity(String activityName) {
        ActivityEntity activity = new ActivityEntity();
        activity.setActivityId(ACTIVITY_ID);
        activity.setActivityName(activityName);
        activity.setLocation("Adelaide");
        activity.setDescription("Description");
        activity.setStartDateTime(LocalDateTime.of(2026, 7, 10, 9, 0));
        activity.setEndDateTime(LocalDateTime.of(2026, 7, 10, 10, 0));
        activity.setDestination(destination());
        activity.setCreatedBy(activeUser());
        activity.setCreatedDate(LocalDateTime.now());
        return activity;
    }

    private User activeUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername(USERNAME);
        user.setEmail("justin@example.com");
        user.setActive(true);
        return user;
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