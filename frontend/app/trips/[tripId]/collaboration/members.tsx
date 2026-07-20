import { useCallback, useState } from "react";
import { Alert, Pressable, RefreshControl, ScrollView, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";

import { getTripById } from "@/src/api/tripApi";
import { getTripMembers, removeTripMember, updateTripMemberRole } from "@/src/api/tripCollaborationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { TripCollaborationRole, TripMember } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";

// Only OWNER can assign/remove members. OWNER itself cannot be assigned from this screen.
type EditableRole = Exclude<TripCollaborationRole, "OWNER">;
const EDITABLE_ROLES: EditableRole[] = ["VIEWER", "EDITOR"];

function getMemberUsername(member: TripMember) {
    return member.username || "Unknown user";
}

export default function TripMembersListScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const theme = useAppTheme();
    const themeColors = theme.colors;
    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripId);

    const [members, setMembers] = useState<TripMember[]>([]);
    const [currentUserRole, setCurrentUserRole] = useState<TripCollaborationRole | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [updatingMemberId, setUpdatingMemberId] = useState<number | null>(null);
    const [error, setError] = useState<string | null>(null);

    const canManageMembers = currentUserRole === "OWNER";

    const loadMembers = useCallback(async () => {
        if (!hasValidTripId) {
            setError("Trip ID is missing or invalid.");
            setIsLoading(false);
            setIsRefreshing(false);
            return;
        }

        try {
            setError(null);

            const [trip, data] = await Promise.all([
                getTripById(tripId),
                getTripMembers(tripId),
            ]);

            setCurrentUserRole(trip.currentUserRole ?? null);
            setMembers(Array.isArray(data) ? data : []);
        } catch (error: unknown) {
            setError(getApiErrorMessage(error, "Failed to load trip members."));
            setCurrentUserRole(null);
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
        }
    }, [hasValidTripId, tripId]);

    useFocusEffect(
        useCallback(() => {
            setIsLoading(true);
            void loadMembers();
        }, [loadMembers])
    );

    async function performRefresh() {
        setIsRefreshing(true);
        await loadMembers();
    }

    function handleRefresh() {
        void performRefresh();
    }

    async function handleRoleChange(member: TripMember, role: EditableRole) {
        if (!canManageMembers || !hasValidTripId || member.role === role || member.role === "OWNER") {
            return;
        }

        try {
            setUpdatingMemberId(member.tripMemberId);
            await updateTripMemberRole(tripId, member.tripMemberId, {role});
            await loadMembers();
        } catch (error: unknown) {
            Alert.alert("Update role failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setUpdatingMemberId(null);
        }
    }

    async function handleConfirmRemoveMember(member: TripMember) {
        try {
            setUpdatingMemberId(member.tripMemberId);
            await removeTripMember(tripId, member.tripMemberId);
            await loadMembers();
        } catch (error: unknown) {
            Alert.alert("Remove failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setUpdatingMemberId(null);
        }
    }

    function handleRemoveMember(member: TripMember) {
        if (!canManageMembers || !hasValidTripId || member.role === "OWNER") {
            return;
        }

        const username = getMemberUsername(member);
        Alert.alert("Remove member", `Remove ${username} from this trip?`, [
            { text: "Cancel", style: "cancel" },
            {
                text: "Remove",
                style: "destructive",
                onPress: () => {
                    void handleConfirmRemoveMember(member);
                },
            },
        ]);
    }

    if (isLoading) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState title="Loading members..." subtitle="Getting trip collaborators." fullScreen />
            </AppScreen>
        );
    }

    return (
        <AppScreen scroll={false} contentContainerStyle={styles.screenContent}>
            <ScrollView
                contentContainerStyle={styles.scrollContent}
                refreshControl={<RefreshControl refreshing={isRefreshing} onRefresh={handleRefresh} tintColor={themeColors.primary} />}
                showsVerticalScrollIndicator={false}
            >
                <View style={styles.header}>
                    <HeaderButton onPress={() => router.back()} />
                    <View style={styles.headerTextGroup}>
                        <Text style={[styles.eyebrow, { color: themeColors.primary }]}>Members</Text>
                        <Text style={[styles.title, { color: themeColors.text }]}>Trip members</Text>
                        <Text style={[styles.subtitle, { color: themeColors.textMuted }]}>
                            {canManageMembers
                                ? "Update roles or remove members from this trip."
                                : "View who currently has access to this trip."}
                        </Text>
                    </View>
                </View>

                <ErrorMessage message={error} title="Could not load members" />

                {!canManageMembers ? (
                    <AppCard variant="soft" contentStyle={styles.noticeContent}>
                        <Ionicons name="lock-closed-outline" size={21} color={themeColors.primary} />
                        <Text style={[styles.noticeText, { color: themeColors.textMuted }]}>
                            Only the trip owner can change roles or remove members.
                        </Text>
                    </AppCard>
                ) : null}

                {members.length === 0 ? (
                    <EmptyState
                        title="No members found"
                        message="Members will appear here after invitations or join requests are accepted."
                        icon={<Ionicons name="people-outline" size={30} color={themeColors.primary} />}
                    />
                ) : (
                    <View style={styles.memberList}>
                        {members.map((member) => (
                            <MemberCard
                                key={member.tripMemberId}
                                member={member}
                                loading={updatingMemberId === member.tripMemberId}
                                canManageMembers={canManageMembers}
                                onChangeRole={(role) => {
                                    void handleRoleChange(member, role);
                                }}
                                onRemove={() => handleRemoveMember(member)}
                            />
                        ))}
                    </View>
                )}
            </ScrollView>
        </AppScreen>
    );
}

