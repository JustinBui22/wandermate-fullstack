import { StyleSheet, Text, View } from "react-native";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { TripCollaborationRole } from "@/src/types/tripCollaboration";
import { getRoleLabel } from "@/src/utils/tripRoleUtils";

type RoleBadgeProps = Readonly<{
    role: TripCollaborationRole | null;
}>;

export function RoleBadge({ role }: RoleBadgeProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    const backgroundColor =
        role === "OWNER"
            ? colors.primarySoft
            : role === "EDITOR"
              ? colors.successSoft
              : role === "VIEWER"
                ? colors.warningSoft
                : colors.surfaceSoft;

    const textColor =
        role === "OWNER"
            ? colors.primary
            : role === "EDITOR"
              ? colors.success
              : role === "VIEWER"
                ? colors.warning
                : colors.textMuted;

    return (
        <View style={[styles.badge, { backgroundColor, borderColor: colors.border }]}>
            <Text style={[styles.text, { color: textColor }]}>{getRoleLabel(role)}</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    badge: {
        alignSelf: "flex-start",
        borderRadius: radius.pill,
        borderWidth: 1,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.xs,
    },
    text: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.4,
    },
});
