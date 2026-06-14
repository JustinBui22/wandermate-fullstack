import { useCallback, useState } from "react";
import {
  Alert,
  FlatList,
  RefreshControl,
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
import { colors, fontWeight, radius, spacing, typography } from "@/src/constants/theme";
import type { Trip } from "@/src/types/trip";

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

export default function TripsScreen() {
  const router = useRouter();
  const [trips, setTrips] = useState<Trip[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadTrips() {
    try {
      setError(null);
      const data = await getMyTrips();
      setTrips(Array.isArray(data) ? data : []);
    } catch (error: any) {
      setError(getApiMessage(error));
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }

  useFocusEffect(
      useCallback(() => {
        setIsLoading(true);
        loadTrips();
      }, [])
  );

  async function handleRefresh() {
    setIsRefreshing(true);
    await loadTrips();
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
        <View style={styles.header}>
          <View style={styles.headerTextGroup}>
            <Text style={styles.eyebrow}>WanderMate</Text>
            <Text style={styles.title}>My Trips</Text>
            <Text style={styles.subtitle}>Manage your travel plans</Text>
          </View>

          <AppButton
              title=""
              onPress={handleCreateTrip}
              fullWidth={false}
              style={styles.addButton}
              leftIcon={<Ionicons name="add" size={24} color={colors.textLight} />}
              testID="create-trip-button"
          />
        </View>

        <ErrorMessage message={error} title="Could not load trips" />

        <FlatList
            data={trips}
            keyExtractor={(item, index) => String(item.tripId ?? index)}
            refreshControl={
              <RefreshControl
                  refreshing={isRefreshing}
                  onRefresh={handleRefresh}
                  tintColor={colors.primary}
                  colors={[colors.primary]}
              />
            }
            ListEmptyComponent={
              <EmptyState
                  title="No trips yet"
                  message="Create your first trip plan and it will appear here."
                  icon={<Ionicons name="map-outline" size={30} color={colors.primary} />}
                  actionLabel="Create trip"
                  onActionPress={handleCreateTrip}
                  style={styles.emptyState}
              />
            }
            renderItem={({ item }) => (
                <TripCard trip={item} onPress={() => handleOpenTrip(item)} />
            )}
            contentContainerStyle={[
              styles.listContent,
              trips.length === 0 && styles.emptyListContent,
            ]}
            showsVerticalScrollIndicator={false}
        />
      </AppScreen>
  );
}

type TripCardProps = Readonly<{
  trip: Trip;
  onPress: () => void;
}>;

function TripCard({ trip, onPress }: TripCardProps) {
  return (
      <AppCard onPress={onPress} style={styles.tripCard} contentStyle={styles.tripCardContent}>
        <View style={styles.tripMainRow}>
          <View style={styles.tripIconBadge}>
            <Ionicons name="airplane-outline" size={20} color={colors.primary} />
          </View>

          <View style={styles.tripTextGroup}>
            <Text style={styles.tripTitle} numberOfLines={1}>
              {trip.tripName || "Untitled trip"}
            </Text>
            <Text style={styles.tripDestination} numberOfLines={1}>
              {trip.destination || "No destination"}
            </Text>
          </View>

          <Ionicons name="chevron-forward" size={22} color={colors.textMuted} />
        </View>

        <View style={styles.tripMetaRow}>
          <View style={styles.metaPill}>
            <Ionicons name="calendar-outline" size={14} color={colors.textMuted} />
            <Text style={styles.metaText} numberOfLines={1}>
              {formatDateRange(trip.startDate, trip.endDate)}
            </Text>
          </View>
        </View>
      </AppCard>
  );
}

const styles = StyleSheet.create({
  screenContent: {
    flex: 1,
    paddingTop: spacing.xl,
    paddingBottom: 0,
    gap: spacing.lg,
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
    color: colors.primary,
    fontSize: typography.caption,
    fontWeight: fontWeight.bold,
    textTransform: "uppercase",
    letterSpacing: 0.7,
  },
  title: {
    color: colors.text,
    fontSize: typography.hero,
    fontWeight: fontWeight.bold,
  },
  subtitle: {
    color: colors.textMuted,
    fontSize: typography.bodySmall,
    lineHeight: 20,
  },
  addButton: {
    width: 50,
    height: 50,
    minHeight: 50,
    borderRadius: radius.lg,
    paddingHorizontal: 0,
  },
  listContent: {
    paddingBottom: 120,
    gap: spacing.md,
  },
  emptyListContent: {
    flexGrow: 1,
    justifyContent: "center",
  },
  emptyState: {
    marginTop: spacing.xl,
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
    backgroundColor: colors.primarySoft,
    alignItems: "center",
    justifyContent: "center",
  },
  tripTextGroup: {
    flex: 1,
    gap: spacing.xs,
  },
  tripTitle: {
    color: colors.text,
    fontSize: typography.body,
    fontWeight: fontWeight.bold,
  },
  tripDestination: {
    color: colors.textMuted,
    fontSize: typography.bodySmall,
    lineHeight: 19,
  },
  tripMetaRow: {
    flexDirection: "row",
    alignItems: "center",
  },
  metaPill: {
    flexShrink: 1,
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.xs,
    backgroundColor: colors.surfaceSoft,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  metaText: {
    color: colors.textMuted,
    fontSize: typography.caption,
    fontWeight: fontWeight.semibold,
  },
});