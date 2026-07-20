import { Ionicons } from "@expo/vector-icons";
import { Stack, useRouter } from "expo-router";
import { StyleSheet, Text, View } from "react-native";

import { AppButton } from "@/src/components/ui/AppButton";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

export default function NotFoundScreen() {
  const router = useRouter();
  const colors = useAppTheme().colors;

  return (
    <>
      <Stack.Screen options={{ title: "Page not found" }} />
      <AppScreen scroll={false} centerContent>
        <View style={styles.container}>
          <View style={[styles.iconBadge, { backgroundColor: colors.primarySoft }]}>
            <Ionicons name="map-outline" size={34} color={colors.primary} />
          </View>
          <Text style={[styles.title, { color: colors.text }]}>This route is not on the map</Text>
          <Text style={[styles.message, { color: colors.textMuted }]}>
            The page may have moved, or the link may no longer be valid.
          </Text>
          <AppButton title="Return to WanderMate" onPress={() => router.replace("/")} />
        </View>
      </AppScreen>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    width: "100%",
    maxWidth: 420,
    alignSelf: "center",
    alignItems: "center",
    gap: spacing.lg,
    padding: spacing.xl,
  },
  iconBadge: {
    width: 72,
    height: 72,
    borderRadius: radius.xl,
    alignItems: "center",
    justifyContent: "center",
  },
  title: {
    fontSize: typography.heading,
    fontWeight: fontWeight.bold,
    textAlign: "center",
  },
  message: {
    fontSize: typography.body,
    lineHeight: 23,
    textAlign: "center",
  },
});
