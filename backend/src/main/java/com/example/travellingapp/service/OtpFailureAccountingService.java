package com.example.travellingapp.service;

public interface OtpFailureAccountingService {
    void recordFailedVerification(
            int otpCheckId,
            int maxRetryVerifyOtp,
            long restrictedDurationMillis
    );
}
