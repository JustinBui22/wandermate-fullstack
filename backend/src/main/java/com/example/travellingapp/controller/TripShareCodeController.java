package com.example.travellingapp.controller;

import com.example.travellingapp.dto.request.create.GenerateTripShareCodeRequest;
import com.example.travellingapp.response_template.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/trips")
public interface TripShareCodeController {

    @PostMapping("/{tripId}/share-codes/regenerate")
    ResponseEntity<ResponseBody<Object>> regenerateShareCode(
            @PathVariable Long tripId,
            @RequestBody(required = false) GenerateTripShareCodeRequest request
    );

    @GetMapping("/share-codes/{code}")
    ResponseEntity<ResponseBody<Object>> previewShareCode(
            @PathVariable String code
    );

    @PostMapping("/share-codes/{code}/join-requests")
    ResponseEntity<ResponseBody<Object>> requestToJoinByShareCode(
            @PathVariable String code
    );

    // When owner leaves share-code screen and comes back, frontend can reload the current active code instead of losing it visually.
    @GetMapping("/{tripId}/share-codes/active")
    ResponseEntity<ResponseBody<Object>> getActiveShareCode(
            @PathVariable Long tripId
    );
}