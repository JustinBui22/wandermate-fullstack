package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.request.OtpDTO;
import com.example.travellingapp.entity.*;
import com.example.travellingapp.enums.EmailEnum;
import com.example.travellingapp.enums.OtpPurpose;
import com.example.travellingapp.enums.SmsEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.*;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.service.OtpService;
import com.example.travellingapp.service.OtpFailureAccountingService;
import com.example.travellingapp.security.data_security.DataSecurity;
import com.example.travellingapp.validator.OtpValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.travellingapp.util.Common.getConfigValue;
import static com.example.travellingapp.util.Common.getEnvConfig;
import static com.example.travellingapp.util.Common.findUser;
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
    private final OtpFailureAccountingService otpFailureAccountingService;
    private final DataSecurity dataSecurity;


    public OtpServiceImpl(EmailServiceImpl emailServiceImpl, ErrorCodeRepository errorCodeRepository, SmsServiceImpl smsServiceImpl, SmsRepository smsRepository, EmailRepository emailRepository, ConfigurationRepository configurationRepository, OtpCheckRepository otpCheckRepository, UserRepository userRepository, OtpValidator otpValidator, OtpFailureAccountingService otpFailureAccountingService, DataSecurity dataSecurity) {
        this.emailServiceImpl = emailServiceImpl;
        this.errorCodeRepository = errorCodeRepository;
        this.smsServiceImpl = smsServiceImpl;
        this.smsRepository = smsRepository;
        this.emailRepository = emailRepository;
        this.configurationRepository = configurationRepository;
        this.otpCheckRepository = otpCheckRepository;
        this.userRepository = userRepository;
        this.otpValidator = otpValidator;
        this.otpFailureAccountingService = otpFailureAccountingService;
        this.dataSecurity = dataSecurity;
    }

    @Override
    public CompleteResponse<Object> sendOtp(OtpDTO otpDTO) {
        try {
            // Validate common inputs for sending otp request
            otpValidator.validateOtpRequest(otpDTO);
            boolean passwordReset = otpDTO.getPurpose() == OtpPurpose.PASSWORD_RESET;
            Optional<User> existingUserOptional = passwordReset
                    ? findUser(otpDTO.getUserName().trim(), configurationRepository, userRepository)
                    : userRepository.findByUsernameAndActive(otpDTO.getUserName());

            // Password recovery must not reveal whether an account exists or
            // whether a supplied destination belongs to it. Return the same
            // success envelope, but send nothing when the details do not match.
            if (passwordReset && existingUserOptional.isEmpty()) {
                log.warn("Password-reset OTP requested for unmatched account details");
                return getCompleteResponse(errorCodeRepository, OTP_SENT_SUCCESS, OTP.name(), null);
            }

            if (existingUserOptional.isPresent()) {
                log.info("User {} is found to send OTP for!", otpDTO.getUserName());
                if (passwordReset && !otpDestinationBelongsToUser(otpDTO, existingUserOptional.get())) {
                    log.warn("Password-reset OTP destination did not match the account");
                    return getCompleteResponse(errorCodeRepository, OTP_SENT_SUCCESS, OTP.name(), null);
                }
                validateOtpDestinationBelongsToExistingUser(otpDTO, existingUserOptional.get());
                otpDTO.setUserName(existingUserOptional.get().getUsername());
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
            log.error("There has been an error in the OTP send flow!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, OTP.name());
        }
    }

    private void validateOtpRetryOrRestriction(OtpDTO otpDTO, OtpCheckEntity otpCheckEntity) {
        try {
            int maxRetrySendOtp = convertStringToInt(getConfigValue(MAX_RETRY_SEND_OTP.name(), configurationRepository, "3"));
            long restrictedOtpDuration = convertStringToLong(getConfigValue(OTP_RESTRICTED_TIME.name(), configurationRepository, "900000"));

            // If the OTP record is currently blocked, restriction status must be checked before cooldown or retry count.
            if (otpCheckEntity.isBlock()) {
                // If the restriction has expired, unblock the record and allow the user to request a fresh OTP immediately.
                if (otpCheckEntity.getOtpRestrictedTime() == null || otpCheckEntity.getOtpRestrictedTime().isBefore(LocalDateTime.now())) {
                    log.info("OTP restriction expired for user {}. Resetting OTP retry state.", otpDTO.getUserName());
                    otpCheckEntity.setOtpRestrictedTime(null);
                    otpCheckEntity.setBlock(false);
                    otpCheckEntity.setRetrySendOtpCount(0);
                    otpCheckEntity.setRetryVerifyOtpCount(0);
                    otpCheckRepository.save(otpCheckEntity);
                    return;
                }

                // The OTP restriction has not expired yet, so the user must wait before requesting another OTP.
                log.error("OTP restriction has not expired yet for user {}.", otpDTO.getUserName());
                throw new BusinessException(OTP_BLOCKED_OR_NOT_FOUND, OTP.name());
            }

            // A newly-created OTP row has no actual OTP code yet, so cooldown/retry rules should not block the first send.
            if (otpCheckEntity.getNewestOtp() == null) {
                return;
            }

            if (otpCheckEntity.getCreatedDate() == null) {
                log.error("Existing OTP check entity for user {} has an OTP code but no created date.", otpDTO.getUserName());
                throw new BusinessException(INTERNAL_SERVER_ERROR, OTP.name());
            }

            // Max retry checked before cooldown so users who already hit the limit are blocked correctly.
            if (otpCheckEntity.getRetrySendOtpCount() >= maxRetrySendOtp) {
                log.error("User {} has exceeded max retry count of sending OTP.", otpDTO.getUserName());
                otpCheckEntity.setBlock(true);

                // Calculate when the OTP restriction will expire.
                LocalDateTime restrictedOtpTime = LocalDateTime.now().plusSeconds(restrictedOtpDuration / 1000);
                otpCheckEntity.setOtpRestrictedTime(restrictedOtpTime);
                otpCheckRepository.save(otpCheckEntity);

                throw new BusinessException(MAX_OTP_RETRY, OTP.name());
            }

            // Cooldown so users cannot spam OTP requests.
            long otpCooldownDuration = convertStringToLong(getConfigValue(OTP_RETRY_COOLDOWN.name(), configurationRepository, "60000"));
            long timeSinceLastOtp = java.time.Duration.between(otpCheckEntity.getCreatedDate(), LocalDateTime.now()).toMillis();

            if (timeSinceLastOtp < otpCooldownDuration) {
                log.error("OTP cooldown has not expired yet for user {}. Elapsed={}ms, required={}ms.",
                        otpDTO.getUserName(), timeSinceLastOtp, otpCooldownDuration);
                throw new BusinessException(OTP_COOLDOWN_NOT_EXPIRED, OTP.name());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("There has been an error in validating OTP retry or restriction!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, OTP.name());
        }
    }

    private OtpCheckEntity getOrCreateOtpCheckEntity(OtpDTO otpDTO) {
        Optional<OtpCheckEntity> existingOtpByUsername = otpCheckRepository.findByUsername(otpDTO.getUserName());
        if (existingOtpByUsername.isPresent()) {
            log.info("Reuse existing OtpCheck entity for username {}!", otpDTO.getUserName());
            return existingOtpByUsername.get();
        }

        // For email OTP, search by email before creating a new row to prevents duplicate email inserts and stops users bypassing cooldown by changing username.
        if (EMAIL_OTP.name().equals(otpDTO.getOtpVerificationMethod()) && otpDTO.getEmail() != null) {
            Optional<OtpCheckEntity> existingOtpByEmail = otpCheckRepository.findByEmailIgnoreCase(otpDTO.getEmail());
            if (existingOtpByEmail.isPresent()) {
                OtpCheckEntity otpCheckEntity = existingOtpByEmail.get();
                log.info("Reuse existing OtpCheck entity for email {} and update username from {} to {}!",
                        otpDTO.getEmail(), otpCheckEntity.getUsername(), otpDTO.getUserName());

                // Keep the OTP record aligned with the latest request => verifyOtp can still find it by username.
                otpCheckEntity.setUsername(otpDTO.getUserName());
                return otpCheckEntity;
            }
        }

        // For phone OTP, also search by phone number before creating a new row.
        // This stops users bypassing OTP cooldown/restriction by changing username but reusing the same phone number.
        if (PHONE_NUM_OTP.name().equals(otpDTO.getOtpVerificationMethod()) && otpDTO.getPhoneNumber() != null) {
            Optional<OtpCheckEntity> existingOtpByPhoneNumber = otpCheckRepository.findFirstByPhoneNumber(otpDTO.getPhoneNumber());
            if (existingOtpByPhoneNumber.isPresent()) {
                OtpCheckEntity otpCheckEntity = existingOtpByPhoneNumber.get();
                log.info("Reuse existing OtpCheck entity for phone number {} and update username from {} to {}!",
                        otpDTO.getPhoneNumber(), otpCheckEntity.getUsername(), otpDTO.getUserName());
                // Keep the OTP record aligned with the latest request so verifyOtp can still find it by username.
                otpCheckEntity.setUsername(otpDTO.getUserName());
                return otpCheckEntity;
            }
        }

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

    private boolean otpDestinationBelongsToUser(OtpDTO otpDTO, User user) {
        if (EMAIL_OTP.name().equals(otpDTO.getOtpVerificationMethod())) {
            return user.getEmail() != null
                    && otpDTO.getEmail() != null
                    && user.getEmail().equalsIgnoreCase(otpDTO.getEmail());
        }

        if (PHONE_NUM_OTP.name().equals(otpDTO.getOtpVerificationMethod())) {
            return user.getPhoneNumber() != null
                    && otpDTO.getPhoneNumber() != null
                    && user.getPhoneNumber().equals(otpDTO.getPhoneNumber());
        }

        return false;
    }

    private void validateOtpDestinationAvailableForRegistration(OtpDTO otpDTO) {
        if (EMAIL_OTP.name().equals(otpDTO.getOtpVerificationMethod())) {
            // Registration can continue only when the email is not already used by an active user.
            otpValidator.validateEmailOtpRequest(otpDTO, configurationRepository.findByConfigCode(EMAIL_PATTERN.name()));

            if (userRepository.findByEmailAndActive(otpDTO.getEmail(), true).isPresent()) {
                log.error("Email {} is already taken!", otpDTO.getEmail());
                throw new BusinessException(EMAIL_TAKEN, REGISTER.name());
            }

            return;
        }

        if (PHONE_NUM_OTP.name().equals(otpDTO.getOtpVerificationMethod())) {
            // Registration can continue only when the phone number is not already used by an active user.
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
        otpCheckEntity.setPurpose(otpDTO.getPurpose());
        otpCheckEntity.setNewestOtp(dataSecurity.hashOtp(otpDTO.getUserName(), otpDTO.getPurpose(), otpCode));
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
        try {
            Optional<EmailContentEntity> emailContentOptional = emailRepository.findByEmailEnum(emailEnum);
            if (emailContentOptional.isEmpty()) {
                log.error("Config for email content not found!");
                throw new BusinessException(CONFIG_NOT_FOUND, OTP.name());
            }
            String emailSubject = emailContentOptional.get().getEmailSubject();
            String emailContent = emailContentOptional.get().getEmailContent();
            String emailSender = getEnvConfig(EMAIL_ADDRESS_CONFIG.name(), EMAIL_ADDRESS_CONFIG.name(), "demo@example.com", configurationRepository);

            emailServiceImpl.sendEmail(emailSender, otpDTO.getEmail(), emailSubject.replace("{expire_time}", String.valueOf(emailOtpExpirationTime / 60000)),
                    emailContent.replace("{name}", otpDTO.getUserName())
                            .replace("{otp}", generatedOtp)
                            .replace("{expire_time}", String.valueOf(emailOtpExpirationTime / 60000)));
            log.info("OTP email sent successfully to {}", otpDTO.getEmail());
        } catch (
                BusinessException e) {
            throw e;
        } catch (
                Exception e) {
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
                log.error("OTP SMS sent failed!");
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
                otpFailureAccountingService.recordFailedVerification(
                        otpCheckEntity.getOtpCheckId(),
                        maxRetryVerifyOtp,
                        restrictedOtpDuration
                );
                throw new BusinessException(VERIFICATION_OTP_EXPIRED, OTP.name());
            }

            // Check if OTP purpose matches the purpose for which the stored OTP was issued
            if (otpCheckEntity.getPurpose() != otpDTO.getPurpose()) {
                log.warn("Verification OTP purpose does not match!");
                otpFailureAccountingService.recordFailedVerification(
                        otpCheckEntity.getOtpCheckId(),
                        maxRetryVerifyOtp,
                        restrictedOtpDuration
                );
                throw new BusinessException(OTP_CODE_NOT_CORRECT, OTP.name());
            }

            // Check if OTP code matches the stored purpose-bound HMAC hash
            if (!dataSecurity.matchesOtp(otpDTO.getUserName(), otpDTO.getPurpose(), otpDTO.getOtp(), otpCheckEntity.getNewestOtp())) {
                log.warn("Verification OTP does not match!");
                otpFailureAccountingService.recordFailedVerification(
                        otpCheckEntity.getOtpCheckId(),
                        maxRetryVerifyOtp,
                        restrictedOtpDuration
                );
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