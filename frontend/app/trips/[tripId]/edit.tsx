import { useCallback, useState, type ComponentProps } from "react";
import {
    ActivityIndicator,
    Alert,
    KeyboardAvoidingView,
    Platform,
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    TextInput,
    View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import DateTimePicker from "@react-native-community/datetimepicker";

import { getTripById, updateTrip } from "@/src/api/tripApi";
import { colors, radius, shadow, spacing } from "@/src/theme/theme";
import {
    formatDisplayDate,
    formatDisplayTime,
    formatForBackend,
    type PickerTarget,
    updateDatePart,
    updateTimePart,
} from "@/src/utils/dateTimePickerUtils";
import {
    getApiErrorMessage, getApiErrorTitle,
    hasApiWarning,
} from "@/src/utils/apiWarningUtils";
import { logger } from "@/src/utils/logger";

type DateTimePickerValueChange = NonNullable<
    ComponentProps<typeof DateTimePicker>["onValueChange"]
>;

export default function EditTripScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId)
        ? params.tripId[0]
        : params.tripId;

    const [tripName, setTripName] = useState("");
    const [destination, setDestination] = useState("");
    const [startDateTime, setStartDateTime] = useState(new Date());
    const [endDateTime, setEndDateTime] = useState(new Date());

    const [activePicker, setActivePicker] = useState<PickerTarget>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function loadTripForEdit() {
        const tripNumberId = Number(tripIdParam);

        if (!tripIdParam || Number.isNaN(tripNumberId)) {
            setError("Trip ID is missing.");
            setIsLoading(false);
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const data = await getTripById(tripNumberId);

            setTripName(data.tripName ?? "");
            setDestination(data.destination ?? "");
            setStartDateTime(new Date(data.startDate));
            setEndDateTime(new Date(data.endDate));
        } catch (error: any) {
            logger.debug(
                "Load trip for edit failed:",
                error.response?.data || error.message
            );

            setError(
                getApiErrorMessage(
                    error,
                    "Failed to load trip. Please try again."
                )
            );
        } finally {
            setIsLoading(false);
        }
    }

    useFocusEffect(
        useCallback(() => {
            loadTripForEdit();
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

    const handlePickerValueChange: DateTimePickerValueChange = (
        _event,
        selectedDate
    ) => {
        if (!activePicker) {
            return;
        }

        applySelectedDateTime(selectedDate);

        if (Platform.OS === "android") {
            setActivePicker(null);
        }
    };

    function handlePickerDismiss() {
        setActivePicker(null);
    }

    async function submitTripUpdate(allowOverlap = false) {
        const tripNumberId = Number(tripIdParam);

        await updateTrip(tripNumberId, {
            tripName: tripName.trim(),
            destination: destination.trim(),
            startDate: formatForBackend(startDateTime),
            endDate: formatForBackend(endDateTime),
            allowOverlap,
        });
    }

    async function handleUpdateTrip() {
        const tripNumberId = Number(tripIdParam);

        if (!tripIdParam || Number.isNaN(tripNumberId)) {
            Alert.alert("Missing trip", "Trip ID is missing.");
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
            logger.debug(
                "Update trip failed:",
                error.response?.data || error.message
            );

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

                                    await submitTripUpdate(true);

                                    Alert.alert(
                                        "Trip updated",
                                        "Your trip has been updated."
                                    );

                                    router.back();
                                } catch (confirmError: any) {
                                    logger.debug(
                                        "Update trip after confirmation failed:",
                                        confirmError.response?.data ||
                                        confirmError.message
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

            Alert.alert(
                getApiErrorTitle(error, "Update trip failed"),
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
        activePicker === "startTime" || activePicker === "endTime"
            ? "time"
            : "date";

    if (isLoading) {
        return (
            <SafeAreaView style={styles.centerContainer}>
                <ActivityIndicator color={colors.primary} />
                <Text style={styles.loadingText}>Loading trip...</Text>
            </SafeAreaView>
        );
    }

    if (error) {
        return (
            <SafeAreaView style={styles.centerContainer}>
                <Text style={styles.errorTitle}>Unable to load trip</Text>
                <Text style={styles.errorText}>{error}</Text>

                <Pressable onPress={loadTripForEdit} style={styles.retryButton}>
                    <Text style={styles.retryButtonText}>Try again</Text>
                </Pressable>

                <Pressable onPress={() => router.back()} style={styles.backTextButton}>
                    <Text style={styles.backText}>Go back</Text>
                </Pressable>
            </SafeAreaView>
        );
    }

    return (
        <SafeAreaView style={styles.safeArea}>
            <KeyboardAvoidingView
                style={styles.keyboardView}
                behavior={Platform.OS === "ios" ? "padding" : undefined}
            >
                <ScrollView
                    contentContainerStyle={styles.container}
                    keyboardShouldPersistTaps="handled"
                    showsVerticalScrollIndicator={false}
                >
                    <View style={styles.header}>
                        <Pressable onPress={() => router.back()} style={styles.backButton}>
                            <Ionicons name="chevron-back" size={24} color={colors.text} />
                        </Pressable>

                        <View style={{ flex: 1 }}>
                            <Text style={styles.title}>Edit Trip</Text>
                            <Text style={styles.subtitle}>
                                Update trip details and travel dates
                            </Text>
                        </View>
                    </View>

                    <View style={styles.card}>
                        <View style={styles.inputGroup}>
                            <Text style={styles.label}>Trip name</Text>
                            <TextInput
                                value={tripName}
                                onChangeText={setTripName}
                                placeholder="e.g. Japan Holiday"
                                placeholderTextColor="#9CA3AF"
                                style={styles.input}
                            />
                        </View>

                        <View style={styles.inputGroup}>
                            <Text style={styles.label}>Main destination</Text>
                            <TextInput
                                value={destination}
                                onChangeText={setDestination}
                                placeholder="e.g. Japan"
                                placeholderTextColor="#9CA3AF"
                                style={styles.input}
                            />
                        </View>

                        <View style={styles.divider} />

                        <Text style={styles.sectionTitle}>Start</Text>

                        <View style={styles.pickerRow}>
                            <Pressable
                                style={styles.pickerButton}
                                onPress={() => setActivePicker("startDate")}
                            >
                                <Ionicons
                                    name="calendar-outline"
                                    size={20}
                                    color={colors.primary}
                                />
                                <View>
                                    <Text style={styles.pickerLabel}>Date</Text>
                                    <Text style={styles.pickerValue}>
                                        {formatDisplayDate(startDateTime)}
                                    </Text>
                                </View>
                            </Pressable>

                            <Pressable
                                style={styles.pickerButton}
                                onPress={() => setActivePicker("startTime")}
                            >
                                <Ionicons
                                    name="time-outline"
                                    size={20}
                                    color={colors.primary}
                                />
                                <View>
                                    <Text style={styles.pickerLabel}>Time</Text>
                                    <Text style={styles.pickerValue}>
                                        {formatDisplayTime(startDateTime)}
                                    </Text>
                                </View>
                            </Pressable>
                        </View>

                        <Text style={styles.sectionTitle}>End</Text>

                        <View style={styles.pickerRow}>
                            <Pressable
                                style={styles.pickerButton}
                                onPress={() => setActivePicker("endDate")}
                            >
                                <Ionicons
                                    name="calendar-outline"
                                    size={20}
                                    color={colors.primary}
                                />
                                <View>
                                    <Text style={styles.pickerLabel}>Date</Text>
                                    <Text style={styles.pickerValue}>
                                        {formatDisplayDate(endDateTime)}
                                    </Text>
                                </View>
                            </Pressable>

                            <Pressable
                                style={styles.pickerButton}
                                onPress={() => setActivePicker("endTime")}
                            >
                                <Ionicons
                                    name="time-outline"
                                    size={20}
                                    color={colors.primary}
                                />
                                <View>
                                    <Text style={styles.pickerLabel}>Time</Text>
                                    <Text style={styles.pickerValue}>
                                        {formatDisplayTime(endDateTime)}
                                    </Text>
                                </View>
                            </Pressable>
                        </View>

                        {activePicker ? (
                            <DateTimePicker
                                value={pickerValue}
                                mode={pickerMode}
                                display={Platform.OS === "ios" ? "spinner" : "default"}
                                onValueChange={handlePickerValueChange}
                                onDismiss={handlePickerDismiss}
                            />
                        ) : null}

                        <Pressable
                            onPress={handleUpdateTrip}
                            disabled={isSubmitting}
                            style={[
                                styles.submitButton,
                                isSubmitting && styles.disabledButton,
                            ]}
                        >
                            {isSubmitting ? (
                                <ActivityIndicator color="#FFFFFF" />
                            ) : (
                                <Text style={styles.submitButtonText}>Save Trip</Text>
                            )}
                        </Pressable>
                    </View>
                </ScrollView>
            </KeyboardAvoidingView>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
        backgroundColor: colors.background,
    },
    keyboardView: {
        flex: 1,
    },
    container: {
        padding: spacing.lg,
        paddingBottom: 120,
    },
    centerContainer: {
        flex: 1,
        backgroundColor: colors.background,
        alignItems: "center",
        justifyContent: "center",
        padding: spacing.lg,
    },
    loadingText: {
        marginTop: spacing.sm,
        color: colors.mutedText,
        fontWeight: "700",
    },
    header: {
        flexDirection: "row",
        gap: spacing.md,
        alignItems: "center",
        marginBottom: spacing.lg,
    },
    backButton: {
        width: 44,
        height: 44,
        borderRadius: 16,
        backgroundColor: colors.card,
        alignItems: "center",
        justifyContent: "center",
        ...shadow.card,
    },
    title: {
        fontSize: 26,
        fontWeight: "900",
        color: colors.text,
    },
    subtitle: {
        marginTop: 4,
        color: colors.mutedText,
        fontWeight: "700",
    },
    card: {
        backgroundColor: colors.card,
        borderRadius: radius.xl,
        padding: spacing.lg,
        ...shadow.card,
    },
    inputGroup: {
        marginBottom: spacing.md,
    },
    label: {
        fontSize: 14,
        fontWeight: "800",
        color: colors.text,
        marginBottom: spacing.sm,
    },
    input: {
        backgroundColor: colors.background,
        borderRadius: radius.md,
        paddingHorizontal: spacing.md,
        paddingVertical: 14,
        fontSize: 15,
        color: colors.text,
        fontWeight: "700",
        borderWidth: 1,
        borderColor: colors.border,
    },
    divider: {
        height: 1,
        backgroundColor: colors.border,
        marginVertical: spacing.md,
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: "900",
        color: colors.text,
        marginBottom: spacing.sm,
    },
    pickerRow: {
        flexDirection: "row",
        gap: spacing.md,
        marginBottom: spacing.lg,
    },
    pickerButton: {
        flex: 1,
        backgroundColor: colors.background,
        borderRadius: radius.md,
        padding: spacing.md,
        borderWidth: 1,
        borderColor: colors.border,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
    },
    pickerLabel: {
        fontSize: 12,
        color: colors.mutedText,
        fontWeight: "800",
    },
    pickerValue: {
        fontSize: 13,
        color: colors.text,
        fontWeight: "800",
        marginTop: 2,
    },
    submitButton: {
        marginTop: spacing.lg,
        backgroundColor: colors.primary,
        borderRadius: radius.lg,
        paddingVertical: 16,
        alignItems: "center",
    },
    disabledButton: {
        opacity: 0.6,
    },
    submitButtonText: {
        color: "#FFFFFF",
        fontSize: 16,
        fontWeight: "900",
    },
    errorTitle: {
        fontSize: 22,
        fontWeight: "900",
        color: colors.text,
        marginBottom: spacing.sm,
    },
    errorText: {
        textAlign: "center",
        color: colors.mutedText,
        lineHeight: 21,
        marginBottom: spacing.lg,
    },
    retryButton: {
        backgroundColor: colors.primary,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.md,
        borderRadius: radius.md,
        marginBottom: spacing.md,
    },
    retryButtonText: {
        color: "#FFFFFF",
        fontWeight: "900",
    },
    backTextButton: {
        padding: spacing.sm,
    },
    backText: {
        color: colors.primary,
        fontWeight: "800",
    },
});