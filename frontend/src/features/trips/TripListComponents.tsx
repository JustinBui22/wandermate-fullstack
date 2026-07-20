import { useState } from "react";
import { Image, Modal, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";

import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { fontWeight } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { Trip, TripSortOption, TripStatus } from "@/src/types/trip";
import { normalizeImageUrl } from "@/src/utils/imageUrlUtils";
import { styles } from "@/src/features/trips/tripListStyles";
import {
    formatDateRange,
    formatDate,
    getTripStatus,
    SORT_OPTIONS,
    STATUS_FILTERS,
    type TripStatusFilter,
} from "@/src/features/trips/tripListUtils";

type FilterModalProps = Readonly<{
    visible: boolean;
    sortOption: TripSortOption;
    statusFilter: TripStatusFilter;
    onChangeSortOption: (value: TripSortOption) => void;
    onChangeStatusFilter: (value: TripStatusFilter) => void;
    onReset: () => void;
    onCancel: () => void;
    onApply: () => void;
}>;

export function FilterModal({
                         visible,
                         sortOption,
                         statusFilter,
                         onChangeSortOption,
                         onChangeStatusFilter,
                         onReset,
                         onCancel,
                         onApply,
                     }: FilterModalProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <Modal
            visible={visible}
            transparent
            animationType="fade"
            onRequestClose={onCancel}
        >
            <View style={styles.modalBackdrop}>
                <View
                    style={[
                        styles.modalCard,
                        {
                            backgroundColor: colors.background,
                            borderColor: colors.border,
                        },
                    ]}
                >
                    <View style={styles.modalHeader}>
                        <View style={styles.modalTitleGroup}>
                            <Text style={[styles.modalTitle, { color: colors.text }]}>
                                Filter trips
                            </Text>

                            <Text
                                style={[
                                    styles.modalSubtitle,
                                    { color: colors.textMuted },
                                ]}
                            >
                                Choose your options, then press OK to apply.
                            </Text>
                        </View>

                        <Pressable
                            accessibilityRole="button"
                            onPress={onCancel}
                            style={({ pressed }) => [
                                styles.modalCloseButton,
                                { backgroundColor: colors.surfaceSoft },
                                pressed && styles.pressed,
                            ]}
                        >
                            <Ionicons
                                name="close"
                                size={22}
                                color={colors.textMuted}
                            />
                        </Pressable>
                    </View>

                    <View style={styles.modalSection}>
                        <Text
                            style={[
                                styles.modalSectionTitle,
                                { color: colors.text },
                            ]}
                        >
                            Status
                        </Text>

                        {STATUS_FILTERS.map((option) => (
                            <ModalOption
                                key={option.value}
                                label={option.label}
                                selected={statusFilter === option.value}
                                onPress={() => onChangeStatusFilter(option.value)}
                            />
                        ))}
                    </View>

                    <View style={styles.modalSection}>
                        <Text
                            style={[
                                styles.modalSectionTitle,
                                { color: colors.text },
                            ]}
                        >
                            Sort by
                        </Text>

                        {SORT_OPTIONS.map((option) => (
                            <ModalOption
                                key={option.value}
                                label={option.label}
                                selected={sortOption === option.value}
                                onPress={() => onChangeSortOption(option.value)}
                            />
                        ))}
                    </View>

                    <View style={styles.modalActions}>
                        <Pressable
                            accessibilityRole="button"
                            onPress={onReset}
                            style={({ pressed }) => [
                                styles.modalGhostButton,
                                pressed && styles.pressed,
                            ]}
                        >
                            <Text
                                style={[
                                    styles.modalGhostButtonText,
                                    { color: colors.textMuted },
                                ]}
                            >
                                Reset
                            </Text>
                        </Pressable>

                        <View style={styles.modalRightActions}>
                            <Pressable
                                accessibilityRole="button"
                                onPress={onCancel}
                                style={({ pressed }) => [
                                    styles.modalSecondaryButton,
                                    {
                                        borderColor: colors.border,
                                        backgroundColor: colors.surface,
                                    },
                                    pressed && styles.pressed,
                                ]}
                            >
                                <Text
                                    style={[
                                        styles.modalSecondaryButtonText,
                                        { color: colors.text },
                                    ]}
                                >
                                    Cancel
                                </Text>
                            </Pressable>

                            <Pressable
                                accessibilityRole="button"
                                onPress={onApply}
                                style={({ pressed }) => [
                                    styles.modalPrimaryButton,
                                    { backgroundColor: colors.primary },
                                    pressed && styles.pressed,
                                ]}
                            >
                                <Text
                                    style={[
                                        styles.modalPrimaryButtonText,
                                        { color: colors.textLight },
                                    ]}
                                >
                                    OK
                                </Text>
                            </Pressable>
                        </View>
                    </View>
                </View>
            </View>
        </Modal>
    );
}

