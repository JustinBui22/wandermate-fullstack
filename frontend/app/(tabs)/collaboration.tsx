import { useCallback, useState } from "react";
import {
    Alert,
    RefreshControl,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useRouter } from "expo-router";

import {
    acceptInvitation,
    getMyPendingInvitations,
    rejectInvitation,
    requestToJoinTrip,
} from "@/src/api/tripCollaborationApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type {
    TripCollaborationRequest,
    TripCollaborationRole,
} from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { formatDateTime } from "@/src/utils/dateFormat";

type JoinRole = Exclude<TripCollaborationRole, "OWNER">;
type InvitationAction = "ACCEPT" | "REJECT";

type LoadingInvitationAction = {
    requestId: number;
    action: InvitationAction;
} | null;

function getApiMessage(error: any) {
    const data = error.response?.data;

    if (typeof data?.body === "string" && data.body.trim()) {
        return data.body;
    }

    return data?.message || error.message || "Failed to load collaboration requests.";
}

export default function CollaborationScreen() {
    const router = useRouter();

    const [invitations, setInvitations] = useState<TripCollaborationRequest[]>([]);
    const [tripIdInput, setTripIdInput] = useState("");
    const [joinRole, setJoinRole] = useState<JoinRole>("VIEWER");

    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [isSubmittingJoinRequest, setIsSubmittingJoinRequest] = useState(false);
    const [loadingInvitationAction, setLoadingInvitationAction] =
        useState<LoadingInvitationAction>(null);
    const [error, setError] = useState<string | null>(null);

    async function loadInvitations() {
        try {
            setError(null);

            const data = await getMyPendingInvitations();
            setInvitations(Array.isArray(data) ? data : []);
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
            loadInvitations();
        }, [])
    );

    async function handleRefresh() {
        setIsRefreshing(true);
        await loadInvitations();
    }

    async function handleAcceptInvitation(invitation: TripCollaborationRequest) {
        try {
            setLoadingInvitationAction({
                requestId: invitation.requestId,
                action: "ACCEPT",
            });

            const response = await acceptInvitation(invitation.requestId);
            const warnings = response.overlapWarnings ?? [];

            await loadInvitations();

            if (warnings.length > 0) {
                Alert.alert(
                    "Invitation accepted",
                    `You joined ${invitation.tripName}. This trip overlaps with ${warnings.length} of your trip(s).`
                );
            } else {
                Alert.alert("Invitation accepted", `You joined ${invitation.tripName}.`);
            }

            router.push(`/trips/${invitation.tripId}` as any);
        } catch (error: any) {
            Alert.alert(
                "Accept invitation failed",
                getApiErrorMessage(error, "Please try again.")
            );
        } finally {
            setLoadingInvitationAction(null);
        }
    }

    function handleRejectInvitation(invitation: TripCollaborationRequest) {
        Alert.alert(
            "Reject invitation",
            `Reject invitation to ${invitation.tripName}?`,
            [
                { text: "Cancel", style: "cancel" },
                {
                    text: "Reject",
                    style: "destructive",
                    onPress: async () => {
                        try {
                            setLoadingInvitationAction({
                                requestId: invitation.requestId,
                                action: "REJECT",
                            });

                            await rejectInvitation(invitation.requestId);
                            await loadInvitations();

                            Alert.alert(
                                "Invitation rejected",
                                `You rejected the invitation to ${invitation.tripName}.`
                            );
                        } catch (error: any) {
                            Alert.alert(
                                "Reject invitation failed",
                                getApiErrorMessage(error, "Please try again.")
                            );
                        } finally {
                            setLoadingInvitationAction(null);
                        }
                    },
                },
            ]
        );
    }

    async function handleSendJoinRequest() {
        const trimmedTripId = tripIdInput.trim();
        const tripId = Number(trimmedTripId);

        if (!trimmedTripId || Number.isNaN(tripId) || tripId <= 0) {
            Alert.alert("Invalid trip ID", "Enter a valid trip ID shared by the trip owner.");
            return;
        }

        try {
            setIsSubmittingJoinRequest(true);

            await requestToJoinTrip(tripId, {
                role: joinRole,
            });

            setTripIdInput("");
            setJoinRole("VIEWER");

            Alert.alert(
                "Request sent",
                "The trip owner can now accept or reject your join request from their Members screen."
            );
        } catch (error: any) {
            Alert.alert(
                "Join request failed",
                getApiErrorMessage(error, "Please try again.")
            );
        } finally {
            setIsSubmittingJoinRequest(false);
        }
    }

    if (isLoading) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Loading collaboration..."
                    subtitle="Getting invitations and request tools ready."
                    fullScreen
                />
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
                    <View style={styles.headerIconBadge}>
                        <Ionicons name="people-outline" size={28} color={colors.primary} />
                    </View>

                    <View style={styles.headerTextGroup}>
                        <Text style={styles.eyebrow}>WanderMate</Text>
                        <Text style={styles.title}>Collaboration</Text>
                        <Text style={styles.subtitle}>
                            Manage invitations and request access to shared trips.
                        </Text>
                    </View>
                </View>

                <ErrorMessage message={error} title="Could not load invitations" />

                <AppCard
                    title="Join a trip"
                    subtitle="Enter the Trip ID shared by the owner and ask to join."
                    contentStyle={styles.formCardContent}
                >
                    <View style={styles.infoBox}>
                        <Ionicons
                            name="information-circle-outline"
                            size={22}
                            color={colors.primary}
                        />

                        <View style={styles.infoTextGroup}>
                            <Text style={styles.infoTitle}>How do I find the Trip ID?</Text>
                            <Text style={styles.infoText}>
                                For V3, the owner shares the Trip ID with you manually. Later this can become a share code or invite link.
                            </Text>
                        </View>
                    </View>

                    <AppInput
                        label="Trip ID"
                        value={tripIdInput}
                        onChangeText={setTripIdInput}
                        keyboardType="number-pad"
                        placeholder="Example: 12"
                        helperText="Ask the trip owner for this ID."
                    />

                    <View style={styles.roleToggleRow}>
                        <RoleOption
                            label="Viewer"
                            selected={joinRole === "VIEWER"}
                            onPress={() => setJoinRole("VIEWER")}
                        />

                        <RoleOption
                            label="Editor"
                            selected={joinRole === "EDITOR"}
                            onPress={() => setJoinRole("EDITOR")}
                        />
                    </View>

                    <AppButton
                        title="Request to Join"
                        onPress={handleSendJoinRequest}
                        loading={isSubmittingJoinRequest}
                        leftIcon={
                            !isSubmittingJoinRequest ? (
                                <Ionicons
                                    name="send-outline"
                                    size={19}
                                    color={colors.textLight}
                                />
                            ) : null
                        }
                    />
                </AppCard>

                <View style={styles.sectionHeader}>
                    <View style={styles.sectionTextGroup}>
                        <Text style={styles.sectionTitle}>Received invitations</Text>
                        <Text style={styles.sectionSubtitle}>
                            Accepting an invitation adds you to the shared trip.
                        </Text>
                    </View>
                </View>

                {invitations.length === 0 ? (
                    <EmptyState
                        title="No pending invitations"
                        message="Trip invitations sent to you will appear here."
                        icon={
                            <Ionicons
                                name="mail-open-outline"
                                size={30}
                                color={colors.primary}
                            />
                        }
                    />
                ) : (
                    <View style={styles.requestList}>
                        {invitations.map((invitation) => (
                            <InvitationCard
                                key={invitation.requestId}
                                invitation={invitation}
                                loadingInvitationAction={loadingInvitationAction}
                                onAccept={() => handleAcceptInvitation(invitation)}
                                onReject={() => handleRejectInvitation(invitation)}
                            />
                        ))}
                    </View>
                )}
            </ScrollView>
        </AppScreen>
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

