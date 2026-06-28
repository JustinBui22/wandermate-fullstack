import { useColorScheme } from "react-native";

import { resolveTheme } from "@/src/theme/appTheme";
import { useThemeStore } from "@/src/stores/themeStore";

export function useAppTheme() {
    const systemTheme = useColorScheme();
    const preferredTheme = useThemeStore((state) => state.preferredTheme);

    return resolveTheme(preferredTheme, systemTheme);
}