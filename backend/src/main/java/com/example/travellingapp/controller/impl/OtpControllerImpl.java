package com.example.travellingapp.controller.impl;

import com.example.travellingapp.controller.OtpController;
import com.example.travellingapp.dto.request.OtpDTO;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OtpControllerImpl implements OtpController {
    private final OtpService otpService;

    public OtpControllerImpl(OtpService otpService) {
        this.otpService = otpService;
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> sendOtp(OtpDTO otpDTO) {
        CompleteResponse<Object> response = otpService.sendOtp(otpDTO);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }

    @Override
    public ResponseEntity<ResponseBody<Object>> verifyOtp(OtpDTO otpDTO) {
        CompleteResponse<Object> response = otpService.verifyOtp(otpDTO);
        return new ResponseEntity<>(response.getResponseBody(), HttpStatus.valueOf(response.getHttpCode()));
    }
}
