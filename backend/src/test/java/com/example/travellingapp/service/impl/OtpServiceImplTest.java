package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.OtpDTO;
import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.entity.EmailContentEntity;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.OtpCheckEntity;
import com.example.travellingapp.entity.SmsEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.EmailEnum;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.enums.OtpPurpose;
import com.example.travellingapp.enums.SmsEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.example.travellingapp.repository.EmailRepository;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.OtpCheckRepository;
import com.example.travellingapp.repository.SmsRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.OtpFailureAccountingService;
import com.example.travellingapp.validator.OtpValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private EmailServiceImpl emailServiceImpl;

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private SmsServiceImpl smsServiceImpl;

    @Mock
    private SmsRepository smsRepository;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private OtpCheckRepository otpCheckRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpValidator otpValidator;

    @Mock
    private OtpFailureAccountingService otpFailureAccountingService;

    private OtpServiceImpl otpService;

    private static final String USERNAME = "JustinBo123";
    private static final String EMAIL_ADDRESS = "justin@example.com";
    private static final String PHONE_NUMBER = "0412345678";

    @BeforeEach
    void setUp() {
        otpService = new OtpServiceImpl(
                emailServiceImpl,
                errorCodeRepository,
                smsServiceImpl,
                smsRepository,
                emailRepository,
                configurationRepository,
                otpCheckRepository,
                userRepository,
                otpValidator,
                otpFailureAccountingService
        );
    }

    @Test
    void sendOtp_shouldReturnGenericSuccessWithoutSending_whenPasswordResetAccountDoesNotExist() {
        OtpDTO request = emailOtpRequest();
        request.setUserName("missing@example.com");
        request.setPurpose(OtpPurpose.PASSWORD_RESET);

        mockConfig(PHONE_VN_PATTERN.name(), "^(0|84|\\+84)(3|5|7|8|9)\\d{7,8}$");
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        mockErrorCode(OTP_SENT_SUCCESS, OTP.name());
        when(userRepository.findByEmailAndActive("missing@example.com", true))
                .thenReturn(Optional.empty());

        CompleteResponse<Object> response = otpService.sendOtp(request);

        assertThat(response.getResponseBody().getCode()).isEqualTo(OTP_SENT_SUCCESS.getCode());
        verify(emailServiceImpl, never()).sendEmail(anyString(), anyString(), anyString(), anyString());
        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void sendOtp_shouldResolveAccountAndSend_whenPasswordResetDetailsMatch() {
        OtpDTO request = emailOtpRequest();
        request.setUserName(EMAIL_ADDRESS);
        request.setPurpose(OtpPurpose.PASSWORD_RESET);
        User user = activeUser();

        mockConfig(PHONE_VN_PATTERN.name(), "^(0|84|\\+84)(3|5|7|8|9)\\d{7,8}$");
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        mockSendRetryConfigs();
        mockOtpExpirationConfig("120000");
        mockConfig(EMAIL_ADDRESS_CONFIG.name(), "noreply@wandermate.com");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());
        mockErrorCode(OTP_SENT_SUCCESS, OTP.name());

        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true)).thenReturn(Optional.of(user));
        when(otpCheckRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(emailRepository.findByEmailEnum(EmailEnum.EMAIL_OTP_REGISTER))
                .thenReturn(Optional.of(emailContent()));

        CompleteResponse<Object> response = otpService.sendOtp(request);

        assertThat(response.getResponseBody().getCode()).isEqualTo(OTP_SENT_SUCCESS.getCode());
        assertThat(request.getUserName()).isEqualTo(USERNAME);
        verify(emailServiceImpl).sendEmail(anyString(), eq(EMAIL_ADDRESS), anyString(), anyString());
    }

    // -------------------------------------------------------------------------
    // sendOtp() - Email OTP success
    // -------------------------------------------------------------------------

    @Test
    void sendOtp_shouldSendEmailOtpAndSaveOtpRecord_whenNewUserEmailIsAvailable() {
        OtpDTO request = emailOtpRequest();

        mockSendRetryConfigs();
        mockOtpExpirationConfig("120000");
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        mockConfig(EMAIL_ADDRESS_CONFIG.name(), "noreply@wandermate.com");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());
        mockErrorCode(OTP_SENT_SUCCESS, OTP.name());

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());
        when(emailRepository.findByEmailEnum(EmailEnum.EMAIL_OTP_REGISTER))
                .thenReturn(Optional.of(emailContent()));

        CompleteResponse<Object> response = otpService.sendOtp(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(OTP_SENT_SUCCESS.getCode());

        ArgumentCaptor<OtpCheckEntity> otpCaptor =
                ArgumentCaptor.forClass(OtpCheckEntity.class);

        verify(otpCheckRepository).save(otpCaptor.capture());

        OtpCheckEntity savedOtp = otpCaptor.getValue();

        assertThat(savedOtp.getUsername()).isEqualTo(USERNAME);
        assertThat(savedOtp.getEmail()).isEqualTo(EMAIL_ADDRESS);
        assertThat(savedOtp.getPhoneNumber()).isNull();
        assertThat(savedOtp.getNewestOtp()).matches("\\d{6}");
        assertThat(savedOtp.getRetrySendOtpCount()).isEqualTo(1);
        assertThat(savedOtp.getRetryVerifyOtpCount()).isEqualTo(0);
        assertThat(savedOtp.isBlock()).isFalse();
        assertThat(savedOtp.getOtpExpirationTime()).isAfter(LocalDateTime.now());

        verify(emailServiceImpl).sendEmail(
                eq("noreply@wandermate.com"),
                eq(EMAIL_ADDRESS),
                contains("2"),
                contains(USERNAME)
        );

        verify(emailServiceImpl).sendEmail(
                anyString(),
                anyString(),
                anyString(),
                contains(savedOtp.getNewestOtp())
        );

        verify(smsServiceImpl, never()).sendSms(anyString(), anyString());
    }

    @Test
    void sendOtp_shouldUpdateExistingOtpRecord_whenUserRequestsEmailOtpAgainAfterCooldown() {
        OtpDTO request = emailOtpRequest();

        OtpCheckEntity existingOtp = otpRecordForEmail();
        existingOtp.setCreatedDate(LocalDateTime.now().minusMinutes(2));
        existingOtp.setRetrySendOtpCount(1);
        existingOtp.setRetryVerifyOtpCount(2);

        mockSendRetryConfigs();
        mockOtpCooldownConfig("60000");
        mockOtpExpirationConfig("120000");
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        mockConfig(EMAIL_ADDRESS_CONFIG.name(), "noreply@wandermate.com");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());
        mockErrorCode(OTP_SENT_SUCCESS, OTP.name());

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(existingOtp));
        when(emailRepository.findByEmailEnum(EmailEnum.EMAIL_OTP_REGISTER))
                .thenReturn(Optional.of(emailContent()));

        otpService.sendOtp(request);

        assertThat(existingOtp.getEmail()).isEqualTo(EMAIL_ADDRESS);
        assertThat(existingOtp.getPhoneNumber()).isNull();
        assertThat(existingOtp.getNewestOtp()).matches("\\d{6}");
        assertThat(existingOtp.getRetrySendOtpCount()).isEqualTo(2);
        assertThat(existingOtp.getRetryVerifyOtpCount()).isEqualTo(0);

        verify(otpCheckRepository).save(existingOtp);
    }

    @Test
    void sendOtp_shouldThrowOtpCooldownNotExpired_whenUserRequestsEmailOtpAgainTooSoon() {
        OtpDTO request = emailOtpRequest();

        OtpCheckEntity existingOtp = otpRecordForEmail();
        existingOtp.setCreatedDate(LocalDateTime.now().minusSeconds(10));
        existingOtp.setRetrySendOtpCount(1);
        existingOtp.setBlock(false);

        mockSendRetryConfigs();
        mockOtpCooldownConfig("60000");
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(existingOtp));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, OTP_COOLDOWN_NOT_EXPIRED, OTP.name());

        verify(emailServiceImpl, never()).sendEmail(anyString(), anyString(), anyString(), anyString());
        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void sendOtp_shouldReuseExistingOtpRecordByEmail_whenSameEmailRequestedWithDifferentUsernameAfterCooldown() {
        OtpDTO request = emailOtpRequest();
        request.setUserName("NewJustinBo123");

        OtpCheckEntity existingOtp = otpRecordForEmail();
        existingOtp.setUsername("OldJustinBo123");
        existingOtp.setCreatedDate(LocalDateTime.now().minusMinutes(2));
        existingOtp.setRetrySendOtpCount(1);
        existingOtp.setRetryVerifyOtpCount(2);

        mockSendRetryConfigs();
        mockOtpCooldownConfig("60000");
        mockOtpExpirationConfig("120000");
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        mockConfig(EMAIL_ADDRESS_CONFIG.name(), "noreply@wandermate.com");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());
        mockErrorCode(OTP_SENT_SUCCESS, OTP.name());

        when(userRepository.findByUsernameAndActive("NewJustinBo123"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername("NewJustinBo123"))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByEmailIgnoreCase(EMAIL_ADDRESS))
                .thenReturn(Optional.of(existingOtp));
        when(emailRepository.findByEmailEnum(EmailEnum.EMAIL_OTP_REGISTER))
                .thenReturn(Optional.of(emailContent()));

        otpService.sendOtp(request);

        assertThat(existingOtp.getUsername()).isEqualTo("NewJustinBo123");
        assertThat(existingOtp.getEmail()).isEqualTo(EMAIL_ADDRESS);
        assertThat(existingOtp.getNewestOtp()).matches("\\d{6}");
        assertThat(existingOtp.getRetrySendOtpCount()).isEqualTo(2);
        assertThat(existingOtp.getRetryVerifyOtpCount()).isEqualTo(0);

        verify(otpCheckRepository).findByEmailIgnoreCase(EMAIL_ADDRESS);
        verify(otpCheckRepository).save(existingOtp);
    }

    @Test
    void sendOtp_shouldThrowOtpCooldownNotExpired_whenSameEmailRequestedWithDifferentUsernameTooSoon() {
        OtpDTO request = emailOtpRequest();
        request.setUserName("NewJustinBo123");

        OtpCheckEntity existingOtp = otpRecordForEmail();
        existingOtp.setUsername("OldJustinBo123");
        existingOtp.setCreatedDate(LocalDateTime.now().minusSeconds(10));
        existingOtp.setRetrySendOtpCount(1);
        existingOtp.setBlock(false);

        mockSendRetryConfigs();
        mockOtpCooldownConfig("60000");
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

        when(userRepository.findByUsernameAndActive("NewJustinBo123"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername("NewJustinBo123"))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByEmailIgnoreCase(EMAIL_ADDRESS))
                .thenReturn(Optional.of(existingOtp));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, OTP_COOLDOWN_NOT_EXPIRED, OTP.name());

        assertThat(existingOtp.getUsername()).isEqualTo("NewJustinBo123");
        verify(otpCheckRepository).findByEmailIgnoreCase(EMAIL_ADDRESS);
        verify(emailServiceImpl, never()).sendEmail(anyString(), anyString(), anyString(), anyString());
        verify(otpCheckRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // sendOtp() - Phone OTP success
    // -------------------------------------------------------------------------

    @Test
    void sendOtp_shouldSendPhoneOtpAndSaveOtpRecord_whenNewUserPhoneIsAvailable() {
        OtpDTO request = phoneOtpRequest();

        mockSendRetryConfigs();
        mockOtpExpirationConfig("120000");
        mockConfig(PHONE_VN_PATTERN.name(), "^(0|\\+84)[0-9]{9,10}$");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());
        mockErrorCode(OTP_SENT_SUCCESS, OTP.name());

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberAndActive(PHONE_NUMBER, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());
        when(smsRepository.findBySmsCodeAndSmsFlow(
                SmsEnum.SMS_OTP_REGISTER.getCode(),
                SmsEnum.SMS_OTP_REGISTER.getFlow().name()
        )).thenReturn(Optional.of(smsContent()));
        when(smsServiceImpl.sendSms(eq(PHONE_NUMBER), contains("Your OTP is")))
                .thenReturn(smsResponse(SMS_SENT_SUCCESS));

        CompleteResponse<Object> response = otpService.sendOtp(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(OTP_SENT_SUCCESS.getCode());

        ArgumentCaptor<OtpCheckEntity> otpCaptor =
                ArgumentCaptor.forClass(OtpCheckEntity.class);

        verify(otpCheckRepository).save(otpCaptor.capture());

        OtpCheckEntity savedOtp = otpCaptor.getValue();

        assertThat(savedOtp.getUsername()).isEqualTo(USERNAME);
        assertThat(savedOtp.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(savedOtp.getEmail()).isNull();
        assertThat(savedOtp.getNewestOtp()).matches("\\d{6}");
        assertThat(savedOtp.getRetrySendOtpCount()).isEqualTo(1);
        assertThat(savedOtp.getRetryVerifyOtpCount()).isEqualTo(0);

        verify(smsServiceImpl).sendSms(eq(PHONE_NUMBER), contains(savedOtp.getNewestOtp()));
        verify(emailServiceImpl, never()).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendOtp_shouldReuseExistingOtpRecordByPhone_whenSamePhoneRequestedWithDifferentUsernameAfterCooldown() {
        OtpDTO request = phoneOtpRequest();
        request.setUserName("NewJustinBo123");

        OtpCheckEntity existingOtp = otpRecordForPhone();
        existingOtp.setUsername("OldJustinBo123");
        existingOtp.setCreatedDate(LocalDateTime.now().minusMinutes(2));
        existingOtp.setRetrySendOtpCount(1);
        existingOtp.setRetryVerifyOtpCount(2);

        mockSendRetryConfigs();
        mockOtpCooldownConfig("60000");
        mockOtpExpirationConfig("120000");
        mockConfig(PHONE_VN_PATTERN.name(), "^(0|84|\\+84)(3|5|7|8|9)\\d{7,8}$");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());
        mockErrorCode(OTP_SENT_SUCCESS, OTP.name());

        when(userRepository.findByUsernameAndActive("NewJustinBo123"))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberAndActive(PHONE_NUMBER, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername("NewJustinBo123"))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findFirstByPhoneNumber(PHONE_NUMBER))
                .thenReturn(Optional.of(existingOtp));
        when(smsRepository.findBySmsCodeAndSmsFlow(
                SmsEnum.SMS_OTP_REGISTER.getCode(),
                SmsEnum.SMS_OTP_REGISTER.getFlow().name()
        )).thenReturn(Optional.of(smsContent()));
        when(smsServiceImpl.sendSms(eq(PHONE_NUMBER), anyString()))
                .thenReturn(smsResponse(SMS_SENT_SUCCESS));

        otpService.sendOtp(request);

        assertThat(existingOtp.getUsername()).isEqualTo("NewJustinBo123");
        assertThat(existingOtp.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(existingOtp.getEmail()).isNull();
        assertThat(existingOtp.getNewestOtp()).matches("\\d{6}");
        assertThat(existingOtp.getRetrySendOtpCount()).isEqualTo(2);
        assertThat(existingOtp.getRetryVerifyOtpCount()).isEqualTo(0);

        verify(otpCheckRepository).findFirstByPhoneNumber(PHONE_NUMBER);
        verify(otpCheckRepository).save(existingOtp);
        verify(smsServiceImpl).sendSms(eq(PHONE_NUMBER), contains(existingOtp.getNewestOtp()));
    }

    @Test
    void sendOtp_shouldThrowOtpBlockedOrNotFound_whenSameUsernameSwitchesFromBlockedEmailOtpToPhoneOtp() {
        OtpDTO request = phoneOtpRequest();

        OtpCheckEntity existingOtp = otpRecordForEmail();
        existingOtp.setBlock(true);
        existingOtp.setOtpRestrictedTime(LocalDateTime.now().plusMinutes(10));

        mockSendRetryConfigs();
        mockConfig(PHONE_VN_PATTERN.name(), "^(0|84|\\+84)(3|5|7|8|9)\\d{7,8}$");

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberAndActive(PHONE_NUMBER, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(existingOtp));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, OTP_BLOCKED_OR_NOT_FOUND, OTP.name());

        verify(smsServiceImpl, never()).sendSms(anyString(), anyString());
        verify(otpCheckRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // sendOtp() - Existing user destination validation
    // -------------------------------------------------------------------------

    @Test
    void sendOtp_shouldSendEmailOtp_whenExistingUserEmailMatches() {
        OtpDTO request = emailOtpRequest();
        User existingUser = activeUser();
        existingUser.setEmail(EMAIL_ADDRESS);

        mockSendRetryConfigs();
        mockOtpExpirationConfig("120000");
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        mockConfig(EMAIL_ADDRESS_CONFIG.name(), "noreply@wandermate.com");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());
        mockErrorCode(OTP_SENT_SUCCESS, OTP.name());

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(existingUser));
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());
        when(emailRepository.findByEmailEnum(EmailEnum.EMAIL_OTP_REGISTER))
                .thenReturn(Optional.of(emailContent()));

        CompleteResponse<Object> response = otpService.sendOtp(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(OTP_SENT_SUCCESS.getCode());

        verify(userRepository, never()).findByEmailAndActive(anyString(), anyBoolean());
        verify(emailServiceImpl).sendEmail(anyString(), eq(EMAIL_ADDRESS), anyString(), anyString());
    }

    @Test
    void sendOtp_shouldThrowOtpEmailNotMatch_whenExistingUserEmailDoesNotMatch() {
        OtpDTO request = emailOtpRequest();
        request.setEmail("wrong@example.com");

        User existingUser = activeUser();
        existingUser.setEmail(EMAIL_ADDRESS);

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(existingUser));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, OTP_EMAIL_NOT_MATCH, OTP.name());

        verify(otpCheckRepository, never()).save(any());
        verify(emailServiceImpl, never()).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendOtp_shouldThrowOtpPhoneNotMatch_whenExistingUserPhoneDoesNotMatch() {
        OtpDTO request = phoneOtpRequest();
        request.setPhoneNumber("0499999999");

        User existingUser = activeUser();
        existingUser.setPhoneNumber(PHONE_NUMBER);

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.of(existingUser));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, OTP_PHONE_NOT_MATCH, OTP.name());

        verify(otpCheckRepository, never()).save(any());
        verify(smsServiceImpl, never()).sendSms(anyString(), anyString());
    }

    // -------------------------------------------------------------------------
    // sendOtp() - Registration duplicate checks
    // -------------------------------------------------------------------------

    @Test
    void sendOtp_shouldThrowEmailTaken_whenNewUserEmailAlreadyExists() {
        OtpDTO request = emailOtpRequest();

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true))
                .thenReturn(Optional.of(activeUser()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, EMAIL_TAKEN, REGISTER.name());

        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void sendOtp_shouldThrowPhoneNumberTaken_whenNewUserPhoneAlreadyExists() {
        OtpDTO request = phoneOtpRequest();

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberAndActive(PHONE_NUMBER, true))
                .thenReturn(Optional.of(activeUser()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, PHONE_NUMBER_TAKEN, REGISTER.name());

        verify(otpCheckRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // sendOtp() - Retry and restriction logic
    // -------------------------------------------------------------------------

    @Test
    void sendOtp_shouldThrowMaxOtpRetryAndBlockOtpRecord_whenRetrySendCountReachedMax() {
        OtpDTO request = emailOtpRequest();

        OtpCheckEntity existingOtp = otpRecordForEmail();
        existingOtp.setRetrySendOtpCount(3);
        existingOtp.setBlock(false);

        mockSendRetryConfigs();
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(existingOtp));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, MAX_OTP_RETRY, OTP.name());

        assertThat(existingOtp.isBlock()).isTrue();
        assertThat(existingOtp.getOtpRestrictedTime()).isNotNull();

        verify(otpCheckRepository).save(existingOtp);
        verify(emailServiceImpl, never()).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendOtp_shouldThrowOtpBlockedOrNotFound_whenOtpRecordIsBlockedAndRestrictionNotExpired() {
        OtpDTO request = emailOtpRequest();

        OtpCheckEntity existingOtp = otpRecordForEmail();
        existingOtp.setBlock(true);
        existingOtp.setOtpRestrictedTime(LocalDateTime.now().plusMinutes(10));

        mockSendRetryConfigs();
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(existingOtp));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, OTP_BLOCKED_OR_NOT_FOUND, OTP.name());

        verify(emailServiceImpl, never()).sendEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendOtp_shouldRemoveRestrictionAndSendOtp_whenBlockedRestrictionExpired() {
        OtpDTO request = emailOtpRequest();

        OtpCheckEntity existingOtp = otpRecordForEmail();
        existingOtp.setBlock(true);
        existingOtp.setRetrySendOtpCount(3);
        existingOtp.setRetryVerifyOtpCount(2);
        existingOtp.setOtpRestrictedTime(LocalDateTime.now().minusMinutes(1));

        mockSendRetryConfigs();
        mockOtpExpirationConfig("120000");
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        mockConfig(EMAIL_ADDRESS_CONFIG.name(), "noreply@wandermate.com");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());
        mockErrorCode(OTP_SENT_SUCCESS, OTP.name());

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(existingOtp));
        when(emailRepository.findByEmailEnum(EmailEnum.EMAIL_OTP_REGISTER))
                .thenReturn(Optional.of(emailContent()));

        CompleteResponse<Object> response = otpService.sendOtp(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(OTP_SENT_SUCCESS.getCode());

        assertThat(existingOtp.isBlock()).isFalse();
        assertThat(existingOtp.getOtpRestrictedTime()).isNull();
        assertThat(existingOtp.getRetryVerifyOtpCount()).isEqualTo(0);

        verify(emailServiceImpl).sendEmail(anyString(), eq(EMAIL_ADDRESS), anyString(), anyString());
        verify(otpCheckRepository, atLeastOnce()).save(existingOtp);
    }

    // -------------------------------------------------------------------------
    // sendOtp() - Email/SMS config failures
    // -------------------------------------------------------------------------

    @Test
    void sendOtp_shouldThrowConfigNotFound_whenEmailContentConfigMissing() {
        OtpDTO request = emailOtpRequest();

        mockSendRetryConfigs();
        mockOtpExpirationConfig("120000");
        mockConfig(EMAIL_PATTERN.name(), "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActive(EMAIL_ADDRESS, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());
        when(emailRepository.findByEmailEnum(EmailEnum.EMAIL_OTP_REGISTER))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, CONFIG_NOT_FOUND, OTP.name());

        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void sendOtp_shouldThrowSmsNotConfig_whenSmsContentConfigMissing() {
        OtpDTO request = phoneOtpRequest();

        mockSendRetryConfigs();
        mockOtpExpirationConfig("120000");
        mockConfig(PHONE_VN_PATTERN.name(), "^(0|\\+84)[0-9]{9,10}$");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberAndActive(PHONE_NUMBER, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());
        when(smsRepository.findBySmsCodeAndSmsFlow(
                SmsEnum.SMS_OTP_REGISTER.getCode(),
                SmsEnum.SMS_OTP_REGISTER.getFlow().name()
        )).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, SMS_NOT_CONFIG, OTP.name());

        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void sendOtp_shouldThrowSmsSentFail_whenSmsServiceReturnsFailureCode() {
        OtpDTO request = phoneOtpRequest();

        mockSendRetryConfigs();
        mockOtpExpirationConfig("120000");
        mockConfig(PHONE_VN_PATTERN.name(), "^(0|\\+84)[0-9]{9,10}$");
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());

        when(userRepository.findByUsernameAndActive(USERNAME))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberAndActive(PHONE_NUMBER, true))
                .thenReturn(Optional.empty());
        when(otpCheckRepository.findByUsername(USERNAME))
                .thenReturn(Optional.empty());
        when(smsRepository.findBySmsCodeAndSmsFlow(
                SmsEnum.SMS_OTP_REGISTER.getCode(),
                SmsEnum.SMS_OTP_REGISTER.getFlow().name()
        )).thenReturn(Optional.of(smsContent()));
        when(smsServiceImpl.sendSms(eq(PHONE_NUMBER), anyString()))
                .thenReturn(smsResponse(SMS_SENT_FAIL));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, SMS_SENT_FAIL, OTP.name());

        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void sendOtp_shouldRethrowBusinessExceptionFromValidator() {
        OtpDTO request = emailOtpRequest();

        doThrow(new BusinessException(OTP_METHOD_MISSING, OTP.name()))
                .when(otpValidator)
                .validateOtpRequest(request);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, OTP_METHOD_MISSING, OTP.name());

        verify(userRepository, never()).findByUsernameAndActive(anyString());
        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void sendOtp_shouldWrapUnexpectedExceptionAsInternalServerError() {
        OtpDTO request = emailOtpRequest();

        doThrow(new RuntimeException("Unexpected validator failure"))
                .when(otpValidator)
                .validateOtpRequest(request);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.sendOtp(request)
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, OTP.name());

        verify(otpCheckRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // generateVerificationOtp()
    // -------------------------------------------------------------------------

    @Test
    void generateVerificationOtp_shouldReturnSixDigitOtp() {
        mockErrorCode(OTP_CREATED_SUCCESS, OTP.name());

        CompleteResponse<Object> response = otpService.generateVerificationOtp();

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(OTP_CREATED_SUCCESS.getCode());

        int otp = Integer.parseInt(response.getResponseBody().getBody().toString());

        assertThat(otp).isBetween(100000, 999999);
    }

    // -------------------------------------------------------------------------
    // verifyOtp()
    // -------------------------------------------------------------------------

    @Test
    void verifyOtp_shouldReturnSuccess_whenOtpMatchesEmailRecord() {
        OtpDTO request = verifyEmailOtpRequest("123456");

        OtpCheckEntity otpRecord = otpRecordForEmail();
        otpRecord.setNewestOtp("123456");
        otpRecord.setOtpExpirationTime(LocalDateTime.now().plusMinutes(2));

        mockVerifyConfigs();
        mockErrorCode(OTP_VERIFICATION_SUCCESS, OTP.name());

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.of(otpRecord));

        CompleteResponse<Object> response = otpService.verifyOtp(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(OTP_VERIFICATION_SUCCESS.getCode());

        verify(otpCheckRepository).delete(otpRecord);
        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void verifyOtp_shouldReturnSuccess_whenOtpMatchesPhoneRecord() {
        OtpDTO request = verifyPhoneOtpRequest("123456");

        OtpCheckEntity otpRecord = otpRecordForPhone();
        otpRecord.setNewestOtp("123456");
        otpRecord.setOtpExpirationTime(LocalDateTime.now().plusMinutes(2));

        mockVerifyConfigs();
        mockErrorCode(OTP_VERIFICATION_SUCCESS, OTP.name());

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.of(otpRecord));

        CompleteResponse<Object> response = otpService.verifyOtp(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(OTP_VERIFICATION_SUCCESS.getCode());

        verify(otpCheckRepository).delete(otpRecord);
        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void verifyOtp_shouldThrowOtpBlockedOrNotFound_whenNoActiveOtpRecordExists() {
        OtpDTO request = verifyEmailOtpRequest("123456");

        mockVerifyConfigs();

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.verifyOtp(request)
        );

        assertBusinessException(exception, OTP_BLOCKED_OR_NOT_FOUND, OTP.name());
    }

    @Test
    void verifyOtp_shouldThrowOtpEmailNotMatch_whenEmailDestinationDoesNotMatch() {
        OtpDTO request = verifyEmailOtpRequest("123456");
        request.setEmail("wrong@example.com");

        OtpCheckEntity otpRecord = otpRecordForEmail();
        otpRecord.setNewestOtp("123456");
        otpRecord.setOtpExpirationTime(LocalDateTime.now().plusMinutes(2));

        mockVerifyConfigs();

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.of(otpRecord));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.verifyOtp(request)
        );

        assertBusinessException(exception, OTP_EMAIL_NOT_MATCH, OTP.name());

        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void verifyOtp_shouldThrowOtpPhoneNotMatch_whenPhoneDestinationDoesNotMatch() {
        OtpDTO request = verifyPhoneOtpRequest("123456");
        request.setPhoneNumber("0499999999");

        OtpCheckEntity otpRecord = otpRecordForPhone();
        otpRecord.setNewestOtp("123456");
        otpRecord.setOtpExpirationTime(LocalDateTime.now().plusMinutes(2));

        mockVerifyConfigs();

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.of(otpRecord));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.verifyOtp(request)
        );

        assertBusinessException(exception, OTP_PHONE_NOT_MATCH, OTP.name());

        verify(otpCheckRepository, never()).save(any());
    }

    @Test
    void verifyOtp_shouldThrowVerificationOtpExpiredAndIncrementRetry_whenOtpIsExpired() {
        OtpDTO request = verifyEmailOtpRequest("123456");

        OtpCheckEntity otpRecord = otpRecordForEmail();
        otpRecord.setNewestOtp("123456");
        otpRecord.setRetryVerifyOtpCount(0);
        otpRecord.setOtpExpirationTime(LocalDateTime.now().minusMinutes(1));

        mockVerifyConfigs();

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.of(otpRecord));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.verifyOtp(request)
        );

        assertBusinessException(exception, VERIFICATION_OTP_EXPIRED, OTP.name());

        verify(otpFailureAccountingService)
                .recordFailedVerification(otpRecord.getOtpCheckId(), 3, 900000L);
        verify(otpCheckRepository, never()).save(otpRecord);
    }

    @Test
    void verifyOtp_shouldThrowOtpCodeNotCorrectAndIncrementRetry_whenOtpDoesNotMatch() {
        OtpDTO request = verifyEmailOtpRequest("999999");

        OtpCheckEntity otpRecord = otpRecordForEmail();
        otpRecord.setNewestOtp("123456");
        otpRecord.setRetryVerifyOtpCount(0);
        otpRecord.setOtpExpirationTime(LocalDateTime.now().plusMinutes(2));

        mockVerifyConfigs();

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.of(otpRecord));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.verifyOtp(request)
        );

        assertBusinessException(exception, OTP_CODE_NOT_CORRECT, OTP.name());

        verify(otpFailureAccountingService)
                .recordFailedVerification(otpRecord.getOtpCheckId(), 3, 900000L);
        verify(otpCheckRepository, never()).save(otpRecord);
    }

    @Test
    void verifyOtp_shouldBlockOtpRecord_whenWrongOtpReachesMaxVerifyRetry() {
        OtpDTO request = verifyEmailOtpRequest("999999");

        OtpCheckEntity otpRecord = otpRecordForEmail();
        otpRecord.setNewestOtp("123456");
        otpRecord.setRetryVerifyOtpCount(2);
        otpRecord.setOtpExpirationTime(LocalDateTime.now().plusMinutes(2));

        mockVerifyConfigs();

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.of(otpRecord));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.verifyOtp(request)
        );

        assertBusinessException(exception, OTP_CODE_NOT_CORRECT, OTP.name());

        verify(otpFailureAccountingService)
                .recordFailedVerification(otpRecord.getOtpCheckId(), 3, 900000L);
        verify(otpCheckRepository, never()).save(otpRecord);
    }

    @Test
    void verifyOtp_shouldBlockOtpRecord_whenExpiredOtpReachesMaxVerifyRetry() {
        OtpDTO request = verifyEmailOtpRequest("123456");

        OtpCheckEntity otpRecord = otpRecordForEmail();
        otpRecord.setNewestOtp("123456");
        otpRecord.setRetryVerifyOtpCount(2);
        otpRecord.setOtpExpirationTime(LocalDateTime.now().minusMinutes(1));

        mockVerifyConfigs();

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.of(otpRecord));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.verifyOtp(request)
        );

        assertBusinessException(exception, VERIFICATION_OTP_EXPIRED, OTP.name());

        verify(otpFailureAccountingService)
                .recordFailedVerification(otpRecord.getOtpCheckId(), 3, 900000L);
        verify(otpCheckRepository, never()).save(otpRecord);
    }

    @Test
    void verifyOtp_shouldRethrowBusinessExceptionFromValidator() {
        OtpDTO request = verifyEmailOtpRequest("123456");

        doThrow(new BusinessException(OTP_BLOCKED_OR_NOT_FOUND, OTP.name()))
                .when(otpValidator)
                .validateVerifyOtpRequest(request);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.verifyOtp(request)
        );

        assertBusinessException(exception, OTP_BLOCKED_OR_NOT_FOUND, OTP.name());

        verify(otpCheckRepository, never()).findByUsernameAndBlock(anyString(), anyBoolean());
    }

    @Test
    void verifyOtp_shouldThrowOtpVerificationFail_whenUnexpectedExceptionOccurs() {
        OtpDTO request = verifyEmailOtpRequest("123456");

        doThrow(new RuntimeException("Unexpected validator failure"))
                .when(otpValidator)
                .validateVerifyOtpRequest(request);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otpService.verifyOtp(request)
        );

        assertBusinessException(exception, OTP_VERIFICATION_FAIL, OTP.name());
    }

    @Test
    void verifyOtp_shouldConsumeOtpRecord_whenOtpMatchesEmailRecord() {
        OtpDTO request = verifyEmailOtpRequest("123456");

        OtpCheckEntity otpRecord = otpRecordForEmail();
        otpRecord.setNewestOtp("123456");
        otpRecord.setOtpExpirationTime(LocalDateTime.now().plusMinutes(2));

        mockVerifyConfigs();
        mockErrorCode(OTP_VERIFICATION_SUCCESS, OTP.name());

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.of(otpRecord));

        CompleteResponse<Object> response = otpService.verifyOtp(request);

        assertThat(response.getResponseBody().getCode())
                .isEqualTo(OTP_VERIFICATION_SUCCESS.getCode());

        verify(otpCheckRepository).delete(otpRecord);
        verify(otpCheckRepository, never()).save(any(OtpCheckEntity.class));
    }

    @Test
    void verifyOtp_shouldThrowOtpBlockedOrNotFound_whenConsumedOtpIsVerifiedAgain() {
        OtpDTO request = verifyEmailOtpRequest("123456");

        OtpCheckEntity otpRecord = otpRecordForEmail();
        otpRecord.setNewestOtp("123456");
        otpRecord.setOtpExpirationTime(LocalDateTime.now().plusMinutes(2));

        mockVerifyConfigs();
        mockErrorCode(OTP_VERIFICATION_SUCCESS, OTP.name());

        when(otpCheckRepository.findByUsernameAndBlock(USERNAME, false))
                .thenReturn(Optional.of(otpRecord))
                .thenReturn(Optional.empty());

        CompleteResponse<Object> firstResponse = otpService.verifyOtp(request);

        assertThat(firstResponse.getResponseBody().getCode())
                .isEqualTo(OTP_VERIFICATION_SUCCESS.getCode());

        verify(otpCheckRepository).delete(otpRecord);

        BusinessException secondException = assertThrows(
                BusinessException.class,
                () -> otpService.verifyOtp(request)
        );

        assertBusinessException(secondException, OTP_BLOCKED_OR_NOT_FOUND, OTP.name());

        verify(otpCheckRepository, times(2))
                .findByUsernameAndBlock(USERNAME, false);
        verify(otpCheckRepository, times(1)).delete(otpRecord);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private OtpDTO emailOtpRequest() {
        OtpDTO request = new OtpDTO();
        request.setUserName(USERNAME);
        request.setOtpVerificationMethod(EMAIL_OTP.name());
        request.setEmail(EMAIL_ADDRESS);
        request.setEmailEnum(EmailEnum.EMAIL_OTP_REGISTER);
        return request;
    }

    private OtpDTO phoneOtpRequest() {
        OtpDTO request = new OtpDTO();
        request.setUserName(USERNAME);
        request.setOtpVerificationMethod(PHONE_NUM_OTP.name());
        request.setPhoneNumber(PHONE_NUMBER);
        request.setSmsEnum(SmsEnum.SMS_OTP_REGISTER);
        return request;
    }

    private OtpDTO verifyEmailOtpRequest(String otp) {
        OtpDTO request = new OtpDTO();
        request.setUserName(USERNAME);
        request.setOtp(otp);
        request.setEmail(EMAIL_ADDRESS);
        return request;
    }

    private OtpDTO verifyPhoneOtpRequest(String otp) {
        OtpDTO request = new OtpDTO();
        request.setUserName(USERNAME);
        request.setOtp(otp);
        request.setPhoneNumber(PHONE_NUMBER);
        return request;
    }

    private User activeUser() {
        User user = new User();
        user.setUsername(USERNAME);
        user.setEmail(EMAIL_ADDRESS);
        user.setPhoneNumber(PHONE_NUMBER);
        user.setActive(true);
        return user;
    }

    private OtpCheckEntity otpRecordForEmail() {
        OtpCheckEntity entity = new OtpCheckEntity();
        entity.setOtpCheckId(1);
        entity.setUsername(USERNAME);
        entity.setEmail(EMAIL_ADDRESS);
        entity.setPhoneNumber(null);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setNewestOtp("123456");
        entity.setOtpExpirationTime(LocalDateTime.now().plusMinutes(2));
        entity.setRetrySendOtpCount(0);
        entity.setRetryVerifyOtpCount(0);
        entity.setBlock(false);
        entity.setOtpRestrictedTime(null);
        return entity;
    }

    private OtpCheckEntity otpRecordForPhone() {
        OtpCheckEntity entity = new OtpCheckEntity();
        entity.setOtpCheckId(2);
        entity.setUsername(USERNAME);
        entity.setEmail(null);
        entity.setPhoneNumber(PHONE_NUMBER);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setNewestOtp("123456");
        entity.setOtpExpirationTime(LocalDateTime.now().plusMinutes(2));
        entity.setRetrySendOtpCount(0);
        entity.setRetryVerifyOtpCount(0);
        entity.setBlock(false);
        entity.setOtpRestrictedTime(null);
        return entity;
    }

    private EmailContentEntity emailContent() {
        EmailContentEntity entity = new EmailContentEntity();
        entity.setEmailEnum(EmailEnum.EMAIL_OTP_REGISTER);
        entity.setEmailSubject("Your OTP expires in {expire_time} minutes");
        entity.setEmailContent("Hi {name}, your OTP is {otp}. It expires in {expire_time} minutes.");
        return entity;
    }

    private SmsEntity smsContent() {
        SmsEntity entity = new SmsEntity();
        entity.setSmsCode(SmsEnum.SMS_OTP_REGISTER.getCode());
        entity.setSmsFlow(SmsEnum.SMS_OTP_REGISTER.getFlow().name());
        entity.setSmsContent("Your OTP is {otp}");
        return entity;
    }

    private ResponseBody<String> smsResponse(ErrorCodeEnum errorCodeEnum) {
        return new ResponseBody<>(
                errorCodeEnum.getCode(),
                errorCodeEnum.getMessage(),
                SMS.name(),
                null
        );
    }

    private void mockSendRetryConfigs() {
        mockConfig(MAX_RETRY_SEND_OTP.name(), "3");
        mockConfig(OTP_RESTRICTED_TIME.name(), "900000");
    }

    private void mockOtpCooldownConfig(String value) {
        mockConfig(OTP_RETRY_COOLDOWN.name(), value);
    }

    private void mockVerifyConfigs() {
        mockConfig(MAX_RETRY_VERIFY_OTP.name(), "3");
        mockConfig(OTP_RESTRICTED_TIME.name(), "900000");
    }

    private void mockOtpExpirationConfig(String value) {
        mockConfig(OTP_EXPIRATION_TIME.name(), value);
    }

    private void mockConfig(String configCode, String configValue) {
        ConfigurationEntity entity = new ConfigurationEntity();
        entity.setConfigCode(configCode);
        entity.setConfigValue(configValue);
        entity.setCreatedDate(LocalDateTime.now());

        when(configurationRepository.findByConfigCode(configCode))
                .thenReturn(Optional.of(entity));
    }

    private void mockErrorCode(ErrorCodeEnum errorCodeEnum, String flow) {
        ErrorCodeEntity entity = new ErrorCodeEntity();
        entity.setErrorCode(errorCodeEnum.getCode());
        entity.setErrorMessage(errorCodeEnum.getMessage());
        entity.setErrorEnum(errorCodeEnum.name());
        entity.setFlow(flow);
        entity.setCreatedDate(LocalDateTime.now());

        when(errorCodeRepository.findByErrorEnumAndFlow(errorCodeEnum.name(), flow))
                .thenReturn(Optional.of(entity));
    }

    private void assertBusinessException(
            BusinessException exception,
            ErrorCodeEnum expectedErrorCode,
            String expectedFlow
    ) {
        assertThat(exception.getErrorCodeEnum()).isEqualTo(expectedErrorCode);
        assertThat(exception.getFlow()).isEqualTo(expectedFlow);
    }
}
