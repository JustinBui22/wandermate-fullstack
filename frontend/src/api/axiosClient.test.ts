import { AxiosError } from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";

const tokenStoreMocks = vi.hoisted(() => ({
    getAccessToken: vi.fn(),
    getSessionToken: vi.fn(),
}));

const sessionLifecycleMocks = vi.hoisted(() => ({
    expireLocalSession: vi.fn(),
}));

const refreshMocks = vi.hoisted(() => ({
    refreshAccessToken: vi.fn(),
}));

vi.mock("@/src/stores/tokenStore", () => tokenStoreMocks);
vi.mock("@/src/refreshApi", () => refreshMocks);
vi.mock("@/src/auth/sessionLifecycle", () => sessionLifecycleMocks);

import { axiosClient } from "@/src/api/axiosClient";

describe("axiosClient authentication handling", () => {
    beforeEach(() => {
        tokenStoreMocks.getAccessToken.mockReset().mockResolvedValue("access-token");
        tokenStoreMocks.getSessionToken.mockReset().mockResolvedValue("session-token");
        sessionLifecycleMocks.expireLocalSession.mockReset().mockResolvedValue(undefined);
        refreshMocks.refreshAccessToken.mockReset();
    });

    it("keeps authentication for an unrelated 403 authorization response", async () => {
        await expect(rejectWith(403, {
            code: "E080",
            message: "You do not have permission to edit this trip",
        })).rejects.toBeInstanceOf(AxiosError);

        expect(sessionLifecycleMocks.expireLocalSession).not.toHaveBeenCalled();
    });

    it("clears authentication when a 403 explicitly reports an invalid session", async () => {
        await expect(rejectWith(403, {
            code: "E023",
            message: "Session token invalid",
        })).rejects.toBeInstanceOf(AxiosError);

        expect(sessionLifecycleMocks.expireLocalSession).toHaveBeenCalledOnce();
    });

    it("clears authentication for an unrecoverable 401 response", async () => {
        await expect(rejectWith(401, {
            code: "E015",
            message: "Token verify fail",
        })).rejects.toBeInstanceOf(AxiosError);

        expect(sessionLifecycleMocks.expireLocalSession).toHaveBeenCalledOnce();
    });
});

async function rejectWith(status: number, data: Record<string, unknown>) {
    return axiosClient.get("/integration-test", {
        adapter: async (config) => {
            throw new AxiosError(
                `Request failed with status ${status}`,
                "ERR_BAD_RESPONSE",
                config,
                undefined,
                {
                    data,
                    status,
                    statusText: "Error",
                    headers: {},
                    config,
                }
            );
        },
    });
}
