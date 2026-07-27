package com.example.travellingapp.security;

import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.ACCESS_DENIED;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    private final ErrorCodeRepository errorCodeRepository;
    private final ObjectMapper objectMapper;

    public JsonAccessDeniedHandler(
            ErrorCodeRepository errorCodeRepository,
            ObjectMapper objectMapper
    ) {
        this.errorCodeRepository = errorCodeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                ACCESS_DENIED,
                COMMON.name(),
                null
        );

        response.setStatus(result.getHttpCode());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), result.getResponseBody());
    }
}