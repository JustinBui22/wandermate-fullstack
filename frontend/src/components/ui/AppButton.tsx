import type { ReactNode } from "react";
import {
    ActivityIndicator,
    Pressable,
    StyleProp,
    StyleSheet,
    Text,
    TextStyle,
    View,
    ViewStyle,
} from "react-native";

import { colors, fontWeight, layout, radius, shadows, spacing, typography } from "@/src/constants/theme";

type AppButtonVariant = "primary" | "secondary" | "outline" | "ghost" | "danger";
type AppButtonSize = "sm" | "md" | "lg";

type AppButtonProps = {
    title: string;
    onPress?: () => void;
    variant?: AppButtonVariant;
    size?: AppButtonSize;
    loading?: boolean;
    disabled?: boolean;
    fullWidth?: boolean;
    leftIcon?: ReactNode;
    rightIcon?: ReactNode;
    style?: StyleProp<ViewStyle>;
    textStyle?: StyleProp<TextStyle>;
    testID?: string;
};

const textVariantStyle: Record<AppButtonVariant, TextStyle> = {
    primary: { color: colors.textLight },
    secondary: { color: colors.primaryDark },
    outline: { color: colors.primaryDark },
    ghost: { color: colors.primaryDark },
    danger: { color: colors.textLight },
};

export function AppButton({
    title,
    onPress,
    variant = "primary",
    size = "md",
    loading = false,
    disabled = false,
    fullWidth = true,
    leftIcon,
    rightIcon,
    style,
    textStyle,
    testID,
}: AppButtonProps) {
    const isDisabled = disabled || loading;

    return (
        <Pressable
            accessibilityRole="button"
            accessibilityState={{ disabled: isDisabled, busy: loading }}
            disabled={isDisabled}
            onPress={onPress}
            style={({ pressed }) => [
                styles.base,
                styles[variant],
                styles[size],
                fullWidth && styles.fullWidth,
                pressed && !isDisabled && styles.pressed,
                isDisabled && styles.disabled,
                style,
            ]}
            testID={testID}
        >
            {loading ? (
                <ActivityIndicator color={variant === "primary" || variant === "danger" ? colors.textLight : colors.primary} />
            ) : (
                <View style={styles.content}>
                    {leftIcon}
                    <Text style={[styles.text, textVariantStyle[variant], textStyle]}>{title}</Text>
                    {rightIcon}
                </View>
            )}
        </Pressable>
    );
}

const styles = StyleSheet.create({
    base: {
        minHeight: layout.buttonHeight,
        borderRadius: radius.md,
        alignItems: "center",
        justifyContent: "center",
        paddingHorizontal: spacing.lg,
        borderWidth: 1,
        borderColor: "transparent",
    },
    fullWidth: {
        width: "100%",
    },
    sm: {
        minHeight: 40,
        paddingHorizontal: spacing.md,
    },
    md: {
        minHeight: layout.buttonHeight,
    },
    lg: {
        minHeight: 58,
        borderRadius: radius.lg,
    },
    primary: {
        backgroundColor: colors.primary,
        ...shadows.button,
    },
    secondary: {
        backgroundColor: colors.primarySoft,
    },
    outline: {
        backgroundColor: colors.surface,
        borderColor: colors.borderStrong,
    },
    ghost: {
        backgroundColor: "transparent",
    },
    danger: {
        backgroundColor: colors.danger,
    },
    disabled: {
        opacity: 0.55,
    },
    pressed: {
        opacity: 0.88,
        transform: [{ scale: 0.99 }],
    },
    content: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.sm,
    },
    text: {
        fontSize: typography.body,
        fontWeight: fontWeight.semibold,
        textAlign: "center",
    },
});
