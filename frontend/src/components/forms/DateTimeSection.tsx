import { Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";

import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
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
    return (
        <View style={styles.dateSection}>
            <Text style={styles.sectionTitle}>{title}</Text>

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
    return (
        <Pressable
            accessibilityRole="button"
            onPress={onPress}
            style={({ pressed }) => [styles.pickerButton, pressed && styles.pickerButtonPressed]}
        >
            <View style={styles.pickerIconBadge}>
                <Ionicons name={icon} size={19} color={colors.primary} />
            </View>

            <View style={styles.pickerTextGroup}>
                <Text style={styles.pickerLabel}>{label}</Text>
                <Text style={styles.pickerValue} numberOfLines={1}>
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
        color: colors.text,
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
        borderColor: colors.border,
        backgroundColor: colors.surface,
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
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    pickerTextGroup: {
        flex: 1,
        gap: 2,
    },
    pickerLabel: {
        color: colors.textMuted,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.4,
    },
    pickerValue: {
        color: colors.text,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
});