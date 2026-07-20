import { clearTokens } from "@/src/stores/tokenStore";

type SessionExpiredHandler = () => void | Promise<void>;

let sessionExpiredHandler: SessionExpiredHandler | null = null;
let expirationPromise: Promise<void> | null = null;

export function registerSessionExpiredHandler(handler: SessionExpiredHandler) {
    sessionExpiredHandler = handler;

    return () => {
        if (sessionExpiredHandler === handler) {
            sessionExpiredHandler = null;
        }
    };
}

export async function expireLocalSession() {
    expirationPromise ??= performExpiration().finally(() => {
        expirationPromise = null;
    });

    return expirationPromise;
}

async function performExpiration() {
    await clearTokens();
    await sessionExpiredHandler?.();
}
