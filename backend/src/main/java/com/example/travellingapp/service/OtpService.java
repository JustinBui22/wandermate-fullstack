package com.example.travellingapp.service;

import com.example.travellingapp.dto.request.OtpDTO;
import com.example.travellingapp.response_template.CompleteResponse;

public interface   OtpService {
    CompleteResponse<Object> sendOtp(OtpDTO otpDTO);

    CompleteResponse<Object> generateVerificationOtp();

    CompleteResponse<Object> verifyOtp(OtpDTO otpDTO);
}
