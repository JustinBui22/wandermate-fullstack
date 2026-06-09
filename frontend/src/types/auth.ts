export type LoginRequest = {
    username: string;
    password: string;
    overrideMaxSession?: boolean;
};

export type LoginTokens = {
    accessToken: string;
    refreshToken: string;
    sessionToken: string;
};

export type OtpVerificationMethod = "EMAIL_OTP" | "PHONE_NUM_OTP";

export type RegisterRequest = {
    username: string;
    password: string;
    email: string;
    phoneNumber: string;
    dob: string;
    otp: string;
};

export type RegisterVerifyRequest = Omit<RegisterRequest, "otp">;

export type SendOtpRequest =
    | {
          userName: string;
          otpVerificationMethod: "EMAIL_OTP";
          email: string;
          emailEnum: "EMAIL_OTP_REGISTER";
      }
    | {
          userName: string;
          otpVerificationMethod: "PHONE_NUM_OTP";
          phoneNumber: string;
          smsEnum: "SMS_OTP_REGISTER";
      };

export type ForgotPasswordRequest = {
    username: string;
    newPassword: string;
    otp: string;
    email?: string;
    phoneNumber?: string;
};

export type ApiResponse<T> = {
    code: string;
    message: string;
    flow?: string;
    body: T;
};
