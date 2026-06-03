package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.ActivityController;
import com.example.travellingapp.dto.request.create.CreateActivityDTO;
import com.example.travellingapp.dto.request.update.UpdateActivityDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActivityControllerImpl implements ActivityController {
    private final ActivityService activityService;

    public ActivityControllerImpl(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> createActivity(
            Long tripId,
            Long destinationId,
            CreateActivityDTO activityDTO
    ) {
        CompleteResponse<Object> response = activityService.createActivity(
                tripId,
                destinationId,
                activityDTO
        );

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getActivitiesByDestination(
            Long tripId,
            Long destinationId
    ) {
        CompleteResponse<Object> response = activityService.getActivitiesByDestination(
                tripId,
                destinationId
        );

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> getActivityById(
            Long tripId,
            Long destinationId,
            Long activityId
    ) {
        CompleteResponse<Object> response = activityService.getActivityById(
                tripId,
                destinationId,
                activityId
        );

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> updateActivity(
            Long tripId,
            Long destinationId,
            Long activityId,
            UpdateActivityDTO updateActivityDTO
    ) {
        CompleteResponse<Object> response = activityService.updateActivity(
                tripId,
                destinationId,
                activityId,
                updateActivityDTO
        );

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> deleteActivity(
            Long tripId,
            Long destinationId,
            Long activityId
    ) {
        CompleteResponse<Object> response = activityService.deleteActivity(
                tripId,
                destinationId,
                activityId
        );

        return new ResponseEntity<>(
                response.getResponseBody(),
                HttpStatus.valueOf(response.getHttpCode())
        );
    }
}