package com.example.travellingapp.mapper;

import com.example.travellingapp.dto.response.TripResponseDTO;
import com.example.travellingapp.entity.TripEntity;
import org.springframework.stereotype.Component;

@Component
public class TripMapper {

    public TripResponseDTO toResponseDTO(TripEntity trip) {
        return new TripResponseDTO(
                trip.getTripId(),
                trip.getTripName(),
                trip.getDestination(),
                trip.getCreatedDate(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getModifiedDate(),
                trip.getUser().getUserId(),
                trip.getUser().getUsername()
        );
    }
}