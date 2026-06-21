import { useCallback, useState } from "react";
import { Alert, Pressable, RefreshControl, ScrollView, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";

import { getTripMembers, removeTripMember, updateTripMemberRole } from "@/src/api/tripCollaborationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type { TripCollaborationRole, TripMember } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";

type EditableRole = Exclude<TripCollaborationRole, "OWNER">;
const EDITABLE_ROLES: EditableRole[] = ["VIEWER", "EDITOR"];

function getMemberUsername(member: TripMember) {
    const value = member as any;
    return value.username || value.memberUsername || value.userUsername || value.user?.username || "Unknown user";
}

export default function TripMembersListScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();
    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripId);

    const [members, setMembers] = useState<TripMember[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [updatingMemberId, setUpdatingMemberId] = useState<number | null>(null);
    const [error, setError] = useState<string | null>(null);

    async function loadMembers() {
        if (!hasValidTripId) {
            setError("Trip ID is missing or invalid.");
            setIsLoading(false);
            setIsRefreshing(false);
            return;
        }

        try {
            setError(null);
            const data = await getTripMembers(tripId);
            setMembers(Array.isArray(data) ? data : []);
        } catch (error: any) {
            setError(getApiErrorMessage(error, "Failed to load trip members."));
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
        }
    }

    useFocusEffect(
        useCallback(() => {
            setIsLoading(true);
            loadMembers();
        }, [tripIdParam])
    );

    async function handleRefresh() {
        setIsRefreshing(true);
        await loadMembers();
    }

    async function handleRoleChange(member: TripMember, role: EditableRole) {
        if (!hasValidTripId || member.role === role || member.role === "OWNER") return;

        try {
            setUpdatingMemberId(member.tripMemberId);
            await updateTripMemberRole(tripId, member.tripMemberId, { role });
            await loadMembers();
        } catch (error: any) {
            Alert.alert("Update role failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setUpdatingMemberId(null);
        }
    }

    function handleRemoveMember(member: TripMember) {
        if (!hasValidTripId || member.role === "OWNER") return;

        const username = getMemberUsername(member);
        Alert.alert("Remove member", `Remove ${username} from this trip?`, [
            { text: "Cancel", style: "cancel" },
            {
                text: "Remove",
                style: "destructive",
                onPress: async () => {
                    try {
                        setUpdatingMemberId(member.tripMemberId);
                        await removeTripMember(tripId, member.tripMemberId);
                        await loadMembers();
                    } catch (error: any) {
                        Alert.alert("Remove failed", getApiErrorMessage(error, "Please try again."));
                    } finally {
                        setUpdatingMemberId(null);
                    }
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
                refreshControl={<RefreshControl refreshing={isRefreshing} onRefresh={handleRefresh} tintColor={colors.primary} />}
                showsVerticalScrollIndicator={false}
            >
                <View style={styles.header}>
                    <HeaderButton onPress={() => router.back()} />
                    <View style={styles.headerTextGroup}>
                        <Text style={styles.eyebrow}>Members</Text>
                        <Text style={styles.title}>Trip members</Text>
                        <Text style={styles.subtitle}>Update roles or remove members from this trip.</Text>
                    </View>
                </View>

                <ErrorMessage message={error} title="Could not load members" />

                {members.length === 0 ? (
                    <EmptyState
                        title="No members found"
                        message="Members will appear here after invitations or join requests are accepted."
                        icon={<Ionicons name="people-outline" size={30} color={colors.primary} />}
                    />
                ) : (
                    <View style={styles.memberList}>
                        {members.map((member) => (
                            <MemberCard
                                key={member.tripMemberId}
                                member={member}
                                loading={updatingMemberId === member.tripMemberId}
                                onChangeRole={(role) => handleRoleChange(member, role)}
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
    return (
        <AppCard onPress={onPress} style={styles.backButton} contentStyle={styles.backButtonContent}>
            <Ionicons name="chevron-back" size={22} color={colors.text} />
        </AppCard>
    );
}

type MemberCardProps = Readonly<{
    member: TripMember;
    loading: boolean;
    onChangeRole: (role: EditableRole) => void;
    onRemove: () => void;
}>;

function MemberCard({ member, loading, onChangeRole, onRemove }: MemberCardProps) {
    const username = getMemberUsername(member);

    return (
        <AppCard contentStyle={styles.memberCardContent}>
            <View style={styles.memberTopRow}>
                <View style={styles.avatar}>
                    <Text style={styles.avatarText}>{username.charAt(0).toUpperCase()}</Text>
                </View>

                <View style={styles.memberTextGroup}>
                    <Text style={styles.memberName}>{username}</Text>
                    {member.email ? <Text style={styles.memberEmail}>{member.email}</Text> : null}
                </View>

                <RoleBadge role={member.role} />
            </View>

            {member.role !== "OWNER" ? (
                <>
                    <View style={styles.roleButtonRow}>
                        {EDITABLE_ROLES.map((role) => (
                            <Pressable
                                key={role}
                                accessibilityRole="button"
                                onPress={() => onChangeRole(role)}
                                disabled={loading}
                                style={({ pressed }) => [
                                    styles.smallRoleButton,
                                    member.role === role && styles.smallRoleButtonSelected,
                                    pressed && styles.pressed,
                                    loading && styles.disabled,
                                ]}
                            >
                                <Text style={[styles.smallRoleButtonText, member.role === role && styles.smallRoleButtonTextSelected]}>
                                    {role === "VIEWER" ? "Viewer" : "Editor"}
                                </Text>
                            </Pressable>
                        ))}
                    </View>

                    <AppButton
                        title="Remove member"
                        onPress={onRemove}
                        loading={loading}
                        variant="danger"
                        leftIcon={<Ionicons name="trash-outline" size={18} color={colors.textLight} />}
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
    smallRoleButtonSelected: { borderColor: colors.primary, backgroundColor: colors.primarySoft },
    smallRoleButtonText: { color: colors.textMuted, fontSize: typography.bodySmall, fontWeight: fontWeight.bold },
    smallRoleButtonTextSelected: { color: colors.primary },
    pressed: { opacity: 0.86, transform: [{ scale: 0.99 }] },
    disabled: { opacity: 0.6 },
});
