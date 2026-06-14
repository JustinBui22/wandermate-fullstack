import { useCallback, useState, type ComponentProps } from "react";
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
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";

import {
    getActivityById,
    updateActivity,
} from "@/src/api/activityApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
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

function getPickerTitle(activePicker: PickerTarget) {
    if (activePicker === "startDate") return "Choose start date";
    if (activePicker === "startTime") return "Choose start time";
    if (activePicker === "endDate") return "Choose end date";
    if (activePicker === "endTime") return "Choose end time";
    return "Choose date/time";
}

function parseDateOrFallback(value: string | undefined, fallback: Date) {
    if (!value) return fallback;

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return fallback;
    }

    return date;
}

export default function EditActivityScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const destinationIdParam = Array.isArray(params.destinationId)
        ? params.destinationId[0]
        : params.destinationId;
    const activityIdParam = Array.isArray(params.activityId)
        ? params.activityId[0]
        : params.activityId;

    const tripNumberId = Number(tripIdParam);
    const destinationNumberId = Number(destinationIdParam);
    const activityNumberId = Number(activityIdParam);
    const hasValidRouteIds = Boolean(tripIdParam)
        && Boolean(destinationIdParam)
        && Boolean(activityIdParam)
        && !Number.isNaN(tripNumberId)
        && !Number.isNaN(destinationNumberId)
        && !Number.isNaN(activityNumberId);

    const [activityName, setActivityName] = useState("");
    const [location, setLocation] = useState("");
    const [description, setDescription] = useState("");
    const [startDateTime, setStartDateTime] = useState(new Date());
    const [endDateTime, setEndDateTime] = useState(new Date());
    const [activePicker, setActivePicker] = useState<PickerTarget>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function loadActivityForEdit() {
        if (!hasValidRouteIds) {
            setError("Trip ID, destination ID, or activity ID is missing or invalid.");
            setIsLoading(false);
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const data = await getActivityById(
                tripNumberId,
                destinationNumberId,
                activityNumberId
            );

            const fallbackStart = new Date();
            const fallbackEnd = new Date(fallbackStart);
            fallbackEnd.setHours(fallbackEnd.getHours() + 1);

            setActivityName(data.activityName ?? "");
            setLocation(data.location ?? "");
            setDescription(data.description ?? "");
            setStartDateTime(parseDateOrFallback(data.startDateTime, fallbackStart));
            setEndDateTime(parseDateOrFallback(data.endDateTime, fallbackEnd));
        } catch (error: any) {
            logger.debug("Load activity for edit failed:", error.response?.data || error.message);

            setError(getApiErrorMessage(error, "Failed to load activity. Please try again."));
        } finally {
            setIsLoading(false);
        }
    }

    useFocusEffect(
        useCallback(() => {
            loadActivityForEdit();
        }, [tripIdParam, destinationIdParam, activityIdParam])
    );

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

    async function handleUpdateActivity() {
        setError(null);

        if (!hasValidRouteIds) {
            Alert.alert("Missing activity", "Trip ID, destination ID, or activity ID is missing or invalid.");
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

            await updateActivity(
                tripNumberId,
                destinationNumberId,
                activityNumberId,
                {
                    activityName: activityName.trim(),
                    location: location.trim() || null,
                    description: description.trim() || null,
                    startDateTime: formatForBackend(startDateTime),
                    endDateTime: formatForBackend(endDateTime),
                }
            );

            Alert.alert("Activity updated", "Activity has been updated.");
            router.back();
        } catch (error: any) {
            logger.debug("Update activity failed:", error.response?.data || error.message);

            setError(
                getApiErrorMessage(
                    error,
                    "Please check the activity time and try again."
                )
            );

            Alert.alert(
                getApiErrorTitle(error, "Update activity failed"),
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

    if (isLoading) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Loading activity..."
                    subtitle="Getting this activity ready for editing."
                    fullScreen
                />
            </AppScreen>
        );
    }

    if (error && !activityName) {
        return (
            <AppScreen scroll={false} centerContent contentContainerStyle={styles.centerContent}>
                <View style={styles.errorIconBadge}>
                    <Ionicons name="alert-circle-outline" size={34} color={colors.danger} />
                </View>

                <View style={styles.centerTextGroup}>
                    <Text style={styles.centerTitle}>Unable to load activity</Text>
                    <Text style={styles.centerSubtitle}>{error}</Text>
                </View>

                <AppButton title="Try again" onPress={loadActivityForEdit} />
                <AppButton title="Go back" onPress={() => router.back()} variant="ghost" />
            </AppScreen>
        );
    }

    return (
        <AppScreen keyboardAvoiding contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <Pressable onPress={() => router.back()} style={styles.backButton}>
                    <Ionicons name="chevron-back" size={22} color={colors.primary} />
                </Pressable>

                <View style={styles.headerTextGroup}>
                    <Text style={styles.eyebrow}>Edit activity</Text>
                    <Text style={styles.title}>Update Activity</Text>
                    <Text style={styles.subtitle}>Edit activity details, location, and time.</Text>
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

                <ErrorMessage message={error} title="Update activity failed" />

                <AppButton
                    title="Save Activity"
                    onPress={handleUpdateActivity}
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
    centerContent: {
        gap: spacing.lg,
    },
    centerTextGroup: {
        alignItems: "center",
        gap: spacing.sm,
    },
    centerTitle: {
        color: colors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
        textAlign: "center",
    },
    centerSubtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 21,
        textAlign: "center",
    },
    errorIconBadge: {
        width: 72,
        height: 72,
        borderRadius: radius.xl,
        backgroundColor: colors.dangerSoft,
        alignItems: "center",
        justifyContent: "center",
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