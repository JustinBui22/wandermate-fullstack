import { useState } from "react";
import {
    Alert,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";

import { createTrip } from "@/src/api/tripApi";
import { DateTimePickerCard } from "@/src/components/forms/DateTimePickerCard";
import { DateSection } from "@/src/components/forms/DateSection";
import { ImageUploadPicker } from "@/src/components/media/ImageUploadPicker";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import { normalizeImageUrl } from "@/src/utils/imageUrlUtils";
import {
    getApiErrorMessage,
    getApiErrorTitle,
    hasApiWarning,
} from "@/src/utils/apiWarningUtils";
import {
    formatDateForBackend,
    type PickerTarget,
    updateDatePart,
} from "@/src/utils/dateTimePickerUtils";

function getDefaultStartDateTime() {
    const today = new Date();
    today.setHours(9, 0, 0, 0);
    return today;
}

function getDefaultEndDateTime() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(18, 0, 0, 0);
    return tomorrow;
}

export default function CreateTripScreen() {
    const router = useRouter();

    const theme = useAppTheme();
    const colors = theme.colors;

    const [tripName, setTripName] = useState("");
    const [destination, setDestination] = useState("");
    const [coverImageUrl, setCoverImageUrl] = useState("");
    const [coverImagePublicId, setCoverImagePublicId] = useState("");
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

        if (activePicker === "endDate") {
            setEndDateTime((current) => updateDatePart(current, selectedDate));
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

    async function submitTrip(allowOverlap = false) {
        await createTrip({
            tripName: tripName.trim(),
            destination: destination.trim(),
            startDate: formatDateForBackend(startDateTime),
            endDate: formatDateForBackend(endDateTime),
            allowOverlap,
            coverImageUrl: normalizeImageUrl(coverImageUrl),
            coverImagePublicId,
        });
    }

    async function handleConfirmCreateWithOverlap() {
        try {
            setIsSubmitting(true);
            setError(null);

            await submitTrip(true);

            Alert.alert(
                "Trip created",
                "Your trip has been created successfully."
            );

            router.back();
        } catch (confirmError: unknown) {
            const message = getApiErrorMessage(
                confirmError,
                "Please check your input and try again."
            );

            setError(message);

            Alert.alert(
                getApiErrorTitle(confirmError, "Create trip failed"),
                message
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    async function handleCreateTrip() {
        setError(null);

        if (!tripName.trim() || !destination.trim()) {
            Alert.alert("Missing details", "Please enter trip name and destination.");
            return;
        }

        if (endDateTime < startDateTime) {
            Alert.alert("Invalid dates", "End date cannot be before start date.");
            return;
        }

        try {
            setIsSubmitting(true);

            await submitTrip(false);

            Alert.alert("Trip created", "Your trip has been created successfully.");
            router.back();
        } catch (error: unknown) {
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
                getApiErrorTitle(error, "Create trip failed"),
                message
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    function handleCreateTripPress() {
        void handleCreateTrip();
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
                    <Ionicons
                        name="chevron-back"
                        size={22}
                        color={colors.primary}
                    />
                </Pressable>

                <View style={styles.headerTextGroup}>
                    <Text style={[styles.eyebrow, { color: colors.primary }]}>
                        New trip
                    </Text>

                    <Text style={[styles.title, { color: colors.text }]}>
                        Create Trip
                    </Text>

                    <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                        Add your destination and travel dates.
                    </Text>
                </View>
            </View>

            <AppCard contentStyle={styles.cardContent}>
                <AppInput
                    label="Trip name"
                    required
                    value={tripName}
                    onChangeText={setTripName}
                    placeholder="e.g. Sydney Weekend"
                    leftIcon={
                        <Ionicons
                            name="airplane-outline"
                            size={20}
                            color={colors.textMuted}
                        />
                    }
                />

                <AppInput
                    label="Destination"
                    required
                    value={destination}
                    onChangeText={setDestination}
                    placeholder="e.g. Sydney"
                    leftIcon={
                        <Ionicons
                            name="location-outline"
                            size={20}
                            color={colors.textMuted}
                        />
                    }
                />

                <ImageUploadPicker
                    label="Trip cover photo"
                    helperText="Optional, but useful for making your trip cards look like a real travel app."
                    imageUrl={coverImageUrl}
                    imageType="trip-covers"
                    previewShape="cover"
                    onChangeImage={(imageUrl, publicId) => {
                        setCoverImageUrl(imageUrl);
                        setCoverImagePublicId(publicId);
                    }}
                />

                <View
                    style={[
                        styles.divider,
                        { backgroundColor: colors.border },
                    ]}
                />

                <DateSection
                    title="Start"
                    date={startDateTime}
                    onDatePress={() => setActivePicker("startDate")}
                />

                <DateSection
                    title="End"
                    date={endDateTime}
                    onDatePress={() => setActivePicker("endDate")}
                />

                <ErrorMessage message={error} title="Create trip failed" />

                <AppButton
                    title="Create trip"
                    onPress={handleCreateTripPress}
                    loading={isSubmitting}
                    rightIcon={
                        <Ionicons
                            name="checkmark-circle"
                            size={20}
                            color={colors.textLight}
                        />
                    }
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
    divider: {
        height: 1,
    },
});