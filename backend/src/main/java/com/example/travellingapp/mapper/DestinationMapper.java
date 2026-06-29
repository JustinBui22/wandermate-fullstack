package com.example.travellingapp.mapper;

import com.example.travellingapp.dto.response.DestinationResponseDTO;
import com.example.travellingapp.entity.DestinationEntity;
import org.springframework.stereotype.Component;

@Component
public class DestinationMapper {

    public DestinationResponseDTO toResponseDTO(DestinationEntity destination) {
        DestinationResponseDTO dto = new DestinationResponseDTO();
        dto.setDestinationId(destination.getDestinationId());
        dto.setDestinationName(destination.getDestinationName());
        dto.setStartDate(destination.getStartDate());
        dto.setEndDate(destination.getEndDate());
        dto.setDestinationOrder(destination.getDestinationOrder());
        dto.setNotes(destination.getNotes());
        dto.setCreatedDate(destination.getCreatedDate());
        dto.setModifiedDate(destination.getModifiedDate());

        if (destination.getTrip() != null) {
            dto.setTripId(destination.getTrip().getTripId());
        }

        if (destination.getCreatedBy() != null) {
            dto.setCreatedByUserId(destination.getCreatedBy().getUserId());
            dto.setCreatedByUsername(destination.getCreatedBy().getUsername());
            dto.setCreatedByDisplayName(destination.getCreatedBy().getDisplayName());
            dto.setCreatedByProfileImageUrl(destination.getCreatedBy().getProfileImageUrl());
        }

        if (destination.getModifiedBy() != null) {
            dto.setModifiedByUserId(destination.getModifiedBy().getUserId());
            dto.setModifiedByUsername(destination.getModifiedBy().getUsername());
            dto.setModifiedByDisplayName(destination.getModifiedBy().getDisplayName());
            dto.setModifiedByProfileImageUrl(destination.getModifiedBy().getProfileImageUrl());
        }
        return dto;
    }
}