import { useCallback, useState } from "react";
import { Alert, RefreshControl, ScrollView, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";

import { getTripById } from "@/src/api/tripApi";
import { acceptJoinRequest, getPendingJoinRequests, rejectJoinRequest } from "@/src/api/tripCollaborationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type { TripCollaborationRequest } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { formatDateTime } from "@/src/utils/dateFormat";

type RequestAction = "ACCEPT" | "REJECT";
type LoadingAction = { requestId: number; action: RequestAction } | null;

function getRequestUsername(request: TripCollaborationRequest) {
    const value = request as any;
    return value.requesterUsername || value.requester || value.requesterUser?.username || value.username || "Unknown user";
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
        "This join request has already been accepted or declined. The list will refresh now.",
        [
            {
                text: "OK",
                onPress: onRefresh,
            },
        ]
    );
}

export default function TripJoinRequestsScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();
    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripId);

    const [requests, setRequests] = useState<TripCollaborationRequest[]>([]);
    const [isOwner, setIsOwner] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [loadingAction, setLoadingAction] = useState<LoadingAction>(null);
    const [error, setError] = useState<string | null>(null);

    const loadRequests = useCallback(async () => {
        if (!hasValidTripId) {
            setError("Trip ID is missing or invalid.");
            setIsOwner(false);
            setIsLoading(false);
            setIsRefreshing(false);
            return;
        }

        try {
            setError(null);

            const trip = await getTripById(tripId);
            const owner = trip.currentUserRole === "OWNER";
            setIsOwner(owner);

            if (!owner) {
                setRequests([]);
                setError("Only the trip owner can review join requests.");
                return;
            }

            const data = await getPendingJoinRequests(tripId);
            setRequests(Array.isArray(data) ? data : []);
        } catch (error: any) {
            setIsOwner(false);
            setRequests([]);
            setError(getApiErrorMessage(error, "Failed to load join requests."));
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
        }
    }, [hasValidTripId, tripId]);

    useFocusEffect(
        useCallback(() => {
            setIsLoading(true);
            void loadRequests();
        }, [loadRequests])
    );

    async function performRefresh() {
        setIsRefreshing(true);
        await loadRequests();
    }

    function handleRefresh() {
        void performRefresh();
    }

    async function performAccept(request: TripCollaborationRequest) {
        if (!isOwner) {
            Alert.alert("Owner only", "Only the trip owner can accept join requests.");
            return;
        }

        if (!isRequestPending(request)) {
            showAlreadyHandledAlert(() => {
                void loadRequests();
            });
            return;
        }

        try {
            setLoadingAction({ requestId: request.requestId, action: "ACCEPT" });
            await acceptJoinRequest(request.requestId);
            await loadRequests();
            Alert.alert("Request accepted", `${getRequestUsername(request)} can now access this trip.`);
        } catch (error: any) {
            if (isAlreadyHandledError(error)) {
                showAlreadyHandledAlert(() => {
                    void loadRequests();
                });
                return;
            }

            Alert.alert("Accept failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setLoadingAction(null);
        }
    }

    function handleAccept(request: TripCollaborationRequest) {
        void performAccept(request);
    }

    async function performReject(request: TripCollaborationRequest) {
        if (!isOwner) {
            Alert.alert("Owner only", "Only the trip owner can reject join requests.");
            return;
        }

        if (!isRequestPending(request)) {
            showAlreadyHandledAlert(() => {
                void loadRequests();
            });
            return;
        }

        try {
            setLoadingAction({ requestId: request.requestId, action: "REJECT" });
            await rejectJoinRequest(request.requestId);
            await loadRequests();
            Alert.alert("Request rejected", `You declined ${getRequestUsername(request)}'s request to join this trip.`);
        } catch (error: any) {
            if (isAlreadyHandledError(error)) {
                showAlreadyHandledAlert(() => {
                    void loadRequests();
                });
                return;
            }

            Alert.alert("Reject failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setLoadingAction(null);
        }
    }

    function handleReject(request: TripCollaborationRequest) {
        void performReject(request);
    }

    if (isLoading) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState title="Loading join requests..." subtitle="Checking pending requests." fullScreen />
            </AppScreen>
        );
    }

    if (!isOwner) {
        return (
            <AppScreen scroll={false} contentContainerStyle={styles.screenContent}>
                <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}>
                    <View style={styles.header}>
                        <HeaderButton onPress={() => router.back()} />
                        <View style={styles.headerTextGroup}>
                            <Text style={styles.eyebrow}>Requests</Text>
                            <Text style={styles.title}>Owner only</Text>
                            <Text style={styles.subtitle}>Only the trip owner can review pending join requests.</Text>
                        </View>
                    </View>

                    <ErrorMessage message={error} title="Access denied" />

                    <EmptyState
                        title="You cannot manage requests"
                        message="Ask the trip owner to accept or reject pending join requests."
                        icon={<Ionicons name="lock-closed-outline" size={30} color={colors.primary} />}
                        actionLabel="Go back"
                        onActionPress={() => router.back()}
                    />
                </ScrollView>
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
                        <Text style={styles.eyebrow}>Requests</Text>
                        <Text style={styles.title}>Pending join requests</Text>
                        <Text style={styles.subtitle}>Review people who requested access through a code, link, or trip request.</Text>
                    </View>
                </View>

                <ErrorMessage message={error} title="Could not load requests" />

                {requests.length === 0 ? (
                    <EmptyState
                        title="No pending requests"
                        message="New join requests will appear here."
                        icon={<Ionicons name="mail-open-outline" size={30} color={colors.primary} />}
                    />
                ) : (
                    <View style={styles.requestList}>
                        {requests.map((request) => (
                            <RequestCard
                                key={request.requestId}
                                request={request}
                                loadingAction={loadingAction}
                                onAccept={() => handleAccept(request)}
                                onReject={() => handleReject(request)}
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

type RequestCardProps = Readonly<{
    request: TripCollaborationRequest;
    loadingAction: LoadingAction;
    onAccept: () => void;
    onReject: () => void;
}>;

function RequestCard({ request, loadingAction, onAccept, onReject }: RequestCardProps) {
    const username = getRequestUsername(request);
    const isAccepting = loadingAction?.requestId === request.requestId && loadingAction.action === "ACCEPT";
    const isRejecting = loadingAction?.requestId === request.requestId && loadingAction.action === "REJECT";

    return (
        <AppCard contentStyle={styles.requestCardContent}>
            <View style={styles.requestTopRow}>
                <View style={styles.avatar}>
                    <Text style={styles.avatarText}>{username.charAt(0).toUpperCase()}</Text>
                </View>

                <View style={styles.requestTextGroup}>
                    <Text style={styles.requestTitle}>{username}</Text>
                    <Text style={styles.requestSubtitle}>Requested {formatDateTime(request.createdDate)}</Text>
                </View>

                <RoleBadge role={request.requestedRole} />
            </View>

            <View style={styles.actionRow}>
                <AppButton title="Reject" onPress={onReject} loading={isRejecting} variant="outline" fullWidth={false} style={styles.actionButton} />
                <AppButton title="Accept" onPress={onAccept} loading={isAccepting} fullWidth={false} style={styles.actionButton} />
            </View>
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
    requestList: { gap: spacing.md },
    requestCardContent: { gap: spacing.lg },
    requestTopRow: { flexDirection: "row", alignItems: "center", gap: spacing.md },
    avatar: { width: 46, height: 46, borderRadius: radius.pill, backgroundColor: colors.primarySoft, alignItems: "center", justifyContent: "center" },
    avatarText: { color: colors.primary, fontSize: typography.body, fontWeight: fontWeight.bold },
    requestTextGroup: { flex: 1, gap: spacing.xs },
    requestTitle: { color: colors.text, fontSize: typography.body, fontWeight: fontWeight.bold },
    requestSubtitle: { color: colors.textMuted, fontSize: typography.caption, lineHeight: 18 },
    actionRow: { flexDirection: "row", gap: spacing.md },
    actionButton: { flex: 1 },
});
