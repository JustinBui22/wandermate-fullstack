package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.create.GenerateTripShareCodeRequest;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripShareCodeAttemptEntity;
import com.example.travellingapp.entity.collaboration.TripShareCodeEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripShareCodeValidatorTest {

    @Mock
    private TripCollaborationRequestValidator tripCollaborationRequestValidator;

    private TripShareCodeValidator validator;

    private static final Long TRIP_ID = 1L;
    private static final long OWNER_USER_ID = 10L;
    private static final long REQUESTER_USER_ID = 20L;

    @BeforeEach
    void setUp() {
        validator = new TripShareCodeValidator(tripCollaborationRequestValidator);
    }

    @Test
    void validateTripId_shouldNotThrow_whenTripIdIsValid() {
        assertDoesNotThrow(() -> validator.validateTripId(TRIP_ID));
    }

    @Test
    void validateTripId_shouldThrowInvalidInput_whenTripIdIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateTripId(null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
    }

    @Test
    void resolveDefaultRole_shouldReturnViewer_whenRequestIsNull() {
        TripEnum result = validator.resolveDefaultRole(null);

        assertThat(result).isEqualTo(TripEnum.VIEWER);
    }

    @Test
    void resolveDefaultRole_shouldReturnViewer_whenRoleIsNull() {
        GenerateTripShareCodeRequest request = new GenerateTripShareCodeRequest();

        TripEnum result = validator.resolveDefaultRole(request);

        assertThat(result).isEqualTo(TripEnum.VIEWER);
    }

    @Test
    void resolveDefaultRole_shouldReturnEditor_whenRoleIsEditor() {
        GenerateTripShareCodeRequest request = new GenerateTripShareCodeRequest();
        request.setDefaultRole(TripEnum.EDITOR);

        TripEnum result = validator.resolveDefaultRole(request);

        assertThat(result).isEqualTo(TripEnum.EDITOR);
    }

    @Test
    void resolveDefaultRole_shouldReturnViewer_whenRoleIsViewer() {
        GenerateTripShareCodeRequest request = new GenerateTripShareCodeRequest();
        request.setDefaultRole(TripEnum.VIEWER);

        TripEnum result = validator.resolveDefaultRole(request);

        assertThat(result).isEqualTo(TripEnum.VIEWER);
    }

    @Test
    void resolveDefaultRole_shouldThrow_whenRoleIsOwner() {
        GenerateTripShareCodeRequest request = new GenerateTripShareCodeRequest();
        request.setDefaultRole(TripEnum.OWNER);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.resolveDefaultRole(request)
        );

        assertBusinessException(
                exception,
                TRIP_OWNER_ROLE_CANNOT_BE_CHANGED,
                TRIP_MEMBER.name()
        );
    }

    @Test
    void validateActiveCodeCanBeRegenerated_shouldNotThrow_whenActiveCodeExpired() {
        TripShareCodeEntity activeCode = activeShareCode();
        activeCode.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertDoesNotThrow(() ->
                validator.validateActiveCodeCanBeRegenerated(
                        activeCode,
                        LocalDateTime.now(),
                        60
                )
        );
    }

    @Test
    void validateActiveCodeCanBeRegenerated_shouldNotThrow_whenCooldownPassed() {
        LocalDateTime now = LocalDateTime.now();
        TripShareCodeEntity activeCode = activeShareCode();
        activeCode.setCreatedDate(now.minusSeconds(120));
        activeCode.setExpiresAt(now.plusHours(1));

        assertDoesNotThrow(() ->
                validator.validateActiveCodeCanBeRegenerated(
                        activeCode,
                        now,
                        60
                )
        );
    }

    @Test
    void validateActiveCodeCanBeRegenerated_shouldThrowTooSoon_whenCooldownNotPassed() {
        LocalDateTime now = LocalDateTime.now();
        TripShareCodeEntity activeCode = activeShareCode();
        activeCode.setCreatedDate(now.minusSeconds(10));
        activeCode.setExpiresAt(now.plusHours(1));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateActiveCodeCanBeRegenerated(
                        activeCode,
                        now,
                        60
                )
        );

        assertBusinessException(
                exception,
                TRIP_SHARE_CODE_GENERATE_TOO_SOON,
                TRIP_MEMBER.name()
        );
    }

    @Test
    void normalizeCode_shouldTrimAndUppercaseCode() {
        String result = validator.normalizeCode("  wm-abc12345  ");

        assertThat(result).isEqualTo("WM-ABC12345");
    }

    @Test
    void normalizeCode_shouldThrowInvalidInput_whenCodeIsBlank() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.normalizeCode("   ")
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
    }

    @Test
    void normalizeCode_shouldThrowInvalidInput_whenCodeIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.normalizeCode(null)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
    }

    @Test
    void validateAttemptIsNotRestricted_shouldNotThrow_whenAttemptIsNull() {
        assertDoesNotThrow(() ->
                validator.validateAttemptIsNotRestricted(
                        null,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void validateAttemptIsNotRestricted_shouldNotThrow_whenRestrictedUntilIsNull() {
        TripShareCodeAttemptEntity attempt = new TripShareCodeAttemptEntity();
        attempt.setRestrictedUntil(null);

        assertDoesNotThrow(() ->
                validator.validateAttemptIsNotRestricted(
                        attempt,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void validateAttemptIsNotRestricted_shouldThrow_whenRestrictionStillActive() {
        TripShareCodeAttemptEntity attempt = new TripShareCodeAttemptEntity();
        attempt.setRestrictedUntil(LocalDateTime.now().plusMinutes(5));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateAttemptIsNotRestricted(
                        attempt,
                        LocalDateTime.now()
                )
        );

        assertBusinessException(
                exception,
                TRIP_SHARE_CODE_ATTEMPT_RESTRICTED,
                TRIP_MEMBER.name()
        );
    }

    @Test
    void validateShareCodeCanBeUsed_shouldNotThrow_whenCodeIsActiveAndNotExpired() {
        TripShareCodeEntity shareCode = activeShareCode();

        assertDoesNotThrow(() ->
                validator.validateShareCodeCanBeUsed(
                        shareCode,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void validateShareCodeCanBeUsed_shouldThrowUsed_whenCodeIsUsed() {
        TripShareCodeEntity shareCode = activeShareCode();
        shareCode.setCodeStatus(TripEnum.USED);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateShareCodeCanBeUsed(
                        shareCode,
                        LocalDateTime.now()
                )
        );

        assertBusinessException(exception, TRIP_SHARE_CODE_USED, TRIP_MEMBER.name());
    }

    @Test
    void validateShareCodeCanBeUsed_shouldThrowRevoked_whenCodeIsRevoked() {
        TripShareCodeEntity shareCode = activeShareCode();
        shareCode.setCodeStatus(TripEnum.REVOKED);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateShareCodeCanBeUsed(
                        shareCode,
                        LocalDateTime.now()
                )
        );

        assertBusinessException(exception, TRIP_SHARE_CODE_REVOKED, TRIP_MEMBER.name());
    }

    @Test
    void validateShareCodeCanBeUsed_shouldThrowExpired_whenStatusIsExpired() {
        TripShareCodeEntity shareCode = activeShareCode();
        shareCode.setCodeStatus(TripEnum.EXPIRED);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateShareCodeCanBeUsed(
                        shareCode,
                        LocalDateTime.now()
                )
        );

        assertBusinessException(exception, TRIP_SHARE_CODE_EXPIRED, TRIP_MEMBER.name());
    }

    @Test
    void validateShareCodeCanBeUsed_shouldThrowExpired_whenActiveCodePassedExpiryTime() {
        TripShareCodeEntity shareCode = activeShareCode();
        shareCode.setCodeStatus(TripEnum.ACTIVE);
        shareCode.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateShareCodeCanBeUsed(
                        shareCode,
                        LocalDateTime.now()
                )
        );

        assertBusinessException(exception, TRIP_SHARE_CODE_EXPIRED, TRIP_MEMBER.name());
    }

    @Test
    void validateShareCodeCanBeUsed_shouldThrowInactive_whenCodeStatusIsNotShareCodeStatus() {
        TripShareCodeEntity shareCode = activeShareCode();
        shareCode.setCodeStatus(TripEnum.PLANNING);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> validator.validateShareCodeCanBeUsed(
                        shareCode,
                        LocalDateTime.now()
                )
        );

        assertBusinessException(exception, TRIP_SHARE_CODE_INACTIVE, TRIP_MEMBER.name());
    }

    @Test
    void validateRequesterCanRequestToJoinByShareCode_shouldDelegateToCollaborationValidator() {
        User owner = user(OWNER_USER_ID, "owner");
        User requester = user(REQUESTER_USER_ID, "requester");
        TripEntity trip = trip(owner);
        TripShareCodeEntity shareCode = activeShareCode();
        shareCode.setTrip(trip);

        validator.validateRequesterCanRequestToJoinByShareCode(
                shareCode,
                requester
        );

        verify(tripCollaborationRequestValidator)
                .validateOwnerCannotRequestToJoinOwnTrip(owner, requester);

        verify(tripCollaborationRequestValidator)
                .validateUserIsNotAlreadyMember(TRIP_ID, requester);

        verify(tripCollaborationRequestValidator)
                .validateNoPendingRequestBetweenUsers(TRIP_ID, requester, owner);
    }

    private TripShareCodeEntity activeShareCode() {
        TripShareCodeEntity shareCode = new TripShareCodeEntity();
        shareCode.setCode("WM-ABC12345");
        shareCode.setCodeStatus(TripEnum.ACTIVE);
        shareCode.setDefaultRole(TripEnum.VIEWER);
        shareCode.setCreatedDate(LocalDateTime.now().minusMinutes(2));
        shareCode.setExpiresAt(LocalDateTime.now().plusHours(1));
        return shareCode;
    }

    private TripEntity trip(User owner) {
        TripEntity trip = new TripEntity();
        trip.setTripId(TRIP_ID);
        trip.setTripName("Adelaide Trip");
        trip.setDestination("Adelaide");
        trip.setStartDate(LocalDateTime.now().plusDays(10));
        trip.setEndDate(LocalDateTime.now().plusDays(15));
        trip.setUser(owner);
        return trip;
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