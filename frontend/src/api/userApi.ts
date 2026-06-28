import { axiosClient } from "./axiosClient";
import type {
    UpdateUserProfileRequest,
    UpdateUserSettingsRequest,
    UserApiResponse,
    UserProfile,
} from "../types/user";

export async function getMyProfile(): Promise<UserProfile> {
    const response = await axiosClient.get<UserApiResponse<UserProfile>>(
        "/api/v1/users/me"
    );

    return response.data.body;
}

export async function updateMyProfile(
    data: UpdateUserProfileRequest
): Promise<UserProfile> {
    const response = await axiosClient.patch<UserApiResponse<UserProfile>>(
        "/api/v1/users/me/profile",
        data
    );

    return response.data.body;
}

export async function updateMySettings(
    data: UpdateUserSettingsRequest
): Promise<UserProfile> {
    const response = await axiosClient.patch<UserApiResponse<UserProfile>>(
        "/api/v1/users/me/settings",
        data
    );

    return response.data.body;
}