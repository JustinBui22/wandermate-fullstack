import { Ionicons } from "@expo/vector-icons";
import { useState } from "react";
import { Image, StyleSheet, Text, View } from "react-native";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { UserProfile } from "@/src/types/user";
import { normalizeImageUrl } from "@/src/utils/imageUrlUtils";

export function ProfileAvatar({ profile }: Readonly<{ profile: UserProfile | null }>) {
    const { colors } = useAppTheme();
    const [failedImageUrl, setFailedImageUrl] = useState<string | null>(null);

    const imageUrl = normalizeImageUrl(profile?.profileImageUrl);
    const shouldShowImage = Boolean(imageUrl) && failedImageUrl !== imageUrl;
    const fallbackInitial = (profile?.displayName || profile?.username || "W")
        .trim()
        .charAt(0)
        .toUpperCase() || "W";

    if (shouldShowImage && imageUrl) {
        return (
            <Image
                source={{ uri: imageUrl }}
                style={[styles.avatar, { backgroundColor: colors.primary }]}
                onError={() => setFailedImageUrl(imageUrl)}
            />
        );
    }

    return (
        <View style={[styles.avatar, { backgroundColor: colors.primary }]}>
            <Text style={[styles.avatarText, { color: colors.textLight }]}>{fallbackInitial}</Text>
        </View>
    );
}

type ProfileDetailRowProps = Readonly<{
    icon: keyof typeof Ionicons.glyphMap;
    label: string;
    value?: string | null;
}>;

export function ProfileDetailRow({ icon, label, value }: ProfileDetailRowProps) {
    const { colors } = useAppTheme();

    return (
        <View style={styles.detailRow}>
            <View style={[styles.detailIcon, { backgroundColor: colors.primarySoft }]}>
                <Ionicons name={icon} size={19} color={colors.primary} />
            </View>
            <View style={styles.detailText}>
                <Text style={[styles.detailLabel, { color: colors.textMuted }]}>{label}</Text>
                <Text style={[styles.detailValue, { color: colors.text }]}>
                    {value || "Not added yet"}
                </Text>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    avatar: {
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
    detailRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    detailIcon: {
        width: 40,
        height: 40,
        borderRadius: radius.md,
        alignItems: "center",
        justifyContent: "center",
    },
    detailText: {
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
});
