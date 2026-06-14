import { useState, type ComponentProps } from "react";
import {
    Alert,
    Platform,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import DateTimePicker from "@react-native-community/datetimepicker";
import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";

import { createTrip } from "@/src/api/tripApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import {
    getApiErrorMessage,
    getApiErrorTitle,
    hasApiWarning,
} from "@/src/utils/apiWarningUtils";
import {
    formatDisplayDate,
    formatDisplayTime,
    formatForBackend,
    type PickerTarget,
    updateDatePart,
    updateTimePart,
} from "@/src/utils/dateTimePickerUtils";
import { logger } from "@/src/utils/logger";

function getDefaultStartDateTime() {
    const today = new Date();
    today.setHours(9, 0, 0, 0);
    return today;
}

function getDefaultEndDateTime() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(18, 0, 0, 0);
    return tomorrow;
}

function getPickerTitle(activePicker: PickerTarget) {
    if (activePicker === "startDate") return "Choose start date";
    if (activePicker === "startTime") return "Choose start time";
    if (activePicker === "endDate") return "Choose end date";
    if (activePicker === "endTime") return "Choose end time";
    return "Choose date/time";
}

export default function CreateTripScreen() {
    const router = useRouter();

    const [tripName, setTripName] = useState("");
    const [destination, setDestination] = useState("");
    const [startDateTime, setStartDateTime] = useState(getDefaultStartDateTime);
    const [endDateTime, setEndDateTime] = useState(getDefaultEndDateTime);
    const [activePicker, setActivePicker] = useState<PickerTarget>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    function applySelectedDateTime(selectedDate: Date) {
        if (activePicker === "startDate") {
            setStartDateTime((current) => updateDatePart(current, selectedDate));
            return;
        }

        if (activePicker === "startTime") {
            setStartDateTime((current) => updateTimePart(current, selectedDate));
            return;
        }

        if (activePicker === "endDate") {
            setEndDateTime((current) => updateDatePart(current, selectedDate));
            return;
        }

        if (activePicker === "endTime") {
            setEndDateTime((current) => updateTimePart(current, selectedDate));
        }
    }

    type DateTimePickerValueChange = NonNullable<
        ComponentProps<typeof DateTimePicker>["onValueChange"]
    >;

    const handlePickerValueChange: DateTimePickerValueChange = (_event, selectedDate) => {
        if (!activePicker) return;

        applySelectedDateTime(selectedDate);

        if (Platform.OS === "android") {
            setActivePicker(null);
        }
    };

    function handlePickerDismiss() {
        setActivePicker(null);
    }

    async function submitTrip(allowOverlap = false) {
        await createTrip({
            tripName: tripName.trim(),
            destination: destination.trim(),
            startDate: formatForBackend(startDateTime),
            endDate: formatForBackend(endDateTime),
            allowOverlap,
        });
    }

    async function handleCreateTrip() {
        setError(null);

        if (!tripName.trim() || !destination.trim()) {
            Alert.alert("Missing details", "Please enter trip name and destination.");
            return;
        }

        if (endDateTime <= startDateTime) {
            Alert.alert("Invalid dates", "End date/time must be after start date/time.");
            return;
        }

        try {
            setIsSubmitting(true);

            await submitTrip(false);

            Alert.alert("Trip created", "Your trip has been created successfully.");
            router.back();
        } catch (error: any) {
            logger.debug("Create trip failed:", error.response?.data || error.message);

            if (hasApiWarning(error, "TRIP_OVERLAP_WARNING")) {
                Alert.alert(
                    "Trip dates overlap",
                    "This trip overlaps with another existing trip. Do you still want to continue?",
                    [
                        {
                            text: "Cancel",
                            style: "cancel",
                        },
                        {
                            text: "Continue anyway",
                            style: "destructive",
                            onPress: async () => {
                                try {
                                    setIsSubmitting(true);
                                    setError(null);

                                    await submitTrip(true);

                                    Alert.alert(
                                        "Trip created",
                                        "Your trip has been created successfully."
                                    );

                                    router.back();
                                } catch (confirmError: any) {
                                    logger.debug(
                                        "Create trip after confirmation failed:",
                                        confirmError.response?.data || confirmError.message
                                    );

                                    setError(
                                        getApiErrorMessage(
                                            confirmError,
                                            "Please check your input and try again."
                                        )
                                    );

                                    Alert.alert(
                                        getApiErrorTitle(confirmError, "Create trip failed"),
                                        getApiErrorMessage(
                                            confirmError,
                                            "Please check your input and try again."
                                        )
                                    );
                                } finally {
                                    setIsSubmitting(false);
                                }
                            },
                        },
                    ]
                );
                return;
            }

            setError(getApiErrorMessage(error, "Please check your input and try again."));

            Alert.alert(
                getApiErrorTitle(error, "Create trip failed"),
                getApiErrorMessage(error, "Please check your input and try again.")
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    const pickerValue =
        activePicker === "startDate" || activePicker === "startTime"
            ? startDateTime
            : endDateTime;

    const pickerMode =
        activePicker === "startTime" || activePicker === "endTime" ? "time" : "date";

    return (
        <AppScreen keyboardAvoiding contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <Pressable onPress={() => router.back()} style={styles.backButton}>
                    <Ionicons name="chevron-back" size={22} color={colors.primary} />
                </Pressable>

                <View style={styles.headerTextGroup}>
                    <Text style={styles.eyebrow}>New trip</Text>
                    <Text style={styles.title}>Create Trip</Text>
                    <Text style={styles.subtitle}>Add your destination and travel dates.</Text>
                </View>
            </View>

            <AppCard contentStyle={styles.cardContent}>
                <AppInput
                    label="Trip name"
                    required
                    value={tripName}
                    onChangeText={setTripName}
                    placeholder="e.g. Sydney Weekend"
                    leftIcon={<Ionicons name="airplane-outline" size={20} color={colors.textMuted} />}
                />

                <AppInput
                    label="Destination"
                    required
                    value={destination}
                    onChangeText={setDestination}
                    placeholder="e.g. Sydney"
                    leftIcon={<Ionicons name="location-outline" size={20} color={colors.textMuted} />}
                />

                <View style={styles.divider} />

                <DateTimeSection
                    title="Start"
                    dateValue={formatDisplayDate(startDateTime)}
                    timeValue={formatDisplayTime(startDateTime)}
                    onDatePress={() => setActivePicker("startDate")}
                    onTimePress={() => setActivePicker("startTime")}
                />

                <DateTimeSection
                    title="End"
                    dateValue={formatDisplayDate(endDateTime)}
                    timeValue={formatDisplayTime(endDateTime)}
                    onDatePress={() => setActivePicker("endDate")}
                    onTimePress={() => setActivePicker("endTime")}
                />

                <ErrorMessage message={error} title="Create trip failed" />

                <AppButton
                    title="Create trip"
                    onPress={handleCreateTrip}
                    loading={isSubmitting}
                    rightIcon={<Ionicons name="checkmark-circle" size={20} color={colors.textLight} />}
                />
            </AppCard>

            {activePicker ? (
                <AppCard variant="outline" contentStyle={styles.pickerCardContent}>
                    <View style={styles.pickerHeader}>
                        <Text style={styles.pickerTitle}>{getPickerTitle(activePicker)}</Text>

                        {Platform.OS === "ios" ? (
                            <Pressable onPress={handlePickerDismiss} hitSlop={10}>
                                <Text style={styles.doneText}>Done</Text>
                            </Pressable>
                        ) : null}
                    </View>

                    <DateTimePicker
                        value={pickerValue}
                        mode={pickerMode}
                        display={Platform.OS === "ios" ? "spinner" : "default"}
                        onValueChange={handlePickerValueChange}
                        onDismiss={handlePickerDismiss}
                    />
                </AppCard>
            ) : null}
        </AppScreen>
    );
}

type DateTimeSectionProps = Readonly<{
    title: string;
    dateValue: string;
    timeValue: string;
    onDatePress: () => void;
    onTimePress: () => void;
}>;

function DateTimeSection({
                             title,
                             dateValue,
                             timeValue,
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
                    value={dateValue}
                    onPress={onDatePress}
                />
                <PickerButton
                    icon="time-outline"
                    label="Time"
                    value={timeValue}
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
                <Text style={styles.pickerValue} numberOfLines={1}>{value}</Text>
            </View>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    screenContent: {
        paddingTop: spacing.lg,
        paddingBottom: spacing.xxl,
        gap: spacing.lg,
    },
    header: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    backButton: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        backgroundColor: colors.surface,
        alignItems: "center",
        justifyContent: "center",
    },
    headerTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    eyebrow: {
        color: colors.primary,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.6,
    },
    title: {
        color: colors.text,
        fontSize: typography.heading,
        fontWeight: fontWeight.bold,
    },
    subtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    cardContent: {
        gap: spacing.lg,
    },
    divider: {
        height: 1,
        backgroundColor: colors.border,
    },
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