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
    acceptJoinRequest,
    getMyPendingInvitations,
    getMySentPendingJoinRequests,
    getPendingJoinRequestsForMyTrips,
    rejectInvitation,
    rejectJoinRequest,
} from "@/src/api/tripCollaborationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { TripCollaborationRequest } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { formatDateTime } from "@/src/utils/dateFormat";

type CollaborationAction =
    | "ACCEPT_INVITATION"
    | "REJECT_INVITATION"
    | "ACCEPT_JOIN_REQUEST"
    | "REJECT_JOIN_REQUEST";

type LoadingAction = {
    requestId: number;
    action: CollaborationAction;
} | null;

function formatRequestDateRange(request: TripCollaborationRequest) {
    const start = formatDateTime(request.tripStartDate);
    const end = formatDateTime(request.tripEndDate);

    if (start === "Not set" && end === "Not set") {
        return "Dates not set";
    }

    if (start !== "Not set" && end === "Not set") {
        return `Starts ${start}`;
    }

    if (start === "Not set" && end !== "Not set") {
        return `Ends ${end}`;
    }

    return `${start} → ${end}`;
}

function showTripPreview(
    title: string,
    request: TripCollaborationRequest,
    actorLabel: string
) {
    Alert.alert(
        title,
        [
            `Trip: ${request.tripName || "Shared trip"}`,
            `Destination: ${request.destination || "Not set"}`,
            `Dates: ${formatRequestDateRange(request)}`,
            `${actorLabel}`,
            `Role: ${request.requestedRole}`,
            `Status: ${request.status}`,
        ].join("\n")
    );
}

function isRequestPending(request: TripCollaborationRequest) {
    return request.status === "PENDING";
}

function isAlreadyHandledError(error: any) {
    const data = error?.response?.data;
    const code = data?.code;
    const message = String(data?.message || data?.body || error?.message || "").toLowerCase();

    return (
        code === "E073" ||
        message.includes("request not found") ||
        message.includes("collaboration request not found") ||
        message.includes("already accepted") ||
        message.includes("already rejected") ||
        message.includes("already declined") ||
        message.includes("already handled")
    );
}

function showAlreadyHandledAlert(onRefresh?: () => void) {
    Alert.alert(
        "Request already handled",
        "This request has already been accepted or declined. The list will refresh now.",
        [
            {
                text: "OK",
                onPress: onRefresh,
            },
        ]
    );
}

