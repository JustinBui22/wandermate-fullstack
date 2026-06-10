import {useEffect, useState} from "react";
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
import {SafeAreaView} from "react-native-safe-area-context";
import {LinearGradient} from "expo-linear-gradient";
import {Ionicons} from "@expo/vector-icons";
import {useRouter} from "expo-router";

import {checkUserExisted, forgotPassword, sendOtp} from "@/src/api/authApi";
import {colors, radius, shadow, spacing} from "@/src/theme/theme";
import type {OtpVerificationMethod} from "@/src/types/auth";

const OTP_EXPIRY_SECONDS = 120;
const RESEND_COOLDOWN_SECONDS = 60;
const OTP_RESTRICTED_MINUTES = 15;
const OTP_BLOCKED_OR_NOT_FOUND_CODE = "E028";
const MAX_OTP_RETRY_CODE = "E026";

function formatTimer(seconds: number) {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, "0")}`;
}

function getApiMessage(error: any) {
    return error.response?.data?.message || error.message || "Something went wrong. Please try again.";
}

function isOtpRestricted(error: any) {
    const code = error.response?.data?.code;
    return code === OTP_BLOCKED_OR_NOT_FOUND_CODE || code === MAX_OTP_RETRY_CODE;
}

export default function ForgotPasswordScreen() {
    const router = useRouter();

    const [step, setStep] = useState<1 | 2 | 3>(1);
    const [userInput, setUserInput] = useState("");
    const [resolvedUsername, setResolvedUsername] = useState<string | null>(null);
    const [email, setEmail] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [otpMethod, setOtpMethod] = useState<OtpVerificationMethod>("EMAIL_OTP");
    const [otp, setOtp] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);
    const [isSendingOtp, setIsSendingOtp] = useState(false);
    const [isResetting, setIsResetting] = useState(false);
    const [otpExpiresIn, setOtpExpiresIn] = useState(0);
    const [resendCooldown, setResendCooldown] = useState(0);
    const [otpSent, setOtpSent] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (otpExpiresIn <= 0 && resendCooldown <= 0) return;

        const timer = setInterval(() => {
            setOtpExpiresIn((current) => Math.max(current - 1, 0));
            setResendCooldown((current) => Math.max(current - 1, 0));
        }, 1000);

        return () => clearInterval(timer);
    }, [otpExpiresIn, resendCooldown]);

    async function resolveUsername() {
        const trimmedUserInput = userInput.trim();

        if (!trimmedUserInput) {
            Alert.alert("Missing account", "Please enter your username, email or phone number.");
            return null;
        }

        const username = await checkUserExisted(trimmedUserInput);
        setResolvedUsername(username);
        return username;
    }

    function validateOtpDestination() {
        if (otpMethod === "EMAIL_OTP" && !email.trim()) {
            Alert.alert("Missing email", "Please enter the email linked to your account.");
            return false;
        }

        if (otpMethod === "PHONE_NUM_OTP" && !phoneNumber.trim()) {
            Alert.alert("Missing phone number", "Please enter the phone number linked to your account.");
            return false;
        }

        return true;
    }

    async function handleContinueToOtpMethod() {
        setError(null);

        try {
            setIsSendingOtp(true);

            const username = await resolveUsername();

            if (!username) return;

            setStep(2);
        } catch (error: any) {
            const message = getApiMessage(error);
            setError(message);
            Alert.alert("Account not found", message);
        } finally {
            setIsSendingOtp(false);
        }
    }

    async function handleSendOtp() {
        if (isSendingOtp) {
            return;
        }
        setError(null);

        if (resendCooldown > 0) {
            Alert.alert("Please wait", `You can resend OTP in ${formatTimer(resendCooldown)}.`);
            return;
        }

        if (!validateOtpDestination()) return;

        try {
            setIsSendingOtp(true);

            const username = resolvedUsername ?? await resolveUsername();
            if (!username) return;

            if (otpMethod === "EMAIL_OTP") {
                await sendOtp({
                    userName: username,
                    otpVerificationMethod: "EMAIL_OTP",
                    email: email.trim(),
                    emailEnum: "EMAIL_OTP_REGISTER",
                });
            } else {
                await sendOtp({
                    userName: username,
                    otpVerificationMethod: "PHONE_NUM_OTP",
                    phoneNumber: phoneNumber.trim(),
                    smsEnum: "SMS_OTP_REGISTER",
                });
            }

            setOtpSent(true);
            setOtp("");
            setOtpExpiresIn(OTP_EXPIRY_SECONDS);
            setResendCooldown(RESEND_COOLDOWN_SECONDS);
            setStep(3);
            Alert.alert("OTP sent", otpMethod === "EMAIL_OTP" ? "Please check your email." : "Please check your phone messages.");
        } catch (error: any) {
            const message = getApiMessage(error);
            setError(message);

            if (isOtpRestricted(error)) {
                Alert.alert(
                    "OTP temporarily blocked",
                    `Please wait about ${OTP_RESTRICTED_MINUTES} minutes before trying again.`
                );
            }
        } finally {
            setIsSendingOtp(false);
        }
    }

    async function handleResetPassword() {
        setError(null);

        if (!otpSent) {
            Alert.alert("Send OTP first", "Please request an OTP before resetting your password.");
            return;
        }

        if (otpExpiresIn <= 0) {
            Alert.alert("OTP expired", "Please request a new OTP code.");
            return;
        }

        if (!otp.trim()) {
            Alert.alert("Missing OTP", "Please enter the OTP code.");
            return;
        }

        if (!newPassword || !confirmPassword) {
            Alert.alert("Missing password", "Please enter and confirm your new password.");
            return;
        }

        if (newPassword !== confirmPassword) {
            Alert.alert("Password mismatch", "New password and confirm password do not match.");
            return;
        }

        try {
            setIsResetting(true);

            const username = resolvedUsername ?? await resolveUsername();
            if (!username) return;

            await forgotPassword({
                username,
                newPassword,
                otp: otp.trim(),
                email: otpMethod === "EMAIL_OTP" ? email.trim() : undefined,
                phoneNumber: otpMethod === "PHONE_NUM_OTP" ? phoneNumber.trim() : undefined,
            });

            Alert.alert("Password updated", "You can now sign in with your new password.", [
                {
                    text: "Go to login",
                    onPress: () => router.replace("/login"),
                },
            ]);
        } catch (error: any) {
            const message = getApiMessage(error);
            setError(message);
            Alert.alert("Reset failed", message);
        } finally {
            setIsResetting(false);
        }
    }

    return (
        <LinearGradient colors={["#EAF2FF", "#F6F8FB", "#FFFFFF"]} style={styles.gradient}>
            <SafeAreaView style={styles.safeArea}>
                <KeyboardAvoidingView style={styles.keyboardView}
                                      behavior={Platform.OS === "ios" ? "padding" : undefined}>
                    <ScrollView contentContainerStyle={styles.scrollContent} keyboardShouldPersistTaps="handled"
                                showsVerticalScrollIndicator={false}>
                        <Pressable onPress={() => router.replace("/login")} style={styles.backButton}>
                            <Ionicons name="chevron-back" size={20} color={colors.primary}/>
                            <Text style={styles.backText}>Back to login</Text>
                        </Pressable>

                        <View style={styles.header}>
                            <Text style={styles.title}>Forgot password</Text>
                            <Text style={styles.subtitle}>Recover your account in a few quick steps.</Text>
                        </View>

                        <View style={styles.card}>
                            <View style={styles.stepHeader}>
                                <Text style={styles.stepText}>Step {step} of 3</Text>
                                <Text style={styles.stepTitle}>
                                    {step === 1
                                        ? "Find your account"
                                        : step === 2
                                            ? "Send verification code"
                                            : "Create new password"}
                                </Text>
                            </View>

                            {step === 1 ? (
                                <>
                                    <AuthInput
                                        label="Username, email or phone"
                                        icon="person-outline"
                                        value={userInput}
                                        onChangeText={(value) => {
                                            setUserInput(value);
                                            setResolvedUsername(null);
                                        }}
                                        placeholder="Enter account detail"
                                    />

                                    {error ? <ErrorBox message={error}/> : null}

                                    <Pressable
                                        onPress={handleContinueToOtpMethod}
                                        disabled={isSendingOtp}
                                        style={({pressed}) => [
                                            styles.primaryButton,
                                            pressed && !isSendingOtp ? styles.buttonPressed : null,
                                            isSendingOtp ? styles.buttonDisabled : null,
                                        ]}
                                    >
                                        {isSendingOtp ? (
                                            <ActivityIndicator color="#FFFFFF"/>
                                        ) : (
                                            <Text style={styles.primaryButtonText}>Continue</Text>
                                        )}
                                    </Pressable>
                                </>
                            ) : null}

                            {step === 2 ? (
                                <>
                                    <Text style={styles.helperText}>
                                        Account found: {resolvedUsername}
                                    </Text>

                                    <Text style={styles.sectionLabel}>OTP method</Text>
                                    <View style={styles.methodRow}>
                                        <MethodButton
                                            label="Email"
                                            icon="mail-outline"
                                            selected={otpMethod === "EMAIL_OTP"}
                                            onPress={() => setOtpMethod("EMAIL_OTP")}
                                        />
                                        <MethodButton
                                            label="Phone"
                                            icon="call-outline"
                                            selected={otpMethod === "PHONE_NUM_OTP"}
                                            onPress={() => setOtpMethod("PHONE_NUM_OTP")}
                                        />
                                    </View>

                                    {otpMethod === "EMAIL_OTP" ? (
                                        <AuthInput
                                            label="Registered email"
                                            icon="mail-outline"
                                            value={email}
                                            onChangeText={setEmail}
                                            placeholder="name@example.com"
                                            keyboardType="email-address"
                                        />
                                    ) : (
                                        <AuthInput
                                            label="Registered phone number"
                                            icon="call-outline"
                                            value={phoneNumber}
                                            onChangeText={setPhoneNumber}
                                            placeholder="Phone number"
                                            keyboardType="phone-pad"
                                        />
                                    )}

                                    {error ? <ErrorBox message={error}/> : null}

                                    <Pressable
                                        onPress={handleSendOtp}
                                        disabled={isSendingOtp || resendCooldown > 0}
                                        style={({pressed}) => [
                                            styles.secondaryButton,
                                            pressed && !isSendingOtp && resendCooldown <= 0 ? styles.buttonPressed : null,
                                            isSendingOtp || resendCooldown > 0 ? styles.buttonDisabled : null,
                                        ]}
                                    >
                                        {isSendingOtp ? (
                                            <ActivityIndicator color={colors.primary}/>
                                        ) : (
                                            <Text style={styles.secondaryButtonText}>
                                                {otpSent ? "Resend OTP" : "Send OTP"}
                                            </Text>
                                        )}
                                    </Pressable>

                                    {resendCooldown > 0 ? (
                                        <Text style={styles.timerText}>
                                            You can resend in {formatTimer(resendCooldown)}
                                        </Text>
                                    ) : null}

                                    <Pressable onPress={() => setStep(1)} style={styles.textButton}>
                                        <Text style={styles.textButtonText}>Use a different account</Text>
                                    </Pressable>
                                </>
                            ) : null}

                            {step === 3 ? (
                                <>
                                    <Text style={styles.helperText}>
                                        Code sent by {otpMethod === "EMAIL_OTP" ? "email" : "phone"}.
                                    </Text>

                                    {otpSent ? (
                                        <Text style={styles.timerText}>
                                            OTP expires in {formatTimer(otpExpiresIn)}{" "}
                                            {resendCooldown > 0 ? `• Resend in ${formatTimer(resendCooldown)}` : ""}
                                        </Text>
                                    ) : null}

                                    <AuthInput
                                        label="OTP code"
                                        icon="key-outline"
                                        value={otp}
                                        onChangeText={setOtp}
                                        placeholder="Enter 6-digit OTP"
                                        keyboardType="number-pad"
                                    />

                                    <AuthInput
                                        label="New password"
                                        icon="lock-closed-outline"
                                        value={newPassword}
                                        onChangeText={setNewPassword}
                                        placeholder="8-20 characters"
                                        secureTextEntry={!isPasswordVisible}
                                        rightIcon={isPasswordVisible ? "eye-off-outline" : "eye-outline"}
                                        onRightIconPress={() => setIsPasswordVisible((current) => !current)}
                                    />

                                    <AuthInput
                                        label="Confirm new password"
                                        icon="lock-closed-outline"
                                        value={confirmPassword}
                                        onChangeText={setConfirmPassword}
                                        placeholder="Repeat new password"
                                        secureTextEntry={!isPasswordVisible}
                                    />

                                    {error ? <ErrorBox message={error}/> : null}

                                    <Pressable
                                        onPress={handleResetPassword}
                                        disabled={isResetting}
                                        style={({pressed}) => [
                                            styles.primaryButton,
                                            pressed && !isResetting ? styles.buttonPressed : null,
                                            isResetting ? styles.buttonDisabled : null,
                                        ]}
                                    >
                                        {isResetting ? (
                                            <ActivityIndicator color="#FFFFFF"/>
                                        ) : (
                                            <Text style={styles.primaryButtonText}>Update password</Text>
                                        )}
                                    </Pressable>

                                    <Pressable
                                        onPress={handleSendOtp}
                                        disabled={isSendingOtp || resendCooldown > 0}
                                        style={styles.textButton}
                                    >
                                        <Text style={styles.textButtonText}>
                                            {resendCooldown > 0
                                                ? `Resend OTP in ${formatTimer(resendCooldown)}`
                                                : "Resend OTP"}
                                        </Text>
                                    </Pressable>

                                    <Pressable onPress={() => setStep(2)} style={styles.textButton}>
                                        <Text style={styles.textButtonText}>Change OTP method</Text>
                                    </Pressable>
                                </>
                            ) : null}
                        </View>
                    </ScrollView>
                </KeyboardAvoidingView>
            </SafeAreaView>
        </LinearGradient>
    );
}

type AuthInputProps = Readonly<{
    label: string;
    icon: keyof typeof Ionicons.glyphMap;
    value: string;
    onChangeText: (value: string) => void;
    placeholder: string;
    keyboardType?: "default" | "email-address" | "phone-pad" | "number-pad";
    secureTextEntry?: boolean;
    rightIcon?: keyof typeof Ionicons.glyphMap;
    onRightIconPress?: () => void;
}>;

function AuthInput({
                       label,
                       icon,
                       value,
                       onChangeText,
                       placeholder,
                       keyboardType = "default",
                       secureTextEntry,
                       rightIcon,
                       onRightIconPress
                   }: AuthInputProps) {
    return (
        <View style={styles.inputGroup}>
            <Text style={styles.label}>{label}</Text>
            <View style={styles.inputWrapper}>
                <Ionicons name={icon} size={20} color={colors.mutedText}/>
                <TextInput value={value} onChangeText={onChangeText} placeholder={placeholder}
                           placeholderTextColor="#9CA3AF" autoCapitalize="none" autoCorrect={false}
                           keyboardType={keyboardType} secureTextEntry={secureTextEntry} style={styles.input}/>
                {rightIcon ? (
                    <Pressable onPress={onRightIconPress} hitSlop={10}>
                        <Ionicons name={rightIcon} size={20} color={colors.mutedText}/>
                    </Pressable>
                ) : null}
            </View>
        </View>
    );
}

function MethodButton({label, icon, selected, onPress}: {
    label: string;
    icon: keyof typeof Ionicons.glyphMap;
    selected: boolean;
    onPress: () => void
}) {
    return (
        <Pressable onPress={onPress} style={[styles.methodButton, selected ? styles.methodButtonSelected : null]}>
            <Ionicons name={icon} size={18} color={selected ? colors.primary : colors.mutedText}/>
            <Text style={[styles.methodText, selected ? styles.methodTextSelected : null]}>{label}</Text>
        </Pressable>
    );
}

function ErrorBox({message}: { message: string }) {
    return (
        <View style={styles.errorBox}>
            <Ionicons name="alert-circle-outline" size={18} color={colors.error}/>
            <Text style={styles.errorText}>{message}</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    gradient: {flex: 1},
    safeArea: {flex: 1},
    keyboardView: {flex: 1},
    scrollContent: {padding: spacing.lg, paddingBottom: spacing.xl},
    backButton: {flexDirection: "row", alignItems: "center", gap: 4, marginBottom: spacing.lg},
    backText: {color: colors.primary, fontWeight: "700"},
    header: {marginBottom: spacing.lg},
    title: {fontSize: 32, fontWeight: "800", color: colors.text},
    subtitle: {fontSize: 15, color: colors.mutedText, marginTop: spacing.sm, lineHeight: 22},
    card: {backgroundColor: colors.card, borderRadius: radius.xl, padding: spacing.lg, ...shadow.card},
    inputGroup: {marginBottom: spacing.md},
    label: {fontSize: 14, fontWeight: "700", color: colors.text, marginBottom: spacing.sm},
    sectionLabel: {fontSize: 14, fontWeight: "800", color: colors.text, marginBottom: spacing.sm},
    inputWrapper: {
        minHeight: 54,
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: radius.md,
        backgroundColor: "#FFFFFF",
        paddingHorizontal: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm
    },
    input: {flex: 1, fontSize: 16, color: colors.text, paddingVertical: Platform.OS === "ios" ? 14 : 10},
    methodRow: {flexDirection: "row", gap: spacing.sm, marginBottom: spacing.md},
    methodButton: {
        flex: 1,
        minHeight: 48,
        borderRadius: radius.md,
        borderWidth: 1,
        borderColor: colors.border,
        backgroundColor: "#FFFFFF",
        alignItems: "center",
        justifyContent: "center",
        flexDirection: "row",
        gap: spacing.sm
    },
    methodButtonSelected: {borderColor: colors.primary, backgroundColor: colors.softBlue},
    methodText: {color: colors.mutedText, fontWeight: "800"},
    methodTextSelected: {color: colors.primary},
    primaryButton: {
        height: 56,
        borderRadius: radius.md,
        backgroundColor: colors.primary,
        alignItems: "center",
        justifyContent: "center",
        marginTop: spacing.sm
    },
    primaryButtonText: {color: "#FFFFFF", fontWeight: "800", fontSize: 16},
    secondaryButton: {
        height: 52,
        borderRadius: radius.md,
        backgroundColor: colors.softBlue,
        borderWidth: 1,
        borderColor: "#BFDBFE",
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.sm
    },
    secondaryButtonText: {color: colors.primary, fontWeight: "800", fontSize: 15},
    buttonPressed: {transform: [{scale: 0.99}]},
    buttonDisabled: {opacity: 0.65},
    timerText: {
        color: colors.mutedText,
        fontSize: 13,
        fontWeight: "700",
        marginBottom: spacing.md,
        textAlign: "center"
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
        marginBottom: spacing.md
    },
    errorText: {flex: 1, color: colors.error, fontSize: 14, lineHeight: 20, fontWeight: "600"},
    stepHeader: {
        marginBottom: spacing.lg,
    },
    stepText: {
        fontSize: 13,
        color: colors.primary,
        fontWeight: "800",
        marginBottom: 4,
    },
    stepTitle: {
        fontSize: 20,
        color: colors.text,
        fontWeight: "800",
    },
    helperText: {
        color: colors.mutedText,
        fontSize: 14,
        lineHeight: 20,
        fontWeight: "600",
        marginBottom: spacing.md,
    },
    textButton: {
        alignItems: "center",
        justifyContent: "center",
        paddingVertical: spacing.sm,
        marginTop: spacing.sm,
    },
    textButtonText: {
        color: colors.primary,
        fontWeight: "800",
        fontSize: 14,
    },
});
