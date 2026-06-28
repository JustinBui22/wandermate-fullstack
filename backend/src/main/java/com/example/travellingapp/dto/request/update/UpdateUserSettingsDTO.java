package com.example.travellingapp.dto.request.update;

import com.example.travellingapp.enums.UserSettingEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserSettingsDTO {
    @NotNull(message = "Preferred theme is required")
    private UserSettingEnum preferredTheme;
}
