export type ApiErrorPayload = {
    code?: string;
    message?: string;
    errorMessage?: string;
    error_message?: string;
    errorDescription?: string;
    error_description?: string;
    body?: string | ApiErrorPayload | null;
};
