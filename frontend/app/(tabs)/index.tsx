import { useState } from "react";
import {
    Alert,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";

import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import { useAuthStore } from "@/src/stores/authStore";

export default function HomeScreen() {
    const router = useRouter();

    const theme = useAppTheme();
    const colors = theme.colors;

    const { logoutUser, username } = useAuthStore();
    const [isLoggingOut, setIsLoggingOut] = useState(false);

    async function performLogout() {
        try {
            setIsLoggingOut(true);
            await logoutUser();
            router.replace("/login" as any);
        } finally {
            setIsLoggingOut(false);
        }
    }

    function handleLogout() {
        Alert.alert(
            "Log out",
            "Are you sure you want to log out of WanderMate?",
            [
                { text: "Cancel", style: "cancel" },
                {
                    text: "Log out",
                    style: "destructive",
                    onPress: () => {
                        void performLogout();
                    },
                },
            ]
        );
    }

    function handleOpenTrips() {
        router.push("/trips" as any);
    }

    function handleCreateTrip() {
        router.push("/trips/create" as any);
    }

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <View style={styles.headerTextGroup}>
                    <Text style={[styles.eyebrow, { color: colors.primary }]}>
                        WanderMate
                    </Text>

                    <Text style={[styles.title, { color: colors.text }]}>
                        Welcome back{username ? `, ${username}` : ""}
                    </Text>

                    <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                        Ready to plan your next trip?
                    </Text>
                </View>

                <Pressable
                    accessibilityRole="button"
                    accessibilityLabel="Log out"
                    onPress={handleLogout}
                    disabled={isLoggingOut}
                    style={({ pressed }) => [
                        styles.logoutButton,
                        {
                            backgroundColor: colors.surface,
                            borderColor: colors.border,
                        },
                        pressed && styles.pressed,
                        isLoggingOut && styles.disabled,
                    ]}
                >
                    <Ionicons
                        name="log-out-outline"
                        size={22}
                        color={colors.text}
                    />
                </Pressable>
            </View>

            <AppCard
                style={[
                    styles.heroCard,
                    { backgroundColor: colors.primary },
                ]}
                contentStyle={styles.heroCardContent}
            >
                <View style={styles.heroIconBadge}>
                    <Ionicons
                        name="airplane"
                        size={30}
                        color={colors.textLight}
                    />
                </View>

                <View style={styles.heroTextGroup}>
                    <Text style={[styles.heroTitle, { color: colors.textLight }]}>
                        Plan smarter trips
                    </Text>

                    <Text style={styles.heroSubtitle}>
                        Create trips, organise destinations, and schedule activities from one clean workspace.
                    </Text>
                </View>

                <AppButton
                    title="Create new trip"
                    onPress={handleCreateTrip}
                    variant="secondary"
                    rightIcon={
                        <Ionicons
                            name="add-circle-outline"
                            size={20}
                            color={colors.primaryDark}
                        />
                    }
                />
            </AppCard>

            <View style={styles.quickActions}>
                <Text style={[styles.sectionTitle, { color: colors.text }]}>
                    Quick actions
                </Text>

                <ActionCard
                    icon="map-outline"
                    title="Manage trips"
                    subtitle="View, edit, or delete your travel plans."
                    onPress={handleOpenTrips}
                />

                <ActionCard
                    icon="add-circle-outline"
                    title="Start a new trip"
                    subtitle="Create a trip with date conflict checks."
                    onPress={handleCreateTrip}
                />
            </View>

            <AppCard variant="soft" contentStyle={styles.infoCardContent}>
                <View
                    style={[
                        styles.infoIconBadge,
                        { backgroundColor: colors.successSoft },
                    ]}
                >
                    <Ionicons
                        name="shield-checkmark-outline"
                        size={22}
                        color={colors.success}
                    />
                </View>

                <View style={styles.infoTextGroup}>
                    <Text style={[styles.infoTitle, { color: colors.text }]}>
                        Protected account session
                    </Text>

                    <Text style={[styles.infoSubtitle, { color: colors.textMuted }]}>
                        Your trips are connected to your signed-in account and protected by token-based authentication.
                    </Text>
                </View>
            </AppCard>
        </AppScreen>
    );
}

type ActionCardProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    title: string;
    subtitle: string;
    onPress: () => void;
}>;

function ActionCard({ icon, title, subtitle, onPress }: ActionCardProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <AppCard onPress={onPress} contentStyle={styles.actionCardContent}>
            <View
                style={[
                    styles.actionIconBadge,
                    { backgroundColor: colors.primarySoft },
                ]}
            >
                <Ionicons
                    name={icon}
                    size={24}
                    color={colors.primary}
                />
            </View>

            <View style={styles.actionTextGroup}>
                <Text style={[styles.actionTitle, { color: colors.text }]}>
                    {title}
                </Text>

                <Text style={[styles.actionSubtitle, { color: colors.textMuted }]}>
                    {subtitle}
                </Text>
            </View>

            <Ionicons
                name="chevron-forward"
                size={22}
                color={colors.textMuted}
            />
        </AppCard>
    );
}

const styles = StyleSheet.create({
    screenContent: {
        paddingTop: spacing.xl,
        paddingBottom: spacing.xxl,
        gap: spacing.lg,
    },
    header: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.md,
    },
    headerTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    eyebrow: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.7,
    },
    title: {
        fontSize: typography.hero,
        fontWeight: fontWeight.bold,
        lineHeight: 38,
    },
    subtitle: {
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    logoutButton: {
        width: 46,
        height: 46,
        borderRadius: radius.lg,
        borderWidth: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    pressed: {
        opacity: 0.86,
        transform: [{ scale: 0.99 }],
    },
    disabled: {
        opacity: 0.55,
    },
    heroCard: {
        borderWidth: 0,
    },
    heroCardContent: {
        padding: spacing.xl,
        gap: spacing.lg,
    },
    heroIconBadge: {
        width: 62,
        height: 62,
        borderRadius: radius.xl,
        backgroundColor: "rgba(255,255,255,0.18)",
        alignItems: "center",
        justifyContent: "center",
    },
    heroTextGroup: {
        gap: spacing.sm,
    },
    heroTitle: {
        fontSize: typography.heading,
        lineHeight: 32,
        fontWeight: fontWeight.bold,
    },
    heroSubtitle: {
        color: "#DBEAFE",
        fontSize: typography.bodySmall,
        lineHeight: 22,
        fontWeight: fontWeight.semibold,
    },
    quickActions: {
        gap: spacing.md,
    },
    sectionTitle: {
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    actionCardContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    actionIconBadge: {
        width: 50,
        height: 50,
        borderRadius: radius.lg,
        alignItems: "center",
        justifyContent: "center",
    },
    actionTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    actionTitle: {
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    actionSubtitle: {
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    infoCardContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    infoIconBadge: {
        width: 46,
        height: 46,
        borderRadius: radius.lg,
        alignItems: "center",
        justifyContent: "center",
    },
    infoTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    infoTitle: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    infoSubtitle: {
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
});