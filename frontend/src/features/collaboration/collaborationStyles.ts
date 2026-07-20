import { StyleSheet } from "react-native";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";

export const styles = StyleSheet.create({
    screenContent: {
        flex: 1,
    },
    scrollContent: {
        paddingTop: spacing.xl,
        paddingBottom: spacing.xxl,
        gap: spacing.lg,
    },
    header: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.md,
    },
    headerTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    eyebrow: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
        textTransform: "uppercase",
        letterSpacing: 0.7,
    },
    title: {
        fontSize: typography.hero,
        fontWeight: fontWeight.bold,
        lineHeight: 38,
    },
    subtitle: {
        fontSize: typography.bodySmall,
        lineHeight: 21,
    },
    joinCardContent: {
        gap: spacing.lg,
    },
    infoBox: {
        flexDirection: "row",
        alignItems: "flex-start",
        gap: spacing.md,
        borderRadius: radius.lg,
        padding: spacing.md,
    },
    infoTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    infoTitle: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    infoText: {
        fontSize: typography.caption,
        lineHeight: 18,
        fontWeight: fontWeight.semibold,
    },
    section: {
        gap: spacing.md,
    },
    sectionHeader: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.md,
    },
    sectionTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    sectionTitle: {
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    sectionSubtitle: {
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    countBadge: {
        minWidth: 34,
        height: 34,
        borderRadius: radius.pill,
        alignItems: "center",
        justifyContent: "center",
        paddingHorizontal: spacing.sm,
    },
    countBadgeText: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    requestList: {
        gap: spacing.md,
    },
    requestCardContent: {
        gap: spacing.lg,
    },
    requestTopRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    tripIconBadge: {
        width: 46,
        height: 46,
        borderRadius: radius.lg,
        alignItems: "center",
        justifyContent: "center",
    },
    requestTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    requestTitle: {
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    requestSubtitle: {
        fontSize: typography.caption,
        lineHeight: 18,
    },
    metaBox: {
        borderRadius: radius.md,
        padding: spacing.md,
        gap: spacing.xs,
    },
    metaText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
        lineHeight: 18,
    },
    actionRow: {
        flexDirection: "row",
        gap: spacing.md,
    },
    actionButton: {
        flex: 1,
    },
    openHintRow: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.sm,
    },
    openHintText: {
        flex: 1,
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
        lineHeight: 18,
    },
});
