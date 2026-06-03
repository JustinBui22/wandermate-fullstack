import { axiosClient } from "./axiosClient";
import type { ApiResponse, LoginRequest, LoginTokens } from "../types/auth";

export async function login(data: LoginRequest): Promise<LoginTokens> {
    const response = await axiosClient.post<ApiResponse<LoginTokens>>(
        "/api/v1/users/login",
        data
    );

    return response.data.body;
}