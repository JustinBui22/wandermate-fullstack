package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.ForgotPasswordDTO;
import com.example.travellingapp.dto.request.LoginDTO;
import com.example.travellingapp.dto.request.OtpDTO;
import com.example.travellingapp.dto.request.update.UpdateUserProfileDTO;
import com.example.travellingapp.dto.request.update.UpdateUserSettingsDTO;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.UserMapper;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.security.AccountEnumerationRateLimiter;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.CloudinaryImageClient;
import com.example.travellingapp.service.TokenService;
import com.example.travellingapp.service.UserService;
import com.example.travellingapp.dto.request.create.CreateUserDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.validator.UserValidator;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import com.example.travellingapp.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;
import static com.example.travellingapp.util.Common.findUser;
import static com.example.travellingapp.util.DateTimeFormatter.toLocalDate;
import static com.example.travellingapp.validator.CommonInputValidator.validatePassword;

@Service
@Log4j2
public class UserServiceImpl implements UserService {
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private final UserRepository userRepository;
    private final ConfigurationRepository configurationRepository;
    private final ErrorCodeRepository errorCodeRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final OtpServiceImpl otpServiceImpl;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final CloudinaryImageClient cloudinaryImageClient;
    private final AccountEnumerationRateLimiter accountEnumerationRateLimiter;


    public UserServiceImpl(UserRepository userRepository, ConfigurationRepository configurationRepository, ErrorCodeRepository errorCodeRepository, TokenService tokenService, PasswordEncoder passwordEncoder, OtpServiceImpl otpServiceImpl, AuthenticatedUserProvider authenticatedUserProvider, UserValidator userValidator, UserMapper userMapper, CloudinaryImageClient cloudinaryImageClient, AccountEnumerationRateLimiter accountEnumerationRateLimiter) {
        this.userRepository = userRepository;
        this.configurationRepository = configurationRepository;
        this.errorCodeRepository = errorCodeRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.otpServiceImpl = otpServiceImpl;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.userValidator = userValidator;
        this.userMapper = userMapper;
        this.cloudinaryImageClient = cloudinaryImageClient;
        this.accountEnumerationRateLimiter = accountEnumerationRateLimiter;
    }

