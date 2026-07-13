import { useEffect, useState } from "react";
import DateTimePicker from "@react-native-community/datetimepicker";
import {
    Alert,
    Platform,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";

import { register, sendOtp, verifyRegisterDetails } from "@/src/api/authApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { colors as staticColors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { OtpVerificationMethod } from "@/src/types/auth";

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
    const data = error.response?.data;

    if (typeof data?.body === "string" && data.body.trim()) {
        return data.body;
    }

    return data?.message || error.message || "Something went wrong. Please try again.";
}

function isOtpRestricted(error: any) {
    const code = error.response?.data?.code;
    return code === OTP_BLOCKED_OR_NOT_FOUND_CODE || code === MAX_OTP_RETRY_CODE;
}

function formatDateForDisplay(date: Date) {
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();

    return `${day}/${month}/${year}`;
}

function getMaximumDobDate() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return today;
}

function getMinimumDobDate() {
    return new Date(1900, 0, 1);
}

function getDefaultDobDate() {
    const date = new Date();
    date.setFullYear(date.getFullYear() - 18);
    date.setHours(0, 0, 0, 0);

    return date;
}

function getStepTitle(step: 1 | 2) {
    return step === 1 ? "Account information" : "Verify your account";
}

export default function RegisterScreen() {
    const router = useRouter();
    const theme = useAppTheme();
    const colors = theme.colors;

    const [step, setStep] = useState<1 | 2>(1);
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [dob, setDob] = useState("");
    const [dobDate, setDobDate] = useState<Date | null>(null);
    const [showDobPicker, setShowDobPicker] = useState(false);
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [otp, setOtp] = useState("");
    const [otpMethod, setOtpMethod] = useState<OtpVerificationMethod>("EMAIL_OTP");
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);
    const [isSendingOtp, setIsSendingOtp] = useState(false);
    const [isRegistering, setIsRegistering] = useState(false);
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

    function getTrimmedValues() {
        return {
            username: username.trim(),
            email: email.trim(),
            phoneNumber: phoneNumber.trim(),
            dob: dob.trim(),
            password,
            confirmPassword,
            otp: otp.trim(),
        };
    }

    function handleDobValueChange(_event: unknown, selectedDate?: Date) {
        if (!selectedDate) {
            if (Platform.OS === "android") {
                setShowDobPicker(false);
            }
            return;
        }

        setDobDate(selectedDate);
        setDob(formatDateForDisplay(selectedDate));

        if (Platform.OS === "android") {
            setShowDobPicker(false);
        }
    }

    function validateAccountFields() {
        const values = getTrimmedValues();

        if (!values.username || !values.email || !values.dob || !values.password || !values.confirmPassword) {
            Alert.alert("Missing details", "Please fill in all account details before requesting OTP.");
            return null;
        }

        if (values.password !== values.confirmPassword) {
            Alert.alert("Password mismatch", "Password and confirm password do not match.");
            return null;
        }

        return values;
    }

    async function handleContinueToOtpStep() {
        setError(null);

        const values = validateAccountFields();
        if (!values) return;

        try {
            setIsSendingOtp(true);

            await verifyRegisterDetails({
                username: values.username,
                email: values.email,
                phoneNumber: values.phoneNumber || undefined,
                dob: values.dob,
                password: values.password,
            });

            setStep(2);
        } catch (error: any) {
            const message = getApiMessage(error);
            setError(message);
            Alert.alert("Cannot continue", message);
        } finally {
            setIsSendingOtp(false);
        }
    }

    async function handleSendOtp() {
        setError(null);

        if (resendCooldown > 0) {
            Alert.alert("Please wait", `You can resend OTP in ${formatTimer(resendCooldown)}.`);
            return;
        }

        const values = validateAccountFields();
        if (!values) return;

        if (otpMethod === "PHONE_NUM_OTP" && !values.phoneNumber) {
            Alert.alert("Phone number required", "Please enter your phone number to receive OTP by phone.");
            return;
        }

        try {
            setIsSendingOtp(true);

            if (otpMethod === "EMAIL_OTP") {
                await sendOtp({
                    userName: values.username,
                    otpVerificationMethod: "EMAIL_OTP",
                    email: values.email,
                    emailEnum: "EMAIL_OTP_REGISTER",
                });
            } else {
                await sendOtp({
                    userName: values.username,
                    otpVerificationMethod: "PHONE_NUM_OTP",
                    phoneNumber: values.phoneNumber,
                    smsEnum: "SMS_OTP_REGISTER",
                });
            }

            setOtpSent(true);
            setOtp("");
            setOtpExpiresIn(OTP_EXPIRY_SECONDS);
            setResendCooldown(RESEND_COOLDOWN_SECONDS);
            Alert.alert(
                "OTP sent",
                otpMethod === "EMAIL_OTP" ? "Please check your email." : "Please check your phone messages."
            );
        } catch (error: any) {
            const message = getApiMessage(error);
            setError(message);

            if (isOtpRestricted(error)) {
                Alert.alert(
                    "OTP temporarily blocked",
                    `Please wait about ${OTP_RESTRICTED_MINUTES} minutes before trying again.`
                );
            } else {
                Alert.alert("Cannot send OTP", message);
            }
        } finally {
            setIsSendingOtp(false);
        }
    }

    async function handleRegister() {
        setError(null);

        const values = validateAccountFields();
        if (!values) return;

        if (!values.otp) {
            Alert.alert("Missing OTP", "Please enter the OTP code.");
            return;
        }

        if (!otpSent) {
            Alert.alert("Send OTP first", "Please request an OTP before creating your account.");
            return;
        }

        if (otpExpiresIn <= 0) {
            Alert.alert("OTP expired", "Please request a new OTP code.");
            return;
        }

        try {
            setIsRegistering(true);

            await register({
                username: values.username,
                email: values.email,
                phoneNumber: values.phoneNumber || undefined,
                dob: values.dob,
                password: values.password,
                otp: values.otp,
            });

            Alert.alert("Account created", "You can now sign in.", [
                {
                    text: "Go to login",
                    onPress: () => router.replace("/login" as any),
                },
            ]);
        } catch (error: any) {
            const message = getApiMessage(error);
            setError(message);
            Alert.alert("Registration failed", message);
        } finally {
            setIsRegistering(false);
        }
    }

    return (
        <AppScreen keyboardAvoiding contentContainerStyle={styles.screenContent}>
            <Pressable onPress={() => router.replace("/login" as any)} style={styles.backButton}>
                <Ionicons name="chevron-back" size={20} color={colors.primary} />
                <Text style={[styles.backText, { color: colors.primary }]}>Back to login</Text>
            </Pressable>

            <View style={styles.header}>
                <View style={[styles.logoBadge, { backgroundColor: colors.primarySoft, borderColor: colors.border }]}>
                    <Ionicons name="person-add-outline" size={26} color={colors.primary} />
                </View>
                <Text style={[styles.title, { color: colors.text }]}>Create account</Text>
                <Text style={[styles.subtitle, { color: colors.textMuted }]}>Create your account in a few quick steps.</Text>
                <Text style={[styles.requiredNote, { color: colors.textMuted }]}>
                    * Fields are required. Phone number is optional unless you choose phone OTP.
                </Text>
            </View>

            <AppCard style={styles.card} contentStyle={styles.cardContent}>
                <View style={styles.stepHeader}>
                    <Text style={[styles.stepText, { color: colors.primary }]}>Step {step} of 2</Text>
                    <Text style={[styles.stepTitle, { color: colors.text }]}>{getStepTitle(step)}</Text>
                </View>

                {step === 1 ? (
                    <View style={styles.section}>
                        <AppInput
                            label="Username"
                            required
                            value={username}
                            onChangeText={setUsername}
                            placeholder="3-20 characters"
                            autoCapitalize="none"
                            autoCorrect={false}
                            leftIcon={<Ionicons name="person-outline" size={20} color={colors.textMuted} />}
                        />

                        <AppInput
                            label="Email"
                            required
                            value={email}
                            onChangeText={setEmail}
                            placeholder="name@example.com"
                            keyboardType="email-address"
                            autoCapitalize="none"
                            autoCorrect={false}
                            leftIcon={<Ionicons name="mail-outline" size={20} color={colors.textMuted} />}
                        />

                        <AppInput
                            label="Phone number"
                            value={phoneNumber}
                            onChangeText={setPhoneNumber}
                            placeholder="Phone number"
                            keyboardType="phone-pad"
                            leftIcon={<Ionicons name="call-outline" size={20} color={colors.textMuted} />}
                        />

                        <View style={styles.dateField}>
                            <Text style={[styles.label, { color: colors.text }]}>
                                Date of birth <Text style={[styles.requiredMark, { color: colors.danger }]}>*</Text>
                            </Text>

                            <Pressable
                                accessibilityRole="button"
                                onPress={() => setShowDobPicker(true)}
                                style={({ pressed }) => [
                                    styles.dateButton,
                                    {
                                        backgroundColor: colors.inputBackground,
                                        borderColor: colors.border,
                                    },
                                    pressed && styles.dateButtonPressed,
                                ]}
                            >
                                <Ionicons name="calendar-outline" size={20} color={colors.textMuted} />
                                <Text style={[
                                    styles.dateText,
                                    { color: dob ? colors.text : colors.placeholder },
                                ]}>
                                    {dob || "Select your date of birth"}
                                </Text>
                            </Pressable>

                            {showDobPicker ? (
                                <View style={[styles.pickerContainer, { backgroundColor: colors.surface, borderColor: colors.border }]}>
                                    <DateTimePicker
                                        value={dobDate ?? getDefaultDobDate()}
                                        mode="date"
                                        display="spinner"
                                        maximumDate={getMaximumDobDate()}
                                        minimumDate={getMinimumDobDate()}
                                        onChange={handleDobValueChange}
                                    />

                                    {Platform.OS === "ios" ? (
                                        <View style={[styles.pickerActionRow, { borderTopColor: colors.border }]}>
                                            <AppButton
                                                title="Done"
                                                onPress={() => setShowDobPicker(false)}
                                                fullWidth={false}
                                                size="sm"
                                            />
                                        </View>
                                    ) : null}
                                </View>
                            ) : null}
                        </View>

                        <AppInput
                            label="Password"
                            required
                            value={password}
                            onChangeText={setPassword}
                            placeholder="8-20 characters"
                            secureTextEntry={!isPasswordVisible}
                            autoCapitalize="none"
                            autoCorrect={false}
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
                            label="Confirm password"
                            required
                            value={confirmPassword}
                            onChangeText={setConfirmPassword}
                            placeholder="Repeat password"
                            secureTextEntry={!isPasswordVisible}
                            autoCapitalize="none"
                            autoCorrect={false}
                            leftIcon={<Ionicons name="lock-closed-outline" size={20} color={colors.textMuted} />}
                        />

                        <ErrorMessage message={error} title="Registration check failed" />

                        <AppButton
                            title="Continue"
                            onPress={() => {
                                void handleContinueToOtpStep();
                            }}
                            loading={isSendingOtp}
                            rightIcon={<Ionicons name="arrow-forward" size={18} color={colors.textLight} />}
                        />
                    </View>
                ) : null}

                {step === 2 ? (
                    <View style={styles.section}>
                        <Text style={[styles.helperText, { color: colors.textMuted }]}>
                            Choose where you want to receive your verification code.
                        </Text>

                        <View style={styles.methodSection}>
                            <Text style={[styles.sectionLabel, { color: colors.text }]}>OTP method</Text>
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
                        </View>

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

                        {otpSent ? (
                            <Text style={[styles.timerText, { color: colors.textMuted }]}>
                                OTP expires in {formatTimer(otpExpiresIn)}{" "}
                                {resendCooldown > 0 ? `• Resend in ${formatTimer(resendCooldown)}` : ""}
                            </Text>
                        ) : resendCooldown > 0 ? (
                            <Text style={[styles.timerText, { color: colors.textMuted }]}>You can resend in {formatTimer(resendCooldown)}</Text>
                        ) : null}

                        <AppInput
                            label="OTP code"
                            required
                            value={otp}
                            onChangeText={setOtp}
                            placeholder="Enter 6-digit OTP"
                            keyboardType="number-pad"
                            leftIcon={<Ionicons name="key-outline" size={20} color={colors.textMuted} />}
                        />

                        <ErrorMessage message={error} title="Registration failed" />

                        <AppButton
                            title="Create account"
                            onPress={() => {
                                void handleRegister();
                            }}
                            loading={isRegistering}
                        />

                        <AppButton
                            title="Back to account information"
                            onPress={() => setStep(1)}
                            variant="ghost"
                        />
                    </View>
                ) : null}
            </AppCard>
        </AppScreen>
    );
}

