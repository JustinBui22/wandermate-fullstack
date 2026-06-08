package com.example.travellingapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginDTO {
    @NotBlank(message = "Username cannot be null or empty")
    private String username;

    @NotBlank(message = "Password cannot be null or empty")
    private String password;

    private boolean overrideMaxSession;

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
