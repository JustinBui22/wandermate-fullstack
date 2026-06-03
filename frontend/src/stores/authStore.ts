import {create} from "zustand";
import {refreshAccessToken} from "@/src/refreshApi";
import {login} from "../api/authApi";
import {
    clearTokens,
    getAccessToken,
    getRefreshToken,
    getSessionToken,
    saveTokens,
} from "../stores/tokenStore";
import type {LoginRequest} from "../types/auth";

type AuthState = {
    isAuthenticated: boolean;
    isLoading: boolean;
    error: string | null;

    loginUser: (data: LoginRequest) => Promise<boolean>;
    logoutUser: () => Promise<void>;
    restoreAuthSession: () => Promise<void>;
    clearError: () => void;
};

export const useAuthStore = create<AuthState>((set) => ({
    isAuthenticated: false,
    isLoading: false,
    error: null,

    loginUser: async (data) => {
        try {
            set({
                isLoading: true,
                error: null,
            });

            const tokens = await login(data);

            await saveTokens(tokens);

            set({
                isAuthenticated: true,
                isLoading: false,
                error: null,
            });

            return true;
        } catch (error: any) {
            const errorMessage =
                error.response?.data?.message ||
                error.message ||
                "Login failed. Please try again.";

            set({
                isAuthenticated: false,
                isLoading: false,
                error: errorMessage,
            });

            return false;
        }
    },

    logoutUser: async () => {
        await clearTokens();

        set({
            isAuthenticated: false,
            isLoading: false,
            error: null,
        });
    },

    restoreAuthSession: async () => {
        try {
            console.log("Restore session started");
            set({
                isLoading: true,
                error: null,
            });

            const accessToken = await getAccessToken();
            const refreshToken = await getRefreshToken();
            const sessionToken = await getSessionToken();

            if (!accessToken || !refreshToken || !sessionToken) {
                await clearTokens();

                set({
                    isAuthenticated: false,
                    isLoading: false,
                    error: null,
                });

                return;
            }
            console.log("Restoring session: refreshing access token...");
            await refreshAccessToken();
            console.log("Session restored successfully.");
            set({
                isAuthenticated: true,
                isLoading: false,
                error: null,
            });
        } catch (error: any) {
            console.log(
                "Restore session failed:",
                error.response?.data || error.message
            );
            await clearTokens();
            set({
                isAuthenticated: false,
                isLoading: false,
                error: null,
            });
        }
    },

    clearError: () => {
        set({error: null});
    },
}));