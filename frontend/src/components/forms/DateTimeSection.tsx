import { Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import { formatDisplayDate, formatDisplayTime } from "@/src/utils/dateTimePickerUtils";

type DateTimeSectionProps = Readonly<{
    title: string;
    dateTime: Date;
    onDatePress: () => void;
    onTimePress: () => void;
}>;

export function DateTimeSection({
    title,
    dateTime,
    onDatePress,
    onTimePress,
}: DateTimeSectionProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View style={styles.dateSection}>
            <Text style={[styles.sectionTitle, { color: colors.text }]}>
                {title}
            </Text>

            <View style={styles.pickerRow}>
                <PickerButton
                    icon="calendar-outline"
                    label="Date"
                    value={formatDisplayDate(dateTime)}
                    onPress={onDatePress}
                />

                <PickerButton
                    icon="time-outline"
                    label="Time"
                    value={formatDisplayTime(dateTime)}
                    onPress={onTimePress}
                />
            </View>
        </View>
    );
}

type PickerButtonProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    label: string;
    value: string;
    onPress: () => void;
}>;

function PickerButton({ icon, label, value, onPress }: PickerButtonProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <Pressable
            accessibilityRole="button"
            onPress={onPress}
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
                <Ionicons name={icon} size={19} color={colors.primary} />
            </View>

            <View style={styles.pickerTextGroup}>
                <Text style={[styles.pickerLabel, { color: colors.textMuted }]}>
                    {label}
                </Text>

                <Text
                    style={[styles.pickerValue, { color: colors.text }]}
                    numberOfLines={1}
                >
                    {value}
                </Text>
            </View>
        </Pressable>
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
    pickerRow: {
        gap: spacing.sm,
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
