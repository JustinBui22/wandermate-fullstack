import { create } from "zustand";

import { login, logout } from "../api/authApi";
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

async function syncPreferredThemeFromProfile() {
    try {
        const profile = await getMyProfile();
        useThemeStore.getState().setPreferredTheme(profile.preferredTheme ?? "SYSTEM");
    } catch (error: any) {
        logger.error(
            "Failed to sync theme preference:",
            error.response?.data || error.message
        );
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
        } catch (error: any) {
            const errorMessage =
                error.response?.data?.message ||
                error.message ||
                "Login failed. Please try again.";

            const errorCode = error.response?.data?.code || null;

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
        } catch (error: any) {
            logger.error("Logout API failed:", error.response?.data || error.message);
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
        } catch (error: any) {
            logger.error(
                "Restore session failed:",
                error.response?.data || error.message
            );

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