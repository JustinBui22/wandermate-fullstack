package com.example.travellingapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
public class ForgotPasswordDTO {
    @NotBlank(message = "Username cannot be null or empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "New password cannot be null or empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String newPassword;

    private String email;

    private String phoneNumber;

    @NotBlank(message = "OTP cannot be null or empty")
    private String otp;


    public ForgotPasswordDTO(String username, String newPassword) {
        this.username = username;
        this.newPassword = newPassword;
    }
}

