package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.create.CreateTripDTO;
import com.example.travellingapp.dto.request.update.UpdateTripDTO;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.INVALID_INPUT;
import static com.example.travellingapp.util.Common.normalizeKeyword;

@Component
@RequiredArgsConstructor
@Log4j2
public class TripValidator {
    public String validateCreateInput(CreateTripDTO tripDTO) {
        if (tripDTO == null) {
            log.error("Invalid input to create trip!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        if (tripDTO.getStartDate() == null || tripDTO.getEndDate() == null) {
            log.error("Trip start date or end date is missing to create a trip!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        String tripName = normalizeKeyword(tripDTO.getTripName());
        String destination = normalizeKeyword(tripDTO.getDestination());

        if (tripName.isBlank() || destination.isBlank()) {
            log.error("Trip name or destination is missing to create a trip!!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        if (!tripDTO.getStartDate().isBefore(tripDTO.getEndDate())) {
            log.error("Start date must be before end date to create a trip!!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        return tripName;
    }

    public String validateUpdateInput(Long tripId, UpdateTripDTO tripDTO) {
        if (tripId == null || tripDTO == null) {
            log.error("Invalid input to update trip!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        if (tripDTO.getStartDate() == null || tripDTO.getEndDate() == null) {
            log.error("Trip start date or end date is missing to update a trip!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        String tripName = normalizeKeyword(tripDTO.getTripName());
        String destination = normalizeKeyword(tripDTO.getDestination());

        if (tripName.isBlank() || destination.isBlank()) {
            log.error("Trip name or destination is missing to update a trip!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        if (!tripDTO.getStartDate().isBefore(tripDTO.getEndDate())) {
            log.error("Start date must be before end date to update a trip!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }
        return tripName;
    }
    public boolean isBlankKeyword(String keyword) {
        return keyword.isBlank();
    }
}