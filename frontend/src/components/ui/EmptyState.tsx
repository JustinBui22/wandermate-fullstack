import type { ReactNode } from "react";
import { StyleProp, StyleSheet, Text, View, ViewStyle } from "react-native";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
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
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View
            style={[
                styles.container,
                {
                    backgroundColor: colors.surface,
                    borderColor: colors.border,
                },
                style,
            ]}
            testID={testID}
        >
            {icon ? (
                <View
                    style={[
                        styles.iconContainer,
                        { backgroundColor: colors.primarySoft },
                    ]}
                >
                    {icon}
                </View>
            ) : null}

            <View style={styles.textContainer}>
                <Text style={[styles.title, { color: colors.text }]}>{title}</Text>
                {message ? (
                    <Text style={[styles.message, { color: colors.textMuted }]}>
                        {message}
                    </Text>
                ) : null}
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
        borderRadius: radius.lg,
        borderWidth: 1,
    },
    iconContainer: {
        width: 56,
        height: 56,
        borderRadius: radius.pill,
        alignItems: "center",
        justifyContent: "center",
    },
    textContainer: {
        alignItems: "center",
        gap: spacing.sm,
    },
    title: {
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
        textAlign: "center",
    },
    message: {
        fontSize: typography.bodySmall,
        textAlign: "center",
        lineHeight: 21,
    },
});
