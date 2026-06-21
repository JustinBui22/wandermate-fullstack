import { useCallback, useEffect, useState } from "react";
import {
    Alert,
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useLocalSearchParams, useRouter } from "expo-router";

import {
    previewTripShareCode,
    requestToJoinByShareCode,
} from "@/src/api/tripCollaborationApi";
import { RoleBadge } from "@/src/components/collaboration/RoleBadge";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppInput } from "@/src/components/ui/AppInput";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type { TripShareCodePreview } from "@/src/types/tripCollaboration";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";
import { formatDateTime } from "@/src/utils/dateFormat";

function getCodeParam(value: string | string[] | undefined) {
    if (Array.isArray(value)) {
        return value[0] ?? "";
    }

    return value ?? "";
}

const INVITE_CODE_PATTERN = /WM-[A-Z0-9]{8}/i;

function extractInviteCode(value: string) {
    const trimmedValue = value.trim();

    if (!trimmedValue) {
        return "";
    }

    const matchedCode = trimmedValue.match(INVITE_CODE_PATTERN);

    if (matchedCode?.[0]) {
        return matchedCode[0].toUpperCase();
    }

    return trimmedValue.toUpperCase();
}

export default function JoinTripScreen() {
    const router = useRouter();
    const params = useLocalSearchParams();
    const codeFromLink = getCodeParam(params.code as string | string[] | undefined);

    const [codeInput, setCodeInput] = useState(extractInviteCode(codeFromLink));
    const [preview, setPreview] = useState<TripShareCodePreview | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [isPreviewLoading, setIsPreviewLoading] = useState(false);
    const [isRequestingJoin, setIsRequestingJoin] = useState(false);

    const loadPreview = useCallback(async (rawCode: string) => {
        const code = extractInviteCode(rawCode);

        if (!code) {
            setPreview(null);
            setError("Enter an invite code first.");
            return;
        }

        try {
            setIsPreviewLoading(true);
            setError(null);

            const data = await previewTripShareCode(code);
            setPreview(data);
            setCodeInput(code);
        } catch (error: any) {
            setPreview(null);
            setError(getApiErrorMessage(error, "Could not preview this invite code."));
        } finally {
            setIsPreviewLoading(false);
        }
    }, []);

    useEffect(() => {
        if (codeFromLink) {
            loadPreview(codeFromLink);
        }
    }, [codeFromLink, loadPreview]);

    async function handlePreviewCode() {
        await loadPreview(codeInput);
    }

    async function handleRequestToJoin() {
        const code = extractInviteCode(codeInput);

        if (!code) {
            Alert.alert("Missing invite code", "Enter the invite code first.");
            return;
        }

        try {
            setIsRequestingJoin(true);

            const request = await requestToJoinByShareCode(code);

            Alert.alert(
                "Request sent",
                `Your request to join ${request.tripName || "this trip"} was sent to the owner.`
            );

            router.replace("/(tabs)/collaboration" as any);
        } catch (error: any) {
            Alert.alert(
                "Join request failed",
                getApiErrorMessage(error, "Please check the invite code and try again.")
            );
        } finally {
            setIsRequestingJoin(false);
        }
    }

    return (
        <AppScreen contentContainerStyle={styles.screenContent}>
            <View style={styles.header}>
                <Pressable
                    accessibilityRole="button"
                    accessibilityLabel="Go back"
                    onPress={() => router.back()}
                    style={({ pressed }) => [
                        styles.headerIconButton,
                        pressed && styles.pressed,
                    ]}
                >
                    <Ionicons name="chevron-back" size={23} color={colors.text} />
                </Pressable>

                <View style={styles.headerTextGroup}>
                    <Text style={styles.eyebrow}>WanderMate</Text>
                    <Text style={styles.title}>Join Trip</Text>
                    <Text style={styles.subtitle}>
                        Enter an invite code or open an invite link to request access.
                    </Text>
                </View>
            </View>

            <AppCard
                title="Invite code"
                subtitle="Preview the trip first, then send a request to the owner."
                contentStyle={styles.cardContent}
            >
                <AppInput
                    label="Code"
                    value={codeInput}
                    onChangeText={(value) => {
                        setCodeInput(value);
                        setPreview(null);
                        setError(null);
                    }}
                    placeholder="Paste code or full invite message"
                    autoCapitalize="characters"
                    autoCorrect={false}
                    leftIcon={<Ionicons name="key-outline" size={20} color={colors.textMuted} />}
                />

                <AppButton
                    title="Preview Trip"
                    onPress={handlePreviewCode}
                    loading={isPreviewLoading}
                    leftIcon={!isPreviewLoading ? <Ionicons name="search-outline" size={20} color={colors.textLight} /> : null}
                />
            </AppCard>

            <ErrorMessage message={error} title="Invite code problem" />

            {preview ? (
                <AppCard contentStyle={styles.previewCardContent}>
                    <View style={styles.previewTopRow}>
                        <View style={styles.tripIconBadge}>
                            <Ionicons name="map-outline" size={24} color={colors.primary} />
                        </View>

                        <View style={styles.previewTextGroup}>
                            <Text style={styles.previewTitle}>{preview.tripName || "Untitled trip"}</Text>
                            <Text style={styles.previewSubtitle}>
                                Owned by {preview.ownerUsername || "Unknown owner"}
                            </Text>
                        </View>
                    </View>

                    <View style={styles.metaGroup}>
                        <View style={styles.metaRow}>
                            <Ionicons name="location-outline" size={17} color={colors.textMuted} />
                            <Text style={styles.metaText}>{preview.destination || "Destination not set"}</Text>
                        </View>

                        <View style={styles.metaRow}>
                            <Ionicons name="calendar-outline" size={17} color={colors.textMuted} />
                            <Text style={styles.metaText}>
                                {formatDateTime(preview.startDate)} → {formatDateTime(preview.endDate)}
                            </Text>
                        </View>

                        <View style={styles.expiryNoticeBox}>
                            <Ionicons name="time-outline" size={18} color={colors.warning} />
                            <View style={styles.expiryNoticeTextGroup}>
                                <Text style={styles.expiryNoticeLabel}>Invite code expires</Text>
                                <Text style={styles.expiryNoticeValue}>
                                    {formatDateTime(preview.expiresAt)}
                                </Text>
                            </View>
                        </View>
                    </View>

                    <View style={styles.roleRow}>
                        <Text style={styles.roleLabel}>You will request to join as</Text>
                        <RoleBadge role={preview.defaultRole} />
                    </View>

                    <AppButton
                        title="Request to Join"
                        onPress={handleRequestToJoin}
                        loading={isRequestingJoin}
                        leftIcon={!isRequestingJoin ? <Ionicons name="send-outline" size={20} color={colors.textLight} /> : null}
                    />
                </AppCard>
            ) : null}
        </AppScreen>
    );
}

