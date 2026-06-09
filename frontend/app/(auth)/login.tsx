import { useState } from "react";
import {
    ActivityIndicator,
    Alert,
    KeyboardAvoidingView,
    Platform,
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    TextInput,
    View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { LinearGradient } from "expo-linear-gradient";
import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";

import { useAuthStore } from "@/src/stores/authStore";
import { colors, radius, shadow, spacing } from "@/src/theme/theme";

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
            colors={["#EAF2FF", "#F6F8FB", "#FFFFFF"]}
            style={styles.gradient}
        >
            <SafeAreaView style={styles.safeArea}>
                <KeyboardAvoidingView
                    style={styles.keyboardView}
                    behavior={Platform.OS === "ios" ? "padding" : undefined}
                >
                    <ScrollView
                        contentContainerStyle={styles.scrollContent}
                        keyboardShouldPersistTaps="handled"
                        showsVerticalScrollIndicator={false}
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

                        <View style={styles.card}>
                            <View style={styles.cardHeader}>
                                <Text style={styles.cardTitle}>Welcome back</Text>
                                <Text style={styles.cardSubtitle}>Continue your journey</Text>
                            </View>

                            <View style={styles.inputGroup}>
                                <Text style={styles.label}>Username</Text>
                                <View style={styles.inputWrapper}>
                                    <Ionicons name="person-outline" size={20} color={colors.mutedText} />
                                    <TextInput
                                        value={username}
                                        onChangeText={setUsername}
                                        placeholder="Enter username"
                                        placeholderTextColor="#9CA3AF"
                                        autoCapitalize="none"
                                        autoCorrect={false}
                                        textContentType="username"
                                        style={styles.input}
                                    />
                                </View>
                            </View>

                            <View style={styles.inputGroup}>
                                <Text style={styles.label}>Password</Text>
                                <View style={styles.inputWrapper}>
                                    <Ionicons name="lock-closed-outline" size={20} color={colors.mutedText} />
                                    <TextInput
                                        value={password}
                                        onChangeText={setPassword}
                                        placeholder="Enter password"
                                        placeholderTextColor="#9CA3AF"
                                        secureTextEntry={!isPasswordVisible}
                                        textContentType="password"
                                        style={styles.input}
                                    />
                                    <Pressable
                                        onPress={() => setIsPasswordVisible((current) => !current)}
                                        hitSlop={10}
                                    >
                                        <Ionicons
                                            name={isPasswordVisible ? "eye-off-outline" : "eye-outline"}
                                            size={20}
                                            color={colors.mutedText}
                                        />
                                    </Pressable>
                                </View>
                            </View>

                            {error && errorCode !== MAX_SESSIONS_REACHED_CODE ? (
                                <View style={styles.errorBox}>
                                    <Ionicons name="alert-circle-outline" size={18} color={colors.error} />
                                    <Text style={styles.errorText}>{error}</Text>
                                </View>
                            ) : null}

                            <Pressable
                                onPress={() => handleLogin(false)}
                                disabled={isLoading}
                                style={({ pressed }) => [
                                    styles.loginButton,
                                    pressed && !isLoading ? styles.loginButtonPressed : null,
                                    isLoading ? styles.loginButtonDisabled : null,
                                ]}
                            >
                                {isLoading ? (
                                    <ActivityIndicator color="#FFFFFF" />
                                ) : (
                                    <>
                                        <Text style={styles.loginButtonText}>Sign in</Text>
                                        <Ionicons name="arrow-forward" size={19} color="#FFFFFF" />
                                    </>
                                )}
                            </Pressable>

                            <View style={styles.linkRow}>
                                <Pressable onPress={() => router.push("/(auth)/forgot-password" as any)}>
                                    <Text style={styles.linkText}>Forgot password?</Text>
                                </Pressable>

                                <Text style={styles.linkDivider}>•</Text>

                                <Pressable onPress={() => router.push("/(auth)/register" as any)}>
                                    <Text style={styles.linkText}>Create account</Text>
                                </Pressable>
                            </View>
                        </View>

                        <Text style={styles.footerText}>
                            Your trips, plans and activities are securely connected to your account.
                        </Text>
                    </ScrollView>
                </KeyboardAvoidingView>
            </SafeAreaView>
        </LinearGradient>
    );
}

