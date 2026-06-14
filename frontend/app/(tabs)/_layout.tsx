import { type ComponentProps } from "react";
import { Tabs } from "expo-router";
import { Ionicons } from "@expo/vector-icons";

import { colors, fontWeight, radius, shadows, spacing, typography } from "@/src/constants/theme";

type TabIconProps = Readonly<{
    focused: boolean;
    color: ComponentProps<typeof Ionicons>["color"];
    activeIcon: keyof typeof Ionicons.glyphMap;
    inactiveIcon: keyof typeof Ionicons.glyphMap;
}>;

function TabIcon({ focused, color, activeIcon, inactiveIcon }: TabIconProps) {
    return (
        <Ionicons
            name={focused ? activeIcon : inactiveIcon}
            size={24}
            color={color}
        />
    );
}

export default function TabLayout() {
    return (
        <Tabs
            screenOptions={{
                headerShown: false,
                tabBarActiveTintColor: colors.primary,
                tabBarInactiveTintColor: colors.textMuted,
                tabBarHideOnKeyboard: true,
                tabBarStyle: {
                    minHeight: 76,
                    paddingTop: spacing.sm,
                    paddingBottom: spacing.md,
                    paddingHorizontal: spacing.md,
                    borderTopWidth: 1,
                    borderTopColor: colors.border,
                    backgroundColor: colors.surface,
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
        </Tabs>
    );
}