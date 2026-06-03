package com.example.travellingapp.controller;

import com.example.travellingapp.dto.request.OtpDTO;
import com.example.travellingapp.response_template.ResponseBody;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/otp")
public interface OtpController {
    @PostMapping("/send")
    ResponseEntity<ResponseBody<Object>> sendOtp(@Valid @RequestBody OtpDTO otpRequest);

    @PostMapping("/verify")
    ResponseEntity<ResponseBody<Object>> verifyOtp(@Valid @RequestBody OtpDTO otpRequest);
}