import { useCallback, useState } from "react";
import { Alert, Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useLocalSearchParams, useRouter } from "expo-router";

import { getTripById } from "@/src/api/tripApi";
import { sendTripInvitation } from "@/src/api/tripCollaborationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { colors as staticColors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { TripCollaborationRole } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";

type InvitableRole = Exclude<TripCollaborationRole, "OWNER">;
const ROLES: InvitableRole[] = ["VIEWER", "EDITOR"];

export default function InviteMemberScreen() {
    const router = useRouter();
    const theme = useAppTheme();
    const colors = theme.colors;
    const params = useLocalSearchParams();
    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripId);

    const [username, setUsername] = useState("");
    const [role, setRole] = useState<InvitableRole>("VIEWER");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isCheckingAccess, setIsCheckingAccess] = useState(true);
    const [isOwner, setIsOwner] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useFocusEffect(
        useCallback(() => {
            async function checkAccess() {
                if (!hasValidTripId) {
                    setError("Trip ID is missing or invalid.");
                    setIsOwner(false);
                    setIsCheckingAccess(false);
                    return;
                }

                try {
                    setError(null);
                    setIsCheckingAccess(true);

                    const trip = await getTripById(tripId);
                    const owner = trip.currentUserRole === "OWNER";
                    setIsOwner(owner);

                    if (!owner) {
                        setError("Only the trip owner can invite members.");
                    }
                } catch (error: unknown) {
                    setIsOwner(false);
                    setError(getApiErrorMessage(error, "Could not verify your access for this trip."));
                } finally {
                    setIsCheckingAccess(false);
                }
            }

            void checkAccess();
        }, [hasValidTripId, tripId])
    );

    async function handleSendInvitation() {
        const targetUsername = username.trim();

        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
            return;
        }

        if (!isOwner) {
            Alert.alert("Owner only", "Only the trip owner can invite members.");
            return;
        }

        if (!targetUsername) {
            Alert.alert("Missing username", "Enter the username you want to invite.");
            return;
        }

        try {
            setIsSubmitting(true);
            await sendTripInvitation(tripId, { username: targetUsername, role });
            setUsername("");
            setRole("VIEWER");

            Alert.alert(
                "Invitation sent",
                `${targetUsername} has been invited as ${role.toLowerCase()}.`
            );
        } catch (error: unknown) {
            Alert.alert(
                "Invitation failed",
                getApiErrorMessage(error, "Please check the username and try again.")
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    if (isCheckingAccess) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Checking access..."
                    subtitle="Confirming whether you can invite members."
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
                        <Text style={[styles.eyebrow, { color: colors.primary }]}>Invite member</Text>
                        <Text style={[styles.title, { color: colors.text }]}>Owner only</Text>
                        <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                            This page is only available to the trip owner.
                        </Text>
                    </View>
                </View>

                <ErrorMessage message={error} title="Access denied" />

                <EmptyState
                    title="You cannot invite members"
                    message="Ask the trip owner to invite members or change sharing settings."
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
                    <Text style={[styles.eyebrow, { color: colors.primary }]}>Invite member</Text>
                    <Text style={[styles.title, { color: colors.text }]}>Invite by username</Text>
                    <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                        Send a direct invitation to a WanderMate user.
                    </Text>
                </View>
            </View>

            <AppCard contentStyle={styles.formContent}>
                <AppInput
                    label="Username"
                    value={username}
                    onChangeText={setUsername}
                    autoCapitalize="none"
                    autoCorrect={false}
                    placeholder="Enter username"
                    helperText="Enter the exact WanderMate username of the person you want to invite."
                    style={{ backgroundColor: colors.surfaceSoft, borderColor: colors.borderStrong }}
                    leftIcon={<Ionicons name="person-outline" size={20} color={colors.textMuted} />}
                />

                <View style={styles.roleSection}>
                    <Text style={[styles.roleLabel, { color: colors.text }]}>Role</Text>
                    <View style={styles.roleRow}>
                        {ROLES.map((item) => (
                            <Pressable
                                key={item}
                                accessibilityRole="button"
                                onPress={() => setRole(item)}
                                style={({ pressed }) => [
                                    styles.roleChip,
                                    { backgroundColor: colors.surface, borderColor: colors.border },
                                    role === item && { backgroundColor: colors.primarySoft, borderColor: colors.primary },
                                    pressed && styles.pressed,
                                ]}
                            >
                                <RoleBadge role={item} />
                                <Text style={[styles.roleHelpText, { color: colors.textMuted }]}>
                                    {item === "VIEWER" ? "Can view the trip" : "Can edit trip details"}
                                </Text>
                            </Pressable>
                        ))}
                    </View>
                </View>

                <AppButton
                    title="Send Invitation"
                    onPress={() => {
                        void handleSendInvitation();
                    }}
                    loading={isSubmitting}
                    leftIcon={<Ionicons name="send-outline" size={19} color={colors.textLight} />}
                />
            </AppCard>
        </AppScreen>
    );
}

function HeaderButton({ onPress }: { onPress: () => void }) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <AppCard onPress={onPress} style={styles.backButton} contentStyle={styles.backButtonContent}>
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
    pressed: {
        opacity: 0.86,
        transform: [{ scale: 0.99 }],
    },
});