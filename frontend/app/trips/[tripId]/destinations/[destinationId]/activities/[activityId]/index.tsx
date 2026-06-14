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

import {
    deleteActivity,
    getActivityById,
} from "@/src/api/activityApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type { Activity } from "@/src/types/activity";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { formatDateTime } from "@/src/utils/dateFormat";

function getApiMessage(error: any) {
    const data = error.response?.data;

    if (typeof data?.body === "string" && data.body.trim()) {
        return data.body;
    }

    return data?.message || error.message || "Failed to load activity detail.";
}

export default function ActivityDetailScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const destinationIdParam = Array.isArray(params.destinationId)
        ? params.destinationId[0]
        : params.destinationId;
    const activityIdParam = Array.isArray(params.activityId)
        ? params.activityId[0]
        : params.activityId;

    const tripNumberId = Number(tripIdParam);
    const destinationNumberId = Number(destinationIdParam);
    const activityNumberId = Number(activityIdParam);
    const hasValidRouteIds = Boolean(tripIdParam)
        && Boolean(destinationIdParam)
        && Boolean(activityIdParam)
        && !Number.isNaN(tripNumberId)
        && !Number.isNaN(destinationNumberId)
        && !Number.isNaN(activityNumberId);

    const [activity, setActivity] = useState<Activity | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function loadActivityDetail() {
        if (!hasValidRouteIds) {
            setError("Trip ID, destination ID, or activity ID is missing or invalid.");
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
            setError(getApiMessage(error));
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
        if (!hasValidRouteIds) {
            Alert.alert("Missing activity", "Trip ID, destination ID, or activity ID is missing or invalid.");
            return;
        }

        router.push(
            `/trips/${tripNumberId}/destinations/${destinationNumberId}/activities/${activityNumberId}/edit` as any
        );
    }

    function handleDeleteActivity() {
        if (!hasValidRouteIds) {
            Alert.alert("Missing activity", "Trip ID, destination ID, or activity ID is missing or invalid.");
            return;
        }

        Alert.alert(
            "Delete activity",
            "Are you sure you want to delete this activity?",
            [
                { text: "Cancel", style: "cancel" },
                {
                    text: "Delete",
                    style: "destructive",
                    onPress: async () => {
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

                            Alert.alert(
                                "Delete activity failed",
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
                    title="Loading activity..."
                    subtitle="Getting this activity detail ready."
                    fullScreen
                />
            </AppScreen>
        );
    }

    if (error || !activity) {
        return (
            <AppScreen scroll={false} centerContent contentContainerStyle={styles.centerContent}>
                <View style={styles.errorIconBadge}>
                    <Ionicons name="alert-circle-outline" size={34} color={colors.danger} />
                </View>

                <View style={styles.centerTextGroup}>
                    <Text style={styles.centerTitle}>Unable to load activity</Text>
                    <Text style={styles.centerSubtitle}>{error ?? "Activity not found."}</Text>
                </View>

                <AppButton title="Try again" onPress={loadActivityDetail} />
                <AppButton title="Go back" onPress={() => router.back()} variant="ghost" />
            </AppScreen>
        );
    }

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <HeaderIconButton
                    icon="chevron-back"
                    accessibilityLabel="Go back"
                    onPress={() => router.back()}
                />

                <HeaderIconButton
                    icon="create-outline"
                    accessibilityLabel="Edit activity"
                    onPress={handleEditActivity}
                />
            </View>

            <AppCard style={styles.heroCard} contentStyle={styles.heroCardContent}>
                <View style={styles.heroIconBadge}>
                    <Ionicons name="walk" size={28} color={colors.textLight} />
                </View>

                <View style={styles.heroTextGroup}>
                    <Text style={styles.activityLabel}>Activity</Text>
                    <Text style={styles.activityName}>{activity.activityName || "Untitled activity"}</Text>

                    {activity.location ? (
                        <View style={styles.locationRow}>
                            <Ionicons name="location-outline" size={16} color="#DBEAFE" />
                            <Text style={styles.locationText}>{activity.location}</Text>
                        </View>
                    ) : (
                        <Text style={styles.locationMuted}>No location added.</Text>
                    )}
                </View>
            </AppCard>

            <View style={styles.infoGrid}>
                <InfoCard
                    icon="time-outline"
                    label="Start"
                    value={formatDateTime(activity.startDateTime)}
                />
                <InfoCard
                    icon="flag-outline"
                    label="End"
                    value={formatDateTime(activity.endDateTime)}
                />
            </View>

            <AppCard title="Description" contentStyle={styles.descriptionCardContent}>
                {activity.description ? (
                    <Text style={styles.descriptionText}>{activity.description}</Text>
                ) : (
                    <Text style={styles.descriptionMuted}>No description added yet.</Text>
                )}
            </AppCard>

            <ErrorMessage message={error} title="Activity detail error" />

            <AppButton
                title="Delete Activity"
                onPress={handleDeleteActivity}
                loading={isDeleting}
                variant="danger"
                leftIcon={<Ionicons name="trash-outline" size={20} color={colors.textLight} />}
                style={styles.deleteButton}
            />
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
    activityLabel: {
        color: "#DBEAFE",
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    activityName: {
        color: colors.textLight,
        fontSize: typography.heading,
        lineHeight: 32,
        fontWeight: fontWeight.bold,
    },
    locationRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.xs,
    },
    locationText: {
        flex: 1,
        color: "#E0F2FE",
        fontSize: typography.bodySmall,
        lineHeight: 21,
        fontWeight: fontWeight.semibold,
    },
    locationMuted: {
        color: "#BFDBFE",
        fontSize: typography.bodySmall,
        lineHeight: 21,
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
    descriptionCardContent: {
        gap: spacing.md,
    },
    descriptionText: {
        color: colors.text,
        fontSize: typography.bodySmall,
        lineHeight: 22,
        fontWeight: fontWeight.medium,
    },
    descriptionMuted: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 22,
        fontWeight: fontWeight.semibold,
    },
    deleteButton: {
        marginTop: spacing.sm,
    },
});