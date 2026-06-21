package com.example.travellingapp.service.impl;

import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.collaboration.TripMemberEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.TripRepository;
import com.example.travellingapp.repository.collaboration.TripMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripAccessServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    private TripAccessServiceImpl tripAccessService;

    private static final Long TRIP_ID = 1L;
    private static final String OWNER_USERNAME = "JustinBo123";
    private static final String EDITOR_USERNAME = "EditorUser";
    private static final String VIEWER_USERNAME = "ViewerUser";
    private static final String NON_MEMBER_USERNAME = "RandomUser";

    @BeforeEach
    void setUp() {
        tripAccessService = new TripAccessServiceImpl(
                tripRepository,
                tripMemberRepository
        );
    }

    // -------------------------------------------------------------------------
    // getUserRole()
    // -------------------------------------------------------------------------

    @Test
    void getUserRole_shouldReturnOwnerRole_whenUserIsOwnerMember() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                OWNER_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.OWNER)));

        TripEnum role = tripAccessService.getUserRole(TRIP_ID, OWNER_USERNAME);

        assertThat(role).isEqualTo(TripEnum.OWNER);
    }

    @Test
    void getUserRole_shouldReturnEditorRole_whenUserIsEditorMember() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                EDITOR_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.EDITOR)));

        TripEnum role = tripAccessService.getUserRole(TRIP_ID, EDITOR_USERNAME);

        assertThat(role).isEqualTo(TripEnum.EDITOR);
    }

    @Test
    void getUserRole_shouldThrowAccessDenied_whenUserIsNotTripMember() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                NON_MEMBER_USERNAME
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.getUserRole(TRIP_ID, NON_MEMBER_USERNAME)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());
    }

    // -------------------------------------------------------------------------
    // assertCanView()
    // -------------------------------------------------------------------------

    @Test
    void assertCanView_shouldPass_whenUserIsTripMember() {
        when(tripMemberRepository.existsByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                VIEWER_USERNAME
        )).thenReturn(true);

        assertDoesNotThrow(() -> tripAccessService.assertCanView(TRIP_ID, VIEWER_USERNAME));
    }

    @Test
    void assertCanView_shouldThrowInvalidInput_whenTripIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.assertCanView(null, VIEWER_USERNAME)
        );

        assertBusinessException(exception, INVALID_INPUT, TRIP_MEMBER.name());

        verifyNoInteractions(tripMemberRepository);
    }

    @Test
    void assertCanView_shouldThrowInvalidInput_whenUsernameIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.assertCanView(TRIP_ID, null)
        );

        assertBusinessException(exception, INVALID_INPUT, TRIP_MEMBER.name());

        verifyNoInteractions(tripMemberRepository);
    }

    @Test
    void assertCanView_shouldThrowInvalidInput_whenUsernameIsBlank() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.assertCanView(TRIP_ID, "   ")
        );

        assertBusinessException(exception, INVALID_INPUT, TRIP_MEMBER.name());

        verifyNoInteractions(tripMemberRepository);
    }

    @Test
    void assertCanView_shouldThrowAccessDenied_whenUserIsNotTripMember() {
        when(tripMemberRepository.existsByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                NON_MEMBER_USERNAME
        )).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.assertCanView(TRIP_ID, NON_MEMBER_USERNAME)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());
    }

    // -------------------------------------------------------------------------
    // assertCanEdit()
    // -------------------------------------------------------------------------

    @Test
    void assertCanEdit_shouldPass_whenUserIsOwner() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                OWNER_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.OWNER)));

        assertDoesNotThrow(() -> tripAccessService.assertCanEdit(TRIP_ID, OWNER_USERNAME));
    }

    @Test
    void assertCanEdit_shouldPass_whenUserIsEditor() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                EDITOR_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.EDITOR)));

        assertDoesNotThrow(() -> tripAccessService.assertCanEdit(TRIP_ID, EDITOR_USERNAME));
    }

    @Test
    void assertCanEdit_shouldThrowAccessDenied_whenUserIsViewer() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                VIEWER_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.VIEWER)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.assertCanEdit(TRIP_ID, VIEWER_USERNAME)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());
    }

    @Test
    void assertCanEdit_shouldThrowAccessDenied_whenUserIsNotTripMember() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                NON_MEMBER_USERNAME
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.assertCanEdit(TRIP_ID, NON_MEMBER_USERNAME)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());
    }

    // -------------------------------------------------------------------------
    // assertIsOwner()
    // -------------------------------------------------------------------------

    @Test
    void assertIsOwner_shouldPass_whenUserIsOwner() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                OWNER_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.OWNER)));

        assertDoesNotThrow(() -> tripAccessService.assertIsOwner(TRIP_ID, OWNER_USERNAME));
    }

    @Test
    void assertIsOwner_shouldThrowAccessDenied_whenUserIsEditor() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                EDITOR_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.EDITOR)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.assertIsOwner(TRIP_ID, EDITOR_USERNAME)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());
    }

    @Test
    void assertIsOwner_shouldThrowAccessDenied_whenUserIsViewer() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                VIEWER_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.VIEWER)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.assertIsOwner(TRIP_ID, VIEWER_USERNAME)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());
    }

    // -------------------------------------------------------------------------
    // getTripIfCanView()
    // -------------------------------------------------------------------------

    @Test
    void getTripIfCanView_shouldReturnTrip_whenUserCanViewAndTripExists() {
        TripEntity trip = trip();

        when(tripMemberRepository.existsByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                VIEWER_USERNAME
        )).thenReturn(true);
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        TripEntity result = tripAccessService.getTripIfCanView(TRIP_ID, VIEWER_USERNAME);

        assertThat(result).isEqualTo(trip);
    }

    @Test
    void getTripIfCanView_shouldThrowTripNotFound_whenUserCanViewButTripDoesNotExist() {
        when(tripMemberRepository.existsByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                VIEWER_USERNAME
        )).thenReturn(true);
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.getTripIfCanView(TRIP_ID, VIEWER_USERNAME)
        );

        assertBusinessException(exception, TRIP_NOT_FOUND, TRIP_MEMBER.name());
    }

    @Test
    void getTripIfCanView_shouldThrowAccessDenied_whenUserCannotView() {
        when(tripMemberRepository.existsByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                NON_MEMBER_USERNAME
        )).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.getTripIfCanView(TRIP_ID, NON_MEMBER_USERNAME)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(tripRepository, never()).findById(anyLong());
    }

    // -------------------------------------------------------------------------
    // getTripIfCanEdit()
    // -------------------------------------------------------------------------

    @Test
    void getTripIfCanEdit_shouldReturnTrip_whenUserIsEditorAndTripExists() {
        TripEntity trip = trip();

        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                EDITOR_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.EDITOR)));
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        TripEntity result = tripAccessService.getTripIfCanEdit(TRIP_ID, EDITOR_USERNAME);

        assertThat(result).isEqualTo(trip);
    }

    @Test
    void getTripIfCanEdit_shouldThrowAccessDenied_whenUserIsViewer() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                VIEWER_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.VIEWER)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.getTripIfCanEdit(TRIP_ID, VIEWER_USERNAME)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(tripRepository, never()).findById(anyLong());
    }

    @Test
    void getTripIfCanEdit_shouldThrowTripNotFound_whenUserCanEditButTripDoesNotExist() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                EDITOR_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.EDITOR)));
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.getTripIfCanEdit(TRIP_ID, EDITOR_USERNAME)
        );

        assertBusinessException(exception, TRIP_NOT_FOUND, TRIP_MEMBER.name());
    }

    // -------------------------------------------------------------------------
    // getTripIfOwner()
    // -------------------------------------------------------------------------

    @Test
    void getTripIfOwner_shouldReturnTrip_whenUserIsOwnerAndTripExists() {
        TripEntity trip = trip();

        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                OWNER_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.OWNER)));
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.of(trip));

        TripEntity result = tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME);

        assertThat(result).isEqualTo(trip);
    }

    @Test
    void getTripIfOwner_shouldThrowAccessDenied_whenUserIsEditor() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                EDITOR_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.EDITOR)));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.getTripIfOwner(TRIP_ID, EDITOR_USERNAME)
        );

        assertBusinessException(exception, TRIP_ACCESS_DENIED, TRIP_MEMBER.name());

        verify(tripRepository, never()).findById(anyLong());
    }

    @Test
    void getTripIfOwner_shouldThrowTripNotFound_whenUserIsOwnerButTripDoesNotExist() {
        when(tripMemberRepository.findByTrip_TripIdAndUser_UsernameAndUser_IsActiveTrue(
                TRIP_ID,
                OWNER_USERNAME
        )).thenReturn(Optional.of(member(TripEnum.OWNER)));
        when(tripRepository.findById(TRIP_ID))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tripAccessService.getTripIfOwner(TRIP_ID, OWNER_USERNAME)
        );

        assertBusinessException(exception, TRIP_NOT_FOUND, TRIP_MEMBER.name());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private TripMemberEntity member(TripEnum role) {
        TripMemberEntity member = new TripMemberEntity();
        member.setRole(role);
        return member;
    }

    private TripEntity trip() {
        TripEntity trip = new TripEntity();
        trip.setTripId(TRIP_ID);
        trip.setTripName("Adelaide Trip");
        trip.setDestination("Adelaide");
        return trip;
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