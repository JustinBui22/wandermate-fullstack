import { StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";

import { AppCard } from "@/src/components/ui/AppCard";
import { colors, fontWeight, spacing, typography } from "@/src/constants/theme";
import type { MyTripOverlapWarning } from "@/src/types/tripCollaboration";
import { formatDateTime } from "@/src/utils/dateFormat";

type OverlapWarningCardProps = Readonly<{
    warnings: MyTripOverlapWarning[];
}>;

export function OverlapWarningCard({ warnings }: OverlapWarningCardProps) {
    if (warnings.length === 0) {
        return null;
    }

    return (
        <AppCard style={styles.card} contentStyle={styles.content}>
            <View style={styles.headerRow}>
                <Ionicons name="warning-outline" size={22} color={colors.warning} />
                <View style={styles.headerTextGroup}>
                    <Text style={styles.title}>Schedule overlap</Text>
                    <Text style={styles.subtitle}>Only you can see this warning.</Text>
                </View>
            </View>

            <View style={styles.warningList}>
                {warnings.map((warning) => (
                    <View key={`${warning.currentTripId}-${warning.overlappingTripId}-${warning.overlapStartDate}`} style={styles.warningItem}>
                        <Text style={styles.warningTripName}>{warning.overlappingTripName}</Text>
                        <Text style={styles.warningTime}>
                            {formatDateTime(warning.overlapStartDate)} → {formatDateTime(warning.overlapEndDate)}
                        </Text>
                    </View>
                ))}
            </View>
        </AppCard>
    );
}

const styles = StyleSheet.create({
    card: {
        borderColor: colors.warningSoft,
        backgroundColor: "#FFFBEB",
    },
    content: {
        gap: spacing.md,
    },
    headerRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    headerTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    title: {
        color: colors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    subtitle: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    warningList: {
        gap: spacing.sm,
    },
    warningItem: {
        gap: spacing.xs,
    },
    warningTripName: {
        color: colors.text,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    warningTime: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
});
