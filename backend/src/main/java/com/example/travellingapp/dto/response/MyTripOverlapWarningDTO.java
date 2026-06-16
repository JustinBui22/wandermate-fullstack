package com.example.travellingapp.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MyTripOverlapWarningDTO {
    private Long currentTripId;
    private String currentTripName;
    private LocalDateTime currentTripStartDate;
    private LocalDateTime currentTripEndDate;

    private Long overlappingTripId;
    private String overlappingTripName;
    private LocalDateTime overlappingTripStartDate;
    private LocalDateTime overlappingTripEndDate;

    private LocalDateTime overlapStartDate;
    private LocalDateTime overlapEndDate;

    private String message;
}
