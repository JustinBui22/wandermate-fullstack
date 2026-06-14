import type { ReactNode } from "react";
import { StyleProp, StyleSheet, Text, View, ViewStyle } from "react-native";

import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { AppButton } from "./AppButton";

type EmptyStateProps = {
    title: string;
    message?: string;
    icon?: ReactNode;
    actionLabel?: string;
    onActionPress?: () => void;
    style?: StyleProp<ViewStyle>;
    testID?: string;
};

export function EmptyState({
    title,
    message,
    icon,
    actionLabel,
    onActionPress,
    style,
    testID,
}: EmptyStateProps) {
    return (
        <View style={[styles.container, style]} testID={testID}>
            {icon ? <View style={styles.iconContainer}>{icon}</View> : null}
            <View style={styles.textContainer}>
                <Text style={styles.title}>{title}</Text>
                {message ? <Text style={styles.message}>{message}</Text> : null}
            </View>
            {actionLabel && onActionPress ? (
                <AppButton title={actionLabel} onPress={onActionPress} fullWidth={false} />
            ) : null}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.lg,
        padding: spacing.xl,
        backgroundColor: colors.surface,
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor: colors.border,
    },
    iconContainer: {
        width: 56,
        height: 56,
        borderRadius: radius.pill,
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    textContainer: {
        alignItems: "center",
        gap: spacing.sm,
    },
    title: {
        color: colors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
        textAlign: "center",
    },
    message: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        textAlign: "center",
        lineHeight: 21,
    },
});
