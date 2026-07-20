const DEFAULT_API_BASE_URL = "https://wandermate-fullstack.onrender.com/Wandermate";

export const APP_ENV =
    process.env.EXPO_PUBLIC_APP_ENV?.trim() || "production";

export const API_BASE_URL =
    process.env.EXPO_PUBLIC_API_BASE_URL?.trim() || DEFAULT_API_BASE_URL;
