package com.example.travellingapp.exception_handler;

import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
            String validationMessage = ex.getBindingResult()
                    .getAllErrors()
                    .getFirst()
                    .getDefaultMessage();
            if (validationMessage != null && !validationMessage.isBlank()) {
                errorMessage = validationMessage;
            }
        }
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INVALID_INPUT,
                COMMON.name(),
                errorMessage
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseBody<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex
    ) {
        log.warn("Invalid request body: {}", ex.getMostSpecificCause().getClass().getSimpleName());
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INVALID_INPUT,
                COMMON.name(),
                "Invalid request body, enum value, date or date-time format"
        );
        return new ResponseEntity<>(
                result.getResponseBody(),
                HttpStatusCode.valueOf(result.getHttpCode())
        );
    }

    // Handle validation exceptions for request parameters (e.g., @RequestParam)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseBody<Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex
    ) {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INVALID_INPUT,
                COMMON.name(),
                "Missing required request parameter: " + ex.getParameterName()
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for request headers (e.g., @RequestHeader)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ResponseBody<Object>> handleMissingRequestHeaderException(
            MissingRequestHeaderException ex
    ) {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INVALID_INPUT,
                COMMON.name(),
                "Missing required request header: " + ex.getHeaderName()
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for request parameters with type mismatch (e.g., @RequestParam with wrong type)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseBody<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex
    ) {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INVALID_INPUT,
                COMMON.name(),
                "Invalid value for request parameter: " + ex.getName()
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for request parameters with @Validated on controller methods (e.g., @RequestParam with @Min, @Max, etc.)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ResponseBody<Object>> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex
    ) {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INVALID_INPUT,
                COMMON.name(),
                "Invalid request parameter"
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for request parameters with @Validated on controller methods (e.g., @RequestParam with @Min, @Max, etc.)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseBody<Object>> handleConstraintViolationException(
            ConstraintViolationException ex
    ) {
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .filter(message -> !message.isBlank())
                .orElse("Invalid request parameter");
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INVALID_INPUT,
                COMMON.name(),
                errorMessage
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for unsupported request methods (e.g., @RequestMapping with wrong HTTP method)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseBody<Object>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex
    ) {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                REQUEST_METHOD_NOT_SUPPORTED,
                COMMON.name(),
                null
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for unsupported media types (e.g., @RequestMapping with wrong Content-Type)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ResponseBody<Object>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex
    ) {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                MEDIA_TYPE_NOT_SUPPORTED,
                COMMON.name(),
                null
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for multipart file upload exceeding the maximum size
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseBody<Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex
    ) {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                PAYLOAD_TOO_LARGE,
                COMMON.name(),
                null
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for missing multipart parts (e.g., @RequestPart with missing file)
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ResponseBody<Object>> handleMissingServletRequestPartException(
            MissingServletRequestPartException ex
    ) {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INVALID_INPUT,
                COMMON.name(),
                "Missing required multipart part: " + ex.getRequestPartName()
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for invalid multipart requests (e.g., missing file or invalid file type)
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ResponseBody<Object>> handleMultipartException(
            MultipartException ex
    ) {
        log.warn("Invalid multipart request: {}", ex.getClass().getSimpleName());
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INVALID_INPUT,
                COMMON.name(),
                "Invalid multipart request"
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for resource not found (e.g., @RequestMapping with non-existing path)
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ResponseBody<Object>> handleResourceNotFoundException(
            Exception ex
    ) {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                RESOURCE_NOT_FOUND,
                COMMON.name(),
                null
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle validation exceptions for access denied (e.g., @PreAuthorize with insufficient permissions)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseBody<Object>> handleAccessDeniedException(
            AccessDeniedException ex
    ) {
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                ACCESS_DENIED,
                COMMON.name(),
                null
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

    // Handle unexpected exceptions that are not explicitly handled by other exception handlers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBody<Object>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected unhandled exception: {}", ex.getClass().getSimpleName());
        CompleteResponse<Object> result = getCompleteResponse(
                errorCodeRepository,
                INTERNAL_SERVER_ERROR,
                COMMON.name(),
                null
        );
        return new ResponseEntity<>(result.getResponseBody(), HttpStatusCode.valueOf(result.getHttpCode()));
    }

}