export default function CollaborationScreen() {
    const router = useRouter();

    const theme = useAppTheme();
    const colors = theme.colors;

    const [pendingInvitations, setPendingInvitations] = useState<TripCollaborationRequest[]>([]);
    const [ownedTripJoinRequests, setOwnedTripJoinRequests] = useState<TripCollaborationRequest[]>([]);
    const [mySentJoinRequests, setMySentJoinRequests] = useState<TripCollaborationRequest[]>([]);

    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [loadingAction, setLoadingAction] = useState<LoadingAction>(null);
    const [error, setError] = useState<string | null>(null);

    const loadCollaboration = useCallback(async () => {
        try {
            setError(null);

            const [invitations, ownedRequests, sentRequests] = await Promise.all([
                getMyPendingInvitations(),
                getPendingJoinRequestsForMyTrips(),
                getMySentPendingJoinRequests(),
            ]);

            setPendingInvitations(Array.isArray(invitations) ? invitations : []);
            setOwnedTripJoinRequests(Array.isArray(ownedRequests) ? ownedRequests : []);
            setMySentJoinRequests(Array.isArray(sentRequests) ? sentRequests : []);
        } catch (error: any) {
            setError(getApiErrorMessage(error, "Failed to load collaboration requests."));
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
        }
    }, []);

    useFocusEffect(
        useCallback(() => {
            setIsLoading(true);
            void loadCollaboration();
        }, [loadCollaboration])
    );

    async function performRefresh() {
        setIsRefreshing(true);
        await loadCollaboration();
    }

    function handleRefresh() {
        void performRefresh();
    }

    function handleJoinWithInviteCode() {
        router.push("/join-trip" as any);
    }

    function handleOpenOwnedJoinRequest(request: TripCollaborationRequest) {
        if (!isRequestPending(request)) {
            showAlreadyHandledAlert(() => {
                void loadCollaboration();
            });
            return;
        }

        router.push(`/trips/${request.tripId}/collaboration/requests` as any);
    }

    async function performAcceptInvitation(request: TripCollaborationRequest) {
        if (!isRequestPending(request)) {
            showAlreadyHandledAlert(() => {
                void loadCollaboration();
            });
            return;
        }

        try {
            setLoadingAction({ requestId: request.requestId, action: "ACCEPT_INVITATION" });
            await acceptInvitation(request.requestId);
            await loadCollaboration();
            Alert.alert("Invitation accepted", `You joined ${request.tripName}.`);
        } catch (error: any) {
            if (isAlreadyHandledError(error)) {
                showAlreadyHandledAlert(() => {
                    void loadCollaboration();
                });
                return;
            }

            Alert.alert("Accept failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setLoadingAction(null);
        }
    }

    function handleAcceptInvitation(request: TripCollaborationRequest) {
        void performAcceptInvitation(request);
    }

    async function performRejectInvitation(request: TripCollaborationRequest) {
        if (!isRequestPending(request)) {
            showAlreadyHandledAlert(() => {
                void loadCollaboration();
            });
            return;
        }

        try {
            setLoadingAction({ requestId: request.requestId, action: "REJECT_INVITATION" });
            await rejectInvitation(request.requestId);
            await loadCollaboration();
            Alert.alert("Invitation rejected", `You declined the invitation to ${request.tripName}.`);
        } catch (error: any) {
            if (isAlreadyHandledError(error)) {
                showAlreadyHandledAlert(() => {
                    void loadCollaboration();
                });
                return;
            }

            Alert.alert("Reject failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setLoadingAction(null);
        }
    }

    function handleRejectInvitation(request: TripCollaborationRequest) {
        void performRejectInvitation(request);
    }

    async function performAcceptJoinRequest(request: TripCollaborationRequest) {
        if (!isRequestPending(request)) {
            showAlreadyHandledAlert(() => {
                void loadCollaboration();
            });
            return;
        }

        try {
            setLoadingAction({ requestId: request.requestId, action: "ACCEPT_JOIN_REQUEST" });
            await acceptJoinRequest(request.requestId);
            await loadCollaboration();
            Alert.alert("Join request accepted", `${request.requesterUsername} can now access ${request.tripName}.`);
        } catch (error: any) {
            if (isAlreadyHandledError(error)) {
                showAlreadyHandledAlert(() => {
                    void loadCollaboration();
                });
                return;
            }

            Alert.alert("Accept failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setLoadingAction(null);
        }
    }

    function handleAcceptJoinRequest(request: TripCollaborationRequest) {
        void performAcceptJoinRequest(request);
    }

    async function performRejectJoinRequest(request: TripCollaborationRequest) {
        if (!isRequestPending(request)) {
            showAlreadyHandledAlert(() => {
                void loadCollaboration();
            });
            return;
        }

        try {
            setLoadingAction({ requestId: request.requestId, action: "REJECT_JOIN_REQUEST" });
            await rejectJoinRequest(request.requestId);
            await loadCollaboration();
            Alert.alert("Join request rejected", `You declined ${request.requesterUsername}'s request to join ${request.tripName}.`);
        } catch (error: any) {
            if (isAlreadyHandledError(error)) {
                showAlreadyHandledAlert(() => {
                    void loadCollaboration();
                });
                return;
            }

            Alert.alert("Reject failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setLoadingAction(null);
        }
    }

    function handleRejectJoinRequest(request: TripCollaborationRequest) {
        void performRejectJoinRequest(request);
    }

    if (isLoading) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Loading collaboration..."
                    subtitle="Checking invitations and join requests."
                    fullScreen
                />
            </AppScreen>
        );
    }

    return (
        <AppScreen scroll={false} contentContainerStyle={styles.screenContent}>
            <ScrollView
                contentContainerStyle={styles.scrollContent}
                refreshControl={
                    <RefreshControl
                        refreshing={isRefreshing}
                        onRefresh={handleRefresh}
                        tintColor={colors.primary}
                        colors={[colors.primary]}
                    />
                }
                showsVerticalScrollIndicator={false}
            >
                <View style={styles.header}>
                    <View style={styles.headerTextGroup}>
                        <Text style={[styles.eyebrow, { color: colors.primary }]}>WanderMate</Text>
                        <Text style={[styles.title, { color: colors.text }]}>Collaboration</Text>
                        <Text style={[styles.subtitle, { color: colors.textMuted }]}>Manage invitations, incoming join requests, and requests you sent.</Text>
                    </View>
                </View>

                <ErrorMessage message={error} title="Could not load collaboration" />

                <AppCard
                    title="Join a trip"
                    subtitle="Use an invite code or deep link from another user."
                    contentStyle={styles.joinCardContent}
                >
                    <View
                        style={[
                            styles.infoBox,
                            { backgroundColor: colors.primarySoft },
                        ]}
                    >
                        <Ionicons name="key-outline" size={22} color={colors.primary} />
                        <View style={styles.infoTextGroup}>
                            <Text style={[styles.infoTitle, { color: colors.primary }]}>Have an invite code?</Text>
                            <Text style={[styles.infoText, { color: colors.primary }]}>Preview the trip and send a join request to the owner.</Text>
                        </View>
                    </View>

                    <AppButton
                        title="Join with Invite Code"
                        onPress={handleJoinWithInviteCode}
                        leftIcon={<Ionicons name="link-outline" size={19} color={colors.textLight} />}
                    />
                </AppCard>

                <RequestSection
                    title="Pending invitations"
                    subtitle="Trips other people invited you to join."
                    count={pendingInvitations.length}
                    emptyTitle="No pending invitations"
                    emptyMessage="When someone invites you to a trip, it will show here."
                    emptyIcon="mail-open-outline"
                >
                    {pendingInvitations.map((invitation) => (
                        <InvitationCard
                            key={invitation.requestId}
                            invitation={invitation}
                            loadingAction={loadingAction}
                            onPreview={() => {
                                if (!isRequestPending(invitation)) {
                                    showAlreadyHandledAlert(() => {
                                        void loadCollaboration();
                                    });
                                    return;
                                }

                                showTripPreview(
                                    "Invitation preview",
                                    invitation,
                                    `Invited by: ${invitation.requesterUsername || "Trip owner"}`
                                );
                            }}
                            onAccept={() => handleAcceptInvitation(invitation)}
                            onReject={() => handleRejectInvitation(invitation)}
                        />
                    ))}
                </RequestSection>

                <RequestSection
                    title="Join requests for my trips"
                    subtitle="Other users requested to join trips that you own."
                    count={ownedTripJoinRequests.length}
                    emptyTitle="No join requests for your trips"
                    emptyMessage="When someone requests access to your trip, it will show here."
                    emptyIcon="people-outline"
                >
                    {ownedTripJoinRequests.map((request) => (
                        <OwnedJoinRequestCard
                            key={request.requestId}
                            request={request}
                            loadingAction={loadingAction}
                            onOpen={() => handleOpenOwnedJoinRequest(request)}
                            onAccept={() => handleAcceptJoinRequest(request)}
                            onReject={() => handleRejectJoinRequest(request)}
                        />
                    ))}
                </RequestSection>

                <RequestSection
                    title="My sent join requests"
                    subtitle="Trips you requested to join and are waiting for owner approval."
                    count={mySentJoinRequests.length}
                    emptyTitle="No sent join requests"
                    emptyMessage="When you request to join another trip, it will show here."
                    emptyIcon="send-outline"
                >
                    {mySentJoinRequests.map((request) => (
                        <SentJoinRequestCard
                            key={request.requestId}
                            request={request}
                            onPreview={() => {
                                if (!isRequestPending(request)) {
                                    showAlreadyHandledAlert(() => {
                                        void loadCollaboration();
                                    });
                                    return;
                                }

                                showTripPreview(
                                    "Join request preview",
                                    request,
                                    `Trip owner: ${request.targetUsername || "Trip owner"}`
                                );
                            }}
                        />
                    ))}
                </RequestSection>
            </ScrollView>
        </AppScreen>
    );
}

type RequestSectionProps = Readonly<{
    title: string;
    subtitle: string;
    count: number;
    emptyTitle: string;
    emptyMessage: string;
    emptyIcon: keyof typeof Ionicons.glyphMap;
    children: React.ReactNode;
}>;

function RequestSection({
                            title,
                            subtitle,
                            count,
                            emptyTitle,
                            emptyMessage,
                            emptyIcon,
                            children,
                        }: RequestSectionProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View style={styles.section}>
            <View style={styles.sectionHeader}>
                <View style={styles.sectionTextGroup}>
                    <Text style={[styles.sectionTitle, { color: colors.text }]}>{title}</Text>
                    <Text style={[styles.sectionSubtitle, { color: colors.textMuted }]}>{subtitle}</Text>
                </View>

                <View style={[styles.countBadge, { backgroundColor: colors.primarySoft }]}>
                    <Text style={[styles.countBadgeText, { color: colors.primary }]}>{count}</Text>
                </View>
            </View>

            {count === 0 ? (
                <EmptyState
                    title={emptyTitle}
                    message={emptyMessage}
                    icon={<Ionicons name={emptyIcon} size={30} color={colors.primary} />}
                />
            ) : (
                <View style={styles.requestList}>{children}</View>
            )}
        </View>
    );
}

type InvitationCardProps = Readonly<{
    invitation: TripCollaborationRequest;
    loadingAction: LoadingAction;
    onPreview: () => void;
    onAccept: () => void;
    onReject: () => void;
}>;

function InvitationCard({
                            invitation,
                            loadingAction,
                            onPreview,
                            onAccept,
                            onReject,
                        }: InvitationCardProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    const isAccepting = loadingAction?.requestId === invitation.requestId && loadingAction.action === "ACCEPT_INVITATION";
    const isRejecting = loadingAction?.requestId === invitation.requestId && loadingAction.action === "REJECT_INVITATION";

    return (
        <AppCard onPress={onPreview} contentStyle={styles.requestCardContent}>
            <RequestTopRow
                icon="mail-outline"
                title={invitation.tripName || "Shared trip"}
                subtitle={`Invited by ${invitation.requesterUsername || "Trip owner"}`}
                role={invitation.requestedRole}
            />

            <RequestMetaBox
                lines={[
                    invitation.destination ? `Destination: ${invitation.destination}` : "Destination not set",
                    `Received ${formatDateTime(invitation.createdDate)}`,
                ]}
            />

            <View style={styles.actionRow}>
                <AppButton
                    title="Reject"
                    onPress={onReject}
                    loading={isRejecting}
                    variant="outline"
                    fullWidth={false}
                    style={styles.actionButton}
                />
                <AppButton
                    title="Accept"
                    onPress={onAccept}
                    loading={isAccepting}
                    fullWidth={false}
                    style={styles.actionButton}
                    rightIcon={<Ionicons name="checkmark-circle-outline" size={18} color={colors.textLight} />}
                />
            </View>
        </AppCard>
    );
}

type OwnedJoinRequestCardProps = Readonly<{
    request: TripCollaborationRequest;
    loadingAction: LoadingAction;
    onOpen: () => void;
    onAccept: () => void;
    onReject: () => void;
}>;

function OwnedJoinRequestCard({
                                  request,
                                  loadingAction,
                                  onOpen,
                                  onAccept,
                                  onReject,
                              }: OwnedJoinRequestCardProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    const isAccepting = loadingAction?.requestId === request.requestId && loadingAction.action === "ACCEPT_JOIN_REQUEST";
    const isRejecting = loadingAction?.requestId === request.requestId && loadingAction.action === "REJECT_JOIN_REQUEST";

    return (
        <AppCard onPress={onOpen} contentStyle={styles.requestCardContent}>
            <RequestTopRow
                icon="person-add-outline"
                title={request.tripName || "Your trip"}
                subtitle={`${request.requesterUsername || "A user"} requested to join`}
                role={request.requestedRole}
            />

            <RequestMetaBox
                lines={[
                    request.destination ? `Destination: ${request.destination}` : "Destination not set",
                    `Requested ${formatDateTime(request.createdDate)}`,
                ]}
            />

            <View style={styles.actionRow}>
                <AppButton
                    title="Reject"
                    onPress={onReject}
                    loading={isRejecting}
                    variant="outline"
                    fullWidth={false}
                    style={styles.actionButton}
                />
                <AppButton
                    title="Accept"
                    onPress={onAccept}
                    loading={isAccepting}
                    fullWidth={false}
                    style={styles.actionButton}
                    rightIcon={<Ionicons name="checkmark-circle-outline" size={18} color={colors.textLight} />}
                />
            </View>

            <View style={styles.openHintRow}>
                <Text style={[styles.openHintText, { color: colors.textMuted }]}>Tap card to open this trip's request screen</Text>
                <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
            </View>
        </AppCard>
    );
}

type SentJoinRequestCardProps = Readonly<{
    request: TripCollaborationRequest;
    onPreview: () => void;
}>;

function SentJoinRequestCard({ request, onPreview }: SentJoinRequestCardProps) {
    return (
        <AppCard onPress={onPreview} contentStyle={styles.requestCardContent}>
            <RequestTopRow
                icon="send-outline"
                title={request.tripName || "Requested trip"}
                subtitle={`Waiting for ${request.targetUsername || "trip owner"} to approve`}
                role={request.requestedRole}
            />

            <RequestMetaBox
                lines={[
                    request.destination ? `Destination: ${request.destination}` : "Destination not set",
                    `Sent ${formatDateTime(request.createdDate)}`,
                    "Status: Waiting for approval",
                ]}
            />
        </AppCard>
    );
}

type RequestTopRowProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    title: string;
    subtitle: string;
    role: TripCollaborationRequest["requestedRole"];
}>;

