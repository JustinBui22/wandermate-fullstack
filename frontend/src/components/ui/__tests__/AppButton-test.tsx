import { fireEvent, render, screen } from "@testing-library/react-native";
import { describe, expect, it, jest } from "@jest/globals";

import { AppButton } from "@/src/components/ui/AppButton";

jest.mock("@/src/hooks/useAppTheme", () => ({
    useAppTheme: () => ({
        name: "LIGHT",
        colors: {
            primary: "#2563EB",
            primarySoft: "#DBEAFE",
            primaryDark: "#1D4ED8",
            danger: "#DC2626",
            text: "#111827",
            textLight: "#FFFFFF",
            borderStrong: "#9CA3AF",
        },
    }),
}));

describe("AppButton", () => {
    it("renders its label and invokes the press handler", async () => {
        const onPress = jest.fn();

        await render(
            <AppButton
                title="Create trip"
                onPress={onPress}
                testID="create-trip-button"
            />
        );

        expect(screen.getByText("Create trip")).toBeOnTheScreen();
        fireEvent.press(screen.getByTestId("create-trip-button"));
        expect(onPress).toHaveBeenCalledTimes(1);
    });

    it("does not invoke the press handler while disabled", async () => {
        const onPress = jest.fn();

        await render(
            <AppButton
                title="Create trip"
                onPress={onPress}
                disabled
                testID="disabled-create-trip-button"
            />
        );

        fireEvent.press(screen.getByTestId("disabled-create-trip-button"));
        expect(onPress).not.toHaveBeenCalled();
    });
});
