package com.example.travellingapp.exception_handler;

import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.response_template.ResponseBody;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Set;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(errorCodeRepository.findByErrorEnumAndFlow(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.valueOf(invocation.getArgument(0));
                    String flow = invocation.getArgument(1);
                    return java.util.Optional.of(createErrorCode(errorCodeEnum, flow));
                });
        globalExceptionHandler = new GlobalExceptionHandler(errorCodeRepository);
    }

    @Test
    void handleBusinessExceptions_shouldReturnExistingApplicationErrorEnvelope() {
        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleBusinessExceptions(new BusinessException(INVALID_INPUT, COMMON.name()));

        assertResponse(response, 400, INVALID_INPUT);
    }

    @Test
    void handleMissingServletRequestParameterException_shouldReturnInvalidInputEnvelope() {
        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleMissingServletRequestParameterException(
                        new MissingServletRequestParameterException("keyword", "String")
                );

        assertResponse(response, 400, INVALID_INPUT);
        assertThat(response.getBody().getBody())
                .isEqualTo("Missing required request parameter: keyword");
    }

    @Test
    void handleMissingRequestHeaderException_shouldReturnInvalidInputEnvelope() {
        MissingRequestHeaderException exception = mock(MissingRequestHeaderException.class);
        when(exception.getHeaderName()).thenReturn("Refresh-Token");

        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleMissingRequestHeaderException(exception);

        assertResponse(response, 400, INVALID_INPUT);
        assertThat(response.getBody().getBody())
                .isEqualTo("Missing required request header: Refresh-Token");
    }

    @Test
    void handleMethodArgumentTypeMismatchException_shouldReturnInvalidInputEnvelope() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("tripId");

        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleMethodArgumentTypeMismatchException(exception);

        assertResponse(response, 400, INVALID_INPUT);
        assertThat(response.getBody().getBody())
                .isEqualTo("Invalid value for request parameter: tripId");
    }

    @Test
    void handleConstraintViolationException_shouldReturnFirstValidationMessage() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Trip ID must be positive");

        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleConstraintViolationException(
                        new ConstraintViolationException(Set.of(violation))
                );

        assertResponse(response, 400, INVALID_INPUT);
        assertThat(response.getBody().getBody()).isEqualTo("Trip ID must be positive");
    }

    @Test
    void handleHttpRequestMethodNotSupportedException_shouldReturnMethodNotAllowedEnvelope() {
        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleHttpRequestMethodNotSupportedException(
                        mock(HttpRequestMethodNotSupportedException.class)
                );

        assertResponse(response, 405, REQUEST_METHOD_NOT_SUPPORTED);
    }

    @Test
    void handleHttpMediaTypeNotSupportedException_shouldReturnUnsupportedMediaTypeEnvelope() {
        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleHttpMediaTypeNotSupportedException(
                        mock(HttpMediaTypeNotSupportedException.class)
                );

        assertResponse(response, 415, MEDIA_TYPE_NOT_SUPPORTED);
    }

    @Test
    void handleMaxUploadSizeExceededException_shouldReturnPayloadTooLargeEnvelope() {
        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleMaxUploadSizeExceededException(
                        new MaxUploadSizeExceededException(5 * 1024 * 1024)
                );

        assertResponse(response, 413, PAYLOAD_TOO_LARGE);
    }

    @Test
    void handleMissingServletRequestPartException_shouldReturnInvalidInputEnvelope() {
        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleMissingServletRequestPartException(
                        new MissingServletRequestPartException("file")
                );

        assertResponse(response, 400, INVALID_INPUT);
        assertThat(response.getBody().getBody())
                .isEqualTo("Missing required multipart part: file");
    }

    @Test
    void handleMultipartException_shouldReturnInvalidInputEnvelope() {
        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleMultipartException(new MultipartException("Malformed multipart request"));

        assertResponse(response, 400, INVALID_INPUT);
        assertThat(response.getBody().getBody()).isEqualTo("Invalid multipart request");
    }

    @Test
    void handleResourceNotFoundException_shouldReturnNotFoundEnvelope() {
        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleResourceNotFoundException(mock(NoResourceFoundException.class));

        assertResponse(response, 404, RESOURCE_NOT_FOUND);
    }

    @Test
    void handleAccessDeniedException_shouldReturnForbiddenEnvelope() {
        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleAccessDeniedException(new AccessDeniedException("Forbidden"));

        assertResponse(response, 403, ACCESS_DENIED);
    }

    @Test
    void handleUnexpectedException_shouldReturnInternalServerErrorWithoutExposingDetails() {
        ResponseEntity<ResponseBody<Object>> response = globalExceptionHandler
                .handleUnexpectedException(new RuntimeException("database password leaked"));

        assertResponse(response, 500, INTERNAL_SERVER_ERROR);
        assertThat(String.valueOf(response.getBody().getBody()))
                .doesNotContain("database password leaked");
    }

    private void assertResponse(
            ResponseEntity<ResponseBody<Object>> response,
            int expectedStatus,
            ErrorCodeEnum expectedErrorCode
    ) {
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(expectedErrorCode.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(expectedErrorCode.getMessage());
        assertThat(response.getBody().getFlow()).isEqualTo(COMMON.name());
    }

    private ErrorCodeEntity createErrorCode(ErrorCodeEnum errorCodeEnum, String flow) {
        ErrorCodeEntity errorCode = new ErrorCodeEntity();
        errorCode.setErrorCode(errorCodeEnum.getCode());
        errorCode.setErrorEnum(errorCodeEnum.name());
        errorCode.setErrorMessage(errorCodeEnum.getMessage());
        errorCode.setErrorDescription(errorCodeEnum.getMessage());
        errorCode.setFlow(flow);
        errorCode.setCreatedDate(Instant.now());
        return errorCode;
    }
}
