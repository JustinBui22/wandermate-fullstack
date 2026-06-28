package com.example.travellingapp.dto.request.update;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileDTO {
    @Size(max = 60, message = "Display name must be at most 60 characters")
    private String displayName;

    @Size(min = 9, max = 15, message = "Phone number must be between 9 and 15 characters")
    private String phoneNumber;

    /**
     * Accepts either DD/MM/YYYY from the registration screen or ISO YYYY-MM-DD from native date inputs later.
     */
    private String dob;

    @Size(max = 500, message = "Profile image URL must be at most 500 characters")
    private String profileImageUrl;
}
