import { useCallback, useState } from "react";
import {
    Alert,
    Image,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useRouter } from "expo-router";

import { getMyProfile, updateMyProfile, updateMySettings } from "@/src/api/userApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import { useAuthStore } from "@/src/stores/authStore";
import { useThemeStore } from "@/src/stores/themeStore";
import type { UserProfile, UserThemePreference } from "@/src/types/user";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";

const THEME_OPTIONS: Array<{
    value: UserThemePreference;
    label: string;
    description: string;
    icon: keyof typeof Ionicons.glyphMap;
}> = [
    {
        value: "SYSTEM",
        label: "System",
        description: "Follow your phone setting later",
        icon: "phone-portrait-outline",
    },
    {
        value: "LIGHT",
        label: "Light",
        description: "Save light mode as your preference",
        icon: "sunny-outline",
    },
    {
        value: "DARK",
        label: "Dark",
        description: "Save dark mode as your preference",
        icon: "moon-outline",
    },
];

export default function ProfileScreen() {
    const router = useRouter();

    const theme = useAppTheme();
    const themedColors = theme.colors;

    const setPreferredTheme = useThemeStore((state) => state.setPreferredTheme);
    const { logoutUser } = useAuthStore();

    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [displayName, setDisplayName] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [dob, setDob] = useState("");
    const [profileImageUrl, setProfileImageUrl] = useState("");

    const [isEditingProfile, setIsEditingProfile] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [isSavingProfile, setIsSavingProfile] = useState(false);
    const [savingTheme, setSavingTheme] = useState<UserThemePreference | null>(null);
    const [isLoggingOut, setIsLoggingOut] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadProfile = useCallback(async () => {
        try {
            setIsLoading(true);
            setError(null);

            const nextProfile = await getMyProfile();

            setProfile(nextProfile);
            syncProfileForm(nextProfile);
            setPreferredTheme(nextProfile.preferredTheme);
        } catch (error: any) {
            setError(getApiErrorMessage(error, "Failed to load your profile."));
        } finally {
            setIsLoading(false);
        }
    }, [setPreferredTheme]);

    useFocusEffect(
        useCallback(() => {
            void loadProfile();
        }, [loadProfile])
    );

    function syncProfileForm(nextProfile: UserProfile) {
        setDisplayName(nextProfile.displayName ?? nextProfile.username ?? "");
        setPhoneNumber(nextProfile.phoneNumber ?? "");
        setDob(nextProfile.dob ?? "");
        setProfileImageUrl(nextProfile.profileImageUrl ?? "");
    }

    async function handleSaveProfile() {
        if (!displayName.trim()) {
            Alert.alert("Missing display name", "Please enter a display name.");
            return;
        }

        try {
            setIsSavingProfile(true);
            setError(null);

            const updatedProfile = await updateMyProfile({
                displayName: displayName.trim(),
                ...(phoneNumber.trim() ? { phoneNumber: phoneNumber.trim() } : {}),
                ...(dob.trim() ? { dob: dob.trim() } : {}),
                ...(profileImageUrl.trim()
                    ? { profileImageUrl: profileImageUrl.trim() }
                    : {}),
            });

            setProfile(updatedProfile);
            syncProfileForm(updatedProfile);
            setIsEditingProfile(false);
        } catch (error: any) {
            setError(getApiErrorMessage(error, "Failed to update your profile."));
        } finally {
            setIsSavingProfile(false);
        }
    }

    async function handleThemeChange(preferredTheme: UserThemePreference) {
        if (profile?.preferredTheme === preferredTheme) {
            return;
        }

        try {
            setSavingTheme(preferredTheme);
            setError(null);

            const updatedProfile = await updateMySettings({ preferredTheme });

            setProfile(updatedProfile);
            syncProfileForm(updatedProfile);
            setPreferredTheme(updatedProfile.preferredTheme);
        } catch (error: any) {
            setError(getApiErrorMessage(error, "Failed to update theme setting."));
        } finally {
            setSavingTheme(null);
        }
    }

    function handleCancelEdit() {
        if (profile) {
            syncProfileForm(profile);
        }

        setIsEditingProfile(false);
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
                    onPress: async () => {
                        try {
                            setIsLoggingOut(true);
                            await logoutUser();
                            router.replace("/login" as any);
                        } finally {
                            setIsLoggingOut(false);
                        }
                    },
                },
            ]
        );
    }

    if (isLoading) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Loading profile..."
                    subtitle="Getting your account and settings."
                    fullScreen
                />
            </AppScreen>
        );
    }

    return (
        <AppScreen keyboardAvoiding contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <ProfileAvatar profile={profile} />

                <View style={styles.headerTextGroup}>
                    <Text
                        style={[
                            styles.eyebrow,
                            { color: themedColors.primary },
                        ]}
                    >
                        Profile
                    </Text>

                    <Text
                        style={[
                            styles.title,
                            { color: themedColors.text },
                        ]}
                    >
                        {profile?.displayName || profile?.username || "Your account"}
                    </Text>

                    <Text
                        style={[
                            styles.subtitle,
                            { color: themedColors.textMuted },
                        ]}
                    >
                        Manage your personal info, app settings, and session.
                    </Text>
                </View>
            </View>

            <ErrorMessage message={error} title="Profile error" />

            <AppCard
                title="Account info"
                subtitle="These details are linked to your WanderMate account."
                footer={
                    isEditingProfile ? (
                        <View style={styles.footerActions}>
                            <AppButton
                                title="Cancel"
                                variant="outline"
                                fullWidth={false}
                                onPress={handleCancelEdit}
                                disabled={isSavingProfile}
                                style={styles.footerButton}
                            />
                            <AppButton
                                title="Save"
                                fullWidth={false}
                                onPress={handleSaveProfile}
                                loading={isSavingProfile}
                                style={styles.footerButton}
                            />
                        </View>
                    ) : (
                        <AppButton
                            title="Edit profile"
                            variant="secondary"
                            onPress={() => setIsEditingProfile(true)}
                            leftIcon={
                                <Ionicons
                                    name="create-outline"
                                    size={20}
                                    color={themedColors.primaryDark}
                                />
                            }
                        />
                    )
                }
            >
                {isEditingProfile ? (
                    <View style={styles.formGroup}>
                        <AppInput
                            label="Display name"
                            value={displayName}
                            onChangeText={setDisplayName}
                            placeholder="How your name appears in the app"
                            leftIcon={
                                <Ionicons
                                    name="person-outline"
                                    size={20}
                                    color={themedColors.textMuted}
                                />
                            }
                        />

                        <AppInput
                            label="Phone number"
                            value={phoneNumber}
                            onChangeText={setPhoneNumber}
                            placeholder="Optional phone number"
                            keyboardType="phone-pad"
                            leftIcon={
                                <Ionicons
                                    name="call-outline"
                                    size={20}
                                    color={themedColors.textMuted}
                                />
                            }
                        />

                        <AppInput
                            label="Date of birth"
                            value={dob}
                            onChangeText={setDob}
                            placeholder="YYYY-MM-DD or DD/MM/YYYY"
                            helperText="For now, type the date manually. We can add a date picker later."
                            leftIcon={
                                <Ionicons
                                    name="calendar-outline"
                                    size={20}
                                    color={themedColors.textMuted}
                                />
                            }
                        />

                        <AppInput
                            label="Profile image URL"
                            value={profileImageUrl}
                            onChangeText={setProfileImageUrl}
                            placeholder="Optional avatar image link"
                            autoCapitalize="none"
                            leftIcon={
                                <Ionicons
                                    name="image-outline"
                                    size={20}
                                    color={themedColors.textMuted}
                                />
                            }
                        />
                    </View>
                ) : (
                    <View style={styles.detailList}>
                        <DetailRow
                            icon="person-outline"
                            label="Username"
                            value={profile?.username}
                        />
                        <DetailRow
                            icon="mail-outline"
                            label="Email"
                            value={profile?.email}
                        />
                        <DetailRow
                            icon="call-outline"
                            label="Phone"
                            value={profile?.phoneNumber || "Not added yet"}
                        />
                        <DetailRow
                            icon="calendar-outline"
                            label="Date of birth"
                            value={profile?.dob || "Not added yet"}
                        />
                    </View>
                )}
            </AppCard>

            <AppCard
                title="Appearance"
                subtitle="Choose how WanderMate should remember your theme preference."
            >
                <View style={styles.themeOptionList}>
                    {THEME_OPTIONS.map((option) => {
                        const isSelected = profile?.preferredTheme === option.value;
                        const isSavingThisTheme = savingTheme === option.value;

                        return (
                            <Pressable
                                key={option.value}
                                accessibilityRole="button"
                                accessibilityState={{
                                    selected: isSelected,
                                    busy: isSavingThisTheme,
                                }}
                                disabled={savingTheme !== null}
                                onPress={() => handleThemeChange(option.value)}
                                style={({ pressed }) => [
                                    styles.themeOption,
                                    {
                                        backgroundColor: themedColors.surface,
                                        borderColor: themedColors.border,
                                    },
                                    isSelected && {
                                        backgroundColor: themedColors.primarySoft,
                                        borderColor: themedColors.primary,
                                    },
                                    pressed && styles.pressed,
                                    savingTheme !== null && styles.disabled,
                                ]}
                            >
                                <View
                                    style={[
                                        styles.themeIconBadge,
                                        {
                                            backgroundColor: themedColors.primarySoft,
                                        },
                                        isSelected && {
                                            backgroundColor: themedColors.primary,
                                        },
                                    ]}
                                >
                                    <Ionicons
                                        name={option.icon}
                                        size={22}
                                        color={
                                            isSelected
                                                ? themedColors.textLight
                                                : themedColors.primary
                                        }
                                    />
                                </View>

                                <View style={styles.themeTextGroup}>
                                    <Text
                                        style={[
                                            styles.themeLabel,
                                            { color: themedColors.text },
                                        ]}
                                    >
                                        {option.label}
                                    </Text>

                                    <Text
                                        style={[
                                            styles.themeDescription,
                                            { color: themedColors.textMuted },
                                        ]}
                                    >
                                        {option.description}
                                    </Text>
                                </View>

                                {isSavingThisTheme ? (
                                    <Text
                                        style={[
                                            styles.savingText,
                                            { color: themedColors.textMuted },
                                        ]}
                                    >
                                        Saving...
                                    </Text>
                                ) : isSelected ? (
                                    <Ionicons
                                        name="checkmark-circle"
                                        size={23}
                                        color={themedColors.success}
                                    />
                                ) : null}
                            </Pressable>
                        );
                    })}
                </View>
            </AppCard>

            <AppCard
                title="Security"
                subtitle="Your current login uses access, refresh, and session tokens."
            >
                <AppButton
                    title="Log out"
                    variant="danger"
                    onPress={handleLogout}
                    loading={isLoggingOut}
                    leftIcon={
                        <Ionicons
                            name="log-out-outline"
                            size={20}
                            color={themedColors.textLight}
                        />
                    }
                />
            </AppCard>
        </AppScreen>
    );
}

