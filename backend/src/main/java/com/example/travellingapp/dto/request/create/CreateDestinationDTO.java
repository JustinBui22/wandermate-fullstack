package com.example.travellingapp.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateDestinationDTO {

    @NotBlank(message = "Destination name is required")
    private String destinationName;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Integer destinationOrder;

    private String notes;

    private Boolean allowOverlap;
}
