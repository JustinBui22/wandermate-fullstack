package com.example.travellingapp.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DestinationResponseDTO {

    private Long destinationId;

    private String destinationName;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer destinationOrder;

    private String notes;

    private Long tripId;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;
}
