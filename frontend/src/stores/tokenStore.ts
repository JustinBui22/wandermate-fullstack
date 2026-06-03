import * as SecureStore from "expo-secure-store";
import type { LoginTokens } from "../types/auth";

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";
const SESSION_TOKEN_KEY = "sessionToken";

export async function saveTokens(tokens: LoginTokens) {
    await SecureStore.setItemAsync(ACCESS_TOKEN_KEY, tokens.accessToken);
    await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, tokens.refreshToken);
    await SecureStore.setItemAsync(SESSION_TOKEN_KEY, tokens.sessionToken);
}

export async function getAccessToken() {
    return SecureStore.getItemAsync(ACCESS_TOKEN_KEY);
}

export async function getRefreshToken() {
    return SecureStore.getItemAsync(REFRESH_TOKEN_KEY);
}

export async function getSessionToken() {
    return SecureStore.getItemAsync(SESSION_TOKEN_KEY);
}

export async function clearTokens() {
    await SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY);
    await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY);
    await SecureStore.deleteItemAsync(SESSION_TOKEN_KEY);
}

export async function saveAccessToken(accessToken: string) {
    await SecureStore.setItemAsync(ACCESS_TOKEN_KEY, accessToken);
}

export async function saveRefreshToken(refreshToken: string) {
    await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, refreshToken);
}

export async function saveSessionToken(sessionToken: string) {
    await SecureStore.setItemAsync(SESSION_TOKEN_KEY, sessionToken);
}