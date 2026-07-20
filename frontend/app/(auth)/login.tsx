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
import { useAppTheme } from "@/src/hooks/useAppTheme";
import { useAuthStore } from "@/src/stores/authStore";
import { fontWeight, radius, shadows, spacing, typography } from "@/src/constants/theme";

const MAX_SESSIONS_REACHED_CODE = "E022";

export default function LoginScreen() {
    const router = useRouter();
    const theme = useAppTheme();
    const themeColors = theme.colors;
    const isDarkMode = theme.name === "DARK";

    const { loginUser, isLoading, error, errorCode, clearError } = useAuthStore();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);

    const heroTitleColor = isDarkMode ? "#FFFFFF" : themeColors.text;
    const heroSubtitleColor = isDarkMode ? "#CBD5E1" : themeColors.textMuted;
    const footerTextColor = isDarkMode ? "#CBD5E1" : themeColors.textMuted;

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
                        onPress: () => {
                            void handleLogin(true);
                        },
                    },
                ]
            );
        }
    }

    return (
        <LinearGradient
            colors={[
                isDarkMode ? themeColors.background : themeColors.primarySoft,
                themeColors.background,
                themeColors.surface,
            ]}
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
                    <View
                        style={[
                            styles.logoBadge,
                            {
                                backgroundColor: themeColors.surface,
                                borderColor: themeColors.border,
                            },
                        ]}
                    >
                        <Ionicons name="airplane" size={28} color={themeColors.primary} />
                    </View>

                    <Text style={[styles.appName, { color: themeColors.primary }]}>WanderMate</Text>
                    <Text
                        style={[
                            styles.heroTitle,
                            {
                                color: heroTitleColor,
                                textShadowColor: isDarkMode ? "rgba(0, 0, 0, 0.5)" : "transparent",
                            },
                        ]}
                    >
                        Plan smarter trips
                    </Text>
                    <Text style={[styles.heroSubtitle, { color: heroSubtitleColor }]}>
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
                        testID="login-username"
                        leftIcon={<Ionicons name="person-outline" size={20} color={themeColors.textMuted} />}
                    />

                    <AppInput
                        label="Password"
                        value={password}
                        onChangeText={setPassword}
                        placeholder="Enter password"
                        secureTextEntry={!isPasswordVisible}
                        textContentType="password"
                        testID="login-password"
                        leftIcon={<Ionicons name="lock-closed-outline" size={20} color={themeColors.textMuted} />}
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
                                    color={themeColors.textMuted}
                                />
                            </Pressable>
                        }
                    />

                    <ErrorMessage
                        message={errorCode === MAX_SESSIONS_REACHED_CODE ? null : error}
                        title="Login failed"
                    />

                    <AppButton
                        title="Sign in"
                        loading={isLoading}
                        onPress={() => handleLogin(false)}
                        rightIcon={isLoading ? null :
                            <Ionicons name="arrow-forward" size={19} color={themeColors.textLight}/>}
                        size="lg"
                        testID="login-submit"
                    />

                    <View style={styles.linkRow}>
                        <Pressable onPress={() => router.push("/forgot-password")}>
                            <Text style={[styles.linkText, { color: themeColors.primary }]}>Forgot password?</Text>
                        </Pressable>

                        <Text style={[styles.linkDivider, { color: themeColors.textMuted }]}>•</Text>

                        <Pressable onPress={() => router.push("/register")}>
                            <Text style={[styles.linkText, { color: themeColors.primary }]}>Create account</Text>
                        </Pressable>
                    </View>
                </AppCard>

                <Text style={[styles.footerText, { color: footerTextColor }]}>
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
        borderWidth: 1,
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.sm,
        ...shadows.card,
    },
    appName: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
        letterSpacing: 0.8,
        textTransform: "uppercase",
    },
    heroTitle: {
        fontSize: typography.hero,
        fontWeight: fontWeight.extraBold,
        lineHeight: 42,
        textAlign: "center",
        textShadowOffset: { width: 0, height: 1 },
        textShadowRadius: 8,
    },
    heroSubtitle: {
        fontSize: typography.body,
        lineHeight: 24,
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
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    linkDivider: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    footerText: {
        fontSize: typography.caption,
        lineHeight: 20,
        paddingHorizontal: spacing.md,
        textAlign: "center",
    },
});
