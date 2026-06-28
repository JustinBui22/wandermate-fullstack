import { ActivityIndicator, StyleSheet, Text, View } from "react-native";

import { fontWeight, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type LoadingStateProps = Readonly<{
    title?: string;
    subtitle?: string;
    fullScreen?: boolean;
}>;

export function LoadingState({
                                 title = "Loading...",
                                 subtitle,
                                 fullScreen = false,
                             }: LoadingStateProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View style={[styles.container, fullScreen && styles.fullScreen]}>
            <ActivityIndicator size="large" color={colors.primary} />

            <View style={styles.textGroup}>
                <Text style={[styles.title, { color: colors.text }]}>
                    {title}
                </Text>

                {subtitle ? (
                    <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                        {subtitle}
                    </Text>
                ) : null}
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.md,
    },
    fullScreen: {
        flex: 1,
    },
    textGroup: {
        alignItems: "center",
        gap: spacing.xs,
    },
    title: {
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    subtitle: {
        fontSize: typography.bodySmall,
        textAlign: "center",
        lineHeight: 20,
    },
});