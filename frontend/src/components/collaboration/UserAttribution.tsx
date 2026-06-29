import { useMemo, useState } from "react";
import {
    Image,
    Modal,
    Pressable,
    StyleSheet,
    Text,
    View,
    type GestureResponderEvent,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

export type AttributionUser = Readonly<{
    userId?: number | null;
    username?: string | null;
    displayName?: string | null;
    profileImageUrl?: string | null;
}>;

type SelectedUser = AttributionUser & Readonly<{
    actionLabel: string;
}>;

type UserAttributionProps = Readonly<{
    itemLabel: "activity" | "destination";
    createdBy?: AttributionUser | null;
    modifiedBy?: AttributionUser | null;
}>;

function getDisplayName(user?: AttributionUser | null) {
    return user?.displayName?.trim() || user?.username?.trim() || "Unknown user";
}

function getUsername(user?: AttributionUser | null) {
    return user?.username?.trim() || "unknown";
}

function getInitials(user?: AttributionUser | null) {
    const name = getDisplayName(user);
    const parts = name
        .split(" ")
        .map((part) => part.trim())
        .filter(Boolean);

    if (parts.length >= 2) {
        return `${parts[0].charAt(0)}${parts[1].charAt(0)}`.toUpperCase();
    }

    return name.charAt(0).toUpperCase() || "?";
}

function isSameUser(first?: AttributionUser | null, second?: AttributionUser | null) {
    if (!first || !second) {
        return false;
    }

    if (first.userId && second.userId) {
        return first.userId === second.userId;
    }

    return Boolean(first.username && second.username && first.username === second.username);
}

function hasUser(user?: AttributionUser | null) {
    return Boolean(user?.userId || user?.username || user?.displayName);
}

export function UserAttribution({
    itemLabel,
    createdBy,
    modifiedBy,
}: UserAttributionProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    const [selectedUser, setSelectedUser] = useState<SelectedUser | null>(null);

    const shouldShowModifiedBy = useMemo(() => {
        return hasUser(modifiedBy) && !isSameUser(createdBy, modifiedBy);
    }, [createdBy, modifiedBy]);

    if (!hasUser(createdBy) && !shouldShowModifiedBy) {
        return null;
    }

    function openUserCard(
        event: GestureResponderEvent,
        user: AttributionUser,
        actionLabel: string
    ) {
        event.stopPropagation?.();
        setSelectedUser({ ...user, actionLabel });
    }

    return (
        <>
            <View style={styles.container}>
                {hasUser(createdBy) ? (
                    <View
                        style={[
                            styles.pill,
                            {
                                backgroundColor: colors.surfaceSoft,
                                borderColor: colors.border,
                            },
                        ]}
                    >
                        <Text style={[styles.label, { color: colors.textMuted }]}>Created</Text>

                        <UserAvatarButton
                            user={createdBy}
                            onPress={(event) =>
                                openUserCard(
                                    event,
                                    createdBy as AttributionUser,
                                    `Created this ${itemLabel}`
                                )
                            }
                        />

                        <Text
                            style={[styles.username, { color: colors.text }]}
                            numberOfLines={1}
                        >
                            @{getUsername(createdBy)}
                        </Text>
                    </View>
                ) : null}

                {shouldShowModifiedBy ? (
                    <View
                        style={[
                            styles.pill,
                            {
                                backgroundColor: colors.surfaceSoft,
                                borderColor: colors.border,
                            },
                        ]}
                    >
                        <Text style={[styles.label, { color: colors.textMuted }]}>Edited</Text>

                        <UserAvatarButton
                            user={modifiedBy}
                            onPress={(event) =>
                                openUserCard(
                                    event,
                                    modifiedBy as AttributionUser,
                                    `Last edited this ${itemLabel}`
                                )
                            }
                        />

                        <Text
                            style={[styles.username, { color: colors.text }]}
                            numberOfLines={1}
                        >
                            @{getUsername(modifiedBy)}
                        </Text>
                    </View>
                ) : null}
            </View>

            <Modal
                visible={selectedUser !== null}
                transparent
                animationType="fade"
                onRequestClose={() => setSelectedUser(null)}
            >
                <View style={styles.modalBackdrop}>
                    <View
                        style={[
                            styles.modalCard,
                            {
                                backgroundColor: colors.surface,
                                borderColor: colors.border,
                            },
                        ]}
                    >
                        <View style={styles.modalHeader}>
                            <UserAvatar user={selectedUser} size={56} />

                            <View style={styles.modalTextGroup}>
                                <Text style={[styles.modalTitle, { color: colors.text }]}>
                                    {getDisplayName(selectedUser)}
                                </Text>

                                <Text style={[styles.modalSubtitle, { color: colors.textMuted }]}>
                                    @{getUsername(selectedUser)}
                                </Text>
                            </View>

                            <Pressable
                                accessibilityRole="button"
                                accessibilityLabel="Close user card"
                                onPress={() => setSelectedUser(null)}
                                style={({ pressed }) => [
                                    styles.closeButton,
                                    { backgroundColor: colors.surfaceSoft },
                                    pressed && styles.pressed,
                                ]}
                            >
                                <Ionicons name="close" size={20} color={colors.textMuted} />
                            </Pressable>
                        </View>

                        <View
                            style={[
                                styles.actionBox,
                                {
                                    backgroundColor: colors.primarySoft,
                                    borderColor: colors.border,
                                },
                            ]}
                        >
                            <Ionicons
                                name="information-circle-outline"
                                size={20}
                                color={colors.primary}
                            />

                            <Text style={[styles.actionText, { color: colors.text }]}>{selectedUser?.actionLabel}</Text>
                        </View>
                    </View>
                </View>
            </Modal>
        </>
    );
}

type UserAvatarButtonProps = Readonly<{
    user?: AttributionUser | null;
    onPress: (event: GestureResponderEvent) => void;
}>;

function UserAvatarButton({ user, onPress }: UserAvatarButtonProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <Pressable
            accessibilityRole="button"
            accessibilityLabel={`Open user card for ${getDisplayName(user)}`}
            onPress={onPress}
            style={({ pressed }) => [
                styles.avatarButton,
                { backgroundColor: colors.primary },
                pressed && styles.pressed,
            ]}
        >
            <UserAvatar user={user} size={26} />
        </Pressable>
    );
}

