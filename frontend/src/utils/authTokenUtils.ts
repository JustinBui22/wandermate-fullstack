import { getAccessToken, getStoredUsername } from "../stores/tokenStore";

function decodeBase64Url(value: string) {
    const base64 = value.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");

    if (typeof globalThis.atob === "function") {
        return globalThis.atob(padded);
    }

    return "";
}

export async function getCurrentUsernameFromAccessToken(): Promise<string | null> {
    try {
        const storedUsername = await getStoredUsername();

        if (storedUsername) {
            return storedUsername;
        }

        const accessToken = await getAccessToken();

        if (!accessToken) {
            return null;
        }

        const payload = accessToken.split(".")[1];

        if (!payload) {
            return null;
        }

        const decodedPayload = JSON.parse(decodeBase64Url(payload));

        return decodedPayload.sub ?? decodedPayload.username ?? null;
    } catch {
        return null;
    }
}
