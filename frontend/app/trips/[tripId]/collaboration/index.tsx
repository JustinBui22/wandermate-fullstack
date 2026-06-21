import { StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useLocalSearchParams, useRouter } from "expo-router";

import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";

export default function TripCollaborationMenuScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();
    const tripId = Array.isArray(params.tripId) ? params.tripId[0] : params.tripId;

    function push(path: string) {
        if (!tripId) return;
        router.push(`/trips/${tripId}/collaboration/${path}` as any);
    }

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <IconButton icon="chevron-back" onPress={() => router.back()} />

                <View style={styles.headerTextGroup}>
                    <Text style={styles.eyebrow}>Trip collaboration</Text>
                    <Text style={styles.title}>Manage sharing</Text>
                    <Text style={styles.subtitle}>
                        Invite people, manage requests, and control who can access this trip.
                    </Text>
                </View>
            </View>

            <View style={styles.optionList}>
                <CollaborationOption
                    icon="person-add-outline"
                    title="Invite member"
                    subtitle="Invite a user directly by username."
                    badge="Owner"
                    onPress={() => push("invite")}
                />

                <CollaborationOption
                    icon="link-outline"
                    title="Invite code / link"
                    subtitle="Generate a single-use code or deep link."
                    badge="New"
                    onPress={() => push("share-code")}
                />

                <CollaborationOption
                    icon="mail-unread-outline"
                    title="Pending join requests"
                    subtitle="Accept or reject users who requested access."
                    onPress={() => push("requests")}
                />

                <CollaborationOption
                    icon="people-outline"
                    title="Members"
                    subtitle="View members, update roles, or remove access."
                    onPress={() => push("members")}
                />
            </View>
        </AppScreen>
    );
}

type IconButtonProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    onPress: () => void;
}>;

function IconButton({ icon, onPress }: IconButtonProps) {
    return (
        <AppCard onPress={onPress} style={styles.backButton} contentStyle={styles.backButtonContent}>
            <Ionicons name={icon} size={22} color={colors.text} />
        </AppCard>
    );
}

type CollaborationOptionProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    title: string;
    subtitle: string;
    badge?: string;
    onPress: () => void;
}>;

function CollaborationOption({ icon, title, subtitle, badge, onPress }: CollaborationOptionProps) {
    return (
        <AppCard onPress={onPress} contentStyle={styles.optionContent}>
            <View style={styles.optionIconBadge}>
                <Ionicons name={icon} size={23} color={colors.primary} />
            </View>

            <View style={styles.optionTextGroup}>
                <View style={styles.optionTitleRow}>
                    <Text style={styles.optionTitle}>{title}</Text>
                    {badge ? (
                        <View style={styles.badge}>
                            <Text style={styles.badgeText}>{badge}</Text>
                        </View>
                    ) : null}
                </View>

                <Text style={styles.optionSubtitle}>{subtitle}</Text>
            </View>

            <Ionicons name="chevron-forward" size={22} color={colors.textMuted} />
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
        fontSize: typography.hero,
        fontWeight: fontWeight.bold,
        lineHeight: 38,
    },
    subtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 21,
    },
    optionList: {
        gap: spacing.md,
    },
    optionContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    optionIconBadge: {
        width: 48,
        height: 48,
        borderRadius: radius.lg,
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    optionTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    optionTitleRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
    },
    optionTitle: {
        color: colors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    optionSubtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    badge: {
        borderRadius: radius.pill,
        backgroundColor: colors.primarySoft,
        paddingHorizontal: spacing.sm,
        paddingVertical: spacing.xs,
    },
    badgeText: {
        color: colors.primary,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
    },
});