type InvitationCardProps = Readonly<{
    invitation: TripCollaborationRequest;
    loadingInvitationAction: LoadingInvitationAction;
    onAccept: () => void;
    onReject: () => void;
}>;

function InvitationCard({
                            invitation,
                            loadingInvitationAction,
                            onAccept,
                            onReject,
                        }: InvitationCardProps) {
    const isAcceptLoading =
        loadingInvitationAction?.requestId === invitation.requestId &&
        loadingInvitationAction.action === "ACCEPT";

    const isRejectLoading =
        loadingInvitationAction?.requestId === invitation.requestId &&
        loadingInvitationAction.action === "REJECT";

    const isActionDisabled =
        loadingInvitationAction?.requestId === invitation.requestId;

    return (
        <AppCard contentStyle={styles.invitationCardContent}>
            <View style={styles.invitationTopRow}>
                <View style={styles.tripIconBadge}>
                    <Ionicons name="map-outline" size={22} color={colors.primary} />
                </View>

                <View style={styles.invitationTextGroup}>
                    <Text style={styles.invitationTitle}>
                        {invitation.tripName || "Untitled trip"}
                    </Text>

                    <Text style={styles.invitationMeta}>
                        Invited by {invitation.requesterUsername} as {invitation.requestedRole}
                    </Text>
                </View>
            </View>

            {invitation.tripStartDate || invitation.tripEndDate ? (
                <Text style={styles.dateText}>
                    {formatDateTime(invitation.tripStartDate)} → {formatDateTime(invitation.tripEndDate)}
                </Text>
            ) : null}

            <View style={styles.actionRow}>
                <AppButton
                    title="Reject"
                    onPress={onReject}
                    loading={isRejectLoading}
                    disabled={isActionDisabled}
                    variant="outline"
                    size="sm"
                    fullWidth={false}
                    style={styles.actionButton}
                />

                <AppButton
                    title="Accept"
                    onPress={onAccept}
                    loading={isAcceptLoading}
                    disabled={isActionDisabled}
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
        paddingTop: spacing.xl,
        paddingBottom: 120,
        gap: spacing.lg,
    },
    header: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    headerIconBadge: {
        width: 54,
        height: 54,
        borderRadius: radius.xl,
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
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
        fontSize: typography.heading,
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
    infoBox: {
        flexDirection: "row",
        alignItems: "flex-start",
        gap: spacing.md,
        borderRadius: radius.lg,
        backgroundColor: colors.primarySoft,
        padding: spacing.md,
    },
    infoTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    infoTitle: {
        color: colors.text,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    infoText: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
    },
    roleToggleRow: {
        flexDirection: "row",
        gap: spacing.sm,
    },
    roleOption: {
        flex: 1,
    },
    sectionHeader: {
        gap: spacing.xs,
    },
    sectionTextGroup: {
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
    requestList: {
        gap: spacing.md,
    },
    invitationCardContent: {
        gap: spacing.md,
    },
    invitationTopRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    tripIconBadge: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    invitationTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    invitationTitle: {
        color: colors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    invitationMeta: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    dateText: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    actionRow: {
        flexDirection: "row",
        gap: spacing.sm,
    },
    actionButton: {
        flex: 1,
    },
});