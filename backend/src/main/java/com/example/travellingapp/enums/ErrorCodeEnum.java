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

    TRIP_MEMBER_ADDED_SUCCESS("E000", "Trip member added successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_MEMBER_ROLE_UPDATED_SUCCESS("E000", "Trip member role updated successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_MEMBER_REMOVED_SUCCESS("E000", "Trip member removed successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    SUGGESTION_CREATED_SUCCESS("E000", "Suggestion created successfully", SUGGESTION, HttpStatusCodeEnum.OK),
    SUGGESTION_RETRIEVED_SUCCESS("E000", "Suggestion retrieved successfully", SUGGESTION, HttpStatusCodeEnum.OK),
    SUGGESTION_APPROVED_SUCCESS("E000", "Suggestion approved successfully", SUGGESTION, HttpStatusCodeEnum.OK),
    SUGGESTION_REJECTED_SUCCESS("E000", "Suggestion rejected successfully", SUGGESTION, HttpStatusCodeEnum.OK),
    TRIP_MEMBERS_RETRIEVED_SUCCESS("E000", "Trip members retrieved successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),

    TRIP_INVITATION_SENT_SUCCESS("E000", "Trip invitation sent successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_INVITATIONS_RETRIEVED_SUCCESS("E000", "Trip invitations retrieved successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_INVITATION_ACCEPTED_SUCCESS("E000", "Trip invitation accepted successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_INVITATION_REJECTED_SUCCESS("E000", "Trip invitation rejected successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),

    TRIP_JOIN_REQUEST_SENT_SUCCESS("E000", "Trip join request sent successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_JOIN_REQUESTS_RETRIEVED_SUCCESS("E000", "Trip join requests retrieved successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_JOIN_REQUEST_ACCEPTED_SUCCESS("E000", "Trip join request accepted successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_JOIN_REQUEST_REJECTED_SUCCESS("E000", "Trip join request rejected successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_OVERLAP_WARNINGS_RETRIEVED_SUCCESS("E000", "Trip overlap warnings retrieved successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),

    TRIP_SHARE_CODE_CREATED_SUCCESS("E000", "Trip share code created successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_SHARE_CODE_RETRIEVED_SUCCESS("E000", "Trip share code retrieved successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    TRIP_SHARE_CODE_JOIN_REQUEST_SENT_SUCCESS("E000", "Trip share code join request sent successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),
    COLLABORATION_SUMMARY_RETRIEVED_SUCCESS("E000", "Collaboration summary retrieved successfully", TRIP_MEMBER, HttpStatusCodeEnum.OK),

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
    TRIP_NAME_ALREADY_EXISTS("E039", "Trip name already exists for this user", TRIP, HttpStatusCodeEnum.CONFLICT),
    INVALID_CONFIG("E040", "Invalid config value", COMMON, HttpStatusCodeEnum.INTERNAL_SERVER_ERROR),
    PHONE_NUMBER_TAKEN("E041", "Phone number taken", REGISTER, HttpStatusCodeEnum.BAD_REQUEST),
    OTP_EMAIL_NOT_MATCH("E042", "OTP email does not match the provided email", OTP, HttpStatusCodeEnum.BAD_REQUEST),
    OTP_PHONE_NOT_MATCH("E043", "OTP phone number does not match the provided phone number", OTP, HttpStatusCodeEnum.BAD_REQUEST),
    OTP_CODE_NOT_CORRECT("E044", "OTP code is not correct", OTP, HttpStatusCodeEnum.BAD_REQUEST),
    DOB_IN_FUTURE("E045", "Date of birth cannot be in the future", REGISTER, HttpStatusCodeEnum.BAD_REQUEST),


    ACTIVITY_TIME_INVALID("E046", "Activity start time must be before end time", ACTIVITY, HttpStatusCodeEnum.BAD_REQUEST),

    TRIP_DATE_IN_PAST("E047", "Trip start date cannot be in the past", TRIP, HttpStatusCodeEnum.BAD_REQUEST),
    DESTINATION_DATE_IN_PAST("E048", "Destination start date cannot be in the past", DESTINATION, HttpStatusCodeEnum.BAD_REQUEST),
    TRIP_DATE_CONFLICT_WITH_EXISTING_DESTINATION("E049", "Trip date range must include all existing destinations", TRIP, HttpStatusCodeEnum.CONFLICT),
    DESTINATION_DATE_CONFLICT_WITH_EXISTING_ACTIVITY("E050", "Destination date range must include all existing activities in this destination", DESTINATION, HttpStatusCodeEnum.CONFLICT),
    ACTIVITY_TIME_CONFLICT_WITH_EXISTING_ACTIVITY("E051", "Activity time must not overlap with existing activities in this trip", ACTIVITY, HttpStatusCodeEnum.CONFLICT),

    ACTIVITY_TIME_NOT_FOUND("E052", "Activity time not found", ACTIVITY, HttpStatusCodeEnum.BAD_REQUEST),
    ACTIVITY_NAME_NOT_FOUND("E053", "Activity name is not found", ACTIVITY, HttpStatusCodeEnum.BAD_REQUEST),
    DESTINATION_NAME_NOT_FOUND("E054", "Destination name is not found", DESTINATION, HttpStatusCodeEnum.BAD_REQUEST),
    DESTINATION_TIME_NOT_FOUND("E055", "Destination time not found", DESTINATION, HttpStatusCodeEnum.BAD_REQUEST),
    DESTINATION_TIME_INVALID("E056", "Destination start time must be before end time", DESTINATION, HttpStatusCodeEnum.BAD_REQUEST),
    TRIP_NAME_NOT_FOUND("E057", "Trip name is not found", TRIP, HttpStatusCodeEnum.BAD_REQUEST),
    TRIP_TIME_NOT_FOUND("E058", "Trip time not found", TRIP, HttpStatusCodeEnum.BAD_REQUEST),
    TRIP_TIME_INVALID("E059", "Trip start time must be before end time", TRIP, HttpStatusCodeEnum.BAD_REQUEST),
    OTP_METHOD_MISSING("E060", "OTP verification method is missing", OTP, HttpStatusCodeEnum.BAD_REQUEST),
    EMAIL_ENUM_MISSING("E061", "Email enum is missing", OTP, HttpStatusCodeEnum.BAD_REQUEST),
    SMS_ENUM_MISSING("E062", "SMS enum is missing", OTP, HttpStatusCodeEnum.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD("E063", "New password cannot be the same as the old password", FORGOT_PASSWORD, HttpStatusCodeEnum.BAD_REQUEST),

    TRIP_ACCESS_DENIED("E064", "You do not have permission to access this trip", TRIP_MEMBER, HttpStatusCodeEnum.FORBIDDEN),
    TRIP_MEMBER_ALREADY_EXISTS("E065", "This user is already a member of the trip", TRIP_MEMBER, HttpStatusCodeEnum.CONFLICT),
    TRIP_MEMBER_NOT_FOUND("E066", "Trip member not found", TRIP_MEMBER, HttpStatusCodeEnum.NOT_FOUND),
    TRIP_OWNER_CANNOT_BE_REMOVED("E067", "Trip owner cannot be removed from the trip", TRIP_MEMBER, HttpStatusCodeEnum.BAD_REQUEST),
    TRIP_OWNER_ROLE_CANNOT_BE_CHANGED("E068", "Trip owner role cannot be changed", TRIP_MEMBER, HttpStatusCodeEnum.BAD_REQUEST),
    SUGGESTION_NOT_FOUND("E069", "Suggestion not found", SUGGESTION, HttpStatusCodeEnum.NOT_FOUND),
    SUGGESTION_ALREADY_APPROVED("E070", "Suggestion already approved", SUGGESTION, HttpStatusCodeEnum.CONFLICT),
    OWNER_CANNOT_BE_ASSIGNED_MANUALLY("E071", "Owner role cannot be assigned manually", TRIP_MEMBER, HttpStatusCodeEnum.BAD_REQUEST),
    SUGGESTION_ALREADY_REJECTED("E072", "Suggestion already rejected", SUGGESTION, HttpStatusCodeEnum.CONFLICT),

    TRIP_COLLABORATION_REQUEST_NOT_FOUND("E073", "Trip collaboration request not found", TRIP_MEMBER, HttpStatusCodeEnum.NOT_FOUND),
    TRIP_COLLABORATION_REQUEST_ALREADY_EXISTS("E074", "A pending collaboration request already exists for this trip and user", TRIP_MEMBER, HttpStatusCodeEnum.CONFLICT),
    TRIP_CANNOT_INVITE_SELF("E075", "Trip owner cannot invite themselves", TRIP_MEMBER, HttpStatusCodeEnum.BAD_REQUEST),
    TRIP_OWNER_CANNOT_REQUEST_TO_JOIN_OWN_TRIP("E076", "Trip owner cannot request to join their own trip", TRIP_MEMBER, HttpStatusCodeEnum.BAD_REQUEST),
    TRIP_STATUS_INVALID("E077", "Trip status is not valid", TRIP, HttpStatusCodeEnum.BAD_REQUEST),

    TRIP_SHARE_CODE_NOT_FOUND("E078", "Trip share code not found", TRIP_MEMBER, HttpStatusCodeEnum.NOT_FOUND),
    TRIP_SHARE_CODE_EXPIRED("E079", "Trip share code has expired", TRIP_MEMBER, HttpStatusCodeEnum.GONE),
    TRIP_SHARE_CODE_INACTIVE("E080", "Trip share code is inactive", TRIP_MEMBER, HttpStatusCodeEnum.BAD_REQUEST),
    TRIP_SHARE_CODE_USED("E081", "Trip share code has already been used", TRIP_MEMBER, HttpStatusCodeEnum.BAD_REQUEST),
    TRIP_SHARE_CODE_REVOKED("E082", "Trip share code has been revoked", TRIP_MEMBER, HttpStatusCodeEnum.BAD_REQUEST),
    TRIP_SHARE_CODE_GENERATE_TOO_SOON("E083", "Please wait before generating another trip share code", TRIP_MEMBER, HttpStatusCodeEnum.TOO_MANY_REQUESTS),
    TRIP_SHARE_CODE_ATTEMPT_RESTRICTED("E084", "Too many invalid invite code attempts. Please try again later", TRIP_MEMBER, HttpStatusCodeEnum.TOO_MANY_REQUESTS),


    DELETE_IMAGE_FAIL("E085", "Failed to delete image from cloud storage", COMMON, HttpStatusCodeEnum.INTERNAL_SERVER_ERROR),
    OTP_COOLDOWN_NOT_EXPIRED("E086", "OTP cooldown period has not expired yet", OTP, HttpStatusCodeEnum.TOO_MANY_REQUESTS),
    REGISTER_OTP_ALREADY_SENT("E087", "An OTP has already been sent for registration. Please wait before requesting another one.", REGISTER, HttpStatusCodeEnum.TOO_MANY_REQUESTS),
    ACCOUNT_ENUMERATION_RATE_LIMITED("E088", "Too many account verification requests. Please try again later.", COMMON, HttpStatusCodeEnum.TOO_MANY_REQUESTS),
    REQUEST_METHOD_NOT_SUPPORTED("E089", "Request method not allowed", COMMON, HttpStatusCodeEnum.METHOD_NOT_ALLOWED),
    MEDIA_TYPE_NOT_SUPPORTED("E090", "Media type not supported", COMMON, HttpStatusCodeEnum.UNSUPPORTED_MEDIA_TYPE),
    PAYLOAD_TOO_LARGE("E091", "Uploaded file is too large", COMMON, HttpStatusCodeEnum.PAYLOAD_TOO_LARGE),
    RESOURCE_NOT_FOUND("E092", "Resource not found", COMMON, HttpStatusCodeEnum.NOT_FOUND),
    ACCESS_DENIED("E093", "Access denied", COMMON, HttpStatusCodeEnum.FORBIDDEN)


    ;

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