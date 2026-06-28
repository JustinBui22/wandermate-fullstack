import { StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type ErrorMessageProps = Readonly<{
    message?: string | null;
    title?: string;
}>;

export function ErrorMessage({
                                 message,
                                 title = "Something went wrong",
                             }: ErrorMessageProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    if (!message) {
        return null;
    }

    return (
        <View
            style={[
                styles.container,
                {
                    backgroundColor: colors.dangerSoft,
                    borderColor: colors.danger,
                },
            ]}
        >
            <Ionicons name="alert-circle-outline" size={22} color={colors.danger} />

            <View style={styles.textGroup}>
                <Text style={[styles.title, { color: colors.danger }]}>
                    {title}
                </Text>
                <Text style={[styles.message, { color: colors.text }]}>
                    {message}
                </Text>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        borderWidth: 1,
        borderRadius: radius.lg,
        padding: spacing.md,
        flexDirection: "row",
        gap: spacing.sm,
    },
    textGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    title: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    message: {
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
});