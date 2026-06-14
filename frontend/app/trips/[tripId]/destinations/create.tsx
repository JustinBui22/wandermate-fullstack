import { useState } from "react";
import {
    Alert,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useLocalSearchParams, useRouter } from "expo-router";

import { createDestination } from "@/src/api/destinationApi";
import { DateTimePickerCard } from "@/src/components/forms/DateTimePickerCard";
import { DateTimeSection } from "@/src/components/forms/DateTimeSection";
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
    formatForBackend,
    type PickerTarget,
    updateDatePart,
    updateTimePart,
} from "@/src/utils/dateTimePickerUtils";

function getDefaultStartDateTime() {
    const date = new Date();
    date.setHours(9, 0, 0, 0);
    return date;
}

function getDefaultEndDateTime() {
    const date = new Date();
    date.setDate(date.getDate() + 1);
    date.setHours(18, 0, 0, 0);
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

export default function CreateDestinationScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripNumberId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripNumberId);

    const [destinationName, setDestinationName] = useState("");
    const [destinationOrder, setDestinationOrder] = useState("");
    const [notes, setNotes] = useState("");
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
    function handlePickerValueChange(selectedDate: Date) {
        if (!activePicker) return;

        applySelectedDateTime(selectedDate);
    }

    function handlePickerDismiss() {
        setActivePicker(null);
    }

    async function submitDestination(allowOverlap = false) {
        await createDestination(tripNumberId, {
            destinationName: destinationName.trim(),
            startDate: formatForBackend(startDateTime),
            endDate: formatForBackend(endDateTime),
            destinationOrder: parseDestinationOrder(destinationOrder),
            notes: notes.trim() || null,
            allowOverlap,
        });
    }

    async function handleCreateDestination() {
        setError(null);

        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
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

            await submitDestination(false);

            Alert.alert("Destination created", "Destination has been added.");
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

                                    await submitDestination(true);

                                    Alert.alert(
                                        "Destination created",
                                        "Destination has been added."
                                    );

                                    router.back();
                                } catch (confirmError: any) {

                                    setError(
                                        getApiErrorMessage(
                                            confirmError,
                                            "Please check your input and try again."
                                        )
                                    );

                                    Alert.alert(
                                        getApiErrorTitle(confirmError, "Create destination failed"),
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
                getApiErrorTitle(error, "Create destination failed"),
                getApiErrorMessage(error, "Please check your input and try again.")
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <AppScreen keyboardAvoiding contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <Pressable onPress={() => router.back()} style={styles.backButton}>
                    <Ionicons name="chevron-back" size={22} color={colors.primary} />
                </Pressable>

                <View style={styles.headerTextGroup}>
                    <Text style={styles.eyebrow}>New destination</Text>
                    <Text style={styles.title}>Add Destination</Text>
                    <Text style={styles.subtitle}>Add a city or place inside this trip.</Text>
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

                <ErrorMessage message={error} title="Create destination failed" />

                <AppButton
                    title="Create Destination"
                    onPress={handleCreateDestination}
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