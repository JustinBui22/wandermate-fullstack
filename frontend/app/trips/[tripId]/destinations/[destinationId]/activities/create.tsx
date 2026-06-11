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
import { useLocalSearchParams, useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import DateTimePicker from "@react-native-community/datetimepicker";

import { createActivity } from "@/src/api/activityApi";
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
    getApiErrorMessage,
    getApiErrorTitle,
} from "@/src/utils/apiWarningUtils";

type DateTimePickerValueChange = NonNullable<
    ComponentProps<typeof DateTimePicker>["onValueChange"]
>;

export default function CreateActivityScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId)
        ? params.tripId[0]
        : params.tripId;

    const destinationIdParam = Array.isArray(params.destinationId)
        ? params.destinationId[0]
        : params.destinationId;

    const defaultStart = new Date();
    defaultStart.setHours(9, 0, 0, 0);

    const defaultEnd = new Date();
    defaultEnd.setHours(10, 0, 0, 0);

    const [activityName, setActivityName] = useState("");
    const [location, setLocation] = useState("");
    const [description, setDescription] = useState("");
    const [startDateTime, setStartDateTime] = useState(defaultStart);
    const [endDateTime, setEndDateTime] = useState(defaultEnd);

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

    async function handleCreateActivity() {
        const tripNumberId = Number(tripIdParam);
        const destinationNumberId = Number(destinationIdParam);

        if (
            !tripIdParam ||
            !destinationIdParam ||
            Number.isNaN(tripNumberId) ||
            Number.isNaN(destinationNumberId)
        ) {
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
            console.log("Create activity failed:", error.response?.data || error.message);

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
        activePicker === "startTime" || activePicker === "endTime"
            ? "time"
            : "date";

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
                            <Text style={styles.title}>Add Activity</Text>
                            <Text style={styles.subtitle}>
                                Add a plan inside this destination
                            </Text>
                        </View>
                    </View>

                    <View style={styles.card}>
                        <View style={styles.inputGroup}>
                            <Text style={styles.label}>Activity name</Text>
                            <TextInput
                                value={activityName}
                                onChangeText={setActivityName}
                                placeholder="e.g. Visit Tokyo Tower"
                                placeholderTextColor="#9CA3AF"
                                style={styles.input}
                            />
                        </View>

                        <View style={styles.inputGroup}>
                            <Text style={styles.label}>Location</Text>
                            <TextInput
                                value={location}
                                onChangeText={setLocation}
                                placeholder="e.g. Tokyo Tower"
                                placeholderTextColor="#9CA3AF"
                                style={styles.input}
                            />
                        </View>

                        <View style={styles.inputGroup}>
                            <Text style={styles.label}>Description</Text>
                            <TextInput
                                value={description}
                                onChangeText={setDescription}
                                placeholder="Optional details"
                                placeholderTextColor="#9CA3AF"
                                style={[styles.input, styles.textArea]}
                                multiline
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
                            onPress={handleCreateActivity}
                            disabled={isSubmitting}
                            style={[
                                styles.submitButton,
                                isSubmitting && styles.disabledButton,
                            ]}
                        >
                            {isSubmitting ? (
                                <ActivityIndicator color="#FFFFFF" />
                            ) : (
                                <Text style={styles.submitButtonText}>
                                    Create Activity
                                </Text>
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
    textArea: {
        minHeight: 90,
        textAlignVertical: "top",
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
});