function HeaderButton({ onPress }: { onPress: () => void }) {
    const theme = useAppTheme();
    const themeColors = theme.colors;

    return (
        <AppCard onPress={onPress} style={styles.backButton} contentStyle={styles.backButtonContent}>
            <Ionicons name="chevron-back" size={22} color={themeColors.text} />
        </AppCard>
    );
}

type MemberCardProps = Readonly<{
    member: TripMember;
    loading: boolean;
    canManageMembers: boolean;
    onChangeRole: (role: EditableRole) => void;
    onRemove: () => void;
}>;

function MemberCard({ member, loading, canManageMembers, onChangeRole, onRemove }: MemberCardProps) {
    const username = getMemberUsername(member);
    const theme = useAppTheme();
    const themeColors = theme.colors;

    return (
        <AppCard contentStyle={styles.memberCardContent}>
            <View style={styles.memberTopRow}>
                <View style={[styles.avatar, { backgroundColor: themeColors.primarySoft }]}>
                    <Text style={[styles.avatarText, { color: themeColors.primary }]}>
                        {username.charAt(0).toUpperCase()}
                    </Text>
                </View>

                <View style={styles.memberTextGroup}>
                    <Text style={[styles.memberName, { color: themeColors.text }]}>{username}</Text>
                    {member.email ? (
                        <Text style={[styles.memberEmail, { color: themeColors.textMuted }]}>
                            {member.email}
                        </Text>
                    ) : null}
                </View>

                <RoleBadge role={member.role} />
            </View>

            {canManageMembers && member.role !== "OWNER" ? (
                <>
                    <View style={styles.roleButtonRow}>
                        {EDITABLE_ROLES.map((role) => {
                            const selected = member.role === role;

                            return (
                                <Pressable
                                    key={role}
                                    accessibilityRole="button"
                                    onPress={() => onChangeRole(role)}
                                    disabled={loading}
                                    style={({ pressed }) => [
                                        styles.smallRoleButton,
                                        {
                                            backgroundColor: selected ? themeColors.primarySoft : themeColors.surface,
                                            borderColor: selected ? themeColors.primary : themeColors.border,
                                        },
                                        pressed && styles.pressed,
                                        loading && styles.disabled,
                                    ]}
                                >
                                    <Text
                                        style={[
                                            styles.smallRoleButtonText,
                                            { color: selected ? themeColors.primary : themeColors.textMuted },
                                        ]}
                                    >
                                        {role === "VIEWER" ? "Viewer" : "Editor"}
                                    </Text>
                                </Pressable>
                            );
                        })}
                    </View>

                    <AppButton
                        title="Remove member"
                        onPress={onRemove}
                        loading={loading}
                        variant="danger"
                        leftIcon={<Ionicons name="trash-outline" size={18} color={themeColors.textLight} />}
                    />
                </>
            ) : null}
        </AppCard>
    );
}

const styles = StyleSheet.create({
    screenContent: { flex: 1 },
    scrollContent: { paddingTop: spacing.lg, paddingBottom: spacing.xxl, gap: spacing.lg },
    header: { gap: spacing.lg },
    backButton: { width: 46, height: 46, borderRadius: radius.lg },
    backButtonContent: { flex: 1, padding: 0, alignItems: "center", justifyContent: "center" },
    headerTextGroup: { gap: spacing.xs },
    eyebrow: { color: colors.primary, fontSize: typography.caption, fontWeight: fontWeight.bold, textTransform: "uppercase", letterSpacing: 0.7 },
    title: { color: colors.text, fontSize: typography.heading, fontWeight: fontWeight.bold },
    subtitle: { color: colors.textMuted, fontSize: typography.bodySmall, lineHeight: 21 },
    noticeContent: { flexDirection: "row", alignItems: "flex-start", gap: spacing.md },
    noticeText: { flex: 1, color: colors.textMuted, fontSize: typography.bodySmall, lineHeight: 20, fontWeight: fontWeight.semibold },
    memberList: { gap: spacing.md },
    memberCardContent: { gap: spacing.lg },
    memberTopRow: { flexDirection: "row", alignItems: "center", gap: spacing.md },
    avatar: { width: 46, height: 46, borderRadius: radius.pill, backgroundColor: colors.primarySoft, alignItems: "center", justifyContent: "center" },
    avatarText: { color: colors.primary, fontSize: typography.body, fontWeight: fontWeight.bold },
    memberTextGroup: { flex: 1, gap: spacing.xs },
    memberName: { color: colors.text, fontSize: typography.body, fontWeight: fontWeight.bold },
    memberEmail: { color: colors.textMuted, fontSize: typography.caption, lineHeight: 18 },
    roleButtonRow: { flexDirection: "row", gap: spacing.sm },
    smallRoleButton: { flex: 1, borderRadius: radius.md, borderWidth: 1, borderColor: colors.border, paddingVertical: spacing.sm, alignItems: "center", backgroundColor: colors.surface },
    smallRoleButtonText: { color: colors.textMuted, fontSize: typography.bodySmall, fontWeight: fontWeight.bold },
    pressed: { opacity: 0.86, transform: [{ scale: 0.99 }] },
    disabled: { opacity: 0.6 },
});
