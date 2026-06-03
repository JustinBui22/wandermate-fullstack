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
import { useEffect, useState } from "react";
import { ActivityIndicator, View } from "react-native";
import "react-native-reanimated";

import { useColorScheme } from "@/components/useColorScheme";
import { useAuthStore } from "@/src/stores/authStore";
import { colors } from "@/src/theme/theme";

export { ErrorBoundary } from "expo-router";

export const unstable_settings = {
  initialRouteName: "(tabs)",
};

SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const [loaded, error] = useFonts({
    SpaceMono: require("../assets/fonts/SpaceMono-Regular.ttf"),
  });

  useEffect(() => {
    if (error) throw error;
  }, [error]);

  useEffect(() => {
    if (loaded) {
      SplashScreen.hideAsync();
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
    restoreAuthSession().finally(() => {
      setIsAuthReady(true);
    });
  }, [restoreAuthSession]);

  useEffect(() => {
    if (!isAuthReady) return;

    const currentRouteGroup = String(segments[0] ?? "");
    const isInAuthGroup = currentRouteGroup === "(auth)";

    if (!isAuthenticated && !isInAuthGroup) {
      router.replace("/login");
      return;
    }

    if (isAuthenticated && isInAuthGroup) {
      router.replace("/");
    }
  }, [isAuthenticated, isAuthReady, router, segments]);

  if (!isAuthReady) {
    return (
        <View
            style={{
              flex: 1,
              justifyContent: "center",
              alignItems: "center",
              backgroundColor: colors.background,
            }}
        >
          <ActivityIndicator color={colors.primary} />
        </View>
    );
  }

  return (
      <ThemeProvider value={colorScheme === "dark" ? DarkTheme : DefaultTheme}>
        <Stack screenOptions={{ headerShown: false }}>
          <Stack.Screen name="(auth)" />
          <Stack.Screen name="(tabs)" />
          <Stack.Screen name="modal" options={{ presentation: "modal" }} />
        </Stack>
      </ThemeProvider>
  );
}