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
    acceptJoinRequest,
    getPendingJoinRequests,
    getTripMembers,
    rejectJoinRequest,
    removeTripMember,
    sendTripInvitation,
    updateTripMemberRole,
} from "@/src/api/tripCollaborationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type {
    TripCollaborationRequest,
    TripCollaborationRole,
    TripMember,
} from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";

type InvitableTripRole = "EDITOR" | "VIEWER";

const INVITABLE_ROLES: InvitableTripRole[] = ["EDITOR", "VIEWER"];

function getBodyArray<T>(response: T[] | { body?: T[] } | null | undefined): T[] {
    if (Array.isArray(response)) {
        return response;
    }

    if (Array.isArray(response?.body)) {
        return response.body;
    }

    return [];
}

function getApiMessage(error: any) {
    const data = error.response?.data;

    if (typeof data?.body === "string" && data.body.trim()) {
        return data.body;
    }

    return data?.message || error.message || "Failed to load members.";
}

function getMemberUsername(member: TripMember) {
    const value = member as any;

    return (
        value.username ||
        value.memberUsername ||
        value.userUsername ||
        value.user?.username ||
        "Unknown user"
    );
}

function getRequestUsername(request: TripCollaborationRequest) {
    const value = request as any;

    return (
        value.requesterUsername ||
        value.requester ||
        value.requesterUser?.username ||
        value.username ||
        "Unknown user"
    );
}

function getRequestRole(request: TripCollaborationRequest): TripCollaborationRole {
    const value = request as any;

    return value.requestedRole || "VIEWER";
}

