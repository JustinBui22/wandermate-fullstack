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
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.ACCESS_DENIED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonAccessDeniedHandlerTest {
    @Mock
    private ErrorCodeRepository errorCodeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void handle_shouldReturnStandardJsonEnvelope_whenAuthorizationIsDenied() throws Exception {
        ErrorCodeEntity errorCode = new ErrorCodeEntity();
        errorCode.setErrorCode(ACCESS_DENIED.getCode());
        errorCode.setErrorEnum(ACCESS_DENIED.name());
        errorCode.setErrorMessage(ACCESS_DENIED.getMessage());
        errorCode.setFlow(COMMON.name());
        errorCode.setCreatedDate(Instant.now());

        when(errorCodeRepository.findByErrorEnumAndFlow(ACCESS_DENIED.name(), COMMON.name()))
                .thenReturn(Optional.of(errorCode));

        JsonAccessDeniedHandler accessDeniedHandler = new JsonAccessDeniedHandler(
                errorCodeRepository,
                objectMapper
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException("Forbidden")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(body.get("code").asText()).isEqualTo(ACCESS_DENIED.getCode());
        assertThat(body.get("message").asText()).isEqualTo(ACCESS_DENIED.getMessage());
        assertThat(body.get("flow").asText()).isEqualTo(COMMON.name());
    }
}
