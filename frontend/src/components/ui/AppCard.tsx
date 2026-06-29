import { type ReactNode } from "react";
import {
    Pressable,
    StyleSheet,
    Text,
    View,
    type StyleProp,
    type ViewStyle,
} from "react-native";

import { fontWeight, radius, shadow, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type AppCardVariant = "default" | "soft" | "outline";

type AppCardProps = Readonly<{
    children?: ReactNode;
    title?: string;
    subtitle?: string;
    footer?: ReactNode;
    onPress?: () => void;
    variant?: AppCardVariant;
    style?: StyleProp<ViewStyle>;
    contentStyle?: StyleProp<ViewStyle>;
    testID?: string;
}>;

export function AppCard({
                            children,
                            title,
                            subtitle,
                            footer,
                            onPress,
                            variant = "default",
                            style,
                            contentStyle,
                            testID,
                        }: AppCardProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    const cardBackgroundColor =
        variant === "soft" ? colors.surfaceSoft : colors.card;

    const cardBorderColor =
        variant === "outline" ? colors.borderStrong : colors.border;

    const cardContent = (
        <>
            {title || subtitle ? (
                <View style={styles.header}>
                    {title ? (
                        <Text style={[styles.title, { color: colors.text }]}>
                            {title}
                        </Text>
                    ) : null}

                    {subtitle ? (
                        <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                            {subtitle}
                        </Text>
                    ) : null}
                </View>
            ) : null}

            {children ? (
                <View style={contentStyle}>
                    {children}
                </View>
            ) : null}

            {footer ? (
                <View style={styles.footer}>
                    {footer}
                </View>
            ) : null}
        </>
    );

    const cardStyle = [
        styles.card,
        {
            backgroundColor: cardBackgroundColor,
            borderColor: cardBorderColor,
        },
        variant !== "soft" && shadow.card,
        style,
    ];

    if (onPress) {
        return (
            <Pressable
                onPress={onPress}
                testID={testID}
                style={({ pressed }) => [
                    cardStyle,
                    pressed && styles.pressed,
                ]}
            >
                {cardContent}
            </Pressable>
        );
    }

    return (
        <View style={cardStyle} testID={testID}>
            {cardContent}
        </View>
    );
}

const styles = StyleSheet.create({
    card: {
        borderWidth: 1,
        borderRadius: radius.xl,
        padding: spacing.lg,
        gap: spacing.md,
    },
    pressed: {
        opacity: 0.9,
        transform: [{ scale: 0.995 }],
    },
    header: {
        gap: spacing.xs,
    },
    title: {
        fontSize: typography.subheading,
        fontWeight: fontWeight.bold,
    },
    subtitle: {
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    footer: {
        marginTop: spacing.sm,
    },
});