    @Transactional
    @Override
    public CompleteResponse<Object> createNewUser(CreateUserDTO registerRequest) {
        ErrorCodeEnum errorCodeEnum;
        try {
            // Validate the format of username, email, password and phone number
            userValidator.validateRegisterInput(registerRequest);

            Optional<User> userOptional = userRepository.findByUsernameAndActive(registerRequest.getUsername());
            // Check if username is taken
            if (userOptional.isPresent()) {
                log.info("Username is not available!");
                throw new BusinessException(USERNAME_TAKEN, REGISTER.name());
            }
            // Check if email is inputted and has valid form and if taken
            else if (userRepository.findByEmailAndActive(registerRequest.getEmail(), true).isPresent()) {
                log.info("Email is taken for registration!");
                throw new BusinessException(EMAIL_TAKEN, REGISTER.name());
            } else if (!StringUtils.isEmpty(registerRequest.getPhoneNumber())
                    && userRepository.findByPhoneNumberAndActive(registerRequest.getPhoneNumber(), true).isPresent()) {
                log.info("Phone number is taken for registration!");
                throw new BusinessException(PHONE_NUMBER_TAKEN, REGISTER.name());
            }
            // Check if the OTP is empty
            else if (StringUtils.isEmpty(registerRequest.getOtp())) {
                log.info("OTP is empty/ invalid!");
                throw new BusinessException(OTP_BLOCKED_OR_NOT_FOUND, REGISTER.name());
            } else if (toLocalDate(registerRequest.getDob()).isAfter(LocalDate.now())) {
                log.error("Date of birth cannot be in the future!");
                throw new BusinessException(DOB_IN_FUTURE, REGISTER.name());
            }
            // Check if OTP code is verified
            OtpDTO verifyOtpDTO = new OtpDTO(registerRequest.getUsername(), registerRequest.getOtp());
            verifyOtpDTO.setEmail(registerRequest.getEmail());
            verifyOtpDTO.setPhoneNumber(registerRequest.getPhoneNumber());
            String verifyOtpErrorCode = otpServiceImpl.verifyOtp(verifyOtpDTO).getResponseBody().getCode();

            if (verifyOtpErrorCode.equals(OTP_VERIFICATION_SUCCESS.getCode())) {
                User newUser = new User(registerRequest.getUsername(), passwordEncoder.encode(registerRequest.getPassword()), registerRequest.getPhoneNumber(), toLocalDate(registerRequest.getDob()), Instant.now(), registerRequest.getEmail(), true);
                userRepository.save(newUser);
                log.info("User has been created!");
                errorCodeEnum = USER_CREATED;
            } else {
                log.error("OTP verification failed!");
                throw new BusinessException(OTP_VERIFICATION_FAIL, REGISTER.name());
            }
            return getCompleteResponse(errorCodeRepository, errorCodeEnum, REGISTER.name(), null);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in registering a new user!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, REGISTER.name());
        }
    }

    public CompleteResponse<Object> checkUserDetails(CreateUserDTO registerRequest) {
        try {
            // Validate the format of username, email, password and phone number
            userValidator.validateRegisterInput(registerRequest);
            Optional<User> userOptional = userRepository.findByUsernameAndActive(registerRequest.getUsername());
            // Check if username is taken
            if (userOptional.isPresent()) {
                log.info("Username is already taken!");
                throw new BusinessException(USERNAME_TAKEN, REGISTER.name());
            } else if (userRepository.findByEmailAndActive(registerRequest.getEmail(), true).isPresent()) {
                log.info("Email is already taken!");
                throw new BusinessException(EMAIL_TAKEN, REGISTER.name());
            } else if (!StringUtils.isEmpty(registerRequest.getPhoneNumber())
                    && userRepository.findByPhoneNumberAndActive(registerRequest.getPhoneNumber(), true).isPresent()) {
                log.info("Phone number is already taken!");
                throw new BusinessException(PHONE_NUMBER_TAKEN, REGISTER.name());
            }
            return getCompleteResponse(errorCodeRepository, USER_DETAILS_VERIFIED, REGISTER.name(), null);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in verifying new user's input!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, REGISTER.name());
        }
    }

    @Transactional
    @Override
    public CompleteResponse<Object> forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        // Check if the new password matches the required pattern
        if (!validatePassword(forgotPasswordDTO.getNewPassword(), configurationRepository.findByConfigCode(PASSWORD_PATTERN.name()))) {
            log.info("New password is weak!");
            throw new BusinessException(PASSWORD_NOT_QUALIFIED, FORGOT_PASSWORD.name());
        }

        //Check if email/phone existed
        String username = forgotPasswordDTO.getUsername();
        Optional<User> userOptional = findUser(username, configurationRepository, userRepository);
        if (userOptional.isEmpty()) {
            passwordEncoder.matches(forgotPasswordDTO.getNewPassword(), DUMMY_PASSWORD_HASH);
            log.error("Account not found to reset password!");
            throw new BusinessException(OTP_VERIFICATION_FAIL, FORGOT_PASSWORD.name());
        }
        User user = userOptional.get();
        // If user entered email/phone, convert it back to real username.
        username = user.getUsername();

        //Verify otp
        OtpDTO verifyOtpDTO = new OtpDTO(username, forgotPasswordDTO.getOtp());
        verifyOtpDTO.setEmail(forgotPasswordDTO.getEmail());
        verifyOtpDTO.setPhoneNumber(forgotPasswordDTO.getPhoneNumber());
        String verifyOtpErrorCode;
        try {
            verifyOtpErrorCode = otpServiceImpl.verifyOtp(verifyOtpDTO).getResponseBody().getCode();
        } catch (BusinessException e) {
            log.error("Update new password failed!");
            throw new BusinessException(OTP_VERIFICATION_FAIL, FORGOT_PASSWORD.name());
        }

        if (!verifyOtpErrorCode.equals(OTP_VERIFICATION_SUCCESS.getCode())) {
            log.error("Update new password failed!");
            throw new BusinessException(OTP_VERIFICATION_FAIL, FORGOT_PASSWORD.name());
        }

        // Check if the new password is the same as the old password
        if (passwordEncoder.matches(forgotPasswordDTO.getNewPassword(), user.getPassword())) {
            log.error("New password cannot be the same as the old password!");
            throw new BusinessException(NEW_PASSWORD_SAME_AS_OLD, FORGOT_PASSWORD.name());
        }

        user.setPassword(passwordEncoder.encode(forgotPasswordDTO.getNewPassword()));
        userRepository.save(user);
        log.info("User password has been updated!");

        // Revoke all active refresh tokens for the user to ensure they cannot use old tokens after changing their password
        tokenService.revokeAllActiveRefreshTokensForUser(username);
        return getCompleteResponse(errorCodeRepository, PASSWORD_UPDATED_SUCCESS, FORGOT_PASSWORD.name(), null);
    }

    @Override
    public CompleteResponse<Object> checkUserExisted(String userInput) {
        String requesterUsername = authenticatedUserProvider.getUsername();
        accountEnumerationRateLimiter.checkAuthenticatedLookupAllowed(requesterUsername);

        String normalizedUserInput = userInput == null ? "" : userInput.trim();
        if (normalizedUserInput.isBlank() || normalizedUserInput.length() > 254) {
            throw new BusinessException(INVALID_INPUT, COMMON.name());
        }

        boolean userExists = findUser(normalizedUserInput, configurationRepository, userRepository).isPresent();
        log.info("Authenticated account lookup completed.");
        return getCompleteResponse(
                errorCodeRepository,
                SEARCH_INFO_SUCCESS,
                COMMON.name(),
                Map.of("exists", userExists)
        );
    }

    @Transactional
    @SuppressWarnings("unchecked")
    @Override
    public CompleteResponse<Object> login(LoginDTO loginRequest) {
        String username = loginRequest.getUsername();
        ErrorCodeEnum errorCodeEnum;
        try {
            Optional<User> userOptional = findUser(username, configurationRepository, userRepository);
            if (userOptional.isEmpty()) {
                passwordEncoder.matches(loginRequest.getPassword(), DUMMY_PASSWORD_HASH);
                log.error("Login failed because the account was not found.");
                return getCompleteResponse(errorCodeRepository, PASSWORD_NOT_CORRECT, LOGIN.name(), null);
            }
            User user = userOptional.get();
            username = user.getUsername();
            // check if password matches and display corresponding error code.
            boolean isPasswordCorrect = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
            errorCodeEnum = isPasswordCorrect ? LOGIN_SUCCESS : PASSWORD_NOT_CORRECT;
            log.info(isPasswordCorrect ? "User logged in successfully!" : "Password incorrect!");
            if (isPasswordCorrect) {
                // Check if the user has exceeded maxed number of active sessions
                tokenService.checkMaxActiveSessions(username, loginRequest.isOverrideMaxSession());
                // Create an authentication object from the user
                // Generate and return the session, access and refresh token
                String sessionId = UUID.randomUUID().toString();
                String accessToken = tokenService.generateAccessToken(username, sessionId).getResponseBody().getBody().toString();
                String sessionToken = tokenService.generateSessionToken(username, sessionId).getResponseBody().getBody().toString();

                Map<String, Object> refreshTokenMap = (Map<String, Object>) tokenService.generateRefreshToken(username, sessionId).getResponseBody().getBody();
                String refreshToken = refreshTokenMap.get("refreshToken").toString();
                Map<String, String> tokenMap = new HashMap<>();
                tokenMap.put("accessToken", accessToken);
                tokenMap.put("refreshToken", refreshToken);
                tokenMap.put("sessionToken", sessionToken);
                return getCompleteResponse(errorCodeRepository, errorCodeEnum, LOGIN.name(), tokenMap);
            }
            return getCompleteResponse(errorCodeRepository, errorCodeEnum, LOGIN.name(), null);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in logging in: {}", e.getClass().getSimpleName());
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Transactional
    @Override
    public CompleteResponse<Object> logout(String sessionToken) {
        String username = authenticatedUserProvider.getUsername();
        String sessionId = authenticatedUserProvider.getSessionId();
        try {
            // Revoke session token
            if (sessionToken == null || sessionToken.isBlank()) {
                throw new BusinessException(SESSION_TOKEN_INVALID, TOKEN.name());
            }
            tokenService.revokeSessionTokenBySessionId(username, sessionId, sessionToken);
            // Revoke refresh token
            tokenService.revokeActiveRefreshTokensBySessionId(sessionId);
            // Clear security context
            SecurityContextHolder.clearContext();
            log.info("User logged out successfully!");
            return getCompleteResponse(errorCodeRepository, LOGOUT_SUCCESS, LOGOUT.name(), null);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in logging out: {}", e.getClass().getSimpleName());
            throw new BusinessException(INTERNAL_SERVER_ERROR, LOGOUT.name());
        }
    }

    @Override
    public CompleteResponse<Object> getMyProfile() {
        try {
            User user = getCurrentActiveUser();

            return getCompleteResponse(
                    errorCodeRepository,
                    SEARCH_INFO_SUCCESS,
                    COMMON.name(),
                    userMapper.toProfileResponseDTO(user)
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in retrieving the current user's profile!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Transactional
    @Override
    public CompleteResponse<Object> updateMyProfile(UpdateUserProfileDTO updateUserProfileDTO) {
        try {
            User user = getCurrentActiveUser();

            LocalDate parsedDob = userValidator.validateUpdateProfileInput(updateUserProfileDTO, user);

            String oldProfileImagePublicId = user.getProfileImagePublicId();

            userMapper.updateProfileEntity(user, updateUserProfileDTO, parsedDob);
            user.setModifiedDate(Instant.now());
            User savedUser = userRepository.save(user);

            // Delete the old profile image from Cloudinary if it has changed
            cloudinaryImageClient.deleteOldCloudinaryImageIfChanged(
                    oldProfileImagePublicId,
                    savedUser.getProfileImagePublicId(),
                    "profile image"
            );

            return getCompleteResponse(
                    errorCodeRepository,
                    SEARCH_INFO_SUCCESS,
                    COMMON.name(),
                    userMapper.toProfileResponseDTO(savedUser)
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in updating the current user's profile!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    @Transactional
    @Override
    public CompleteResponse<Object> updateMySettings(UpdateUserSettingsDTO updateUserSettingsDTO) {
        try {
            User user = getCurrentActiveUser();

            userValidator.validateUpdateSettingsInput(updateUserSettingsDTO);

            userMapper.updateSettingsEntity(user, updateUserSettingsDTO);
            user.setModifiedDate(Instant.now());

            User savedUser = userRepository.save(user);

            return getCompleteResponse(
                    errorCodeRepository,
                    SEARCH_INFO_SUCCESS,
                    COMMON.name(),
                    userMapper.toProfileResponseDTO(savedUser)
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in updating the current user's settings!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    private User getCurrentActiveUser() {
        String username = authenticatedUserProvider.getUsername();
        return userRepository.findByUsernameAndActive(username)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, COMMON.name()));
    }
}