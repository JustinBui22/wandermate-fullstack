package com.example.travellingapp.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CreateActivityDTO {

    @NotBlank(message = "Activity name is required")
    private String activityName;

    private String location;

    private String description;

    @NotNull(message = "Activity start time is required")
    private LocalDateTime startDateTime;

    @NotNull(message = "Activity end time is required")
    private LocalDateTime endDateTime;
}
