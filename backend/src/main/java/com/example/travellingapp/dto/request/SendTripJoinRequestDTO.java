package com.example.travellingapp.dto.request;

import com.example.travellingapp.enums.TripEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendTripJoinRequestDTO {
    private TripEnum role;
}
