package com.example.travellingapp.controller.impl;

import com.example.travellingapp.dto.request.OtpDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static com.example.travellingapp.enums.CommonEnum.OTP;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OtpControllerImplTest {

    private OtpService otpService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        otpService = mock(OtpService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OtpControllerImpl(otpService))
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void sendOtp_shouldReturnServiceResponse() throws Exception {
        OtpDTO request = new OtpDTO();
        request.setUserName("JustinBo123");
        request.setEmail("justin@example.com");
        request.setOtpVerificationMethod("EMAIL_OTP");

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "OTP sent successfully",
                OTP.name(),
                Map.of("sent", true)
        );

        when(otpService.sendOtp(any(OtpDTO.class)))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.flow").value(OTP.name()))
                .andExpect(jsonPath("$.body.sent").value(true));

        verify(otpService).sendOtp(any(OtpDTO.class));
    }

    @Test
    void verifyOtp_shouldReturnServiceResponse() throws Exception {
        OtpDTO request = new OtpDTO();
        request.setUserName("JustinBo123");
        request.setEmail("justin@example.com");
        request.setOtpVerificationMethod("EMAIL_OTP");
        request.setOtp("123456");

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "OTP verified successfully",
                OTP.name(),
                Map.of("verified", true)
        );

        when(otpService.verifyOtp(any(OtpDTO.class)))
                .thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.flow").value(OTP.name()))
                .andExpect(jsonPath("$.body.verified").value(true));

        verify(otpService).verifyOtp(any(OtpDTO.class));
    }
}
