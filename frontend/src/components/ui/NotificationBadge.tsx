import { StyleSheet, Text, View } from "react-native";

type NotificationBadgeProps = {
    count?: number;
    maxCount?: number;
    size?: "small" | "medium";
};

export function NotificationBadge({
                                      count = 0,
                                      maxCount = 99,
                                      size = "medium",
                                  }: NotificationBadgeProps) {
    if (count <= 0) {
        return null;
    }

    const label = count > maxCount ? `${maxCount}+` : String(count);

    return (
        <View
            pointerEvents="none"
            style={[
                styles.badge,
                size === "small" ? styles.smallBadge : styles.mediumBadge,
            ]}
        >
            <Text
                style={[
                    styles.text,
                    size === "small" ? styles.smallText : styles.mediumText,
                ]}
            >
                {label}
            </Text>
        </View>
    );
}

const styles = StyleSheet.create({
    badge: {
        position: "absolute",
        top: -7,
        right: -10,
        minWidth: 18,
        height: 18,
        borderRadius: 999,
        backgroundColor: "#EF4444",
        alignItems: "center",
        justifyContent: "center",
        paddingHorizontal: 5,
        borderWidth: 2,
        borderColor: "#FFFFFF",
        zIndex: 20,
    },
    smallBadge: {
        minWidth: 16,
        height: 16,
        top: -6,
        right: -8,
        paddingHorizontal: 4,
    },
    mediumBadge: {
        minWidth: 18,
        height: 18,
    },
    text: {
        color: "#FFFFFF",
        fontWeight: "800",
        includeFontPadding: false,
        textAlign: "center",
    },
    smallText: {
        fontSize: 9,
        lineHeight: 12,
    },
    mediumText: {
        fontSize: 10,
        lineHeight: 13,
    },
});