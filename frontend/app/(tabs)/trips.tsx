import { useCallback, useMemo, useState } from "react";
import {
    Alert,
    Pressable,
    RefreshControl,
    ScrollView,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect, useRouter } from "expo-router";

import { getMyTrips } from "@/src/api/tripApi";
import { AppButton } from "@/src/components/ui/AppButton";
import { AppScreen } from "@/src/components/ui/AppScreen";
import { EmptyState } from "@/src/components/ui/EmptyState";
import { ErrorMessage } from "@/src/components/ui/ErrorMessage";
import { LoadingState } from "@/src/components/ui/LoadingState";
import { useAppTheme } from "@/src/hooks/useAppTheme";
import type { Trip, TripSortOption } from "@/src/types/trip";

import { FilterModal, TripSection } from "@/src/features/trips/TripListComponents";
import { styles } from "@/src/features/trips/tripListStyles";
import {
    DEFAULT_SORT_OPTION,
    DEFAULT_STATUS_FILTER,
    getApiMessage,
    getSortLabel,
    getStatusFilterLabel,
    type TripStatusFilter,
} from "@/src/features/trips/tripListUtils";

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
        } catch (error: unknown) {
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
        router.push("/trips/create");
    }

    function handleOpenTrip(trip: Trip) {
        if (!trip.tripId) {
            Alert.alert("Missing trip ID", "This trip cannot be opened.");
            return;
        }

        router.push({
            pathname: "/trips/[tripId]",
            params: { tripId: String(trip.tripId) },
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
