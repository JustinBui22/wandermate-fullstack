import { Ionicons } from "@expo/vector-icons";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { UserAttribution } from "@/src/components/collaboration/UserAttribution";
import { AppCard } from "@/src/components/ui/AppCard";
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { Activity } from "@/src/types/activity";
import { formatDateTime } from "@/src/utils/dateFormat";

type HeaderButtonProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    accessibilityLabel: string;
    onPress: () => void;
}>;

export function DestinationHeaderButton({ icon, accessibilityLabel, onPress }: HeaderButtonProps) {
    const { colors } = useAppTheme();
    return (
        <Pressable
            accessibilityRole="button"
            accessibilityLabel={accessibilityLabel}
            onPress={onPress}
            style={({ pressed }) => [
                styles.headerButton,
                { backgroundColor: colors.surface, borderColor: colors.border },
                pressed && styles.pressed,
            ]}
        >
            <Ionicons name={icon} size={23} color={colors.text} />
        </Pressable>
    );
}

type DestinationInfoCardProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    label: string;
    value: string;
}>;

export function DestinationInfoCard({ icon, label, value }: DestinationInfoCardProps) {
    const { colors } = useAppTheme();
    return (
        <AppCard variant="soft" contentStyle={styles.infoCard}>
            <View style={[styles.infoIcon, { backgroundColor: colors.primarySoft }]}>
                <Ionicons name={icon} size={20} color={colors.primary} />
            </View>
            <View style={styles.infoText}>
                <Text style={[styles.infoLabel, { color: colors.textMuted }]}>{label}</Text>
                <Text style={[styles.infoValue, { color: colors.text }]}>{value}</Text>
            </View>
        </AppCard>
    );
}

export function DestinationActivityCard({
    activity,
    onPress,
}: Readonly<{ activity: Activity; onPress: () => void }>) {
    const { colors } = useAppTheme();
    return (
        <AppCard onPress={onPress} contentStyle={styles.activityCard}>
            <View style={[styles.activityIcon, { backgroundColor: colors.primarySoft }]}>
                <Ionicons name="walk" size={22} color={colors.primary} />
            </View>
            <View style={styles.activityContent}>
                <Text style={[styles.activityTitle, { color: colors.text }]} numberOfLines={1}>
                    {activity.activityName || "Untitled activity"}
                </Text>
                {activity.location ? (
                    <Text style={[styles.activityLocation, { color: colors.textMuted }]} numberOfLines={1}>
                        {activity.location}
                    </Text>
                ) : null}
                <Text style={[styles.activityTime, { color: colors.textMuted }]} numberOfLines={2}>
                    {formatDateTime(activity.startDateTime)} → {formatDateTime(activity.endDateTime)}
                </Text>
                {activity.description ? (
                    <Text style={[styles.activityDescription, { color: colors.textMuted }]} numberOfLines={2}>
                        {activity.description}
                    </Text>
                ) : null}
                <UserAttribution
                    itemLabel="activity"
                    createdBy={{
                        userId: activity.createdByUserId,
                        username: activity.createdByUsername,
                        displayName: activity.createdByDisplayName,
                        profileImageUrl: activity.createdByProfileImageUrl,
                    }}
                    modifiedBy={{
                        userId: activity.modifiedByUserId,
                        username: activity.modifiedByUsername,
                        displayName: activity.modifiedByDisplayName,
                        profileImageUrl: activity.modifiedByProfileImageUrl,
                    }}
                />
            </View>
            <Ionicons name="chevron-forward" size={22} color={colors.textMuted} />
        </AppCard>
    );
}

const styles = StyleSheet.create({
    headerButton: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        borderWidth: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    pressed: { opacity: 0.86, transform: [{ scale: 0.99 }] },
    infoCard: { flexDirection: "row", alignItems: "center", gap: spacing.md },
    infoIcon: { width: 42, height: 42, borderRadius: radius.md, alignItems: "center", justifyContent: "center" },
    infoText: { flex: 1, gap: spacing.xs },
    infoLabel: { fontSize: typography.caption, fontWeight: fontWeight.bold, textTransform: "uppercase", letterSpacing: 0.4 },
    infoValue: { fontSize: typography.bodySmall, lineHeight: 20, fontWeight: fontWeight.bold },
    activityCard: { flexDirection: "row", alignItems: "center", gap: spacing.md },
    activityIcon: { width: 44, height: 44, borderRadius: radius.lg, alignItems: "center", justifyContent: "center" },
    activityContent: { flex: 1, gap: spacing.xs },
    activityTitle: { fontSize: typography.body, fontWeight: fontWeight.bold },
    activityLocation: { fontSize: typography.caption, fontWeight: fontWeight.semibold },
    activityTime: { fontSize: typography.caption, lineHeight: 18, fontWeight: fontWeight.semibold },
    activityDescription: { fontSize: typography.bodySmall, lineHeight: 19 },
});
