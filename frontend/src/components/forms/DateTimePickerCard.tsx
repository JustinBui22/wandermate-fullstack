import { type ComponentProps } from "react";
import { Platform, Pressable, StyleSheet, Text, View } from "react-native";
import DateTimePicker from "@react-native-community/datetimepicker";

import { AppCard } from "@/src/components/ui/AppCard";
import { colors, fontWeight, spacing, typography } from "@/src/constants/theme";
import {
    getPickerMode,
    getPickerTitle,
    getPickerValue,
    type PickerTarget,
} from "@/src/utils/dateTimePickerUtils";

type DateTimePickerValueChange = NonNullable<
    ComponentProps<typeof DateTimePicker>["onValueChange"]
>;

type DateTimePickerCardProps = Readonly<{
    activePicker: Exclude<PickerTarget, null>;
    startDateTime: Date;
    endDateTime: Date;
    onChangeDate: (selectedDate: Date) => void;
    onClose: () => void;
}>;

export function DateTimePickerCard({
                                       activePicker,
                                       startDateTime,
                                       endDateTime,
                                       onChangeDate,
                                       onClose,
                                   }: DateTimePickerCardProps) {
    const handlePickerValueChange: DateTimePickerValueChange = (_event, selectedDate) => {
        if (selectedDate) {
            onChangeDate(selectedDate);
        }

        if (Platform.OS === "android") {
            onClose();
        }
    };

    return (
        <AppCard variant="outline" contentStyle={styles.pickerCardContent}>
            <View style={styles.pickerHeader}>
                <Text style={styles.pickerTitle}>{getPickerTitle(activePicker)}</Text>

                {Platform.OS === "ios" ? (
                    <Pressable onPress={onClose} hitSlop={10}>
                        <Text style={styles.doneText}>Done</Text>
                    </Pressable>
                ) : null}
            </View>

            <DateTimePicker
                value={getPickerValue(activePicker, startDateTime, endDateTime)}
                mode={getPickerMode(activePicker)}
                display={Platform.OS === "ios" ? "spinner" : "default"}
                onValueChange={handlePickerValueChange}
                onDismiss={onClose}
            />
        </AppCard>
    );
}

const styles = StyleSheet.create({
    pickerCardContent: {
        gap: spacing.md,
    },
    pickerHeader: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        gap: spacing.md,
    },
    pickerTitle: {
        color: colors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    doneText: {
        color: colors.primary,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
});