package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.create.CreateUserDTO;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.validator.CommonInputValidator.*;

@Component
@RequiredArgsConstructor
@Log4j2
public class UserValidator {
    private final ConfigurationRepository configurationRepository;

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
            log.info("Password created is weak!");
            throw new BusinessException(PASSWORD_NOT_QUALIFIED, REGISTER.name());
        }
        // Check if the phone number has a correct format
        else if (!validatePhoneForm(registerRequest.getPhoneNumber(), configurationRepository.findByConfigCode(PHONE_VN_PATTERN.name()))) {
            log.info("Phone format is invalid");
            throw new BusinessException(PHONE_FORMAT_INVALID, REGISTER.name());
        }
    }
}