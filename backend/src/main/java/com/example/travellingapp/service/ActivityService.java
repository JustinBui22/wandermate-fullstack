package com.example.travellingapp.service;

import com.example.travellingapp.dto.request.create.CreateActivityDTO;
import com.example.travellingapp.dto.request.update.UpdateActivityDTO;
import com.example.travellingapp.response_template.CompleteResponse;

public interface ActivityService {

    CompleteResponse<Object> createActivity(
            Long tripId,
            Long destinationId,
            CreateActivityDTO activityDTO
    );

    CompleteResponse<Object> getActivitiesByDestination(
            Long tripId,
            Long destinationId
    );

    CompleteResponse<Object> getActivityById(
            Long tripId,
            Long destinationId,
            Long activityId
    );

    CompleteResponse<Object> updateActivity(
            Long tripId,
            Long destinationId,
            Long activityId,
            UpdateActivityDTO updateActivityDTO
    );

    CompleteResponse<Object> deleteActivity(
            Long tripId,
            Long destinationId,
            Long activityId
    );
}