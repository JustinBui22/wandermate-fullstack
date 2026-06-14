import type { ReactNode } from "react";
import {
    Pressable,
    StyleProp,
    StyleSheet,
    Text,
    View,
    ViewStyle,
} from "react-native";

import { colors, fontWeight, radius, shadows, spacing, typography } from "@/src/constants/theme";

type AppCardVariant = "default" | "soft" | "outline";

type AppCardProps = {
    children?: ReactNode;
    title?: string;
    subtitle?: string;
    footer?: ReactNode;
    variant?: AppCardVariant;
    onPress?: () => void;
    style?: StyleProp<ViewStyle>;
    contentStyle?: StyleProp<ViewStyle>;
    testID?: string;
};

export function AppCard({
    children,
    title,
    subtitle,
    footer,
    variant = "default",
    onPress,
    style,
    contentStyle,
    testID,
}: AppCardProps) {
    const content = (
        <View style={[styles.content, contentStyle]}>
            {(title || subtitle) && (
                <View style={styles.header}>
                    {title ? <Text style={styles.title}>{title}</Text> : null}
                    {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
                </View>
            )}

            {children}

            {footer ? <View style={styles.footer}>{footer}</View> : null}
        </View>
    );

    if (onPress) {
        return (
            <Pressable
                accessibilityRole="button"
                onPress={onPress}
                style={({ pressed }) => [
                    styles.base,
                    styles[variant],
                    pressed && styles.pressed,
                    style,
                ]}
                testID={testID}
            >
                {content}
            </Pressable>
        );
    }

    return (
        <View style={[styles.base, styles[variant], style]} testID={testID}>
            {content}
        </View>
    );
}

const styles = StyleSheet.create({
    base: {
        borderRadius: radius.lg,
        overflow: "hidden",
    },
    default: {
        backgroundColor: colors.surface,
        ...shadows.card,
    },
    soft: {
        backgroundColor: colors.surfaceSoft,
        borderWidth: 1,
        borderColor: colors.border,
    },
    outline: {
        backgroundColor: colors.surface,
        borderWidth: 1,
        borderColor: colors.border,
    },
    pressed: {
        opacity: 0.88,
        transform: [{ scale: 0.995 }],
    },
    content: {
        padding: spacing.lg,
        gap: spacing.md,
    },
    header: {
        gap: spacing.xs,
    },
    title: {
        color: colors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    subtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    footer: {
        borderTopWidth: 1,
        borderTopColor: colors.border,
        paddingTop: spacing.md,
    },
});
