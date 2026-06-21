import { useState } from "react";
import { Alert, Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useLocalSearchParams, useRouter } from "expo-router";

import { sendTripInvitation } from "@/src/api/tripCollaborationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type { TripCollaborationRole } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";

type InvitableRole = Exclude<TripCollaborationRole, "OWNER">;
const ROLES: InvitableRole[] = ["VIEWER", "EDITOR"];

export default function InviteMemberScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();
    const tripIdParam = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;
    const tripId = Number(tripIdParam);
    const hasValidTripId = Boolean(tripIdParam) && !Number.isNaN(tripId);

    const [username, setUsername] = useState("");
    const [role, setRole] = useState<InvitableRole>("VIEWER");
    const [isSubmitting, setIsSubmitting] = useState(false);

    async function handleSendInvitation() {
        const targetUsername = username.trim();

        if (!hasValidTripId) {
            Alert.alert("Missing trip", "Trip ID is missing or invalid.");
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
        } catch (error: any) {
            Alert.alert(
                "Invitation failed",
                getApiErrorMessage(error, "Please check the username and try again.")
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <HeaderButton onPress={() => router.back()} />
                <View style={styles.headerTextGroup}>
                    <Text style={styles.eyebrow}>Invite member</Text>
                    <Text style={styles.title}>Invite by username</Text>
                    <Text style={styles.subtitle}>
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
                    leftIcon={<Ionicons name="person-outline" size={20} color={colors.textMuted} />}
                />

                <View style={styles.roleSection}>
                    <Text style={styles.roleLabel}>Role</Text>
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
                                    {item === "VIEWER" ? "Can view the trip" : "Can edit trip details"}
                                </Text>
                            </Pressable>
                        ))}
                    </View>
                </View>

                <AppButton
                    title="Send Invitation"
                    onPress={handleSendInvitation}
                    loading={isSubmitting}
                    leftIcon={<Ionicons name="send-outline" size={19} color={colors.textLight} />}
                />
            </AppCard>
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
    pressed: {
        opacity: 0.86,
        transform: [{ scale: 0.99 }],
    },
});
