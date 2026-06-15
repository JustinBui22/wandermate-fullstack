package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.AddTripMemberDTO;
import com.example.travellingapp.dto.request.update.UpdateTripMemberRoleDTO;
import com.example.travellingapp.enums.TripMemberRoleEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP_MEMBER;
import static com.example.travellingapp.enums.ErrorCodeEnum.INVALID_INPUT;
import static com.example.travellingapp.enums.ErrorCodeEnum.OWNER_CANNOT_BE_ASSIGNED_MANUALLY;
import static com.example.travellingapp.util.Common.normalizeKeyword;

@Component
@RequiredArgsConstructor
@Log4j2
public class TripMemberValidator {

    public void validateTripId(Long tripId) {
        if (tripId == null) {
            log.error("Trip ID is missing for trip member request!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }
    }

    public void validateTripMemberId(Long tripMemberId) {
        if (tripMemberId == null) {
            log.error("Trip member ID is missing for trip member request!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }
    }

    public String validateAddTripMemberInput(
            Long tripId,
            AddTripMemberDTO addTripMemberDTO
    ) {
        validateTripId(tripId);

        if (addTripMemberDTO == null) {
            log.error("Invalid input to add trip member!");
            throw new BusinessException(INVALID_INPUT, TRIP_MEMBER.name());
        }
        String username = normalizeKeyword(addTripMemberDTO.getUsername());
        if (username.isBlank()) {
            log.error("Username is missing to add trip member!");
            throw new BusinessException(INVALID_INPUT, TRIP_MEMBER.name());
        }
        validateNonOwnerRole(addTripMemberDTO.getRole());
        return username;
    }

    public TripMemberRoleEnum validateUpdateTripMemberRoleInput(
            Long tripId,
            Long tripMemberId,
            UpdateTripMemberRoleDTO updateTripMemberRoleDTO
    ) {
        validateTripId(tripId);
        validateTripMemberId(tripMemberId);

        if (updateTripMemberRoleDTO == null) {
            log.error("Invalid input to update trip member role!");
            throw new BusinessException(INVALID_INPUT, TRIP_MEMBER.name());
        }

        return validateNonOwnerRole(updateTripMemberRoleDTO.getRole());
    }

    public void validateRemoveTripMemberInput(
            Long tripId,
            Long tripMemberId
    ) {
        validateTripId(tripId);
        validateTripMemberId(tripMemberId);
    }

    private TripMemberRoleEnum validateNonOwnerRole(String role) {
        String normalizedRole = normalizeKeyword(role);

        if (normalizedRole.isBlank()) {
            log.error("Trip member role is missing!");
            throw new BusinessException(INVALID_INPUT, TRIP_MEMBER.name());
        }
        try {
            TripMemberRoleEnum tripMemberRole =
                    TripMemberRoleEnum.valueOf(normalizedRole.toUpperCase());

            if (tripMemberRole == TripMemberRoleEnum.OWNER) {
                log.error("OWNER role cannot be assigned manually!");
                throw new BusinessException(OWNER_CANNOT_BE_ASSIGNED_MANUALLY, TRIP_MEMBER.name());
            }
            return tripMemberRole;
        } catch (IllegalArgumentException e) {
            log.error("Trip member role {} is invalid!", normalizedRole);
            throw new BusinessException(INVALID_INPUT, TRIP_MEMBER.name());
        }
    }
}
