import { useEffect, useState } from "react";
import { ActivityIndicator, StyleSheet, Text, View } from "react-native";
import { useFonts } from "expo-font";
import {
  DarkTheme,
  DefaultTheme,
  Stack,
  ThemeProvider,
  useRouter,
  useSegments,
} from "expo-router";
import * as SplashScreen from "expo-splash-screen";
import { StatusBar } from "expo-status-bar";
import "react-native-reanimated";

import { PersistentBottomTabs } from "@/src/components/navigation/PersistentBottomTabs";
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import { useAuthStore } from "@/src/stores/authStore";

export { ErrorBoundary } from "expo-router";

export const unstable_settings = {
  initialRouteName: "(tabs)",
};

SplashScreen.preventAutoHideAsync().catch(() => null);

export default function RootLayout() {
  const [loaded, error] = useFonts({
    SpaceMono: require("../assets/fonts/SpaceMono-Regular.ttf"),
    // Load Ionicons from the app assets before any icon component mounts.
    // This avoids repeated runtime downloads from the Metro node_modules asset URL.
    ionicons: require("../assets/fonts/Ionicons.ttf"),
  });

  useEffect(() => {
    if (error) throw error;
  }, [error]);

  useEffect(() => {
    if (loaded) {
      SplashScreen.hideAsync().catch(() => null);
    }
  }, [loaded]);

  if (!loaded) {
    return null;
  }

  return <RootLayoutNav />;
}

function RootLayoutNav() {
  const theme = useAppTheme();
  const colors = theme.colors;
  const router = useRouter();
  const segments = useSegments();
  const { isAuthenticated, restoreAuthSession } = useAuthStore();
  const [isAuthReady, setIsAuthReady] = useState(false);

  const currentRouteGroup = String(segments[0] ?? "");
  const shouldShowPersistentTabs =
      isAuthenticated &&
      currentRouteGroup !== "(auth)" &&
      currentRouteGroup !== "(tabs)";

  useEffect(() => {
    let isMounted = true;

    restoreAuthSession()
        .catch(() => null)
        .finally(() => {
          if (isMounted) {
            setIsAuthReady(true);
          }
        });

    return () => {
      isMounted = false;
    };
  }, [restoreAuthSession]);

  useEffect(() => {
    if (!isAuthReady) return;

    const isInAuthGroup = currentRouteGroup === "(auth)";

    if (!isAuthenticated && !isInAuthGroup) {
      router.replace("/login");
      return;
    }

    if (isAuthenticated && isInAuthGroup) {
      router.replace("/");
    }
  }, [currentRouteGroup, isAuthenticated, isAuthReady, router]);

  if (!isAuthReady) {
    return <AuthLoadingScreen />;
  }

  return (
      <ThemeProvider value={theme.name === "DARK" ? DarkTheme : DefaultTheme}>
        <StatusBar style={theme.name === "DARK" ? "light" : "dark"} />

        <View style={styles.rootContainer}>
          <Stack
              screenOptions={{
                headerShown: false,
                contentStyle: { backgroundColor: colors.background },
                animation: "slide_from_right",
              }}
          >
            <Stack.Screen name="(auth)" />
            <Stack.Screen name="(tabs)" />
          </Stack>

          {shouldShowPersistentTabs ? <PersistentBottomTabs /> : null}
        </View>
      </ThemeProvider>
  );
}

function AuthLoadingScreen() {
  const theme = useAppTheme();
  const colors = theme.colors;

  return (
      <View style={[styles.loadingScreen, { backgroundColor: colors.background }]}>
        <View style={[styles.logoBadge, { backgroundColor: colors.primary }]}>
          <Text style={[styles.logoText, { color: colors.textLight }]}>W</Text>
        </View>

        <ActivityIndicator color={colors.primary} size="large" />

        <View style={styles.loadingTextGroup}>
          <Text style={[styles.loadingTitle, { color: colors.text }]}>WanderMate</Text>
          <Text style={[styles.loadingSubtitle, { color: colors.textMuted }]}>Preparing your session...</Text>
        </View>
      </View>
  );
}

const styles = StyleSheet.create({
  rootContainer: {
    flex: 1,
  },
  loadingScreen: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    gap: spacing.lg,
    padding: spacing.xl,
  },
  logoBadge: {
    width: 72,
    height: 72,
    borderRadius: radius.xl,
    alignItems: "center",
    justifyContent: "center",
  },
  logoText: {
    fontSize: typography.heading,
    fontWeight: fontWeight.bold,
  },
  loadingTextGroup: {
    alignItems: "center",
    gap: spacing.xs,
  },
  loadingTitle: {
    fontSize: typography.title,
    fontWeight: fontWeight.bold,
  },
  loadingSubtitle: {
    fontSize: typography.bodySmall,
    lineHeight: 20,
    textAlign: "center",
  },
});