const styles = StyleSheet.create({
    gradient: { flex: 1 },
    safeArea: { flex: 1 },
    keyboardView: { flex: 1 },
    scrollContent: {
        flexGrow: 1,
        justifyContent: "center",
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.xl,
    },
    heroSection: {
        alignItems: "center",
        marginBottom: spacing.xl,
    },
    logoBadge: {
        width: 64,
        height: 64,
        borderRadius: 22,
        backgroundColor: colors.card,
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.md,
        ...shadow.card,
    },
    appName: {
        fontSize: 15,
        fontWeight: "700",
        color: colors.primary,
        letterSpacing: 0.8,
        textTransform: "uppercase",
        marginBottom: spacing.sm,
    },
    heroTitle: {
        fontSize: 34,
        lineHeight: 40,
        fontWeight: "800",
        color: colors.text,
        textAlign: "center",
        marginBottom: spacing.sm,
    },
    heroSubtitle: {
        fontSize: 16,
        lineHeight: 23,
        color: colors.mutedText,
        textAlign: "center",
        maxWidth: 330,
    },
    card: {
        backgroundColor: colors.card,
        borderRadius: radius.xl,
        padding: spacing.lg,
        ...shadow.card,
    },
    cardHeader: { marginBottom: spacing.lg },
    cardTitle: {
        fontSize: 24,
        fontWeight: "800",
        color: colors.text,
    },
    cardSubtitle: {
        fontSize: 15,
        color: colors.mutedText,
        marginTop: 4,
    },
    inputGroup: { marginBottom: spacing.md },
    label: {
        fontSize: 14,
        fontWeight: "700",
        color: colors.text,
        marginBottom: spacing.sm,
    },
    inputWrapper: {
        minHeight: 54,
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: radius.md,
        backgroundColor: "#FFFFFF",
        paddingHorizontal: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
    },
    input: {
        flex: 1,
        fontSize: 16,
        color: colors.text,
        paddingVertical: Platform.OS === "ios" ? 14 : 10,
    },
    errorBox: {
        backgroundColor: "#FEF2F2",
        borderWidth: 1,
        borderColor: "#FECACA",
        borderRadius: radius.md,
        padding: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
        marginBottom: spacing.md,
    },
    errorText: {
        flex: 1,
        color: colors.error,
        fontSize: 14,
        lineHeight: 20,
        fontWeight: "600",
    },
    loginButton: {
        height: 56,
        borderRadius: radius.md,
        backgroundColor: colors.primary,
        alignItems: "center",
        justifyContent: "center",
        flexDirection: "row",
        gap: spacing.sm,
        marginTop: spacing.sm,
    },
    loginButtonPressed: {
        backgroundColor: colors.primaryDark,
        transform: [{ scale: 0.99 }],
    },
    loginButtonDisabled: { opacity: 0.65 },
    loginButtonText: {
        color: "#FFFFFF",
        fontSize: 16,
        fontWeight: "800",
    },
    linkRow: {
        flexDirection: "row",
        justifyContent: "center",
        alignItems: "center",
        gap: spacing.sm,
        paddingVertical: spacing.md,
        marginTop: spacing.xs,
    },
    linkText: {
        color: colors.primary,
        fontSize: 14,
        fontWeight: "700",
    },
    linkDivider: {
        color: colors.mutedText,
        fontSize: 14,
        fontWeight: "700",
    },
    footerText: {
        textAlign: "center",
        fontSize: 13,
        color: colors.mutedText,
        lineHeight: 20,
        marginTop: spacing.lg,
        paddingHorizontal: spacing.md,
    },
});
