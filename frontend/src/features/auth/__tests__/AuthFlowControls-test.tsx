import { fireEvent, render, screen } from "@testing-library/react-native";
import { describe, expect, it, jest } from "@jest/globals";

import {
    OtpCooldownBadge,
    OtpMethodButton,
} from "@/src/features/auth/AuthFlowControls";

jest.mock("@/src/hooks/useAppTheme", () => ({
    useAppTheme: () => ({
        name: "LIGHT",
        colors: {
            primary: "#2563EB",
            primarySoft: "#DBEAFE",
            surface: "#FFFFFF",
            surfaceSoft: "#F1F5F9",
            textMuted: "#64748B",
            border: "#E2E8F0",
        },
    }),
}));

describe("AuthFlowControls", () => {
    it("invokes the selected OTP method handler", async () => {
        const onPress = jest.fn();

        await render(
            <OtpMethodButton
                label="Email"
                icon="mail-outline"
                selected
                onPress={onPress}
            />
        );

        fireEvent.press(screen.getByRole("radio", { name: "Email OTP" }));
        expect(onPress).toHaveBeenCalledTimes(1);
    });

    it("renders a formatted cooldown while waiting", async () => {
        await render(<OtpCooldownBadge seconds={65} />);
        expect(screen.getByText("1:05")).toBeOnTheScreen();
    });

    it("renders nothing after the cooldown reaches zero", async () => {
        const result = await render(<OtpCooldownBadge seconds={0} />);
        expect(result.toJSON()).toBeNull();
    });
});
