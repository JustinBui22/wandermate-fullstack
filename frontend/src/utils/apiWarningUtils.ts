export function hasApiWarning(error: any, warningCode: string) {
    const responseData = error?.response?.data;

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

export function getApiErrorMessage(error: any, fallbackMessage: string) {
    const responseData = error?.response?.data;
    const body = responseData?.body;

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
        fallbackMessage
    );
}

export function getApiErrorCode(error: any) {
    return error?.response?.data?.code;
}

export function getApiErrorTitle(error: any, fallbackTitle: string) {
    const code = getApiErrorCode(error);

    switch (code) {
        case "E049":
            return "Trip date conflict";

        case "E050":
            return "Destination date conflict";

        case "E051":
            return "Activity time conflict";

        case "E046":
            return "Invalid activity time";

        case "E052":
            return "Missing activity time";

        case "E053":
            return "Missing activity name";

        case "E054":
            return "Missing destination name";

        case "E055":
            return "Missing destination time";

        case "E056":
            return "Invalid destination time";

        case "E057":
            return "Missing trip name";

        case "E058":
            return "Missing trip time";

        case "E059":
            return "Invalid trip time";

        case "E060":
            return "Missing OTP method";

        case "E061":
            return "Missing email config";

        case "E062":
            return "Missing SMS config";

        default:
            return fallbackTitle;
    }
}