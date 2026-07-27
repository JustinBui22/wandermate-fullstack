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
import { DateSection } from "@/src/components/forms/DateSection";
import { ImageUploadPicker } from "@/src/components/media/ImageUploadPicker";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors as staticColors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import {
    getApiErrorMessage,
    getApiErrorTitle,
    hasApiWarning,
} from "@/src/utils/apiWarningUtils";
import { normalizeImageUrl } from "@/src/utils/imageUrlUtils";
import {
    formatDateForBackend,
    parseDateOnly,
    type PickerTarget,
    updateDatePart,
} from "@/src/utils/dateTimePickerUtils";


export default function EditTripScreen() {
    const router = useRouter();

    const theme = useAppTheme();
    const colors = theme.colors;
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripNumberId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripNumberId);

    const [tripName, setTripName] = useState("");
    const [destination, setDestination] = useState("");
    const [coverImageUrl, setCoverImageUrl] = useState("");
    const [coverImagePublicId, setCoverImagePublicId] = useState("");
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
            setCoverImageUrl(data.coverImageUrl ?? "");
            setCoverImagePublicId(data.coverImagePublicId ?? "");
            setStartDateTime(parseDateOnly(data.startDate, fallbackStart));
            setEndDateTime(parseDateOnly(data.endDate, fallbackEnd));
        } catch (error: unknown) {

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


        if (activePicker === "endDate") {
            setEndDateTime((current) => updateDatePart(current, selectedDate));
            return;
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
            startDate: formatDateForBackend(startDateTime),
            endDate: formatDateForBackend(endDateTime),
            allowOverlap,
            coverImageUrl: normalizeImageUrl(coverImageUrl),
            coverImagePublicId,
        });
    }

    async function handleConfirmOverlapUpdate() {
        try {
            setIsSubmitting(true);
            setError(null);

            await submitTripUpdate(true);

            Alert.alert("Trip updated", "Your trip has been updated.");
            router.back();
        } catch (confirmError: unknown) {
            const message = getApiErrorMessage(
                confirmError,
                "Please check your input and try again."
            );

            setError(message);

            Alert.alert(
                getApiErrorTitle(confirmError, "Update trip failed"),
                message
            );
        } finally {
            setIsSubmitting(false);
        }
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

        if (endDateTime < startDateTime) {
            Alert.alert("Invalid dates", "End date cannot be before start date.");
            return;
        }

        try {
            setIsSubmitting(true);

            await submitTripUpdate(false);

            Alert.alert("Trip updated", "Your trip has been updated.");
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
                                void handleConfirmOverlapUpdate();
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

                <AppButton
                    title="Try again"
                    onPress={() => {
                        void loadTripForEdit();
                    }}
                />
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

                <ImageUploadPicker
                    label="Trip cover photo"
                    helperText="Choose a photo from your phone or camera."
                    imageUrl={coverImageUrl}
                    imageType="trip-covers"
                    previewShape="cover"
                    onChangeImage={(imageUrl, publicId) => {
                        setCoverImageUrl(imageUrl);
                        setCoverImagePublicId(publicId);
                    }}
                />

                <View style={[styles.divider, { backgroundColor: colors.border }]} />

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

                <ErrorMessage message={error} title="Update trip failed" />

                <AppButton
                    title="Save Trip"
                    onPress={() => {
                        void handleUpdateTrip();
                    }}
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
        color: staticColors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
        textAlign: "center",
    },
    centerSubtitle: {
        color: staticColors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 21,
        textAlign: "center",
    },
    errorIconBadge: {
        width: 72,
        height: 72,
        borderRadius: radius.xl,
        backgroundColor: staticColors.dangerSoft,
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
        backgroundColor: staticColors.surface,
        borderWidth: 1,
        borderColor: staticColors.border,
        alignItems: "center",
        justifyContent: "center",
    },
    headerTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    eyebrow: {
        color: staticColors.primary,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.6,
    },
    title: {
        color: staticColors.text,
        fontSize: typography.heading,
        fontWeight: fontWeight.bold,
    },
    subtitle: {
        color: staticColors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    cardContent: {
        gap: spacing.lg,
    },
    divider: {
        height: 1,
        backgroundColor: staticColors.border,
    },
});