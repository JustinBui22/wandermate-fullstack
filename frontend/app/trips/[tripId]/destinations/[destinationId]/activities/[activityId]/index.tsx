import { useCallback, useState } from "react";
import {
    ActivityIndicator,
    Alert,
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";

import {
    deleteActivity,
    getActivityById,
} from "@/src/api/activityApi";
import type { Activity } from "@/src/types/activity";
import { colors, radius, shadow, spacing } from "@/src/theme/theme";
import { formatDateTime } from "@/src/utils/dateFormat";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { logger } from "@/src/utils/logger";

export default function ActivityDetailScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId)
        ? params.tripId[0]
        : params.tripId;

    const destinationIdParam = Array.isArray(params.destinationId)
        ? params.destinationId[0]
        : params.destinationId;

    const activityIdParam = Array.isArray(params.activityId)
        ? params.activityId[0]
        : params.activityId;

    const [activity, setActivity] = useState<Activity | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function loadActivityDetail() {
        const tripNumberId = Number(tripIdParam);
        const destinationNumberId = Number(destinationIdParam);
        const activityNumberId = Number(activityIdParam);

        if (
            !tripIdParam ||
            !destinationIdParam ||
            !activityIdParam ||
            Number.isNaN(tripNumberId) ||
            Number.isNaN(destinationNumberId) ||
            Number.isNaN(activityNumberId)
        ) {
            setError("Trip ID, destination ID, or activity ID is missing.");
            setIsLoading(false);
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const data = await getActivityById(
                tripNumberId,
                destinationNumberId,
                activityNumberId
            );

            setActivity(data);
        } catch (error: any) {
            logger.debug(
                "Load activity detail failed:",
                error.response?.data || error.message
            );

            setError(
                getApiErrorMessage(
                    error,
                    "Failed to load activity. Please try again."
                )
            );
        } finally {
            setIsLoading(false);
        }
    }

    useFocusEffect(
        useCallback(() => {
            loadActivityDetail();
        }, [tripIdParam, destinationIdParam, activityIdParam])
    );

    function handleEditActivity() {
        if (!tripIdParam || !destinationIdParam || !activityIdParam) {
            Alert.alert("Missing activity", "Activity ID is missing.");
            return;
        }

        router.push(
            `/trips/${tripIdParam}/destinations/${destinationIdParam}/activities/${activityIdParam}/edit`
        );
    }

    function handleDeleteActivity() {
        if (!tripIdParam || !destinationIdParam || !activityIdParam) {
            Alert.alert("Missing activity", "Activity ID is missing.");
            return;
        }

        Alert.alert(
            "Delete activity",
            "Are you sure you want to delete this activity?",
            [
                {
                    text: "Cancel",
                    style: "cancel",
                },
                {
                    text: "Delete",
                    style: "destructive",
                    onPress: async () => {
                        const tripNumberId = Number(tripIdParam);
                        const destinationNumberId = Number(destinationIdParam);
                        const activityNumberId = Number(activityIdParam);

                        try {
                            setIsDeleting(true);

                            await deleteActivity(
                                tripNumberId,
                                destinationNumberId,
                                activityNumberId
                            );

                            Alert.alert("Activity deleted", "Activity has been deleted.");
                            router.back();
                        } catch (error: any) {
                            logger.debug(
                                "Delete activity failed:",
                                error.response?.data || error.message
                            );

                            Alert.alert(
                                "Delete activity failed",
                                getApiErrorMessage(
                                    error,
                                    "Please try again."
                                )
                            );
                        } finally {
                            setIsDeleting(false);
                        }
                    },
                },
            ]
        );
    }

    if (isLoading) {
        return (
            <SafeAreaView style={styles.centerContainer}>
            <ActivityIndicator color={colors.primary} />
        <Text style={styles.loadingText}>Loading activity...</Text>
        </SafeAreaView>
    );
    }

    if (error || !activity) {
        return (
            <SafeAreaView style={styles.centerContainer}>
            <View style={styles.errorIcon}>
            <Ionicons
                name="alert-circle-outline"
        size={34}
        color={colors.error}
        />
        </View>

        <Text style={styles.errorTitle}>Unable to load activity</Text>
        <Text style={styles.errorText}>
            {error ?? "Activity not found."}
        </Text>

        <Pressable onPress={loadActivityDetail} style={styles.retryButton}>
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
        <ScrollView
            contentContainerStyle={styles.container}
    showsVerticalScrollIndicator={false}
    >
    <View style={styles.header}>
    <Pressable onPress={() => router.back()} style={styles.iconButton}>
    <Ionicons name="chevron-back" size={24} color={colors.text} />
    </Pressable>

    <Pressable onPress={handleEditActivity} style={styles.iconButton}>
    <Ionicons name="create-outline" size={23} color={colors.text} />
    </Pressable>
    </View>

    <View style={styles.heroCard}>
    <View style={styles.heroIcon}>
    <Ionicons name="walk" size={30} color="#FFFFFF" />
        </View>

        <Text style={styles.label}>Activity</Text>

        <Text style={styles.title}>
        {activity.activityName}
        </Text>

    {activity.location ? (
        <Text style={styles.location}>
            {activity.location}
            </Text>
    ) : (
        <Text style={styles.locationMuted}>
            No location added.
    </Text>
    )}
    </View>

    <View style={styles.infoGrid}>
    <View style={styles.infoCard}>
    <View style={styles.infoIcon}>
    <Ionicons
        name="time-outline"
    size={20}
    color={colors.primary}
    />
    </View>

    <Text style={styles.infoLabel}>Start</Text>
        <Text style={styles.infoValue}>
        {formatDateTime(activity.startDateTime)}
    </Text>
    </View>

    <View style={styles.infoCard}>
    <View style={styles.infoIcon}>
    <Ionicons
        name="flag-outline"
    size={20}
    color={colors.primary}
    />
    </View>

    <Text style={styles.infoLabel}>End</Text>
        <Text style={styles.infoValue}>
        {formatDateTime(activity.endDateTime)}
    </Text>
    </View>
    </View>

    <View style={styles.descriptionCard}>
    <Text style={styles.descriptionTitle}>Description</Text>
        <Text style={styles.descriptionText}>
        {activity.description || "No description added yet."}
        </Text>
        </View>

        <Pressable
    onPress={handleDeleteActivity}
    disabled={isDeleting}
    style={[
            styles.deleteButton,
        isDeleting && styles.disabledButton,
]}
>
    {isDeleting ? (
        <ActivityIndicator color="#FFFFFF" />
    ) : (
        <>
            <Ionicons name="trash-outline" size={20} color="#FFFFFF" />
    <Text style={styles.deleteButtonText}>Delete Activity</Text>
    </>
    )}
    </Pressable>
    </ScrollView>
    </SafeAreaView>
);
}

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
        backgroundColor: colors.background,
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
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: spacing.lg,
    },
    iconButton: {
        width: 44,
        height: 44,
        borderRadius: 16,
        backgroundColor: colors.card,
        alignItems: "center",
        justifyContent: "center",
        ...shadow.card,
    },
    heroCard: {
        backgroundColor: colors.primary,
        borderRadius: radius.xl,
        padding: spacing.xl,
        marginBottom: spacing.lg,
        ...shadow.card,
    },
    heroIcon: {
        width: 58,
        height: 58,
        borderRadius: 20,
        backgroundColor: "rgba(255,255,255,0.18)",
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.lg,
    },
    label: {
        color: "#DBEAFE",
        fontSize: 15,
        fontWeight: "800",
        marginBottom: spacing.sm,
    },
    title: {
        color: "#FFFFFF",
        fontSize: 30,
        lineHeight: 36,
        fontWeight: "900",
    },
    location: {
        color: "#E0F2FE",
        fontSize: 15,
        lineHeight: 22,
        marginTop: spacing.md,
        fontWeight: "700",
    },
    locationMuted: {
        color: "#BFDBFE",
        fontSize: 15,
        lineHeight: 22,
        marginTop: spacing.md,
        fontWeight: "600",
    },
    infoGrid: {
        gap: spacing.md,
        marginBottom: spacing.lg,
    },
    infoCard: {
        backgroundColor: colors.card,
        borderRadius: radius.lg,
        padding: spacing.md,
        ...shadow.card,
    },
    infoIcon: {
        width: 42,
        height: 42,
        borderRadius: 14,
        backgroundColor: colors.softBlue,
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.sm,
    },
    infoLabel: {
        color: colors.mutedText,
        fontSize: 13,
        fontWeight: "700",
        marginBottom: 4,
    },
    infoValue: {
        color: colors.text,
        fontSize: 16,
        fontWeight: "800",
        lineHeight: 22,
    },
    descriptionCard: {
        backgroundColor: colors.card,
        borderRadius: radius.lg,
        padding: spacing.md,
        marginBottom: spacing.lg,
        ...shadow.card,
    },
    descriptionTitle: {
        fontSize: 16,
        fontWeight: "900",
        color: colors.text,
        marginBottom: spacing.sm,
    },
    descriptionText: {
        color: colors.mutedText,
        fontSize: 15,
        lineHeight: 22,
        fontWeight: "600",
    },
    deleteButton: {
        backgroundColor: colors.error,
        borderRadius: radius.lg,
        paddingVertical: 16,
        alignItems: "center",
        justifyContent: "center",
        flexDirection: "row",
        gap: spacing.sm,
    },
    disabledButton: {
        opacity: 0.6,
    },
    deleteButtonText: {
        color: "#FFFFFF",
        fontSize: 16,
        fontWeight: "900",
    },
    errorIcon: {
        width: 72,
        height: 72,
        borderRadius: 24,
        backgroundColor: "#FEF2F2",
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.md,
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