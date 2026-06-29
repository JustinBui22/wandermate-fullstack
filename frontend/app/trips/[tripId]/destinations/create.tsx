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
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
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

    const theme = useAppTheme();
    const colors = theme.colors;

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
        if (!activePicker) {
            return;
        }

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

    async function handleConfirmCreateWithOverlap() {
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
            const message = getApiErrorMessage(
                confirmError,
                "Please check your input and try again."
            );

            setError(message);

            Alert.alert(
                getApiErrorTitle(confirmError, "Create destination failed"),
                message
            );
        } finally {
            setIsSubmitting(false);
        }
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
                            onPress: () => {
                                void handleConfirmCreateWithOverlap();
                            },
                        },
                    ]
                );
                return;
            }

            const message = getApiErrorMessage(
                error,
                "Please check your input and try again."
            );

            setError(message);

            Alert.alert(
                getApiErrorTitle(error, "Create destination failed"),
                message
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    function handleCreateDestinationPress() {
        void handleCreateDestination();
    }

    return (
        <AppScreen keyboardAvoiding contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <Pressable
                    onPress={() => router.back()}
                    style={[
                        styles.backButton,
                        {
                            backgroundColor: colors.surface,
                            borderColor: colors.border,
                        },
                    ]}
                >
                    <Ionicons name="chevron-back" size={22} color={colors.primary} />
                </Pressable>

                <View style={styles.headerTextGroup}>
                    <Text style={[styles.eyebrow, { color: colors.primary }]}>New destination</Text>
                    <Text style={[styles.title, { color: colors.text }]}>Add Destination</Text>
                    <Text style={[styles.subtitle, { color: colors.textMuted }]}>Add a city or place inside this trip.</Text>
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

                <View style={[styles.divider, { backgroundColor: colors.border }]} />

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
                    onPress={handleCreateDestinationPress}
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
        borderWidth: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    headerTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    eyebrow: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.6,
    },
    title: {
        fontSize: typography.heading,
        fontWeight: fontWeight.bold,
    },
    subtitle: {
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
    },
});
