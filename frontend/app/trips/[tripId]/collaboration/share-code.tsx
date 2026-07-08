import { useCallback, useState } from "react";
import { Alert, Pressable, Share, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import * as Clipboard from "expo-clipboard";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";

import { getTripById } from "@/src/api/tripApi";
import {
    getActiveTripShareCode,
    regenerateTripShareCode,
} from "@/src/api/tripCollaborationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type { TripShareCode } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { formatDateTime } from "@/src/utils/dateFormat";

type InvitableRole = "EDITOR" | "VIEWER";

const ROLES: InvitableRole[] = ["VIEWER", "EDITOR"];

export default function TripShareCodeScreen() {
    const router = useRouter();
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
        } catch (error: any) {
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
        } catch (error: any) {
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
                        <Text style={styles.eyebrow}>Invite code</Text>
                        <Text style={styles.title}>Owner only</Text>
                        <Text style={styles.subtitle}>
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
                    <Text style={styles.eyebrow}>Invite code</Text>

                    <Text style={styles.title}>Share by code or link</Text>

                    <Text style={styles.subtitle}>
                        Generate a single-use invite code. When someone successfully requests to
                        join, the code becomes used.
                    </Text>
                </View>
            </View>

            <ErrorMessage message={accessError} title="Invite code warning" />

            <AppCard contentStyle={styles.formContent}>
                <View style={styles.roleSection}>
                    <Text style={styles.roleLabel}>Default role for this code</Text>

                    <View style={styles.roleRow}>
                        {ROLES.map((item) => (
                            <Pressable
                                key={item}
                                accessibilityRole="button"
                                onPress={() => setRole(item)}
                                style={({ pressed }) => [
                                    styles.roleChip,
                                    role === item && styles.roleChipSelected,
                                    pressed && styles.pressed,
                                ]}
                            >
                                <RoleBadge role={item} />

                                <Text style={styles.roleHelpText}>
                                    {item === "VIEWER"
                                        ? "View-only access"
                                        : "Can edit trip content"}
                                </Text>
                            </Pressable>
                        ))}
                    </View>
                </View>

                <AppButton
                    title={shareCode ? "Regenerate Invite Code" : "Generate Invite Code"}
                    onPress={handleGenerateCode}
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

                    <Text style={styles.infoText}>
                        Checking for active invite code...
                    </Text>
                </AppCard>
            ) : null}

            {shareCode ? (
                <AppCard
                    title="Current invite"
                    subtitle="Use the code for manual entry or the link for deep-link opening."
                    contentStyle={styles.codeCardContent}
                >
                    <View style={styles.codeBox}>
                        <Text style={styles.codeLabel}>Invite code</Text>

                        <Text style={styles.codeText}>{shareCode.code}</Text>
                    </View>

                    <View style={styles.expiryBox}>
                        <Ionicons name="time-outline" size={22} color={colors.warning} />

                        <View style={styles.expiryTextGroup}>
                            <Text style={styles.expiryTitle}>
                                Expires {formatDateTime(shareCode.expiresAt)}
                            </Text>

                            <Text style={styles.expirySubtitle}>
                                This code is single-use. It also becomes invalid if you regenerate
                                another code.
                            </Text>
                        </View>
                    </View>

                    <View style={styles.linkBox}>
                        <Text style={styles.linkLabel}>Deep link</Text>

                        <Text style={styles.linkText} numberOfLines={2}>
                            {shareCode.inviteLink}
                        </Text>
                    </View>

                    <View style={styles.actionGrid}>
                        <AppButton
                            title="Copy Code"
                            onPress={handleCopyCode}
                            variant="outline"
                            fullWidth={false}
                            style={styles.actionButton}
                        />

                        <AppButton
                            title="Copy Link"
                            onPress={handleCopyLink}
                            variant="outline"
                            fullWidth={false}
                            style={styles.actionButton}
                        />
                    </View>

                    <AppButton
                        title="Share Invite Message"
                        onPress={handleShareInvite}
                        loading={isSharing}
                        variant="outline"
                        leftIcon={
                            <Ionicons
                                name="share-social-outline"
                                size={19}
                                color={colors.primary}
                            />
                        }
                    />

                    <AppButton
                        title="Regenerate Invite Code"
                        onPress={handleGenerateCode}
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
            ) : null}
        </AppScreen>
    );
}

function HeaderButton({ onPress }: Readonly<{ onPress: () => void }>) {
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
        lineHeight: 21,
    },
    formContent: {
        gap: spacing.lg,
    },
    roleSection: {
        gap: spacing.sm,
    },
    roleLabel: {
        color: colors.text,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    roleRow: {
        gap: spacing.md,
    },
    roleChip: {
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor: colors.border,
        backgroundColor: colors.surface,
        padding: spacing.md,
        gap: spacing.sm,
    },
    roleChipSelected: {
        borderColor: colors.primary,
        backgroundColor: colors.primarySoft,
    },
    roleHelpText: {
        color: colors.textMuted,
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
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
        fontWeight: fontWeight.semibold,
    },
    codeCardContent: {
        gap: spacing.lg,
    },
    codeBox: {
        borderRadius: radius.lg,
        backgroundColor: colors.primary,
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
        color: colors.textLight,
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
        borderColor: colors.warning,
        backgroundColor: colors.warningSoft,
        padding: spacing.md,
    },
    expiryTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    expiryTitle: {
        color: colors.warning,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    expirySubtitle: {
        color: colors.warning,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    linkBox: {
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor: colors.border,
        backgroundColor: colors.surfaceSoft,
        padding: spacing.md,
        gap: spacing.xs,
    },
    linkLabel: {
        color: colors.textMuted,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.4,
    },
    linkText: {
        color: colors.text,
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
