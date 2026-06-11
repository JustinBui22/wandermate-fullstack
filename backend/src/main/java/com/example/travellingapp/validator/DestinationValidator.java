package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.create.CreateDestinationDTO;
import com.example.travellingapp.dto.request.update.UpdateDestinationDTO;
import com.example.travellingapp.entity.TripEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import static com.example.travellingapp.enums.CommonEnum.DESTINATION;
import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.util.Common.normalizeKeyword;

@Component
@RequiredArgsConstructor
@Log4j2
public class DestinationValidator {

    public String validateCreateInput(Long tripId, CreateDestinationDTO destinationDTO) {
        if (tripId == null || destinationDTO == null) {
            log.error("Invalid input to create destination!");
            throw new BusinessException(INVALID_INPUT, DESTINATION.name());
        }

        if (destinationDTO.getStartDate() == null || destinationDTO.getEndDate() == null) {
            log.error("Destination start date or end date is missing to create a destination!");
            throw new BusinessException(DESTINATION_TIME_NOT_FOUND, DESTINATION.name());
        }

        String destinationName = normalizeKeyword(destinationDTO.getDestinationName());

        if (destinationName.isBlank()) {
            log.error("Destination name is missing to create a destination!");
            throw new BusinessException(DESTINATION_NAME_NOT_FOUND, DESTINATION.name());
        }

        if (!destinationDTO.getStartDate().isBefore(destinationDTO.getEndDate())) {
            log.error("Start date must be before end date to create a destination!");
            throw new BusinessException(DESTINATION_TIME_INVALID, DESTINATION.name());
        }

        return destinationName;
    }

    public String validateUpdateInput(
            Long tripId,
            Long destinationId,
            UpdateDestinationDTO destinationDTO
    ) {
        if (tripId == null || destinationId == null || destinationDTO == null) {
            log.error("Invalid input to update destination!");
            throw new BusinessException(INVALID_INPUT, DESTINATION.name());
        }

        if (destinationDTO.getStartDate() == null || destinationDTO.getEndDate() == null) {
            log.error("Destination start date or end date is missing to update a destination!");
            throw new BusinessException(DESTINATION_TIME_NOT_FOUND, DESTINATION.name());
        }

        String destinationName = normalizeKeyword(destinationDTO.getDestinationName());

        if (destinationName.isBlank()) {
            log.error("Destination name is missing to update a destination!");
            throw new BusinessException(DESTINATION_NAME_NOT_FOUND, DESTINATION.name());
        }

        if (!destinationDTO.getStartDate().isBefore(destinationDTO.getEndDate())) {
            log.error("Start date must be before end date to update a destination!");
            throw new BusinessException(DESTINATION_TIME_INVALID, DESTINATION.name());
        }

        return destinationName;
    }

    public void validateDestinationInsideTrip(
            LocalDateTime destinationStart,
            LocalDateTime destinationEnd,
            TripEntity trip
    ) {
        if (trip == null) {
            log.error("Trip is missing when validating destination date range!");
            throw new BusinessException(INVALID_INPUT, DESTINATION.name());
        }

        if (trip.getStartDate() == null || trip.getEndDate() == null) {
            log.error("Trip start date or end date is missing when validating destination date range!");
            throw new BusinessException(TRIP_TIME_NOT_FOUND, DESTINATION.name());
        }

        boolean startsBeforeTrip = destinationStart.isBefore(trip.getStartDate());
        boolean endsAfterTrip = destinationEnd.isAfter(trip.getEndDate());

        if (startsBeforeTrip || endsAfterTrip) {
            log.error("Destination date range must be inside the trip date range!");
            throw new BusinessException(DESTINATION_DATE_OUTSIDE_TRIP_RANGE, DESTINATION.name());
        }
    }
}