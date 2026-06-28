package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.response.MyTripOverlapWarningDTO;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.TripOverlapWarningMapper;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.TripAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripOverlapWarningServiceImplTest {

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private TripAccessService tripAccessService;

    @Mock
    private TripOverlapWarningMapper tripOverlapWarningMapper;

    private TripOverlapWarningServiceImpl tripOverlapWarningService;

    private static final Long CURRENT_TRIP_ID = 1L;
    private static final String OWNER_USERNAME = "OwnerUser";
    private static final String MEMBER_USERNAME = "MemberUser";

    @BeforeEach
    void setUp() {
        tripOverlapWarningService = new TripOverlapWarningServiceImpl(
                tripMemberRepository,
                errorCodeRepository,
                authenticatedUserProvider,
                tripAccessService,
                tripOverlapWarningMapper
        );
    }

    @Test
    void getOverlapWarnings_shouldReturnEmptyList_whenCurrentUserIsOwner() {
        TripEntity currentTrip = currentTrip();

        mockErrorCode(TRIP_OVERLAP_WARNINGS_RETRIEVED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(OWNER_USERNAME);
        when(tripAccessService.getTripIfCanView(CURRENT_TRIP_ID, OWNER_USERNAME))
                .thenReturn(currentTrip);
        when(tripAccessService.getUserRole(CURRENT_TRIP_ID, OWNER_USERNAME))
                .thenReturn(TripEnum.OWNER);

        CompleteResponse<Object> response = tripOverlapWarningService.getOverlapWarnings(CURRENT_TRIP_ID);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(TRIP_OVERLAP_WARNINGS_RETRIEVED_SUCCESS.getCode());

        @SuppressWarnings("unchecked")
        List<MyTripOverlapWarningDTO> body =
                (List<MyTripOverlapWarningDTO>) response.getResponseBody().getBody();

        assertThat(body).isEmpty();

        verify(tripMemberRepository, never())
                .findOverlappingTripsForMember(anyString(), anyLong(), any(), any());

        verifyNoInteractions(tripOverlapWarningMapper);
    }

    @Test
    void getOverlapWarnings_shouldReturnWarnings_whenCurrentUserIsEditorAndHasOverlap() {
        TripEntity currentTrip = currentTrip();
        TripEntity overlappingTrip = overlappingTrip();
        MyTripOverlapWarningDTO warningDTO = warningDTO();

        mockErrorCode(TRIP_OVERLAP_WARNINGS_RETRIEVED_SUCCESS, TRIP_MEMBER.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(MEMBER_USERNAME);
        when(tripAccessService.getTripIfCanView(CURRENT_TRIP_ID, MEMBER_USERNAME))
                .thenReturn(currentTrip);
        when(tripAccessService.getUserRole(CURRENT_TRIP_ID, MEMBER_USERNAME))
                .thenReturn(TripEnum.EDITOR);

        when(tripMemberRepository.findOverlappingTripsForMember(
                MEMBER_USERNAME,
                CURRENT_TRIP_ID,
                currentTrip.getStartDate(),
                currentTrip.getEndDate()
        )).thenReturn(List.of(overlappingTrip));

        when(tripOverlapWarningMapper.toWarningDTO(currentTrip, overlappingTrip))
                .thenReturn(warningDTO);

        CompleteResponse<Object> response = tripOverlapWarningService.getOverlapWarnings(CURRENT_TRIP_ID);

        @SuppressWarnings("unchecked")
        List<MyTripOverlapWarningDTO> body =
                (List<MyTripOverlapWarningDTO>) response.getResponseBody().getBody();

        assertThat(body).hasSize(1);
        assertThat(body.get(0)).isEqualTo(warningDTO);
        assertThat(body.get(0).getCurrentTripId()).isEqualTo(CURRENT_TRIP_ID);
        assertThat(body.get(0).getOverlappingTripId()).isEqualTo(2L);
        assertThat(body.get(0).getOverlappingTripName()).isEqualTo("Melbourne Trip");

        verify(tripOverlapWarningMapper).toWarningDTO(currentTrip, overlappingTrip);
    }

    @Test
    void getOverlapWarnings_shouldThrowInvalidInput_whenTripIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripOverlapWarningService.getOverlapWarnings(null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verifyNoInteractions(authenticatedUserProvider);
        verifyNoInteractions(tripAccessService);
        verifyNoInteractions(tripMemberRepository);
        verifyNoInteractions(tripOverlapWarningMapper);
    }

    @Test
    void getOverlapWarnings_shouldRethrowBusinessException_whenUserCannotViewTrip() {
        when(authenticatedUserProvider.getUsername()).thenReturn(MEMBER_USERNAME);
        when(tripAccessService.getTripIfCanView(CURRENT_TRIP_ID, MEMBER_USERNAME))
                .thenThrow(new BusinessException(TRIP_ACCESS_DENIED, TRIP_MEMBER.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripOverlapWarningService.getOverlapWarnings(CURRENT_TRIP_ID)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verifyNoInteractions(tripMemberRepository);
        verifyNoInteractions(tripOverlapWarningMapper);
    }

    @Test
    void buildWarningsForUser_shouldMapOverlappingTripsThroughMapper() {
        TripEntity currentTrip = currentTrip();
        TripEntity overlappingTrip = overlappingTrip();
        MyTripOverlapWarningDTO warningDTO = warningDTO();

        when(tripMemberRepository.findOverlappingTripsForMember(
                MEMBER_USERNAME,
                CURRENT_TRIP_ID,
                currentTrip.getStartDate(),
                currentTrip.getEndDate()
        )).thenReturn(List.of(overlappingTrip));

        when(tripOverlapWarningMapper.toWarningDTO(currentTrip, overlappingTrip))
                .thenReturn(warningDTO);

        List<MyTripOverlapWarningDTO> warnings =
                tripOverlapWarningService.buildWarningsForUser(currentTrip, MEMBER_USERNAME);

        assertThat(warnings).hasSize(1);
        assertThat(warnings.get(0)).isEqualTo(warningDTO);

        verify(tripMemberRepository).findOverlappingTripsForMember(
                MEMBER_USERNAME,
                CURRENT_TRIP_ID,
                currentTrip.getStartDate(),
                currentTrip.getEndDate()
        );

        verify(tripOverlapWarningMapper).toWarningDTO(currentTrip, overlappingTrip);
    }

    @Test
    void buildWarningsForUser_shouldThrowInvalidInput_whenCurrentTripIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripOverlapWarningService.buildWarningsForUser(null, MEMBER_USERNAME)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verifyNoInteractions(tripMemberRepository);
        verifyNoInteractions(tripOverlapWarningMapper);
    }

    @Test
    void buildWarningsForUser_shouldThrowInvalidInput_whenCurrentTripIdIsNull() {
        TripEntity trip = currentTrip();
        trip.setTripId(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripOverlapWarningService.buildWarningsForUser(trip, MEMBER_USERNAME)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verifyNoInteractions(tripMemberRepository);
        verifyNoInteractions(tripOverlapWarningMapper);
    }

    @Test
    void buildWarningsForUser_shouldThrowInvalidInput_whenUsernameIsBlank() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripOverlapWarningService.buildWarningsForUser(currentTrip(), "   ")
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verifyNoInteractions(tripMemberRepository);
        verifyNoInteractions(tripOverlapWarningMapper);
    }

    private TripEntity currentTrip() {
        TripEntity trip = new TripEntity();
        trip.setTripId(CURRENT_TRIP_ID);
        trip.setTripName("Adelaide Trip");
        trip.setStartDate(LocalDateTime.of(2026, 7, 10, 9, 0));
        trip.setEndDate(LocalDateTime.of(2026, 7, 15, 18, 0));
        return trip;
    }

    private TripEntity overlappingTrip() {
        TripEntity trip = new TripEntity();
        trip.setTripId(2L);
        trip.setTripName("Melbourne Trip");
        trip.setStartDate(LocalDateTime.of(2026, 7, 12, 9, 0));
        trip.setEndDate(LocalDateTime.of(2026, 7, 18, 18, 0));
        return trip;
    }

    private MyTripOverlapWarningDTO warningDTO() {
        MyTripOverlapWarningDTO dto = new MyTripOverlapWarningDTO();
        dto.setCurrentTripId(CURRENT_TRIP_ID);
        dto.setCurrentTripName("Adelaide Trip");
        dto.setOverlappingTripId(2L);
        dto.setOverlappingTripName("Melbourne Trip");
        dto.setOverlapStartDate(LocalDateTime.of(2026, 7, 12, 9, 0));
        dto.setOverlapEndDate(LocalDateTime.of(2026, 7, 15, 18, 0));
        dto.setMessage("This trip overlaps with Melbourne Trip.");
        return dto;
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