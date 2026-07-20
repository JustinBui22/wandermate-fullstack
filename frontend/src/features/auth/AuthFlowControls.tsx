import type { ComponentProps } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type IoniconName = ComponentProps<typeof Ionicons>["name"];

type OtpMethodButtonProps = {
    label: string;
    icon: IoniconName;
    selected: boolean;
    onPress: () => void;
};

export function OtpMethodButton({
    label,
    icon,
    selected,
    onPress,
}: OtpMethodButtonProps) {
    const colors = useAppTheme().colors;

    return (
        <Pressable
            accessibilityRole="radio"
            accessibilityState={{ checked: selected }}
            accessibilityLabel={`${label} OTP`}
            onPress={onPress}
            style={({ pressed }) => [
                styles.methodButton,
                {
                    backgroundColor: selected ? colors.primarySoft : colors.surface,
                    borderColor: selected ? colors.primary : colors.border,
                },
                pressed && styles.pressed,
            ]}
        >
            <Ionicons
                name={icon}
                size={19}
                color={selected ? colors.primary : colors.textMuted}
            />
            <Text
                style={[
                    styles.methodText,
                    { color: selected ? colors.primary : colors.textMuted },
                ]}
            >
                {label}
            </Text>
        </Pressable>
    );
}

type OtpCooldownBadgeProps = {
    seconds: number;
};

export function OtpCooldownBadge({ seconds }: OtpCooldownBadgeProps) {
    const colors = useAppTheme().colors;

    if (seconds <= 0) {
        return null;
    }

    return (
        <View
            accessibilityRole="timer"
            accessibilityLabel={`OTP resend available in ${seconds} seconds`}
            style={[
                styles.cooldownBadge,
                {
                    backgroundColor: colors.surfaceSoft,
                    borderColor: colors.border,
                },
            ]}
        >
            <Ionicons name="time-outline" size={15} color={colors.textMuted} />
            <Text style={[styles.cooldownText, { color: colors.textMuted }]}>
                {formatTimer(seconds)}
            </Text>
        </View>
    );
}

function formatTimer(seconds: number) {
    const safeSeconds = Math.max(0, seconds);
    const minutes = Math.floor(safeSeconds / 60);
    const remainingSeconds = safeSeconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, "0")}`;
}

const styles = StyleSheet.create({
    methodButton: {
        flex: 1,
        minHeight: 48,
        borderRadius: radius.md,
        borderWidth: 1,
        alignItems: "center",
        justifyContent: "center",
        flexDirection: "row",
        gap: spacing.sm,
    },
    methodText: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    pressed: {
        opacity: 0.85,
        transform: [{ scale: 0.99 }],
    },
    cooldownBadge: {
        minHeight: 38,
        borderRadius: radius.pill,
        borderWidth: 1,
        paddingHorizontal: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.xs,
    },
    cooldownText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
    },
});
