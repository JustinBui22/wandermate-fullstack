package com.example.travellingapp.enums;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;


import static com.example.travellingapp.enums.CommonEnum.*;

@Log4j2
@Getter
public enum ErrorCodeEnum {
    USER_CREATED("E000", "User created", REGISTER, HttpStatusCodeEnum.OK),
    LOGIN_SUCCESS("E000", "Log in successfully", LOGIN, HttpStatusCodeEnum.OK),
    LOGOUT_SUCCESS("E000", "Log out successfully", LOGOUT, HttpStatusCodeEnum.OK),
    TOKEN_GENERATE_SUCCESS("E000", "Token generate successfully", TOKEN, HttpStatusCodeEnum.OK),
    TOKEN_RETRIEVE_SUCCESS("E000", "Token retrieved successfully", TOKEN, HttpStatusCodeEnum.OK),
    TOKEN_VERIFY_SUCCESS("E000", "Token verified successfully", TOKEN, HttpStatusCodeEnum.OK),
    OTP_VERIFICATION_SUCCESS("E000", "OTP code verification successfully", OTP, HttpStatusCodeEnum.OK),
    OTP_CREATED_SUCCESS("E000", "Otp created successfully", OTP, HttpStatusCodeEnum.OK),
    OTP_SENT_SUCCESS("E000", "Otp sent successfully", OTP, HttpStatusCodeEnum.OK),
    SMS_SENT_SUCCESS("E000", "Sms sent successfully", SMS, HttpStatusCodeEnum.OK),
    EMAIL_SENT_SUCCESS("E000", "Email sent successfully", EMAIL, HttpStatusCodeEnum.OK),
    USER_DETAILS_VERIFIED("E000", "Users details pass the verification", REGISTER, HttpStatusCodeEnum.OK),
    PASSWORD_UPDATED_SUCCESS("E000", "New password updated successfully", REGISTER, HttpStatusCodeEnum.OK),
    SEARCH_INFO_SUCCESS("E000", "Search info successfully", COMMON, HttpStatusCodeEnum.OK),
    TRIP_CREATED_SUCCESS("E000", "Trip created successfully", TRIP, HttpStatusCodeEnum.OK),
    TRIPS_RETRIEVED_SUCCESS("E000", "Trips retrieved successfully", TRIP, HttpStatusCodeEnum.OK),
    TRIP_UPDATED_SUCCESS("E000", "Trip updated successfully", TRIP, HttpStatusCodeEnum.OK),
    TRIP_DELETED_SUCCESS("E000", "Trip deleted successfully", TRIP, HttpStatusCodeEnum.OK),
    ACTIVITY_CREATED_SUCCESS("E000", "Activity created successfully", ACTIVITY, HttpStatusCodeEnum.OK),
    ACTIVITY_RETRIEVED_SUCCESS("E000", "Activity retrieved successfully", ACTIVITY, HttpStatusCodeEnum.OK),
    ACTIVITY_UPDATED_SUCCESS("E000", "Activity updated successfully", ACTIVITY, HttpStatusCodeEnum.OK),
    ACTIVITY_DELETED_SUCCESS("E000", "Activity deleted successfully", ACTIVITY, HttpStatusCodeEnum.OK),
    DESTINATION_CREATED_SUCCESS("E000", "Destination created successfully", DESTINATION, HttpStatusCodeEnum.OK),
    DESTINATION_RETRIEVED_SUCCESS("E000", "Destination retrieved successfully", DESTINATION, HttpStatusCodeEnum.OK),
    DESTINATION_UPDATED_SUCCESS("E000", "Destination updated successfully", DESTINATION, HttpStatusCodeEnum.OK),
    DESTINATION_DELETED_SUCCESS("E000", "Destination deleted successfully", DESTINATION, HttpStatusCodeEnum.OK),