type UserAvatarProps = Readonly<{
    user?: AttributionUser | null;
    size: number;
}>;

function UserAvatar({ user, size }: UserAvatarProps) {
    const theme = useAppTheme();
    const colors = theme.colors;
    const [imageFailed, setImageFailed] = useState(false);

    const imageUrl = user?.profileImageUrl?.trim();
    const shouldShowImage = Boolean(imageUrl) && !imageFailed;

    const avatarStyle = {
        width: size,
        height: size,
        borderRadius: size / 2,
    };

    if (shouldShowImage) {
        return (
            <Image
                source={{ uri: imageUrl as string }}
                style={[styles.avatarImage, avatarStyle]}
                onError={() => setImageFailed(true)}
            />
        );
    }

    return (
        <View
            style={[
                styles.avatarFallback,
                avatarStyle,
                { backgroundColor: colors.primary },
            ]}
        >
            <Text
                style={[
                    styles.avatarInitials,
                    {
                        color: colors.textLight,
                        fontSize: size >= 50 ? typography.body : typography.caption,
                    },
                ]}
            >
                {getInitials(user)}
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: spacing.sm,
        marginTop: spacing.xs,
    },
    pill: {
        flexDirection: "row",
        alignItems: "center",
        alignSelf: "flex-start",
        gap: spacing.xs,
        borderRadius: radius.pill,
        borderWidth: 1,
        paddingVertical: spacing.xs,
        paddingHorizontal: spacing.sm,
        maxWidth: "100%",
    },
    label: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.3,
    },
    username: {
        flexShrink: 1,
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
    },
    avatarButton: {
        borderRadius: radius.pill,
    },
    avatarImage: {
        resizeMode: "cover",
    },
    avatarFallback: {
        alignItems: "center",
        justifyContent: "center",
    },
    avatarInitials: {
        fontWeight: fontWeight.bold,
    },
    pressed: {
        opacity: 0.86,
        transform: [{ scale: 0.98 }],
    },
    modalBackdrop: {
        flex: 1,
        backgroundColor: "rgba(15, 23, 42, 0.42)",
        alignItems: "center",
        justifyContent: "center",
        padding: spacing.lg,
    },
    modalCard: {
        width: "100%",
        borderRadius: radius.xl,
        borderWidth: 1,
        padding: spacing.lg,
        gap: spacing.lg,
    },
    modalHeader: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    modalTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    modalTitle: {
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    modalSubtitle: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.semibold,
    },
    closeButton: {
        width: 38,
        height: 38,
        borderRadius: radius.pill,
        alignItems: "center",
        justifyContent: "center",
    },
    actionBox: {
        borderRadius: radius.lg,
        borderWidth: 1,
        padding: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
    },
    actionText: {
        flex: 1,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.semibold,
        lineHeight: 20,
    },
});