type ProfileAvatarProps = Readonly<{
    profile: UserProfile | null;
}>;

function ProfileAvatar({ profile }: ProfileAvatarProps) {
    const theme = useAppTheme();
    const themedColors = theme.colors;
    const [failedImageUrl, setFailedImageUrl] = useState<string | null>(null);

    const imageUrl = profile?.profileImageUrl?.trim() || null;
    const shouldShowImage = Boolean(imageUrl) && failedImageUrl !== imageUrl;
    const fallbackInitial = (profile?.displayName || profile?.username || "W")
        .trim()
        .charAt(0)
        .toUpperCase() || "W";

    if (shouldShowImage) {
        return (
            <Image
                source={{ uri: imageUrl as string }}
                style={[
                    styles.avatarBadge,
                    { backgroundColor: themedColors.primary },
                ]}
                onError={() => setFailedImageUrl(imageUrl)}
            />
        );
    }

    return (
        <View
            style={[
                styles.avatarBadge,
                { backgroundColor: themedColors.primary },
            ]}
        >
            <Text
                style={[
                    styles.avatarText,
                    { color: themedColors.textLight },
                ]}
            >
                {fallbackInitial}
            </Text>
        </View>
    );
}

type DetailRowProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    label: string;
    value?: string | null;
}>;

