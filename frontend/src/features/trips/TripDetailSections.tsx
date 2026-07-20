import { Ionicons } from "@expo/vector-icons";
import { Pressable, StyleSheet, Text, View } from "react-native";

import { UserAttribution } from "@/src/components/collaboration/UserAttribution";
import { AppCard } from "@/src/components/ui/AppCard";
import { NotificationBadge } from "@/src/components/ui/NotificationBadge";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { Destination } from "@/src/types/destination";
import type { Trip } from "@/src/types/trip";
import { formatDateTime } from "@/src/utils/dateFormat";

export function TripHeroContent({ trip }: Readonly<{ trip: Trip }>) {
    return (
        <>
            <View style={styles.heroIcon}>
                <Ionicons name="map" size={28} color={colors.textLight} />
            </View>
            <View style={styles.heroText}>
                <Text style={styles.destination} numberOfLines={1}>
                    {trip.destination || "No destination"}
                </Text>
                <Text style={styles.tripName}>{trip.tripName || "Untitled trip"}</Text>
            </View>
        </>
    );
}

type TripHeaderButtonProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    accessibilityLabel: string;
    onPress: () => void;
    badgeCount?: number;
}>;

export function TripHeaderButton({
    icon,
    accessibilityLabel,
    onPress,
    badgeCount = 0,
}: TripHeaderButtonProps) {
    const { colors: themeColors } = useAppTheme();

    return (
        <Pressable
            accessibilityRole="button"
            accessibilityLabel={accessibilityLabel}
            onPress={onPress}
            style={({ pressed }) => [
                styles.headerButton,
                { backgroundColor: themeColors.surface, borderColor: themeColors.border },
                pressed && styles.pressed,
            ]}
        >
            <View style={styles.headerButtonContent}>
                <Ionicons name={icon} size={23} color={themeColors.text} />
                <NotificationBadge count={badgeCount} size="small" />
            </View>
        </Pressable>
    );
}

type TripInfoCardProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    label: string;
    value: string;
}>;

export function TripInfoCard({ icon, label, value }: TripInfoCardProps) {
    const { colors: themeColors } = useAppTheme();

    return (
        <AppCard variant="soft" contentStyle={styles.infoContent}>
            <View style={[styles.infoIcon, { backgroundColor: themeColors.primarySoft }]}>
                <Ionicons name={icon} size={20} color={themeColors.primary} />
            </View>
            <View style={styles.infoText}>
                <Text style={[styles.infoLabel, { color: themeColors.textMuted }]}>{label}</Text>
                <Text style={[styles.infoValue, { color: themeColors.text }]}>{value}</Text>
            </View>
        </AppCard>
    );
}

export function TripDestinationCard({
    destination,
    onPress,
}: Readonly<{ destination: Destination; onPress: () => void }>) {
    const { colors: themeColors } = useAppTheme();

    return (
        <AppCard onPress={onPress} contentStyle={styles.destinationCard}>
            <View style={[styles.destinationIcon, { backgroundColor: themeColors.primarySoft }]}>
                <Ionicons name="location" size={22} color={themeColors.primary} />
            </View>
            <View style={styles.destinationContent}>
                <Text style={[styles.destinationTitle, { color: themeColors.text }]} numberOfLines={1}>
                    {destination.destinationName || "Untitled destination"}
                </Text>
                <Text style={[styles.destinationDate, { color: themeColors.textMuted }]}>
                    {formatDateTime(destination.startDate)} → {formatDateTime(destination.endDate)}
                </Text>
                {destination.notes ? (
                    <Text style={[styles.destinationNotes, { color: themeColors.textMuted }]} numberOfLines={2}>
                        {destination.notes}
                    </Text>
                ) : null}
            </View>
            <UserAttribution
                itemLabel="destination"
                createdBy={{
                    userId: destination.createdByUserId,
                    username: destination.createdByUsername,
                    displayName: destination.createdByDisplayName,
                    profileImageUrl: destination.createdByProfileImageUrl,
                }}
                modifiedBy={{
                    userId: destination.modifiedByUserId,
                    username: destination.modifiedByUsername,
                    displayName: destination.modifiedByDisplayName,
                    profileImageUrl: destination.modifiedByProfileImageUrl,
                }}
            />
            <Ionicons name="chevron-forward" size={22} color={themeColors.textMuted} />
        </AppCard>
    );
}

const styles = StyleSheet.create({
    heroIcon: {
        width: 58,
        height: 58,
        borderRadius: radius.xl,
        backgroundColor: "rgba(255,255,255,0.18)",
        alignItems: "center",
        justifyContent: "center",
    },
    heroText: { gap: spacing.sm },
    destination: { color: "#DBEAFE", fontSize: typography.bodySmall, fontWeight: fontWeight.bold },
    tripName: { color: colors.textLight, fontSize: typography.heading, lineHeight: 32, fontWeight: fontWeight.bold },
    headerButton: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        borderWidth: 1,
        alignItems: "center",
        justifyContent: "center",
        overflow: "visible",
    },
    headerButtonContent: { position: "relative", alignItems: "center", justifyContent: "center" },
    pressed: { opacity: 0.86, transform: [{ scale: 0.99 }] },
    infoContent: { flexDirection: "row", alignItems: "center", gap: spacing.md },
    infoIcon: { width: 42, height: 42, borderRadius: radius.md, alignItems: "center", justifyContent: "center" },
    infoText: { flex: 1, gap: spacing.xs },
    infoLabel: { fontSize: typography.caption, fontWeight: fontWeight.bold, textTransform: "uppercase", letterSpacing: 0.4 },
    infoValue: { fontSize: typography.bodySmall, lineHeight: 20, fontWeight: fontWeight.bold },
    destinationCard: { flexDirection: "row", alignItems: "center", gap: spacing.md },
    destinationIcon: { width: 44, height: 44, borderRadius: radius.lg, alignItems: "center", justifyContent: "center" },
    destinationContent: { flex: 1, gap: spacing.xs },
    destinationTitle: { fontSize: typography.body, fontWeight: fontWeight.bold },
    destinationDate: { fontSize: typography.caption, lineHeight: 18, fontWeight: fontWeight.semibold },
    destinationNotes: { fontSize: typography.bodySmall, lineHeight: 19 },
});
