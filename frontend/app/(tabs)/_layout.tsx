import { type ComponentProps, useCallback, useState } from "react";
import { View } from "react-native";
import { Tabs, useFocusEffect } from "expo-router";
import { Ionicons } from "@expo/vector-icons";

import { getCollaborationSummary } from "@/src/api/tripCollaborationApi";
import { NotificationBadge } from "@/src/components/ui/NotificationBadge";
import {
    fontWeight,
    radius,
    shadows,
    spacing,
    typography,
} from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type TabIconProps = Readonly<{
    focused: boolean;
    color: ComponentProps<typeof Ionicons>["color"];
    activeIcon: keyof typeof Ionicons.glyphMap;
    inactiveIcon: keyof typeof Ionicons.glyphMap;
    badgeCount?: number;
}>;

function TabIcon({
                     focused,
                     color,
                     activeIcon,
                     inactiveIcon,
                     badgeCount = 0,
                 }: TabIconProps) {
    return (
        <View style={styles.iconContainer}>
            <Ionicons
                name={focused ? activeIcon : inactiveIcon}
                size={24}
                color={color}
            />

            <NotificationBadge count={badgeCount} />
        </View>
    );
}

export default function TabLayout() {
    const [collaborationBadgeCount, setCollaborationBadgeCount] = useState(0);

    const theme = useAppTheme();
    const themedColors = theme.colors;

    const loadCollaborationSummary = useCallback(async () => {
        try {
            const summary = await getCollaborationSummary();

            setCollaborationBadgeCount(
                Math.max(0, summary.totalPendingActionCount ?? 0)
            );
        } catch {
            setCollaborationBadgeCount(0);
        }
    }, []);

    useFocusEffect(
        useCallback(() => {
            void loadCollaborationSummary();
        }, [loadCollaborationSummary])
    );

    return (
        <Tabs
            screenOptions={{
                headerShown: false,
                tabBarActiveTintColor: themedColors.primary,
                tabBarInactiveTintColor: themedColors.textMuted,
                tabBarHideOnKeyboard: true,
                tabBarStyle: {
                    minHeight: 76,
                    paddingTop: spacing.sm,
                    paddingBottom: spacing.md,
                    paddingHorizontal: spacing.md,
                    borderTopWidth: 1,
                    borderTopColor: themedColors.border,
                    backgroundColor: themedColors.surface,
                    ...shadows.card,
                },
                tabBarItemStyle: {
                    borderRadius: radius.lg,
                    paddingVertical: spacing.xs,
                },
                tabBarLabelStyle: {
                    fontSize: typography.caption,
                    fontWeight: fontWeight.bold,
                    marginTop: spacing.xs,
                },
            }}
        >
            <Tabs.Screen
                name="index"
                options={{
                    title: "Home",
                    tabBarIcon: ({ color, focused }) => (
                        <TabIcon
                            focused={focused}
                            color={color}
                            activeIcon="home"
                            inactiveIcon="home-outline"
                        />
                    ),
                }}
            />

            <Tabs.Screen
                name="trips"
                options={{
                    title: "Trips",
                    tabBarIcon: ({ color, focused }) => (
                        <TabIcon
                            focused={focused}
                            color={color}
                            activeIcon="map"
                            inactiveIcon="map-outline"
                        />
                    ),
                }}
            />

            <Tabs.Screen
                name="collaboration"
                options={{
                    title: "Collab",
                    tabBarIcon: ({ color, focused }) => (
                        <TabIcon
                            focused={focused}
                            color={color}
                            activeIcon="people"
                            inactiveIcon="people-outline"
                            badgeCount={collaborationBadgeCount}
                        />
                    ),
                }}
            />

            <Tabs.Screen
                name="profile"
                options={{
                    title: "Profile",
                    tabBarIcon: ({ color, focused }) => (
                        <TabIcon
                            focused={focused}
                            color={color}
                            activeIcon="person-circle"
                            inactiveIcon="person-circle-outline"
                        />
                    ),
                }}
            />
        </Tabs>
    );
}

const styles = {
    iconContainer: {
        position: "relative" as const,
        alignItems: "center" as const,
        justifyContent: "center" as const,
    },
};