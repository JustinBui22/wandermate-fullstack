import { StyleSheet, Text, View } from "react-native";

import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type { TripCollaborationRole } from "@/src/types/tripCollaboration";
import { getRoleLabel } from "@/src/utils/tripRoleUtils";

type RoleBadgeProps = Readonly<{
    role: TripCollaborationRole | null;
}>;

export function RoleBadge({ role }: RoleBadgeProps) {
    return (
        <View style={[styles.badge, role === "OWNER" && styles.ownerBadge, role === "EDITOR" && styles.editorBadge, role === "VIEWER" && styles.viewerBadge]}>
            <Text style={[styles.text, role === "OWNER" && styles.ownerText, role === "EDITOR" && styles.editorText, role === "VIEWER" && styles.viewerText]}>
                {getRoleLabel(role)}
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    badge: {
        alignSelf: "flex-start",
        borderRadius: radius.pill,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.xs,
        backgroundColor: colors.softGray,
    },
    ownerBadge: {
        backgroundColor: colors.primarySoft,
    },
    editorBadge: {
        backgroundColor: colors.successSoft,
    },
    viewerBadge: {
        backgroundColor: colors.warningSoft,
    },
    text: {
        color: colors.textMuted,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.4,
    },
    ownerText: {
        color: colors.primaryDark,
    },
    editorText: {
        color: colors.success,
    },
    viewerText: {
        color: colors.warning,
    },
});
