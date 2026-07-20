import { useEffect, useState } from "react";
import {
    Alert,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";

import { forgotPassword, sendOtp } from "@/src/api/authApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { OtpCooldownBadge, OtpMethodButton } from "@/src/features/auth/AuthFlowControls";
import { colors as staticColors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { OtpVerificationMethod } from "@/src/types/auth";
import { getApiErrorCode, getApiErrorMessage } from "@/src/utils/apiWarningUtils";

const OTP_EXPIRY_SECONDS = 300;
const RESEND_COOLDOWN_SECONDS = 60;
const OTP_RESTRICTED_MINUTES = 15;
const OTP_BLOCKED_OR_NOT_FOUND_CODE = "E028";
const MAX_OTP_RETRY_CODE = "E026";

function formatTimer(seconds: number) {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, "0")}`;
}

function getApiMessage(error: unknown) {
    return getApiErrorMessage(error, "Something went wrong. Please try again.");
}

function isOtpRestricted(error: unknown) {
    const code = getApiErrorCode(error);
    return code === OTP_BLOCKED_OR_NOT_FOUND_CODE || code === MAX_OTP_RETRY_CODE;
}

function getStepTitle(step: 1 | 2 | 3) {
    if (step === 1) return "Find your account";
    if (step === 2) return "Send verification code";
    return "Create new password";
}

export default function ForgotPasswordScreen() {
    const router = useRouter();
    const theme = useAppTheme();
    const colors = theme.colors;

    const [step, setStep] = useState<1 | 2 | 3>(1);
    const [userInput, setUserInput] = useState("");
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

    function validateAccountInput() {
        const trimmedUserInput = userInput.trim();

        if (!trimmedUserInput) {
            Alert.alert("Missing account", "Please enter your username, email or phone number.");
            return null;
        }

        return trimmedUserInput;
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

    function handleContinueToOtpMethod() {
        setError(null);
        if (!validateAccountInput()) return;
        setStep(2);
    }

    async function handleSendOtp() {
        if (isSendingOtp) return;

        setError(null);

        if (resendCooldown > 0) {
            Alert.alert("Please wait", `You can resend OTP in ${formatTimer(resendCooldown)}.`);
            return;
        }

        if (!validateOtpDestination()) return;

        try {
            setIsSendingOtp(true);

            const accountInput = validateAccountInput();
            if (!accountInput) return;

            if (otpMethod === "EMAIL_OTP") {
                await sendOtp({
                    userName: accountInput,
                    otpVerificationMethod: "EMAIL_OTP",
                    email: email.trim(),
                    emailEnum: "EMAIL_OTP_REGISTER",
                    purpose: "PASSWORD_RESET",
                });
            } else {
                await sendOtp({
                    userName: accountInput,
                    otpVerificationMethod: "PHONE_NUM_OTP",
                    phoneNumber: phoneNumber.trim(),
                    smsEnum: "SMS_OTP_REGISTER",
                    purpose: "PASSWORD_RESET",
                });
            }

            setOtpSent(true);
            setOtp("");
            setOtpExpiresIn(OTP_EXPIRY_SECONDS);
            setResendCooldown(RESEND_COOLDOWN_SECONDS);
            setStep(3);
            Alert.alert(
                "Check for your OTP",
                "If the account details match our records, a verification code has been sent."
            );
        } catch (error: unknown) {
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

            const accountInput = validateAccountInput();
            if (!accountInput) return;

            await forgotPassword({
                username: accountInput,
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
        } catch (error: unknown) {
            const message = getApiMessage(error);
            setError(message);
            Alert.alert("Reset failed", message);
        } finally {
            setIsResetting(false);
        }
    }

    return (
        <AppScreen keyboardAvoiding contentContainerStyle={styles.screenContent}>
            <Pressable onPress={() => router.replace("/login")} style={styles.backButton}>
                <Ionicons name="chevron-back" size={20} color={colors.primary} />
                <Text style={[styles.backText, { color: colors.primary }]}>Back to login</Text>
            </Pressable>

            <View style={styles.header}>
                <View style={[styles.logoBadge, { backgroundColor: colors.primarySoft, borderColor: colors.border }]}>
                    <Ionicons name="key-outline" size={26} color={colors.primary} />
                </View>
                <Text style={[styles.title, { color: colors.text }]}>Forgot password</Text>
                <Text style={[styles.subtitle, { color: colors.textMuted }]}>Recover your account in a few quick steps.</Text>
            </View>

            <AppCard style={styles.card} contentStyle={styles.cardContent}>
                <View style={styles.stepHeader}>
                    <Text style={[styles.stepText, { color: colors.primary }]}>Step {step} of 3</Text>
                    <Text style={[styles.stepTitle, { color: colors.text }]}>{getStepTitle(step)}</Text>
                </View>

                {step === 1 ? (
                    <View style={styles.section}>
                        <AppInput
                            label="Username, email or phone"
                            value={userInput}
                            onChangeText={setUserInput}
                            placeholder="Enter account detail"
                            autoCapitalize="none"
                            autoCorrect={false}
                            leftIcon={<Ionicons name="person-outline" size={20} color={colors.textMuted} />}
                        />

                        <AppButton
                            title="Continue"
                            onPress={handleContinueToOtpMethod}
                        />
                    </View>
                ) : null}

                {step === 2 ? (
                    <View style={styles.section}>
                        <Text style={[styles.helperText, { color: colors.textMuted }]}>
                            For privacy, WanderMate will not confirm whether an account exists. Enter the destination connected to your account.
                        </Text>

                        <View style={styles.methodSection}>
                            <Text style={[styles.sectionLabel, { color: colors.text }]}>OTP method</Text>
                            <View style={styles.methodRow}>
                                <OtpMethodButton
                                    label="Email"
                                    icon="mail-outline"
                                    selected={otpMethod === "EMAIL_OTP"}
                                    onPress={() => setOtpMethod("EMAIL_OTP")}
                                />
                                <OtpMethodButton
                                    label="Phone"
                                    icon="call-outline"
                                    selected={otpMethod === "PHONE_NUM_OTP"}
                                    onPress={() => setOtpMethod("PHONE_NUM_OTP")}
                                />
                            </View>
                        </View>

                        {otpMethod === "EMAIL_OTP" ? (
                            <AppInput
                                label="Registered email"
                                value={email}
                                onChangeText={setEmail}
                                placeholder="name@example.com"
                                keyboardType="email-address"
                                autoCapitalize="none"
                                autoCorrect={false}
                                leftIcon={<Ionicons name="mail-outline" size={20} color={colors.textMuted} />}
                            />
                        ) : (
                            <AppInput
                                label="Registered phone number"
                                value={phoneNumber}
                                onChangeText={setPhoneNumber}
                                placeholder="Phone number"
                                keyboardType="phone-pad"
                                leftIcon={<Ionicons name="call-outline" size={20} color={colors.textMuted} />}
                            />
                        )}

                        <ErrorMessage message={error} title="OTP request failed" />

                        <View style={styles.otpActionRow}>
                            <AppButton
                                title={otpSent ? "Resend OTP" : "Send OTP"}
                                onPress={() => {
                                    void handleSendOtp();
                                }}
                                loading={isSendingOtp}
                                disabled={resendCooldown > 0}
                                variant="secondary"
                                fullWidth={false}
                                style={styles.otpActionButton}
                            />
                            <OtpCooldownBadge seconds={resendCooldown} />
                        </View>

                        {resendCooldown > 0 ? (
                            <Text style={[styles.timerText, { color: colors.textMuted }]}>You can resend in {formatTimer(resendCooldown)}</Text>
                        ) : null}

                        <AppButton
                            title="Use a different account"
                            onPress={() => setStep(1)}
                            variant="ghost"
                        />
                    </View>
                ) : null}

                {step === 3 ? (
                    <View style={styles.section}>
                        <Text style={[styles.helperText, { color: colors.textMuted }]}>
                            Code sent by {otpMethod === "EMAIL_OTP" ? "email" : "phone"}.
                        </Text>

                        {otpSent ? (
                            <Text style={[styles.timerText, { color: colors.textMuted }]}>
                                OTP expires in {formatTimer(otpExpiresIn)}{" "}
                                {resendCooldown > 0 ? `• Resend in ${formatTimer(resendCooldown)}` : ""}
                            </Text>
                        ) : null}

                        <AppInput
                            label="OTP code"
                            value={otp}
                            onChangeText={setOtp}
                            placeholder="Enter 6-digit OTP"
                            keyboardType="number-pad"
                            leftIcon={<Ionicons name="key-outline" size={20} color={colors.textMuted} />}
                        />

                        <AppInput
                            label="New password"
                            value={newPassword}
                            onChangeText={setNewPassword}
                            placeholder="8-20 characters"
                            secureTextEntry={!isPasswordVisible}
                            leftIcon={<Ionicons name="lock-closed-outline" size={20} color={colors.textMuted} />}
                            rightIcon={
                                <Pressable
                                    onPress={() => setIsPasswordVisible((current) => !current)}
                                    hitSlop={10}
                                >
                                    <Ionicons
                                        name={isPasswordVisible ? "eye-off-outline" : "eye-outline"}
                                        size={20}
                                        color={colors.textMuted}
                                    />
                                </Pressable>
                            }
                        />

                        <AppInput
                            label="Confirm new password"
                            value={confirmPassword}
                            onChangeText={setConfirmPassword}
                            placeholder="Repeat new password"
                            secureTextEntry={!isPasswordVisible}
                            leftIcon={<Ionicons name="lock-closed-outline" size={20} color={colors.textMuted} />}
                        />

                        <ErrorMessage message={error} title="Reset failed" />

                        <AppButton
                            title="Update password"
                            onPress={() => {
                                void handleResetPassword();
                            }}
                            loading={isResetting}
                        />

                        <View style={styles.otpActionRow}>
                            <AppButton
                                title="Resend OTP"
                                onPress={() => {
                                    void handleSendOtp();
                                }}
                                loading={isSendingOtp}
                                disabled={resendCooldown > 0}
                                variant="ghost"
                                fullWidth={false}
                                style={styles.otpActionButton}
                            />
                            <OtpCooldownBadge seconds={resendCooldown} />
                        </View>

                        <AppButton
                            title="Change OTP method"
                            onPress={() => setStep(2)}
                            variant="ghost"
                        />
                    </View>
                ) : null}
            </AppCard>
        </AppScreen>
    );
}

const styles = StyleSheet.create({
    screenContent: {
        paddingTop: spacing.lg,
        paddingBottom: spacing.xxl,
        gap: spacing.lg,
    },
    backButton: {
        flexDirection: "row",
        alignItems: "center",
        alignSelf: "flex-start",
        gap: spacing.xs,
    },
    backText: {
        color: staticColors.primary,
        fontWeight: fontWeight.semibold,
    },
    header: {
        alignItems: "center",
        gap: spacing.sm,
    },
    logoBadge: {
        width: 58,
        height: 58,
        borderRadius: radius.xl,
        backgroundColor: staticColors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
        marginBottom: spacing.xs,
    },
    title: {
        color: staticColors.text,
        fontSize: typography.hero,
        fontWeight: fontWeight.bold,
        textAlign: "center",
    },
    subtitle: {
        color: staticColors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 21,
        textAlign: "center",
        maxWidth: 320,
    },
    card: {
        marginTop: spacing.sm,
    },
    cardContent: {
        gap: spacing.lg,
    },
    stepHeader: {
        gap: spacing.xs,
    },
    stepText: {
        color: staticColors.primary,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.5,
    },
    stepTitle: {
        color: staticColors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    section: {
        gap: spacing.md,
    },
    otpActionRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
    },
    otpActionButton: {
        flex: 1,
    },
    helperText: {
        color: staticColors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
        fontWeight: fontWeight.semibold,
    },
    methodSection: {
        gap: spacing.sm,
    },
    sectionLabel: {
        color: staticColors.text,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    methodRow: {
        flexDirection: "row",
        gap: spacing.sm,
    },
    timerText: {
        color: staticColors.textMuted,
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
        lineHeight: 18,
        textAlign: "center",
    },
});
