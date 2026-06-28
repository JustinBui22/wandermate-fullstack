import { type ReactNode } from "react";
import {
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    StyleSheet,
    View,
    type StyleProp,
    type ViewStyle,
} from "react-native";

import { spacing } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type AppScreenProps = Readonly<{
    children: ReactNode;
    scroll?: boolean;
    centerContent?: boolean;
    keyboardAvoiding?: boolean;
    contentContainerStyle?: StyleProp<ViewStyle>;
}>;

export function AppScreen({
                              children,
                              scroll = true,
                              centerContent = false,
                              keyboardAvoiding = false,
                              contentContainerStyle,
                          }: AppScreenProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    const content = scroll ? (
        <ScrollView
            style={[styles.container, { backgroundColor: colors.background }]}
            contentContainerStyle={[
                styles.content,
                centerContent && styles.centerContent,
                contentContainerStyle,
            ]}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
        >
            {children}
        </ScrollView>
    ) : (
        <View
            style={[
                styles.container,
                styles.content,
                centerContent && styles.centerContent,
                { backgroundColor: colors.background },
                contentContainerStyle,
            ]}
        >
            {children}
        </View>
    );

    if (!keyboardAvoiding) {
        return content;
    }

    return (
        <KeyboardAvoidingView
            style={[styles.container, { backgroundColor: colors.background }]}
            behavior={Platform.OS === "ios" ? "padding" : undefined}
        >
            {content}
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    content: {
        flexGrow: 1,
        paddingHorizontal: spacing.lg,
        paddingBottom: spacing.xl,
    },
    centerContent: {
        justifyContent: "center",
    },
});