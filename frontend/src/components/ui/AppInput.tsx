import { type ReactNode } from "react";
import {
    StyleSheet,
    Text,
    TextInput,
    View,
    type TextInputProps,
} from "react-native";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type AppInputProps = TextInputProps & Readonly<{
    label?: string;
    helperText?: string;
    errorMessage?: string;
    leftIcon?: ReactNode;
}>;

export function AppInput({
                             label,
                             helperText,
                             errorMessage,
                             leftIcon,
                             style,
                             placeholderTextColor,
                             ...inputProps
                         }: AppInputProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View style={styles.wrapper}>
            {label ? (
                <Text style={[styles.label, { color: colors.text }]}>
                    {label}
                </Text>
            ) : null}

            <View
                style={[
                    styles.inputContainer,
                    {
                        backgroundColor: colors.inputBackground,
                        borderColor: errorMessage ? colors.danger : colors.border,
                    },
                ]}
            >
                {leftIcon ? (
                    <View style={styles.iconWrapper}>
                        {leftIcon}
                    </View>
                ) : null}

                <TextInput
                    {...inputProps}
                    style={[
                        styles.input,
                        { color: colors.text },
                        style,
                    ]}
                    placeholderTextColor={placeholderTextColor ?? colors.placeholder}
                />
            </View>

            {errorMessage ? (
                <Text style={[styles.errorText, { color: colors.danger }]}>
                    {errorMessage}
                </Text>
            ) : helperText ? (
                <Text style={[styles.helperText, { color: colors.textMuted }]}>
                    {helperText}
                </Text>
            ) : null}
        </View>
    );
}

const styles = StyleSheet.create({
    wrapper: {
        gap: spacing.xs,
    },
    label: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.semibold,
    },
    inputContainer: {
        minHeight: 48,
        borderWidth: 1,
        borderRadius: radius.lg,
        paddingHorizontal: spacing.md,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
    },
    iconWrapper: {
        alignItems: "center",
        justifyContent: "center",
    },
    input: {
        flex: 1,
        fontSize: typography.body,
        paddingVertical: spacing.sm,
    },
    helperText: {
        fontSize: typography.caption,
        lineHeight: 18,
    },
    errorText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
        lineHeight: 18,
    },
});