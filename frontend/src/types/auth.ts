export type LoginRequest = {
    username: string;
    password: string;
};

export type LoginTokens = {
    accessToken: string;
    refreshToken: string;
    sessionToken: string;
};

export type ApiResponse<T> = {
    code: string;
    message: string;
    body: T;
};