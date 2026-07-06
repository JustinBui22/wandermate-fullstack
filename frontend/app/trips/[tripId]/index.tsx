import React, {useCallback, useState} from "react";
import {
    Alert,
    ImageBackground,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import {Ionicons} from "@expo/vector-icons";
import {useFocusEffect, useLocalSearchParams, useRouter} from "expo-router";

import {getDestinationsByTrip} from "@/src/api/destinationApi";
import {getCollaborationSummary} from "@/src/api/tripCollaborationApi";
import {deleteTrip, getTripById} from "@/src/api/tripApi";
import {UserAttribution} from "@/src/components/collaboration/UserAttribution";
import {AppButton} from "@/src/components/ui/AppButton";
import {AppCard} from "@/src/components/ui/AppCard";
import {AppScreen} from "@/src/components/ui/AppScreen";
import {EmptyState} from "@/src/components/ui/EmptyState";
import {ErrorMessage} from "@/src/components/ui/ErrorMessage";
import {LoadingState} from "@/src/components/ui/LoadingState";
import {NotificationBadge} from "@/src/components/ui/NotificationBadge";
import {colors, fontWeight, radius, spacing, typography} from "@/src/constants/theme";
import type {Destination} from "@/src/types/destination";
import type {Trip} from "@/src/types/trip";
import {getApiErrorMessage} from "@/src/utils/apiWarningUtils";
import {formatDateTime} from "@/src/utils/dateFormat";
import {normalizeImageUrl} from "@/src/utils/imageUrlUtils";
import {useAppTheme} from "@/src/hooks/useAppTheme";

function getApiMessage(error: any) {
    const data = error.response?.data;


    if (typeof data?.body === "string" && data.body.trim()) {
        return data.body;
    }

    return data?.message || error.message || "Failed to load trip detail.";


}

export default function TripDetailScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();
    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;

    const [trip, setTrip] = useState<Trip | null>(null);
    const [destinations, setDestinations] = useState<Destination[]>([]);
    const [coverImageFailed, setCoverImageFailed] = useState(false);
    const [tripCollaborationBadgeCount, setTripCollaborationBadgeCount] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const tripNumberId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripNumberId);

    const loadTripDetail = useCallback(async () => {
        if (!hasValidTripId) {
            setError("Trip ID is missing or invalid.");
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

            setTrip(tripData);
            setCoverImageFailed(false);
            setDestinations(Array.isArray(destinationData) ? destinationData : []);
        } catch (error: any) {
            setError(getApiMessage(error));
        } finally {
            setIsLoading(false);
        }
    }, [hasValidTripId, tripNumberId]);

    const loadTripCollaborationBadge = useCallback(async () => {
        if (!hasValidTripId) {
            setTripCollaborationBadgeCount(0);
            return;
        }

        try {
            const summary = await getCollaborationSummary();
            const count =
                summary.tripPendingJoinRequestCounts?.[String(tripNumberId)] ?? 0;

            setTripCollaborationBadgeCount(Math.max(0, count));
        } catch {
            setTripCollaborationBadgeCount(0);
        }
    }, [hasValidTripId, tripNumberId]);

    useFocusEffect(
        useCallback(() => {
            void loadTripDetail();
            void loadTripCollaborationBadge();
        }, [loadTripDetail, loadTripCollaborationBadge])
    );

    function handleRetry() {
        void loadTripDetail();
        void loadTripCollaborationBadge();
    }

    function handleAddDestination() {
        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
            return;
        }

        router.push(`/trips/${tripNumberId}/destinations/create` as any);
    }

    function handleOpenDestination(destinationId: number) {
        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
            return;
        }

        router.push(`/trips/${tripNumberId}/destinations/${destinationId}` as any);
    }

    function handleEditTrip() {
        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
            return;
        }

        router.push(`/trips/${tripNumberId}/edit` as any);
    }

    function handleOpenCollaboration() {
        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
            return;
        }

        router.push(`/trips/${tripNumberId}/collaboration` as any);
    }

    function handleDeleteTrip() {
        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
            return;
        }

        Alert.alert(
            "Delete trip",
            "This will delete this trip, all destinations, and all activities inside it. Are you sure?",
            [
                {text: "Cancel", style: "cancel"},
                {
                    text: "Delete",
                    style: "destructive",
                    onPress: async () => {
                        try {
                            setIsDeleting(true);
                            await deleteTrip(tripNumberId);

                            Alert.alert("Trip deleted", "Trip has been deleted.");
                            router.back();
                        } catch (error: any) {
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
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Loading trip..."
                    subtitle="Getting your destinations and trip details."
                    fullScreen
                />
            </AppScreen>
        );
    }

    if (error || !trip) {
        return (
            <AppScreen scroll={false} centerContent contentContainerStyle={styles.centerContent}>
                <View style={styles.errorIconBadge}>
                    <Ionicons name="alert-circle-outline" size={34} color={colors.danger}/>
                </View>

                <View style={styles.centerTextGroup}>
                    <Text style={styles.centerTitle}>Unable to load trip</Text>
                    <Text style={styles.centerSubtitle}>{error ?? "Trip not found."}</Text>
                </View>

                <AppButton title="Try again" onPress={handleRetry}/>
                <AppButton title="Go back" onPress={() => router.back()} variant="ghost"/>
            </AppScreen>
        );
    }

    const coverImageUrl = normalizeImageUrl(trip.coverImageUrl);
    const shouldShowCoverImage = Boolean(coverImageUrl) && !coverImageFailed;

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <HeaderIconButton
                    icon="chevron-back"
                    accessibilityLabel="Go back"
                    onPress={() => router.back()}
                />

                <View style={styles.headerActions}>
                    <HeaderIconButton
                        icon="people-outline"
                        accessibilityLabel="Open collaboration"
                        onPress={handleOpenCollaboration}
                        badgeCount={tripCollaborationBadgeCount}
                    />

                    <HeaderIconButton
                        icon="create-outline"
                        accessibilityLabel="Edit trip"
                        onPress={handleEditTrip}
                    />
                </View>
            </View>

            <AppCard style={styles.heroCard} contentStyle={styles.heroCardContent}>
                {shouldShowCoverImage ? (
                    <ImageBackground
                        source={{ uri: coverImageUrl as string }}
                        style={styles.heroCoverImage}
                        imageStyle={styles.heroCoverImageInner}
                        onError={() => setCoverImageFailed(true)}
                    >
                        <View style={styles.heroImageOverlay}>
                            <HeroContent trip={trip} />
                        </View>
                    </ImageBackground>
                ) : (
                    <HeroContent trip={trip} />
                )}
            </AppCard>

            <View style={styles.infoGrid}>
                <InfoCard
                    icon="calendar-outline"
                    label="Start"
                    value={formatDateTime(trip.startDate)}
                />
                <InfoCard
                    icon="flag-outline"
                    label="End"
                    value={formatDateTime(trip.endDate)}
                />
            </View>

            <ErrorMessage message={error} title="Trip detail error"/>

            <View style={styles.sectionHeader}>
                <View style={styles.sectionTextGroup}>
                    <Text style={styles.sectionTitle}>Destinations</Text>
                    <Text style={styles.sectionSubtitle}>Add each city or place for this trip.</Text>
                </View>

                <AppButton
                    title=""
                    onPress={handleAddDestination}
                    fullWidth={false}
                    style={styles.addButton}
                    leftIcon={<Ionicons name="add" size={23} color={colors.textLight}/>}
                    testID="add-destination-button"
                />
            </View>

            {destinations.length === 0 ? (
                <EmptyState
                    title="No destinations yet"
                    message="Add cities or places first. Activities will be added inside each destination."
                    icon={<Ionicons name="location-outline" size={30} color={colors.primary}/>}
                    actionLabel="Add first destination"
                    onActionPress={handleAddDestination}
                />
            ) : (
                <View style={styles.destinationList}>
                    {destinations.map((destination) => (
                        <DestinationCard
                            key={destination.destinationId}
                            destination={destination}
                            onPress={() => handleOpenDestination(destination.destinationId)}
                        />
                    ))}
                </View>
            )}

            <AppButton
                title="Delete Trip"
                onPress={handleDeleteTrip}
                loading={isDeleting}
                variant="danger"
                leftIcon={<Ionicons name="trash-outline" size={20} color={colors.textLight}/>}
                style={styles.deleteButton}
            />
        </AppScreen>
    );


}

type HeroContentProps = Readonly<{
    trip: Trip;
}>;

function HeroContent({trip}: HeroContentProps) {
    return (
        <>
            <View style={styles.heroIconBadge}>
                <Ionicons name="map" size={28} color={colors.textLight}/>
            </View>

            <View style={styles.heroTextGroup}>
                <Text style={styles.destinationText} numberOfLines={1}>
                    {trip.destination || "No destination"}
                </Text>
                <Text style={styles.tripNameText}>{trip.tripName || "Untitled trip"}</Text>
            </View>
        </>
    );
}

type HeaderIconButtonProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    accessibilityLabel: string;
    onPress: () => void;
    badgeCount?: number;
}>;

