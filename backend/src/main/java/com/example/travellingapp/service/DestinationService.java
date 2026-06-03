package com.example.travellingapp.service;

import com.example.travellingapp.dto.request.create.CreateDestinationDTO;
import com.example.travellingapp.dto.request.update.UpdateDestinationDTO;
import com.example.travellingapp.response_template.CompleteResponse;

public interface DestinationService {

    CompleteResponse<Object> createDestination(Long tripId, CreateDestinationDTO destinationDTO);

    CompleteResponse<Object> getDestinationsByTrip(Long tripId);

    CompleteResponse<Object> getDestinationById(Long tripId, Long destinationId);

    CompleteResponse<Object> updateDestination(
            Long tripId,
            Long destinationId,
            UpdateDestinationDTO destinationDTO
    );

    CompleteResponse<Object> deleteDestination(Long tripId, Long destinationId);
}
