package com.example.travellingapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreviewTripShareCodeRequest {

    @NotBlank
    @Size(max = 32)
    private String code;
}
