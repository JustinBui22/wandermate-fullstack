package com.example.travellingapp.exception_handler;

import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.repository.ErrorCodeRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.INVALID_INPUT;
import static com.example.travellingapp.enums.ErrorCodeEnum.MEDIA_TYPE_NOT_SUPPORTED;
import static com.example.travellingapp.enums.ErrorCodeEnum.REQUEST_METHOD_NOT_SUPPORTED;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerMvcTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ErrorCodeRepository errorCodeRepository = mock(ErrorCodeRepository.class);
        when(errorCodeRepository.findByErrorEnumAndFlow(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    ErrorCodeEnum errorCodeEnum = ErrorCodeEnum.valueOf(invocation.getArgument(0));
                    String flow = invocation.getArgument(1);
                    return Optional.of(createErrorCode(errorCodeEnum, flow));
                });

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler(errorCodeRepository))
                .build();
    }

    @Test
    void malformedJson_shouldReturnStandardInvalidInputEnvelope() throws Exception {
        mockMvc.perform(post("/exception-test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Trip\",\"date\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.flow").value(COMMON.name()));
    }

    @Test
    void invalidDate_shouldReturnStandardInvalidInputEnvelope() throws Exception {
        mockMvc.perform(post("/exception-test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Trip\",\"date\":\"not-a-date\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.flow").value(COMMON.name()));
    }

    @Test
    void invalidDto_shouldReturnStandardInvalidInputEnvelope() throws Exception {
        mockMvc.perform(post("/exception-test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"date\":\"2027-08-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.body").value("Name is required"));
    }

    @Test
    void missingRequestParameter_shouldReturnStandardInvalidInputEnvelope() throws Exception {
        mockMvc.perform(get("/exception-test/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.body").value("Missing required request parameter: keyword"));
    }

    @Test
    void pathVariableTypeMismatch_shouldReturnStandardInvalidInputEnvelope() throws Exception {
        mockMvc.perform(get("/exception-test/trips/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.body").value("Invalid value for request parameter: tripId"));
    }

    @Test
    void unsupportedMethod_shouldReturnStandardMethodNotAllowedEnvelope() throws Exception {
        mockMvc.perform(put("/exception-test/search"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(REQUEST_METHOD_NOT_SUPPORTED.getCode()))
                .andExpect(jsonPath("$.message").value(REQUEST_METHOD_NOT_SUPPORTED.getMessage()))
                .andExpect(jsonPath("$.flow").value(COMMON.name()));
    }

    @Test
    void unsupportedMediaType_shouldReturnStandardUnsupportedMediaTypeEnvelope() throws Exception {
        mockMvc.perform(post("/exception-test/body")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not-json"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value(MEDIA_TYPE_NOT_SUPPORTED.getCode()))
                .andExpect(jsonPath("$.message").value(MEDIA_TYPE_NOT_SUPPORTED.getMessage()))
                .andExpect(jsonPath("$.flow").value(COMMON.name()));
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

    @RestController
    @RequestMapping("/exception-test")
    static class ExceptionTestController {

        @PostMapping(value = "/body", consumes = MediaType.APPLICATION_JSON_VALUE)
        ResponseEntity<Void> readBody(@Valid @RequestBody TestRequest request) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/search")
        ResponseEntity<Void> search(@RequestParam String keyword) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/trips/{tripId}")
        ResponseEntity<Void> getTrip(@PathVariable Long tripId) {
            return ResponseEntity.ok().build();
        }
    }

    record TestRequest(
            @NotBlank(message = "Name is required") String name,
            LocalDate date
    ) {
    }
}
