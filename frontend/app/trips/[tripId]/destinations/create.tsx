import {useState, type ComponentProps} from "react";
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
import {SafeAreaView} from "react-native-safe-area-context";
import {useLocalSearchParams, useRouter} from "expo-router";
import {Ionicons} from "@expo/vector-icons";
import DateTimePicker from "@react-native-community/datetimepicker";
import {
    getApiErrorMessage,
    hasApiWarning,
} from "@/src/utils/apiWarningUtils";
import {createDestination} from "@/src/api/destinationApi";
import {colors, radius, shadow, spacing} from "@/src/theme/theme";
import {
    formatDisplayDate,
    formatDisplayTime,
    formatForBackend,
    type PickerTarget,
    updateDatePart,
    updateTimePart,
} from "@/src/utils/dateTimePickerUtils";

export default function CreateDestinationScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId)
        ? params.tripId[0]
        : params.tripId;

    const defaultStart = new Date();
    defaultStart.setHours(9, 0, 0, 0);

    const defaultEnd = new Date();
    defaultEnd.setDate(defaultEnd.getDate() + 1);
    defaultEnd.setHours(18, 0, 0, 0);

    const [destinationName, setDestinationName] = useState("");
    const [destinationOrder, setDestinationOrder] = useState("");
    const [notes, setNotes] = useState("");
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

    function handlePickerDismiss() {
        setActivePicker(null);
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

    async function submitDestination(allowOverlap = false) {
        const tripNumberId = Number(tripIdParam);

        await createDestination(tripNumberId, {
            destinationName: destinationName.trim(),
            startDate: formatForBackend(startDateTime),
            endDate: formatForBackend(endDateTime),
            destinationOrder: destinationOrder.trim()
                ? Number(destinationOrder)
                : null,
            notes: notes.trim() || null,
            allowOverlap,
        });
    }

    async function handleCreateDestination() {
        const tripNumberId = Number(tripIdParam);

        if (!tripIdParam || Number.isNaN(tripNumberId)) {
            Alert.alert("Missing trip", "Trip ID is missing.");
            return;
        }

        if (!destinationName.trim()) {
            Alert.alert("Missing destination", "Please enter destination name.");
            return;
        }

        if (endDateTime <= startDateTime) {
            Alert.alert("Invalid date", "End date/time must be after start date/time.");
            return;
        }

        try {
            setIsSubmitting(true);

            await submitDestination(false);

            Alert.alert("Destination created", "Destination has been added.");
            router.back();
        } catch (error: any) {
            console.log("Create destination failed:", error.response?.data || error.message);

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

                                    await submitDestination(true);

                                    Alert.alert(
                                        "Destination created",
                                        "Destination has been added."
                                    );

                                    router.back();
                                } catch (confirmError: any) {
                                    console.log(
                                        "Create destination after confirmation failed:",
                                        confirmError.response?.data ||
                                        confirmError.message
                                    );

                                    Alert.alert(
                                        "Create destination failed",
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
                "Create destination failed",
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
                            <Ionicons name="chevron-back" size={24} color={colors.text}/>
                        </Pressable>

                        <View style={{flex: 1}}>
                            <Text style={styles.title}>Add Destination</Text>
                            <Text style={styles.subtitle}>
                                Add a city or place inside this trip
                            </Text>
                        </View>
                    </View>

                    <View style={styles.card}>
                        <View style={styles.inputGroup}>
                            <Text style={styles.label}>Destination name</Text>
                            <TextInput
                                value={destinationName}
                                onChangeText={setDestinationName}
                                placeholder="e.g. Tokyo"
                                placeholderTextColor="#9CA3AF"
                                style={styles.input}
                            />
                        </View>

                        <View style={styles.inputGroup}>
                            <Text style={styles.label}>Order</Text>
                            <TextInput
                                value={destinationOrder}
                                onChangeText={setDestinationOrder}
                                placeholder="e.g. 1"
                                placeholderTextColor="#9CA3AF"
                                keyboardType="number-pad"
                                style={styles.input}
                            />
                        </View>

                        <View style={styles.inputGroup}>
                            <Text style={styles.label}>Notes</Text>
                            <TextInput
                                value={notes}
                                onChangeText={setNotes}
                                placeholder="Optional notes"
                                placeholderTextColor="#9CA3AF"
                                style={[styles.input, styles.textArea]}
                                multiline
                            />
                        </View>

                        <View style={styles.divider}/>

                        <Text style={styles.sectionTitle}>Start</Text>

                        <View style={styles.pickerRow}>
                            <Pressable
                                style={styles.pickerButton}
                                onPress={() => setActivePicker("startDate")}
                            >
                                <Ionicons name="calendar-outline" size={20} color={colors.primary}/>
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
                                <Ionicons name="time-outline" size={20} color={colors.primary}/>
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
                                <Ionicons name="calendar-outline" size={20} color={colors.primary}/>
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
                                <Ionicons name="time-outline" size={20} color={colors.primary}/>
                                <View>
                                    <Text style={styles.pickerLabel}>Time</Text>
                                    <Text style={styles.pickerValue}>
                                        {formatDisplayTime(endDateTime)}
                                    </Text>
                                </View>
                            </Pressable>
                        </View>

                        {activePicker && (
                            <DateTimePicker
                                value={pickerValue}
                                mode={pickerMode}
                                display={Platform.OS === "ios" ? "spinner" : "default"}
                                onValueChange={handlePickerValueChange}
                                onDismiss={handlePickerDismiss}
                            />
                        )}

                        <Pressable
                            onPress={handleCreateDestination}
                            disabled={isSubmitting}
                            style={[
                                styles.submitButton,
                                isSubmitting && styles.disabledButton,
                            ]}
                        >
                            {isSubmitting ? (
                                <ActivityIndicator color="#FFFFFF"/>
                            ) : (
                                <Text style={styles.submitButtonText}>Create Destination</Text>
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