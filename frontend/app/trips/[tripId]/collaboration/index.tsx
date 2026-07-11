import { useCallback, useState } from "react";
import { StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";

import { getTripById } from "@/src/api/tripApi";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors as staticColors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { TripRole } from "@/src/types/trip";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";

export default function TripCollaborationMenuScreen() {
    const router = useRouter();
    const theme = useAppTheme();
    const colors = theme.colors;
    const params = useLocalSearchParams();
    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripId);

    const [currentUserRole, setCurrentUserRole] = useState<TripRole | null>(null);
    const [isLoadingRole, setIsLoadingRole] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useFocusEffect(
        useCallback(() => {
            async function loadRole() {
                if (!hasValidTripId) {
                    setError("Trip ID is missing or invalid.");
                    setIsLoadingRole(false);
                    return;
                }

                try {
                    setIsLoadingRole(true);
                    setError(null);

                    const trip = await getTripById(tripId);
                    setCurrentUserRole(trip.currentUserRole ?? null);
                } catch (error: any) {
                    setError(getApiErrorMessage(error, "Could not load your role for this trip."));
                    setCurrentUserRole(null);
                } finally {
                    setIsLoadingRole(false);
                }
            }

            void loadRole();
        }, [hasValidTripId, tripId])
    );

    function push(path: string) {
        if (!hasValidTripId) return;
        router.push(`/trips/${tripId}/collaboration/${path}` as any);
    }

    if (isLoadingRole) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Loading collaboration..."
                    subtitle="Checking your access for this trip."
                    fullScreen
                />
            </AppScreen>
        );
    }

    const isOwner = currentUserRole === "OWNER";

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <IconButton icon="chevron-back" onPress={() => router.back()} />

                <View style={styles.headerTextGroup}>
                    <Text style={[styles.eyebrow, { color: colors.primary }]}>Trip collaboration</Text>
                    <Text style={[styles.title, { color: colors.text }]}>Manage sharing</Text>
                    <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                        {isOwner
                            ? "Invite people, manage requests, and control who can access this trip."
                            : "View who has access to this trip. Only the owner can manage sharing settings."}
                    </Text>
                </View>
            </View>

            <ErrorMessage message={error} title="Could not load collaboration" />

            {!isOwner ? (
                <AppCard variant="soft" contentStyle={styles.noticeContent}>
                    <Ionicons name="lock-closed-outline" size={22} color={colors.primary} />
                    <Text style={[styles.noticeText, { color: colors.textMuted }]}>
                        You can view the member list, but invitation tools are hidden because you are not the trip owner.
                    </Text>
                </AppCard>
            ) : null}

            <View style={styles.optionList}>
                {isOwner ? (
                    <>
                        <CollaborationOption
                            icon="person-add-outline"
                            title="Invite member"
                            subtitle="Invite a user directly by username."
                            badge="Owner"
                            onPress={() => push("invite")}
                        />

                        <CollaborationOption
                            icon="link-outline"
                            title="Invite code / link"
                            subtitle="Generate a single-use code or deep link."
                            badge="Owner"
                            onPress={() => push("share-code")}
                        />

                        <CollaborationOption
                            icon="mail-unread-outline"
                            title="Pending join requests"
                            subtitle="Accept or reject users who requested access."
                            badge="Owner"
                            onPress={() => push("requests")}
                        />
                    </>
                ) : null}

                <CollaborationOption
                    icon="people-outline"
                    title="Members"
                    subtitle={
                        isOwner
                            ? "View members, update roles, or remove access."
                            : "View people who currently have access to this trip."
                    }
                    onPress={() => push("members")}
                />
            </View>
        </AppScreen>
    );
}

type IconButtonProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    onPress: () => void;
}>;

function IconButton({ icon, onPress }: IconButtonProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <AppCard onPress={onPress} style={styles.backButton} contentStyle={styles.backButtonContent}>
            <Ionicons name={icon} size={22} color={colors.text} />
        </AppCard>
    );
}

type CollaborationOptionProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    title: string;
    subtitle: string;
    badge?: string;
    onPress: () => void;
}>;

function CollaborationOption({ icon, title, subtitle, badge, onPress }: CollaborationOptionProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <AppCard onPress={onPress} contentStyle={styles.optionContent}>
            <View style={[styles.optionIconBadge, { backgroundColor: colors.primarySoft }]}>
                <Ionicons name={icon} size={23} color={colors.primary} />
            </View>

            <View style={styles.optionTextGroup}>
                <View style={styles.optionTitleRow}>
                    <Text style={[styles.optionTitle, { color: colors.text }]}>{title}</Text>
                    {badge ? (
                        <View style={[styles.badge, { backgroundColor: colors.primarySoft }]}>
                            <Text style={[styles.badgeText, { color: colors.primary }]}>{badge}</Text>
                        </View>
                    ) : null}
                </View>

                <Text style={[styles.optionSubtitle, { color: colors.textMuted }]}>{subtitle}</Text>
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
    header: {
        gap: spacing.lg,
    },
    backButton: {
        width: 46,
        height: 46,
        borderRadius: radius.lg,
    },
    backButtonContent: {
        flex: 1,
        padding: 0,
        alignItems: "center",
        justifyContent: "center",
    },
    headerTextGroup: {
        gap: spacing.xs,
    },
    eyebrow: {
        color: staticColors.primary,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.7,
    },
    title: {
        color: staticColors.text,
        fontSize: typography.hero,
        fontWeight: fontWeight.bold,
        lineHeight: 38,
    },
    subtitle: {
        color: staticColors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 21,
    },
    noticeContent: {
        flexDirection: "row",
        alignItems: "flex-start",
        gap: spacing.md,
    },
    noticeText: {
        flex: 1,
        color: staticColors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
        fontWeight: fontWeight.semibold,
    },
    optionList: {
        gap: spacing.md,
    },
    optionContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    optionIconBadge: {
        width: 48,
        height: 48,
        borderRadius: radius.lg,
        backgroundColor: staticColors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    optionTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    optionTitleRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
    },
    optionTitle: {
        color: staticColors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    optionSubtitle: {
        color: staticColors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    badge: {
        borderRadius: radius.pill,
        backgroundColor: staticColors.primarySoft,
        paddingHorizontal: spacing.sm,
        paddingVertical: spacing.xs,
    },
    badgeText: {
        color: staticColors.primary,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
    },
});