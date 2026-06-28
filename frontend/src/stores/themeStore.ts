import { create } from "zustand";

import type { ThemePreference } from "@/src/theme/appTheme";

type ThemeState = {
    preferredTheme: ThemePreference;
    setPreferredTheme: (preferredTheme: ThemePreference) => void;
    resetPreferredTheme: () => void;
};

export const useThemeStore = create<ThemeState>((set) => ({
    preferredTheme: "SYSTEM",

    setPreferredTheme: (preferredTheme) => {
        set({ preferredTheme });
    },

    resetPreferredTheme: () => {
        set({ preferredTheme: "SYSTEM" });
    },
}));