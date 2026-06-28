package com.example.travellingapp.mapper;

import com.example.travellingapp.dto.response.MyTripOverlapWarningDTO;
import com.example.travellingapp.entity.TripEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TripOverlapWarningMapper {
    public MyTripOverlapWarningDTO toWarningDTO(TripEntity currentTrip, TripEntity overlappingTrip) {
        MyTripOverlapWarningDTO dto = new MyTripOverlapWarningDTO();

        // Set current shared trip information
        dto.setCurrentTripId(currentTrip.getTripId());
        dto.setCurrentTripName(currentTrip.getTripName());
        dto.setCurrentTripStartDate(currentTrip.getStartDate());
        dto.setCurrentTripEndDate(currentTrip.getEndDate());

        // Set overlapping trip information
        dto.setOverlappingTripId(overlappingTrip.getTripId());
        dto.setOverlappingTripName(overlappingTrip.getTripName());
        dto.setOverlappingTripStartDate(overlappingTrip.getStartDate());
        dto.setOverlappingTripEndDate(overlappingTrip.getEndDate());

        // Calculate the actual overlapping time range
        LocalDateTime overlapStartDate = currentTrip.getStartDate().isAfter(overlappingTrip.getStartDate())
                ? currentTrip.getStartDate()
                : overlappingTrip.getStartDate();

        LocalDateTime overlapEndDate = currentTrip.getEndDate().isBefore(overlappingTrip.getEndDate())
                ? currentTrip.getEndDate()
                : overlappingTrip.getEndDate();

        dto.setOverlapStartDate(overlapStartDate);
        dto.setOverlapEndDate(overlapEndDate);

        // Build warning message for frontend popup/banner
        dto.setMessage(
                "This shared trip overlaps with your trip \""
                        + overlappingTrip.getTripName()
                        + "\" from "
                        + overlapStartDate
                        + " to "
                        + overlapEndDate
                        + "."
        );
        return dto;
    }
}
