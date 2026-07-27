import { Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import { formatDisplayDate } from "@/src/utils/dateTimePickerUtils";

type DateSectionProps = Readonly<{
    title: string;
    date: Date;
    onDatePress: () => void;
}>;

export function DateSection({ title, date, onDatePress }: DateSectionProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View style={styles.dateSection}>
            <Text style={[styles.sectionTitle, { color: colors.text }]}>
                {title}
            </Text>

            <Pressable
                accessibilityRole="button"
                onPress={onDatePress}
                style={({ pressed }) => [
                    styles.pickerButton,
                    {
                        borderColor: colors.border,
                        backgroundColor: colors.surface,
                    },
                    pressed && styles.pickerButtonPressed,
                ]}
            >
                <View
                    style={[
                        styles.pickerIconBadge,
                        { backgroundColor: colors.primarySoft },
                    ]}
                >
                    <Ionicons name="calendar-outline" size={19} color={colors.primary} />
                </View>

                <View style={styles.pickerTextGroup}>
                    <Text style={[styles.pickerLabel, { color: colors.textMuted }]}>Date</Text>
                    <Text
                        style={[styles.pickerValue, { color: colors.text }]}
                        numberOfLines={1}
                    >
                        {formatDisplayDate(date)}
                    </Text>
                </View>
            </Pressable>
        </View>
    );
}

const styles = StyleSheet.create({
    dateSection: {
        gap: spacing.sm,
    },
    sectionTitle: {
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    pickerButton: {
        minHeight: 64,
        borderRadius: radius.md,
        borderWidth: 1,
        paddingHorizontal: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    pickerButtonPressed: {
        opacity: 0.86,
        transform: [{ scale: 0.99 }],
    },
    pickerIconBadge: {
        width: 38,
        height: 38,
        borderRadius: radius.md,
        alignItems: "center",
        justifyContent: "center",
    },
    pickerTextGroup: {
        flex: 1,
        gap: 2,
    },
    pickerLabel: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.4,
    },
    pickerValue: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
});
