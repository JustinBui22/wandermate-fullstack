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
import { SafeAreaView } from "react-native-safe-area-context";

import { spacing } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";

type AppScreenProps = Readonly<{
    children: ReactNode;
    scroll?: boolean;
    centerContent?: boolean;
    keyboardAvoiding?: boolean;
    style?: StyleProp<ViewStyle>;
    safeAreaStyle?: StyleProp<ViewStyle>;
    contentContainerStyle?: StyleProp<ViewStyle>;
}>;

export function AppScreen({
                              children,
                              scroll = true,
                              centerContent = false,
                              keyboardAvoiding = false,
                              style,
                              safeAreaStyle,
                              contentContainerStyle,
                          }: AppScreenProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    const body = scroll ? (
        <ScrollView
            style={[styles.container, { backgroundColor: colors.background }, style]}
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
                style,
                contentContainerStyle,
            ]}
        >
            {children}
        </View>
    );

    const content = keyboardAvoiding ? (
        <KeyboardAvoidingView
            style={styles.container}
            behavior={Platform.OS === "ios" ? "padding" : undefined}
        >
            {body}
        </KeyboardAvoidingView>
    ) : (
        body
    );

    return (
        <SafeAreaView
            style={[
                styles.container,
                { backgroundColor: colors.background },
                safeAreaStyle,
            ]}
        >
            {content}
        </SafeAreaView>
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