function RequestTopRow({ icon, title, subtitle, role }: RequestTopRowProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View style={styles.requestTopRow}>
            <View style={[styles.tripIconBadge, { backgroundColor: colors.primarySoft }]}>
                <Ionicons name={icon} size={22} color={colors.primary} />
            </View>

            <View style={styles.requestTextGroup}>
                <Text style={[styles.requestTitle, { color: colors.text }]} numberOfLines={1}>{title}</Text>
                <Text style={[styles.requestSubtitle, { color: colors.textMuted }]}>{subtitle}</Text>
            </View>

            <RoleBadge role={role} />
        </View>
    );
}

type RequestMetaBoxProps = Readonly<{
    lines: string[];
}>;

function RequestMetaBox({ lines }: RequestMetaBoxProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View style={[styles.metaBox, { backgroundColor: colors.surfaceSoft }]}>
            {lines.map((line) => (
                <Text key={line} style={[styles.metaText, { color: colors.textMuted }]}>
                    {line}
                </Text>
            ))}
        </View>
    );
}

const styles = StyleSheet.create({
    screenContent: {
        flex: 1,
    },
    scrollContent: {
        paddingTop: spacing.xl,
        paddingBottom: spacing.xxl,
        gap: spacing.lg,
    },
    header: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.md,
    },
    headerTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    eyebrow: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.7,
    },
    title: {
        fontSize: typography.hero,
        fontWeight: fontWeight.bold,
        lineHeight: 38,
    },
    subtitle: {
        fontSize: typography.bodySmall,
        lineHeight: 21,
    },
    joinCardContent: {
        gap: spacing.lg,
    },
    infoBox: {
        flexDirection: "row",
        alignItems: "flex-start",
        gap: spacing.md,
        borderRadius: radius.lg,
        padding: spacing.md,
    },
    infoTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    infoTitle: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    infoText: {
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    section: {
        gap: spacing.md,
    },
    sectionHeader: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.md,
    },
    sectionTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    sectionTitle: {
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    sectionSubtitle: {
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    countBadge: {
        minWidth: 34,
        height: 34,
        borderRadius: radius.pill,
        alignItems: "center",
        justifyContent: "center",
        paddingHorizontal: spacing.sm,
    },
    countBadgeText: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    requestList: {
        gap: spacing.md,
    },
    requestCardContent: {
        gap: spacing.lg,
    },
    requestTopRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    tripIconBadge: {
        width: 46,
        height: 46,
        borderRadius: radius.lg,
        alignItems: "center",
        justifyContent: "center",
    },
    requestTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    requestTitle: {
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    requestSubtitle: {
        fontSize: typography.caption,
        lineHeight: 18,
    },
    metaBox: {
        borderRadius: radius.md,
        padding: spacing.md,
        gap: spacing.xs,
    },
    metaText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
        lineHeight: 18,
    },
    actionRow: {
        flexDirection: "row",
        gap: spacing.md,
    },
    actionButton: {
        flex: 1,
    },
    openHintRow: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.sm,
    },
    openHintText: {
        flex: 1,
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
        lineHeight: 18,
    },
});