const styles = StyleSheet.create({
    screenContent: {
        paddingTop: spacing.lg,
        paddingBottom: spacing.xxl,
        gap: spacing.lg,
    },
    header: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    headerIconButton: {
        width: 42,
        height: 42,
        borderRadius: radius.md,
        backgroundColor: colors.surface,
        alignItems: "center",
        justifyContent: "center",
        borderWidth: 1,
        borderColor: colors.border,
    },
    headerTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    eyebrow: {
        color: colors.primary,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.7,
    },
    title: {
        color: colors.text,
        fontSize: typography.heading,
        fontWeight: fontWeight.bold,
    },
    subtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    pressed: {
        opacity: 0.86,
        transform: [{ scale: 0.99 }],
    },
    cardContent: {
        gap: spacing.md,
    },
    previewCardContent: {
        gap: spacing.lg,
    },
    previewTopRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    tripIconBadge: {
        width: 50,
        height: 50,
        borderRadius: radius.lg,
        backgroundColor: colors.primarySoft,
        alignItems: "center",
        justifyContent: "center",
    },
    previewTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    previewTitle: {
        color: colors.text,
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    previewSubtitle: {
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    metaGroup: {
        gap: spacing.sm,
    },
    metaRow: {
        flexDirection: "row",
        alignItems: "flex-start",
        gap: spacing.sm,
    },
    metaText: {
        flex: 1,
        color: colors.textMuted,
        fontSize: typography.bodySmall,
        lineHeight: 20,
        fontWeight: fontWeight.semibold,
    },
    expiryNoticeBox: {
        flexDirection: "row",
        alignItems: "flex-start",
        gap: spacing.sm,
        borderRadius: radius.lg,
        borderWidth: 1,
        borderColor: colors.warningSoft,
        backgroundColor: colors.warningSoft,
        padding: spacing.md,
        marginTop: spacing.xs,
    },
    expiryNoticeTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    expiryNoticeLabel: {
        color: colors.warning,
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.5,
    },
    expiryNoticeValue: {
        color: colors.warning,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
        lineHeight: 20,
    },
    roleRow: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.md,
        borderRadius: radius.lg,
        backgroundColor: colors.surfaceSoft,
        borderWidth: 1,
        borderColor: colors.border,
        padding: spacing.md,
    },
    roleLabel: {
        flex: 1,
        color: colors.text,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
});
