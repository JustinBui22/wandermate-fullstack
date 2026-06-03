package com.example.travellingapp.enums;

import static com.example.travellingapp.enums.CommonEnum.REGISTER;

import lombok.Getter;

@Getter
public enum SmsEnum {
    SMS_OTP_REGISTER("SMS01", "Sms send otp to register through phone number", REGISTER);

    private final String code;
    private final String description;
    private final CommonEnum flow;

    SmsEnum(String code, String description, CommonEnum flow) {
        this.code = code;
        this.description = description;
        this.flow = flow;
    }
}
