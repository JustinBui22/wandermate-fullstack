import {useEffect, useState} from "react";
import DateTimePicker from "@react-native-community/datetimepicker";
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

import {register, sendOtp, verifyRegisterDetails} from "@/src/api/authApi";
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

export default function RegisterScreen() {
    const router = useRouter();
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

    function getDefaultDobDate() {
        const date = new Date();
        date.setFullYear(date.getFullYear() - 18);
        date.setHours(0, 0, 0, 0);

        return date;
    }

    function handleDobValueChange(_event: unknown, selectedDate: Date) {
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

        const values = getTrimmedValues();

        if (
            !values.username ||
            !values.email ||
            !values.dob ||
            !values.password ||
            !values.confirmPassword
        ) {
            Alert.alert("Missing details", "Please fill in all required fields.");
            return;
        }

        if (values.password !== values.confirmPassword) {
            Alert.alert("Password mismatch", "Password and confirm password do not match.");
            return;
        }

        try {
            setIsSendingOtp(true);

            if (values.password !== values.confirmPassword) {
                Alert.alert("Password mismatch", "Password and confirm password do not match.");
                return;
            }
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
            Alert.alert("OTP sent", otpMethod === "EMAIL_OTP" ? "Please check your email." : "Please check your phone messages.");
        } catch (error: any) {
            const message = getApiMessage(error);
            setError(message);

            if (isOtpRestricted(error)) {
                Alert.alert("OTP temporarily blocked", `Please wait about ${OTP_RESTRICTED_MINUTES} minutes before trying again.`);
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
                    onPress: () => router.replace("/login"),
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
                            <Text style={styles.title}>Create account</Text>
                            <Text style={styles.subtitle}>
                                Create your account in a few quick steps.
                            </Text>
                            <Text style={styles.requiredNote}>* Fields are required. Phone number is optional unless you choose phone OTP.</Text>
                        </View>

                        <View style={styles.card}>
                            <View style={styles.stepHeader}>
                                <Text style={styles.stepText}>Step {step} of 2</Text>
                                <Text style={styles.stepTitle}>
                                    {step === 1 ? "Account information" : "Verify your account"}
                                </Text>
                            </View>

                            {step === 1 ? (
                                <>
                                    <AuthInput
                                        label="Username"
                                        required
                                        icon="person-outline"
                                        value={username}
                                        onChangeText={setUsername}
                                        placeholder="3-20 characters"
                                    />

                                    <AuthInput
                                        label="Email"
                                        required
                                        icon="mail-outline"
                                        value={email}
                                        onChangeText={setEmail}
                                        placeholder="name@example.com"
                                        keyboardType="email-address"
                                    />

                                    <AuthInput
                                        label="Phone number"
                                        icon="call-outline"
                                        value={phoneNumber}
                                        onChangeText={setPhoneNumber}
                                        placeholder="Phone number"
                                        keyboardType="phone-pad"
                                    />

                                    <View style={styles.inputGroup}>
                                        <Text style={styles.label}>
                                            Date of birth
                                            <Text style={styles.requiredMark}> *</Text>
                                        </Text>

                                        <Pressable
                                            onPress={() => setShowDobPicker(true)}
                                            style={styles.inputWrapper}
                                        >
                                            <Ionicons name="calendar-outline" size={20} color={colors.mutedText} />

                                            <Text
                                                style={[
                                                    styles.datePickerText,
                                                    !dob ? styles.datePickerPlaceholder : null,
                                                ]}
                                            >
                                                {dob || "Select your date of birth"}
                                            </Text>
                                        </Pressable>

                                        {showDobPicker ? (
                                            <>
                                                <DateTimePicker
                                                    value={dobDate ?? getDefaultDobDate()}
                                                    mode="date"
                                                    display="spinner"
                                                    // display={Platform.OS === "ios" ? "spinner" : "default"}
                                                    maximumDate={getMaximumDobDate()}
                                                    minimumDate={getMinimumDobDate()}
                                                    onValueChange={handleDobValueChange}
                                                />

                                                {Platform.OS === "ios" ? (
                                                    <View style={styles.pickerActionRow}>
                                                        <Pressable
                                                            onPress={() => setShowDobPicker(false)}
                                                            style={styles.pickerDoneButton}
                                                        >
                                                            <Text style={styles.pickerDoneText}>Done</Text>
                                                        </Pressable>
                                                    </View>
                                                ) : null}
                                            </>
                                        ) : null}
                                    </View>

                                    <AuthInput
                                        label="Password"
                                        required
                                        icon="lock-closed-outline"
                                        value={password}
                                        onChangeText={setPassword}
                                        placeholder="8-20 characters"
                                        secureTextEntry={!isPasswordVisible}
                                        rightIcon={isPasswordVisible ? "eye-off-outline" : "eye-outline"}
                                        onRightIconPress={() => setIsPasswordVisible((current) => !current)}
                                    />

                                    <AuthInput
                                        label="Confirm password"
                                        required
                                        icon="lock-closed-outline"
                                        value={confirmPassword}
                                        onChangeText={setConfirmPassword}
                                        placeholder="Repeat password"
                                        secureTextEntry={!isPasswordVisible}
                                    />

                                    {error ? <ErrorBox message={error}/> : null}

                                    <Pressable
                                        onPress={handleContinueToOtpStep}
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
                                        Choose where you want to receive your verification code.
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

                                    <Pressable
                                        onPress={handleSendOtp}
                                        disabled={isSendingOtp || resendCooldown > 0}
                                        style={({pressed}) => [
                                            styles.secondaryButton,
                                            pressed && !isSendingOtp && resendCooldown <= 0
                                                ? styles.buttonPressed
                                                : null,
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

                                    {otpSent ? (
                                        <Text style={styles.timerText}>
                                            OTP expires in {formatTimer(otpExpiresIn)}{" "}
                                            {resendCooldown > 0 ? `• Resend in ${formatTimer(resendCooldown)}` : ""}
                                        </Text>
                                    ) : null}

                                    <AuthInput
                                        label="OTP code"
                                        required
                                        icon="key-outline"
                                        value={otp}
                                        onChangeText={setOtp}
                                        placeholder="Enter 6-digit OTP"
                                        keyboardType="number-pad"
                                    />

                                    {error ? <ErrorBox message={error}/> : null}

                                    <Pressable
                                        onPress={handleRegister}
                                        disabled={isRegistering}
                                        style={({pressed}) => [
                                            styles.primaryButton,
                                            pressed && !isRegistering ? styles.buttonPressed : null,
                                            isRegistering ? styles.buttonDisabled : null,
                                        ]}
                                    >
                                        {isRegistering ? (
                                            <ActivityIndicator color="#FFFFFF"/>
                                        ) : (
                                            <Text style={styles.primaryButtonText}>Create account</Text>
                                        )}
                                    </Pressable>

                                    <Pressable
                                        onPress={() => setStep(1)}
                                        style={styles.textButton}
                                    >
                                        <Text style={styles.textButtonText}>Back to account information</Text>
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
    required?: boolean;
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
                       onRightIconPress,
                       required = false,
                   }: AuthInputProps) {
    return (
        <View style={styles.inputGroup}>
            <Text style={styles.label}>
                {label}
                {required ? <Text style={styles.requiredMark}> *</Text> : null}
            </Text>
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

type MethodButtonProps = Readonly<{
    label: string;
    icon: keyof typeof Ionicons.glyphMap;
    selected: boolean;
    onPress: () => void;
}>;

function MethodButton({label, icon, selected, onPress}: MethodButtonProps) {
    return (
        <Pressable onPress={onPress} style={[styles.methodButton, selected ? styles.methodButtonSelected : null]}>
            <Ionicons name={icon} size={18} color={selected ? colors.primary : colors.mutedText}/>
            <Text style={[styles.methodText, selected ? styles.methodTextSelected : null]}>{label}</Text>
        </Pressable>
    );
}

type ErrorBoxProps = Readonly<{
    message: string;
}>;

function ErrorBox({message}: ErrorBoxProps) {
    return (
        <View style={styles.errorBox}>
            <Ionicons name="alert-circle-outline" size={18} color={colors.error}/>
            <Text style={styles.errorText}>{message}</Text>
        </View>
    );
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

    requiredNote: {
        marginTop: spacing.sm,
        color: colors.mutedText,
        fontSize: 13,
        fontWeight: "700",
    },
    requiredMark: {
        color: colors.error,
        fontWeight: "900",
    },
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
    datePickerText: {
        flex: 1,
        color: colors.text,
        fontSize: 15,
        fontWeight: "600",
    },
    datePickerPlaceholder: {
        color: colors.mutedText,
    },
    pickerActionRow: {
        alignItems: "flex-end",
        marginTop: spacing.sm,
    },

    pickerDoneButton: {
        backgroundColor: colors.primary,
        borderRadius: 12,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.sm,
    },

    pickerDoneText: {
        color: "#FFFFFF",
        fontWeight: "800",
        fontSize: 14,
    },
});
