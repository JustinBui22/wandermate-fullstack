package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.TripMemberRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TripMemberResponseDTO {
    private Long tripMemberId;
    private Long tripId;
    private Long userId;
    private String username;
    private String email;
    private TripMemberRoleEnum role;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
