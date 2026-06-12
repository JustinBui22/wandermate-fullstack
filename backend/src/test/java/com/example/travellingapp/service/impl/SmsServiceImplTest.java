package com.example.travellingapp.service.impl;

import com.example.travellingapp.response_template.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.travellingapp.enums.CommonEnum.SMS;
import static com.example.travellingapp.enums.ErrorCodeEnum.SMS_SENT_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

class SmsServiceImplTest {

    private SmsServiceImpl smsService;

    @BeforeEach
    void setUp() {
        smsService = new SmsServiceImpl();
    }

    @Test
    void sendSms_shouldReturnSmsSentSuccess() {
        ResponseBody<String> response = smsService.sendSms(
                "0412345678",
                "Your OTP is 123456"
        );

        assertThat(response.getCode()).isEqualTo(SMS_SENT_SUCCESS.getCode());
        assertThat(response.getFlow()).isEqualTo(SMS.name());
        assertThat(response.getBody()).isNull();
    }

    @Test
    void sendSms_shouldReturnSuccessEvenWhenPhoneNumberIsBlank_becauseNoValidationExistsYet() {
        ResponseBody<String> response = smsService.sendSms(
                "",
                "Your OTP is 123456"
        );

        assertThat(response.getCode()).isEqualTo(SMS_SENT_SUCCESS.getCode());
        assertThat(response.getFlow()).isEqualTo(SMS.name());
    }

    @Test
    void sendSms_shouldReturnSuccessEvenWhenSmsMessageIsBlank_becauseNoValidationExistsYet() {
        ResponseBody<String> response = smsService.sendSms(
                "0412345678",
                ""
        );

        assertThat(response.getCode()).isEqualTo(SMS_SENT_SUCCESS.getCode());
        assertThat(response.getFlow()).isEqualTo(SMS.name());
    }
}