type ModalOptionProps = Readonly<{
    label: string;
    selected: boolean;
    onPress: () => void;
}>;

function ModalOption({ label, selected, onPress }: ModalOptionProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <Pressable
            accessibilityRole="button"
            onPress={onPress}
            style={({ pressed }) => [
                styles.modalOption,
                {
                    borderColor: selected ? colors.primary : colors.border,
                    backgroundColor: selected ? colors.primarySoft : colors.surface,
                },
                pressed && styles.pressed,
            ]}
        >
            <Text
                style={[
                    styles.modalOptionText,
                    {
                        color: selected ? colors.text : colors.textMuted,
                        fontWeight: selected ? fontWeight.bold : fontWeight.semibold,
                    },
                ]}
            >
                {label}
            </Text>

            {selected ? (
                <Ionicons
                    name="checkmark-circle"
                    size={20}
                    color={colors.primary}
                />
            ) : (
                <View
                    style={[
                        styles.modalOptionEmptyCircle,
                        { borderColor: colors.border },
                    ]}
                />
            )}
        </Pressable>
    );
}

type TripSectionProps = Readonly<{
    title: string;
    subtitle: string;
    trips: Trip[];
    emptyMessage: string;
    onOpenTrip: (trip: Trip) => void;
}>;

export function TripSection({
                         title,
                         subtitle,
                         trips,
                         emptyMessage,
                         onOpenTrip,
                     }: TripSectionProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    return (
        <View style={styles.section}>
            <View style={styles.sectionHeader}>
                <View style={styles.sectionTextGroup}>
                    <Text style={[styles.sectionTitle, { color: colors.text }]}>
                        {title}
                    </Text>

                    <Text
                        style={[
                            styles.sectionSubtitle,
                            { color: colors.textMuted },
                        ]}
                    >
                        {subtitle}
                    </Text>
                </View>

                <View
                    style={[
                        styles.countBadge,
                        { backgroundColor: colors.primarySoft },
                    ]}
                >
                    <Text
                        style={[
                            styles.countBadgeText,
                            { color: colors.primary },
                        ]}
                    >
                        {trips.length}
                    </Text>
                </View>
            </View>

            {trips.length === 0 ? (
                <AppCard contentStyle={styles.sectionEmptyCardContent}>
                    <Ionicons
                        name="folder-open-outline"
                        size={22}
                        color={colors.textMuted}
                    />

                    <Text
                        style={[
                            styles.sectionEmptyText,
                            { color: colors.textMuted },
                        ]}
                    >
                        {emptyMessage}
                    </Text>
                </AppCard>
            ) : (
                <View style={styles.tripList}>
                    {trips.map((trip, index) => (
                        <TripCard
                            key={String(trip.tripId ?? `${trip.tripName}-${index}`)}
                            trip={trip}
                            onPress={() => onOpenTrip(trip)}
                        />
                    ))}
                </View>
            )}
        </View>
    );
}

type TripCardProps = Readonly<{
    trip: Trip;
    onPress: () => void;
}>;

