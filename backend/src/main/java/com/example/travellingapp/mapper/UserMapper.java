package com.example.travellingapp.mapper;

import com.example.travellingapp.dto.request.update.UpdateUserProfileDTO;
import com.example.travellingapp.dto.request.update.UpdateUserSettingsDTO;
import com.example.travellingapp.dto.response.UserProfileResponseDTO;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.UserSettingEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserMapper {

    public UserProfileResponseDTO toProfileResponseDTO(User user) {
        UserProfileResponseDTO response = new UserProfileResponseDTO();

        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());

        response.setDisplayName(
                user.getDisplayName() == null || user.getDisplayName().isBlank()
                        ? user.getUsername()
                        : user.getDisplayName()
        );

        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setDob(user.getDob());

        response.setPreferredTheme(
                user.getPreferredTheme() == null
                        ? UserSettingEnum.SYSTEM
                        : user.getPreferredTheme()
        );

        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setCreatedDate(user.getCreatedDate());
        response.setModifiedDate(user.getModifiedDate());

        return response;
    }

    public void updateProfileEntity(
            User user,
            UpdateUserProfileDTO updateUserProfileDTO,
            LocalDate parsedDob
    ) {
        String displayName = trimToNull(updateUserProfileDTO.getDisplayName());
        if (displayName != null) {
            user.setDisplayName(displayName);
        }

        String phoneNumber = trimToNull(updateUserProfileDTO.getPhoneNumber());
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }

        if (parsedDob != null) {
            user.setDob(parsedDob);
        }
        if (updateUserProfileDTO.getProfileImageUrl() != null) {
            user.setProfileImageUrl(trimToNull(updateUserProfileDTO.getProfileImageUrl()));
        }
    }

    public void updateSettingsEntity(
            User user,
            UpdateUserSettingsDTO updateUserSettingsDTO
    ) {
        user.setPreferredTheme(updateUserSettingsDTO.getPreferredTheme());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}