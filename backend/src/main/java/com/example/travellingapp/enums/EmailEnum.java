package com.example.travellingapp.enums;

import static com.example.travellingapp.enums.CommonEnum.REGISTER;

import lombok.Getter;

@Getter
public enum EmailEnum {
    EMAIL_OTP_REGISTER("EMS01", "Email send otp to register through email", REGISTER);

    private final String code;
    private final String description;
    private final CommonEnum flow;

    EmailEnum(String code, String description, CommonEnum flow) {
        this.code = code;
        this.description = description;
        this.flow = flow;
    }
}
