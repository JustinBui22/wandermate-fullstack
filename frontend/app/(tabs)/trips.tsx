import { useCallback, useMemo, useState } from "react";
import {
    Alert,
    Modal,
    Pressable,
    RefreshControl,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useRouter } from "expo-router";

import { getMyTrips } from "@/src/api/tripApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppCard } from "@/src/components/ui/AppCard";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { Trip, TripSortOption, TripStatus } from "@/src/types/trip";

type TripStatusFilter = "ALL" | TripStatus;

const DEFAULT_STATUS_FILTER: TripStatusFilter = "ALL";
const DEFAULT_SORT_OPTION: TripSortOption = "MODIFIED_DATE_DESC";

const SORT_OPTIONS: ReadonlyArray<{
    label: string;
    value: TripSortOption;
}> = [
    { label: "Name A-Z", value: "NAME_ASC" },
    { label: "Name Z-A", value: "NAME_DESC" },
    { label: "Created newest", value: "CREATED_DATE_DESC" },
    { label: "Created oldest", value: "CREATED_DATE_ASC" },
    { label: "Updated newest", value: "MODIFIED_DATE_DESC" },
    { label: "Updated oldest", value: "MODIFIED_DATE_ASC" },
];

const STATUS_FILTERS: ReadonlyArray<{
    label: string;
    value: TripStatusFilter;
}> = [
    { label: "All", value: "ALL" },
    { label: "Planning", value: "PLANNING" },
    { label: "Ongoing", value: "ONGOING" },
    { label: "Finished", value: "FINISHED" },
];

function getApiMessage(error: any) {
    const data = error.response?.data;

    if (typeof data?.body === "string" && data.body.trim()) {
        return data.body;
    }

    return data?.message || error.message || "Failed to load trips.";
}

function formatDateRange(startDate?: string, endDate?: string) {
    if (!startDate && !endDate) return "Dates not set";
    if (startDate && !endDate) return `Starts ${formatDate(startDate)}`;
    if (!startDate && endDate) return `Ends ${formatDate(endDate)}`;

    return `${formatDate(startDate)} → ${formatDate(endDate)}`;
}

function formatDate(value?: string) {
    if (!value) return "Not set";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleDateString(undefined, {
        day: "2-digit",
        month: "short",
        year: "numeric",
    });
}

function resolveTripStatusFromDates(startDate?: string, endDate?: string): TripStatus {
    const now = new Date();

    const start = startDate ? new Date(startDate) : null;
    const end = endDate ? new Date(endDate) : null;

    const hasValidStart = start && !Number.isNaN(start.getTime());
    const hasValidEnd = end && !Number.isNaN(end.getTime());

    if (hasValidStart && now < start) {
        return "PLANNING";
    }

    if (hasValidEnd && now > end) {
        return "FINISHED";
    }

    return "ONGOING";
}

function getTripStatus(trip: Trip): TripStatus {
    return trip.tripStatus ?? resolveTripStatusFromDates(trip.startDate, trip.endDate);
}

function getSortLabel(sortOption: TripSortOption) {
    return SORT_OPTIONS.find((option) => option.value === sortOption)?.label ?? "Updated newest";
}

function getStatusFilterLabel(statusFilter: TripStatusFilter) {
    return STATUS_FILTERS.find((option) => option.value === statusFilter)?.label ?? "All";
}

