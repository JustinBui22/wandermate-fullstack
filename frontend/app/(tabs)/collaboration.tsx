import { useCallback, useState } from "react";
import { Alert, RefreshControl, ScrollView, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useRouter } from "expo-router";

import { acceptInvitation, getMyPendingInvitations, rejectInvitation } from "@/src/api/tripCollaborationApi";
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

type InvitationAction = "ACCEPT" | "REJECT";
type LoadingInvitationAction = { requestId: number; action: InvitationAction } | null;

function getInvitationOwner(request: TripCollaborationRequest) {
    const value = request as any;
    return value.targetUsername || value.ownerUsername || value.inviterUsername || value.targetUser?.username || "Trip owner";
}

export default function CollaborationScreen() {
    const router = useRouter();
    const [invitations, setInvitations] = useState<TripCollaborationRequest[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [loadingAction, setLoadingAction] = useState<LoadingInvitationAction>(null);
    const [error, setError] = useState<string | null>(null);

    async function loadInvitations() {
        try {
            setError(null);
            const data = await getMyPendingInvitations();
            setInvitations(Array.isArray(data) ? data : []);
        } catch (error: any) {
            setError(getApiErrorMessage(error, "Failed to load collaboration invitations."));
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

    async function handleAcceptInvitation(request: TripCollaborationRequest) {
        try {
            setLoadingAction({ requestId: request.requestId, action: "ACCEPT" });
            await acceptInvitation(request.requestId);
            await loadInvitations();
            Alert.alert("Invitation accepted", `You joined ${request.tripName}.`);
        } catch (error: any) {
            Alert.alert("Accept failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setLoadingAction(null);
        }
    }

    async function handleRejectInvitation(request: TripCollaborationRequest) {
        try {
            setLoadingAction({ requestId: request.requestId, action: "REJECT" });
            await rejectInvitation(request.requestId);
            await loadInvitations();
        } catch (error: any) {
            Alert.alert("Reject failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setLoadingAction(null);
        }
    }

    if (isLoading) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState title="Loading collaboration..." subtitle="Checking your invitations." fullScreen />
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
                    <View style={styles.headerTextGroup}>
                        <Text style={styles.eyebrow}>WanderMate</Text>
                        <Text style={styles.title}>Collaboration</Text>
                        <Text style={styles.subtitle}>Join shared trips and manage invitations sent to you.</Text>
                    </View>
                </View>

                <ErrorMessage message={error} title="Could not load invitations" />

                <AppCard title="Join a trip" subtitle="Use an invite code or deep link from another user." contentStyle={styles.joinCardContent}>
                    <View style={styles.infoBox}>
                        <Ionicons name="key-outline" size={22} color={colors.primary} />
                        <View style={styles.infoTextGroup}>
                            <Text style={styles.infoTitle}>Have an invite code?</Text>
                            <Text style={styles.infoText}>Preview the trip and send a join request to the owner.</Text>
                        </View>
                    </View>

                    <AppButton
                        title="Join with Invite Code"
                        onPress={() => router.push("/join-trip" as any)}
                        leftIcon={<Ionicons name="link-outline" size={19} color={colors.textLight} />}
                    />
                </AppCard>

                <View style={styles.sectionHeader}>
                    <View style={styles.sectionTextGroup}>
                        <Text style={styles.sectionTitle}>Pending invitations</Text>
                        <Text style={styles.sectionSubtitle}>Trips where someone invited you directly.</Text>
                    </View>
                    <View style={styles.countBadge}>
                        <Text style={styles.countBadgeText}>{invitations.length}</Text>
                    </View>
                </View>

                {invitations.length === 0 ? (
                    <EmptyState
                        title="No pending invitations"
                        message="When someone invites you to a trip, it will show here."
                        icon={<Ionicons name="mail-open-outline" size={30} color={colors.primary} />}
                    />
                ) : (
                    <View style={styles.invitationList}>
                        {invitations.map((invitation) => (
                            <InvitationCard
                                key={invitation.requestId}
                                invitation={invitation}
                                loadingAction={loadingAction}
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

type InvitationCardProps = Readonly<{
    invitation: TripCollaborationRequest;
    loadingAction: LoadingInvitationAction;
    onAccept: () => void;
    onReject: () => void;
}>;

function InvitationCard({ invitation, loadingAction, onAccept, onReject }: InvitationCardProps) {
    const isAccepting = loadingAction?.requestId === invitation.requestId && loadingAction.action === "ACCEPT";
    const isRejecting = loadingAction?.requestId === invitation.requestId && loadingAction.action === "REJECT";

    return (
        <AppCard contentStyle={styles.invitationCardContent}>
            <View style={styles.invitationTopRow}>
                <View style={styles.tripIconBadge}>
                    <Ionicons name="map-outline" size={22} color={colors.primary} />
                </View>

                <View style={styles.invitationTextGroup}>
                    <Text style={styles.invitationTitle}>{invitation.tripName || "Shared trip"}</Text>
                    <Text style={styles.invitationSubtitle}>Invited by {getInvitationOwner(invitation)}</Text>
                </View>

                <RoleBadge role={invitation.requestedRole} />
            </View>

            <View style={styles.metaBox}>
                <Text style={styles.metaText}>Received {formatDateTime(invitation.createdDate)}</Text>
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
    scrollContent: { paddingTop: spacing.xl, paddingBottom: spacing.xxl, gap: spacing.lg },
    header: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: spacing.md },
    headerTextGroup: { flex: 1, gap: spacing.xs },
    eyebrow: { color: colors.primary, fontSize: typography.caption, fontWeight: fontWeight.bold, textTransform: "uppercase", letterSpacing: 0.7 },
    title: { color: colors.text, fontSize: typography.hero, fontWeight: fontWeight.bold, lineHeight: 38 },
    subtitle: { color: colors.textMuted, fontSize: typography.bodySmall, lineHeight: 21 },
    joinCardContent: { gap: spacing.lg },
    infoBox: { flexDirection: "row", alignItems: "flex-start", gap: spacing.md, borderRadius: radius.lg, backgroundColor: colors.primarySoft, padding: spacing.md },
    infoTextGroup: { flex: 1, gap: spacing.xs },
    infoTitle: { color: colors.primary, fontSize: typography.bodySmall, fontWeight: fontWeight.bold },
    infoText: { color: colors.primary, fontSize: typography.caption, lineHeight: 18, fontWeight: fontWeight.semibold },
    sectionHeader: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", gap: spacing.md },
    sectionTextGroup: { flex: 1, gap: spacing.xs },
    sectionTitle: { color: colors.text, fontSize: typography.title, fontWeight: fontWeight.bold },
    sectionSubtitle: { color: colors.textMuted, fontSize: typography.bodySmall, lineHeight: 20 },
    countBadge: { minWidth: 34, height: 34, borderRadius: radius.pill, backgroundColor: colors.primarySoft, alignItems: "center", justifyContent: "center", paddingHorizontal: spacing.sm },
    countBadgeText: { color: colors.primary, fontSize: typography.bodySmall, fontWeight: fontWeight.bold },
    invitationList: { gap: spacing.md },
    invitationCardContent: { gap: spacing.lg },
    invitationTopRow: { flexDirection: "row", alignItems: "center", gap: spacing.md },
    tripIconBadge: { width: 46, height: 46, borderRadius: radius.lg, backgroundColor: colors.primarySoft, alignItems: "center", justifyContent: "center" },
    invitationTextGroup: { flex: 1, gap: spacing.xs },
    invitationTitle: { color: colors.text, fontSize: typography.body, fontWeight: fontWeight.bold },
    invitationSubtitle: { color: colors.textMuted, fontSize: typography.caption, lineHeight: 18 },
    metaBox: { borderRadius: radius.md, backgroundColor: colors.surfaceSoft, padding: spacing.md },
    metaText: { color: colors.textMuted, fontSize: typography.caption, fontWeight: fontWeight.semibold },
    actionRow: { flexDirection: "row", gap: spacing.md },
    actionButton: { flex: 1 },
});
