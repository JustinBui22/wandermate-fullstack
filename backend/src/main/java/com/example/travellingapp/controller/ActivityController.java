package com.example.travellingapp.controller;

import com.example.travellingapp.dto.request.create.CreateActivityDTO;
import com.example.travellingapp.dto.request.update.UpdateActivityDTO;
import com.example.travellingapp.response_template.ResponseBody;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/trips/{tripId}/destinations/{destinationId}/activities")
public interface ActivityController {

    @PostMapping
    ResponseEntity<ResponseBody<Object>> createActivity(
            @PathVariable Long tripId,
            @PathVariable Long destinationId,
            @Valid @RequestBody CreateActivityDTO activityDTO
    );

    @GetMapping
    ResponseEntity<ResponseBody<Object>> getActivitiesByDestination(
            @PathVariable Long tripId,
            @PathVariable Long destinationId
    );

    @GetMapping("/{activityId}")
    ResponseEntity<ResponseBody<Object>> getActivityById(
            @PathVariable Long tripId,
            @PathVariable Long destinationId,
            @PathVariable Long activityId
    );

    @PutMapping("/{activityId}")
    ResponseEntity<ResponseBody<Object>> updateActivity(
            @PathVariable Long tripId,
            @PathVariable Long destinationId,
            @PathVariable Long activityId,
            @Valid @RequestBody UpdateActivityDTO updateActivityDTO
    );

    @DeleteMapping("/{activityId}")
    ResponseEntity<ResponseBody<Object>> deleteActivity(
            @PathVariable Long tripId,
            @PathVariable Long destinationId,
            @PathVariable Long activityId
    );
}