package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.create.CreateTripDTO;
import com.example.travellingapp.dto.request.update.UpdateTripDTO;
import com.example.travellingapp.dto.response.TripResponseDTO;
import com.example.travellingapp.entity.AccommodationEntity;
import com.example.travellingapp.entity.CityEntity;
import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.RestaurantEntity;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripMapper;
import com.example.travellingapp.repository.AccommodationRepository;
import com.example.travellingapp.repository.CityRepository;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.repository.DestinationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.RestaurantRepository;
import com.example.travellingapp.repository.TripRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.CloudinaryImageClient;
import com.example.travellingapp.service.TripAccessService;
import com.example.travellingapp.validator.TripValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private CloudinaryImageClient cloudinaryImageClient;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private TripMapper tripMapper;

    @Mock
    private TripValidator tripValidator;

    @Mock
    private DestinationRepository destinationRepository;

    private TripServiceImpl tripService;

    private static final Long TRIP_ID = 1L;
    private static final String USERNAME = "JustinBo123";

    @BeforeEach
    void setUp() {
        tripService = new TripServiceImpl(
                errorCodeRepository,
                cityRepository,
                restaurantRepository,
                accommodationRepository,
                configurationRepository,
                userRepository,
                tripRepository,
                authenticatedUserProvider,
                tripMapper,
                tripValidator,
                destinationRepository,
                tripMemberRepository,
                tripAccessService,
                cloudinaryImageClient
        );
    }

    // -------------------------------------------------------------------------
    // createTrip()
    // -------------------------------------------------------------------------

    @Test
    void createTrip_shouldCreateTripAndCreateOwnerMember_whenInputIsValidAndNoOverlapExists() {
        CreateTripDTO request = validCreateRequest();
        User user = activeUser();
        TripResponseDTO responseDTO = mock(TripResponseDTO.class);

        mockErrorCode(TRIP_CREATED_SUCCESS, TRIP.name());

        when(tripValidator.validateCreateInput(request))
                .thenReturn("Adelaide Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(user));
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCase(USERNAME, "Adelaide Trip"))
                .thenReturn(false);
        when(tripRepository.existsByUser_UsernameAndStartDateLessThanAndEndDateGreaterThan(
                USERNAME,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(false);
        when(tripRepository.save(any(TripEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tripMapper.toResponseDTO(any(TripEntity.class)))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = tripService.createTrip(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_CREATED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);

        ArgumentCaptor<TripEntity> tripCaptor = ArgumentCaptor.forClass(TripEntity.class);
        verify(tripRepository).save(tripCaptor.capture());

        TripEntity savedTrip = tripCaptor.getValue();

        assertThat(savedTrip.getTripName()).isEqualTo("Adelaide Trip");
        assertThat(savedTrip.getDestination()).isEqualTo("adelaide");
        assertThat(savedTrip.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(savedTrip.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(savedTrip.getUser()).isEqualTo(user);
        assertThat(savedTrip.getCreatedDate()).isNotNull();
        assertThat(savedTrip.getCoverImageUrl()).isEqualTo(request.getCoverImageUrl());
        assertThat(savedTrip.getCoverImagePublicId()).isEqualTo(request.getCoverImagePublicId());

        ArgumentCaptor<TripMemberEntity> memberCaptor =
                ArgumentCaptor.forClass(TripMemberEntity.class);

        verify(tripMemberRepository).save(memberCaptor.capture());

        TripMemberEntity savedMember = memberCaptor.getValue();

        assertThat(savedMember.getTrip()).isEqualTo(savedTrip);
        assertThat(savedMember.getUser()).isEqualTo(user);
        assertThat(savedMember.getRole()).isEqualTo(TripEnum.OWNER);
        assertThat(savedMember.getCreatedDate()).isNotNull();
    }

    @Test
    void createTrip_shouldRethrowBusinessException_whenValidatorRejectsInput() {
        CreateTripDTO request = validCreateRequest();

        when(tripValidator.validateCreateInput(request))
                .thenThrow(new BusinessException(TRIP_TIME_INVALID, TRIP.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.createTrip(request)
        );

        assertBusinessException(exception, TRIP_TIME_INVALID, TRIP.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripRepository, never()).save(any(TripEntity.class));
        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    @Test
    void createTrip_shouldThrowUserNotFound_whenAuthenticatedUserDoesNotExist() {
        CreateTripDTO request = validCreateRequest();

        when(tripValidator.validateCreateInput(request))
                .thenReturn("Adelaide Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.createTrip(request)
        );

        assertBusinessException(exception, USER_NOT_FOUND, COMMON.name());

        verify(tripRepository, never()).save(any(TripEntity.class));
        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    @Test
    void createTrip_shouldThrowTripNameAlreadyExists_whenUserAlreadyHasSameTripName() {
        CreateTripDTO request = validCreateRequest();

        when(tripValidator.validateCreateInput(request))
                .thenReturn("Adelaide Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(activeUser()));
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCase(USERNAME, "Adelaide Trip"))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.createTrip(request)
        );

        assertBusinessException(exception, TRIP_NAME_ALREADY_EXISTS, COMMON.name());

        verify(tripRepository, never())
                .existsByUser_UsernameAndStartDateLessThanAndEndDateGreaterThan(
                        anyString(),
                        any(),
                        any()
                );
        verify(tripRepository, never()).save(any(TripEntity.class));
        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    @Test
    void createTrip_shouldThrowTripOverlapWarning_whenOverlapExistsAndAllowOverlapFalse() {
        CreateTripDTO request = validCreateRequest();
        request.setAllowOverlap(false);

        when(tripValidator.validateCreateInput(request))
                .thenReturn("Adelaide Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(activeUser()));
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCase(USERNAME, "Adelaide Trip"))
                .thenReturn(false);
        when(tripRepository.existsByUser_UsernameAndStartDateLessThanAndEndDateGreaterThan(
                USERNAME,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.createTrip(request)
        );

        assertBusinessException(exception, TRIP_OVERLAP_WARNING, TRIP.name());

        verify(tripRepository, never()).save(any(TripEntity.class));
        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    @Test
    void createTrip_shouldCreateTrip_whenOverlapExistsButAllowOverlapTrue() {
        CreateTripDTO request = validCreateRequest();
        request.setAllowOverlap(true);

        mockErrorCode(TRIP_CREATED_SUCCESS, TRIP.name());

        when(tripValidator.validateCreateInput(request))
                .thenReturn("Adelaide Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(activeUser()));
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCase(USERNAME, "Adelaide Trip"))
                .thenReturn(false);
        when(tripRepository.existsByUser_UsernameAndStartDateLessThanAndEndDateGreaterThan(
                USERNAME,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(true);
        when(tripRepository.save(any(TripEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tripMapper.toResponseDTO(any(TripEntity.class)))
                .thenReturn(mock(TripResponseDTO.class));

        CompleteResponse<Object> response = tripService.createTrip(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_CREATED_SUCCESS.getCode());

        verify(tripRepository).save(any(TripEntity.class));
        verify(tripMemberRepository).save(any(TripMemberEntity.class));
    }

    @Test
    void createTrip_shouldWrapUnexpectedExceptionAsInternalServerError() {
        CreateTripDTO request = validCreateRequest();

        when(tripValidator.validateCreateInput(request))
                .thenThrow(new RuntimeException("Unexpected validator failure"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.createTrip(request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(tripRepository, never()).save(any(TripEntity.class));
        verify(tripMemberRepository, never()).save(any(TripMemberEntity.class));
    }

    // -------------------------------------------------------------------------
    // getTrips()
    // -------------------------------------------------------------------------

    @Test
    void getTrips_shouldReturnAccessibleTripsForAuthenticatedUser() {
        User user = activeUser();
        TripEntity trip1 = trip("Adelaide Trip");
        TripEntity trip2 = trip("Melbourne Trip");

        TripResponseDTO response1 = mock(TripResponseDTO.class);
        TripResponseDTO response2 = mock(TripResponseDTO.class);

        mockErrorCode(TRIPS_RETRIEVED_SUCCESS, TRIP.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(user));
        when(tripMemberRepository.findAccessibleTripsByUsername(USERNAME))
                .thenReturn(List.of(trip1, trip2));
        when(tripMapper.toResponseDTO(trip1))
                .thenReturn(response1);
        when(tripMapper.toResponseDTO(trip2))
                .thenReturn(response2);

        CompleteResponse<Object> response = tripService.getTrips(
        TripEnum.ALL,
        "ALL",
        TripEnum.MODIFIED_DATE_DESC
);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIPS_RETRIEVED_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        List<TripResponseDTO> body =
                (List<TripResponseDTO>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(response1, response2);
    }

    @Test
    void getTrips_shouldThrowUserNotFound_whenAuthenticatedUserDoesNotExist() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.getTrips(
        TripEnum.ALL,
        "ALL",
        TripEnum.MODIFIED_DATE_DESC
)
        );

        assertBusinessException(exception, USER_NOT_FOUND, COMMON.name());

        verify(tripMemberRepository, never()).findAccessibleTripsByUsername(anyString());
    }

    @Test
    void getTrips_shouldWrapUnexpectedExceptionAsInternalServerError() {
        when(authenticatedUserProvider.getUsername())
                .thenThrow(new RuntimeException("Security context unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.getTrips(
        TripEnum.ALL,
        "ALL",
        TripEnum.MODIFIED_DATE_DESC
)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());
    }

    // -------------------------------------------------------------------------
    // getTripById()
    // -------------------------------------------------------------------------

    @Test
    void getTripById_shouldReturnTrip_whenUserCanViewTrip() {
        TripEntity trip = trip("Adelaide Trip");
        TripResponseDTO responseDTO = mock(TripResponseDTO.class);

        mockErrorCode(TRIPS_RETRIEVED_SUCCESS, TRIP.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanView(TRIP_ID, USERNAME))
                .thenReturn(trip);
        when(tripMapper.toResponseDTO(trip))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = tripService.getTripById(TRIP_ID);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIPS_RETRIEVED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);
    }

    @Test
    void getTripById_shouldThrowInvalidInput_whenTripIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.getTripById(null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripAccessService, never()).getTripIfCanView(anyLong(), anyString());
    }

    @Test
    void getTripById_shouldThrowAccessDenied_whenUserCannotViewTrip() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanView(TRIP_ID, USERNAME))
                .thenThrow(new BusinessException(TRIP_ACCESS_DENIED, COMMON.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.getTripById(TRIP_ID)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, COMMON.name());
    }

    @Test
    void getTripById_shouldWrapUnexpectedExceptionAsInternalServerError() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanView(TRIP_ID, USERNAME))
                .thenThrow(new RuntimeException("Database unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.getTripById(TRIP_ID)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());
    }

    // -------------------------------------------------------------------------
    // updateTrip()
    // -------------------------------------------------------------------------

    @Test
    void updateTrip_shouldUpdateTrip_whenUserCanEditTripAndNoConflictsExist() throws IOException {
        UpdateTripDTO request = validUpdateRequest();
        TripEntity existingTrip = trip("Old Trip");
        TripResponseDTO responseDTO = mock(TripResponseDTO.class);

        mockErrorCode(TRIP_UPDATED_SUCCESS, TRIP.name());

        when(tripValidator.validateUpdateInput(TRIP_ID, request))
                .thenReturn("Updated Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(existingTrip);
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCaseAndTripIdNot(
                USERNAME,
                "Updated Trip",
                TRIP_ID
        )).thenReturn(false);
        when(tripRepository.existsByUser_UsernameAndTripIdNotAndStartDateLessThanAndEndDateGreaterThan(
                USERNAME,
                TRIP_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndStartDateBefore(
                TRIP_ID,
                request.getStartDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndEndDateAfter(
                TRIP_ID,
                request.getEndDate()
        )).thenReturn(false);
        when(tripMapper.toResponseDTO(existingTrip))
                .thenReturn(responseDTO);

        CompleteResponse<Object> response = tripService.updateTrip(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_UPDATED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody())
                .isEqualTo(responseDTO);

        assertThat(existingTrip.getTripName()).isEqualTo("Updated Trip");
        assertThat(existingTrip.getDestination()).isEqualTo("melbourne");
        assertThat(existingTrip.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(existingTrip.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(existingTrip.getModifiedDate()).isNotNull();
        assertThat(existingTrip.getCoverImageUrl()).isEqualTo(request.getCoverImageUrl());
        assertThat(existingTrip.getCoverImagePublicId()).isEqualTo(request.getCoverImagePublicId());

        verify(tripRepository).save(existingTrip);
        verify(cloudinaryImageClient).deleteImage("wandermate/trip-covers/users/1/trip-cover-1-old");
    }


    @Test
    void updateTrip_shouldDeleteOldCloudinaryCover_whenCoverImageIsRemoved() throws IOException {
        UpdateTripDTO request = validUpdateRequest();
        request.setCoverImageUrl("");
        request.setCoverImagePublicId("");

        TripEntity existingTrip = trip("Old Trip");
        existingTrip.setCoverImageUrl("https://res.cloudinary.com/demo/image/upload/old-cover.png");
        existingTrip.setCoverImagePublicId("wandermate/trip-covers/users/1/trip-cover-1-old");
        TripResponseDTO responseDTO = mock(TripResponseDTO.class);

        mockSuccessfulUpdateDependencies(request, existingTrip, responseDTO);

        CompleteResponse<Object> response = tripService.updateTrip(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_UPDATED_SUCCESS.getCode());
        assertThat(existingTrip.getCoverImageUrl()).isNull();
        assertThat(existingTrip.getCoverImagePublicId()).isNull();
        verify(cloudinaryImageClient).deleteImage("wandermate/trip-covers/users/1/trip-cover-1-old");
    }

    @Test
    void updateTrip_shouldNotDeleteCloudinaryCover_whenPublicIdIsUnchanged() throws IOException {
        UpdateTripDTO request = validUpdateRequest();
        request.setCoverImageUrl("https://res.cloudinary.com/demo/image/upload/same-cover.png");
        request.setCoverImagePublicId("wandermate/trip-covers/users/1/trip-cover-1-same");

        TripEntity existingTrip = trip("Old Trip");
        existingTrip.setCoverImageUrl("https://res.cloudinary.com/demo/image/upload/same-cover.png");
        existingTrip.setCoverImagePublicId("wandermate/trip-covers/users/1/trip-cover-1-same");
        TripResponseDTO responseDTO = mock(TripResponseDTO.class);

        mockSuccessfulUpdateDependencies(request, existingTrip, responseDTO);

        CompleteResponse<Object> response = tripService.updateTrip(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_UPDATED_SUCCESS.getCode());
        verify(cloudinaryImageClient, never()).deleteImage(anyString());
    }

    @Test
    void updateTrip_shouldStillReturnSuccess_whenOldCloudinaryCoverDeleteFails() throws IOException {
        UpdateTripDTO request = validUpdateRequest();
        TripEntity existingTrip = trip("Old Trip");
        TripResponseDTO responseDTO = mock(TripResponseDTO.class);

        mockSuccessfulUpdateDependencies(request, existingTrip, responseDTO);
        doThrow(new IOException("Cloudinary delete failed"))
                .when(cloudinaryImageClient)
                .deleteImage("wandermate/trip-covers/users/1/trip-cover-1-old");

        CompleteResponse<Object> response = tripService.updateTrip(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_UPDATED_SUCCESS.getCode());
        verify(cloudinaryImageClient).deleteImage("wandermate/trip-covers/users/1/trip-cover-1-old");
    }

    @Test
    void updateTrip_shouldRethrowBusinessException_whenValidatorRejectsInput() {
        UpdateTripDTO request = validUpdateRequest();

        when(tripValidator.validateUpdateInput(TRIP_ID, request))
                .thenThrow(new BusinessException(TRIP_TIME_INVALID, TRIP.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.updateTrip(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_TIME_INVALID, TRIP.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripRepository, never()).save(any(TripEntity.class));
    }

    @Test
    void updateTrip_shouldThrowAccessDenied_whenUserCannotEditTrip() {
        UpdateTripDTO request = validUpdateRequest();

        when(tripValidator.validateUpdateInput(TRIP_ID, request))
                .thenReturn("Updated Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenThrow(new BusinessException(TRIP_ACCESS_DENIED, COMMON.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.updateTrip(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, COMMON.name());

        verify(tripRepository, never()).save(any(TripEntity.class));
    }

    @Test
    void updateTrip_shouldThrowTripNameAlreadyExists_whenAnotherTripHasSameName() {
        UpdateTripDTO request = validUpdateRequest();

        when(tripValidator.validateUpdateInput(TRIP_ID, request))
                .thenReturn("Updated Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip("Old Trip"));
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCaseAndTripIdNot(
                USERNAME,
                "Updated Trip",
                TRIP_ID
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.updateTrip(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_NAME_ALREADY_EXISTS, COMMON.name());

        verify(tripRepository, never())
                .existsByUser_UsernameAndTripIdNotAndStartDateLessThanAndEndDateGreaterThan(
                        anyString(),
                        anyLong(),
                        any(),
                        any()
                );
        verify(tripRepository, never()).save(any(TripEntity.class));
    }

    @Test
    void updateTrip_shouldThrowTripOverlapWarning_whenOverlapExistsAndAllowOverlapFalse() {
        UpdateTripDTO request = validUpdateRequest();
        request.setAllowOverlap(false);

        when(tripValidator.validateUpdateInput(TRIP_ID, request))
                .thenReturn("Updated Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip("Old Trip"));
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCaseAndTripIdNot(
                USERNAME,
                "Updated Trip",
                TRIP_ID
        )).thenReturn(false);
        when(tripRepository.existsByUser_UsernameAndTripIdNotAndStartDateLessThanAndEndDateGreaterThan(
                USERNAME,
                TRIP_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.updateTrip(TRIP_ID, request)
        );

        assertBusinessException(exception, TRIP_OVERLAP_WARNING, TRIP.name());

        verify(destinationRepository, never()).existsByTrip_TripIdAndStartDateBefore(anyLong(), any());
        verify(tripRepository, never()).save(any(TripEntity.class));
    }

    @Test
    void updateTrip_shouldContinue_whenOverlapExistsButAllowOverlapTrue() {
        UpdateTripDTO request = validUpdateRequest();
        request.setAllowOverlap(true);

        TripEntity existingTrip = trip("Old Trip");

        mockErrorCode(TRIP_UPDATED_SUCCESS, TRIP.name());

        when(tripValidator.validateUpdateInput(TRIP_ID, request))
                .thenReturn("Updated Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(existingTrip);
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCaseAndTripIdNot(
                USERNAME,
                "Updated Trip",
                TRIP_ID
        )).thenReturn(false);
        when(tripRepository.existsByUser_UsernameAndTripIdNotAndStartDateLessThanAndEndDateGreaterThan(
                USERNAME,
                TRIP_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(true);
        when(destinationRepository.existsByTrip_TripIdAndStartDateBefore(
                TRIP_ID,
                request.getStartDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndEndDateAfter(
                TRIP_ID,
                request.getEndDate()
        )).thenReturn(false);
        when(tripMapper.toResponseDTO(existingTrip))
                .thenReturn(mock(TripResponseDTO.class));

        CompleteResponse<Object> response = tripService.updateTrip(TRIP_ID, request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_UPDATED_SUCCESS.getCode());

        verify(tripRepository).save(existingTrip);
    }

    @Test
    void updateTrip_shouldThrowTripDateConflict_whenExistingDestinationStartsBeforeNewTripStart() {
        UpdateTripDTO request = validUpdateRequest();

        when(tripValidator.validateUpdateInput(TRIP_ID, request))
                .thenReturn("Updated Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip("Old Trip"));
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCaseAndTripIdNot(
                USERNAME,
                "Updated Trip",
                TRIP_ID
        )).thenReturn(false);
        when(tripRepository.existsByUser_UsernameAndTripIdNotAndStartDateLessThanAndEndDateGreaterThan(
                USERNAME,
                TRIP_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndStartDateBefore(
                TRIP_ID,
                request.getStartDate()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.updateTrip(TRIP_ID, request)
        );

        assertBusinessException(
                exception,
                TRIP_DATE_CONFLICT_WITH_EXISTING_DESTINATION,
                TRIP.name()
        );

        verify(destinationRepository, never()).existsByTrip_TripIdAndEndDateAfter(anyLong(), any());
        verify(tripRepository, never()).save(any(TripEntity.class));
    }

    @Test
    void updateTrip_shouldThrowTripDateConflict_whenExistingDestinationEndsAfterNewTripEnd() {
        UpdateTripDTO request = validUpdateRequest();

        when(tripValidator.validateUpdateInput(TRIP_ID, request))
                .thenReturn("Updated Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(trip("Old Trip"));
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCaseAndTripIdNot(
                USERNAME,
                "Updated Trip",
                TRIP_ID
        )).thenReturn(false);
        when(tripRepository.existsByUser_UsernameAndTripIdNotAndStartDateLessThanAndEndDateGreaterThan(
                USERNAME,
                TRIP_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndStartDateBefore(
                TRIP_ID,
                request.getStartDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndEndDateAfter(
                TRIP_ID,
                request.getEndDate()
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.updateTrip(TRIP_ID, request)
        );

        assertBusinessException(
                exception,
                TRIP_DATE_CONFLICT_WITH_EXISTING_DESTINATION,
                TRIP.name()
        );

        verify(tripRepository, never()).save(any(TripEntity.class));
    }

    @Test
    void updateTrip_shouldWrapUnexpectedExceptionAsInternalServerError() {
        UpdateTripDTO request = validUpdateRequest();

        when(tripValidator.validateUpdateInput(TRIP_ID, request))
                .thenThrow(new RuntimeException("Unexpected validator failure"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.updateTrip(TRIP_ID, request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(tripRepository, never()).save(any(TripEntity.class));
    }

    // -------------------------------------------------------------------------
    // deleteTrip()
    // -------------------------------------------------------------------------

    @Test
    void deleteTrip_shouldDeleteTrip_whenUserIsOwner() throws IOException {
        TripEntity trip = trip("Adelaide Trip");

        mockErrorCode(TRIP_DELETED_SUCCESS, TRIP.name());

        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, USERNAME))
                .thenReturn(trip);

        CompleteResponse<Object> response = tripService.deleteTrip(TRIP_ID);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_DELETED_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isNull();

        verify(tripRepository).delete(trip);
        verify(cloudinaryImageClient).deleteImage("wandermate/trip-covers/users/1/trip-cover-1-old");
    }

    @Test
    void deleteTrip_shouldThrowInvalidInput_whenTripIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.deleteTrip(null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(authenticatedUserProvider, never()).getUsername();
        verify(tripAccessService, never()).getTripIfOwner(anyLong(), anyString());
        verify(tripRepository, never()).delete(any(TripEntity.class));
    }

    @Test
    void deleteTrip_shouldThrowAccessDenied_whenUserIsNotOwner() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, USERNAME))
                .thenThrow(new BusinessException(TRIP_ACCESS_DENIED, COMMON.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.deleteTrip(TRIP_ID)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, COMMON.name());

        verify(tripRepository, never()).delete(any(TripEntity.class));
    }

    @Test
    void deleteTrip_shouldWrapUnexpectedExceptionAsInternalServerError() {
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfOwner(TRIP_ID, USERNAME))
                .thenThrow(new RuntimeException("Database unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripService.deleteTrip(TRIP_ID)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(tripRepository, never()).delete(any(TripEntity.class));
    }

    // -------------------------------------------------------------------------
    // suggestCityList()
    // -------------------------------------------------------------------------

    @Test
    void suggestCityList_shouldReturnEmptyList_whenKeywordIsTooShort() {
        mockMinSuggestCharacter("2");
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        CompleteResponse<Object> response = tripService.suggestCityList("a");

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(SEARCH_INFO_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        List<CityEntity> body = (List<CityEntity>) response.getResponseBody().getBody();

        assertThat(body).isEmpty();

        verify(cityRepository, never())
                .findTop10ByCityNameStartingWithIgnoreCaseOrderByCityNameAsc(anyString());
    }

    @Test
    void suggestCityList_shouldReturnCities_whenKeywordIsLongEnough() {
        CityEntity city1 = mock(CityEntity.class);
        CityEntity city2 = mock(CityEntity.class);

        mockMinSuggestCharacter("2");
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(cityRepository.findTop10ByCityNameStartingWithIgnoreCaseOrderByCityNameAsc("ad"))
                .thenReturn(List.of(city1, city2));

        CompleteResponse<Object> response = tripService.suggestCityList(" ad ");

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(SEARCH_INFO_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        List<CityEntity> body = (List<CityEntity>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(city1, city2);
    }

    // -------------------------------------------------------------------------
    // suggestRestaurantList()
    // -------------------------------------------------------------------------

    @Test
    void suggestRestaurantList_shouldReturnEmptyList_whenKeywordIsTooShort() {
        mockMinSuggestCharacter("2");
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        CompleteResponse<Object> response = tripService.suggestRestaurantList("a");

        @SuppressWarnings("unchecked")
        List<RestaurantEntity> body =
                (List<RestaurantEntity>) response.getResponseBody().getBody();

        assertThat(body).isEmpty();

        verify(restaurantRepository, never())
                .findTop10ByRestaurantNameStartingWithIgnoreCaseOrderByRestaurantNameAsc(anyString());
    }

    @Test
    void suggestRestaurantList_shouldReturnRestaurants_whenKeywordIsLongEnough() {
        RestaurantEntity restaurant = mock(RestaurantEntity.class);

        mockMinSuggestCharacter("2");
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(restaurantRepository.findTop10ByRestaurantNameStartingWithIgnoreCaseOrderByRestaurantNameAsc("su"))
                .thenReturn(List.of(restaurant));

        CompleteResponse<Object> response = tripService.suggestRestaurantList(" su ");

        @SuppressWarnings("unchecked")
        List<RestaurantEntity> body =
                (List<RestaurantEntity>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(restaurant);
    }

    // -------------------------------------------------------------------------
    // suggestAccommodationList()
    // -------------------------------------------------------------------------

    @Test
    void suggestAccommodationList_shouldReturnEmptyList_whenKeywordIsTooShort() {
        mockMinSuggestCharacter("2");
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        CompleteResponse<Object> response = tripService.suggestAccommodationList("a");

        @SuppressWarnings("unchecked")
        List<AccommodationEntity> body =
                (List<AccommodationEntity>) response.getResponseBody().getBody();

        assertThat(body).isEmpty();

        verify(accommodationRepository, never())
                .findTop10ByAccommodationNameStartingWithIgnoreCaseOrderByAccommodationNameAsc(anyString());
    }

    @Test
    void suggestAccommodationList_shouldReturnAccommodation_whenKeywordIsLongEnough() {
        AccommodationEntity accommodation = mock(AccommodationEntity.class);

        mockMinSuggestCharacter("2");
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(accommodationRepository.findTop10ByAccommodationNameStartingWithIgnoreCaseOrderByAccommodationNameAsc("ho"))
                .thenReturn(List.of(accommodation));

        CompleteResponse<Object> response = tripService.suggestAccommodationList(" ho ");

        @SuppressWarnings("unchecked")
        List<AccommodationEntity> body =
                (List<AccommodationEntity>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(accommodation);
    }

    // -------------------------------------------------------------------------
    // searchCityList()
    // -------------------------------------------------------------------------

    @Test
    void searchCityList_shouldReturnEmptyList_whenKeywordIsBlank() {
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(tripValidator.isBlankKeyword(""))
                .thenReturn(true);

        CompleteResponse<Object> response = tripService.searchCityList("   ");

        @SuppressWarnings("unchecked")
        List<CityEntity> body = (List<CityEntity>) response.getResponseBody().getBody();

        assertThat(body).isEmpty();

        verify(cityRepository, never()).findAllByCityNameContainingIgnoreCase(anyString());
    }

    @Test
    void searchCityList_shouldReturnCities_whenKeywordIsNotBlank() {
        CityEntity city = mock(CityEntity.class);

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(tripValidator.isBlankKeyword("adelaide"))
                .thenReturn(false);
        when(cityRepository.findAllByCityNameContainingIgnoreCase("adelaide"))
                .thenReturn(List.of(city));

        CompleteResponse<Object> response = tripService.searchCityList(" adelaide ");

        @SuppressWarnings("unchecked")
        List<CityEntity> body = (List<CityEntity>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(city);
    }

    // -------------------------------------------------------------------------
    // searchRestaurantList()
    // -------------------------------------------------------------------------

    @Test
    void searchRestaurantList_shouldReturnEmptyList_whenKeywordIsBlank() {
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(tripValidator.isBlankKeyword(""))
                .thenReturn(true);

        CompleteResponse<Object> response = tripService.searchRestaurantList("   ");

        @SuppressWarnings("unchecked")
        List<RestaurantEntity> body =
                (List<RestaurantEntity>) response.getResponseBody().getBody();

        assertThat(body).isEmpty();

        verify(restaurantRepository, never()).findAllByRestaurantNameContainingIgnoreCase(anyString());
    }

    @Test
    void searchRestaurantList_shouldReturnRestaurants_whenKeywordIsNotBlank() {
        RestaurantEntity restaurant = mock(RestaurantEntity.class);

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(tripValidator.isBlankKeyword("sushi"))
                .thenReturn(false);
        when(restaurantRepository.findAllByRestaurantNameContainingIgnoreCase("sushi"))
                .thenReturn(List.of(restaurant));

        CompleteResponse<Object> response = tripService.searchRestaurantList(" sushi ");

        @SuppressWarnings("unchecked")
        List<RestaurantEntity> body =
                (List<RestaurantEntity>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(restaurant);
    }

    // -------------------------------------------------------------------------
    // searchAccommodationList()
    // -------------------------------------------------------------------------

    @Test
    void searchAccommodationList_shouldReturnEmptyList_whenKeywordIsBlank() {
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(tripValidator.isBlankKeyword(""))
                .thenReturn(true);

        CompleteResponse<Object> response = tripService.searchAccommodationList("   ");

        @SuppressWarnings("unchecked")
        List<AccommodationEntity> body =
                (List<AccommodationEntity>) response.getResponseBody().getBody();

        assertThat(body).isEmpty();

        verify(accommodationRepository, never()).findAllByAccommodationNameContainingIgnoreCase(anyString());
    }

    @Test
    void searchAccommodationList_shouldReturnAccommodation_whenKeywordIsNotBlank() {
        AccommodationEntity accommodation = mock(AccommodationEntity.class);

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(tripValidator.isBlankKeyword("hotel"))
                .thenReturn(false);
        when(accommodationRepository.findAllByAccommodationNameContainingIgnoreCase("hotel"))
                .thenReturn(List.of(accommodation));

        CompleteResponse<Object> response = tripService.searchAccommodationList(" hotel ");

        @SuppressWarnings("unchecked")
        List<AccommodationEntity> body =
                (List<AccommodationEntity>) response.getResponseBody().getBody();

        assertThat(body).containsExactly(accommodation);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateTripDTO validCreateRequest() {
        CreateTripDTO request = new CreateTripDTO();
        request.setTripName("Adelaide Trip");
        request.setDestination(" adelaide ");
        request.setStartDate(LocalDateTime.of(2026, 7, 10, 0, 0));
        request.setEndDate(LocalDateTime.of(2026, 7, 15, 23, 59));
        request.setAllowOverlap(false);
        request.setCoverImageUrl("https://res.cloudinary.com/demo/image/upload/new-cover.png");
        request.setCoverImagePublicId("wandermate/trip-covers/users/1/trip-cover-1-new");
        return request;
    }

    private UpdateTripDTO validUpdateRequest() {
        UpdateTripDTO request = new UpdateTripDTO();
        request.setTripName("Updated Trip");
        request.setDestination(" melbourne ");
        request.setStartDate(LocalDateTime.of(2026, 7, 20, 0, 0));
        request.setEndDate(LocalDateTime.of(2026, 7, 25, 23, 59));
        request.setAllowOverlap(false);
        request.setCoverImageUrl("https://res.cloudinary.com/demo/image/upload/new-cover.png");
        request.setCoverImagePublicId("wandermate/trip-covers/users/1/trip-cover-1-new");
        return request;
    }

    private User activeUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername(USERNAME);
        user.setEmail("justin@example.com");
        user.setActive(true);
        return user;
    }

    private TripEntity trip(String tripName) {
        TripEntity trip = new TripEntity();
        trip.setTripId(TRIP_ID);
        trip.setTripName(tripName);
        trip.setDestination("Adelaide");
        trip.setStartDate(LocalDateTime.of(2026, 7, 10, 0, 0));
        trip.setEndDate(LocalDateTime.of(2026, 7, 15, 23, 59));
        trip.setCreatedDate(LocalDateTime.now());
        trip.setCoverImageUrl("https://res.cloudinary.com/demo/image/upload/old-cover.png");
        trip.setCoverImagePublicId("wandermate/trip-covers/users/1/trip-cover-1-old");
        trip.setUser(activeUser());
        return trip;
    }


    private void mockSuccessfulUpdateDependencies(
            UpdateTripDTO request,
            TripEntity existingTrip,
            TripResponseDTO responseDTO
    ) {
        mockErrorCode(TRIP_UPDATED_SUCCESS, TRIP.name());

        when(tripValidator.validateUpdateInput(TRIP_ID, request))
                .thenReturn("Updated Trip");
        when(authenticatedUserProvider.getUsername())
                .thenReturn(USERNAME);
        when(tripAccessService.getTripIfCanEdit(TRIP_ID, USERNAME))
                .thenReturn(existingTrip);
        when(tripRepository.existsByUser_UsernameAndTripNameIgnoreCaseAndTripIdNot(
                USERNAME,
                "Updated Trip",
                TRIP_ID
        )).thenReturn(false);
        when(tripRepository.existsByUser_UsernameAndTripIdNotAndStartDateLessThanAndEndDateGreaterThan(
                USERNAME,
                TRIP_ID,
                request.getEndDate(),
                request.getStartDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndStartDateBefore(
                TRIP_ID,
                request.getStartDate()
        )).thenReturn(false);
        when(destinationRepository.existsByTrip_TripIdAndEndDateAfter(
                TRIP_ID,
                request.getEndDate()
        )).thenReturn(false);
        when(tripMapper.toResponseDTO(existingTrip))
                .thenReturn(responseDTO);
    }

    private void mockMinSuggestCharacter(String value) {
        mockConfig("MIN_SUGGEST_CHARACTER", value);
    }

    private void mockConfig(String configCode, String configValue) {
        ConfigurationEntity entity = new ConfigurationEntity();
        entity.setConfigCode(configCode);
        entity.setConfigValue(configValue);
        entity.setCreatedDate(LocalDateTime.now());

        when(configurationRepository.findByConfigCode(configCode))
                .thenReturn(Optional.of(entity));
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