    INVALID_INPUT("E001", "Invalid input provided", COMMON, HttpStatusCodeEnum.BAD_REQUEST),
    USERNAME_TAKEN("E002", "Username taken", REGISTER, HttpStatusCodeEnum.BAD_REQUEST),
    EMAIL_TAKEN("E003", "Email taken", REGISTER, HttpStatusCodeEnum.BAD_REQUEST),
    PASSWORD_NOT_QUALIFIED("E004", "Password not qualified", REGISTER, HttpStatusCodeEnum.BAD_REQUEST),
    USER_NOT_FOUND("E005", "User not found", COMMON, HttpStatusCodeEnum.NOT_FOUND),
    CLIENT_SERVER_ERROR("E006", "Client internal server error", COMMON, HttpStatusCodeEnum.INTERNAL_SERVER_ERROR),
    PASSWORD_NOT_CORRECT("E007", "Password not correct", LOGIN, HttpStatusCodeEnum.UNAUTHORIZED),
    UNDEFINED_ERROR_CODE("E008", "Undefined error code", COMMON, HttpStatusCodeEnum.INTERNAL_SERVER_ERROR),
    UNDEFINED_HTTP_CODE("E009", "Undefined http status code", COMMON, HttpStatusCodeEnum.INTERNAL_SERVER_ERROR),
    EMAIL_PATTERN_INVALID("E010", "Email form is invalid", REGISTER, HttpStatusCodeEnum.BAD_REQUEST),
    PHONE_FORMAT_INVALID("E011", "Phone format is invalid", REGISTER, HttpStatusCodeEnum.BAD_REQUEST),
    SMS_NOT_CONFIG("E012", "Sms config is not found", SMS, HttpStatusCodeEnum.NOT_FOUND),
    USERNAME_FORMAT_INVALID("E013", "Username format invalid", REGISTER, HttpStatusCodeEnum.BAD_REQUEST),
    TOKEN_GENERATE_FAIL("E014", "Token generate fail", TOKEN, HttpStatusCodeEnum.INTERNAL_SERVER_ERROR),
    TOKEN_VERIFY_FAIL("E015", "Token verify fail", TOKEN, HttpStatusCodeEnum.UNAUTHORIZED),
    TOKEN_EXPIRE("E016", "Token expires", TOKEN, HttpStatusCodeEnum.UNAUTHORIZED),
    INTERNAL_SERVER_ERROR("E017", "Internal server error", COMMON, HttpStatusCodeEnum.INTERNAL_SERVER_ERROR),
    CONFIG_NOT_FOUND("E018", "Config not found", COMMON, HttpStatusCodeEnum.NOT_FOUND),
    INPUT_FORMAT_INVALID("E019", "Input format invalid", COMMON, HttpStatusCodeEnum.BAD_REQUEST),
    OTP_VERIFICATION_FAIL("E020", "OTP code verification fail", COMMON, HttpStatusCodeEnum.UNAUTHORIZED),
    TOKEN_NOT_FOUND("E021", "Token not found", TOKEN, HttpStatusCodeEnum.NOT_FOUND),
    MAX_SESSIONS_REACHED("E022", "Max session reached", LOGIN, HttpStatusCodeEnum.TOO_MANY_REQUESTS),
    SESSION_TOKEN_INVALID("E023", "Token session invalid", TOKEN, HttpStatusCodeEnum.UNAUTHORIZED),
    SMS_SENT_FAIL("E024", "Sms sent failed", SMS, HttpStatusCodeEnum.INTERNAL_SERVER_ERROR),
    EMAIL_SENT_FAIL("E025", "Email sent failed", SMS, HttpStatusCodeEnum.INTERNAL_SERVER_ERROR),
    MAX_OTP_RETRY("E026", "Max OTP retry exceeded", OTP, HttpStatusCodeEnum.TOO_MANY_REQUESTS),
    VERIFICATION_OTP_EXPIRED("E027", "Verification OTP expired", OTP, HttpStatusCodeEnum.GONE),
    OTP_BLOCKED_OR_NOT_FOUND("E028", "OTP is currently blocked or not found", OTP, HttpStatusCodeEnum.NOT_FOUND),
    USER_EXISTED("E029", "User existed", COMMON, HttpStatusCodeEnum.CONFLICT),
    REFRESH_TOKEN_INVALID("E030", "Token refresh invalid", TOKEN, HttpStatusCodeEnum.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("E031", "Token refresh expired", TOKEN, HttpStatusCodeEnum.UNAUTHORIZED),
    TRIP_NOT_FOUND("E032", "Trip not found", TRIP, HttpStatusCodeEnum.NOT_FOUND),
    ACTIVITY_NOT_FOUND("E033", "Activity not found", ACTIVITY, HttpStatusCodeEnum.NOT_FOUND),
    DESTINATION_NOT_FOUND("E034", "Destination not found", DESTINATION, HttpStatusCodeEnum.NOT_FOUND),
    TRIP_OVERLAP_WARNING("W001", "This trip overlaps with another existing trip", TRIP, HttpStatusCodeEnum.CONFLICT),
    DESTINATION_OVERLAP_WARNING("W002", "This destination overlaps with another destination in this trip", DESTINATION, HttpStatusCodeEnum.CONFLICT),
    TRIP_DATE_CONFLICT_WITH_DESTINATION("E035", "Trip dates must include all existing destinations", TRIP, HttpStatusCodeEnum.CONFLICT),
    DESTINATION_DATE_OUTSIDE_TRIP_RANGE("E036", "Destination dates must stay inside the trip date range", DESTINATION, HttpStatusCodeEnum.BAD_REQUEST),
    ACTIVITY_OUTSIDE_DESTINATION_RANGE("E037", "Activity time must stay inside the destination date range", ACTIVITY, HttpStatusCodeEnum.BAD_REQUEST),
    ACTIVITY_OVERLAP_ERROR("E038", "Activity time overlaps with another activity in this trip", ACTIVITY, HttpStatusCodeEnum.CONFLICT),
    TRIP_NAME_ALREADY_EXISTS("E039", "Trip name already exists for this user", TRIP, HttpStatusCodeEnum.CONFLICT);

    private final String code;
    private final String message;
    private final CommonEnum flow;
    private final HttpStatusCodeEnum httpStatusCodeEnum;

    ErrorCodeEnum(String code, String message, CommonEnum flow, HttpStatusCodeEnum httpStatusCodeEnum) {
        this.code = code;
        this.message = message;
        this.flow = flow;
        this.httpStatusCodeEnum = httpStatusCodeEnum;
    }
}
