import { create } from "zustand";

import { login, logout } from "../api/authApi";
import { registerSessionExpiredHandler } from "@/src/auth/sessionLifecycle";
import { getMyProfile } from "@/src/api/userApi";
import { refreshAccessToken } from "@/src/refreshApi";
import {
    clearTokens,
    getAccessToken,
    getRefreshToken,
    getSessionToken,
    getStoredUsername,
    saveTokens,
} from "../stores/tokenStore";
import { useThemeStore } from "@/src/stores/themeStore";
import type { LoginRequest } from "../types/auth";
import { logger } from "../utils/logger";
import { getApiErrorCode, getApiErrorMessage } from "@/src/utils/apiWarningUtils";

async function syncPreferredThemeFromProfile() {
    try {
        const profile = await getMyProfile();
        useThemeStore.getState().setPreferredTheme(profile.preferredTheme ?? "SYSTEM");
    } catch (error: unknown) {
        logger.error("Failed to sync theme preference:", getApiErrorMessage(error, "Unknown error"));
    }
}

type AuthState = {
    isAuthenticated: boolean;
    isLoading: boolean;
    error: string | null;
    errorCode: string | null;
    username: string | null;

    loginUser: (data: LoginRequest) => Promise<boolean>;
    logoutUser: () => Promise<void>;
    restoreAuthSession: () => Promise<void>;
    clearError: () => void;
};

export const useAuthStore = create<AuthState>((set) => ({
    isAuthenticated: false,
    isLoading: false,
    error: null,
    errorCode: null,
    username: null,

    loginUser: async (data) => {
        try {
            set({
                isLoading: true,
                error: null,
                errorCode: null,
            });

            const normalizedUsername = data.username.trim();

            const tokens = await login({
                ...data,
                username: normalizedUsername,
            });

            await saveTokens(tokens, normalizedUsername);
            await syncPreferredThemeFromProfile();

            set({
                isAuthenticated: true,
                isLoading: false,
                error: null,
                errorCode: null,
                username: normalizedUsername,
            });

            return true;
        } catch (error: unknown) {
            const errorMessage = getApiErrorMessage(
                error,
                "Login failed. Please try again."
            );
            const errorCode = getApiErrorCode(error);

            set({
                isAuthenticated: false,
                isLoading: false,
                error: errorMessage,
                errorCode,
                username: null,
            });

            return false;
        }
    },

    logoutUser: async () => {
        try {
            await logout();
        } catch (error: unknown) {
            logger.error("Logout API failed:", getApiErrorMessage(error, "Unknown error"));
        } finally {
            await clearTokens();
            useThemeStore.getState().resetPreferredTheme();

            set({
                isAuthenticated: false,
                isLoading: false,
                error: null,
                errorCode: null,
                username: null,
            });
        }
    },

    restoreAuthSession: async () => {
        try {
            logger.debug("Restore session started");

            set({
                isLoading: true,
                error: null,
                errorCode: null,
            });

            const accessToken = await getAccessToken();
            const refreshToken = await getRefreshToken();
            const sessionToken = await getSessionToken();
            const storedUsername = await getStoredUsername();

            if (!accessToken || !refreshToken || !sessionToken || !storedUsername) {
                await clearTokens();
                useThemeStore.getState().resetPreferredTheme();

                set({
                    isAuthenticated: false,
                    isLoading: false,
                    error: null,
                    errorCode: null,
                    username: null,
                });

                return;
            }

            logger.debug("Restoring session: refreshing access token...");
            await refreshAccessToken();
            await syncPreferredThemeFromProfile();
            logger.debug("Session restored successfully.");

            set({
                isAuthenticated: true,
                isLoading: false,
                error: null,
                errorCode: null,
                username: storedUsername,
            });
        } catch (error: unknown) {
            logger.error("Restore session failed:", getApiErrorMessage(error, "Unknown error"));

            await clearTokens();
            useThemeStore.getState().resetPreferredTheme();

            set({
                isAuthenticated: false,
                isLoading: false,
                error: null,
                errorCode: null,
                username: null,
            });
        }
    },

    clearError: () => {
        set({ error: null, errorCode: null });
    },
}));

registerSessionExpiredHandler(() => {
    useThemeStore.getState().resetPreferredTheme();
    useAuthStore.setState({
        isAuthenticated: false,
        isLoading: false,
        error: null,
        errorCode: null,
        username: null,
    });
});
