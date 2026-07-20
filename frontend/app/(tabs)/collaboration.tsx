import { useCallback, useState } from "react";
import {
    Alert,
    RefreshControl,
    ScrollView,
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
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { TripCollaborationRequest } from "@/src/types/tripCollaboration";
import { getApiErrorCode, getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { formatDateTime } from "@/src/utils/dateFormat";

import {
    InvitationCard,
    OwnedJoinRequestCard,
    RequestSection,
    SentJoinRequestCard,
} from "@/src/features/collaboration/CollaborationRequestComponents";
import { styles } from "@/src/features/collaboration/collaborationStyles";
import type { LoadingAction } from "@/src/features/collaboration/collaborationViewTypes";

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

function isAlreadyHandledError(error: unknown) {
    const code = getApiErrorCode(error);
    const message = getApiErrorMessage(error, "").toLowerCase();

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
        } catch (error: unknown) {
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
        router.push("/join-trip");
    }

    function handleOpenOwnedJoinRequest(request: TripCollaborationRequest) {
        if (!isRequestPending(request)) {
            showAlreadyHandledAlert(() => {
                void loadCollaboration();
            });
            return;
        }

        router.push({
            pathname: "/trips/[tripId]/collaboration/requests",
            params: { tripId: request.tripId },
        });
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
        } catch (error: unknown) {
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
        } catch (error: unknown) {
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
        } catch (error: unknown) {
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
        } catch (error: unknown) {
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
