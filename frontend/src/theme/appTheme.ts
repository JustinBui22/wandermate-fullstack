export type ThemePreference = "LIGHT" | "DARK" | "SYSTEM";

export type AppThemeName = "LIGHT" | "DARK";

export type AppThemeColors = {
    primary: string;
    primaryDark: string;
    primarySoft: string;

    background: string;
    surface: string;
    surfaceSoft: string;
    card: string;

    text: string;
    textLight: string;
    textMuted: string;

    border: string;
    borderStrong: string;

    success: string;
    warning: string;
    danger: string;
    dangerSoft: string;

    inputBackground: string;
    placeholder: string;
};

export type AppTheme = {
    name: AppThemeName;
    colors: AppThemeColors;
};

export const lightTheme: AppTheme = {
    name: "LIGHT",
    colors: {
        primary: "#2563EB",
        primaryDark: "#1D4ED8",
        primarySoft: "#DBEAFE",

        background: "#F8FAFC",
        surface: "#FFFFFF",
        surfaceSoft: "#F1F5F9",
        card: "#FFFFFF",

        text: "#0F172A",
        textLight: "#FFFFFF",
        textMuted: "#64748B",

        border: "#E2E8F0",
        borderStrong: "#CBD5E1",

        success: "#16A34A",
        warning: "#F59E0B",
        danger: "#DC2626",
        dangerSoft: "#FEE2E2",

        inputBackground: "#FFFFFF",
        placeholder: "#94A3B8",
    },
};

export const darkTheme: AppTheme = {
    name: "DARK",
    colors: {
        primary: "#60A5FA",
        primaryDark: "#3B82F6",
        primarySoft: "#1E3A8A",

        background: "#020617",
        surface: "#0F172A",
        surfaceSoft: "#1E293B",
        card: "#111827",

        text: "#F8FAFC",
        textLight: "#FFFFFF",
        textMuted: "#94A3B8",

        border: "#334155",
        borderStrong: "#475569",

        success: "#22C55E",
        warning: "#FBBF24",
        danger: "#F87171",
        dangerSoft: "#7F1D1D",

        inputBackground: "#0F172A",
        placeholder: "#64748B",
    },
};

export function resolveTheme(
    preferredTheme: ThemePreference,
    systemTheme: "light" | "dark" | "unspecified" | null | undefined
): AppTheme {
    if (preferredTheme === "DARK") {
        return darkTheme;
    }

    if (preferredTheme === "LIGHT") {
        return lightTheme;
    }

    return systemTheme === "dark" ? darkTheme : lightTheme;
}