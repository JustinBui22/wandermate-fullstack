package com.example.travellingapp.util;

import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.CommonEnum;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.HttpStatusCodeEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.repository.UserRepository;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.validator.CommonInputValidator.validateEmailForm;
import static com.example.travellingapp.validator.CommonInputValidator.validatePhoneForm;

@Log4j2
public class Common {
    private Common() {
    }

    public static String[] getNonAuthenticatedUrls(ConfigurationRepository configurationRepository) {
        Optional<ConfigurationEntity> nonAuthenRequestUrlOptional = configurationRepository.findByConfigCode(NON_AUTHENTICATED_REQUEST.name());
        if (nonAuthenRequestUrlOptional.isPresent()) {
            // Clean and split the configuration value to get individual URLs
            String[] nonAuthenticatedUrls = nonAuthenRequestUrlOptional.get().getConfigValue()
                    .replaceAll("[{}]", "") // Remove curly braces
                    .split(","); // Split by commas to get individual URLs
            // Clean up each URL in the array (remove newlines, trim spaces)
            return Arrays.stream(nonAuthenticatedUrls)
                    .map(url -> url.replace("\\n", "").trim()) // Remove newline characters and trim spaces
                    .toArray(String[]::new);
        } else {
            log.warn("There is no config for {}", NON_AUTHENTICATED_REQUEST);
            return new String[0];
        }
    }

    public static HttpStatusCodeEnum getHttpFromErrorCode(String errorCode) {
        if (errorCode.isEmpty()) {
            log.info("Error code is null, returning undefined HTTP status code.");
            return UNDEFINED_ERROR_CODE.getHttpStatusCodeEnum();
        }
        try {
            // Map the HTTP code using the ErrorCode object
            HttpStatusCodeEnum httpStatusCode = HttpStatusCodeEnum.resolve(ErrorCodeEnum.valueOf(errorCode).getHttpStatusCodeEnum().value);
            return Optional.ofNullable(httpStatusCode)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "No matching constant for [" + httpStatusCode + "]"
                            )
                    );
        } catch (IllegalArgumentException e) {
            // Log and return default if mapping fails
            log.error("There is an error extracting http code for {}",
                    errorCode);
            throw new BusinessException(UNDEFINED_HTTP_CODE, COMMON.name());
        }
    }

    public static String getErrorCode(ErrorCodeEntity errorCodeEntity) {
        if (errorCodeEntity == null) {
            log.info("Error code is null, returning undefined error code.");
            return UNDEFINED_ERROR_CODE.getCode();
        }
        try {
            return errorCodeEntity.getErrorCode();
        } catch (IllegalArgumentException e) {
            log.error("There is an error extracting error code for {}!",
                    errorCodeEntity.getErrorEnum());
            throw new BusinessException(UNDEFINED_ERROR_CODE, COMMON.name());
        }
    }

    public static String getErrorCodeMessage(ErrorCodeEntity errorCodeEntity) {
        if (errorCodeEntity == null) {
            log.info("Error code message is null, returning undefined error code message.");
            return UNDEFINED_ERROR_CODE.getMessage();
        }
        try {
            return errorCodeEntity.getErrorMessage();
        } catch (IllegalArgumentException e) {
            log.error("There is an error extracting error code message for {}",
                    errorCodeEntity.getErrorEnum());
            throw new BusinessException(UNDEFINED_ERROR_CODE, COMMON.name());
        }
    }

    public static String getConfigValue(CommonEnum commonEnum, ConfigurationRepository configurationRepository, String flow) {
        return configurationRepository.findByConfigCode(commonEnum.name())
                .map(ConfigurationEntity::getConfigValue)
                .orElseGet(() -> {
                    log.error("There is no config value for {}", commonEnum.name());
                    throw new BusinessException(CONFIG_NOT_FOUND, flow);
                });
    }

    public static String getConfigValue(String key, ConfigurationRepository configurationRepository, String defaultValue) {
        return configurationRepository.findByConfigCode(key)
                .map(ConfigurationEntity::getConfigValue)
                .orElseGet(() -> {
                    log.error("There is no config value for {} ---> Getting default value {}!", key, defaultValue);
                    return defaultValue;
                });
    }

    public static Optional<User> findUser(String username, ConfigurationRepository configurationRepository, UserRepository userRepository) {
        boolean isPhoneNumber = validatePhoneForm(username, configurationRepository.findByConfigCode(PHONE_VN_PATTERN.name()));
        boolean isEmail = validateEmailForm(username, configurationRepository.findByConfigCode(EMAIL_PATTERN.name()));

        // Retrieve the user based on username type (phone number or username or email)
        Optional<User> userOptional;
        if (isPhoneNumber) {
            userOptional = userRepository.findByPhoneNumberAndActive(username, true);
        } else if (isEmail) {
            userOptional = userRepository.findByEmailAndActive(username, true);
        } else {
            userOptional = userRepository.findByUsernameAndActive(username, true);
        }
        return userOptional;
    }

    public static String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    public static String getEmailConfig(String envKey, String dbConfigCode, String defaultValue, ConfigurationRepository configurationRepository) {
        String envValue = System.getenv(envKey);

        if (envValue != null
                && !envValue.isBlank()
                && !envValue.equalsIgnoreCase("replace_me")) {
            log.info("Using values from environment variable for {}.", dbConfigCode);
            return envValue;
        }
        log.info("Using values from database/default for {}.", dbConfigCode);
        return getConfigValue(dbConfigCode, configurationRepository, defaultValue);
    }
}