export default function TripMembersScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripNumberId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripNumberId);

    const [members, setMembers] = useState<TripMember[]>([]);
    const [joinRequests, setJoinRequests] = useState<TripCollaborationRequest[]>([]);
    const [inviteUsername, setInviteUsername] = useState("");
    const [inviteRole, setInviteRole] = useState<InvitableTripRole>("VIEWER");

    const [isLoading, setIsLoading] = useState(true);
    const [isSendingInvite, setIsSendingInvite] = useState(false);
    const [isRefreshingAction, setIsRefreshingAction] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function loadMembersScreen() {
        if (!hasValidTripId) {
            setError("Trip ID is missing or invalid.");
            setIsLoading(false);
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            const [membersResponse, joinRequestsResponse] = await Promise.all([
                getTripMembers(tripNumberId),
                getPendingJoinRequests(tripNumberId),
            ]);

            setMembers(getBodyArray<TripMember>(membersResponse));
            setJoinRequests(getBodyArray<TripCollaborationRequest>(joinRequestsResponse));
        } catch (error: any) {
            setError(getApiMessage(error));
        } finally {
            setIsLoading(false);
        }
    }

    useFocusEffect(
        useCallback(() => {
            loadMembersScreen();
        }, [tripIdParam])
    );

    async function handleSendInvitation() {
        const username = inviteUsername.trim();

        if (!username) {
            Alert.alert("Missing username", "Enter the username you want to invite.");
            return;
        }

        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
            return;
        }

        try {
            setIsSendingInvite(true);

            await sendTripInvitation(tripNumberId, {
                username,
                role: inviteRole,
            });

            setInviteUsername("");
            setInviteRole("VIEWER");

            Alert.alert(
                "Invitation sent",
                `${username} has been invited as ${inviteRole.toLowerCase()}.`
            );

            await loadMembersScreen();
        } catch (error: any) {
            Alert.alert(
                "Invitation failed",
                getApiErrorMessage(error, "Please check the username and try again.")
            );
        } finally {
            setIsSendingInvite(false);
        }
    }

    async function handleAcceptJoinRequest(requestId: number) {
        try {
            setIsRefreshingAction(true);
            await acceptJoinRequest(requestId);
            await loadMembersScreen();
        } catch (error: any) {
            Alert.alert(
                "Accept request failed",
                getApiErrorMessage(error, "Please try again.")
            );
        } finally {
            setIsRefreshingAction(false);
        }
    }

    async function handleRejectJoinRequest(requestId: number) {
        try {
            setIsRefreshingAction(true);
            await rejectJoinRequest(requestId);
            await loadMembersScreen();
        } catch (error: any) {
            Alert.alert(
                "Reject request failed",
                getApiErrorMessage(error, "Please try again.")
            );
        } finally {
            setIsRefreshingAction(false);
        }
    }

    async function handleUpdateRole(member: TripMember, nextRole: InvitableTripRole) {
        if (member.role === "OWNER") {
            Alert.alert("Owner role locked", "The trip owner role cannot be changed.");
            return;
        }

        if (member.role === nextRole) {
            return;
        }

        try {
            setIsRefreshingAction(true);

            await updateTripMemberRole(tripNumberId, member.tripMemberId, {
                role: nextRole,
            });

            await loadMembersScreen();
        } catch (error: any) {
            Alert.alert(
                "Update role failed",
                getApiErrorMessage(error, "Please try again.")
            );
        } finally {
            setIsRefreshingAction(false);
        }
    }

    function handleRemoveMember(member: TripMember) {
        if (member.role === "OWNER") {
            Alert.alert("Cannot remove owner", "The trip owner cannot be removed.");
            return;
        }

        const username = getMemberUsername(member);

        Alert.alert(
            "Remove member",
            `Remove ${username} from this trip?`,
            [
                { text: "Cancel", style: "cancel" },
                {
                    text: "Remove",
                    style: "destructive",
                    onPress: async () => {
                        try {
                            setIsRefreshingAction(true);
                            await removeTripMember(tripNumberId, member.tripMemberId);
                            await loadMembersScreen();
                        } catch (error: any) {
                            Alert.alert(
                                "Remove member failed",
                                getApiErrorMessage(error, "Please try again.")
                            );
                        } finally {
                            setIsRefreshingAction(false);
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
                    title="Loading members..."
                    subtitle="Getting trip members and pending requests."
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

                <View style={styles.centerTextGroup}>
                    <Text style={styles.centerTitle}>Unable to load members</Text>
                    <Text style={styles.centerSubtitle}>{error}</Text>
                </View>

                <AppButton title="Try again" onPress={loadMembersScreen} />
                <AppButton title="Go back" onPress={() => router.back()} variant="ghost" />
            </AppScreen>
        );
    }

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <Pressable
                    accessibilityRole="button"
                    accessibilityLabel="Go back"
                    onPress={() => router.back()}
                    style={({ pressed }) => [
                        styles.headerIconButton,
                        pressed && styles.pressed,
                    ]}
                >
                    <Ionicons name="chevron-back" size={23} color={colors.text} />
                </Pressable>

                <View style={styles.headerTextGroup}>
                    <Text style={styles.headerTitle}>Trip Members</Text>
                    <Text style={styles.headerSubtitle}>
                        Invite people and manage trip access.
                    </Text>
                </View>
            </View>

            <AppCard
                title="Invite member"
                subtitle="Only the owner can invite users to this trip."
                contentStyle={styles.inviteCardContent}
            >
                <AppInput
                    label="Username"
                    value={inviteUsername}
                    onChangeText={setInviteUsername}
                    placeholder="Enter username"
                    autoCapitalize="none"
                    autoCorrect={false}
                    leftIcon={<Ionicons name="person-add-outline" size={20} color={colors.textMuted} />}
                />

                <View style={styles.rolePicker}>
                    {INVITABLE_ROLES.map((role) => {
                        const selected = inviteRole === role;

                        return (
                            <Pressable
                                key={role}
                                accessibilityRole="button"
                                onPress={() => setInviteRole(role)}
                                style={[
                                    styles.roleOption,
                                    selected && styles.roleOptionSelected,
                                ]}
                            >
                                <Text
                                    style={[
                                        styles.roleOptionText,
                                        selected && styles.roleOptionTextSelected,
                                    ]}
                                >
                                    {role === "EDITOR" ? "Editor" : "Viewer"}
                                </Text>
                            </Pressable>
                        );
                    })}
                </View>

                <AppButton
                    title="Send Invitation"
                    onPress={handleSendInvitation}
                    loading={isSendingInvite}
                    leftIcon={!isSendingInvite ? <Ionicons name="send-outline" size={20} color={colors.textLight} /> : null}
                    testID="send-trip-invitation-button"
                />
            </AppCard>

            <View style={styles.sectionHeader}>
                <View style={styles.sectionTextGroup}>
                    <Text style={styles.sectionTitle}>Pending join requests</Text>
                    <Text style={styles.sectionSubtitle}>
                        Accept or reject users asking to join this trip.
                    </Text>
                </View>
            </View>

            {joinRequests.length === 0 ? (
                <EmptyState
                    title="No pending requests"
                    message="Join requests will appear here when users ask to join this trip."
                    icon={<Ionicons name="mail-open-outline" size={30} color={colors.primary} />}
                />
            ) : (
                <View style={styles.list}>
                    {joinRequests.map((request) => (
                        <JoinRequestCard
                            key={request.requestId}
                            request={request}
                            disabled={isRefreshingAction}
                            onAccept={() => handleAcceptJoinRequest(request.requestId)}
                            onReject={() => handleRejectJoinRequest(request.requestId)}
                        />
                    ))}
                </View>
            )}

            <View style={styles.sectionHeader}>
                <View style={styles.sectionTextGroup}>
                    <Text style={styles.sectionTitle}>Members</Text>
                    <Text style={styles.sectionSubtitle}>
                        Owner can change editor/viewer roles or remove members.
                    </Text>
                </View>
            </View>

            {members.length === 0 ? (
                <EmptyState
                    title="No members found"
                    message="This trip has no member records yet."
                    icon={<Ionicons name="people-outline" size={30} color={colors.primary} />}
                />
            ) : (
                <View style={styles.list}>
                    {members.map((member) => (
                        <MemberCard
                            key={member.tripMemberId}
                            member={member}
                            disabled={isRefreshingAction}
                            onUpdateRole={(role) => handleUpdateRole(member, role)}
                            onRemove={() => handleRemoveMember(member)}
                        />
                    ))}
                </View>
            )}
        </AppScreen>
    );
}

type JoinRequestCardProps = Readonly<{
    request: TripCollaborationRequest;
    disabled: boolean;
    onAccept: () => void;
    onReject: () => void;
}>;

function JoinRequestCard({
                             request,
                             disabled,
                             onAccept,
                             onReject,
                         }: JoinRequestCardProps) {
    const username = getRequestUsername(request);
    const requestedRole = getRequestRole(request);

    return (
        <AppCard contentStyle={styles.requestCardContent}>
            <View style={styles.avatarBadge}>
                <Ionicons name="person-outline" size={22} color={colors.primary} />
            </View>

            <View style={styles.memberMainContent}>
                <Text style={styles.memberName}>{username}</Text>
                <Text style={styles.memberSubtitle}>
                    Wants to join as {requestedRole.toLowerCase()}.
                </Text>
                <RoleBadge role={requestedRole} />
            </View>

            <View style={styles.requestActions}>
                <Pressable
                    accessibilityRole="button"
                    disabled={disabled}
                    onPress={onReject}
                    style={({ pressed }) => [
                        styles.smallIconButton,
                        styles.rejectButton,
                        pressed && styles.pressed,
                        disabled && styles.disabledButton,
                    ]}
                >
                    <Ionicons name="close" size={20} color={colors.danger} />
                </Pressable>

                <Pressable
                    accessibilityRole="button"
                    disabled={disabled}
                    onPress={onAccept}
                    style={({ pressed }) => [
                        styles.smallIconButton,
                        styles.acceptButton,
                        pressed && styles.pressed,
                        disabled && styles.disabledButton,
                    ]}
                >
                    <Ionicons name="checkmark" size={20} color={colors.success} />
                </Pressable>
            </View>
        </AppCard>
    );
}

type MemberCardProps = Readonly<{
    member: TripMember;
    disabled: boolean;
    onUpdateRole: (role: InvitableTripRole) => void;
    onRemove: () => void;
}>;

function MemberCard({
                        member,
                        disabled,
                        onUpdateRole,
                        onRemove,
                    }: MemberCardProps) {
    const username = getMemberUsername(member);
    const isOwner = member.role === "OWNER";

    return (
        <AppCard contentStyle={styles.memberCardContent}>
            <View style={styles.avatarBadge}>
                <Ionicons name="person" size={22} color={colors.primary} />
            </View>

            <View style={styles.memberMainContent}>
                <Text style={styles.memberName}>{username}</Text>
                <RoleBadge role={member.role} />

                {!isOwner ? (
                    <View style={styles.memberRoleActions}>
                        {INVITABLE_ROLES.map((role) => {
                            const selected = member.role === role;

                            return (
                                <Pressable
                                    key={role}
                                    accessibilityRole="button"
                                    disabled={disabled}
                                    onPress={() => onUpdateRole(role)}
                                    style={[
                                        styles.memberRoleChip,
                                        selected && styles.memberRoleChipSelected,
                                        disabled && styles.disabledButton,
                                    ]}
                                >
                                    <Text
                                        style={[
                                            styles.memberRoleChipText,
                                            selected && styles.memberRoleChipTextSelected,
                                        ]}
                                    >
                                        {role === "EDITOR" ? "Editor" : "Viewer"}
                                    </Text>
                                </Pressable>
                            );
                        })}
                    </View>
                ) : (
                    <Text style={styles.ownerHint}>
                        Owner role cannot be changed.
                    </Text>
                )}
            </View>

            {!isOwner ? (
                <Pressable
                    accessibilityRole="button"
                    disabled={disabled}
                    onPress={onRemove}
                    style={({ pressed }) => [
                        styles.removeButton,
                        pressed && styles.pressed,
                        disabled && styles.disabledButton,
                    ]}
                >
                    <Ionicons name="trash-outline" size={20} color={colors.danger} />
                </Pressable>
            ) : null}
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
    headerTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    headerTitle: {
        color: colors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    headerSubtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    pressed: {
        opacity: 0.86,
        transform: [{ scale: 0.99 }],
    },
    inviteCardContent: {
        gap: spacing.md,
    },
    rolePicker: {
        flexDirection: "row",
        gap: spacing.sm,
    },
    roleOption: {
        flex: 1,
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor: colors.border,
        backgroundColor: colors.surface,
        paddingVertical: spacing.md,
        alignItems: "center",
    },
    roleOptionSelected: {
        borderColor: colors.primary,
        backgroundColor: colors.primarySoft,
    },
    roleOptionText: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    roleOptionTextSelected: {
        color: colors.primary,
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
    list: {
        gap: spacing.md,
    },
    requestCardContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    memberCardContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    avatarBadge: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    memberMainContent: {
        flex: 1,
        gap: spacing.xs,
    },
    memberName: {
        color: colors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    memberSubtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    requestActions: {
        flexDirection: "row",
        gap: spacing.sm,
    },
    smallIconButton: {
        width: 40,
        height: 40,
        borderRadius: radius.md,
        alignItems: "center",
        justifyContent: "center",
        borderWidth: 1,
    },
    rejectButton: {
        backgroundColor: colors.dangerSoft,
        borderColor: colors.dangerSoft,
    },
    acceptButton: {
        backgroundColor: colors.successSoft,
        borderColor: colors.successSoft,
    },
    removeButton: {
        width: 40,
        height: 40,
        borderRadius: radius.md,
        backgroundColor: colors.dangerSoft,
        alignItems: "center",
        justifyContent: "center",
    },
    memberRoleActions: {
        flexDirection: "row",
        gap: spacing.sm,
        marginTop: spacing.xs,
    },
    memberRoleChip: {
        borderRadius: radius.pill,
        borderWidth: 1,
        borderColor: colors.border,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.xs,
        backgroundColor: colors.surface,
    },
    memberRoleChipSelected: {
        borderColor: colors.primary,
        backgroundColor: colors.primarySoft,
    },
    memberRoleChipText: {
        color: colors.textMuted,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
    },
    memberRoleChipTextSelected: {
        color: colors.primary,
    },
    ownerHint: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
        marginTop: spacing.xs,
    },
    disabledButton: {
        opacity: 0.5,
    },
});