package com.example.travellingapp.mapper;

import com.example.travellingapp.dto.response.ActivityResponseDTO;
import com.example.travellingapp.entity.ActivityEntity;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public ActivityResponseDTO toResponseDTO(ActivityEntity activity) {
        return new ActivityResponseDTO(
                activity.getActivityId(),
                activity.getDestination().getDestinationId(),
                activity.getDestination().getTrip().getTripId(),
                activity.getActivityName(),
                activity.getLocation(),
                activity.getDescription(),
                activity.getStartDateTime(),
                activity.getEndDateTime(),
                activity.getCreatedDate(),
                activity.getModifiedDate(),
                activity.getCreatedBy() == null ? null : activity.getCreatedBy().getUserId(),
                activity.getCreatedBy() == null ? null : activity.getCreatedBy().getUsername(),
                activity.getModifiedBy() == null ? null : activity.getModifiedBy().getUserId(),
                activity.getModifiedBy() == null ? null : activity.getModifiedBy().getUsername()
        );
    }
}