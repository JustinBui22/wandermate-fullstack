import { StyleSheet } from "react-native";

import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";

export const styles = StyleSheet.create({
    screenContent: {
        flex: 1,
    },
    scrollContent: {
        paddingTop: spacing.xl,
        paddingBottom: 120,
        gap: spacing.lg,
    },
    emptyScrollContent: {
        flexGrow: 1,
        justifyContent: "center",
    },
    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
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
    },
    subtitle: {
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    headerActions: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
    },
    filterIconButton: {
        width: 50,
        height: 50,
        borderRadius: radius.lg,
        borderWidth: 1,
        alignItems: "center",
        justifyContent: "center",
        position: "relative",
    },
    filterDot: {
        position: "absolute",
        top: 9,
        right: 9,
        width: 8,
        height: 8,
        borderRadius: 99,
        backgroundColor: "#EF4444",
    },
    addButton: {
        width: 50,
        height: 50,
        minHeight: 50,
        borderRadius: radius.lg,
        paddingHorizontal: 0,
    },
    filterSummary: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.xs,
        borderRadius: radius.pill,
        borderWidth: 1,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.sm,
    },
    filterSummaryText: {
        flex: 1,
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
    },
    emptyState: {
        marginTop: spacing.xl,
    },
    pressed: {
        opacity: 0.86,
        transform: [{ scale: 0.99 }],
    },
    modalBackdrop: {
        flex: 1,
        backgroundColor: "rgba(15, 23, 42, 0.36)",
        justifyContent: "center",
        padding: spacing.lg,
    },
    modalCard: {
        borderRadius: radius.xl,
        borderWidth: 1,
        padding: spacing.lg,
        gap: spacing.lg,
    },
    modalHeader: {
        flexDirection: "row",
        alignItems: "flex-start",
        justifyContent: "space-between",
        gap: spacing.md,
    },
    modalTitleGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    modalTitle: {
        fontSize: typography.title,
        fontWeight: fontWeight.bold,
    },
    modalSubtitle: {
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    modalCloseButton: {
        width: 38,
        height: 38,
        borderRadius: radius.pill,
        alignItems: "center",
        justifyContent: "center",
    },
    modalSection: {
        gap: spacing.sm,
    },
    modalSectionTitle: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    modalOption: {
        minHeight: 44,
        borderRadius: radius.lg,
        borderWidth: 1,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.sm,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.md,
    },
    modalOptionText: {
        flex: 1,
        fontSize: typography.bodySmall,
    },
    modalOptionEmptyCircle: {
        width: 20,
        height: 20,
        borderRadius: 99,
        borderWidth: 1,
    },
    modalActions: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.md,
    },
    modalRightActions: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.sm,
    },
    modalGhostButton: {
        minHeight: 42,
        justifyContent: "center",
        paddingHorizontal: spacing.md,
    },
    modalGhostButtonText: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    modalSecondaryButton: {
        minHeight: 42,
        borderRadius: radius.lg,
        borderWidth: 1,
        alignItems: "center",
        justifyContent: "center",
        paddingHorizontal: spacing.lg,
    },
    modalSecondaryButtonText: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
    },
    modalPrimaryButton: {
        minHeight: 42,
        borderRadius: radius.lg,
        alignItems: "center",
        justifyContent: "center",
        paddingHorizontal: spacing.lg,
    },
    modalPrimaryButtonText: {
        fontSize: typography.bodySmall,
        fontWeight: fontWeight.bold,
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
    sectionEmptyCardContent: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    sectionEmptyText: {
        flex: 1,
        fontSize: typography.bodySmall,
        lineHeight: 20,
    },
    tripList: {
        gap: spacing.md,
    },
    tripCard: {
        borderRadius: radius.xl,
    },
    tripCardContent: {
        gap: spacing.md,
    },
    tripCoverImage: {
        width: "100%",
        height: 132,
        borderRadius: radius.lg,
        resizeMode: "cover",
        marginBottom: spacing.xs,
    },
    tripMainRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.md,
    },
    tripIconBadge: {
        width: 44,
        height: 44,
        borderRadius: radius.lg,
        alignItems: "center",
        justifyContent: "center",
    },
    tripTextGroup: {
        flex: 1,
        gap: spacing.xs,
    },
    tripTitle: {
        fontSize: typography.body,
        fontWeight: fontWeight.bold,
    },
    tripDestination: {
        fontSize: typography.bodySmall,
        lineHeight: 19,
    },
    tripRightGroup: {
        alignItems: "flex-end",
        justifyContent: "center",
        gap: spacing.xs,
    },
    statusBadge: {
        borderRadius: radius.pill,
        borderWidth: 1,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.xs,
    },
    statusBadgeText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
    },
    roleBadge: {
        borderRadius: radius.pill,
        borderWidth: 1,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.xs,
    },
    roleBadgeText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.bold,
    },
    tripMetaRow: {
        flexDirection: "row",
        alignItems: "center",
        flexWrap: "wrap",
        gap: spacing.sm,
    },
    metaPill: {
        flexShrink: 1,
        flexDirection: "row",
        alignItems: "center",
        gap: spacing.xs,
        borderRadius: radius.pill,
        borderWidth: 1,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.sm,
    },
    metaText: {
        fontSize: typography.caption,
        fontWeight: fontWeight.semibold,
    },
});
