import { useState, type ComponentProps } from "react";
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
import { useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import DateTimePicker from "@react-native-community/datetimepicker";
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

import { createTrip } from "@/src/api/tripApi";
import { colors, radius, shadow, spacing } from "@/src/theme/theme";
import { logger } from "@/src/utils/logger";

export default function CreateTripScreen() {
    const router = useRouter();

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(18, 0, 0, 0);

    const today = new Date();
    today.setHours(9, 0, 0, 0);

    const [tripName, setTripName] = useState("");
    const [destination, setDestination] = useState("");
    const [startDateTime, setStartDateTime] = useState(today);
    const [endDateTime, setEndDateTime] = useState(tomorrow);
    const [activePicker, setActivePicker] = useState<PickerTarget>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

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
                            <Text style={styles.title}>Create Trip</Text>
                            <Text style={styles.subtitle}>
                                Add your destination and travel dates
                            </Text>
                        </View>
                    </View>

                    <View style={styles.card}>
                        <View style={styles.inputGroup}>
                            <Text style={styles.label}>Trip name</Text>
                            <TextInput
                                value={tripName}
                                onChangeText={setTripName}
                                placeholder="e.g. Sydney Weekend"
                                placeholderTextColor="#9CA3AF"
                                style={styles.input}
                            />
                        </View>

                        <View style={styles.inputGroup}>
                            <Text style={styles.label}>Destination</Text>
                            <TextInput
                                value={destination}
                                onChangeText={setDestination}
                                placeholder="e.g. Sydney"
                                placeholderTextColor="#9CA3AF"
                                style={styles.input}
                            />
                        </View>

                        <View style={styles.sectionDivider} />

                        <Text style={styles.sectionTitle}>Start</Text>

                        <View style={styles.pickerRow}>
                            <Pressable
                                style={styles.pickerButton}
                                onPress={() => setActivePicker("startDate")}
                            >
                                <Ionicons name="calendar-outline" size={20} color={colors.primary} />
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
                                <Ionicons name="time-outline" size={20} color={colors.primary} />
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
                                <Ionicons name="calendar-outline" size={20} color={colors.primary} />
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
                                <Ionicons name="time-outline" size={20} color={colors.primary} />
                                <View>
                                    <Text style={styles.pickerLabel}>Time</Text>
                                    <Text style={styles.pickerValue}>
                                        {formatDisplayTime(endDateTime)}
                                    </Text>
                                </View>
                            </Pressable>
                        </View>

                        <Pressable
                            onPress={handleCreateTrip}
                            disabled={isSubmitting}
                            style={({ pressed }) => [
                                styles.createButton,
                                pressed && !isSubmitting ? styles.createButtonPressed : null,
                                isSubmitting ? styles.createButtonDisabled : null,
                            ]}
                        >
                            {isSubmitting ? (
                                <ActivityIndicator color="#FFFFFF" />
                            ) : (
                                <>
                                    <Text style={styles.createButtonText}>Create trip</Text>
                                    <Ionicons name="checkmark-circle" size={20} color="#FFFFFF" />
                                </>
                            )}
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
    header: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
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
        fontSize: 28,
        fontWeight: "900",
        color: colors.text,
    },
    subtitle: {
        fontSize: 15,
        color: colors.mutedText,
        marginTop: 3,
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
        minHeight: 54,
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: radius.md,
        paddingHorizontal: spacing.md,
        fontSize: 16,
        color: colors.text,
        backgroundColor: "#FFFFFF",
    },
    sectionDivider: {
        height: 1,
        backgroundColor: colors.border,
        marginVertical: spacing.md,
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: "900",
        color: colors.text,
        marginBottom: spacing.sm,
        marginTop: spacing.sm,
    },
    pickerRow: {
        gap: spacing.sm,
        marginBottom: spacing.md,
    },
    pickerButton: {
        minHeight: 64,
        borderRadius: radius.md,
        borderWidth: 1,
        borderColor: colors.border,
        backgroundColor: "#FFFFFF",
        paddingHorizontal: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    pickerLabel: {
        fontSize: 12,
        color: colors.mutedText,
        fontWeight: "700",
        marginBottom: 2,
    },
    pickerValue: {
        fontSize: 15,
        color: colors.text,
        fontWeight: "800",
    },
    createButton: {
        height: 56,
        borderRadius: radius.md,
        backgroundColor: colors.primary,
        alignItems: "center",
        justifyContent: "center",
        flexDirection: "row",
        gap: spacing.sm,
        marginTop: spacing.sm,
    },
    createButtonPressed: {
        backgroundColor: colors.primaryDark,
        transform: [{ scale: 0.99 }],
    },
    createButtonDisabled: {
        opacity: 0.65,
    },
    createButtonText: {
        color: "#FFFFFF",
        fontSize: 16,
        fontWeight: "900",
    },
});