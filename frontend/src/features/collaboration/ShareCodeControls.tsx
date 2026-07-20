import { Ionicons } from "@expo/vector-icons";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { TripShareCode } from "@/src/types/tripCollaboration";
import { formatDateTime } from "@/src/utils/dateFormat";

export type InvitableRole = "EDITOR" | "VIEWER";

export function ShareCodeRoleSelector({
    role,
    onRoleChange,
}: Readonly<{ role: InvitableRole; onRoleChange: (role: InvitableRole) => void }>) {
    const { colors } = useAppTheme();
    const roles: InvitableRole[] = ["VIEWER", "EDITOR"];

    return (
        <View style={styles.roleSection}>
            <Text style={[styles.roleLabel, { color: colors.text }]}>Default role for this code</Text>
            <View style={styles.roleRow}>
                {roles.map((item) => (
                    <Pressable
                        key={item}
                        accessibilityRole="button"
                        accessibilityState={{ selected: role === item }}
                        onPress={() => onRoleChange(item)}
                        style={({ pressed }) => [
                            styles.roleChip,
                            { backgroundColor: colors.surface, borderColor: colors.border },
                            role === item && { backgroundColor: colors.primarySoft, borderColor: colors.primary },
                            pressed && styles.pressed,
                        ]}
                    >
                        <RoleBadge role={item} />
                        <Text style={[styles.roleHelp, { color: colors.textMuted }]}>
                            {item === "VIEWER" ? "View-only access" : "Can edit trip content"}
                        </Text>
                    </Pressable>
                ))}
            </View>
        </View>
    );
}

type CurrentShareCodeCardProps = Readonly<{
    shareCode: TripShareCode;
    isSharing: boolean;
    isRegenerating: boolean;
    onCopyCode: () => void;
    onCopyLink: () => void;
    onShare: () => void;
    onRegenerate: () => void;
}>;

export function CurrentShareCodeCard({
    shareCode,
    isSharing,
    isRegenerating,
    onCopyCode,
    onCopyLink,
    onShare,
    onRegenerate,
}: CurrentShareCodeCardProps) {
    const { colors } = useAppTheme();

    return (
        <AppCard
            title="Current invite"
            subtitle="Use the code for manual entry or the link for deep-link opening."
            contentStyle={styles.codeCard}
        >
            <View style={[styles.codeBox, { backgroundColor: colors.primary }]}>
                <Text style={styles.codeLabel}>Invite code</Text>
                <Text style={[styles.codeText, { color: colors.textLight }]}>{shareCode.code}</Text>
            </View>

            <View style={[styles.expiryBox, { borderColor: colors.warning, backgroundColor: colors.warningSoft }]}>
                <Ionicons name="time-outline" size={22} color={colors.warning} />
                <View style={styles.expiryText}>
                    <Text style={[styles.expiryTitle, { color: colors.warning }]}>
                        Expires {formatDateTime(shareCode.expiresAt)}
                    </Text>
                    <Text style={[styles.expirySubtitle, { color: colors.warning }]}>
                        This code is single-use and becomes invalid if another code is generated.
                    </Text>
                </View>
            </View>

            <View style={[styles.linkBox, { backgroundColor: colors.surfaceSoft, borderColor: colors.border }]}>
                <Text style={[styles.linkLabel, { color: colors.textMuted }]}>Deep link</Text>
                <Text style={[styles.linkText, { color: colors.text }]} numberOfLines={2}>
                    {shareCode.inviteLink}
                </Text>
            </View>

            <View style={styles.actionGrid}>
                <AppButton title="Copy Code" onPress={onCopyCode} variant="outline" fullWidth={false} style={styles.actionButton} />
                <AppButton title="Copy Link" onPress={onCopyLink} variant="outline" fullWidth={false} style={styles.actionButton} />
            </View>
            <AppButton
                title="Share Invite Message"
                onPress={onShare}
                loading={isSharing}
                variant="outline"
                leftIcon={<Ionicons name="share-social-outline" size={19} color={colors.primary} />}
            />
            <AppButton
                title="Regenerate Invite Code"
                onPress={onRegenerate}
                loading={isRegenerating}
                leftIcon={<Ionicons name="refresh-outline" size={19} color={colors.textLight} />}
            />
        </AppCard>
    );
}

const styles = StyleSheet.create({
    roleSection: { gap: spacing.md },
    roleLabel: { fontSize: typography.bodySmall, fontWeight: fontWeight.bold },
    roleRow: { gap: spacing.md },
    roleChip: { borderWidth: 1, borderRadius: radius.lg, padding: spacing.md, gap: spacing.sm },
    roleHelp: { fontSize: typography.caption, lineHeight: 18 },
    pressed: { opacity: 0.86, transform: [{ scale: 0.995 }] },
    codeCard: { gap: spacing.lg },
    codeBox: { borderRadius: radius.xl, paddingVertical: spacing.xl, paddingHorizontal: spacing.lg, alignItems: "center", gap: spacing.sm },
    codeLabel: { color: "#DBEAFE", fontSize: typography.caption, fontWeight: fontWeight.bold, textTransform: "uppercase", letterSpacing: 0.8 },
    codeText: { fontSize: 32, lineHeight: 38, fontWeight: fontWeight.bold, letterSpacing: 3 },
    expiryBox: { borderWidth: 1, borderRadius: radius.lg, padding: spacing.md, flexDirection: "row", alignItems: "flex-start", gap: spacing.md },
    expiryText: { flex: 1, gap: spacing.xs },
    expiryTitle: { fontSize: typography.bodySmall, fontWeight: fontWeight.bold },
    expirySubtitle: { fontSize: typography.caption, lineHeight: 18, fontWeight: fontWeight.semibold },
    linkBox: { borderWidth: 1, borderRadius: radius.lg, padding: spacing.md, gap: spacing.xs },
    linkLabel: { fontSize: typography.caption, fontWeight: fontWeight.bold, textTransform: "uppercase", letterSpacing: 0.5 },
    linkText: { fontSize: typography.bodySmall, lineHeight: 20, fontWeight: fontWeight.semibold },
    actionGrid: { flexDirection: "row", gap: spacing.md },
    actionButton: { flex: 1 },
});
