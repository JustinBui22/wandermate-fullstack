import {Tabs} from "expo-router";
import {Ionicons} from "@expo/vector-icons";
import {colors} from "@/src/theme/theme";

export default function TabLayout() {
    return (
        <Tabs
            screenOptions={{
                headerShown: false,
                tabBarActiveTintColor: colors.primary,
                tabBarInactiveTintColor: "#9CA3AF",
                tabBarStyle: {
                    height: 72,
                    paddingTop: 8,
                    paddingBottom: 12,
                    borderTopWidth: 0,
                    backgroundColor: "#FFFFFF",
                    elevation: 12,
                    shadowColor: "#0F172A",
                    shadowOpacity: 0.08,
                    shadowRadius: 16,
                    shadowOffset: {width: 0, height: -4},
                },
                tabBarLabelStyle: {
                    fontSize: 12,
                    fontWeight: "700",
                },
            }}
        >
            <Tabs.Screen
                name="index"
                options={{
                    title: "Home",
                    tabBarIcon: ({color, focused}) => (
                        <Ionicons
                            name={focused ? "home" : "home-outline"}
                            size={24}
                            color={color}
                        />
                    ),
                }}
            />

            <Tabs.Screen
                name="trips"
                options={{
                    title: "Trips",
                    tabBarIcon: ({color, focused}) => (
                        <Ionicons
                            name={focused ? "map" : "map-outline"}
                            size={24}
                            color={color}
                        />
                    ),
                }}
            />
        </Tabs>
    );
}