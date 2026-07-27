package com.example.travellingapp.validator;

import com.example.travellingapp.dto.request.OtpDTO;
import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.validator.CommonInputValidator.validateEmailForm;
import static com.example.travellingapp.validator.CommonInputValidator.validatePhoneForm;

@Component
@Log4j2
public class OtpValidator {

    public void validateOtpRequest(OtpDTO otpDTO) {
        if (otpDTO == null || otpDTO.getUserName() == null || otpDTO.getUserName().isBlank()) {
            log.error("Invalid OTP request.");
            throw new BusinessException(INVALID_INPUT, OTP.name());
        }

        if (otpDTO.getOtpVerificationMethod() == null || otpDTO.getOtpVerificationMethod().isBlank()) {
            log.error("OTP verification method is missing.");
            throw new BusinessException(OTP_METHOD_MISSING, OTP.name());
        }
    }

    public void validateEmailOtpRequest(OtpDTO otpDTO, Optional<ConfigurationEntity> emailPatternConfig) {
        if (otpDTO.getEmailEnum() == null) {
            log.error("Email enum is missing.");
            throw new BusinessException(EMAIL_ENUM_MISSING, OTP.name());
        }

        if (!validateEmailForm(otpDTO.getEmail(), emailPatternConfig)) {
            log.error("OTP email format is invalid!");
            throw new BusinessException(EMAIL_PATTERN_INVALID, OTP.name());
        }
    }

    public void validatePhoneOtpRequest(OtpDTO otpDTO, Optional<ConfigurationEntity> phonePatternConfig) {
        if (otpDTO.getSmsEnum() == null) {
            log.error("SMS enum is missing.");
            throw new BusinessException(SMS_ENUM_MISSING, OTP.name());
        }

        if (!validatePhoneForm(otpDTO.getPhoneNumber(), phonePatternConfig)) {
            log.error("Phone number is invalid!");
            throw new BusinessException(PHONE_FORMAT_INVALID, OTP.name());
        }
    }

    public void validateVerifyOtpRequest(OtpDTO otpDTO) {
        if (otpDTO == null
                || otpDTO.getUserName() == null
                || otpDTO.getUserName().isBlank()
                || otpDTO.getOtp() == null
                || otpDTO.getOtp().isBlank()) {
            log.error("There is no OTP for verification.");
            throw new BusinessException(OTP_BLOCKED_OR_NOT_FOUND, OTP.name());
        }
    }
}