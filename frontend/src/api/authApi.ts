import { axiosClient } from "./axiosClient";
import type {
    ApiResponse,
    ForgotPasswordRequest,
    LoginRequest,
    LoginTokens,
    RegisterRequest,
    RegisterVerifyRequest,
    SendOtpRequest,
} from "../types/auth";

export async function login(data: LoginRequest): Promise<LoginTokens> {
    const response = await axiosClient.post<ApiResponse<LoginTokens>>(
        "/api/v1/users/login",
        data
    );

    return response.data.body;
}

export async function logout(): Promise<void> {
    await axiosClient.post<ApiResponse<unknown>>("/api/v1/users/logout", {});
}

export async function verifyRegisterDetails(data: RegisterVerifyRequest): Promise<void> {
    await axiosClient.post<ApiResponse<unknown>>("/api/v1/users/register/verify", {
        ...data,
        otp: "",
    });
}

export async function sendOtp(data: SendOtpRequest): Promise<void> {
    await axiosClient.post<ApiResponse<unknown>>("/api/v1/otp/send", data);
}

export async function register(data: RegisterRequest): Promise<void> {
    await axiosClient.post<ApiResponse<unknown>>("/api/v1/users/register", data);
}

export async function forgotPassword(data: ForgotPasswordRequest): Promise<void> {
    await axiosClient.post<ApiResponse<unknown>>("/api/v1/users/forgot-password", data);
}
