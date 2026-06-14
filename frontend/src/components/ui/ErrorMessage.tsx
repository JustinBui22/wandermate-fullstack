import { StyleProp, StyleSheet, Text, View, ViewStyle } from "react-native";

import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";

type ErrorMessageProps = {
    message?: string | string[] | null;
    title?: string;
    compact?: boolean;
    style?: StyleProp<ViewStyle>;
    testID?: string;
};

export function ErrorMessage({
    message,
    title = "Something went wrong",
    compact = false,
    style,
    testID,
}: ErrorMessageProps) {
    if (!message || (Array.isArray(message) && message.length === 0)) {
        return null;
    }

    const messages = Array.isArray(message) ? message : [message];

    return (
        <View style={[styles.container, compact && styles.compact, style]} testID={testID}>
            {!compact ? <Text style={styles.title}>{title}</Text> : null}
            {messages.map((item) => (
                <Text key={item} style={styles.message}>
                    {item}
                </Text>
            ))}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        backgroundColor: colors.dangerSoft,
        borderColor: colors.danger,
        borderWidth: 1,
        borderRadius: radius.md,
        padding: spacing.md,
        gap: spacing.xs,
    },
    compact: {
        paddingVertical: spacing.sm,
    },
    title: {
        color: colors.danger,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    message: {
        color: colors.danger,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
});
