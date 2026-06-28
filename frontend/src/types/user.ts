import type { ApiResponse } from "./auth";

import type { ThemePreference } from "@/src/theme/appTheme";

export type UserThemePreference = ThemePreference;

export type UserProfile = {
    userId: number;
    username: string;
    displayName: string;
    email: string;
    phoneNumber?: string | null;
    dob?: string | null;
    preferredTheme: UserThemePreference;
    profileImageUrl?: string | null;
    createdDate?: string | null;
    modifiedDate?: string | null;
};

export type UpdateUserProfileRequest = {
    displayName?: string;
    phoneNumber?: string;
    dob?: string;
    profileImageUrl?: string;
};

export type UpdateUserSettingsRequest = {
    preferredTheme: UserThemePreference;
};

export type UserApiResponse<T> = ApiResponse<T>;