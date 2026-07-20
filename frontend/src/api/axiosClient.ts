import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { API_BASE_URL } from "../constants/env";
import {
    getAccessToken,
    getSessionToken,
} from "../stores/tokenStore";
import { expireLocalSession } from "@/src/auth/sessionLifecycle";
import { refreshAccessToken } from "@/src/refreshApi";
import type { ApiErrorPayload } from "@/src/types/api";

type RetryableRequestConfig = InternalAxiosRequestConfig & {
    _retry?: boolean;
};

export const axiosClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});
let refreshPromise: Promise<string> | null = null;

axiosClient.interceptors.request.use(async (config) => {
    const accessToken = await getAccessToken();
    const sessionToken = await getSessionToken();

    if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`;
    }

    if (sessionToken) {
        config.headers["Session-Token"] = sessionToken;
    }

    return config;
});

axiosClient.interceptors.response.use(
    (response) => response,
    async (error: AxiosError<ApiErrorPayload>) => {
        const originalRequest = error.config as RetryableRequestConfig | undefined;

        const status = error.response?.status;
        const code = error.response?.data?.code;
        const message = error.response?.data?.message;

        const isAccessTokenExpired =
            code === "E016" ||
            message?.toLowerCase?.().includes("access token expires") ||
            message?.toLowerCase?.().includes("access token expired");

        const isSessionInvalid =
            code === "E023" ||
            message?.toLowerCase?.().includes("session token invalid") ||
            message?.toLowerCase?.().includes("session invalid");

        const isTokenInvalid =
            code === "E015" ||
            message?.toLowerCase?.().includes("token verify fail");

        if (!originalRequest) {
            return Promise.reject(error);
        }

        if (isAccessTokenExpired && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                refreshPromise ??= refreshAccessToken().finally(() => {
                    refreshPromise = null;
                });

                const newAccessToken = await refreshPromise;

                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

                const sessionToken = await getSessionToken();

                if (sessionToken) {
                    originalRequest.headers["Session-Token"] = sessionToken;
                }

                return axiosClient(originalRequest);
            } catch (refreshError) {
                await expireLocalSession();
                return Promise.reject(refreshError);
            }
        }

        // A 403 commonly means the authenticated user lacks permission for one
        // resource. Keep the session unless the backend explicitly reports an
        // authentication/session failure.
        if (status === 401 || isSessionInvalid || isTokenInvalid) {
            await expireLocalSession();
        }

        return Promise.reject(error);
    }
);
