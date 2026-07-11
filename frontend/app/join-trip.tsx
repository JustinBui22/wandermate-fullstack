import { useEffect, useMemo, useState } from "react";
import { Alert, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useLocalSearchParams, useRouter } from "expo-router";

import { previewTripShareCode, requestToJoinByShareCode } from "@/src/api/tripCollaborationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { colors as staticColors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { TripShareCodePreview } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { formatDateTime } from "@/src/utils/dateFormat";

const SHARE_CODE_PATTERN = /WM-[A-Z0-9]{8}/i;

function extractShareCode(value: string) {
    const match = value.match(SHARE_CODE_PATTERN);
    return (match?.[0] ?? value).trim().toUpperCase();
}

export default function JoinTripScreen() {
    const router = useRouter();
    const theme = useAppTheme();
    const colors = theme.colors;
    const params = useLocalSearchParams();
    const codeParam = Array.isArray(params.code) ? params.code[0] : params.code;

    const [inputValue, setInputValue] = useState(codeParam ? String(codeParam) : "");
    const [preview, setPreview] = useState<TripShareCodePreview | null>(null);
    const [isPreviewing, setIsPreviewing] = useState(false);
    const [isRequesting, setIsRequesting] = useState(false);

    const normalizedCode = useMemo(() => extractShareCode(inputValue), [inputValue]);

    useEffect(() => {
        if (codeParam) {
            const extractedCode = extractShareCode(String(codeParam));
            setInputValue(extractedCode);
            handlePreviewCode(extractedCode);
        }
    }, [codeParam]);

    async function handlePreviewCode(codeOverride?: string) {
        const code = extractShareCode(codeOverride ?? inputValue);

        if (!code) {
            Alert.alert("Missing code", "Enter or paste an invite code first.");
            return;
        }

        try {
            setIsPreviewing(true);
            const result = await previewTripShareCode(code);
            setInputValue(code);
            setPreview(result);
        } catch (error: any) {
            setPreview(null);
            Alert.alert("Preview failed", getApiErrorMessage(error, "Please check the code and try again."));
        } finally {
            setIsPreviewing(false);
        }
    }

    async function handleRequestToJoin() {
        if (!preview) {
            Alert.alert("Preview required", "Preview the trip before requesting to join.");
            return;
        }

        try {
            setIsRequesting(true);
            await requestToJoinByShareCode(normalizedCode);
            Alert.alert(
                "Request sent",
                "Your join request was sent to the trip owner.",
                [{ text: "OK", onPress: () => router.replace("/(tabs)/collaboration" as any) }]
            );
        } catch (error: any) {
            Alert.alert("Request failed", getApiErrorMessage(error, "Please try again."));
        } finally {
            setIsRequesting(false);
        }
    }

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <AppCard onPress={() => router.back()} style={styles.backButton} contentStyle={styles.backButtonContent}>
                    <Ionicons name="chevron-back" size={22} color={colors.text} />
                </AppCard>

                <View style={styles.headerTextGroup}>
                    <Text style={[styles.eyebrow, { color: colors.primary }]}>Join trip</Text>
                    <Text style={[styles.title, { color: colors.text }]}>Use invite code</Text>
                    <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                        Paste a code, deep link, or full invite message. WanderMate will extract the code automatically.
                    </Text>
                </View>
            </View>

            <AppCard contentStyle={styles.formContent}>
                <AppInput
                    label="Invite code"
                    value={inputValue}
                    onChangeText={(value) => {
                        setInputValue(value);
                        setPreview(null);
                    }}
                    autoCapitalize="characters"
                    autoCorrect={false}
                    placeholder="WM-ABC12345"
                    helperText="You can paste only the code, the link, or the whole invite message."
                    leftIcon={<Ionicons name="key-outline" size={20} color={colors.textMuted} />}
                />

                {normalizedCode ? (
                    <View style={[styles.extractedBox, { backgroundColor: colors.primarySoft }]}>
                        <Text style={[styles.extractedLabel, { color: colors.primary }]}>Detected code</Text>
                        <Text style={[styles.extractedCode, { color: colors.primary }]}>{normalizedCode}</Text>
                    </View>
                ) : null}

                <AppButton
                    title="Preview Trip"
                    onPress={() => {
                        void handlePreviewCode();
                    }}
                    loading={isPreviewing}
                    leftIcon={<Ionicons name="search-outline" size={19} color={colors.textLight} />}
                />
            </AppCard>

            {preview ? (
                <AppCard title="Trip preview" subtitle="Check this is the correct trip before requesting access." contentStyle={styles.previewContent}>
                    <View style={styles.tripTitleRow}>
                        <View style={[styles.tripIconBadge, { backgroundColor: colors.primarySoft }]}>
                            <Ionicons name="map" size={22} color={colors.primary} />
                        </View>

                        <View style={styles.tripTextGroup}>
                            <Text style={[styles.tripTitle, { color: colors.text }]}>{preview.tripName}</Text>
                            <Text style={[styles.tripSubtitle, { color: colors.textMuted }]}>{preview.destination || "No destination"}</Text>
                        </View>

                        <RoleBadge role={preview.defaultRole} />
                    </View>

                    <InfoRow label="Owner" value={preview.ownerUsername} icon="person-outline" />
                    <InfoRow label="Start" value={formatDateTime(preview.startDate)} icon="calendar-outline" />
                    <InfoRow label="End" value={formatDateTime(preview.endDate)} icon="flag-outline" />

                    <View style={[styles.expiryBox, { borderColor: colors.warning, backgroundColor: colors.warningSoft }]}>
                        <Ionicons name="time-outline" size={22} color={colors.warning} />
                        <View style={styles.expiryTextGroup}>
                            <Text style={[styles.expiryTitle, { color: colors.warning }]}>Invite expires {formatDateTime(preview.expiresAt)}</Text>
                            <Text style={[styles.expirySubtitle, { color: colors.warning }]}>This invite code is single-use and may become invalid after it is used.</Text>
                        </View>
                    </View>

                    <AppButton
                        title="Request to Join"
                        onPress={() => {
                            void handleRequestToJoin();
                        }}
                        loading={isRequesting}
                        leftIcon={<Ionicons name="paper-plane-outline" size={19} color={colors.textLight} />}
                    />
                </AppCard>
            ) : null}
        </AppScreen>
    );
}

type InfoRowProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    label: string;
    value: string;
}>;

function InfoRow({ icon, label, value }: InfoRowProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View style={[styles.infoRow, { backgroundColor: colors.surfaceSoft }]}>
            <Ionicons name={icon} size={18} color={colors.textMuted} />
            <Text style={[styles.infoLabel, { color: colors.textMuted }]}>{label}</Text>
            <Text style={[styles.infoValue, { color: colors.text }]}>{value}</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    screenContent: { paddingTop: spacing.lg, paddingBottom: spacing.xxl, gap: spacing.lg },
    header: { gap: spacing.lg },
    backButton: { width: 46, height: 46, borderRadius: radius.lg },
    backButtonContent: { flex: 1, padding: 0, alignItems: "center", justifyContent: "center" },
    headerTextGroup: { gap: spacing.xs },
    eyebrow: { color: staticColors.primary, fontSize: typography.caption, fontWeight: fontWeight.bold, textTransform: "uppercase", letterSpacing: 0.7 },
    title: { color: staticColors.text, fontSize: typography.heading, fontWeight: fontWeight.bold },
    subtitle: { color: staticColors.textMuted, fontSize: typography.bodySmall, lineHeight: 21 },
    formContent: { gap: spacing.lg },
    extractedBox: { borderRadius: radius.lg, backgroundColor: staticColors.primarySoft, padding: spacing.md, gap: spacing.xs },
    extractedLabel: { color: staticColors.primary, fontSize: typography.caption, fontWeight: fontWeight.bold, textTransform: "uppercase", letterSpacing: 0.5 },
    extractedCode: { color: staticColors.primary, fontSize: typography.title, fontWeight: fontWeight.bold, letterSpacing: 1 },
    previewContent: { gap: spacing.lg },
    tripTitleRow: { flexDirection: "row", alignItems: "center", gap: spacing.md },
    tripIconBadge: { width: 46, height: 46, borderRadius: radius.lg, backgroundColor: staticColors.primarySoft, alignItems: "center", justifyContent: "center" },
    tripTextGroup: { flex: 1, gap: spacing.xs },
    tripTitle: { color: staticColors.text, fontSize: typography.body, fontWeight: fontWeight.bold },
    tripSubtitle: { color: staticColors.textMuted, fontSize: typography.bodySmall, lineHeight: 20 },
    infoRow: { flexDirection: "row", alignItems: "center", gap: spacing.sm, borderRadius: radius.md, backgroundColor: staticColors.surfaceSoft, padding: spacing.md },
    infoLabel: { color: staticColors.textMuted, fontSize: typography.caption, fontWeight: fontWeight.bold, minWidth: 46, textTransform: "uppercase", letterSpacing: 0.4 },
    infoValue: { flex: 1, color: staticColors.text, fontSize: typography.bodySmall, fontWeight: fontWeight.semibold },
    expiryBox: { flexDirection: "row", alignItems: "flex-start", gap: spacing.md, borderRadius: radius.lg, borderWidth: 1, borderColor: staticColors.warning, backgroundColor: staticColors.warningSoft, padding: spacing.md },
    expiryTextGroup: { flex: 1, gap: spacing.xs },
    expiryTitle: { color: staticColors.warning, fontSize: typography.bodySmall, fontWeight: fontWeight.bold },
    expirySubtitle: { color: staticColors.warning, fontSize: typography.caption, lineHeight: 18, fontWeight: fontWeight.semibold },
});