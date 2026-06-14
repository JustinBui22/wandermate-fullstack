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

import { useColorScheme } from "@/components/useColorScheme";
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAuthStore } from "@/src/stores/authStore";

export { ErrorBoundary } from "expo-router";

export const unstable_settings = {
  initialRouteName: "(tabs)",
};

SplashScreen.preventAutoHideAsync().catch(() => null);

export default function RootLayout() {
  const [loaded, error] = useFonts({
    SpaceMono: require("../assets/fonts/SpaceMono-Regular.ttf"),
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
  const colorScheme = useColorScheme();
  const router = useRouter();
  const segments = useSegments();
  const { isAuthenticated, restoreAuthSession } = useAuthStore();
  const [isAuthReady, setIsAuthReady] = useState(false);

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

    const currentRouteGroup = String(segments[0] ?? "");
    const isInAuthGroup = currentRouteGroup === "(auth)";

    if (!isAuthenticated && !isInAuthGroup) {
      router.replace("/login" as any);
      return;
    }

    if (isAuthenticated && isInAuthGroup) {
      router.replace("/" as any);
    }
  }, [isAuthenticated, isAuthReady, router, segments]);

  if (!isAuthReady) {
    return <AuthLoadingScreen />;
  }

  return (
      <ThemeProvider value={colorScheme === "dark" ? DarkTheme : DefaultTheme}>
        <StatusBar style="dark" />

        <Stack
            screenOptions={{
              headerShown: false,
              contentStyle: { backgroundColor: colors.background },
              animation: "slide_from_right",
            }}
        >
          <Stack.Screen name="(auth)" />
          <Stack.Screen name="(tabs)" />
          <Stack.Screen name="trips" />
          <Stack.Screen name="modal" options={{ presentation: "modal" }} />
        </Stack>
      </ThemeProvider>
  );
}

function AuthLoadingScreen() {
  return (
      <View style={styles.loadingScreen}>
        <View style={styles.logoBadge}>
          <Text style={styles.logoText}>W</Text>
        </View>

        <ActivityIndicator color={colors.primary} size="large" />

        <View style={styles.loadingTextGroup}>
          <Text style={styles.loadingTitle}>WanderMate</Text>
          <Text style={styles.loadingSubtitle}>Preparing your session...</Text>
        </View>
      </View>
  );
}

const styles = StyleSheet.create({
  loadingScreen: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    gap: spacing.lg,
    backgroundColor: colors.background,
    padding: spacing.xl,
  },
  logoBadge: {
    width: 72,
    height: 72,
    borderRadius: radius.xl,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.primary,
  },
  logoText: {
    color: colors.textLight,
    fontSize: typography.heading,
    fontWeight: fontWeight.bold,
  },
  loadingTextGroup: {
    alignItems: "center",
    gap: spacing.xs,
  },
  loadingTitle: {
    color: colors.text,
    fontSize: typography.title,
    fontWeight: fontWeight.bold,
  },
  loadingSubtitle: {
    color: colors.textMuted,
    fontSize: typography.bodySmall,
    lineHeight: 20,
    textAlign: "center",
  },
});