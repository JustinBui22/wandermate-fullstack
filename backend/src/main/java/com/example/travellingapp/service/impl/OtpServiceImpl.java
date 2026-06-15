package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.OtpDTO;
import com.example.travellingapp.entity.*;
import com.example.travellingapp.enums.EmailEnum;
import com.example.travellingapp.enums.SmsEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.*;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.service.OtpService;
import com.example.travellingapp.validator.OtpValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.travellingapp.util.Common.getConfigValue;
import static com.example.travellingapp.util.Common.getEmailConfig;
import static com.example.travellingapp.util.DataConverter.convertStringToInt;
import static com.example.travellingapp.util.DataConverter.convertStringToLong;
import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Log4j2
@Service

public class OtpServiceImpl implements OtpService {
    private final EmailServiceImpl emailServiceImpl;
    private final ErrorCodeRepository errorCodeRepository;
    private final SmsServiceImpl smsServiceImpl;
    private final SmsRepository smsRepository;
    private final EmailRepository emailRepository;
    private final ConfigurationRepository configurationRepository;
    private final OtpCheckRepository otpCheckRepository;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();
    private final OtpValidator otpValidator;


    public OtpServiceImpl(EmailServiceImpl emailServiceImpl, ErrorCodeRepository errorCodeRepository, SmsServiceImpl smsServiceImpl, SmsRepository smsRepository, EmailRepository emailRepository, ConfigurationRepository configurationRepository, OtpCheckRepository otpCheckRepository, UserRepository userRepository, OtpValidator otpValidator) {
        this.emailServiceImpl = emailServiceImpl;
        this.errorCodeRepository = errorCodeRepository;
        this.smsServiceImpl = smsServiceImpl;
        this.smsRepository = smsRepository;
        this.emailRepository = emailRepository;
        this.configurationRepository = configurationRepository;
        this.otpCheckRepository = otpCheckRepository;
        this.userRepository = userRepository;
        this.otpValidator = otpValidator;
    }

