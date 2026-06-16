package com.example.travellingapp.mapper;

import com.example.travellingapp.dto.response.TripCollaborationRequestResponseDTO;
import com.example.travellingapp.entity.collaboration.TripCollaborationRequestEntity;
import org.springframework.stereotype.Component;

@Component
public class TripCollaborationRequestMapper {

    public TripCollaborationRequestResponseDTO toResponseDTO(
            TripCollaborationRequestEntity request
    ) {
        TripCollaborationRequestResponseDTO dto = new TripCollaborationRequestResponseDTO();
        dto.setRequestId(request.getRequestId());
        dto.setRequestedRole(request.getRequestedRole());
        dto.setRequestType(request.getRequestType());
        dto.setStatus(request.getStatus());
        dto.setCreatedDate(request.getCreatedDate());
        dto.setModifiedDate(request.getModifiedDate());
        dto.setRespondedDate(request.getRespondedDate());
        if (request.getTrip() != null) {
            dto.setTripId(request.getTrip().getTripId());
            dto.setTripName(request.getTrip().getTripName());
            dto.setDestination(request.getTrip().getDestination());
            dto.setTripStartDate(request.getTrip().getStartDate());
            dto.setTripEndDate(request.getTrip().getEndDate());
        }
        if (request.getRequester() != null) {
            dto.setRequesterUserId(request.getRequester().getUserId());
            dto.setRequesterUsername(request.getRequester().getUsername());
        }
        if (request.getTargetUser() != null) {
            dto.setTargetUserId(request.getTargetUser().getUserId());
            dto.setTargetUsername(request.getTargetUser().getUsername());
        }
        return dto;
    }
}
