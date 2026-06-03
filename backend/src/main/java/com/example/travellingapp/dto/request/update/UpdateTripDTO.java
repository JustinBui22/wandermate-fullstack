package com.example.travellingapp.dto.request.update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class UpdateTripDTO {

    @NotBlank(message = "Trip name is required")
    @Size(min = 3, max = 50, message = "Trip name must be between 3 and 50 characters")
    private String tripName;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Boolean allowOverlap;
}

