import {useCallback, useState} from "react";
import {
    ActivityIndicator,
    Alert,
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from "react-native";
import {SafeAreaView} from "react-native-safe-area-context";
import {useFocusEffect, useLocalSearchParams, useRouter} from "expo-router";
import {Ionicons} from "@expo/vector-icons";
import {getDestinationsByTrip} from "@/src/api/destinationApi";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import {deleteTrip, getTripById} from "@/src/api/tripApi";
import type {Trip} from "@/src/types/trip";
import {colors, radius, shadow, spacing} from "@/src/theme/theme";
import {formatDateTime} from "@/src/utils/dateFormat";
import type {Destination} from "@/src/types/destination";
import { logger } from "@/src/utils/logger";

export default function TripDetailScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();
    const tripIdParam = Array.isArray(params.tripId)
        ? params.tripId[0]
        : params.tripId;
    const [isDeleting, setIsDeleting] = useState(false);
    const [trip, setTrip] = useState<Trip | null>(null);
    const [destinations, setDestinations] = useState<Destination[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    async function loadTripDetail() {
        const tripNumberId = Number(tripIdParam);

        if (!tripIdParam || Number.isNaN(tripNumberId)) {
            setError("Trip ID is missing.");
            setIsLoading(false);
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const [tripData, destinationData] = await Promise.all([
                getTripById(tripNumberId),
                getDestinationsByTrip(tripNumberId),
            ]);

            logger.debug("Trip detail:", tripData);
            logger.debug("Destinations loaded:", destinationData);

            setTrip(tripData);
            setDestinations(Array.isArray(destinationData) ? destinationData : []);
        } catch (error: any) {
            logger.debug("Load trip detail failed:", error.response?.data || error.message);

            setError(
                error.response?.data?.message ||
                "Failed to load trip detail. Please try again."
            );
        } finally {
            setIsLoading(false);
        }
    }

    useFocusEffect(
        useCallback(() => {
            loadTripDetail();
        }, [tripIdParam])
    );

    function handleAddDestination() {
        if (!tripIdParam) {
            Alert.alert("Missing trip", "Trip ID is missing.");
            return;
        }

        router.push(`/trips/${tripIdParam}/destinations/create`);
    }

    function handleOpenDestination(destinationId: number) {
        if (!tripIdParam) {
            Alert.alert("Missing trip", "Trip ID is missing.");
            return;
        }

        router.push(`/trips/${tripIdParam}/destinations/${destinationId}`);
    }

    function handleDeleteTrip() {
        if (!tripIdParam) {
            Alert.alert("Missing trip", "Trip ID is missing.");
            return;
        }

        Alert.alert(
            "Delete trip",
            "This will delete this trip, all destinations, and all activities inside it. Are you sure?",
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

                        if (Number.isNaN(tripNumberId)) {
                            Alert.alert("Missing trip", "Trip ID is invalid.");
                            return;
                        }

                        try {
                            setIsDeleting(true);

                            await deleteTrip(tripNumberId);

                            Alert.alert("Trip deleted", "Trip has been deleted.");
                            router.back();
                        } catch (error: any) {
                            logger.debug(
                                "Delete trip failed:",
                                error.response?.data || error.message
                            );

                            Alert.alert(
                                "Delete trip failed",
                                getApiErrorMessage(error, "Please try again.")
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
                <ActivityIndicator color={colors.primary}/>
                <Text style={styles.loadingText}>Loading trip...</Text>
            </SafeAreaView>
        );
    }

    if (error || !trip) {
        return (
            <SafeAreaView style={styles.centerContainer}>
                <View style={styles.errorIcon}>
                    <Ionicons name="alert-circle-outline" size={34} color={colors.error}/>
                </View>

                <Text style={styles.errorTitle}>Unable to load trip</Text>
                <Text style={styles.errorText}>{error ?? "Trip not found."}</Text>

                <Pressable onPress={loadTripDetail} style={styles.retryButton}>
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
                        <Ionicons name="chevron-back" size={24} color={colors.text}/>
                    </Pressable>

                    <Pressable
                        onPress={() => {
                            if (!tripIdParam) {
                                Alert.alert("Missing trip", "Trip ID is missing.");
                                return;
                            }
                            router.push(`/trips/${tripIdParam}/edit`);
                        }}
                        style={styles.iconButton}
                    >
                        <Ionicons name="create-outline" size={23} color={colors.text} />
                    </Pressable>
                </View>

                <View style={styles.heroCard}>
                    <View style={styles.heroIcon}>
                        <Ionicons name="map" size={28} color="#FFFFFF"/>
                    </View>

                    <Text style={styles.destination}>
                        {trip.destination ?? "No destinations"}
                    </Text>

                    <Text style={styles.tripName}>
                        {trip.tripName ?? "Untitled trip"}
                    </Text>
                </View>

                <View style={styles.infoGrid}>
                    <View style={styles.infoCard}>
                        <View style={styles.infoIcon}>
                            <Ionicons name="calendar-outline" size={20} color={colors.primary}/>
                        </View>
                        <Text style={styles.infoLabel}>Start</Text>
                        <Text style={styles.infoValue}>{formatDateTime(trip.startDate)}</Text>
                    </View>

                    <View style={styles.infoCard}>
                        <View style={styles.infoIcon}>
                            <Ionicons name="flag-outline" size={20} color={colors.primary}/>
                        </View>
                        <Text style={styles.infoLabel}>End</Text>
                        <Text style={styles.infoValue}>{formatDateTime(trip.endDate)}</Text>
                    </View>
                </View>

                <View style={styles.sectionHeader}>
                    <View>
                        <Text style={styles.sectionTitle}>Destinations</Text>
                        <Text style={styles.sectionSubtitle}>
                            Add each city or place for this trip
                        </Text>
                    </View>

                    <Pressable onPress={handleAddDestination} style={styles.smallAddButton}>
                        <Ionicons name="add" size={22} color="#FFFFFF"/>
                    </Pressable>
                </View>

                {destinations.length === 0 ? (
                    <View style={styles.emptyActivityCard}>
                        <View style={styles.emptyActivityIcon}>
                            <Ionicons name="location-outline" size={34} color={colors.primary}/>
                        </View>

                        <Text style={styles.emptyActivityTitle}>No destinations yet</Text>
                        <Text style={styles.emptyActivityText}>
                            Add cities or places first. Activities will be added inside each destination.
                        </Text>

                        <Pressable onPress={handleAddDestination} style={styles.activityButton}>
                            <Text style={styles.activityButtonText}>Add first destination</Text>
                        </Pressable>
                    </View>
                ) : (
                    <View style={styles.destinationList}>
                        {destinations.map((destination) => (
                            <Pressable
                                key={destination.destinationId}
                                style={styles.destinationCard}
                                onPress={() => handleOpenDestination(destination.destinationId)}
                            >
                                <View style={styles.destinationIcon}>
                                    <Ionicons name="location" size={22} color={colors.primary}/>
                                </View>

                                <View style={styles.destinationContent}>
                                    <Text style={styles.destinationCardTitle}>
                                        {destination.destinationName}
                                    </Text>

                                    <Text style={styles.destinationCardDate}>
                                        {formatDateTime(destination.startDate)} - {formatDateTime(destination.endDate)}
                                    </Text>

                                    {destination.notes ? (
                                        <Text style={styles.destinationCardNotes} numberOfLines={2}>
                                            {destination.notes}
                                        </Text>
                                    ) : null}
                                </View>

                                <Ionicons name="chevron-forward" size={22} color={colors.mutedText}/>
                            </Pressable>
                        ))}
                    </View>
                )
                }
                <Pressable
                    onPress={handleDeleteTrip}
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
                            <Text style={styles.deleteButtonText}>Delete Trip</Text>
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
    destination: {
        color: "#DBEAFE",
        fontSize: 15,
        fontWeight: "800",
        marginBottom: spacing.sm,
    },
    tripName: {
        color: "#FFFFFF",
        fontSize: 30,
        lineHeight: 36,
        fontWeight: "900",
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
    emptyActivityCard: {
        backgroundColor: colors.card,
        borderRadius: radius.xl,
        padding: spacing.xl,
        alignItems: "center",
        ...shadow.card,
    },
    emptyActivityIcon: {
        width: 72,
        height: 72,
        borderRadius: 24,
        backgroundColor: colors.softBlue,
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.md,
    },
    emptyActivityTitle: {
        fontSize: 21,
        fontWeight: "900",
        color: colors.text,
        marginBottom: spacing.sm,
    },
    emptyActivityText: {
        textAlign: "center",
        color: colors.mutedText,
        lineHeight: 21,
        marginBottom: spacing.lg,
    },
    activityButton: {
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
    destinationList: {
        gap: spacing.md,
    },
    destinationCard: {
        backgroundColor: colors.card,
        borderRadius: radius.lg,
        padding: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
        ...shadow.card,
    },
    destinationIcon: {
        width: 44,
        height: 44,
        borderRadius: 16,
        backgroundColor: colors.softBlue,
        alignItems: "center",
        justifyContent: "center",
    },
    destinationContent: {
        flex: 1,
    },
    destinationCardTitle: {
        fontSize: 16,
        fontWeight: "900",
        color: colors.text,
        marginBottom: 4,
    },
    destinationCardDate: {
        fontSize: 12,
        fontWeight: "700",
        color: colors.mutedText,
    },
    destinationCardNotes: {
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