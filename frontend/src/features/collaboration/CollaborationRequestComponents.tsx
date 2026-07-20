import type { ReactNode } from "react";
import { Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";

import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { styles } from "@/src/features/collaboration/collaborationStyles";
import type { LoadingAction } from "@/src/features/collaboration/collaborationViewTypes";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { TripCollaborationRequest } from "@/src/types/tripCollaboration";
import { formatDateTime } from "@/src/utils/dateFormat";

type RequestSectionProps = Readonly<{
    title: string;
    subtitle: string;
    count: number;
    emptyTitle: string;
    emptyMessage: string;
    emptyIcon: keyof typeof Ionicons.glyphMap;
    children: ReactNode;
}>;

export function RequestSection({
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

export function InvitationCard({
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

export function OwnedJoinRequestCard({
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

export function SentJoinRequestCard({ request, onPreview }: SentJoinRequestCardProps) {
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
