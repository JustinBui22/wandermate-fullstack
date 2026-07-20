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
import { useSegments } from "expo-router";
import { SafeAreaView } from "react-native-safe-area-context";

import { spacing } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import { useAuthStore } from "@/src/stores/authStore";

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
    const segments = useSegments();
    const { isAuthenticated } = useAuthStore();
    const currentRouteGroup = String(segments[0] ?? "");
    const shouldPadForPersistentTabs =
        isAuthenticated &&
        currentRouteGroup !== "(auth)" &&
        currentRouteGroup !== "(tabs)";

    const body = scroll ? (
        <ScrollView
            style={[styles.container, { backgroundColor: colors.background }, style]}
            contentContainerStyle={[
                styles.content,
                centerContent && styles.centerContent,
                contentContainerStyle,
                shouldPadForPersistentTabs && styles.persistentTabPadding,
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
                shouldPadForPersistentTabs && styles.persistentTabPadding,
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
    persistentTabPadding: {
        paddingBottom: 112,
    },
});
