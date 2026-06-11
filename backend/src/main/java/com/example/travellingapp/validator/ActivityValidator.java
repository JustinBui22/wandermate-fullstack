package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.create.CreateActivityDTO;
import com.example.travellingapp.dto.request.update.UpdateActivityDTO;
import com.example.travellingapp.entity.DestinationEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import static com.example.travellingapp.enums.CommonEnum.ACTIVITY;
import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.util.Common.normalizeKeyword;

@Component
@RequiredArgsConstructor
@Log4j2
public class ActivityValidator {

    public String validateCreateInput(
            Long tripId,
            Long destinationId,
            CreateActivityDTO activityDTO
    ) {
        if (tripId == null || destinationId == null || activityDTO == null) {
            log.error("Invalid input to create activity!");
            throw new BusinessException(INVALID_INPUT, ACTIVITY.name());
        }

        if (activityDTO.getStartDateTime() == null || activityDTO.getEndDateTime() == null) {
            log.error("Activity start or end time is missing to create activity!");
            throw new BusinessException(ACTIVITY_TIME_NOT_FOUND, ACTIVITY.name());
        }

        if (!activityDTO.getStartDateTime().isBefore(activityDTO.getEndDateTime())) {
            log.error("Activity start time must be before end time to create activity!");
            throw new BusinessException(ACTIVITY_TIME_INVALID, ACTIVITY.name());
        }

        String activityName = normalizeKeyword(activityDTO.getActivityName());

        if (activityName.isBlank()) {
            log.error("Activity name is missing to create activity!");
            throw new BusinessException(ACTIVITY_NAME_NOT_FOUND, ACTIVITY.name());
        }

        return activityName;
    }

    public String validateUpdateInput(
            Long tripId,
            Long destinationId,
            Long activityId,
            UpdateActivityDTO activityDTO
    ) {
        if (tripId == null || destinationId == null || activityId == null || activityDTO == null) {
            log.error("Invalid input to update activity!");
            throw new BusinessException(INVALID_INPUT, ACTIVITY.name());
        }

        if (activityDTO.getStartDateTime() == null || activityDTO.getEndDateTime() == null) {
            log.error("Activity start or end time is missing to update activity!");
            throw new BusinessException(ACTIVITY_TIME_NOT_FOUND, ACTIVITY.name());
        }

        if (!activityDTO.getStartDateTime().isBefore(activityDTO.getEndDateTime())) {
            log.error("Activity start time must be before end time to update activity!");
            throw new BusinessException(ACTIVITY_TIME_INVALID, ACTIVITY.name());
        }

        String activityName = normalizeKeyword(activityDTO.getActivityName());

        if (activityName.isBlank()) {
            log.error("Activity name is missing to update activity!");
            throw new BusinessException(ACTIVITY_NAME_NOT_FOUND, ACTIVITY.name());
        }

        return activityName;
    }

    public void validateActivityInsideDestination(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            DestinationEntity destination
    ) {
        if (destination == null) {
            log.error("Destination is missing when validating activity time!");
            throw new BusinessException(DESTINATION_NOT_FOUND, ACTIVITY.name());
        }

        if (startDateTime.isBefore(destination.getStartDate())
                || endDateTime.isAfter(destination.getEndDate())) {
            log.error("Activity time must be within the destination duration!");
            throw new BusinessException(ACTIVITY_OUTSIDE_DESTINATION_RANGE, ACTIVITY.name());
        }
    }
}