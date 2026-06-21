type ApiErrorLike = {
    response?: {
        data?: unknown;
    };
    message?: string;
};

const FRIENDLY_ERROR_MESSAGES: Record<string, string> = {
    E022: "Too many active sessions. Continue only if you want to remove the oldest session.",
    E026: "Too many OTP attempts. Please wait before trying again.",
    E028: "OTP is unavailable or temporarily blocked. Please request a new code later.",
    E046: "Activity start time must be before activity end time.",
    E047: "Trip start date cannot be in the past.",
    E048: "Destination start date cannot be in the past.",
    E049: "Trip dates must include all existing destinations.",
    E050: "Destination dates must include all existing activities.",
    E051: "This activity overlaps with another activity in the same trip.",
    E052: "Activity start and end time are required.",
    E053: "Activity name is required.",
    E054: "Destination name is required.",
    E055: "Destination start and end date are required.",
    E056: "Destination start date must be before destination end date.",
    E057: "Trip name is required.",
    E058: "Trip start and end date are required.",
    E059: "Trip start date must be before trip end date.",
    E060: "Please choose an OTP verification method.",
    E061: "Email OTP configuration is missing. Please try again later.",
    E062: "SMS OTP configuration is missing. Please try again later.",
    E078: "Invite code was not found. Please check the code and try again.",
    E079: "This invite code has expired. Ask the owner to generate a new one.",
    E080: "This invite code is not active anymore.",
    E081: "This invite code has already been used. Ask the owner to generate a new one.",
    E082: "This invite code has been revoked. Ask the owner to generate a new one.",
    E083: "Please wait before generating another invite code.",
    E084: "Too many invalid invite code attempts. Please try again later.",
};

const FRIENDLY_ERROR_TITLES: Record<string, string> = {
    E022: "Too many sessions",
    E026: "OTP temporarily blocked",
    E028: "OTP unavailable",
    E046: "Invalid activity time",
    E047: "Invalid trip date",
    E048: "Invalid destination date",
    E049: "Trip date conflict",
    E050: "Destination date conflict",
    E051: "Activity time conflict",
    E052: "Missing activity time",
    E053: "Missing activity name",
    E054: "Missing destination name",
    E055: "Missing destination time",
    E056: "Invalid destination time",
    E057: "Missing trip name",
    E058: "Missing trip time",
    E059: "Invalid trip time",
    E060: "Missing OTP method",
    E061: "Missing email config",
    E062: "Missing SMS config",
    E078: "Invite code not found",
    E079: "Invite code expired",
    E080: "Invite code inactive",
    E081: "Invite code used",
    E082: "Invite code revoked",
    E083: "Generate too soon",
    E084: "Too many attempts",
};

function getResponseData(error: ApiErrorLike) {
    return error?.response?.data as any;
}

function getNestedBody(error: ApiErrorLike) {
    return getResponseData(error)?.body;
}

export function getApiErrorCode(error: ApiErrorLike) {
    const responseData = getResponseData(error);
    const body = getNestedBody(error);

    return responseData?.code || body?.code || null;
}

export function hasApiWarning(error: ApiErrorLike, warningCode: string) {
    const responseData = getResponseData(error);

    if (!responseData) {
        return false;
    }

    const responseText = JSON.stringify(responseData).toLowerCase();
    const warningCodeText = warningCode.toLowerCase();

    if (responseText.includes(warningCodeText)) {
        return true;
    }

    if (
        warningCode === "TRIP_OVERLAP_WARNING" &&
        responseText.includes("trip") &&
        responseText.includes("overlap")
    ) {
        return true;
    }

    return warningCode === "DESTINATION_OVERLAP_WARNING" &&
        responseText.includes("destination") &&
        responseText.includes("overlap");
}

export function getApiErrorMessage(error: ApiErrorLike, fallbackMessage: string) {
    const code = getApiErrorCode(error);

    if (code && FRIENDLY_ERROR_MESSAGES[code]) {
        return FRIENDLY_ERROR_MESSAGES[code];
    }

    const responseData = getResponseData(error);
    const body = getNestedBody(error);

    if (typeof body === "string" && body.trim()) {
        return body;
    }

    return (
        responseData?.message ||
        responseData?.errorMessage ||
        responseData?.error_message ||
        responseData?.errorDescription ||
        responseData?.error_description ||
        body?.message ||
        body?.errorMessage ||
        body?.error_message ||
        body?.errorDescription ||
        body?.error_description ||
        error?.message ||
        fallbackMessage
    );
}

export function getApiErrorTitle(error: ApiErrorLike, fallbackTitle: string) {
    const code = getApiErrorCode(error);

    if (code && FRIENDLY_ERROR_TITLES[code]) {
        return FRIENDLY_ERROR_TITLES[code];
    }

    return fallbackTitle;
}