package com.example.travellingapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ActivityResponseDTO {
    private Long activityId;
    private Long destinationId;
    private Long tripId;
    private String activityName;
    private String location;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Instant createdDate;
    private Instant modifiedDate;

    private Long createdByUserId;
    private String createdByUsername;
    private String createdByDisplayName;
    private String createdByProfileImageUrl;

    private Long modifiedByUserId;
    private String modifiedByUsername;
    private String modifiedByDisplayName;
    private String modifiedByProfileImageUrl;
}