function TripCard({ trip, onPress }: TripCardProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    const status = getTripStatus(trip);
    const [coverImageFailed, setCoverImageFailed] = useState(false);
    const coverImageUrl = normalizeImageUrl(trip.coverImageUrl);
    const shouldShowCoverImage = Boolean(coverImageUrl) && !coverImageFailed;

    return (
        <AppCard
            onPress={onPress}
            style={styles.tripCard}
            contentStyle={styles.tripCardContent}
        >
            {shouldShowCoverImage ? (
                <Image
                    source={{ uri: coverImageUrl as string }}
                    style={styles.tripCoverImage}
                    onError={() => setCoverImageFailed(true)}
                />
            ) : null}

            <View style={styles.tripMainRow}>
                <View
                    style={[
                        styles.tripIconBadge,
                        { backgroundColor: colors.primarySoft },
                    ]}
                >
                    <Ionicons
                        name="airplane-outline"
                        size={20}
                        color={colors.primary}
                    />
                </View>

                <View style={styles.tripTextGroup}>
                    <Text
                        style={[styles.tripTitle, { color: colors.text }]}
                        numberOfLines={1}
                    >
                        {trip.tripName || "Untitled trip"}
                    </Text>

                    <Text
                        style={[
                            styles.tripDestination,
                            { color: colors.textMuted },
                        ]}
                        numberOfLines={1}
                    >
                        {trip.destination || "No destination"}
                    </Text>
                </View>

                <View style={styles.tripRightGroup}>
                    <StatusBadge status={status} />
                    <Ionicons
                        name="chevron-forward"
                        size={22}
                        color={colors.textMuted}
                    />
                </View>
            </View>

            <View style={styles.tripMetaRow}>
                <View
                    style={[
                        styles.metaPill,
                        {
                            backgroundColor: colors.surfaceSoft,
                            borderColor: colors.border,
                        },
                    ]}
                >
                    <Ionicons
                        name="calendar-outline"
                        size={14}
                        color={colors.textMuted}
                    />

                    <Text
                        style={[
                            styles.metaText,
                            { color: colors.textMuted },
                        ]}
                        numberOfLines={1}
                    >
                        {formatDateRange(trip.startDate, trip.endDate)}
                    </Text>
                </View>

                {trip.currentUserRole ? (
                    <RoleBadge role={trip.currentUserRole} />
                ) : null}
            </View>

            <View style={styles.tripMetaRow}>
                <View
                    style={[
                        styles.metaPill,
                        {
                            backgroundColor: colors.surfaceSoft,
                            borderColor: colors.border,
                        },
                    ]}
                >
                    <Ionicons
                        name="time-outline"
                        size={14}
                        color={colors.textMuted}
                    />

                    <Text
                        style={[
                            styles.metaText,
                            { color: colors.textMuted },
                        ]}
                        numberOfLines={1}
                    >
                        Updated {formatDate(trip.modifiedDate || trip.createdDate)}
                    </Text>
                </View>
            </View>
        </AppCard>
    );
}

type StatusBadgeProps = Readonly<{
    status: TripStatus;
}>;

function StatusBadge({ status }: StatusBadgeProps) {
    const labelByStatus: Record<TripStatus, string> = {
        PLANNING: "Planning",
        ONGOING: "Ongoing",
        FINISHED: "Done",
    };

    const styleByStatus: Record<
        TripStatus,
        {
            backgroundColor: string;
            borderColor: string;
            color: string;
        }
    > = {
        PLANNING: {
            backgroundColor: "#FFF7ED",
            borderColor: "#FDBA74",
            color: "#C2410C",
        },
        ONGOING: {
            backgroundColor: "#ECFDF5",
            borderColor: "#6EE7B7",
            color: "#047857",
        },
        FINISHED: {
            backgroundColor: "#F1F5F9",
            borderColor: "#CBD5E1",
            color: "#475569",
        },
    };

    const statusStyle = styleByStatus[status];

    return (
        <View
            style={[
                styles.statusBadge,
                {
                    backgroundColor: statusStyle.backgroundColor,
                    borderColor: statusStyle.borderColor,
                },
            ]}
        >
            <Text style={[styles.statusBadgeText, { color: statusStyle.color }]}>
                {labelByStatus[status]}
            </Text>
        </View>
    );
}

type RoleBadgeProps = Readonly<{
    role: NonNullable<Trip["currentUserRole"]>;
}>;

function RoleBadge({ role }: RoleBadgeProps) {
    const theme = useAppTheme();
    const colors = theme.colors;

    const labelByRole: Record<NonNullable<Trip["currentUserRole"]>, string> = {
        OWNER: "Owner",
        EDITOR: "Editor",
        VIEWER: "Viewer",
    };

    return (
        <View
            style={[
                styles.roleBadge,
                {
                    backgroundColor: colors.primarySoft,
                    borderColor: colors.primarySoft,
                },
            ]}
        >
            <Text style={[styles.roleBadgeText, { color: colors.primary }]}>
                {labelByRole[role]}
            </Text>
        </View>
    );
}
