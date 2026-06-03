package com.example.travellingapp.controller;

import com.example.travellingapp.dto.request.create.CreateDestinationDTO;
import com.example.travellingapp.dto.request.update.UpdateDestinationDTO;
import com.example.travellingapp.response_template.ResponseBody;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
@RequestMapping("/api/v1/trips/{tripId}/destinations")
public interface DestinationController {

    @PostMapping
    ResponseEntity<ResponseBody<Object>> createDestination(
            @PathVariable Long tripId,
            @Valid @RequestBody CreateDestinationDTO destinationDTO
    );

    @GetMapping
    ResponseEntity<ResponseBody<Object>> getDestinationsByTrip(
            @PathVariable Long tripId
    );

    @GetMapping("/{destinationId}")
    ResponseEntity<ResponseBody<Object>> getDestinationById(
            @PathVariable Long tripId,
            @PathVariable Long destinationId
    );

    @PutMapping("/{destinationId}")
    ResponseEntity<ResponseBody<Object>> updateDestination(
            @PathVariable Long tripId,
            @PathVariable Long destinationId,
            @Valid @RequestBody UpdateDestinationDTO destinationDTO
    );

    @DeleteMapping("/{destinationId}")
    ResponseEntity<ResponseBody<Object>> deleteDestination(
            @PathVariable Long tripId,
            @PathVariable Long destinationId
    );
}