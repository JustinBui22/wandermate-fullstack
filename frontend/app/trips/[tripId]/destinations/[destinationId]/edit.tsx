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

import {
    getDestinationById,
    updateDestination,
} from "@/src/api/destinationApi";
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

function parseDestinationOrder(value: string) {
    const trimmedValue = value.trim();

    if (!trimmedValue) {
        return null;
    }

    const parsedValue = Number(trimmedValue);

    if (Number.isNaN(parsedValue)) {
        return null;
    }

    return parsedValue;
}

export default function EditDestinationScreen() {
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

    const [destinationName, setDestinationName] = useState("");
    const [destinationOrder, setDestinationOrder] = useState("");
    const [notes, setNotes] = useState("");
    const [startDateTime, setStartDateTime] = useState(new Date());
    const [endDateTime, setEndDateTime] = useState(new Date());
    const [activePicker, setActivePicker] = useState<PickerTarget>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function loadDestinationForEdit() {
        if (!hasValidRouteIds) {
            setError("Trip ID or destination ID is missing or invalid.");
            setIsLoading(false);
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const data = await getDestinationById(tripNumberId, destinationNumberId);
            const fallbackStart = new Date();
            const fallbackEnd = new Date(fallbackStart);
            fallbackEnd.setDate(fallbackEnd.getDate() + 1);

            setDestinationName(data.destinationName ?? "");
            setDestinationOrder(
                data.destinationOrder !== null && data.destinationOrder !== undefined
                    ? String(data.destinationOrder)
                    : ""
            );
            setNotes(data.notes ?? "");
            setStartDateTime(parseDateOrFallback(data.startDate, fallbackStart));
            setEndDateTime(parseDateOrFallback(data.endDate, fallbackEnd));
        } catch (error: any) {

            setError(getApiErrorMessage(error, "Failed to load destination. Please try again."));
        } finally {
            setIsLoading(false);
        }
    }

    useFocusEffect(
        useCallback(() => {
            loadDestinationForEdit();
        }, [tripIdParam, destinationIdParam])
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

    async function submitDestinationUpdate(allowOverlap = false) {
        await updateDestination(tripNumberId, destinationNumberId, {
            destinationName: destinationName.trim(),
            startDate: formatForBackend(startDateTime),
            endDate: formatForBackend(endDateTime),
            destinationOrder: parseDestinationOrder(destinationOrder),
            notes: notes.trim() || null,
            allowOverlap,
        });
    }

    async function handleUpdateDestination() {
        setError(null);

        if (!hasValidRouteIds) {
            Alert.alert("Missing destination", "Trip ID or destination ID is missing or invalid.");
            return;
        }

        if (!destinationName.trim()) {
            Alert.alert("Missing destination", "Please enter destination name.");
            return;
        }

        if (destinationOrder.trim() && Number.isNaN(Number(destinationOrder.trim()))) {
            Alert.alert("Invalid order", "Destination order must be a number.");
            return;
        }

        if (endDateTime <= startDateTime) {
            Alert.alert("Invalid dates", "End date/time must be after start date/time.");
            return;
        }

        try {
            setIsSubmitting(true);

            await submitDestinationUpdate(false);

            Alert.alert("Destination updated", "Destination has been updated.");
            router.back();
        } catch (error: any) {

            if (hasApiWarning(error, "DESTINATION_OVERLAP_WARNING")) {
                Alert.alert(
                    "Destination dates overlap",
                    "This destination overlaps with another destination in this trip. Do you still want to continue?",
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

                                    await submitDestinationUpdate(true);

                                    Alert.alert("Destination updated", "Destination has been updated.");
                                    router.back();
                                } catch (confirmError: any) {

                                    setError(
                                        getApiErrorMessage(
                                            confirmError,
                                            "Please check your input and try again."
                                        )
                                    );

                                    Alert.alert(
                                        getApiErrorTitle(confirmError, "Update destination failed"),
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
                getApiErrorTitle(error, "Update destination failed"),
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
                    title="Loading destination..."
                    subtitle="Getting this destination ready for editing."
                    fullScreen
                />
            </AppScreen>
        );
    }

    if (error && !destinationName) {
        return (
            <AppScreen scroll={false} centerContent contentContainerStyle={styles.centerContent}>
                <View style={styles.errorIconBadge}>
                    <Ionicons name="alert-circle-outline" size={34} color={colors.danger} />
                </View>

                <View style={styles.centerTextGroup}>
                    <Text style={styles.centerTitle}>Unable to load destination</Text>
                    <Text style={styles.centerSubtitle}>{error}</Text>
                </View>

                <AppButton title="Try again" onPress={loadDestinationForEdit} />
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
                    <Text style={styles.eyebrow}>Edit destination</Text>
                    <Text style={styles.title}>Update Destination</Text>
                    <Text style={styles.subtitle}>Edit destination details, notes, order, and dates.</Text>
                </View>
            </View>

            <AppCard contentStyle={styles.cardContent}>
                <AppInput
                    label="Destination name"
                    required
                    value={destinationName}
                    onChangeText={setDestinationName}
                    placeholder="e.g. Tokyo"
                    leftIcon={<Ionicons name="location-outline" size={20} color={colors.textMuted} />}
                />

                <AppInput
                    label="Order"
                    value={destinationOrder}
                    onChangeText={setDestinationOrder}
                    placeholder="e.g. 1"
                    keyboardType="number-pad"
                    helperText="Optional. Use this to arrange destinations in order."
                    leftIcon={<Ionicons name="list-outline" size={20} color={colors.textMuted} />}
                />

                <AppInput
                    label="Notes"
                    value={notes}
                    onChangeText={setNotes}
                    placeholder="Optional notes"
                    multiline
                    inputStyle={styles.notesInput}
                    leftIcon={<Ionicons name="document-text-outline" size={20} color={colors.textMuted} />}
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

                <ErrorMessage message={error} title="Update destination failed" />

                <AppButton
                    title="Save Destination"
                    onPress={handleUpdateDestination}
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
    notesInput: {
        minHeight: 92,
    },
    divider: {
        height: 1,
        backgroundColor: colors.border,
    },
});