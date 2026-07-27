package com.example.travellingapp.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MyTripOverlapWarningDTO {
    private Long currentTripId;
    private String currentTripName;
    private LocalDate currentTripStartDate;
    private LocalDate currentTripEndDate;

    private Long overlappingTripId;
    private String overlappingTripName;
    private LocalDate overlappingTripStartDate;
    private LocalDate overlappingTripEndDate;

    private LocalDate overlapStartDate;
    private LocalDate overlapEndDate;

    private String message;
}