function HeaderIconButton({
                              icon,
                              accessibilityLabel,
                              onPress,
                              badgeCount = 0,
                          }: HeaderIconButtonProps) {
    return (
        <Pressable
            accessibilityRole="button"
            accessibilityLabel={accessibilityLabel}
            onPress={onPress}
            style={({pressed}) => [styles.headerIconButton, pressed && styles.pressed]}
        >
            <View style={styles.headerIconContent}>
                <Ionicons name={icon} size={23} color={colors.text} />
                <NotificationBadge count={badgeCount} size="small" />
            </View>
        </Pressable>
    );
}

type InfoCardProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    label: string;
    value: string;
}>;

function InfoCard({icon, label, value}: InfoCardProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <AppCard variant="soft" contentStyle={styles.infoCardContent}>
            <View
                style={[
                    styles.infoIconBadge,
                    {backgroundColor: colors.primarySoft},
                ]}
            >
                <Ionicons
                    name={icon}
                    size={20}
                    color={colors.primary}
                />
            </View>

            <View style={styles.infoTextGroup}>
                <Text style={[styles.infoLabel, {color: colors.textMuted}]}>
                    {label}
                </Text>

                <Text style={[styles.infoValue, {color: colors.text}]}>
                    {value}
                </Text>
            </View>
        </AppCard>
    );
}

