import { Platform } from "react-native";

export const colors = {
    background: "#F6F8FB",
    surface: "#FFFFFF",
    card: "#FFFFFF",
    surfaceSoft: "#F9FAFB",
    softGray: "#F3F4F6",

    primary: "#2563EB",
    primaryDark: "#1E40AF",
    primarySoft: "#EAF2FF",
    softBlue: "#EAF2FF",

    text: "#111827",
    textMuted: "#6B7280",
    mutedText: "#6B7280",
    textLight: "#FFFFFF",

    border: "#E5E7EB",
    borderStrong: "#D1D5DB",

    danger: "#DC2626",
    error: "#DC2626",
    dangerSoft: "#FEE2E2",

    success: "#059669",
    successSoft: "#D1FAE5",

    warning: "#D97706",
    warningSoft: "#FEF3C7",

    disabled: "#9CA3AF",
    overlay: "rgba(17, 24, 39, 0.35)",

    inputBackground: "#FFFFFF",
    placeholder: "#9CA3AF",
} as const;

export const spacing = {
    none: 0,
    xxs: 2,
    xs: 4,
    sm: 8,
    md: 12,
    lg: 16,
    xl: 24,
    xxl: 32,
    xxxl: 44,
} as const;

export const radius = {
    sm: 8,
    md: 12,
    lg: 16,
    xl: 22,
    xxl: 28,
    pill: 999,
    full: 999,
} as const;

export const typography = {
    caption: 12,
    bodySmall: 14,
    body: 16,
    subheading: 18,
    title: 20,
    heading: 26,
    hero: 32,
} as const;

export const fontWeight = {
    regular: "400",
    medium: "500",
    semibold: "600",
    bold: "700",
    extraBold: "800",
} as const;

export const layout = {
    screenPadding: spacing.lg,
    maxContentWidth: 720,
    inputHeight: 52,
    buttonHeight: 52,
} as const;

export const shadows = {
    card: Platform.select({
        ios: {
            shadowColor: "#0F172A",
            shadowOpacity: 0.08,
            shadowRadius: 18,
            shadowOffset: { width: 0, height: 10 },
        },
        android: {
            elevation: 4,
        },
        default: {},
    }),
    button: Platform.select({
        ios: {
            shadowColor: "#1E40AF",
            shadowOpacity: 0.18,
            shadowRadius: 12,
            shadowOffset: { width: 0, height: 6 },
        },
        android: {
            elevation: 3,
        },
        default: {},
    }),
} as const;

// Backward-compatible alias.
// Some files import `shadow`, others import `shadows`.
export const shadow = shadows;

export const theme = {
    colors,
    spacing,
    radius,
    typography,
    fontWeight,
    layout,
    shadows,
    shadow,
} as const;

export type AppTheme = typeof theme;
export type ColorKey = keyof typeof colors;