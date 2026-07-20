import { useCallback, useState } from "react";
import { Alert, Share, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import * as Clipboard from "expo-clipboard";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";

import { getTripById } from "@/src/api/tripApi";
import {
    getActiveTripShareCode,
    regenerateTripShareCode,
} from "@/src/api/tripCollaborationApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import {
    CurrentShareCodeCard,
    ShareCodeRoleSelector,
    type InvitableRole,
} from "@/src/features/collaboration/ShareCodeControls";
import { colors as staticColors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { TripShareCode } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";

export default function TripShareCodeScreen() {
    const router = useRouter();
    const theme = useAppTheme();
    const colors = theme.colors;
    const params = useLocalSearchParams();

    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripId);

    const [isCheckingAccess, setIsCheckingAccess] = useState(true);
    const [isOwner, setIsOwner] = useState(false);
    const [accessError, setAccessError] = useState<string | null>(null);
    const [isLoadingActiveCode, setIsLoadingActiveCode] = useState(false);
    const [role, setRole] = useState<InvitableRole>("VIEWER");
    const [shareCode, setShareCode] = useState<TripShareCode | null>(null);
    const [isGenerating, setIsGenerating] = useState(false);
    const [isSharing, setIsSharing] = useState(false);

    const loadScreenData = useCallback(async () => {
        if (!hasValidTripId) {
            setAccessError("Trip ID is missing or invalid.");
            setIsOwner(false);
            setIsCheckingAccess(false);
            return;
        }

        try {
            setIsCheckingAccess(true);
            setIsLoadingActiveCode(true);
            setAccessError(null);

            const trip = await getTripById(tripId);
            const owner = trip.currentUserRole === "OWNER";
            setIsOwner(owner);

            if (!owner) {
                setShareCode(null);
                setAccessError("Only the trip owner can generate invite codes.");
                return;
            }

            const activeCode = await getActiveTripShareCode(tripId);
            setShareCode(activeCode);

            if (
                activeCode?.defaultRole === "VIEWER" ||
                activeCode?.defaultRole === "EDITOR"
            ) {
                setRole(activeCode.defaultRole);
            }
        } catch (error: unknown) {
            setIsOwner(false);
            setAccessError(getApiErrorMessage(error, "Could not load invite code settings."));
            setShareCode(null);
        } finally {
            setIsCheckingAccess(false);
            setIsLoadingActiveCode(false);
        }
    }, [hasValidTripId, tripId]);

    useFocusEffect(
        useCallback(() => {
            void loadScreenData();
        }, [loadScreenData])
    );

    async function handleGenerateCode() {
        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
            return;
        }

        if (!isOwner) {
            Alert.alert("Owner only", "Only the trip owner can generate invite codes.");
            return;
        }

        try {
            setIsGenerating(true);

            const result = await regenerateTripShareCode(tripId, {
                defaultRole: role,
            });

            setShareCode(result);

            Alert.alert(
                "Invite code ready",
                "Share this single-use code with one person. It becomes invalid after successful use."
            );
        } catch (error: unknown) {
            Alert.alert(
                "Generate code failed",
                getApiErrorMessage(error, "Please wait a moment and try again.")
            );
        } finally {
            setIsGenerating(false);
        }
    }

    async function handleCopyCode() {
        if (!shareCode) {
            return;
        }

        await Clipboard.setStringAsync(shareCode.code);
        Alert.alert("Code copied", "Only the invite code was copied.");
    }

    async function handleCopyLink() {
        if (!shareCode) {
            return;
        }

        await Clipboard.setStringAsync(shareCode.inviteLink);
        Alert.alert("Link copied", "The deep link was copied.");
    }

    async function handleShareInvite() {
        if (!shareCode) {
            return;
        }

        const message = [
            `Join my WanderMate trip: ${shareCode.tripName}`,
            `Invite code: ${shareCode.code}`,
            `Link: ${shareCode.inviteLink}`,
        ].join("\n");

        try {
            setIsSharing(true);
            await Share.share({ message });
        } finally {
            setIsSharing(false);
        }
    }

    if (isCheckingAccess) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Checking access..."
                    subtitle="Confirming whether you can generate invite codes."
                    fullScreen
                />
            </AppScreen>
        );
    }

    if (!isOwner) {
        return (
            <AppScreen contentContainerStyle={styles.screenContent}>
                <View style={styles.header}>
                    <HeaderButton onPress={() => router.back()} />

                    <View style={styles.headerTextGroup}>
                        <Text style={[styles.eyebrow, { color: colors.primary }]}>Invite code</Text>
                        <Text style={[styles.title, { color: colors.text }]}>Owner only</Text>
                        <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                            This page is only available to the trip owner.
                        </Text>
                    </View>
                </View>

                <ErrorMessage message={accessError} title="Access denied" />

                <EmptyState
                    title="You cannot generate invite codes"
                    message="Ask the trip owner to share an invite code or update collaboration settings."
                    icon={<Ionicons name="lock-closed-outline" size={30} color={colors.primary} />}
                    actionLabel="Go back"
                    onActionPress={() => router.back()}
                />
            </AppScreen>
        );
    }

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <HeaderButton onPress={() => router.back()} />

                <View style={styles.headerTextGroup}>
                    <Text style={[styles.eyebrow, { color: colors.primary }]}>Invite code</Text>

                    <Text style={[styles.title, { color: colors.text }]}>Share by code or link</Text>

                    <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                        Generate a single-use invite code. When someone successfully requests to
                        join, the code becomes used.
                    </Text>
                </View>
            </View>

            <ErrorMessage message={accessError} title="Invite code warning" />

            <AppCard contentStyle={styles.formContent}>
                <ShareCodeRoleSelector role={role} onRoleChange={setRole} />

                <AppButton
                    title={shareCode ? "Regenerate Invite Code" : "Generate Invite Code"}
                    onPress={() => {
                        void handleGenerateCode();
                    }}
                    loading={isGenerating || isLoadingActiveCode}
                    leftIcon={
                        <Ionicons
                            name="refresh-outline"
                            size={19}
                            color={colors.textLight}
                        />
                    }
                />
            </AppCard>

            {isLoadingActiveCode ? (
                <AppCard contentStyle={styles.infoCardContent}>
                    <Ionicons name="sync-outline" size={22} color={colors.primary} />

                    <Text style={[styles.infoText, { color: colors.textMuted }]}>
                        Checking for active invite code...
                    </Text>
                </AppCard>
            ) : null}

            {shareCode ? (
                <CurrentShareCodeCard
                    shareCode={shareCode}
                    isSharing={isSharing}
                    isRegenerating={isGenerating || isLoadingActiveCode}
                    onCopyCode={() => { void handleCopyCode(); }}
                    onCopyLink={() => { void handleCopyLink(); }}
                    onShare={() => { void handleShareInvite(); }}
                    onRegenerate={() => { void handleGenerateCode(); }}
                />
            ) : null}
        </AppScreen>
    );
}