type DestinationCardProps = Readonly<{
    destination: Destination;
    onPress: () => void;
}>;

function DestinationCard({destination, onPress}: DestinationCardProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <AppCard onPress={onPress} contentStyle={styles.destinationCardContent}>
            <View style={[styles.destinationIconBadge, { backgroundColor: colors.primarySoft }]}>
                <Ionicons name="location" size={22} color={colors.primary}/>
            </View>

            <View style={styles.destinationContent}>
                <Text style={[styles.destinationTitle, { color: colors.text }]} numberOfLines={1}>
                    {destination.destinationName || "Untitled destination"}
                </Text>

                <Text style={[styles.destinationDate, { color: colors.textMuted }]}>
                    {formatDateTime(destination.startDate)} → {formatDateTime(destination.endDate)}
                </Text>

                {destination.notes ? (
                    <Text style={[styles.destinationNotes, { color: colors.textMuted }]} numberOfLines={2}>
                        {destination.notes}
                    </Text>
                ) : null}
            </View>

            <UserAttribution
                itemLabel="destination"
                createdBy={{
                    userId: destination.createdByUserId,
                    username: destination.createdByUsername,
                    displayName: destination.createdByDisplayName,
                    profileImageUrl: destination.createdByProfileImageUrl,
                }}
                modifiedBy={{
                    userId: destination.modifiedByUserId,
                    username: destination.modifiedByUsername,
                    displayName: destination.modifiedByDisplayName,
                    profileImageUrl: destination.modifiedByProfileImageUrl,
                }}
            />

            <Ionicons name="chevron-forward" size={22} color={colors.textMuted}/>
        </AppCard>
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
        color: colors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
        textAlign: "center",
    },
    centerSubtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 21,
        textAlign: "center",
    },
    errorIconBadge: {
        width: 72,
        height: 72,
        borderRadius: radius.xl,
        backgroundColor: colors.dangerSoft,
        alignItems: "center",
        justifyContent: "center",
    },
    header: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.md,
    },
    headerActions: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
    },
    headerIconButton: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        backgroundColor: colors.surface,
        borderWidth: 1,
        borderColor: colors.border,
        alignItems: "center",
        justifyContent: "center",
        overflow: "visible",
    },
    headerIconContent: {
        position: "relative",
        alignItems: "center",
        justifyContent: "center",
    },
    pressed: {
        opacity: 0.86,
        transform: [{scale: 0.99}],
    },
    heroCard: {
        backgroundColor: colors.primary,
    },
    heroCardContent: {
        padding: spacing.xl,
        gap: spacing.lg,
    },
    heroCoverImage: {
        minHeight: 210,
        margin: -spacing.xl,
        justifyContent: "flex-end",
    },
    heroCoverImageInner: {
        borderRadius: radius.xl,
    },
    heroImageOverlay: {
        minHeight: 210,
        justifyContent: "flex-end",
        gap: spacing.lg,
        padding: spacing.xl,
        backgroundColor: "rgba(15, 23, 42, 0.42)",
        borderRadius: radius.xl,
    },
    heroIconBadge: {
        width: 58,
        height: 58,
        borderRadius: radius.xl,
        backgroundColor: "rgba(255,255,255,0.18)",
        alignItems: "center",
        justifyContent: "center",
    },
    heroTextGroup: {
        gap: spacing.sm,
    },
    destinationText: {
        color: "#DBEAFE",
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    tripNameText: {
        color: colors.textLight,
        fontSize: typography.heading,
        lineHeight: 32,
        fontWeight: fontWeight.bold,
    },
    infoGrid: {
        gap: spacing.md,
    },
    infoCardContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    infoIconBadge: {
        width: 42,
        height: 42,
        borderRadius: radius.md,
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    infoTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    infoLabel: {
        color: colors.textMuted,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.4,
    },
    infoValue: {
        color: colors.text,
        fontSize: typography.bodySmall,
        lineHeight: 20,
        fontWeight: fontWeight.bold,
    },
    sectionHeader: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        gap: spacing.md,
    },
    sectionTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    sectionTitle: {
        color: colors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    sectionSubtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    addButton: {
        width: 48,
        height: 48,
        minHeight: 48,
        borderRadius: radius.lg,
        paddingHorizontal: 0,
    },
    destinationList: {
        gap: spacing.md,
    },
    destinationCardContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    destinationIconBadge: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    destinationContent: {
        flex: 1,
        gap: spacing.xs,
    },
    destinationTitle: {
        color: colors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    destinationDate: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    destinationNotes: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 19,
    },
    deleteButton: {
        marginTop: spacing.sm,
    },
});
