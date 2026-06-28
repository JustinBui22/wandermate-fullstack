package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.create.CreateUserDTO;
import com.example.travellingapp.dto.request.update.UpdateUserProfileDTO;
import com.example.travellingapp.dto.request.update.UpdateUserSettingsDTO;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.repository.UserRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.util.DateTimeFormatter.toLocalDate;
import static com.example.travellingapp.validator.CommonInputValidator.*;

@Component
@RequiredArgsConstructor
@Log4j2
public class UserValidator {
    private final ConfigurationRepository configurationRepository;
    private final UserRepository userRepository;

    public void validateRegisterInput(CreateUserDTO registerRequest) {
        if (!validateUsername(registerRequest.getUsername(), configurationRepository.findByConfigCode(USERNAME_PATTERN.name()))) {
            log.info("Username format is invalid!");
            throw new BusinessException(USERNAME_FORMAT_INVALID, REGISTER.name());
        }
        // Check if email is inputted and has valid form and if taken
        else if (!validateEmailForm(registerRequest.getEmail(), configurationRepository.findByConfigCode(EMAIL_PATTERN.name()))) {
            log.info("Email format is invalid");
            throw new BusinessException(EMAIL_PATTERN_INVALID, REGISTER.name());
        }
        // Check if the password meets the security requirement
        else if (!validatePassword(registerRequest.getPassword(), configurationRepository.findByConfigCode(PASSWORD_PATTERN.name()))) {
            log.error("Password created is weak!");
            throw new BusinessException(PASSWORD_NOT_QUALIFIED, REGISTER.name());
        }
        // Check if the phone number has a correct format
        else if (!StringUtils.isEmpty(registerRequest.getPhoneNumber()) && !validatePhoneForm(registerRequest.getPhoneNumber(), configurationRepository.findByConfigCode(PHONE_VN_PATTERN.name()))) {
            log.error("Phone format is invalid");
            throw new BusinessException(PHONE_FORMAT_INVALID, REGISTER.name());
        }
    }

    public LocalDate validateUpdateProfileInput(UpdateUserProfileDTO updateUserProfileDTO, User currentUser) {
        if (updateUserProfileDTO == null || currentUser == null) {
            log.error("Invalid input to update user profile!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        String phoneNumber = trimToNull(updateUserProfileDTO.getPhoneNumber());
        if (phoneNumber != null) {
            validateProfilePhoneNumber(phoneNumber, currentUser);
        }

        String dob = trimToNull(updateUserProfileDTO.getDob());
        if (dob == null) {
            return null;
        }

        LocalDate parsedDob = parseProfileDob(dob);

        if (parsedDob.isAfter(LocalDate.now())) {
            log.error("Date of birth cannot be in the future!");
            throw new BusinessException(DOB_IN_FUTURE, REGISTER.name());
        }

        return parsedDob;
    }

    public void validateUpdateSettingsInput(UpdateUserSettingsDTO updateUserSettingsDTO) {
        if (updateUserSettingsDTO == null || updateUserSettingsDTO.getPreferredTheme() == null) {
            log.error("Invalid input to update user settings!");
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }
    }

    private void validateProfilePhoneNumber(String phoneNumber, User currentUser) {
        if (!validatePhoneForm(phoneNumber, configurationRepository.findByConfigCode(PHONE_VN_PATTERN.name()))) {
            log.error("Phone format is invalid to update profile!");
            throw new BusinessException(PHONE_FORMAT_INVALID, REGISTER.name());
        }

        Optional<User> existingPhoneUser = userRepository.findByPhoneNumberAndActive(phoneNumber, true);

        if (existingPhoneUser.isPresent() && existingPhoneUser.get().getUserId() != currentUser.getUserId()) {
            log.error("Phone number is already taken!");
            throw new BusinessException(PHONE_NUMBER_TAKEN, REGISTER.name());
        }
    }

    private LocalDate parseProfileDob(String dob) {
        try {
            if (dob.contains("-")) {
                return LocalDate.parse(dob);
            }
            return toLocalDate(dob);
        } catch (Exception e) {
            log.error("DOB format is invalid!", e);
            throw new BusinessException(INVALID_INPUT, REGISTER.name());
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}