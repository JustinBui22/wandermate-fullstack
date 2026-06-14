import { useState } from "react";
import {
    Alert,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { LinearGradient } from "expo-linear-gradient";
import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";

import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { useAuthStore } from "@/src/stores/authStore";
import { colors, fontWeight, radius, shadows, spacing, typography } from "@/src/constants/theme";

const MAX_SESSIONS_REACHED_CODE = "E022";

export default function LoginScreen() {
    const router = useRouter();
    const { loginUser, isLoading, error, errorCode, clearError } = useAuthStore();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);

    async function handleLogin(overrideMaxSession = false) {
        clearError();

        if (!username.trim() || !password.trim()) {
            Alert.alert("Missing details", "Please enter your username and password.");
            return;
        }

        const success = await loginUser({
            username: username.trim(),
            password,
            overrideMaxSession,
        });

        if (success) {
            router.replace("/");
            return;
        }

        const latestErrorCode = useAuthStore.getState().errorCode;

        if (!overrideMaxSession && latestErrorCode === MAX_SESSIONS_REACHED_CODE) {
            Alert.alert(
                "Too many active sessions",
                "Your account is already signed in on the maximum number of devices. Continue to sign in here and remove the oldest session?",
                [
                    { text: "Cancel", style: "cancel" },
                    {
                        text: "Continue",
                        style: "destructive",
                        onPress: () => handleLogin(true),
                    },
                ]
            );
        }
    }

    return (
        <LinearGradient
            colors={[colors.primarySoft, colors.background, colors.surface]}
            style={styles.gradient}
        >
            <AppScreen
                centerContent
                keyboardAvoiding
                safeAreaStyle={styles.transparentBackground}
                style={styles.transparentBackground}
                contentContainerStyle={styles.content}
            >
                <View style={styles.heroSection}>
                    <View style={styles.logoBadge}>
                        <Ionicons name="airplane" size={28} color={colors.primary} />
                    </View>

                    <Text style={styles.appName}>WanderMate</Text>
                    <Text style={styles.heroTitle}>Plan smarter trips</Text>
                    <Text style={styles.heroSubtitle}>
                        Sign in to manage your travel plans, activities and itineraries in one place.
                    </Text>
                </View>

                <AppCard
                    title="Welcome back"
                    subtitle="Continue your journey"
                    style={styles.card}
                    contentStyle={styles.cardContent}
                >
                    <AppInput
                        label="Username"
                        value={username}
                        onChangeText={setUsername}
                        placeholder="Enter username"
                        autoCapitalize="none"
                        autoCorrect={false}
                        textContentType="username"
                        leftIcon={<Ionicons name="person-outline" size={20} color={colors.textMuted} />}
                    />

                    <AppInput
                        label="Password"
                        value={password}
                        onChangeText={setPassword}
                        placeholder="Enter password"
                        secureTextEntry={!isPasswordVisible}
                        textContentType="password"
                        leftIcon={<Ionicons name="lock-closed-outline" size={20} color={colors.textMuted} />}
                        rightIcon={
                            <Pressable
                                accessibilityRole="button"
                                accessibilityLabel={isPasswordVisible ? "Hide password" : "Show password"}
                                hitSlop={10}
                                onPress={() => setIsPasswordVisible((current) => !current)}
                            >
                                <Ionicons
                                    name={isPasswordVisible ? "eye-off-outline" : "eye-outline"}
                                    size={20}
                                    color={colors.textMuted}
                                />
                            </Pressable>
                        }
                    />

                    <ErrorMessage
                        message={errorCode !== MAX_SESSIONS_REACHED_CODE ? error : null}
                        title="Login failed"
                    />

                    <AppButton
                        title="Sign in"
                        loading={isLoading}
                        onPress={() => handleLogin(false)}
                        rightIcon={!isLoading ? <Ionicons name="arrow-forward" size={19} color={colors.textLight} /> : null}
                        size="lg"
                    />

                    <View style={styles.linkRow}>
                        <Pressable onPress={() => router.push("/(auth)/forgot-password" as any)}>
                            <Text style={styles.linkText}>Forgot password?</Text>
                        </Pressable>

                        <Text style={styles.linkDivider}>•</Text>

                        <Pressable onPress={() => router.push("/(auth)/register" as any)}>
                            <Text style={styles.linkText}>Create account</Text>
                        </Pressable>
                    </View>
                </AppCard>

                <Text style={styles.footerText}>
                    Your trips, plans and activities are securely connected to your account.
                </Text>
            </AppScreen>
        </LinearGradient>
    );
}

const styles = StyleSheet.create({
    gradient: {
        flex: 1,
    },
    transparentBackground: {
        backgroundColor: "transparent",
    },
    content: {
        paddingVertical: spacing.xl,
    },
    heroSection: {
        alignItems: "center",
        gap: spacing.sm,
        marginBottom: spacing.sm,
    },
    logoBadge: {
        width: 64,
        height: 64,
        borderRadius: radius.xl,
        backgroundColor: colors.surface,
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.sm,
        ...shadows.card,
    },
    appName: {
        color: colors.primary,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
        letterSpacing: 0.8,
        textTransform: "uppercase",
    },
    heroTitle: {
        color: colors.text,
        fontSize: typography.hero,
        fontWeight: "800",
        lineHeight: 40,
        textAlign: "center",
    },
    heroSubtitle: {
        color: colors.textMuted,
        fontSize: typography.body,
        lineHeight: 23,
        maxWidth: 330,
        textAlign: "center",
    },
    card: {
        borderRadius: radius.xl,
    },
    cardContent: {
        gap: spacing.md,
    },
    linkRow: {
        flexDirection: "row",
        justifyContent: "center",
        alignItems: "center",
        gap: spacing.sm,
        paddingTop: spacing.xs,
    },
    linkText: {
        color: colors.primary,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    linkDivider: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    footerText: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 20,
        paddingHorizontal: spacing.md,
        textAlign: "center",
    },
});
