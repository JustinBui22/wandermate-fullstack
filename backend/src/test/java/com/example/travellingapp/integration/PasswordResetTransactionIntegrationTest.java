package com.example.travellingapp.integration;

import com.example.travellingapp.dto.request.ForgotPasswordDTO;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.mapper.UserMapper;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.security.AccountEnumerationRateLimiter;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.CloudinaryImageClient;
import com.example.travellingapp.service.TokenService;
import com.example.travellingapp.service.impl.OtpServiceImpl;
import com.example.travellingapp.service.impl.UserServiceImpl;
import com.example.travellingapp.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.example.travellingapp.enums.CommonEnum.OTP;
import static com.example.travellingapp.enums.ErrorCodeEnum.OTP_VERIFICATION_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(UserServiceImpl.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PasswordResetTransactionIntegrationTest {

    private static final String USERNAME = "JustinBo123";
    private static final String OLD_PASSWORD = "encoded-old-password";
    private static final String NEW_PASSWORD = "NewTest123!";

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private OtpServiceImpl otpService;

    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    @MockitoBean
    private UserValidator userValidator;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private CloudinaryImageClient cloudinaryImageClient;

    @MockitoBean
    private AccountEnumerationRateLimiter accountEnumerationRateLimiter;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User();
        user.setUsername(USERNAME);
        user.setPassword(OLD_PASSWORD);
        user.setEmail("justin@example.com");
        user.setActive(true);
        user.setOAuth2(false);
        user.setCreatedDate(Instant.now());
        userRepository.saveAndFlush(user);

        when(passwordEncoder.matches(NEW_PASSWORD, OLD_PASSWORD)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("encoded-new-password");
        when(otpService.verifyOtp(any())).thenReturn(
                new CompleteResponse<>(
                        new ResponseBody<>(
                                OTP_VERIFICATION_SUCCESS.getCode(),
                                OTP_VERIFICATION_SUCCESS.getMessage(),
                                OTP.name(),
                                null
                        ),
                        OTP_VERIFICATION_SUCCESS.getHttpStatusCodeEnum().value
                )
        );
    }

    @Test
    void forgotPassword_rollsBackPasswordUpdateWhenSessionRevocationFails() {
        doThrow(new IllegalStateException("Token store unavailable"))
                .when(tokenService)
                .revokeAllActiveRefreshTokensForUser(USERNAME);

        ForgotPasswordDTO request = new ForgotPasswordDTO();
        request.setUsername(USERNAME);
        request.setNewPassword(NEW_PASSWORD);
        request.setOtp("123456");

        assertThrows(IllegalStateException.class, () -> userService.forgotPassword(request));

        User persistedUser = userRepository.findByUsernameAndActive(USERNAME).orElseThrow();
        assertThat(persistedUser.getPassword()).isEqualTo(OLD_PASSWORD);
    }
}
