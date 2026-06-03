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

    return (
        responseData?.message ||
        responseData?.errorMessage ||
        responseData?.error_message ||
        responseData?.errorDescription ||
        responseData?.error_description ||
        responseData?.body?.message ||
        responseData?.body?.errorMessage ||
        responseData?.body?.error_message ||
        responseData?.body?.errorDescription ||
        responseData?.body?.error_description ||
        fallbackMessage
    );
}