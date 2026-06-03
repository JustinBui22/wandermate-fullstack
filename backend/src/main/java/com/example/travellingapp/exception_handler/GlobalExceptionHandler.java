package com.example.travellingapp.exception_handler;

import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static com.example.travellingapp.response_template.CompleteResponse.getCompleteResponse;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ErrorCodeRepository errorCodeRepository;

    public GlobalExceptionHandler(ErrorCodeRepository errorCodeRepository) {
        this.errorCodeRepository = errorCodeRepository;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseBody<Object>> handleBusinessExceptions(BusinessException ex) {
        CompleteResponse<Object> result = getCompleteResponse(errorCodeRepository, ex.getErrorCodeEnum(), ex.getFlow(), null);
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for request body parameters (e.g., @Valid annotated DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseBody<Object>> handleMethodValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = "Invalid request body!";
        if (ex.getBindingResult().hasErrors()) {
            errorMessage = Objects.requireNonNull(ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage());
        }
        CompleteResponse<Object> result = getCompleteResponse(errorCodeRepository, INVALID_INPUT, COMMON.name(), errorMessage);
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseBody<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex
    ) {
        log.warn("Invalid request body: {}", ex.getMostSpecificCause().getMessage());
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INVALID_INPUT,
                COMMON.name(),
                "Invalid request body or enum value"
        );
        return new ResponseEntity<>(
                result.getResponseBody(),
                HttpStatusCode.valueOf(result.getHttpCode())
        );
    }

}
