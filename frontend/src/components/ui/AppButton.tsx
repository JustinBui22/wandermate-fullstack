import { type ReactNode } from "react";
import {
    ActivityIndicator,
    Pressable,
    StyleSheet,
    Text,
    View,
    type StyleProp,
    type ViewStyle,
} from "react-native";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type AppButtonVariant = "primary" | "secondary" | "outline" | "danger" | "ghost";
type AppButtonSize = "sm" | "md" | "lg";

type AppButtonProps = Readonly<{
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
    testID?: string;
}>;

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
                              testID,
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
            testID={testID}
            style={({ pressed }) => [
                styles.button,
                styles[size],
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
                <View style={styles.content}>
                    {leftIcon}
                    <Text
                        style={[
                            styles.title,
                            styles[`${size}Text`],
                            { color: textColor },
                        ]}
                    >
                        {title}
                    </Text>
                    {rightIcon}
                </View>
            )}
        </Pressable>
    );
}

const styles = StyleSheet.create({
    button: {
        borderRadius: radius.lg,
        borderWidth: 1,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
    },
    content: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.sm,
    },
    sm: {
        minHeight: 40,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.sm,
    },
    md: {
        minHeight: 48,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.md,
    },
    lg: {
        minHeight: 54,
        paddingHorizontal: spacing.xl,
        paddingVertical: spacing.md,
    },
    fullWidth: {
        width: "100%",
    },
    title: {
        fontWeight: fontWeight.bold,
    },
    smText: {
        fontSize: typography.bodySmall,
    },
    mdText: {
        fontSize: typography.body,
    },
    lgText: {
        fontSize: typography.body,
    },
    pressed: {
        opacity: 0.88,
        transform: [{ scale: 0.99 }],
    },
    disabled: {
        opacity: 0.6,
    },
});