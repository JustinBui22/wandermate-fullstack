package com.example.travellingapp.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateActivityDTO {

    @NotBlank(message = "Activity name is required")
    private String activityName;

    private String location;

    private String description;

    @NotNull(message = "Activity start time is required")
    private LocalDateTime startDateTime;

    @NotNull(message = "Activity end time is required")
    private LocalDateTime endDateTime;
}

