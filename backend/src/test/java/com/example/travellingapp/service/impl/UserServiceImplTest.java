package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.ForgotPasswordDTO;
import com.example.travellingapp.dto.request.LoginDTO;
import com.example.travellingapp.dto.request.OtpDTO;
import com.example.travellingapp.dto.request.create.CreateUserDTO;
import com.example.travellingapp.dto.request.update.UpdateUserProfileDTO;
import com.example.travellingapp.dto.request.update.UpdateUserSettingsDTO;
import com.example.travellingapp.dto.response.UserProfileResponseDTO;
import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.UserSettingEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.mapper.UserMapper;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.security.AccountEnumerationRateLimiter;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.CloudinaryImageClient;
import com.example.travellingapp.service.TokenService;
import com.example.travellingapp.util.Common;
import com.example.travellingapp.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpServiceImpl otpServiceImpl;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private UserValidator userValidator;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CloudinaryImageClient cloudinaryImageClient;

    @Mock
    private AccountEnumerationRateLimiter accountEnumerationRateLimiter;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                configurationRepository,
                errorCodeRepository,
                tokenService,
                passwordEncoder,
                otpServiceImpl,
                authenticatedUserProvider,
                userValidator,
                userMapper,
                cloudinaryImageClient,
                accountEnumerationRateLimiter
        );
    }

    // -------------------------------------------------------------------------
    // checkUserExisted()
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void checkUserExisted_shouldReturnGenericExistsTrue_withoutReturningCanonicalUsername() {
        String requesterUsername = "Requester123";
        String userInput = "justin@example.com";

        when(authenticatedUserProvider.getUsername()).thenReturn(requesterUsername);
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            userInput,
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.of(activeUser("JustinBo123")));

            CompleteResponse<Object> response = userService.checkUserExisted(userInput);

            Map<String, Boolean> body =
                    (Map<String, Boolean>) response.getResponseBody().getBody();

            assertThat(response.getResponseBody().getCode())
                    .isEqualTo(SEARCH_INFO_SUCCESS.getCode());
            assertThat(body).containsExactly(Map.entry("exists", true));
            assertThat(response.getResponseBody().getBody().toString())
                    .doesNotContain("JustinBo123");

            verify(accountEnumerationRateLimiter).checkAuthenticatedLookupAllowed(requesterUsername);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkUserExisted_shouldReturnSameSuccessEnvelope_whenUserDoesNotExist() {
        String requesterUsername = "Requester123";
        String userInput = "missing@example.com";

        when(authenticatedUserProvider.getUsername()).thenReturn(requesterUsername);
        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            userInput,
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.empty());

            CompleteResponse<Object> response = userService.checkUserExisted(userInput);

            Map<String, Boolean> body =
                    (Map<String, Boolean>) response.getResponseBody().getBody();

            assertThat(response.getResponseBody().getCode())
                    .isEqualTo(SEARCH_INFO_SUCCESS.getCode());
            assertThat(body).containsExactly(Map.entry("exists", false));

            verify(accountEnumerationRateLimiter).checkAuthenticatedLookupAllowed(requesterUsername);
        }
    }

    @Test
    void checkUserExisted_shouldRejectBlankInput_afterApplyingRateLimit() {
        String requesterUsername = "Requester123";

        when(authenticatedUserProvider.getUsername()).thenReturn(requesterUsername);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.checkUserExisted("   ")
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(accountEnumerationRateLimiter)
                .checkAuthenticatedLookupAllowed(requesterUsername);
        verifyNoInteractions(configurationRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void checkUserExisted_shouldStopBeforeLookup_whenRateLimitIsExceeded() {
        String requesterUsername = "Requester123";
        String userInput = "justin@example.com";

        when(authenticatedUserProvider.getUsername()).thenReturn(requesterUsername);
        doThrow(new BusinessException(ACCOUNT_ENUMERATION_RATE_LIMITED, COMMON.name()))
                .when(accountEnumerationRateLimiter)
                .checkAuthenticatedLookupAllowed(requesterUsername);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.checkUserExisted(userInput)
        );

        assertBusinessException(
                exception,
                ACCOUNT_ENUMERATION_RATE_LIMITED,
                COMMON.name()
        );

        verifyNoInteractions(configurationRepository);
        verifyNoInteractions(userRepository);
    }

    // -------------------------------------------------------------------------
    // checkUserDetails()
    // -------------------------------------------------------------------------

    @Test
    void checkUserDetails_shouldReturnSuccess_whenRegisterInputIsValidAndPhoneNumberIsMissing() {
        CreateUserDTO request = validRegisterRequest();
        request.setPhoneNumber(null);
        request.setOtp("");

        mockErrorCode(USER_DETAILS_VERIFIED, REGISTER.name());

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(request.getEmail(), true))
                .thenReturn(Optional.empty());

        CompleteResponse<Object> response = userService.checkUserDetails(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(USER_DETAILS_VERIFIED.getCode());

        verify(userValidator).validateRegisterInput(request);
        verify(userRepository).findByUsernameAndActive(request.getUsername());
        verify(userRepository).findByEmailAndActive(request.getEmail(), true);
        verify(userRepository, never()).findByPhoneNumberAndActive(anyString(), anyBoolean());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void checkUserDetails_shouldThrowUsernameTaken_whenUsernameAlreadyExists() {
        CreateUserDTO request = validRegisterRequest();

        User existingUser = activeUser(request.getUsername());
        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.of(existingUser));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.checkUserDetails(request)
        );

        assertBusinessException(exception, USERNAME_TAKEN, REGISTER.name());

        verify(userValidator).validateRegisterInput(request);
        verify(userRepository, never()).findByEmailAndActive(anyString(), anyBoolean());
        verify(userRepository, never()).findByPhoneNumberAndActive(anyString(), anyBoolean());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void checkUserDetails_shouldThrowEmailTaken_whenEmailAlreadyExists() {
        CreateUserDTO request = validRegisterRequest();

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(request.getEmail(), true))
                .thenReturn(Optional.of(activeUser("OtherUser")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.checkUserDetails(request)
        );

        assertBusinessException(exception, EMAIL_TAKEN, REGISTER.name());

        verify(userValidator).validateRegisterInput(request);
        verify(userRepository, never()).findByPhoneNumberAndActive(anyString(), anyBoolean());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void checkUserDetails_shouldThrowPhoneNumberTaken_whenPhoneNumberIsProvidedAndAlreadyExists() {
        CreateUserDTO request = validRegisterRequest();
        request.setPhoneNumber("0412345678");

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(request.getEmail(), true))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberAndActive(request.getPhoneNumber(), true))
                .thenReturn(Optional.of(activeUser("OtherUser")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.checkUserDetails(request)
        );

        assertBusinessException(exception, PHONE_NUMBER_TAKEN, REGISTER.name());

        verify(userValidator).validateRegisterInput(request);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void checkUserDetails_shouldRethrowBusinessExceptionFromValidator_whenPasswordIsWeak() {
        CreateUserDTO request = validRegisterRequest();

        doThrow(new BusinessException(PASSWORD_NOT_QUALIFIED, REGISTER.name()))
                .when(userValidator)
                .validateRegisterInput(request);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.checkUserDetails(request)
        );

        assertBusinessException(exception, PASSWORD_NOT_QUALIFIED, REGISTER.name());

        verify(userRepository, never()).findByUsernameAndActive(anyString());
        verify(userRepository, never()).findByEmailAndActive(anyString(), anyBoolean());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void checkUserDetails_shouldWrapUnexpectedExceptionAsInternalServerError() {
        CreateUserDTO request = validRegisterRequest();

        doThrow(new RuntimeException("Unexpected validator failure"))
                .when(userValidator)
                .validateRegisterInput(request);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.checkUserDetails(request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, REGISTER.name());

        verify(userRepository, never()).save(any(User.class));
    }

    // -------------------------------------------------------------------------
    // createNewUser()
    // -------------------------------------------------------------------------

    @Test
    void createNewUser_shouldRethrowBusinessExceptionFromValidator_whenPasswordIsWeak() {
        CreateUserDTO request = validRegisterRequest();

        doThrow(new BusinessException(PASSWORD_NOT_QUALIFIED, REGISTER.name()))
                .when(userValidator)
                .validateRegisterInput(request);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createNewUser(request)
        );

        assertBusinessException(exception, PASSWORD_NOT_QUALIFIED, REGISTER.name());

        verify(userValidator).validateRegisterInput(request);
        verify(userRepository, never()).findByUsernameAndActive(anyString());
        verify(userRepository, never()).findByEmailAndActive(anyString(), anyBoolean());
        verify(userRepository, never()).findByPhoneNumberAndActive(anyString(), anyBoolean());
        verify(otpServiceImpl, never()).verifyOtp(any(OtpDTO.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createNewUser_shouldCreateUser_whenOtpIsValidAndPhoneNumberIsMissing() {
        CreateUserDTO request = validRegisterRequest();
        request.setPhoneNumber(null);

        mockErrorCode(USER_CREATED, REGISTER.name());

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(request.getEmail(), true))
                .thenReturn(Optional.empty());
        when(otpServiceImpl.verifyOtp(any(OtpDTO.class)))
                .thenReturn(response(OTP_VERIFICATION_SUCCESS, OTP.name(), null));
        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");

        CompleteResponse<Object> response = userService.createNewUser(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(USER_CREATED.getCode());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(request.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(savedUser.getPhoneNumber()).isNull();
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.isActive()).isTrue();

        ArgumentCaptor<OtpDTO> otpCaptor = ArgumentCaptor.forClass(OtpDTO.class);
        verify(otpServiceImpl).verifyOtp(otpCaptor.capture());

        OtpDTO verifyOtpDTO = otpCaptor.getValue();
        assertThat(verifyOtpDTO.getUserName()).isEqualTo(request.getUsername());
        assertThat(verifyOtpDTO.getOtp()).isEqualTo(request.getOtp());
        assertThat(verifyOtpDTO.getEmail()).isEqualTo(request.getEmail());
        assertThat(verifyOtpDTO.getPhoneNumber()).isNull();

        verify(userRepository, never()).findByPhoneNumberAndActive(anyString(), anyBoolean());
    }

    @Test
    void createNewUser_shouldThrowUsernameTaken_whenUsernameAlreadyExists() {
        CreateUserDTO request = validRegisterRequest();

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.of(activeUser(request.getUsername())));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createNewUser(request)
        );

        assertBusinessException(exception, USERNAME_TAKEN, REGISTER.name());

        verify(userRepository, never()).findByEmailAndActive(anyString(), anyBoolean());
        verify(otpServiceImpl, never()).verifyOtp(any(OtpDTO.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createNewUser_shouldThrowEmailTaken_whenEmailAlreadyExists() {
        CreateUserDTO request = validRegisterRequest();

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(request.getEmail(), true))
                .thenReturn(Optional.of(activeUser("OtherUser")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createNewUser(request)
        );

        assertBusinessException(exception, EMAIL_TAKEN, REGISTER.name());

        verify(otpServiceImpl, never()).verifyOtp(any(OtpDTO.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createNewUser_shouldThrowPhoneNumberTaken_whenPhoneNumberIsProvidedAndAlreadyExists() {
        CreateUserDTO request = validRegisterRequest();
        request.setPhoneNumber("0412345678");

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(request.getEmail(), true))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberAndActive(request.getPhoneNumber(), true))
                .thenReturn(Optional.of(activeUser("OtherUser")));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createNewUser(request)
        );

        assertBusinessException(exception, PHONE_NUMBER_TAKEN, REGISTER.name());

        verify(otpServiceImpl, never()).verifyOtp(any(OtpDTO.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createNewUser_shouldThrowOtpBlockedOrNotFound_whenOtpIsMissing() {
        CreateUserDTO request = validRegisterRequest();
        request.setOtp("");

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(request.getEmail(), true))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createNewUser(request)
        );

        assertBusinessException(exception, OTP_BLOCKED_OR_NOT_FOUND, REGISTER.name());

        verify(otpServiceImpl, never()).verifyOtp(any(OtpDTO.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createNewUser_shouldThrowDobInFuture_whenDobIsInFuture() {
        CreateUserDTO request = validRegisterRequest();
        request.setDob("01/01/2999");

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(request.getEmail(), true))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createNewUser(request)
        );

        assertBusinessException(exception, DOB_IN_FUTURE, REGISTER.name());

        verify(otpServiceImpl, never()).verifyOtp(any(OtpDTO.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createNewUser_shouldThrowOtpVerificationFail_whenOtpVerificationDoesNotReturnSuccess() {
        CreateUserDTO request = validRegisterRequest();

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(request.getEmail(), true))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberAndActive(request.getPhoneNumber(), true))
                .thenReturn(Optional.empty());
        when(otpServiceImpl.verifyOtp(any(OtpDTO.class)))
                .thenReturn(response(OTP_VERIFICATION_FAIL, OTP.name(), null));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createNewUser(request)
        );

        assertBusinessException(exception, OTP_VERIFICATION_FAIL, REGISTER.name());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createNewUser_shouldWrapUnexpectedExceptionAsInternalServerError() {
        CreateUserDTO request = validRegisterRequest();

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenThrow(new RuntimeException("Database unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createNewUser(request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, REGISTER.name());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createNewUser_shouldWrapSaveFailureAsInternalServerError_afterOtpVerificationSucceeds() {
        CreateUserDTO request = validRegisterRequest();

        when(userRepository.findByUsernameAndActive(request.getUsername()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(request.getEmail(), true))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberAndActive(request.getPhoneNumber(), true))
                .thenReturn(Optional.empty());
        when(otpServiceImpl.verifyOtp(any(OtpDTO.class)))
                .thenReturn(response(OTP_VERIFICATION_SUCCESS, OTP.name(), null));
        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Database save failed"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createNewUser(request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, REGISTER.name());

        verify(otpServiceImpl).verifyOtp(any(OtpDTO.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createNewUser_shouldBeTransactional_soOtpConsumptionRollsBackIfUserSaveFails() throws NoSuchMethodException {
        assertThat(hasTransactionalAnnotation("createNewUser", CreateUserDTO.class)).isTrue();
    }

    // -------------------------------------------------------------------------
    // forgotPassword()
    // -------------------------------------------------------------------------

    @Test
    void forgotPassword_shouldThrowPasswordNotQualified_whenNewPasswordIsWeak() {
        ForgotPasswordDTO request = new ForgotPasswordDTO();
        request.setUsername("JustinBo123");
        request.setNewPassword("weak");
        request.setOtp("123456");
        request.setEmail("justin@example.com");

        User user = activeUser("JustinBo123");
        user.setEmail("justin@example.com");
        user.setPassword("encoded-old-password");

        mockForgotPasswordConfigs();

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            request.getUsername(),
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.of(user));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> userService.forgotPassword(request)
            );

            assertBusinessException(exception, PASSWORD_NOT_QUALIFIED, FORGOT_PASSWORD.name());
            assertThat(user.getPassword()).isEqualTo("encoded-old-password");

            verify(otpServiceImpl, never()).verifyOtp(any(OtpDTO.class));
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
            verify(tokenService, never()).revokeAllActiveRefreshTokensForUser(anyString());
        }
    }

    @Test
    void forgotPassword_shouldThrowNewPasswordSameAsOld_onlyAfterOtpVerification() {
        ForgotPasswordDTO request = new ForgotPasswordDTO();
        request.setUsername("JustinBo123");
        request.setNewPassword("OldTest123!");
        request.setOtp("123456");
        request.setEmail("justin@example.com");

        User user = activeUser("JustinBo123");
        user.setEmail("justin@example.com");
        user.setPassword("encoded-old-password");

        mockForgotPasswordConfigs();

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            request.getUsername(),
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
                    .thenReturn(true);
            when(otpServiceImpl.verifyOtp(any(OtpDTO.class)))
                    .thenReturn(response(OTP_VERIFICATION_SUCCESS, OTP.name(), null));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> userService.forgotPassword(request)
            );

            assertBusinessException(exception, NEW_PASSWORD_SAME_AS_OLD, FORGOT_PASSWORD.name());
            assertThat(user.getPassword()).isEqualTo("encoded-old-password");

            verify(otpServiceImpl).verifyOtp(any(OtpDTO.class));
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
            verify(tokenService, never()).revokeAllActiveRefreshTokensForUser(anyString());
        }
    }

    @Test
    void forgotPassword_shouldBeTransactional_soOtpConsumptionRollsBackIfPasswordSaveFails() throws NoSuchMethodException {
        assertThat(hasTransactionalAnnotation("forgotPassword", ForgotPasswordDTO.class)).isTrue();
    }

    @Test
    void forgotPassword_shouldUpdatePassword_whenUserExistsAndOtpIsValid() {
        ForgotPasswordDTO request = new ForgotPasswordDTO();
        request.setUsername("JustinBo123");
        request.setNewPassword("NewTest123!");
        request.setOtp("123456");
        request.setEmail("justin@example.com");

        User user = activeUser("JustinBo123");
        user.setEmail("justin@example.com");
        user.setPassword("old-password");

        mockErrorCode(PASSWORD_UPDATED_SUCCESS, FORGOT_PASSWORD.name());

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            request.getUsername(),
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.of(user));

            when(otpServiceImpl.verifyOtp(any(OtpDTO.class)))
                    .thenReturn(response(OTP_VERIFICATION_SUCCESS, OTP.name(), null));
            when(passwordEncoder.encode(request.getNewPassword()))
                    .thenReturn("encoded-new-password");

            CompleteResponse<Object> response = userService.forgotPassword(request);

            assertThat(response.getResponseBody().getCode())
                    .isEqualTo(PASSWORD_UPDATED_SUCCESS.getCode());

            assertThat(user.getPassword()).isEqualTo("encoded-new-password");
            verify(userRepository).save(user);
            verify(tokenService).revokeAllActiveRefreshTokensForUser("JustinBo123");

            ArgumentCaptor<OtpDTO> otpCaptor = ArgumentCaptor.forClass(OtpDTO.class);
            verify(otpServiceImpl).verifyOtp(otpCaptor.capture());

            OtpDTO verifyOtpDTO = otpCaptor.getValue();
            assertThat(verifyOtpDTO.getUserName()).isEqualTo("JustinBo123");
            assertThat(verifyOtpDTO.getOtp()).isEqualTo("123456");
            assertThat(verifyOtpDTO.getEmail()).isEqualTo("justin@example.com");
        }
    }

    @Test
    void forgotPassword_shouldReturnGenericOtpFailure_whenUserDoesNotExist() {
        ForgotPasswordDTO request = new ForgotPasswordDTO();
        request.setUsername("missingUser");
        request.setNewPassword("NewTest123!");
        request.setOtp("123456");

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            request.getUsername(),
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> userService.forgotPassword(request)
            );

            assertBusinessException(exception, OTP_VERIFICATION_FAIL, FORGOT_PASSWORD.name());

            verify(passwordEncoder).matches(eq(request.getNewPassword()), anyString());
            verify(otpServiceImpl, never()).verifyOtp(any(OtpDTO.class));
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    void forgotPassword_shouldThrowOtpVerificationFail_whenOtpIsInvalid() {
        ForgotPasswordDTO request = new ForgotPasswordDTO();
        request.setUsername("JustinBo123");
        request.setNewPassword("NewTest123!");
        request.setOtp("999999");

        User user = activeUser("JustinBo123");
        String originalPassword = user.getPassword();

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            request.getUsername(),
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.of(user));

            when(otpServiceImpl.verifyOtp(any(OtpDTO.class)))
                    .thenThrow(new BusinessException(OTP_CODE_NOT_CORRECT, OTP.name()));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> userService.forgotPassword(request)
            );

            assertBusinessException(exception, OTP_VERIFICATION_FAIL, FORGOT_PASSWORD.name());
            assertThat(user.getPassword()).isEqualTo(originalPassword);

            verify(userRepository, never()).save(any(User.class));
            verify(tokenService, never()).revokeAllActiveRefreshTokensForUser(anyString());
        }
    }

    // -------------------------------------------------------------------------
    // login()
    // -------------------------------------------------------------------------

    @Test
    void login_shouldReturnTokens_whenPasswordIsCorrect() {
        LoginDTO request = new LoginDTO();
        request.setUsername("JustinBo123");
        request.setPassword("Correct123!");
        request.setOverrideMaxSession(false);

        User user = activeUser("JustinBo123");
        user.setPassword("encoded-password");

        mockErrorCode(LOGIN_SUCCESS, LOGIN.name());

        Map<String, Object> refreshTokenBody = new HashMap<>();
        refreshTokenBody.put("refreshToken", "refresh-token-value");

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            request.getUsername(),
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                    .thenReturn(true);
            doNothing().when(tokenService)
                    .checkMaxActiveSessions(user.getUsername(), false);
            when(tokenService.generateAccessToken(eq(user.getUsername()), anyString()))
                    .thenReturn(response(TOKEN_GENERATE_SUCCESS, TOKEN.name(), "access-token-value"));
            when(tokenService.generateSessionToken(eq(user.getUsername()), anyString()))
                    .thenReturn(response(TOKEN_GENERATE_SUCCESS, TOKEN.name(), "session-token-value"));
            when(tokenService.generateRefreshToken(eq(user.getUsername()), anyString()))
                    .thenReturn(response(TOKEN_GENERATE_SUCCESS, TOKEN.name(), refreshTokenBody));

            CompleteResponse<Object> response = userService.login(request);

            assertThat(response.getResponseBody().getCode())
                    .isEqualTo(LOGIN_SUCCESS.getCode());

            @SuppressWarnings("unchecked")
            Map<String, String> body = (Map<String, String>) response.getResponseBody().getBody();

            assertThat(body.get("accessToken")).isEqualTo("access-token-value");
            assertThat(body.get("refreshToken")).isEqualTo("refresh-token-value");
            assertThat(body.get("sessionToken")).isEqualTo("session-token-value");

            verify(tokenService).checkMaxActiveSessions(user.getUsername(), false);
            verify(tokenService).generateAccessToken(eq(user.getUsername()), anyString());
            verify(tokenService).generateSessionToken(eq(user.getUsername()), anyString());
            verify(tokenService).generateRefreshToken(eq(user.getUsername()), anyString());
        }
    }

    @Test
    void login_shouldReturnPasswordNotCorrect_whenPasswordIsWrong() {
        LoginDTO request = new LoginDTO();
        request.setUsername("JustinBo123");
        request.setPassword("WrongPassword");

        User user = activeUser("JustinBo123");
        user.setPassword("encoded-password");

        mockErrorCode(PASSWORD_NOT_CORRECT, LOGIN.name());

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            request.getUsername(),
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                    .thenReturn(false);

            CompleteResponse<Object> response = userService.login(request);

            assertThat(response.getResponseBody().getCode())
                    .isEqualTo(PASSWORD_NOT_CORRECT.getCode());
            assertThat(response.getResponseBody().getBody()).isNull();

            verify(tokenService, never()).checkMaxActiveSessions(anyString(), anyBoolean());
            verify(tokenService, never()).generateAccessToken(anyString(), anyString());
            verify(tokenService, never()).generateRefreshToken(anyString(), anyString());
            verify(tokenService, never()).generateSessionToken(anyString(), anyString());
        }
    }

    @Test
    void login_shouldReturnSameInvalidCredentialsResponse_whenUserDoesNotExist() {
        LoginDTO request = new LoginDTO();
        request.setUsername("missingUser");
        request.setPassword("Password123!");

        mockErrorCode(PASSWORD_NOT_CORRECT, LOGIN.name());

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            request.getUsername(),
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.empty());

            CompleteResponse<Object> response = userService.login(request);

            assertThat(response.getResponseBody().getCode())
                    .isEqualTo(PASSWORD_NOT_CORRECT.getCode());
            assertThat(response.getResponseBody().getBody()).isNull();
            verify(passwordEncoder).matches(eq(request.getPassword()), anyString());
            verify(tokenService, never()).generateAccessToken(anyString(), anyString());
        }
    }

    @Test
    void login_shouldRethrowMaxSessionsReached_whenTokenServiceRejectsLogin() {
        LoginDTO request = new LoginDTO();
        request.setUsername("JustinBo123");
        request.setPassword("Correct123!");
        request.setOverrideMaxSession(false);

        User user = activeUser("JustinBo123");
        user.setPassword("encoded-password");

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            request.getUsername(),
                            configurationRepository,
                            userRepository
                    ))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                    .thenReturn(true);
            doThrow(new BusinessException(MAX_SESSIONS_REACHED, LOGIN.name()))
                    .when(tokenService)
                    .checkMaxActiveSessions(user.getUsername(), false);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> userService.login(request)
            );

            assertBusinessException(exception, MAX_SESSIONS_REACHED, LOGIN.name());

            verify(tokenService, never()).generateAccessToken(anyString(), anyString());
            verify(tokenService, never()).generateRefreshToken(anyString(), anyString());
            verify(tokenService, never()).generateSessionToken(anyString(), anyString());
        }
    }

    @Test
    void login_shouldWrapUnexpectedExceptionAsInternalServerError() {
        LoginDTO request = new LoginDTO();
        request.setUsername("JustinBo123");
        request.setPassword("Correct123!");

        try (MockedStatic<Common> commonMock = mockStatic(Common.class, CALLS_REAL_METHODS)) {
            commonMock.when(() -> Common.findUser(
                            request.getUsername(),
                            configurationRepository,
                            userRepository
                    ))
                    .thenThrow(new RuntimeException("Database unavailable"));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> userService.login(request)
            );

            assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());
        }
    }

    // -------------------------------------------------------------------------
    // logout()
    // -------------------------------------------------------------------------

    @Test
    void logout_shouldRevokeSessionAndRefreshTokensAndClearSecurityContext_whenSessionTokenIsValid() {
        String username = "JustinBo123";
        String sessionId = "session-123";
        String sessionToken = "raw-session-token";

        mockErrorCode(LOGOUT_SUCCESS, LOGOUT.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(username);
        when(authenticatedUserProvider.getSessionId()).thenReturn(sessionId);

        CompleteResponse<Object> response = userService.logout(sessionToken);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(LOGOUT_SUCCESS.getCode());

        verify(tokenService).revokeSessionTokenBySessionId(username, sessionId, sessionToken);
        verify(tokenService).revokeActiveRefreshTokensBySessionId(sessionId);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void logout_shouldThrowSessionTokenInvalid_whenSessionTokenIsNull() {
        when(authenticatedUserProvider.getUsername()).thenReturn("JustinBo123");
        when(authenticatedUserProvider.getSessionId()).thenReturn("session-123");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.logout(null)
        );

        assertBusinessException(exception, SESSION_TOKEN_INVALID, TOKEN.name());

        verify(tokenService, never()).revokeSessionTokenBySessionId(anyString(), anyString(), anyString());
        verify(tokenService, never()).revokeActiveRefreshTokensBySessionId(anyString());
    }

    @Test
    void logout_shouldThrowSessionTokenInvalid_whenSessionTokenIsBlank() {
        when(authenticatedUserProvider.getUsername()).thenReturn("JustinBo123");
        when(authenticatedUserProvider.getSessionId()).thenReturn("session-123");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.logout("   ")
        );

        assertBusinessException(exception, SESSION_TOKEN_INVALID, TOKEN.name());

        verify(tokenService, never()).revokeSessionTokenBySessionId(anyString(), anyString(), anyString());
        verify(tokenService, never()).revokeActiveRefreshTokensBySessionId(anyString());
    }

    @Test
    void logout_shouldRethrowBusinessExceptionFromTokenService() {
        String username = "JustinBo123";
        String sessionId = "session-123";
        String sessionToken = "raw-session-token";

        when(authenticatedUserProvider.getUsername()).thenReturn(username);
        when(authenticatedUserProvider.getSessionId()).thenReturn(sessionId);

        doThrow(new BusinessException(SESSION_TOKEN_INVALID, TOKEN.name()))
                .when(tokenService)
                .revokeSessionTokenBySessionId(username, sessionId, sessionToken);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.logout(sessionToken)
        );

        assertBusinessException(exception, SESSION_TOKEN_INVALID, TOKEN.name());

        verify(tokenService, never()).revokeActiveRefreshTokensBySessionId(anyString());
    }

    @Test
    void logout_shouldWrapUnexpectedExceptionAsInternalServerError() {
        String username = "JustinBo123";
        String sessionId = "session-123";
        String sessionToken = "raw-session-token";

        when(authenticatedUserProvider.getUsername()).thenReturn(username);
        when(authenticatedUserProvider.getSessionId()).thenReturn(sessionId);

        doThrow(new RuntimeException("Database unavailable"))
                .when(tokenService)
                .revokeSessionTokenBySessionId(username, sessionId, sessionToken);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.logout(sessionToken)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, LOGOUT.name());

        verify(tokenService, never()).revokeActiveRefreshTokensBySessionId(anyString());
    }

    // -------------------------------------------------------------------------
    // getMyProfile()
    // -------------------------------------------------------------------------

    @Test
    void getMyProfile_shouldReturnProfile_whenCurrentUserExists() {
        User user = profileUser();
        UserProfileResponseDTO responseDTO = profileResponseDTO(user);

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(userMapper.toProfileResponseDTO(user)).thenReturn(responseDTO);

        CompleteResponse<Object> response = userService.getMyProfile();

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(SEARCH_INFO_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(responseDTO);

        verify(authenticatedUserProvider).getUsername();
        verify(userRepository).findByUsernameAndActive(user.getUsername());
        verify(userMapper).toProfileResponseDTO(user);
    }

    @Test
    void getMyProfile_shouldThrowUserNotFound_whenCurrentUserDoesNotExist() {
        when(authenticatedUserProvider.getUsername()).thenReturn("missingUser");
        when(userRepository.findByUsernameAndActive("missingUser"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.getMyProfile()
        );

        assertBusinessException(exception, USER_NOT_FOUND, COMMON.name());

        verifyNoInteractions(userMapper);
    }

    @Test
    void getMyProfile_shouldWrapUnexpectedExceptionAsInternalServerError() {
        when(authenticatedUserProvider.getUsername()).thenReturn("JustinBo123");
        when(userRepository.findByUsernameAndActive("JustinBo123"))
                .thenThrow(new RuntimeException("Database unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.getMyProfile()
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verifyNoInteractions(userMapper);
    }

    // -------------------------------------------------------------------------
    // updateMyProfile()
    // -------------------------------------------------------------------------

    @Test
    void updateMyProfile_shouldValidateMapSaveAndReturnProfile_whenInputIsValid() {
        User user = profileUser();

        UpdateUserProfileDTO request = new UpdateUserProfileDTO();
        request.setDisplayName("Justin Bui");
        request.setPhoneNumber("0412345678");
        request.setDob("1999-08-16");
        request.setProfileImageUrl("https://res.cloudinary.com/demo/image/upload/new-avatar.png");
        request.setProfileImagePublicId("wandermate/profile-images/users/1/profile-1-new");

        LocalDate parsedDob = LocalDate.of(1999, 8, 16);
        UserProfileResponseDTO responseDTO = profileResponseDTO(user);

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(userValidator.validateUpdateProfileInput(request, user))
                .thenReturn(parsedDob);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toProfileResponseDTO(user)).thenReturn(responseDTO);

        CompleteResponse<Object> response = userService.updateMyProfile(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(SEARCH_INFO_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(responseDTO);
        assertThat(user.getModifiedDate()).isNotNull();

        verify(userValidator).validateUpdateProfileInput(request, user);
        verify(userMapper).updateProfileEntity(user, request, parsedDob);
        verify(userRepository).save(user);
        verify(userMapper).toProfileResponseDTO(user);
    }


    @Test
    void updateMyProfile_shouldDeleteOldCloudinaryImage_whenProfileImageIsReplaced() throws IOException {
        User user = profileUser();
        user.setProfileImagePublicId("wandermate/profile-images/users/1/profile-1-old");

        UpdateUserProfileDTO request = new UpdateUserProfileDTO();
        request.setDisplayName("Justin Bui");
        request.setProfileImageUrl("https://res.cloudinary.com/demo/image/upload/new-avatar.png");
        request.setProfileImagePublicId("wandermate/profile-images/users/1/profile-1-new");

        LocalDate parsedDob = LocalDate.of(1999, 8, 16);
        UserProfileResponseDTO responseDTO = profileResponseDTO(user);

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(userValidator.validateUpdateProfileInput(request, user))
                .thenReturn(parsedDob);
        doAnswer(invocation -> {
            User mappedUser = invocation.getArgument(0);
            mappedUser.setProfileImageUrl(request.getProfileImageUrl());
            mappedUser.setProfileImagePublicId(request.getProfileImagePublicId());
            return null;
        }).when(userMapper).updateProfileEntity(user, request, parsedDob);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toProfileResponseDTO(user)).thenReturn(responseDTO);

        CompleteResponse<Object> response = userService.updateMyProfile(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(SEARCH_INFO_SUCCESS.getCode());
        verify(cloudinaryImageClient).deleteOldCloudinaryImageIfChanged(
                "wandermate/profile-images/users/1/profile-1-old",
                "wandermate/profile-images/users/1/profile-1-new",
                "profile image"
        );
    }

    @Test
    void updateMyProfile_shouldDeleteOldCloudinaryImage_whenProfileImageIsRemoved() throws IOException {
        User user = profileUser();
        user.setProfileImagePublicId("wandermate/profile-images/users/1/profile-1-old");

        UpdateUserProfileDTO request = new UpdateUserProfileDTO();
        request.setProfileImageUrl("");
        request.setProfileImagePublicId("");

        UserProfileResponseDTO responseDTO = profileResponseDTO(user);

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(userValidator.validateUpdateProfileInput(request, user))
                .thenReturn(user.getDob());
        doAnswer(invocation -> {
            User mappedUser = invocation.getArgument(0);
            mappedUser.setProfileImageUrl(null);
            mappedUser.setProfileImagePublicId(null);
            return null;
        }).when(userMapper).updateProfileEntity(user, request, user.getDob());
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toProfileResponseDTO(user)).thenReturn(responseDTO);

        CompleteResponse<Object> response = userService.updateMyProfile(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(SEARCH_INFO_SUCCESS.getCode());
        verify(cloudinaryImageClient).deleteOldCloudinaryImageIfChanged(
                "wandermate/profile-images/users/1/profile-1-old",
                null,
                "profile image"
        );
    }

    @Test
    void updateMyProfile_shouldNotDeleteCloudinaryImage_whenProfileImagePublicIdIsUnchanged() throws IOException {
        User user = profileUser();
        user.setProfileImagePublicId("wandermate/profile-images/users/1/profile-1-same");

        UpdateUserProfileDTO request = new UpdateUserProfileDTO();
        request.setProfileImageUrl("https://res.cloudinary.com/demo/image/upload/same-avatar.png");
        request.setProfileImagePublicId("wandermate/profile-images/users/1/profile-1-same");

        UserProfileResponseDTO responseDTO = profileResponseDTO(user);

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(userValidator.validateUpdateProfileInput(request, user))
                .thenReturn(user.getDob());
        doAnswer(invocation -> null)
                .when(userMapper)
                .updateProfileEntity(user, request, user.getDob());
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toProfileResponseDTO(user)).thenReturn(responseDTO);

        CompleteResponse<Object> response = userService.updateMyProfile(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(SEARCH_INFO_SUCCESS.getCode());
        verify(cloudinaryImageClient).deleteOldCloudinaryImageIfChanged(
                "wandermate/profile-images/users/1/profile-1-same",
                "wandermate/profile-images/users/1/profile-1-same",
                "profile image"
        );
    }

    @Test
    void updateMyProfile_shouldAskCloudinaryClientToCleanUpOldImage_whenProfileImageIsReplaced() throws IOException {
        User user = profileUser();
        user.setProfileImagePublicId("wandermate/profile-images/users/1/profile-1-old");

        UpdateUserProfileDTO request = new UpdateUserProfileDTO();
        request.setProfileImageUrl("https://res.cloudinary.com/demo/image/upload/new-avatar.png");
        request.setProfileImagePublicId("wandermate/profile-images/users/1/profile-1-new");

        UserProfileResponseDTO responseDTO = profileResponseDTO(user);

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(userValidator.validateUpdateProfileInput(request, user))
                .thenReturn(user.getDob());
        doAnswer(invocation -> {
            User mappedUser = invocation.getArgument(0);
            mappedUser.setProfileImageUrl(request.getProfileImageUrl());
            mappedUser.setProfileImagePublicId(request.getProfileImagePublicId());
            return null;
        }).when(userMapper).updateProfileEntity(user, request, user.getDob());
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toProfileResponseDTO(user)).thenReturn(responseDTO);
        CompleteResponse<Object> response = userService.updateMyProfile(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(SEARCH_INFO_SUCCESS.getCode());
        verify(cloudinaryImageClient).deleteOldCloudinaryImageIfChanged(
                "wandermate/profile-images/users/1/profile-1-old",
                "wandermate/profile-images/users/1/profile-1-new",
                "profile image"
        );
    }

    @Test
    void updateMyProfile_shouldThrowUserNotFound_whenCurrentUserDoesNotExist() {
        UpdateUserProfileDTO request = new UpdateUserProfileDTO();

        when(authenticatedUserProvider.getUsername()).thenReturn("missingUser");
        when(userRepository.findByUsernameAndActive("missingUser"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateMyProfile(request)
        );

        assertBusinessException(exception, USER_NOT_FOUND, COMMON.name());

        verifyNoInteractions(userValidator);
        verifyNoInteractions(userMapper);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateMyProfile_shouldRethrowBusinessExceptionFromValidator() {
        User user = profileUser();

        UpdateUserProfileDTO request = new UpdateUserProfileDTO();
        request.setPhoneNumber("0412345678");

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(userValidator.validateUpdateProfileInput(request, user))
                .thenThrow(new BusinessException(PHONE_NUMBER_TAKEN, REGISTER.name()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateMyProfile(request)
        );

        assertBusinessException(exception, PHONE_NUMBER_TAKEN, REGISTER.name());

        verify(userMapper, never()).updateProfileEntity(any(User.class), any(UpdateUserProfileDTO.class), any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateMyProfile_shouldWrapUnexpectedExceptionAsInternalServerError() {
        User user = profileUser();

        UpdateUserProfileDTO request = new UpdateUserProfileDTO();
        request.setDisplayName("Justin Bui");

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(userValidator.validateUpdateProfileInput(request, user))
                .thenReturn(null);
        doThrow(new RuntimeException("Mapper failed"))
                .when(userMapper)
                .updateProfileEntity(user, request, null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateMyProfile(request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(userRepository, never()).save(any(User.class));
    }

    // -------------------------------------------------------------------------
    // updateMySettings()
    // -------------------------------------------------------------------------

    @Test
    void updateMySettings_shouldValidateMapSaveAndReturnProfile_whenInputIsValid() {
        User user = profileUser();

        UpdateUserSettingsDTO request = new UpdateUserSettingsDTO();
        request.setPreferredTheme(UserSettingEnum.DARK);

        UserProfileResponseDTO responseDTO = profileResponseDTO(user);

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toProfileResponseDTO(user)).thenReturn(responseDTO);

        CompleteResponse<Object> response = userService.updateMySettings(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(SEARCH_INFO_SUCCESS.getCode());
        assertThat(response.getResponseBody().getBody()).isEqualTo(responseDTO);
        assertThat(user.getModifiedDate()).isNotNull();

        verify(userValidator).validateUpdateSettingsInput(request);
        verify(userMapper).updateSettingsEntity(user, request);
        verify(userRepository).save(user);
        verify(userMapper).toProfileResponseDTO(user);
    }

    @Test
    void updateMySettings_shouldThrowUserNotFound_whenCurrentUserDoesNotExist() {
        UpdateUserSettingsDTO request = new UpdateUserSettingsDTO();
        request.setPreferredTheme(UserSettingEnum.DARK);

        when(authenticatedUserProvider.getUsername()).thenReturn("missingUser");
        when(userRepository.findByUsernameAndActive("missingUser"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateMySettings(request)
        );

        assertBusinessException(exception, USER_NOT_FOUND, COMMON.name());

        verifyNoInteractions(userValidator);
        verifyNoInteractions(userMapper);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateMySettings_shouldRethrowBusinessExceptionFromValidator() {
        User user = profileUser();

        UpdateUserSettingsDTO request = new UpdateUserSettingsDTO();

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));

        doThrow(new BusinessException(INVALID_INPUT, COMMON.name()))
                .when(userValidator)
                .validateUpdateSettingsInput(request);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateMySettings(request)
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());

        verify(userMapper, never()).updateSettingsEntity(any(User.class), any(UpdateUserSettingsDTO.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateMySettings_shouldWrapUnexpectedExceptionAsInternalServerError() {
        User user = profileUser();

        UpdateUserSettingsDTO request = new UpdateUserSettingsDTO();
        request.setPreferredTheme(UserSettingEnum.LIGHT);

        when(authenticatedUserProvider.getUsername()).thenReturn(user.getUsername());
        when(userRepository.findByUsernameAndActive(user.getUsername()))
                .thenReturn(Optional.of(user));

        doThrow(new RuntimeException("Mapper failed"))
                .when(userMapper)
                .updateSettingsEntity(user, request);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateMySettings(request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());

        verify(userRepository, never()).save(any(User.class));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User profileUser() {
        User user = activeUser("JustinBo123");
        user.setUserId(1L);
        user.setDisplayName("Justin Bui");
        user.setDob(LocalDate.of(1999, 8, 16));
        user.setCreatedDate(Instant.parse("2026-01-01T09:00:00Z"));
        user.setPreferredTheme(UserSettingEnum.SYSTEM);
        user.setProfileImageUrl("https://example.com/avatar.png");
        user.setProfileImagePublicId("wandermate/profile-images/users/1/profile-1-existing");
        return user;
    }

    private UserProfileResponseDTO profileResponseDTO(User user) {
        UserProfileResponseDTO responseDTO = new UserProfileResponseDTO();
        responseDTO.setUserId(user.getUserId());
        responseDTO.setUsername(user.getUsername());
        responseDTO.setDisplayName(user.getDisplayName());
        responseDTO.setEmail(user.getEmail());
        responseDTO.setPhoneNumber(user.getPhoneNumber());
        responseDTO.setDob(user.getDob());
        responseDTO.setPreferredTheme(user.getPreferredTheme());
        responseDTO.setProfileImageUrl(user.getProfileImageUrl());
        responseDTO.setProfileImagePublicId(user.getProfileImagePublicId());
        responseDTO.setCreatedDate(user.getCreatedDate());
        responseDTO.setModifiedDate(user.getModifiedDate());
        return responseDTO;
    }

    private CreateUserDTO validRegisterRequest() {
        CreateUserDTO request = new CreateUserDTO();
        request.setUsername("JustinBo123");
        request.setPassword("Test123!");
        request.setEmail("justin@example.com");
        request.setPhoneNumber("0412345678");
        request.setDob("16/08/1999");
        request.setOtp("123456");
        return request;
    }

    private User activeUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setEmail(username.toLowerCase() + "@example.com");
        user.setPhoneNumber("0412345678");
        user.setActive(true);
        return user;
    }

    private CompleteResponse<Object> response(ErrorCodeEnum errorCodeEnum, String flow, Object body) {
        return new CompleteResponse<>(
                new ResponseBody<>(
                        errorCodeEnum.getCode(),
                        errorCodeEnum.getMessage(),
                        flow,
                        body
                ),
                200
        );
    }

    private void mockErrorCode(ErrorCodeEnum errorCodeEnum, String flow) {
        ErrorCodeEntity entity = new ErrorCodeEntity();
        entity.setErrorCode(errorCodeEnum.getCode());
        entity.setErrorMessage(errorCodeEnum.getMessage());
        entity.setErrorEnum(errorCodeEnum.name());
        entity.setFlow(flow);
        entity.setCreatedDate(Instant.now());

        when(errorCodeRepository.findByErrorEnumAndFlow(errorCodeEnum.name(), flow))
                .thenReturn(Optional.of(entity));
    }

    private boolean hasTransactionalAnnotation(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = UserServiceImpl.class.getMethod(methodName, parameterTypes);

        return method.isAnnotationPresent(Transactional.class)
                || UserServiceImpl.class.isAnnotationPresent(Transactional.class);
    }

    private void assertBusinessException(
            BusinessException exception,
            ErrorCodeEnum expectedErrorCode,
            String expectedFlow
    ) {
        assertThat(exception.getErrorCodeEnum()).isEqualTo(expectedErrorCode);
        assertThat(exception.getFlow()).isEqualTo(expectedFlow);
    }

    private void mockForgotPasswordConfigs() {
        when(configurationRepository.findByConfigCode(anyString()))
                .thenAnswer(invocation -> {
                    String configCode = invocation.getArgument(0);

                    ConfigurationEntity entity = new ConfigurationEntity();
                    entity.setConfigCode(configCode);
                    entity.setCreatedDate(Instant.now());

                    if (PHONE_VN_PATTERN.name().equals(configCode)) {
                        entity.setConfigValue("^(0|\\+84)[0-9]{9,10}$");
                        return Optional.of(entity);
                    }

                    if (EMAIL_PATTERN.name().equals(configCode)) {
                        entity.setConfigValue("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
                        return Optional.of(entity);
                    }

                    if (PASSWORD_PATTERN.name().equals(configCode)) {
                        entity.setConfigValue("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+\\[\\]{}|;:'\\\",.<>?])[^\\s<>\\\\/]{8,20}$");
                        return Optional.of(entity);
                    }

                    return Optional.empty();
                });
    }
}
