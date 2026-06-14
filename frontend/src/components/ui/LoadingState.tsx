import { ActivityIndicator, StyleProp, StyleSheet, Text, View, ViewStyle } from "react-native";

import { colors, fontWeight, spacing, typography } from "@/src/constants/theme";

type LoadingStateProps = {
    title?: string;
    subtitle?: string;
    fullScreen?: boolean;
    style?: StyleProp<ViewStyle>;
    testID?: string;
};

export function LoadingState({
    title = "Loading...",
    subtitle,
    fullScreen = false,
    style,
    testID,
}: LoadingStateProps) {
    return (
        <View style={[styles.container, fullScreen && styles.fullScreen, style]} testID={testID}>
            <ActivityIndicator color={colors.primary} size="large" />
            <View style={styles.textContainer}>
                <Text style={styles.title}>{title}</Text>
                {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.md,
        padding: spacing.xl,
    },
    fullScreen: {
        flex: 1,
        minHeight: 360,
    },
    textContainer: {
        alignItems: "center",
        gap: spacing.xs,
    },
    title: {
        color: colors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.semibold,
        textAlign: "center",
    },
    subtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        textAlign: "center",
        lineHeight: 20,
    },
});
