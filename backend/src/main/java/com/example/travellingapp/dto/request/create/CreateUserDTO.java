package com.example.travellingapp.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class CreateUserDTO {
    @NotBlank(message = "Username cannot be null or empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Password cannot be null or empty")
    @Size(min = 8, max = 20, message = "Password must be between 3 and 20 characters")
    private String password;

    private String email;

    @Size(min = 9, max = 15, message = "Phone number must be between 9 and 15 characters")
    private String phoneNumber;

    @NotNull(message = "Date of birth cannot be null or empty")
    @Pattern(regexp = "^(0[1-9]|[12]\\d|3[01])/(0[1-9]|1[0-2])/\\d{4}$", message = "Invalid date format, must be DD/MM/YYYY")
    private String dob;

    private String referredCode;

    @NotNull(message = "OTP cannot be null or empty")
    private String otp;

    public CreateUserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

