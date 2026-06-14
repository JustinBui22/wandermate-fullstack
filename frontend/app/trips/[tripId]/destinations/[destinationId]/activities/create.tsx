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
import { useLocalSearchParams, useRouter } from "expo-router";

import { createActivity } from "@/src/api/activityApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import {
    getApiErrorMessage,
    getApiErrorTitle,
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

type DateTimePickerValueChange = NonNullable<
    ComponentProps<typeof DateTimePicker>["onValueChange"]
>;

function getDefaultStartDateTime() {
    const date = new Date();
    date.setHours(9, 0, 0, 0);
    return date;
}

function getDefaultEndDateTime() {
    const date = new Date();
    date.setHours(10, 0, 0, 0);
    return date;
}

function getPickerTitle(activePicker: PickerTarget) {
    if (activePicker === "startDate") return "Choose start date";
    if (activePicker === "startTime") return "Choose start time";
    if (activePicker === "endDate") return "Choose end date";
    if (activePicker === "endTime") return "Choose end time";
    return "Choose date/time";
}

export default function CreateActivityScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const destinationIdParam = Array.isArray(params.destinationId)
        ? params.destinationId[0]
        : params.destinationId;

    const tripNumberId = Number(tripIdParam);
    const destinationNumberId = Number(destinationIdParam);
    const hasValidRouteIds = Boolean(tripIdParam)
        && Boolean(destinationIdParam)
        && !Number.isNaN(tripNumberId)
        && !Number.isNaN(destinationNumberId);

    const [activityName, setActivityName] = useState("");
    const [location, setLocation] = useState("");
    const [description, setDescription] = useState("");
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

    async function handleCreateActivity() {
        setError(null);

        if (!hasValidRouteIds) {
            Alert.alert("Missing destination", "Trip ID or destination ID is missing.");
            return;
        }

        if (!activityName.trim()) {
            Alert.alert("Missing activity", "Please enter activity name.");
            return;
        }

        if (endDateTime <= startDateTime) {
            Alert.alert("Invalid time", "End date/time must be after start date/time.");
            return;
        }

        try {
            setIsSubmitting(true);

            await createActivity(tripNumberId, destinationNumberId, {
                activityName: activityName.trim(),
                location: location.trim() || null,
                description: description.trim() || null,
                startDateTime: formatForBackend(startDateTime),
                endDateTime: formatForBackend(endDateTime),
            });

            Alert.alert("Activity created", "Activity has been added.");
            router.back();
        } catch (error: any) {
            logger.debug("Create activity failed:", error.response?.data || error.message);

            setError(
                getApiErrorMessage(
                    error,
                    "Please check the activity time and try again."
                )
            );

            Alert.alert(
                getApiErrorTitle(error, "Create activity failed"),
                getApiErrorMessage(
                    error,
                    "Please check the activity time and try again."
                )
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
                    <Text style={styles.eyebrow}>New activity</Text>
                    <Text style={styles.title}>Add Activity</Text>
                    <Text style={styles.subtitle}>Add a plan inside this destination.</Text>
                </View>
            </View>

            <AppCard contentStyle={styles.cardContent}>
                <AppInput
                    label="Activity name"
                    required
                    value={activityName}
                    onChangeText={setActivityName}
                    placeholder="e.g. Visit Tokyo Tower"
                    leftIcon={<Ionicons name="walk-outline" size={20} color={colors.textMuted} />}
                />

                <AppInput
                    label="Location"
                    value={location}
                    onChangeText={setLocation}
                    placeholder="e.g. Tokyo Tower"
                    leftIcon={<Ionicons name="location-outline" size={20} color={colors.textMuted} />}
                />

                <AppInput
                    label="Description"
                    value={description}
                    onChangeText={setDescription}
                    placeholder="Optional details"
                    multiline
                    inputStyle={styles.descriptionInput}
                    leftIcon={<Ionicons name="document-text-outline" size={20} color={colors.textMuted} />}
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

                <ErrorMessage message={error} title="Create activity failed" />

                <AppButton
                    title="Create Activity"
                    onPress={handleCreateActivity}
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
                <Text style={styles.pickerValue} numberOfLines={1}>
                    {value}
                </Text>
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
        borderWidth: 1,
        borderColor: colors.border,
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
    descriptionInput: {
        minHeight: 92,
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