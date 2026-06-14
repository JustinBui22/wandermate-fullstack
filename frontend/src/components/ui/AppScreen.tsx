import type { ReactNode } from "react";
import {
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    StyleProp,
    StyleSheet,
    View,
    ViewStyle,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { colors, layout, spacing } from "@/src/constants/theme";

type AppScreenProps = {
    children: ReactNode;
    scroll?: boolean;
    padded?: boolean;
    keyboardAvoiding?: boolean;
    centerContent?: boolean;
    safeAreaStyle?: StyleProp<ViewStyle>;
    contentContainerStyle?: StyleProp<ViewStyle>;
    style?: StyleProp<ViewStyle>;
    testID?: string;
};

export function AppScreen({
    children,
    scroll = true,
    padded = true,
    keyboardAvoiding = false,
    centerContent = false,
    safeAreaStyle,
    contentContainerStyle,
    style,
    testID,
}: AppScreenProps) {
    const contentStyle = [
        styles.content,
        padded && styles.padded,
        centerContent && styles.centerContent,
        contentContainerStyle,
    ];

    const body = scroll ? (
        <ScrollView
            contentContainerStyle={contentStyle}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
            style={styles.flex}
            testID={testID}
        >
            {children}
        </ScrollView>
    ) : (
        <View style={[contentStyle, styles.flex]} testID={testID}>
            {children}
        </View>
    );

    return (
        <SafeAreaView style={[styles.safeArea, safeAreaStyle]}>
            <KeyboardAvoidingView
                behavior={Platform.OS === "ios" ? "padding" : undefined}
                enabled={keyboardAvoiding}
                style={[styles.flex, style]}
            >
                {body}
            </KeyboardAvoidingView>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
        backgroundColor: colors.background,
    },
    flex: {
        flex: 1,
    },
    content: {
        width: "100%",
        maxWidth: layout.maxContentWidth,
        alignSelf: "center",
        gap: spacing.lg,
    },
    padded: {
        padding: layout.screenPadding,
    },
    centerContent: {
        flexGrow: 1,
        justifyContent: "center",
    },
});
