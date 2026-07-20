import { type ComponentProps, useCallback, useEffect, useMemo, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { usePathname, useRouter } from "expo-router";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import { getCollaborationSummary } from "@/src/api/tripCollaborationApi";
import { NotificationBadge } from "@/src/components/ui/NotificationBadge";
import { fontWeight, radius, shadow, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type TabHref = "/" | "/trips" | "/collaboration" | "/profile";

type TabItem = Readonly<{
    label: string;
    href: TabHref;
    activeIcon: keyof typeof Ionicons.glyphMap;
    inactiveIcon: keyof typeof Ionicons.glyphMap;
    isActive: (pathname: string) => boolean;
}>;

type IconColor = ComponentProps<typeof Ionicons>["color"];

const TABS: TabItem[] = [
    {
        label: "Home",
        href: "/",
        activeIcon: "home",
        inactiveIcon: "home-outline",
        isActive: (pathname) => pathname === "/" || pathname === "/index",
    },
    {
        label: "Trips",
        href: "/trips",
        activeIcon: "map",
        inactiveIcon: "map-outline",
        isActive: (pathname) => pathname === "/trips" || pathname.startsWith("/trips/"),
    },
    {
        label: "Collab",
        href: "/collaboration",
        activeIcon: "people",
        inactiveIcon: "people-outline",
        isActive: (pathname) => pathname === "/collaboration" || pathname.includes("/collaboration"),
    },
    {
        label: "Profile",
        href: "/profile",
        activeIcon: "person-circle",
        inactiveIcon: "person-circle-outline",
        isActive: (pathname) => pathname === "/profile",
    },
];

export function PersistentBottomTabs() {
    const router = useRouter();
    const pathname = usePathname();
    const safeAreaInsets = useSafeAreaInsets();
    const theme = useAppTheme();
    const colors = theme.colors;
    const [collaborationBadgeCount, setCollaborationBadgeCount] = useState(0);

    const loadCollaborationSummary = useCallback(async () => {
        try {
            const summary = await getCollaborationSummary();
            setCollaborationBadgeCount(Math.max(0, summary.totalPendingActionCount ?? 0));
        } catch {
            setCollaborationBadgeCount(0);
        }
    }, []);

    useEffect(() => {
        void loadCollaborationSummary();
    }, [loadCollaborationSummary, pathname]);

    const barShadow = useMemo(
        () => (theme.name === "LIGHT" ? shadow.card : styles.noShadow),
        [theme.name]
    );

    function handleTabPress(href: TabHref) {
        // Replace instead of push so the user does not need to press back through many nested screens.
        router.replace(href);
    }

    return (
        <View pointerEvents="box-none" style={styles.overlay}>
            <View
                style={[
                    styles.container,
                    {
                        paddingBottom: Math.max(safeAreaInsets.bottom, spacing.sm),
                        backgroundColor: colors.surface,
                        borderTopColor: colors.border,
                    },
                    barShadow,
                ]}
            >
                {TABS.map((tab) => {
                    const isActive = tab.isActive(pathname);
                    const color: IconColor = isActive ? colors.primary : colors.textMuted;
                    const badgeCount = tab.label === "Collab" ? collaborationBadgeCount : 0;

                    return (
                        <Pressable
                            key={tab.href}
                            accessibilityRole="button"
                            accessibilityState={{ selected: isActive }}
                            onPress={() => handleTabPress(tab.href)}
                            style={({ pressed }) => [
                                styles.tabItem,
                                isActive && { backgroundColor: colors.primarySoft },
                                pressed && styles.pressed,
                            ]}
                        >
                            <View style={styles.iconContainer}>
                                <Ionicons
                                    name={isActive ? tab.activeIcon : tab.inactiveIcon}
                                    size={24}
                                    color={color}
                                />
                                <NotificationBadge count={badgeCount} />
                            </View>

                            <Text style={[styles.label, { color }]}>{tab.label}</Text>
                        </Pressable>
                    );
                })}
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    overlay: {
        position: "absolute",
        left: 0,
        right: 0,
        bottom: 0,
        zIndex: 50,
    },
    container: {
        minHeight: 76,
        paddingTop: spacing.sm,
        paddingHorizontal: spacing.md,
        borderTopWidth: 1,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
    },
    tabItem: {
        flex: 1,
        minHeight: 54,
        borderRadius: radius.lg,
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.xs,
    },
    iconContainer: {
        position: "relative",
        alignItems: "center",
        justifyContent: "center",
    },
    label: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
    },
    pressed: {
        opacity: 0.82,
        transform: [{ scale: 0.98 }],
    },
    noShadow: {
        elevation: 0,
        shadowOpacity: 0,
        shadowRadius: 0,
        shadowOffset: { width: 0, height: 0 },
    },
});
