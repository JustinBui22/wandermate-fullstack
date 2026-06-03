import axios from "axios";
import { API_BASE_URL } from "@/src/constants/env";
import {
    getRefreshToken,
    getSessionToken,
    saveAccessToken,
    saveRefreshToken,
    saveSessionToken,
} from "@/src/stores/tokenStore";

type ApiResponse<T> = {
    code: string;
    message: string;
    flow?: string;
    body: T;
};

type RefreshResponseBody = {
    accessToken?: string;
    refreshToken?: string;
    sessionToken?: string;
};

const refreshClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

export async function refreshAccessToken(): Promise<string> {
    const refreshToken = await getRefreshToken();
    const sessionToken = await getSessionToken();

    if (!refreshToken || !sessionToken) {
        throw new Error("Missing refresh token or session token.");
    }

    const response = await refreshClient.post<ApiResponse<RefreshResponseBody>>(
        "/api/v1/auth/refresh",
        {},
        {
            headers: {
                "Refresh-Token": refreshToken,
                "Session-Token": sessionToken,
            },
        }
    );

    const body = response.data.body;

    if (!body?.accessToken) {
        throw new Error("Refresh response does not contain access token.");
    }

    await saveAccessToken(body.accessToken);

    if (body.refreshToken) {
        await saveRefreshToken(body.refreshToken);
    }

    if (body.sessionToken) {
        await saveSessionToken(body.sessionToken);
    }

    return body.accessToken;
}