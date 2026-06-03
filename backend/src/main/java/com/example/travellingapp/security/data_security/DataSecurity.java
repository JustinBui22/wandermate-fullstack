package com.example.travellingapp.security.data_security;

import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ConfigurationRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.enums.ErrorCodeEnum.INTERNAL_SERVER_ERROR;
import static com.example.travellingapp.util.Common.getConfigValue;

@Log4j2
@Component
public class DataSecurity {

    private final ConfigurationRepository configurationRepository;

    public DataSecurity(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public String hashData(String data) {
        try {
            String secret = getConfigValue(
                    SECRET_KEY_CONFIG,
                    configurationRepository,
                    TOKEN.name()
            );

            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(keySpec);

            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            log.error("Failed to hash data!", e);
            throw new BusinessException(INTERNAL_SERVER_ERROR, TOKEN.name());
        }
    }
}