type MethodButtonProps = Readonly<{
    label: string;
    icon: keyof typeof Ionicons.glyphMap;
    selected: boolean;
    onPress: () => void;
}>;

function MethodButton({ label, icon, selected, onPress }: MethodButtonProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <Pressable
            accessibilityRole="button"
            accessibilityState={{ selected }}
            onPress={onPress}
            style={({ pressed }) => [
                styles.methodButton,
                { backgroundColor: colors.surface, borderColor: colors.border },
                selected && { backgroundColor: colors.primarySoft, borderColor: colors.primary },
                pressed && styles.methodButtonPressed,
            ]}
        >
            <Ionicons name={icon} size={18} color={selected ? colors.primary : colors.textMuted} />
            <Text style={[styles.methodText, { color: selected ? colors.primary : colors.textMuted }]}>{label}</Text>
        </Pressable>
    );
}

type OtpCooldownBadgeProps = Readonly<{
    seconds: number;
}>;

function OtpCooldownBadge({ seconds }: OtpCooldownBadgeProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    if (seconds <= 0) {
        return null;
    }

    return (
        <View
            style={[
                styles.cooldownBadge,
                { backgroundColor: colors.warningSoft, borderColor: colors.warning },
            ]}
        >
            <Ionicons name="time-outline" size={15} color={colors.warning} />
            <Text style={[styles.cooldownBadgeText, { color: colors.warning }]}>Wait {formatTimer(seconds)}</Text>
        </View>
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
    requiredNote: {
        color: staticColors.textMuted,
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
        lineHeight: 18,
        textAlign: "center",
        maxWidth: 330,
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
    cooldownBadge: {
        minHeight: 38,
        borderRadius: radius.pill,
        borderWidth: 1,
        paddingHorizontal: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.xs,
    },
    cooldownBadgeText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
    },
    label: {
        color: staticColors.text,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.semibold,
    },
    requiredMark: {
        color: staticColors.danger,
    },
    dateField: {
        gap: spacing.sm,
    },
    dateButton: {
        minHeight: 52,
        borderWidth: 1,
        borderColor: staticColors.border,
        borderRadius: radius.md,
        backgroundColor: staticColors.surface,
        flexDirection: "row",
        alignItems: "center",
        paddingHorizontal: spacing.md,
        gap: spacing.sm,
    },
    dateButtonPressed: {
        opacity: 0.86,
    },
    dateText: {
        flex: 1,
        color: staticColors.text,
        fontSize: typography.body,
        fontWeight: fontWeight.medium,
    },
    datePlaceholder: {
        color: staticColors.textMuted,
    },
    pickerContainer: {
        borderWidth: 1,
        borderColor: staticColors.border,
        borderRadius: radius.md,
        backgroundColor: staticColors.surface,
        overflow: "hidden",
    },
    pickerActionRow: {
        alignItems: "flex-end",
        padding: spacing.md,
        borderTopWidth: 1,
        borderTopColor: staticColors.border,
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
    methodButton: {
        flex: 1,
        minHeight: 48,
        borderRadius: radius.md,
        borderWidth: 1,
        borderColor: staticColors.border,
        backgroundColor: staticColors.surface,
        alignItems: "center",
        justifyContent: "center",
        flexDirection: "row",
        gap: spacing.sm,
    },
    methodButtonSelected: {
        borderColor: staticColors.primary,
        backgroundColor: staticColors.primarySoft,
    },
    methodButtonPressed: {
        opacity: 0.85,
        transform: [{ scale: 0.99 }],
    },
    methodText: {
        color: staticColors.textMuted,
        fontWeight: fontWeight.bold,
    },
    methodTextSelected: {
        color: staticColors.primary,
    },
    timerText: {
        color: staticColors.textMuted,
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
        lineHeight: 18,
        textAlign: "center",
    },
});