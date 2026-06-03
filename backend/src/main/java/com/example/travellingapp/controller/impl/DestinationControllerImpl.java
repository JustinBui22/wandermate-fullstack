package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.DestinationController;
import com.example.travellingapp.dto.request.create.CreateDestinationDTO;
import com.example.travellingapp.dto.request.update.UpdateDestinationDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.DestinationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DestinationControllerImpl implements DestinationController {

    private final DestinationService destinationService;

    public DestinationControllerImpl(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> createDestination(
            Long tripId,
            CreateDestinationDTO destinationDTO
    ) {
        CompleteResponse<Object> response = destinationService.createDestination(tripId, destinationDTO);

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getDestinationsByTrip(Long tripId) {
        CompleteResponse<Object> response = destinationService.getDestinationsByTrip(tripId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getDestinationById(Long tripId, Long destinationId) {
        CompleteResponse<Object> response = destinationService.getDestinationById(tripId, destinationId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> updateDestination(Long tripId, Long destinationId, UpdateDestinationDTO destinationDTO) {
        CompleteResponse<Object> response = destinationService.updateDestination(tripId, destinationId, destinationDTO);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> deleteDestination(Long tripId, Long destinationId) {
        CompleteResponse<Object> response = destinationService.deleteDestination(tripId, destinationId);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }
}