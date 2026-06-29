import { useCallback, useState } from "react";
import {
    Alert,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";

import { getTripById, updateTrip } from "@/src/api/tripApi";
import { DateTimePickerCard } from "@/src/components/forms/DateTimePickerCard";
import { DateTimeSection } from "@/src/components/forms/DateTimeSection";
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
    hasApiWarning,
} from "@/src/utils/apiWarningUtils";
import {
    formatForBackend,
    type PickerTarget,
    updateDatePart,
    updateTimePart,
} from "@/src/utils/dateTimePickerUtils";

function parseDateOrFallback(value: string | undefined, fallback: Date) {
    if (!value) return fallback;

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return fallback;
    }

    return date;
}

export default function EditTripScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripNumberId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripNumberId);

    const [tripName, setTripName] = useState("");
    const [destination, setDestination] = useState("");
    const [startDateTime, setStartDateTime] = useState(new Date());
    const [endDateTime, setEndDateTime] = useState(new Date());
    const [activePicker, setActivePicker] = useState<PickerTarget>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function loadTripForEdit() {
        if (!hasValidTripId) {
            setError("Trip ID is missing or invalid.");
            setIsLoading(false);
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const data = await getTripById(tripNumberId);
            const fallbackStart = new Date();
            const fallbackEnd = new Date(fallbackStart);
            fallbackEnd.setDate(fallbackEnd.getDate() + 1);

            setTripName(data.tripName ?? "");
            setDestination(data.destination ?? "");
            setStartDateTime(parseDateOrFallback(data.startDate, fallbackStart));
            setEndDateTime(parseDateOrFallback(data.endDate, fallbackEnd));
        } catch (error: any) {

            setError(getApiErrorMessage(error, "Failed to load trip. Please try again."));
        } finally {
            setIsLoading(false);
        }
    }

    useFocusEffect(
        useCallback(() => {
            void loadTripForEdit();
        }, [tripIdParam])
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
    function handlePickerValueChange(selectedDate: Date) {
        if (!activePicker) return;

        applySelectedDateTime(selectedDate);
    }

    function handlePickerDismiss() {
        setActivePicker(null);
    }

    async function submitTripUpdate(allowOverlap = false) {
        await updateTrip(tripNumberId, {
            tripName: tripName.trim(),
            destination: destination.trim(),
            startDate: formatForBackend(startDateTime),
            endDate: formatForBackend(endDateTime),
            allowOverlap,
        });
    }

    async function handleUpdateTrip() {
        setError(null);

        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
            return;
        }

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

            await submitTripUpdate(false);

            Alert.alert("Trip updated", "Your trip has been updated.");
            router.back();
        } catch (error: any) {

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

                                    await submitTripUpdate(true);

                                    Alert.alert("Trip updated", "Your trip has been updated.");
                                    router.back();
                                } catch (confirmError: any) {

                                    setError(
                                        getApiErrorMessage(
                                            confirmError,
                                            "Please check your input and try again."
                                        )
                                    );

                                    Alert.alert(
                                        getApiErrorTitle(confirmError, "Update trip failed"),
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
                getApiErrorTitle(error, "Update trip failed"),
                getApiErrorMessage(error, "Please check your input and try again.")
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    if (isLoading) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Loading trip..."
                    subtitle="Getting this trip ready for editing."
                    fullScreen
                />
            </AppScreen>
        );
    }

    if (error && !tripName && !destination) {
        return (
            <AppScreen scroll={false} centerContent contentContainerStyle={styles.centerContent}>
                <View style={styles.errorIconBadge}>
                    <Ionicons name="alert-circle-outline" size={34} color={colors.danger} />
                </View>

                <View style={styles.centerTextGroup}>
                    <Text style={styles.centerTitle}>Unable to load trip</Text>
                    <Text style={styles.centerSubtitle}>{error}</Text>
                </View>

                <AppButton title="Try again" onPress={loadTripForEdit} />
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
                    <Text style={styles.eyebrow}>Edit trip</Text>
                    <Text style={styles.title}>Update Trip</Text>
                    <Text style={styles.subtitle}>Edit trip details and travel dates.</Text>
                </View>
            </View>

            <AppCard contentStyle={styles.cardContent}>
                <AppInput
                    label="Trip name"
                    required
                    value={tripName}
                    onChangeText={setTripName}
                    placeholder="e.g. Japan Holiday"
                    leftIcon={<Ionicons name="airplane-outline" size={20} color={colors.textMuted} />}
                />

                <AppInput
                    label="Main destination"
                    required
                    value={destination}
                    onChangeText={setDestination}
                    placeholder="e.g. Japan"
                    leftIcon={<Ionicons name="location-outline" size={20} color={colors.textMuted} />}
                />

                <View style={styles.divider} />

                <DateTimeSection
                    title="Start"
                    dateTime={startDateTime}
                    onDatePress={() => setActivePicker("startDate")}
                    onTimePress={() => setActivePicker("startTime")}
                />

                <DateTimeSection
                    title="End"
                    dateTime={endDateTime}
                    onDatePress={() => setActivePicker("endDate")}
                    onTimePress={() => setActivePicker("endTime")}
                />

                <ErrorMessage message={error} title="Update trip failed" />

                <AppButton
                    title="Save Trip"
                    onPress={handleUpdateTrip}
                    loading={isSubmitting}
                    rightIcon={<Ionicons name="checkmark-circle" size={20} color={colors.textLight} />}
                />
            </AppCard>

            {activePicker ? (
                <DateTimePickerCard
                    activePicker={activePicker}
                    startDateTime={startDateTime}
                    endDateTime={endDateTime}
                    onChangeDate={handlePickerValueChange}
                    onClose={handlePickerDismiss}
                />
            ) : null}
        </AppScreen>
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
    divider: {
        height: 1,
        backgroundColor: colors.border,
    },
});