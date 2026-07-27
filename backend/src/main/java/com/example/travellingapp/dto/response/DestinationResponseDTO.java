package com.example.travellingapp.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class DestinationResponseDTO {

    private Long destinationId;

    private String destinationName;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer destinationOrder;

    private String notes;

    private Long tripId;

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