function DetailRow({ icon, label, value }: DetailRowProps) {
    const theme = useAppTheme();
    const themedColors = theme.colors;

    return (
        <View style={styles.detailRow}>
            <View
                style={[
                    styles.detailIconBadge,
                    { backgroundColor: themedColors.primarySoft },
                ]}
            >
                <Ionicons
                    name={icon}
                    size={19}
                    color={themedColors.primary}
                />
            </View>

            <View style={styles.detailTextGroup}>
                <Text
                    style={[
                        styles.detailLabel,
                        { color: themedColors.textMuted },
                    ]}
                >
                    {label}
                </Text>

                <Text
                    style={[
                        styles.detailValue,
                        { color: themedColors.text },
                    ]}
                >
                    {value || "Not added yet"}
                </Text>
            </View>
        </View>
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
        gap: spacing.md,
    },
    avatarBadge: {
        width: 68,
        height: 68,
        borderRadius: radius.xl,
        alignItems: "center",
        justifyContent: "center",
    },
    avatarText: {
        fontSize: typography.heading,
        fontWeight: fontWeight.bold,
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
        fontSize: typography.heading,
        fontWeight: fontWeight.bold,
        lineHeight: 32,
    },
    subtitle: {
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    formGroup: {
        gap: spacing.md,
    },
    detailList: {
        gap: spacing.md,
    },
    detailRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    detailIconBadge: {
        width: 40,
        height: 40,
        borderRadius: radius.md,
        alignItems: "center",
        justifyContent: "center",
    },
    detailTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    detailLabel: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.4,
    },
    detailValue: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.semibold,
    },
    footerActions: {
        flexDirection: "row",
        gap: spacing.md,
    },
    footerButton: {
        flex: 1,
    },
    themeOptionList: {
        gap: spacing.md,
    },
    themeOption: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
        borderWidth: 1,
        borderRadius: radius.lg,
        padding: spacing.md,
    },
    themeIconBadge: {
        width: 42,
        height: 42,
        borderRadius: radius.md,
        alignItems: "center",
        justifyContent: "center",
    },
    themeTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    themeLabel: {
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    themeDescription: {
        fontSize: typography.caption,
        lineHeight: 18,
    },
    savingText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
    },
    pressed: {
        opacity: 0.88,
        transform: [{ scale: 0.995 }],
    },
    disabled: {
        opacity: 0.65,
    },
});