import { type ReactNode } from "react";
import {
    ActivityIndicator,
    Pressable,
    StyleSheet,
    Text,
    type StyleProp,
    type ViewStyle,
} from "react-native";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type AppButtonVariant = "primary" | "secondary" | "outline" | "danger" | "ghost";

type AppButtonProps = Readonly<{
    title: string;
    onPress?: () => void;
    variant?: AppButtonVariant;
    loading?: boolean;
    disabled?: boolean;
    fullWidth?: boolean;
    leftIcon?: ReactNode;
    style?: StyleProp<ViewStyle>;
}>;

export function AppButton({
                              title,
                              onPress,
                              variant = "primary",
                              loading = false,
                              disabled = false,
                              fullWidth = true,
                              leftIcon,
                              style,
                          }: AppButtonProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    const isDisabled = disabled || loading;

    const backgroundColor =
        variant === "primary"
            ? colors.primary
            : variant === "secondary"
                ? colors.primarySoft
                : variant === "danger"
                    ? colors.danger
                    : "transparent";

    const borderColor =
        variant === "outline"
            ? colors.borderStrong
            : variant === "danger"
                ? colors.danger
                : backgroundColor;

    const textColor =
        variant === "primary" || variant === "danger"
            ? colors.textLight
            : variant === "secondary"
                ? colors.primaryDark
                : colors.text;

    return (
        <Pressable
            accessibilityRole="button"
            disabled={isDisabled}
            onPress={onPress}
            style={({ pressed }) => [
                styles.button,
                fullWidth && styles.fullWidth,
                {
                    backgroundColor,
                    borderColor,
                },
                pressed && !isDisabled && styles.pressed,
                isDisabled && styles.disabled,
                style,
            ]}
        >
            {loading ? (
                <ActivityIndicator color={textColor} />
            ) : (
                <>
                    {leftIcon}
                    <Text style={[styles.title, { color: textColor }]}>
                        {title}
                    </Text>
                </>
            )}
        </Pressable>
    );
}

const styles = StyleSheet.create({
    button: {
        minHeight: 48,
        borderRadius: radius.lg,
        borderWidth: 1,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.sm,
    },
    fullWidth: {
        width: "100%",
    },
    title: {
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    pressed: {
        opacity: 0.88,
        transform: [{ scale: 0.99 }],
    },
    disabled: {
        opacity: 0.6,
    },
});