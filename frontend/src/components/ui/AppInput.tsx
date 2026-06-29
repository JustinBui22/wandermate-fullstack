import { type ReactNode } from "react";
import {
    StyleSheet,
    Text,
    TextInput,
    View,
    type StyleProp,
    type TextInputProps,
    type TextStyle,
    type ViewStyle,
} from "react-native";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type AppInputProps = TextInputProps & Readonly<{
    label?: string;
    helperText?: string;
    errorMessage?: string;
    leftIcon?: ReactNode;
    rightIcon?: ReactNode;
    required?: boolean;
    containerStyle?: StyleProp<ViewStyle>;
    inputStyle?: StyleProp<TextStyle>;
}>;

export function AppInput({
                             label,
                             helperText,
                             errorMessage,
                             leftIcon,
                             rightIcon,
                             required = false,
                             containerStyle,
                             inputStyle,
                             style,
                             placeholderTextColor,
                             ...inputProps
                         }: AppInputProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View style={[styles.wrapper, containerStyle]}>
            {label ? (
                <Text style={[styles.label, { color: colors.text }]}>
                    {label}
                    {required ? <Text style={{ color: colors.danger }}> *</Text> : null}
                </Text>
            ) : null}

            <View
                style={[
                    styles.inputContainer,
                    {
                        backgroundColor: colors.inputBackground,
                        borderColor: errorMessage ? colors.danger : colors.border,
                    },
                    style as StyleProp<ViewStyle>,
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
                        inputStyle,
                    ]}
                    placeholderTextColor={placeholderTextColor ?? colors.placeholder}
                />

                {rightIcon ? (
                    <View style={styles.iconWrapper}>
                        {rightIcon}
                    </View>
                ) : null}
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