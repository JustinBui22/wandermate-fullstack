import { Pressable, StyleSheet, Text, View } from "react-native";
import { useRouter } from "expo-router";
import { Ionicons } from "@expo/vector-icons";

import { useAuthStore } from "@/src/stores/authStore";
import { colors, radius, shadow, spacing } from "@/src/theme/theme";

export default function HomeScreen() {
    const router = useRouter();
    const { logoutUser } = useAuthStore();

    async function handleLogout() {
        await logoutUser();
        router.replace("/login");
    }

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <View>
                    <Text style={styles.greeting}>Welcome back</Text>
                    <Text style={styles.title}>Ready for your next trip?</Text>
                </View>

                <Pressable onPress={handleLogout} style={styles.iconButton}>
                    <Ionicons name="log-out-outline" size={22} color={colors.text} />
                </Pressable>
            </View>

            <View style={styles.heroCard}>
                <Text style={styles.heroTitle}>Plan smarter trips</Text>
                <Text style={styles.heroSubtitle}>
                    Create, manage and organize your travel plans from one place.
                </Text>
            </View>

            <Pressable style={styles.actionCard} onPress={() => router.push("/trips")}>
                <View style={styles.actionIcon}>
                    <Ionicons name="map-outline" size={24} color={colors.primary} />
                </View>

                <View style={{ flex: 1 }}>
                    <Text style={styles.actionTitle}>Manage trips</Text>
                    <Text style={styles.actionSubtitle}>View your current travel plans</Text>
                </View>

                <Ionicons name="chevron-forward" size={22} color={colors.mutedText} />
            </Pressable>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: spacing.lg,
        backgroundColor: colors.background,
    },
    header: {
        marginTop: spacing.xl,
        marginBottom: spacing.lg,
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
    },
    greeting: {
        fontSize: 15,
        color: colors.mutedText,
        marginBottom: 4,
    },
    title: {
        fontSize: 26,
        fontWeight: "800",
        color: colors.text,
    },
    iconButton: {
        width: 44,
        height: 44,
        borderRadius: 16,
        backgroundColor: colors.card,
        alignItems: "center",
        justifyContent: "center",
        ...shadow.card,
    },
    heroCard: {
        backgroundColor: colors.primary,
        borderRadius: radius.xl,
        padding: spacing.lg,
        marginBottom: spacing.lg,
    },
    heroTitle: {
        color: "#FFFFFF",
        fontSize: 24,
        fontWeight: "800",
        marginBottom: spacing.sm,
    },
    heroSubtitle: {
        color: "#DBEAFE",
        fontSize: 15,
        lineHeight: 22,
    },
    actionCard: {
        backgroundColor: colors.card,
        borderRadius: radius.lg,
        padding: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
        ...shadow.card,
    },
    actionIcon: {
        width: 52,
        height: 52,
        borderRadius: 18,
        backgroundColor: colors.softBlue,
        alignItems: "center",
        justifyContent: "center",
    },
    actionTitle: {
        fontSize: 17,
        fontWeight: "800",
        color: colors.text,
    },
    actionSubtitle: {
        fontSize: 14,
        color: colors.mutedText,
        marginTop: 3,
    },
});