import { useCallback, useState } from "react";
import { getActivitiesByDestination } from "@/src/api/activityApi";
import type { Activity } from "@/src/types/activity";
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
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import {
    deleteDestination,
    getDestinationById,
} from "@/src/api/destinationApi";
import type { Destination } from "@/src/types/destination";
import { colors, radius, shadow, spacing } from "@/src/theme/theme";
import { formatDateTime } from "@/src/utils/dateFormat";
import { logger } from "@/src/utils/logger";

export default function DestinationDetailScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();
    const [activities, setActivities] = useState<Activity[]>([]);
    const tripIdParam = Array.isArray(params.tripId)
        ? params.tripId[0]
        : params.tripId;

    const destinationIdParam = Array.isArray(params.destinationId)
        ? params.destinationId[0]
        : params.destinationId;

    const [destination, setDestination] = useState<Destination | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function loadDestinationDetail() {
        const tripNumberId = Number(tripIdParam);
        const destinationNumberId = Number(destinationIdParam);

        if (
            !tripIdParam ||
            !destinationIdParam ||
            Number.isNaN(tripNumberId) ||
            Number.isNaN(destinationNumberId)
        ) {
            setError("Trip ID or destination ID is missing.");
            setIsLoading(false);
            return;
        }

        try {
            const [destinationData, activityData] = await Promise.all([
                getDestinationById(tripNumberId, destinationNumberId),
                getActivitiesByDestination(tripNumberId, destinationNumberId),
            ]);

            logger.debug("Destination detail:", destinationData);
            logger.debug("Activities loaded:", activityData);

            setDestination(destinationData);
            setActivities(Array.isArray(activityData) ? activityData : []);
        } catch (error: any) {
            logger.debug(
                "Load destination detail failed:",
                error.response?.data || error.message
            );

            setError(
                error.response?.data?.message ||
                "Failed to load destination detail. Please try again."
            );
        } finally {
            setIsLoading(false);
        }
    }

    useFocusEffect(
        useCallback(() => {
            loadDestinationDetail();
        }, [tripIdParam, destinationIdParam])
    );

    function handleEditDestination() {
        if (!tripIdParam || !destinationIdParam) {
            Alert.alert("Missing destination", "Destination ID is missing.");
            return;
        }

        router.push(
            `/trips/${tripIdParam}/destinations/${destinationIdParam}/edit`
        );
    }

    function handleDeleteDestination() {
        if (!tripIdParam || !destinationIdParam) {
            Alert.alert("Missing destination", "Trip ID or destination ID is missing.");
            return;
        }

        Alert.alert(
            "Delete destination",
            "Are you sure you want to delete this destination? All activities inside this destination will also be deleted.",
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

                        if (
                            Number.isNaN(tripNumberId) ||
                            Number.isNaN(destinationNumberId)
                        ) {
                            Alert.alert(
                                "Missing destination",
                                "Trip ID or destination ID is invalid."
                            );
                            return;
                        }

                        try {
                            setIsDeleting(true);

                            await deleteDestination(
                                tripNumberId,
                                destinationNumberId
                            );

                            Alert.alert(
                                "Destination deleted",
                                "Destination has been deleted."
                            );

                            router.back();
                        } catch (error: any) {
                            logger.debug(
                                "Delete destination failed:",
                                error.response?.data || error.message
                            );

                            Alert.alert(
                                "Delete destination failed",
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

    function handleAddActivity() {
        if (!tripIdParam || !destinationIdParam) {
            Alert.alert("Missing destination", "Trip ID or destination ID is missing.");
            return;
        }

        router.push(
            `/trips/${tripIdParam}/destinations/${destinationIdParam}/activities/create`
        );
    }

    function handleOpenActivity(activityId: number) {
        if (!tripIdParam || !destinationIdParam) {
            Alert.alert("Missing activity", "Trip ID or destination ID is missing.");
            return;
        }

        router.push(
            `/trips/${tripIdParam}/destinations/${destinationIdParam}/activities/${activityId}`
        );
    }

    if (isLoading) {
        return (
            <SafeAreaView style={styles.centerContainer}>
                <ActivityIndicator color={colors.primary} />
                <Text style={styles.loadingText}>Loading destination...</Text>
            </SafeAreaView>
        );
    }

    if (error || !destination) {
        return (
            <SafeAreaView style={styles.centerContainer}>
                <View style={styles.errorIcon}>
                    <Ionicons
                        name="alert-circle-outline"
                        size={34}
                        color={colors.error}
                    />
                </View>

                <Text style={styles.errorTitle}>Unable to load destination</Text>
                <Text style={styles.errorText}>
                    {error ?? "Destination not found."}
                </Text>

                <Pressable onPress={loadDestinationDetail} style={styles.retryButton}>
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

                    <Pressable onPress={handleEditDestination} style={styles.iconButton}>
                        <Ionicons name="create-outline" size={23} color={colors.text} />
                    </Pressable>
                </View>

                <View style={styles.heroCard}>
                    <View style={styles.heroIcon}>
                        <Ionicons name="location" size={30} color="#FFFFFF" />
                    </View>

                    <Text style={styles.destinationLabel}>Destination</Text>

                    <Text style={styles.destinationName}>
                        {destination.destinationName}
                    </Text>

                    {destination.notes ? (
                        <Text style={styles.notes}>{destination.notes}</Text>
                    ) : (
                        <Text style={styles.notesMuted}>No notes added yet.</Text>
                    )}
                </View>

                <View style={styles.infoGrid}>
                    <View style={styles.infoCard}>
                        <View style={styles.infoIcon}>
                            <Ionicons
                                name="calendar-outline"
                                size={20}
                                color={colors.primary}
                            />
                        </View>

                        <Text style={styles.infoLabel}>Start</Text>
                        <Text style={styles.infoValue}>
                            {formatDateTime(destination.startDate)}
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
                            {formatDateTime(destination.endDate)}
                        </Text>
                    </View>

                    <View style={styles.infoCard}>
                        <View style={styles.infoIcon}>
                            <Ionicons
                                name="swap-vertical-outline"
                                size={20}
                                color={colors.primary}
                            />
                        </View>

                        <Text style={styles.infoLabel}>Order</Text>
                        <Text style={styles.infoValue}>
                            {destination.destinationOrder ?? "Not set"}
                        </Text>
                    </View>
                </View>

                <View style={styles.sectionHeader}>
                    <View>
                        <Text style={styles.sectionTitle}>Activities</Text>
                        <Text style={styles.sectionSubtitle}>
                            Add plans inside this destination
                        </Text>
                    </View>

                    <Pressable onPress={handleAddActivity} style={styles.smallAddButton}>
                        <Ionicons name="add" size={22} color="#FFFFFF" />
                    </Pressable>
                </View>

                {activities.length === 0 ? (
                    <View style={styles.emptyCard}>
                        <View style={styles.emptyIcon}>
                            <Ionicons name="walk-outline" size={34} color={colors.primary} />
                        </View>

                        <Text style={styles.emptyTitle}>No activities yet</Text>
                        <Text style={styles.emptyText}>
                            Add activities such as sightseeing, meals, transport, or hotel check-ins.
                        </Text>

                        <Pressable onPress={handleAddActivity} style={styles.activityButton}>
                            <Text style={styles.activityButtonText}>Add first activity</Text>
                        </Pressable>
                    </View>
                ) : (
                    <View style={styles.activityList}>
                        {activities.map((activity) => (
                            <Pressable
                                key={activity.activityId}
                                style={styles.activityCard}
                                onPress={() => handleOpenActivity(activity.activityId)}
                            >
                                <View style={styles.activityIcon}>
                                    <Ionicons name="walk" size={22} color={colors.primary} />
                                </View>

                                <View style={styles.activityContent}>
                                    <Text style={styles.activityTitle}>
                                        {activity.activityName}
                                    </Text>

                                    {activity.location ? (
                                        <Text style={styles.activityLocation} numberOfLines={1}>
                                            {activity.location}
                                        </Text>
                                    ) : null}

                                    <Text style={styles.activityTime}>
                                        {formatDateTime(activity.startDateTime)} - {formatDateTime(activity.endDateTime)}
                                    </Text>

                                    {activity.description ? (
                                        <Text style={styles.activityDescription} numberOfLines={2}>
                                            {activity.description}
                                        </Text>
                                    ) : null}
                                </View>

                                <Ionicons
                                    name="chevron-forward"
                                    size={22}
                                    color={colors.mutedText}
                                />
                            </Pressable>
                        ))}
                    </View>
                )}
                <Pressable
                    onPress={handleDeleteDestination}
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
                            <Text style={styles.deleteButtonText}>Delete Destination</Text>
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
    destinationLabel: {
        color: "#DBEAFE",
        fontSize: 15,
        fontWeight: "800",
        marginBottom: spacing.sm,
    },
    destinationName: {
        color: "#FFFFFF",
        fontSize: 30,
        lineHeight: 36,
        fontWeight: "900",
    },
    notes: {
        color: "#E0F2FE",
        fontSize: 15,
        lineHeight: 22,
        marginTop: spacing.md,
        fontWeight: "600",
    },
    notesMuted: {
        color: "#BFDBFE",
        fontSize: 15,
        lineHeight: 22,
        marginTop: spacing.md,
        fontWeight: "600",
    },
    infoGrid: {
        gap: spacing.md,
        marginBottom: spacing.xl,
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
    sectionHeader: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: spacing.md,
    },
    sectionTitle: {
        fontSize: 23,
        fontWeight: "900",
        color: colors.text,
    },
    sectionSubtitle: {
        fontSize: 14,
        color: colors.mutedText,
        marginTop: 3,
    },
    smallAddButton: {
        width: 46,
        height: 46,
        borderRadius: 17,
        backgroundColor: colors.primary,
        alignItems: "center",
        justifyContent: "center",
        ...shadow.card,
    },
    emptyCard: {
        backgroundColor: colors.card,
        borderRadius: radius.xl,
        padding: spacing.xl,
        alignItems: "center",
        ...shadow.card,
    },
    emptyIcon: {
        width: 72,
        height: 72,
        borderRadius: 24,
        backgroundColor: colors.softBlue,
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.md,
    },
    emptyTitle: {
        fontSize: 21,
        fontWeight: "900",
        color: colors.text,
        marginBottom: spacing.sm,
    },
    emptyText: {
        textAlign: "center",
        color: colors.mutedText,
        lineHeight: 21,
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
    activityButton: {
        marginTop: spacing.lg,
        backgroundColor: colors.primary,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.md,
        borderRadius: radius.md,
    },
    activityButtonText: {
        color: "#FFFFFF",
        fontWeight: "900",
        fontSize: 15,
    },
    activityList: {
        gap: spacing.md,
    },
    activityCard: {
        backgroundColor: colors.card,
        borderRadius: radius.lg,
        padding: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
        ...shadow.card,
    },
    activityIcon: {
        width: 44,
        height: 44,
        borderRadius: 16,
        backgroundColor: colors.softBlue,
        alignItems: "center",
        justifyContent: "center",
    },
    activityContent: {
        flex: 1,
    },
    activityTitle: {
        fontSize: 16,
        fontWeight: "900",
        color: colors.text,
        marginBottom: 4,
    },
    activityLocation: {
        fontSize: 13,
        fontWeight: "700",
        color: colors.mutedText,
        marginBottom: 4,
    },
    activityTime: {
        fontSize: 12,
        fontWeight: "700",
        color: colors.mutedText,
    },
    activityDescription: {
        marginTop: 6,
        fontSize: 13,
        color: colors.mutedText,
        lineHeight: 18,
    },
    deleteButton: {
        marginTop: spacing.xl,
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
});