export default function TripsScreen() {
    const router = useRouter();

    const theme = useAppTheme();
    const colors = theme.colors;

    const [trips, setTrips] = useState<Trip[]>([]);
    const [sortOption, setSortOption] = useState<TripSortOption>(DEFAULT_SORT_OPTION);
    const [statusFilter, setStatusFilter] = useState<TripStatusFilter>(DEFAULT_STATUS_FILTER);

    const [draftSortOption, setDraftSortOption] =
        useState<TripSortOption>(DEFAULT_SORT_OPTION);
    const [draftStatusFilter, setDraftStatusFilter] =
        useState<TripStatusFilter>(DEFAULT_STATUS_FILTER);

    const [isFilterModalVisible, setIsFilterModalVisible] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadTrips = useCallback(async (
        nextStatusFilter: TripStatusFilter = statusFilter,
        nextSortOption: TripSortOption = sortOption
    ) => {
        try {
            setError(null);

            const data = await getMyTrips({
                ownership: "ALL",
                status: nextStatusFilter,
                sort: nextSortOption,
            });

            setTrips(Array.isArray(data) ? data : []);
        } catch (error: any) {
            setError(getApiMessage(error));
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
        }
    }, [statusFilter, sortOption]);

    useFocusEffect(
        useCallback(() => {
            setIsLoading(true);
            void loadTrips();
        }, [loadTrips])
    );

    const createdTrips = useMemo(() => {
        return trips.filter(
            (trip) => trip.currentUserRole === "OWNER" || !trip.currentUserRole
        );
    }, [trips]);

    const joinedTrips = useMemo(() => {
        return trips.filter(
            (trip) => trip.currentUserRole === "EDITOR" || trip.currentUserRole === "VIEWER"
        );
    }, [trips]);

    const hasTrips = trips.length > 0;
    const hasVisibleTrips = createdTrips.length > 0 || joinedTrips.length > 0;
    const hasActiveFilter =
        statusFilter !== DEFAULT_STATUS_FILTER || sortOption !== DEFAULT_SORT_OPTION;

    async function performRefresh() {
        setIsRefreshing(true);
        await loadTrips();
    }

    function handleRefresh() {
        void performRefresh();
    }

    function handleCreateTrip() {
        router.push("/trips/create" as never);
    }

    function handleOpenTrip(trip: Trip) {
        if (!trip.tripId) {
            Alert.alert("Missing trip ID", "This trip cannot be opened.");
            return;
        }

        router.push({
            pathname: "/trips/[tripId]" as never,
            params: { tripId: String(trip.tripId) } as never,
        });
    }

    function handleOpenFilterModal() {
        setDraftSortOption(sortOption);
        setDraftStatusFilter(statusFilter);
        setIsFilterModalVisible(true);
    }

    function handleCloseFilterModal() {
        setIsFilterModalVisible(false);
    }

    async function performApplyFilters() {
        setSortOption(draftSortOption);
        setStatusFilter(draftStatusFilter);
        setIsFilterModalVisible(false);
        setIsLoading(true);
        await loadTrips(draftStatusFilter, draftSortOption);
    }

    function handleApplyFilters() {
        void performApplyFilters();
    }

    function handleResetDraftFilters() {
        setDraftSortOption(DEFAULT_SORT_OPTION);
        setDraftStatusFilter(DEFAULT_STATUS_FILTER);
    }

    async function performClearFilters() {
        setSortOption(DEFAULT_SORT_OPTION);
        setStatusFilter(DEFAULT_STATUS_FILTER);
        setIsLoading(true);
        await loadTrips(DEFAULT_STATUS_FILTER, DEFAULT_SORT_OPTION);
    }

    function handleClearFilters() {
        void performClearFilters();
    }

    function renderTripContent() {
        if (!hasTrips) {
            return (
                <EmptyState
                    title="No trips yet"
                    message="Create your first trip plan and it will appear here."
                    icon={
                        <Ionicons
                            name="map-outline"
                            size={30}
                            color={colors.primary}
                        />
                    }
                    actionLabel="Create trip"
                    onActionPress={handleCreateTrip}
                    style={styles.emptyState}
                />
            );
        }

        if (hasVisibleTrips) {
            return (
                <>
                    <TripSection
                        title="Trips I created"
                        subtitle="Trips where you are the owner."
                        trips={createdTrips}
                        emptyMessage="No created trips match your current filters."
                        onOpenTrip={handleOpenTrip}
                    />

                    <TripSection
                        title="Trips I joined"
                        subtitle="Trips shared with you as editor or viewer."
                        trips={joinedTrips}
                        emptyMessage="No joined trips match your current filters."
                        onOpenTrip={handleOpenTrip}
                    />
                </>
            );
        }

        return (
            <EmptyState
                title="No trips match your filters"
                message="Try changing the status filter or sorting option."
                icon={
                    <Ionicons
                        name="filter-outline"
                        size={30}
                        color={colors.primary}
                    />
                }
                actionLabel="Clear filter"
                onActionPress={handleClearFilters}
                style={styles.emptyState}
            />
        );
    }

    if (isLoading) {
        return (
            <AppScreen scroll={false} centerContent>
                <LoadingState
                    title="Loading trips..."
                    subtitle="Getting your travel plans ready."
                    fullScreen
                />
            </AppScreen>
        );
    }

    return (
        <AppScreen scroll={false} contentContainerStyle={styles.screenContent}>
            <ScrollView
                refreshControl={
                    <RefreshControl
                        refreshing={isRefreshing}
                        onRefresh={handleRefresh}
                        tintColor={colors.primary}
                        colors={[colors.primary]}
                    />
                }
                contentContainerStyle={[
                    styles.scrollContent,
                    !hasTrips && styles.emptyScrollContent,
                ]}
                showsVerticalScrollIndicator={false}
            >
                <View style={styles.header}>
                    <View style={styles.headerTextGroup}>
                        <Text style={[styles.eyebrow, { color: colors.primary }]}>
                            WanderMate
                        </Text>

                        <Text style={[styles.title, { color: colors.text }]}>
                            My Trips
                        </Text>

                        <Text style={[styles.subtitle, { color: colors.textMuted }]}>
                            Manage trips you created and trips shared with you.
                        </Text>
                    </View>

                    <View style={styles.headerActions}>
                        <Pressable
                            accessibilityRole="button"
                            onPress={handleOpenFilterModal}
                            style={({ pressed }) => [
                                styles.filterIconButton,
                                {
                                    borderColor: hasActiveFilter ? colors.primary : colors.border,
                                    backgroundColor: hasActiveFilter ? colors.primary : colors.surface,
                                },
                                pressed && styles.pressed,
                            ]}
                        >
                            <Ionicons
                                name="filter"
                                size={22}
                                color={hasActiveFilter ? colors.textLight : colors.primary}
                            />

                            {hasActiveFilter ? <View style={styles.filterDot} /> : null}
                        </Pressable>

                        <AppButton
                            title=""
                            onPress={handleCreateTrip}
                            fullWidth={false}
                            style={styles.addButton}
                            leftIcon={
                                <Ionicons
                                    name="add"
                                    size={24}
                                    color={colors.textLight}
                                />
                            }
                            testID="create-trip-button"
                        />
                    </View>
                </View>

                <ErrorMessage message={error} title="Could not load trips" />

                {hasTrips || hasActiveFilter ? (
                    <View
                        style={[
                            styles.filterSummary,
                            {
                                borderColor: colors.border,
                                backgroundColor: colors.surfaceSoft,
                            },
                        ]}
                    >
                        <Ionicons
                            name="options-outline"
                            size={16}
                            color={colors.textMuted}
                        />

                        <Text
                            style={[
                                styles.filterSummaryText,
                                { color: colors.textMuted },
                            ]}
                            numberOfLines={1}
                        >
                            {getStatusFilterLabel(statusFilter)} · {getSortLabel(sortOption)}
                        </Text>
                    </View>
                ) : null}

                {renderTripContent()}
            </ScrollView>

            <FilterModal
                visible={isFilterModalVisible}
                sortOption={draftSortOption}
                statusFilter={draftStatusFilter}
                onChangeSortOption={setDraftSortOption}
                onChangeStatusFilter={setDraftStatusFilter}
                onReset={handleResetDraftFilters}
                onCancel={handleCloseFilterModal}
                onApply={handleApplyFilters}
            />
        </AppScreen>
    );
}

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

function FilterModal({
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

function TripSection({
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

    return (
        <AppCard
            onPress={onPress}
            style={styles.tripCard}
            contentStyle={styles.tripCardContent}
        >
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

const styles = StyleSheet.create({
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