function HeaderButton({ onPress }: Readonly<{ onPress: () => void }>) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <AppCard
            onPress={onPress}
            style={styles.backButton}
            contentStyle={styles.backButtonContent}
        >
            <Ionicons name="chevron-back" size={22} color={colors.text} />
        </AppCard>
    );
}

const styles = StyleSheet.create({
    screenContent: {
        paddingTop: spacing.lg,
        paddingBottom: spacing.xxl,
        gap: spacing.lg,
    },
    header: {
        gap: spacing.lg,
    },
    backButton: {
        width: 46,
        height: 46,
        borderRadius: radius.lg,
    },
    backButtonContent: {
        flex: 1,
        padding: 0,
        alignItems: "center",
        justifyContent: "center",
    },
    headerTextGroup: {
        gap: spacing.xs,
    },
    eyebrow: {
        color: staticColors.primary,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.7,
    },
    title: {
        color: staticColors.text,
        fontSize: typography.heading,
        fontWeight: fontWeight.bold,
    },
    subtitle: {
        color: staticColors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 21,
    },
    formContent: {
        gap: spacing.lg,
    },
    roleSection: {
        gap: spacing.sm,
    },
    roleLabel: {
        color: staticColors.text,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    roleRow: {
        gap: spacing.md,
    },
    roleChip: {
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor: staticColors.border,
        backgroundColor: staticColors.surface,
        padding: spacing.md,
        gap: spacing.sm,
    },
    roleChipSelected: {
        borderColor: staticColors.primary,
        backgroundColor: staticColors.primarySoft,
    },
    roleHelpText: {
        color: staticColors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    infoCardContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    infoText: {
        flex: 1,
        color: staticColors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
        fontWeight: fontWeight.semibold,
    },
    codeCardContent: {
        gap: spacing.lg,
    },
    codeBox: {
        borderRadius: radius.lg,
        backgroundColor: staticColors.primary,
        padding: spacing.lg,
        gap: spacing.xs,
    },
    codeLabel: {
        color: "#DBEAFE",
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.6,
    },
    codeText: {
        color: staticColors.textLight,
        fontSize: typography.heading,
        fontWeight: fontWeight.bold,
        letterSpacing: 1.2,
    },
    expiryBox: {
        flexDirection: "row",
        alignItems: "flex-start",
        gap: spacing.md,
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor: staticColors.warning,
        backgroundColor: staticColors.warningSoft,
        padding: spacing.md,
    },
    expiryTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    expiryTitle: {
        color: staticColors.warning,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    expirySubtitle: {
        color: staticColors.warning,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    linkBox: {
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor: staticColors.border,
        backgroundColor: staticColors.surfaceSoft,
        padding: spacing.md,
        gap: spacing.xs,
    },
    linkLabel: {
        color: staticColors.textMuted,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.4,
    },
    linkText: {
        color: staticColors.text,
        fontSize: typography.bodySmall,
        lineHeight: 20,
        fontWeight: fontWeight.semibold,
    },
    actionGrid: {
        flexDirection: "row",
        gap: spacing.md,
    },
    actionButton: {
        flex: 1,
    },
    pressed: {
        opacity: 0.86,
        transform: [{ scale: 0.99 }],
    },
});
