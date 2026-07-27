package com.example.travellingapp.security;

import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.time.Instant;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.TOKEN;
import static com.example.travellingapp.enums.ErrorCodeEnum.TOKEN_VERIFY_FAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonAuthenticationEntryPointTest {
    @Mock
    private ErrorCodeRepository errorCodeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void commence_shouldReturnStandardJsonEnvelope_whenAuthenticationIsMissing() throws Exception {
        ErrorCodeEntity errorCode = new ErrorCodeEntity();
        errorCode.setErrorCode(TOKEN_VERIFY_FAIL.getCode());
        errorCode.setErrorEnum(TOKEN_VERIFY_FAIL.name());
        errorCode.setErrorMessage(TOKEN_VERIFY_FAIL.getMessage());
        errorCode.setFlow(TOKEN.name());
        errorCode.setCreatedDate(Instant.now());

        when(errorCodeRepository.findByErrorEnumAndFlow(TOKEN_VERIFY_FAIL.name(), TOKEN.name()))
                .thenReturn(Optional.of(errorCode));

        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint(
                errorCodeRepository,
                objectMapper
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new InsufficientAuthenticationException("Missing bearer token")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(body.get("code").asText()).isEqualTo(TOKEN_VERIFY_FAIL.getCode());
        assertThat(body.get("message").asText()).isEqualTo(TOKEN_VERIFY_FAIL.getMessage());
        assertThat(body.get("flow").asText()).isEqualTo(TOKEN.name());
    }
}
