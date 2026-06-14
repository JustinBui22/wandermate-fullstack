import type { ReactNode } from "react";
import {
    StyleProp,
    StyleSheet,
    Text,
    TextInput,
    TextInputProps,
    TextStyle,
    View,
    ViewStyle,
} from "react-native";

import { colors, fontWeight, layout, radius, spacing, typography } from "@/src/constants/theme";
import { ErrorMessage } from "./ErrorMessage";

type AppInputProps = TextInputProps & {
    label?: string;
    helperText?: string;
    error?: string | null;
    required?: boolean;
    leftIcon?: ReactNode;
    rightIcon?: ReactNode;
    containerStyle?: StyleProp<ViewStyle>;
    inputContainerStyle?: StyleProp<ViewStyle>;
    inputStyle?: StyleProp<TextStyle>;
};

export function AppInput({
    label,
    helperText,
    error,
    required = false,
    leftIcon,
    rightIcon,
    containerStyle,
    inputContainerStyle,
    inputStyle,
    editable = true,
    multiline = false,
    placeholderTextColor = colors.textMuted,
    style,
    ...textInputProps
}: AppInputProps) {
    return (
        <View style={[styles.container, containerStyle]}>
            {label ? (
                <Text style={styles.label}>
                    {label}
                    {required ? <Text style={styles.required}> *</Text> : null}
                </Text>
            ) : null}

            <View
                style={[
                    styles.inputContainer,
                    multiline && styles.multilineContainer,
                    !editable && styles.disabledContainer,
                    error && styles.errorBorder,
                    inputContainerStyle,
                ]}
            >
                {leftIcon ? <View style={styles.icon}>{leftIcon}</View> : null}
                <TextInput
                    {...textInputProps}
                    editable={editable}
                    multiline={multiline}
                    placeholderTextColor={placeholderTextColor}
                    style={[
                        styles.input,
                        multiline && styles.multilineInput,
                        !editable && styles.disabledInput,
                        inputStyle,
                        style,
                    ]}
                />
                {rightIcon ? <View style={styles.icon}>{rightIcon}</View> : null}
            </View>

            {error ? (
                <ErrorMessage message={error} compact />
            ) : helperText ? (
                <Text style={styles.helperText}>{helperText}</Text>
            ) : null}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        gap: spacing.sm,
    },
    label: {
        color: colors.text,
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.semibold,
    },
    required: {
        color: colors.danger,
    },
    inputContainer: {
        minHeight: layout.inputHeight,
        borderWidth: 1,
        borderColor: colors.border,
        borderRadius: radius.md,
        backgroundColor: colors.surface,
        flexDirection: "row",
        alignItems: "center",
        paddingHorizontal: spacing.md,
        gap: spacing.sm,
    },
    multilineContainer: {
        minHeight: 112,
        alignItems: "flex-start",
        paddingVertical: spacing.sm,
    },
    errorBorder: {
        borderColor: colors.danger,
    },
    disabledContainer: {
        backgroundColor: colors.surfaceSoft,
    },
    input: {
        flex: 1,
        color: colors.text,
        fontSize: typography.body,
        paddingVertical: spacing.sm,
    },
    multilineInput: {
        minHeight: 92,
        textAlignVertical: "top",
    },
    disabledInput: {
        color: colors.textMuted,
    },
    icon: {
        minWidth: 24,
        alignItems: "center",
        justifyContent: "center",
    },
    helperText: {
        color: colors.textMuted,
        fontSize: typography.caption,
        lineHeight: 18,
    },
});
