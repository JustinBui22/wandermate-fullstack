package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.create.GenerateTripShareCodeRequest;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.entity.collaboration.TripShareCodeAttemptEntity;
import com.example.travellingapp.entity.collaboration.TripShareCodeEntity;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;

@Component
public class TripShareCodeValidator {

    private final TripCollaborationRequestValidator tripCollaborationRequestValidator;

    public TripShareCodeValidator(
            TripCollaborationRequestValidator tripCollaborationRequestValidator
    ) {
        this.tripCollaborationRequestValidator = tripCollaborationRequestValidator;
    }

    public void validateTripId(Long tripId) {
        // Validate trip ID
        if (tripId == null) {
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }
    }

    public TripEnum resolveDefaultRole(GenerateTripShareCodeRequest request) {
        // Default share-code role is VIEWER
        if (request == null || request.getDefaultRole() == null) {
            return TripEnum.VIEWER;
        }

        // Share-code role can only be EDITOR or VIEWER
        if (
                request.getDefaultRole() == TripEnum.EDITOR
                        || request.getDefaultRole() == TripEnum.VIEWER
        ) {
            return request.getDefaultRole();
        }

        throw new BusinessException(TRIP_OWNER_ROLE_CANNOT_BE_CHANGED, TRIP_MEMBER.name());
    }

    public void validateActiveCodeCanBeRegenerated(
            TripShareCodeEntity activeCode,
            LocalDateTime now,
            long cooldownSeconds
    ) {
        // Expired active code can be replaced immediately
        if (activeCode.getExpiresAt().isBefore(now)) {
            return;
        }

        // Cooldown blocks only active unused code
        if (activeCode.getCreatedDate().plusSeconds(cooldownSeconds).isAfter(now)) {
            throw new BusinessException(TRIP_SHARE_CODE_GENERATE_TOO_SOON, TRIP_MEMBER.name());
        }
    }

    public String normalizeCode(String code) {
        // Validate share code input
        if (code == null || code.isBlank()) {
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        return code.trim().toUpperCase();
    }

    public void validateAttemptIsNotRestricted(
            TripShareCodeAttemptEntity attempt,
            LocalDateTime now
    ) {
        // No attempt record means user is not restricted
        if (attempt == null) {
            return;
        }

        // Block user if restriction time has not passed
        if (
                attempt.getRestrictedUntil() != null
                        && attempt.getRestrictedUntil().isAfter(now)
        ) {
            throw new BusinessException(
                    TRIP_SHARE_CODE_ATTEMPT_RESTRICTED,
                    TRIP_MEMBER.name()
            );
        }
    }

    public void validateShareCodeCanBeUsed(
            TripShareCodeEntity shareCode,
            LocalDateTime now
    ) {
        // Used code cannot be reused
        if (shareCode.getCodeStatus() == TripEnum.USED) {
            throw new BusinessException(TRIP_SHARE_CODE_USED, TRIP_MEMBER.name());
        }

        // Revoked code cannot be used
        if (shareCode.getCodeStatus() == TripEnum.REVOKED) {
            throw new BusinessException(TRIP_SHARE_CODE_REVOKED, TRIP_MEMBER.name());
        }

        // Expired code cannot be used
        if (shareCode.getCodeStatus() == TripEnum.EXPIRED) {
            throw new BusinessException(TRIP_SHARE_CODE_EXPIRED, TRIP_MEMBER.name());
        }

        // Any non-active status is invalid
        if (shareCode.getCodeStatus() != TripEnum.ACTIVE) {
            throw new BusinessException(TRIP_SHARE_CODE_INACTIVE, TRIP_MEMBER.name());
        }

        // Active code cannot be used after expiry time
        if (shareCode.getExpiresAt().isBefore(now)) {
            throw new BusinessException(TRIP_SHARE_CODE_EXPIRED, TRIP_MEMBER.name());
        }
    }

    public void validateRequesterCanRequestToJoinByShareCode(
            TripShareCodeEntity shareCode,
            User requester
    ) {
        // Owner cannot request to join their own trip
        tripCollaborationRequestValidator.validateOwnerCannotRequestToJoinOwnTrip(
                shareCode.getTrip().getUser(),
                requester
        );

        // Requester must not already be a trip member
        tripCollaborationRequestValidator.validateUserIsNotAlreadyMember(
                shareCode.getTrip().getTripId(),
                requester
        );

        // Avoid duplicate pending invitation or join request between requester and owner
        tripCollaborationRequestValidator.validateNoPendingRequestBetweenUsers(
                shareCode.getTrip().getTripId(),
                requester,
                shareCode.getTrip().getUser()
        );
    }
}