    @Override
    public CompleteResponse<Object> sendOtp(OtpDTO otpDTO) {
        try {
            // Validate common input for sending otp request
            otpValidator.validateOtpRequest(otpDTO);
            // Check if user already existed to send otp for
            Optional<User> existingUserOptional = userRepository.findByUsernameAndActive(otpDTO.getUserName());

            if (existingUserOptional.isPresent()) {
                log.info("User {} is found to send OTP for!", otpDTO.getUserName());
                validateOtpDestinationBelongsToExistingUser(otpDTO, existingUserOptional.get());
            } else {
                log.info("New user with username {} to send OTP for!", otpDTO.getUserName());
                validateOtpDestinationAvailableForRegistration(otpDTO);
            }
            OtpCheckEntity otpCheckEntity = getOrCreateOtpCheckEntity(otpDTO);
            // Validate the OTP retry count and restriction
            validateOtpRetryOrRestriction(otpDTO, otpCheckEntity);
            sendOtpCommonFlow(otpDTO, otpCheckEntity);
            return getCompleteResponse(errorCodeRepository, OTP_SENT_SUCCESS, OTP.name(), null);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in sending otp!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, OTP.name());
        }
    }

    private void validateOtpRetryOrRestriction(OtpDTO otpDTO, OtpCheckEntity otpCheckEntity) {
        try {
            int maxRetrySendOtp = convertStringToInt(getConfigValue(MAX_RETRY_SEND_OTP.name(), configurationRepository, "3"));
            long restrictedOtpDuration = convertStringToLong(getConfigValue(OTP_RESTRICTED_TIME.name(), configurationRepository, "900000"));

            // Check if the OTP record is currently blocked/restricted
            if (otpCheckEntity.isBlock()) {
                // If the restricted time is empty or already expired, remove the restriction
                if (otpCheckEntity.getOtpRestrictedTime() == null || otpCheckEntity.getOtpRestrictedTime().isBefore(LocalDateTime.now())) {
                    log.info("OTP restriction removed for user {}!", otpDTO.getUserName());
                    otpCheckEntity.setOtpRestrictedTime(null);
                    otpCheckEntity.setBlock(false);
                    otpCheckEntity.setRetrySendOtpCount(0);
                    otpCheckEntity.setRetryVerifyOtpCount(0);
                    otpCheckRepository.save(otpCheckEntity);
                    return;
                }
                // The OTP restriction has not expired yet, so the user must wait before requesting another OTP
                log.error("Time restricted from sending otp is not expired for user {}!", otpDTO.getUserName());
                throw new BusinessException(OTP_BLOCKED_OR_NOT_FOUND, OTP.name());
            }

            // Check if the user has already reached the maximum OTP retry/send limit
            if (otpCheckEntity.getRetrySendOtpCount() >= maxRetrySendOtp) {
                log.error("User {} has exceeded max retry count of sending OTP", otpDTO.getUserName());
                otpCheckEntity.setBlock(true);
                // Calculate when the OTP restriction will expire
                LocalDateTime restrictedOtpTime = LocalDateTime.now().plusSeconds(restrictedOtpDuration / 1000);
                otpCheckEntity.setOtpRestrictedTime(restrictedOtpTime);
                otpCheckRepository.save(otpCheckEntity);
                throw new BusinessException(MAX_OTP_RETRY, OTP.name());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in validating OTP retry or restriction!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, OTP.name());
        }
    }

    private OtpCheckEntity getOrCreateOtpCheckEntity(OtpDTO otpDTO) {
        // Check if the user has requested to send OTP before to determine if create new OTP check entity or update the existing one
        return otpCheckRepository
                .findByUsername(otpDTO.getUserName())
                .orElseGet(() -> {
                    log.info("Create new OtpCheck entity for user {}!", otpDTO.getUserName());
                    return new OtpCheckEntity(
                            otpDTO.getUserName(),
                            otpDTO.getEmail(),
                            LocalDateTime.now(),
                            otpDTO.getPhoneNumber(),
                            0,
                            0,
                            null,
                            false
                    );
                });
    }

    private void validateOtpDestinationBelongsToExistingUser(OtpDTO otpDTO, User user) {
        if (EMAIL_OTP.name().equals(otpDTO.getOtpVerificationMethod())) {
            if (user.getEmail() == null || otpDTO.getEmail() == null || !user.getEmail().equalsIgnoreCase(otpDTO.getEmail())) {
                log.error("OTP email does not match registered email for user {}", otpDTO.getUserName());
                throw new BusinessException(OTP_EMAIL_NOT_MATCH, OTP.name());
            }
            return;
        }
        if (PHONE_NUM_OTP.name().equals(otpDTO.getOtpVerificationMethod()) && (user.getPhoneNumber() == null || otpDTO.getPhoneNumber() == null || !user.getPhoneNumber().equals(otpDTO.getPhoneNumber()))) {
            log.error("OTP phone number does not match registered phone number for user {}", otpDTO.getUserName());
            throw new BusinessException(OTP_PHONE_NOT_MATCH, OTP.name());
        }
    }

    private void validateOtpDestinationAvailableForRegistration(OtpDTO otpDTO) {
        if (EMAIL_OTP.name().equals(otpDTO.getOtpVerificationMethod())) {
            // Validate email otp request
            otpValidator.validateEmailOtpRequest(otpDTO, configurationRepository.findByConfigCode(EMAIL_PATTERN.name()));
            if (userRepository.findByEmailAndActive(otpDTO.getEmail(), true).isPresent()) {
                log.error("Email {} is already taken!", otpDTO.getEmail());
                throw new BusinessException(EMAIL_TAKEN, REGISTER.name());
            }
            return;
        }

        if (PHONE_NUM_OTP.name().equals(otpDTO.getOtpVerificationMethod())) {
            // Validate phone otp request
            otpValidator.validatePhoneOtpRequest(otpDTO, configurationRepository.findByConfigCode(PHONE_VN_PATTERN.name()));
            if (userRepository.findByPhoneNumberAndActive(otpDTO.getPhoneNumber(), true).isPresent()) {
                log.error("Phone number {} is already linked to an active user!", otpDTO.getPhoneNumber());
                throw new BusinessException(PHONE_NUMBER_TAKEN, REGISTER.name());
            }
        }
    }

    private void sendOtpCommonFlow(OtpDTO otpDTO, OtpCheckEntity otpCheckEntity) {
        String otpCode = String.valueOf(generateVerificationOtp().getResponseBody().getBody());
        long expirationOtpDuration = convertStringToLong(getConfigValue(OTP_EXPIRATION_TIME.name(), configurationRepository, "120000"));
        if (otpDTO.getOtpVerificationMethod().equals(PHONE_NUM_OTP.name())) {
            sendPhoneOtp(otpDTO, otpCode, otpDTO.getSmsEnum());
            otpCheckEntity.setPhoneNumber(otpDTO.getPhoneNumber());
            otpCheckEntity.setEmail(null);
        } else if (otpDTO.getOtpVerificationMethod().equals(EMAIL_OTP.name())) {
            sendEmailOtp(otpDTO, otpCode, otpDTO.getEmailEnum(), expirationOtpDuration);
            otpCheckEntity.setEmail(otpDTO.getEmail());
            otpCheckEntity.setPhoneNumber(null);
        } else {
            log.error("Unsupported OTP verification method {}", otpDTO.getOtpVerificationMethod());
            throw new BusinessException(INVALID_INPUT, OTP.name());
        }
        otpCheckEntity.setNewestOtp(otpCode);
        otpCheckEntity.setCreatedDate(LocalDateTime.now());
        LocalDateTime expirationOtpTime = otpCheckEntity.getCreatedDate().plusSeconds(expirationOtpDuration / 1000);
        otpCheckEntity.setOtpExpirationTime(expirationOtpTime);
        // New OTP means previous wrong verification attempts should reset
        otpCheckEntity.setRetryVerifyOtpCount(0);
        otpCheckEntity.setRetrySendOtpCount(otpCheckEntity.getRetrySendOtpCount() + 1);
        otpCheckRepository.save(otpCheckEntity);
    }

    private void sendEmailOtp(OtpDTO otpDTO, String generatedOtp, EmailEnum emailEnum, long emailOtpExpirationTime) {
        // Validate email otp request
        otpValidator.validateEmailOtpRequest(
                otpDTO,
                configurationRepository.findByConfigCode(EMAIL_PATTERN.name())
        );
        // Send verification code through email
        try {
            Optional<EmailContentEntity> emailContentOptional = emailRepository.findByEmailEnum(emailEnum);
            if (emailContentOptional.isEmpty()) {
                log.info("Config for email content not found!");
                throw new BusinessException(CONFIG_NOT_FOUND, OTP.name());
            }
            String emailSubject = emailContentOptional.get().getEmailSubject();
            String emailContent = emailContentOptional.get().getEmailContent();
            String emailSender = getEmailConfig(EMAIL_ADDRESS_CONFIG.name(), EMAIL_ADDRESS_CONFIG.name(), "demo@example.com", configurationRepository);

            emailServiceImpl.sendEmail(emailSender, otpDTO.getEmail(), emailSubject.replace("{expire_time}", String.valueOf(emailOtpExpirationTime / 60000)),
                    emailContent.replace("{name}", otpDTO.getUserName())
                            .replace("{otp}", generatedOtp)
                            .replace("{expire_time}", String.valueOf(emailOtpExpirationTime / 60000)));
            log.info("OTP email sent successfully to {}", otpDTO.getEmail());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in sending otp email!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, OTP.name());
        }
    }

    private void sendPhoneOtp(OtpDTO otpDTO, String generatedOtp, SmsEnum smsEnum) {
        // Validate phone otp request
        otpValidator.validatePhoneOtpRequest(
                otpDTO,
                configurationRepository.findByConfigCode(PHONE_VN_PATTERN.name())
        );
        // Get sms config for sms otp verification
        Optional<SmsEntity> registerSmsOptional = smsRepository.findBySmsCodeAndSmsFlow(smsEnum.getCode(), smsEnum.getFlow().name());
        if (registerSmsOptional.isPresent()) {
            String registerMessageFormat = registerSmsOptional.get().getSmsContent();
            log.info("Start sending sms {} for otp verification in {} flow !", smsEnum.name(), smsEnum.getFlow());
            String registerOtpMessage = registerMessageFormat.replace("{otp}", generatedOtp);
            if (!smsServiceImpl.sendSms(otpDTO.getPhoneNumber(), registerOtpMessage).getCode().equals(SMS_SENT_SUCCESS.getCode())) {
                log.info("OTP SMS sent failed!");
                throw new BusinessException(SMS_SENT_FAIL, OTP.name());
            }
        } else {
            log.error("There is no config for sms {} for {} flow!", smsEnum.name(), smsEnum.getFlow());
            throw new BusinessException(SMS_NOT_CONFIG, OTP.name());
        }
    }

    @Override
    public CompleteResponse<Object> generateVerificationOtp() {
        try {
            log.info("Start generating otp!");
            int otp = 100000 + random.nextInt(900000); // Ensures a 6-digit number
            return getCompleteResponse(errorCodeRepository, OTP_CREATED_SUCCESS, OTP.name(), otp);
        } catch (Exception e) {
            log.error("There has been an error in generating otp!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, REGISTER.name());
        }
    }

    private void verifyOtpFailed(int maxRetryVerifyOtp, long restrictedOtpDuration, OtpCheckEntity otpCheckEntity) {
        otpCheckEntity.setRetryVerifyOtpCount((otpCheckEntity.getRetryVerifyOtpCount() + 1));
        if (otpCheckEntity.getRetryVerifyOtpCount() >= maxRetryVerifyOtp) {
            otpCheckEntity.setBlock(true);
            LocalDateTime restrictedOtpTime = LocalDateTime.now().plusSeconds(restrictedOtpDuration / 1000);
            otpCheckEntity.setOtpRestrictedTime(restrictedOtpTime);
        }
        otpCheckRepository.save(otpCheckEntity);
    }

    @Override
    public CompleteResponse<Object> verifyOtp(OtpDTO otpDTO) {
        try {
            // Validate basic input for verifying OTP request
            otpValidator.validateVerifyOtpRequest(otpDTO);

            int maxRetryVerifyOtp = convertStringToInt(getConfigValue(MAX_RETRY_VERIFY_OTP.name(), configurationRepository, "3"));
            long restrictedOtpDuration = convertStringToLong(getConfigValue(OTP_RESTRICTED_TIME.name(), configurationRepository, "900000"));

            // Check if there is an active OTP record for this username
            Optional<OtpCheckEntity> otpCheckEntityOptional = otpCheckRepository.findByUsernameAndBlock(otpDTO.getUserName(), false);

            if (otpCheckEntityOptional.isEmpty()) {
                log.error("There is no OTP check entity for verification!");
                throw new BusinessException(OTP_BLOCKED_OR_NOT_FOUND, OTP.name());
            }

            OtpCheckEntity otpCheckEntity = otpCheckEntityOptional.get();
            // If OTP was sent by email, final verification must use the same email
            if (otpCheckEntity.getEmail() != null && (otpDTO.getEmail() == null
                    || !otpCheckEntity.getEmail().equalsIgnoreCase(otpDTO.getEmail()))) {
                log.error("OTP email destination does not match for user {}", otpDTO.getUserName());
                throw new BusinessException(OTP_EMAIL_NOT_MATCH, OTP.name());
            }

            // If OTP was sent by phone, final verification must use the same phone number
            if (otpCheckEntity.getPhoneNumber() != null && (otpDTO.getPhoneNumber() == null
                    || !otpCheckEntity.getPhoneNumber().equals(otpDTO.getPhoneNumber()))) {
                log.error("OTP phone number destination does not match for user {}", otpDTO.getUserName());
                throw new BusinessException(OTP_PHONE_NOT_MATCH, OTP.name());
            }

            // Check if OTP has expired
            if (otpCheckEntity.getOtpExpirationTime() != null
                    && LocalDateTime.now().isAfter(otpCheckEntity.getOtpExpirationTime())) {
                log.warn("Verification OTP has expired!");
                verifyOtpFailed(maxRetryVerifyOtp, restrictedOtpDuration, otpCheckEntity);
                throw new BusinessException(VERIFICATION_OTP_EXPIRED, OTP.name());
            }

            // Check if OTP code matches
            if (!otpDTO.getOtp().equals(otpCheckEntity.getNewestOtp())) {
                log.warn("Verification OTP does not match!");
                verifyOtpFailed(maxRetryVerifyOtp, restrictedOtpDuration, otpCheckEntity);
                throw new BusinessException(OTP_CODE_NOT_CORRECT, OTP.name());
            }

            // Invalidate/delete the OTP record after successful verification to prevent reuse
            otpCheckRepository.delete(otpCheckEntity);
            return getCompleteResponse(
                    errorCodeRepository,
                    OTP_VERIFICATION_SUCCESS,
                    OTP.name(),
                    null
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in verifying otp!", e);
            throw new BusinessException(OTP_VERIFICATION_FAIL, OTP.name());
        }
    }
}
