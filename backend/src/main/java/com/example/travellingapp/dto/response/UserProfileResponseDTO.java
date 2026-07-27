package com.example.travellingapp.dto.response;

import com.example.travellingapp.enums.UserSettingEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Instant;

@Getter
@Setter
public class UserProfileResponseDTO {
    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private String phoneNumber;
    private LocalDate dob;
    private UserSettingEnum preferredTheme;
    private String profileImageUrl;
    private Instant createdDate;
    private Instant modifiedDate;
    private String profileImagePublicId;
}
