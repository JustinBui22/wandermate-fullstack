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

import { getActivitiesByDestination } from "@/src/api/activityApi";
import {
    deleteDestination,
    getDestinationById,
} from "@/src/api/destinationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type { Activity } from "@/src/types/activity";
import type { Destination } from "@/src/types/destination";
import type { TripCollaborationRole } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { formatDateTime } from "@/src/utils/dateFormat";
import { canEditTripPlan, getCurrentUserTripRole } from "@/src/utils/tripRoleUtils";

function getApiMessage(error: any) {
    const data = error.response?.data;

    if (typeof data?.body === "string" && data.body.trim()) {
        return data.body;
    }

    return data?.message || error.message || "Failed to load destination detail.";
}

export default function DestinationDetailScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const destinationIdParam = Array.isArray(params.destinationId)
        ? params.destinationId[0]
        : params.destinationId;

    const tripNumberId = Number(tripIdParam);
    const destinationNumberId = Number(destinationIdParam);
    const hasValidRouteIds = Boolean(tripIdParam)
        && Boolean(destinationIdParam)
        && !Number.isNaN(tripNumberId)
        && !Number.isNaN(destinationNumberId);

    const [destination, setDestination] = useState<Destination | null>(null);
    const [activities, setActivities] = useState<Activity[]>([]);
    const [currentRole, setCurrentRole] = useState<TripCollaborationRole | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function loadDestinationDetail() {
        if (!hasValidRouteIds) {
            setError("Trip ID or destination ID is missing or invalid.");
            setIsLoading(false);
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const [destinationData, activityData, roleData] = await Promise.all([
                getDestinationById(tripNumberId, destinationNumberId),
                getActivitiesByDestination(tripNumberId, destinationNumberId),
                getCurrentUserTripRole(tripNumberId),
            ]);

            setDestination(destinationData);
            setActivities(Array.isArray(activityData) ? activityData : []);
            setCurrentRole(roleData.role);
        } catch (error: any) {
            setError(getApiMessage(error));
        } finally {
            setIsLoading(false);
        }
    }

    useFocusEffect(
        useCallback(() => {
            void loadDestinationDetail();
        }, [tripIdParam, destinationIdParam])
    );

    function handleEditDestination() {
        if (!hasValidRouteIds) {
            Alert.alert("Missing destination", "Trip ID or destination ID is missing or invalid.");
            return;
        }

        router.push(`/trips/${tripNumberId}/destinations/${destinationNumberId}/edit` as any);
    }

    function handleAddActivity() {
        if (!hasValidRouteIds) {
            Alert.alert("Missing destination", "Trip ID or destination ID is missing or invalid.");
            return;
        }

        router.push(`/trips/${tripNumberId}/destinations/${destinationNumberId}/activities/create` as any);
    }

    function handleOpenActivity(activityId: number) {
        if (!hasValidRouteIds) {
            Alert.alert("Missing activity", "Trip ID or destination ID is missing or invalid.");
            return;
        }

        router.push(`/trips/${tripNumberId}/destinations/${destinationNumberId}/activities/${activityId}` as any);
    }

    function handleDeleteDestination() {
        if (!hasValidRouteIds) {
            Alert.alert("Missing destination", "Trip ID or destination ID is missing or invalid.");
            return;
        }

        Alert.alert(
            "Delete destination",
            "This will delete this destination and all activities inside it. Are you sure?",
            [
                { text: "Cancel", style: "cancel" },
                {
                    text: "Delete",
                    style: "destructive",
                    onPress: async () => {
                        try {
                            setIsDeleting(true);

                            await deleteDestination(tripNumberId, destinationNumberId);

                            Alert.alert("Destination deleted", "Destination has been deleted.");
                            router.back();
                        } catch (error: any) {

                            Alert.alert(
                                "Delete destination failed",
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
                    title="Loading destination..."
                    subtitle="Getting activities and destination details."
                    fullScreen
                />
            </AppScreen>
        );
    }

    if (error || !destination) {
        return (
            <AppScreen scroll={false} centerContent contentContainerStyle={styles.centerContent}>
                <View style={styles.errorIconBadge}>
                    <Ionicons name="alert-circle-outline" size={34} color={colors.danger} />
                </View>

                <View style={styles.centerTextGroup}>
                    <Text style={styles.centerTitle}>Unable to load destination</Text>
                    <Text style={styles.centerSubtitle}>{error ?? "Destination not found."}</Text>
                </View>

                <AppButton title="Try again" onPress={loadDestinationDetail} />
                <AppButton title="Go back" onPress={() => router.back()} variant="ghost" />
            </AppScreen>
        );
    }

    const canEditPlan = canEditTripPlan(currentRole);

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <HeaderIconButton
                    icon="chevron-back"
                    accessibilityLabel="Go back"
                    onPress={() => router.back()}
                />

                {canEditPlan ? (
                    <HeaderIconButton
                        icon="create-outline"
                        accessibilityLabel="Edit destination"
                        onPress={handleEditDestination}
                    />
                ) : null}
            </View>

            <AppCard style={styles.heroCard} contentStyle={styles.heroCardContent}>
                <View style={styles.heroIconBadge}>
                    <Ionicons name="location" size={28} color={colors.textLight} />
                </View>

                <View style={styles.heroTextGroup}>
                    <Text style={styles.destinationLabel}>Destination</Text>
                    <Text style={styles.destinationName}>{destination.destinationName || "Untitled destination"}</Text>
                    <RoleBadge role={currentRole} />

                    {destination.notes ? (
                        <Text style={styles.notes}>{destination.notes}</Text>
                    ) : (
                        <Text style={styles.notesMuted}>No notes added yet.</Text>
                    )}
                </View>
            </AppCard>

            <View style={styles.infoGrid}>
                <InfoCard
                    icon="calendar-outline"
                    label="Start"
                    value={formatDateTime(destination.startDate)}
                />
                <InfoCard
                    icon="flag-outline"
                    label="End"
                    value={formatDateTime(destination.endDate)}
                />
                <InfoCard
                    icon="swap-vertical-outline"
                    label="Order"
                    value={destination.destinationOrder?.toString() ?? "Not set"}
                />
            </View>

            <ErrorMessage message={error} title="Destination detail error" />

            <View style={styles.sectionHeader}>
                <View style={styles.sectionTextGroup}>
                    <Text style={styles.sectionTitle}>Activities</Text>
                    <Text style={styles.sectionSubtitle}>Add plans inside this destination.</Text>
                </View>

                {canEditPlan ? (
                    <AppButton
                        title=""
                        onPress={handleAddActivity}
                        fullWidth={false}
                        style={styles.addButton}
                        leftIcon={<Ionicons name="add" size={23} color={colors.textLight} />}
                        testID="add-activity-button"
                    />
                ) : null}
            </View>

            {activities.length === 0 ? (
                <EmptyState
                    title="No activities yet"
                    message="Add sightseeing, meals, transport, check-ins, or other plans for this destination."
                    icon={<Ionicons name="walk-outline" size={30} color={colors.primary} />}
                    actionLabel={canEditPlan ? "Add first activity" : undefined}
                    onActionPress={canEditPlan ? handleAddActivity : undefined}
                />
            ) : (
                <View style={styles.activityList}>
                    {activities.map((activity) => (
                        <ActivityCard
                            key={activity.activityId}
                            activity={activity}
                            onPress={() => handleOpenActivity(activity.activityId)}
                        />
                    ))}
                </View>
            )}

            {canEditPlan ? (
                <AppButton
                    title="Delete Destination"
                    onPress={handleDeleteDestination}
                    loading={isDeleting}
                    variant="danger"
                    leftIcon={<Ionicons name="trash-outline" size={20} color={colors.textLight} />}
                    style={styles.deleteButton}
                />
            ) : null}
        </AppScreen>
    );
}

type HeaderIconButtonProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    accessibilityLabel: string;
    onPress: () => void;
}>;

function HeaderIconButton({ icon, accessibilityLabel, onPress }: HeaderIconButtonProps) {
    return (
        <Pressable
            accessibilityRole="button"
            accessibilityLabel={accessibilityLabel}
            onPress={onPress}
            style={({ pressed }) => [styles.headerIconButton, pressed && styles.pressed]}
        >
            <Ionicons name={icon} size={23} color={colors.text} />
        </Pressable>
    );
}

type InfoCardProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    label: string;
    value: string;
}>;

function InfoCard({ icon, label, value }: InfoCardProps) {
    return (
        <AppCard variant="soft" contentStyle={styles.infoCardContent}>
            <View style={styles.infoIconBadge}>
                <Ionicons name={icon} size={20} color={colors.primary} />
            </View>
            <View style={styles.infoTextGroup}>
                <Text style={styles.infoLabel}>{label}</Text>
                <Text style={styles.infoValue}>{value}</Text>
            </View>
        </AppCard>
    );
}

type ActivityCardProps = Readonly<{
    activity: Activity;
    onPress: () => void;
}>;

function ActivityCard({ activity, onPress }: ActivityCardProps) {
    return (
        <AppCard onPress={onPress} contentStyle={styles.activityCardContent}>
            <View style={styles.activityIconBadge}>
                <Ionicons name="walk" size={22} color={colors.primary} />
            </View>

            <View style={styles.activityContent}>
                <Text style={styles.activityTitle} numberOfLines={1}>
                    {activity.activityName || "Untitled activity"}
                </Text>

                {activity.location ? (
                    <Text style={styles.activityLocation} numberOfLines={1}>
                        {activity.location}
                    </Text>
                ) : null}

                <Text style={styles.activityTime} numberOfLines={2}>
                    {formatDateTime(activity.startDateTime)} → {formatDateTime(activity.endDateTime)}
                </Text>

                {activity.description ? (
                    <Text style={styles.activityDescription} numberOfLines={2}>
                        {activity.description}
                    </Text>
                ) : null}
            </View>

            <Ionicons name="chevron-forward" size={22} color={colors.textMuted} />
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
    headerIconButton: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        backgroundColor: colors.surface,
        borderWidth: 1,
        borderColor: colors.border,
        alignItems: "center",
        justifyContent: "center",
    },
    pressed: {
        opacity: 0.86,
        transform: [{ scale: 0.99 }],
    },
    heroCard: {
        backgroundColor: colors.primary,
    },
    heroCardContent: {
        padding: spacing.xl,
        gap: spacing.lg,
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
    destinationLabel: {
        color: "#DBEAFE",
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    destinationName: {
        color: colors.textLight,
        fontSize: typography.heading,
        lineHeight: 32,
        fontWeight: fontWeight.bold,
    },
    notes: {
        color: "#E0F2FE",
        fontSize: typography.bodySmall,
        lineHeight: 22,
        fontWeight: fontWeight.semibold,
    },
    notesMuted: {
        color: "#BFDBFE",
        fontSize: typography.bodySmall,
        lineHeight: 22,
        fontWeight: fontWeight.semibold,
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
    activityList: {
        gap: spacing.md,
    },
    activityCardContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    activityIconBadge: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    activityContent: {
        flex: 1,
        gap: spacing.xs,
    },
    activityTitle: {
        color: colors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    activityLocation: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.bold,
    },
    activityTime: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    activityDescription: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 19,
    },
    deleteButton: {
        marginTop: spacing.sm,
    },
});