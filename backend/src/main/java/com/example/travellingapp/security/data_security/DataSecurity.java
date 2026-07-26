package com.example.travellingapp.security.data_security;

import com.example.travellingapp.enums.OtpPurpose;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.security.TokenSecretProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.INTERNAL_SERVER_ERROR;

@Log4j2
@Component
public class DataSecurity {

    private final TokenSecretProvider tokenSecretProvider;

    public DataSecurity(TokenSecretProvider tokenSecretProvider) {
        this.tokenSecretProvider = tokenSecretProvider;
    }

    public String hashData(String data) {
        return hashData(data, tokenSecretProvider.getRefreshTokenHashKey());
    }

    public String hashOtp(String username, OtpPurpose purpose, String otp) {
        if (username == null || username.isBlank() || purpose == null || otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("Username, OTP purpose and OTP code are required for hashing");
        }

        String payload = purpose.name()
                + ":"
                + username.trim().toLowerCase(Locale.ROOT)
                + ":"
                + otp;

        return hashData(payload, tokenSecretProvider.getOtpHashKey());
    }

    public boolean matchesOtp(String username, OtpPurpose purpose, String otp, String storedOtpHash) {
        if (storedOtpHash == null || storedOtpHash.isBlank()) {
            return false;
        }

        String candidateOtpHash = hashOtp(username, purpose, otp);
        return MessageDigest.isEqual(
                candidateOtpHash.getBytes(StandardCharsets.UTF_8),
                storedOtpHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String hashData(String data, SecretKey secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to hash data!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, TOKEN.name());
        }
    }
}