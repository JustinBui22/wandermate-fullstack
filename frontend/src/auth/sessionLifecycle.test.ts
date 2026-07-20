import { beforeEach, describe, expect, it, vi } from "vitest";

const tokenStoreMocks = vi.hoisted(() => ({
    clearTokens: vi.fn(),
}));

vi.mock("@/src/stores/tokenStore", () => tokenStoreMocks);

import {
    expireLocalSession,
    registerSessionExpiredHandler,
} from "@/src/auth/sessionLifecycle";

describe("sessionLifecycle", () => {
    beforeEach(() => {
        tokenStoreMocks.clearTokens.mockReset().mockResolvedValue(undefined);
    });

    it("clears secure tokens and notifies the authentication store", async () => {
        const handler = vi.fn();
        const unregister = registerSessionExpiredHandler(handler);

        await expireLocalSession();

        expect(tokenStoreMocks.clearTokens).toHaveBeenCalledOnce();
        expect(handler).toHaveBeenCalledOnce();
        unregister();
    });

    it("does not call a handler after it has been unregistered", async () => {
        const handler = vi.fn();
        const unregister = registerSessionExpiredHandler(handler);
        unregister();

        await expireLocalSession();

        expect(tokenStoreMocks.clearTokens).toHaveBeenCalledOnce();
        expect(handler).not.toHaveBeenCalled();
    });
});
