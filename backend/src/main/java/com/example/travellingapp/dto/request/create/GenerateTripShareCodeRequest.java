package com.example.travellingapp.dto.request.create;

import com.example.travellingapp.enums.TripEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateTripShareCodeRequest {
    private TripEnum defaultRole;
}
