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
    private final SecureRandom random = new SecureRandom();
    private final OtpValidator otpValidator;


    public OtpServiceImpl(EmailServiceImpl emailServiceImpl, ErrorCodeRepository errorCodeRepository, SmsServiceImpl smsServiceImpl, SmsRepository smsRepository, EmailRepository emailRepository, ConfigurationRepository configurationRepository, OtpCheckRepository otpCheckRepository, OtpValidator otpValidator) {
        this.emailServiceImpl = emailServiceImpl;
        this.errorCodeRepository = errorCodeRepository;
        this.smsServiceImpl = smsServiceImpl;
        this.smsRepository = smsRepository;
        this.emailRepository = emailRepository;
        this.configurationRepository = configurationRepository;
        this.otpCheckRepository = otpCheckRepository;
        this.otpValidator = otpValidator;
    }

    @Override
    public CompleteResponse<Object> sendOtp(OtpDTO otpDTO) {
        try {
            // Validate common input for sending otp request
            otpValidator.validateOtpRequest(otpDTO);

            Optional<OtpCheckEntity> otpCheckEntityOptional = otpCheckRepository.findByUsernameAndBlock(otpDTO.getUserName(), false);
            OtpCheckEntity otpCheckEntity;
            // Check if the user has requested to send OTP before
            if (otpCheckEntityOptional.isPresent()) {
                log.info("User {} has requested to send OTP before!", otpDTO.getUserName());
                otpCheckEntity = otpCheckEntityOptional.get();
                int maxRetryOtp = convertStringToInt(getConfigValue(MAX_RETRY_OTP.name(), configurationRepository, "3"));
                long restrictedOtpDuration = convertStringToLong(getConfigValue(OTP_RESTRICTED_TIME.name(), configurationRepository, "900000L"));

                // Check if the number of resend OTP reach the limitation
                if (otpCheckEntity.getRetryCount() >= maxRetryOtp) {
                    log.error("User {} has exceeded max retry count of otp", otpDTO.getUserName());
                    otpCheckEntity.setBlock(true);
                    LocalDateTime restrictedOtpTime = LocalDateTime.now().plusSeconds(restrictedOtpDuration / 1000);
                    otpCheckEntity.setOtpRestrictedTime(restrictedOtpTime);
                    throw new BusinessException(MAX_OTP_RETRY, OTP.name());
                }
            } else if (otpCheckRepository.findByUsernameAndBlock(otpDTO.getUserName(), true).isPresent()) {
                // The account is currently restricted from OTP sending
                otpCheckEntity = otpCheckRepository.findByUsernameAndBlock(otpDTO.getUserName(), true).get();
                if (otpCheckEntity.getOtpRestrictedTime() == null || otpCheckEntity.getOtpRestrictedTime().isBefore(LocalDateTime.now())) {
                    // The account is now removed from being restricted from OTP sending
                    log.info("OTP restriction removed for user {} !", otpDTO.getUserName());
                    otpCheckEntity.setOtpRestrictedTime(null);
                    otpCheckEntity.setBlock(false);
                    otpCheckEntity.setRetryCount(0);
                    otpCheckRepository.save(otpCheckEntity);
                } else {
                    // The account is still restricted from OTP sending
                    log.error("Time restricted from sending otp is not expired!");
                    throw new BusinessException(OTP_BLOCKED_OR_NOT_FOUND, OTP.name());
                }
            } else {
                // Customer has not sent OTP before
                log.info("Create new OtpCheck entity for user {}!", otpDTO.getUserName());
                otpCheckEntity = new OtpCheckEntity(otpDTO.getUserName(), otpDTO.getEmail(), LocalDateTime.now(), otpDTO.getPhoneNumber(), 0, null, false);
            }
            sendOtpCommonFlow(otpDTO, otpCheckEntity);
            return getCompleteResponse(errorCodeRepository, OTP_SENT_SUCCESS, OTP.name(), null);
        } catch (
                BusinessException e) {
            throw e;
        } catch (
                Exception e) {
            log.error("There has been an error in sending otp!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, OTP.name());
        }
    }

    private void sendOtpCommonFlow(OtpDTO otpDTO, OtpCheckEntity otpCheckEntity) {
        String otpCode = String.valueOf(generateVerificationOtp().getResponseBody().getBody());
        long expirationOtpDuration = convertStringToLong(getConfigValue(OTP_EXPIRATION_TIME.name(), configurationRepository, "120000L"));
        if (otpDTO.getOtpVerificationMethod().equals(PHONE_NUM_OTP.name())) {
            sendPhoneOtp(otpDTO, otpCode, otpDTO.getSmsEnum());
        } else if (otpDTO.getOtpVerificationMethod().equals(EMAIL_OTP.name())) {
            sendEmailOtp(otpDTO, otpCode, otpDTO.getEmailEnum(), expirationOtpDuration);
        }
        // Save otp to db for otp verification
        LocalDateTime expirationOtpTime = LocalDateTime.now().plusSeconds(expirationOtpDuration / 1000);
        otpCheckEntity.setNewestOtp(otpCode);
        otpCheckEntity.setOtpExpirationTime(expirationOtpTime);
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
            //String emailSender = getConfigValue(EMAIL_ADDRESS_CONFIG.name(), configurationRepository, "needforspeed160899@gmail.com");
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

    @Override
    public CompleteResponse<Object> verifyOtp(OtpDTO otpDTO) {
        int maxRetryOtp = convertStringToInt(getConfigValue(MAX_RETRY_OTP.name(), configurationRepository, "3"));
        long restrictedOtpDuration = convertStringToLong(getConfigValue(OTP_RESTRICTED_TIME.name(), configurationRepository, "900000L"));

        // Validate input for verifying otp request
        otpValidator.validateVerifyOtpRequest(otpDTO);

        // Check if there is an OTP entity existed
        Optional<OtpCheckEntity> otpCheckEntityOptional = otpCheckRepository.findByUsernameAndBlock(otpDTO.getUserName(), false);
        if (otpCheckEntityOptional.isEmpty()) {
            log.error("There is no OTP check entity for verification!");
            throw new BusinessException(OTP_BLOCKED_OR_NOT_FOUND, OTP.name());
        }
        OtpCheckEntity otpCheckEntity = otpCheckEntityOptional.get();

        // Check if OTP has expired
        if (otpCheckEntity.getOtpExpirationTime() != null && LocalDateTime.now().isAfter(otpCheckEntity.getOtpExpirationTime())) {
            log.warn("Verification OTP has expired!");
            verifyOtpFailed(maxRetryOtp, restrictedOtpDuration, otpCheckEntity);
            throw new BusinessException(VERIFICATION_OTP_EXPIRED, OTP.name());
        }

        // OTP does not match
        if (!otpDTO.getOtp().equals(otpCheckEntity.getNewestOtp())) {
            log.warn("Verification OTP does not match!");
            verifyOtpFailed(maxRetryOtp, restrictedOtpDuration, otpCheckEntity);
            throw new BusinessException(OTP_VERIFICATION_FAIL, OTP.name());
        } else {
            return getCompleteResponse(errorCodeRepository, OTP_VERIFICATION_SUCCESS, OTP.name(), null);
        }
    }

    private void verifyOtpFailed(int maxRetryOtp, long restrictedOtpDuration, OtpCheckEntity otpCheckEntity) {
        otpCheckEntity.setRetryCount(otpCheckEntity.getRetryCount() + 1);
        if (otpCheckEntity.getRetryCount() >= maxRetryOtp) {
            otpCheckEntity.setBlock(true);
            LocalDateTime restrictedOtpTime = LocalDateTime.now().plusSeconds(restrictedOtpDuration / 1000);
            otpCheckEntity.setOtpRestrictedTime(restrictedOtpTime);
        }
        otpCheckRepository.save(otpCheckEntity);
    }
}
