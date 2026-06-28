package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.create.CreateTripDTO;
import com.example.travellingapp.dto.request.update.UpdateTripDTO;
import com.example.travellingapp.enums.TripEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.CommonEnum.TRIP;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.util.Common.normalizeKeyword;

@Component
@RequiredArgsConstructor
@Log4j2
public class TripValidator {
    public String validateCreateInput(CreateTripDTO tripDTO) {
        if (tripDTO == null) {
            log.error("Invalid input to create trip!");
            throw new BusinessException(INVALID_INPUT, TRIP.name());
        }

        if (tripDTO.getStartDate() == null || tripDTO.getEndDate() == null) {
            log.error("Trip start date or end date is missing to create a trip!");
            throw new BusinessException(TRIP_TIME_NOT_FOUND, TRIP.name());
        }

        String tripName = normalizeKeyword(tripDTO.getTripName());
        String destination = normalizeKeyword(tripDTO.getDestination());

        if (tripName.isBlank() || destination.isBlank()) {
            log.error("Trip name or destination is missing to create a trip!!");
            throw new BusinessException(TRIP_NAME_NOT_FOUND, TRIP.name());
        }

        if (!tripDTO.getStartDate().isBefore(tripDTO.getEndDate())) {
            log.error("Start date must be before end date to create a trip!!");
            throw new BusinessException(TRIP_TIME_INVALID, TRIP.name());
        }

        return tripName;
    }

    public String validateUpdateInput(Long tripId, UpdateTripDTO tripDTO) {
        if (tripId == null || tripDTO == null) {
            log.error("Invalid input to update trip!");
            throw new BusinessException(INVALID_INPUT, TRIP.name());
        }

        if (tripDTO.getStartDate() == null || tripDTO.getEndDate() == null) {
            log.error("Trip start date or end date is missing to update a trip!");
            throw new BusinessException(TRIP_TIME_NOT_FOUND, TRIP.name());
        }

        String tripName = normalizeKeyword(tripDTO.getTripName());
        String destination = normalizeKeyword(tripDTO.getDestination());

        if (tripName.isBlank() || destination.isBlank()) {
            log.error("Trip name or destination is missing to update a trip!");
            throw new BusinessException(TRIP_NAME_NOT_FOUND, TRIP.name());
        }

        if (!tripDTO.getStartDate().isBefore(tripDTO.getEndDate())) {
            log.error("Start date must be before end date to update a trip!");
            throw new BusinessException(TRIP_TIME_INVALID, TRIP.name());
        }
        return tripName;
    }
    public boolean isBlankKeyword(String keyword) {
        return keyword.isBlank();
    }

    public TripEnum validateOwnershipFilter(TripEnum ownership) {
        if (ownership == null) {
            return TripEnum.ALL;
        }

        if (ownership == TripEnum.ALL
                || ownership == TripEnum.CREATED
                || ownership == TripEnum.JOINED) {
            return ownership;
        }

        log.error("Invalid trip ownership filter!");
        throw new BusinessException(INVALID_INPUT, COMMON.name());
    }

    public TripEnum validateStatusFilter(String status) {
        if (status == null || status.isBlank()) {
            return TripEnum.ALL;
        }

        if (TripEnum.ALL.name().equalsIgnoreCase(status)) {
            return TripEnum.ALL;
        }

        TripEnum statusEnum;

        try {
            statusEnum = TripEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid trip status filter: {}", status);
            throw new BusinessException(TRIP_STATUS_INVALID, TRIP.name());
        }

        if (statusEnum.getGroup() != TripEnum.Group.STATUS) {
            log.error("Trip enum {} is not a status enum!", statusEnum);
            throw new BusinessException(TRIP_STATUS_INVALID, TRIP.name());
        }

        return statusEnum;
    }

    public TripEnum validateSortOption(TripEnum sort) {
        if (sort == null) {
            return TripEnum.MODIFIED_DATE_DESC;
        }

        if (sort.getGroup() == TripEnum.Group.SORT) {
            return sort;
        }
        log.error("Invalid trip sort option!");
        throw new BusinessException(INVALID_INPUT, COMMON.name());
    }
}