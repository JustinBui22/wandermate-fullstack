package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.TripEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripMemberResponseDTO {
    private Long tripMemberId;
    private Long tripId;
    private Long userId;
    private String username;
    private String email;
    private TripEnum role;
    private Instant createdDate;
    private Instant modifiedDate;
}
