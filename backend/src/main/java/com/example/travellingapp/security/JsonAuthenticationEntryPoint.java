package com.example.travellingapp.security;

import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.example.travellingapp.enums.CommonEnum.TOKEN;
import static com.example.travellingapp.enums.ErrorCodeEnum.TOKEN_VERIFY_FAIL;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ErrorCodeRepository errorCodeRepository;
    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(
            ErrorCodeRepository errorCodeRepository,
            ObjectMapper objectMapper
    ) {
        this.errorCodeRepository = errorCodeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                TOKEN_VERIFY_FAIL,
                TOKEN.name(),
                null
        );

        response.setStatus(result.getHttpCode());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), result.getResponseBody());
    }
}
