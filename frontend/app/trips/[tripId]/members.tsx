import { useCallback, useState } from "react";
import {
    Alert,
    Pressable,
    RefreshControl,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";

import {
    acceptJoinRequest,
    getPendingJoinRequests,
    getTripMembers,
    rejectJoinRequest,
    removeTripMember,
    sendTripInvitation,
    updateTripMemberRole,
} from "@/src/api/tripCollaborationApi";
import { getTripById } from "@/src/api/tripApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type { Trip } from "@/src/types/trip";
import type { TripCollaborationRequest, TripCollaborationRole, TripMember } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { getCurrentUsernameFromAccessToken } from "@/src/utils/authTokenUtils";
import { formatDateTime } from "@/src/utils/dateFormat";
import { canManageTripMembers, getRoleLabel } from "@/src/utils/tripRoleUtils";

function getApiMessage(error: any) {
    const data = error.response?.data;

    if (typeof data?.body === "string" && data.body.trim()) {
        return data.body;
    }

    return data?.message || error.message || "Failed to load trip members.";
}

export default function TripMembersScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();
    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripNumberId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripNumberId);

    const [trip, setTrip] = useState<Trip | null>(null);
    const [members, setMembers] = useState<TripMember[]>([]);
    const [joinRequests, setJoinRequests] = useState<TripCollaborationRequest[]>([]);
    const [currentUsername, setCurrentUsername] = useState<string | null>(null);
    const [currentRole, setCurrentRole] = useState<TripCollaborationRole | null>(null);
    const [inviteUsername, setInviteUsername] = useState("");
    const [inviteRole, setInviteRole] = useState<Exclude<TripCollaborationRole, "OWNER">>("VIEWER");
    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [isSendingInvitation, setIsSendingInvitation] = useState(false);
    const [activeMemberId, setActiveMemberId] = useState<number | null>(null);
    const [activeRequestId, setActiveRequestId] = useState<number | null>(null);
    const [error, setError] = useState<string | null>(null);

    const canManageMembers = canManageTripMembers(currentRole);

    async function loadMembers() {
        if (!hasValidTripId) {
            setError("Trip ID is missing or invalid.");
            setIsLoading(false);
            return;
        }

        try {
            setError(null);
            const username = await getCurrentUsernameFromAccessToken();
            const [tripData, memberData] = await Promise.all([
                getTripById(tripNumberId),
                getTripMembers(tripNumberId),
            ]);

            const currentMember = username
                ? memberData.find((member) => member.username === username)
                : null;

            setCurrentUsername(username);
            setCurrentRole(currentMember?.role ?? null);
            setTrip(tripData);
            setMembers(Array.isArray(memberData) ? memberData : []);

            if (currentMember?.role === "OWNER") {
                const requests = await getPendingJoinRequests(tripNumberId);
                setJoinRequests(Array.isArray(requests) ? requests : []);
            } else {
                setJoinRequests([]);
            }
        } catch (error: any) {
            setError(getApiMessage(error));
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

    async function handleSendInvitation() {
        if (!inviteUsername.trim()) {
            Alert.alert("Missing username", "Enter the username you want to invite.");
            return;
        }

        try {
            setIsSendingInvitation(true);
            await sendTripInvitation(tripNumberId, {
                username: inviteUsername.trim(),
                role: inviteRole,
            });
            setInviteUsername("");
            Alert.alert("Invitation sent", "The user must accept before they become a member.");
            await loadMembers();
        } catch (error: any) {
            Alert.alert("Send invitation failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setIsSendingInvitation(false);
        }
    }

    function handleChangeMemberRole(member: TripMember) {
        const nextRole: Exclude<TripCollaborationRole, "OWNER"> = member.role === "EDITOR" ? "VIEWER" : "EDITOR";

        Alert.alert(
            "Change role",
            `Change ${member.username} to ${getRoleLabel(nextRole)}?`,
            [
                { text: "Cancel", style: "cancel" },
                {
                    text: "Change",
                    onPress: async () => {
                        try {
                            setActiveMemberId(member.tripMemberId);
                            await updateTripMemberRole(tripNumberId, member.tripMemberId, { role: nextRole });
                            await loadMembers();
                        } catch (error: any) {
                            Alert.alert("Update role failed", getApiErrorMessage(error, "Please try again."));
                        } finally {
                            setActiveMemberId(null);
                        }
                    },
                },
            ]
        );
    }

    function handleRemoveMember(member: TripMember) {
        Alert.alert(
            "Remove member",
            `Remove ${member.username} from this trip?`,
            [
                { text: "Cancel", style: "cancel" },
                {
                    text: "Remove",
                    style: "destructive",
                    onPress: async () => {
                        try {
                            setActiveMemberId(member.tripMemberId);
                            await removeTripMember(tripNumberId, member.tripMemberId);
                            await loadMembers();
                        } catch (error: any) {
                            Alert.alert("Remove member failed", getApiErrorMessage(error, "Please try again."));
                        } finally {
                            setActiveMemberId(null);
                        }
                    },
                },
            ]
        );
    }

    async function handleAcceptJoinRequest(request: TripCollaborationRequest) {
        try {
            setActiveRequestId(request.requestId);
            await acceptJoinRequest(request.requestId);
            Alert.alert("Join request accepted", `${request.requesterUsername} is now a trip member.`);
            await loadMembers();
        } catch (error: any) {
            Alert.alert("Accept request failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setActiveRequestId(null);
        }
    }

    async function handleRejectJoinRequest(request: TripCollaborationRequest) {
        try {
            setActiveRequestId(request.requestId);
            await rejectJoinRequest(request.requestId);
            await loadMembers();
        } catch (error: any) {
            Alert.alert("Reject request failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setActiveRequestId(null);
        }
    }

    if (isLoading) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Loading members..."
                    subtitle="Getting collaboration details."
                    fullScreen
                />
            </AppScreen>
        );
    }

    if (error) {
        return (
            <AppScreen scroll={false} centerContent contentContainerStyle={styles.centerContent}>
                <View style={styles.errorIconBadge}>
                    <Ionicons name="alert-circle-outline" size={34} color={colors.danger} />
                </View>
                <Text style={styles.centerTitle}>Unable to load members</Text>
                <Text style={styles.centerSubtitle}>{error}</Text>
                <AppButton title="Try again" onPress={loadMembers} />
                <AppButton title="Go back" onPress={() => router.back()} variant="ghost" />
            </AppScreen>
        );
    }

    return (
        <AppScreen scroll={false} contentContainerStyle={styles.screenContent}>
            <ScrollView
                refreshControl={
                    <RefreshControl
                        refreshing={isRefreshing}
                        onRefresh={handleRefresh}
                        tintColor={colors.primary}
                        colors={[colors.primary]}
                    />
                }
                contentContainerStyle={styles.scrollContent}
                showsVerticalScrollIndicator={false}
            >
                <View style={styles.header}>
                    <HeaderIconButton
                        icon="chevron-back"
                        accessibilityLabel="Go back"
                        onPress={() => router.back()}
                    />
                    <View style={styles.headerTextGroup}>
                        <Text style={styles.eyebrow}>Collaboration</Text>
                        <Text style={styles.title}>{trip?.tripName || "Trip members"}</Text>
                        <Text style={styles.subtitle}>Your role: {getRoleLabel(currentRole)}</Text>
                    </View>
                </View>

                {canManageMembers ? (
                    <AppCard title="Invite member" contentStyle={styles.formCardContent}>
                        <AppInput
                            label="Username"
                            value={inviteUsername}
                            onChangeText={setInviteUsername}
                            placeholder="Enter username"
                            autoCapitalize="none"
                            helperText="User becomes a member only after accepting the invitation."
                        />
                        <View style={styles.roleToggleRow}>
                            <RoleOption
                                label="Viewer"
                                selected={inviteRole === "VIEWER"}
                                onPress={() => setInviteRole("VIEWER")}
                            />
                            <RoleOption
                                label="Editor"
                                selected={inviteRole === "EDITOR"}
                                onPress={() => setInviteRole("EDITOR")}
                            />
                        </View>
                        <AppButton
                            title="Send Invitation"
                            onPress={handleSendInvitation}
                            loading={isSendingInvitation}
                            leftIcon={<Ionicons name="mail-outline" size={19} color={colors.textLight} />}
                        />
                    </AppCard>
                ) : null}

                {canManageMembers ? (
                    <View style={styles.sectionGroup}>
                        <Text style={styles.sectionTitle}>Pending join requests</Text>
                        {joinRequests.length === 0 ? (
                            <EmptyState
                                title="No pending requests"
                                message="Requests to join this trip will appear here."
                                icon={<Ionicons name="person-add-outline" size={30} color={colors.primary} />}
                            />
                        ) : (
                            <View style={styles.listGroup}>
                                {joinRequests.map((request) => (
                                    <JoinRequestCard
                                        key={request.requestId}
                                        request={request}
                                        activeRequestId={activeRequestId}
                                        onAccept={() => handleAcceptJoinRequest(request)}
                                        onReject={() => handleRejectJoinRequest(request)}
                                    />
                                ))}
                            </View>
                        )}
                    </View>
                ) : null}

                <View style={styles.sectionGroup}>
                    <Text style={styles.sectionTitle}>Members</Text>
                    <View style={styles.listGroup}>
                        {members.map((member) => (
                            <MemberCard
                                key={member.tripMemberId}
                                member={member}
                                currentUsername={currentUsername}
                                canManageMembers={canManageMembers}
                                isLoading={activeMemberId === member.tripMemberId}
                                onChangeRole={() => handleChangeMemberRole(member)}
                                onRemove={() => handleRemoveMember(member)}
                            />
                        ))}
                    </View>
                </View>
            </ScrollView>
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

type RoleOptionProps = Readonly<{
    label: string;
    selected: boolean;
    onPress: () => void;
}>;

function RoleOption({ label, selected, onPress }: RoleOptionProps) {
    return (
        <AppButton
            title={label}
            onPress={onPress}
            variant={selected ? "primary" : "outline"}
            size="sm"
            fullWidth={false}
            style={styles.roleOption}
        />
    );
}

type MemberCardProps = Readonly<{
    member: TripMember;
    currentUsername: string | null;
    canManageMembers: boolean;
    isLoading: boolean;
    onChangeRole: () => void;
    onRemove: () => void;
}>;

function MemberCard({ member, currentUsername, canManageMembers, isLoading, onChangeRole, onRemove }: MemberCardProps) {
    const isCurrentUser = member.username === currentUsername;
    const canManageThisMember = canManageMembers && member.role !== "OWNER";

    return (
        <AppCard contentStyle={styles.memberCardContent}>
            <View style={styles.memberTopRow}>
                <View style={styles.memberAvatar}>
                    <Text style={styles.memberAvatarText}>{member.username?.charAt(0).toUpperCase() || "?"}</Text>
                </View>
                <View style={styles.memberTextGroup}>
                    <Text style={styles.memberUsername}>{member.username}{isCurrentUser ? " (You)" : ""}</Text>
                    {member.email ? <Text style={styles.memberEmail}>{member.email}</Text> : null}
                </View>
                <RoleBadge role={member.role} />
            </View>

            {canManageThisMember ? (
                <View style={styles.actionRow}>
                    <AppButton
                        title={`Make ${member.role === "EDITOR" ? "Viewer" : "Editor"}`}
                        onPress={onChangeRole}
                        loading={isLoading}
                        variant="outline"
                        size="sm"
                        fullWidth={false}
                        style={styles.actionButton}
                    />
                    <AppButton
                        title="Remove"
                        onPress={onRemove}
                        loading={isLoading}
                        variant="danger"
                        size="sm"
                        fullWidth={false}
                        style={styles.actionButton}
                    />
                </View>
            ) : null}
        </AppCard>
    );
}

type JoinRequestCardProps = Readonly<{
    request: TripCollaborationRequest;
    activeRequestId: number | null;
    onAccept: () => void;
    onReject: () => void;
}>;

function JoinRequestCard({ request, activeRequestId, onAccept, onReject }: JoinRequestCardProps) {
    const isLoading = activeRequestId === request.requestId;

    return (
        <AppCard contentStyle={styles.joinRequestContent}>
            <View style={styles.joinRequestTopRow}>
                <View style={styles.memberAvatar}>
                    <Text style={styles.memberAvatarText}>{request.requesterUsername?.charAt(0).toUpperCase() || "?"}</Text>
                </View>
                <View style={styles.memberTextGroup}>
                    <Text style={styles.memberUsername}>{request.requesterUsername}</Text>
                    <Text style={styles.memberEmail}>Requested {request.requestedRole}</Text>
                    {request.createdDate ? (
                        <Text style={styles.dateText}>{formatDateTime(request.createdDate)}</Text>
                    ) : null}
                </View>
            </View>
            <View style={styles.actionRow}>
                <AppButton
                    title="Reject"
                    onPress={onReject}
                    loading={isLoading}
                    disabled={isLoading}
                    variant="outline"
                    size="sm"
                    fullWidth={false}
                    style={styles.actionButton}
                />
                <AppButton
                    title="Accept"
                    onPress={onAccept}
                    loading={isLoading}
                    disabled={isLoading}
                    size="sm"
                    fullWidth={false}
                    style={styles.actionButton}
                />
            </View>
        </AppCard>
    );
}

const styles = StyleSheet.create({
    screenContent: {
        flex: 1,
    },
    scrollContent: {
        paddingTop: spacing.lg,
        paddingBottom: spacing.xxl,
        gap: spacing.lg,
    },
    centerContent: {
        gap: spacing.lg,
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
    headerTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    eyebrow: {
        color: colors.primary,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.7,
    },
    title: {
        color: colors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    subtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    formCardContent: {
        gap: spacing.md,
    },
    roleToggleRow: {
        flexDirection: "row",
        gap: spacing.sm,
    },
    roleOption: {
        flex: 1,
    },
    sectionGroup: {
        gap: spacing.md,
    },
    sectionTitle: {
        color: colors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    listGroup: {
        gap: spacing.md,
    },
    memberCardContent: {
        gap: spacing.md,
    },
    memberTopRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    memberAvatar: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    memberAvatarText: {
        color: colors.primaryDark,
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    memberTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    memberUsername: {
        color: colors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    memberEmail: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    dateText: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
    },
    actionRow: {
        flexDirection: "row",
        gap: spacing.sm,
    },
    actionButton: {
        flex: 1,
    },
    joinRequestContent: {
        gap: spacing.md,
    },
    joinRequestTopRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
});
