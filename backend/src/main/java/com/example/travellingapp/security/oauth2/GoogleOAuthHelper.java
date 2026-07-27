package com.example.travellingapp.security.oauth2;

import com.example.travellingapp.entity.ConfigurationEntity;
import com.example.travellingapp.repository.ConfigurationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;

import static com.example.travellingapp.enums.CommonEnum.*;
import static com.example.travellingapp.util.Common.getConfigValue;
import static com.example.travellingapp.util.Common.getEnvConfig;

@Log4j2
@Component
public class GoogleOAuthHelper implements SchedulingConfigurer {
    private final ConfigurationRepository configurationRepository;
    private final TaskScheduler taskScheduler;
    private static final String OAUTH_REFRESH_TOKEN_FIELD = "refresh_token";

    public GoogleOAuthHelper(ConfigurationRepository configurationRepository, TaskScheduler taskScheduler) {
        this.configurationRepository = configurationRepository;
        this.taskScheduler = taskScheduler;
    }

    public void refreshOAuthToken() {
        try {
            String enabled = getEnvConfig(EMAIL_OAUTH_REFRESH_ENABLED.name(), EMAIL_OAUTH_REFRESH_ENABLED.name(), "false", configurationRepository);
            if (!Boolean.parseBoolean(enabled)) {
                log.info("Email OAuth refresh is disabled! Skipping Oauth token refresh.");
                return;
            }
            //String refreshToken = getConfigValue(EMAIL_REFRESH_TOKEN, configurationRepository, OTP.name());
            String refreshToken = getEnvConfig(EMAIL_REFRESH_TOKEN.name(), EMAIL_REFRESH_TOKEN.name(), "", configurationRepository);
            RestTemplate restTemplate = new RestTemplate();
            // Prepare request data
            //String clientId = getConfigValue(EMAIL_CLIENT_ID.name(), configurationRepository, OTP.name());
            String clientId = getEnvConfig(EMAIL_CLIENT_ID.name(), EMAIL_CLIENT_ID.name(), "", configurationRepository);
            //String clientSecret = getConfigValue(EMAIL_CLIENT_SECRET.name(), configurationRepository, OTP.name());
            String clientSecret = getEnvConfig(EMAIL_CLIENT_SECRET.name(), EMAIL_CLIENT_SECRET.name(), "", configurationRepository);
            LinkedMultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
            requestData.add("client_id", clientId);
            requestData.add("client_secret", clientSecret);
            requestData.add(OAUTH_REFRESH_TOKEN_FIELD, refreshToken);
            requestData.add("grant_type", OAUTH_REFRESH_TOKEN_FIELD);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<LinkedMultiValueMap<String, String>> request = new HttpEntity<>(requestData, headers);

            // Send POST request
            //String tokenUrl = getConfigValue(EMAIL_TOKEN_URL, configurationRepository, "https://oauth2.googleapis.com/token");
            String tokenUrl = getEnvConfig(EMAIL_TOKEN_URL.name(), EMAIL_TOKEN_URL.name(), "https://oauth2.googleapis.com/token", configurationRepository);
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            // Parse JSON response
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String newAccessToken = jsonNode.get("access_token").asText();
                // get new refresh token if received
                String newRefreshToken = jsonNode.findValue(OAUTH_REFRESH_TOKEN_FIELD) == null ? null : jsonNode.get(OAUTH_REFRESH_TOKEN_FIELD).asText();
                if (newRefreshToken != null && !newRefreshToken.equals(refreshToken)) {
                    ConfigurationEntity oauthRefreshTokenConfig = configurationRepository.findByConfigCode(EMAIL_REFRESH_TOKEN.name())
                            .orElse(new ConfigurationEntity(EMAIL_REFRESH_TOKEN.name(), newRefreshToken, Instant.now()));
                    oauthRefreshTokenConfig.setConfigValue(newRefreshToken);
                    oauthRefreshTokenConfig.setModifiedDate(Instant.now());
                    log.info("New refresh token for Oauth2 received!");
                    configurationRepository.save(oauthRefreshTokenConfig);
                }
                log.info("New access token for Oauth2 received!");
                // Store new access token
                ConfigurationEntity config = configurationRepository.findByConfigCode(EMAIL_ACCESS_TOKEN_CONFIG.name())
                        .orElse(new ConfigurationEntity(EMAIL_ACCESS_TOKEN_CONFIG.name(), newAccessToken, Instant.now()));
                config.setConfigValue(newAccessToken);
                config.setModifiedDate(Instant.now());
                configurationRepository.save(config);
                log.info("OAuth2 token refreshed successfully.");
            } else {
                log.error(" Error code: {} - Failed to refresh OAuth2 token!", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Exception in refreshing OAuth2 token: {}", e.getClass().getSimpleName());
        }
    }

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar taskRegistrar) {
        long refreshRate = Long.parseLong(getConfigValue(EMAIL_REFRESH_ACCESS_TOKEN_RATE.name(), configurationRepository, "3500000"));
        taskRegistrar.setTaskScheduler(taskScheduler);
        taskRegistrar.addFixedRateTask(this::refreshOAuthToken, Duration.ofMillis(refreshRate));
        log.info("OAuth token refresh scheduled every {} milliseconds.", refreshRate);
    }
}