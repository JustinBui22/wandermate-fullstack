import { useFocusEffect, useRouter } from "expo-router";
import type { Trip } from "@/src/types/trip";
import { useCallback, useState } from "react";
import {
  Alert,
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";

import { getMyTrips } from "@/src/api/tripApi";
import { colors, radius, shadow, spacing } from "@/src/theme/theme";

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
      setError(error.response?.data?.message || error.message || "Failed to load trips.");
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

  if (isLoading) {
    return (
        <View style={styles.centerContainer}>
          <ActivityIndicator color={colors.primary} />
          <Text style={styles.loadingText}>Loading trips...</Text>
        </View>
    );
  }

  return (
      <View style={styles.container}>
        <View style={styles.header}>
          <View>
            <Text style={styles.title}>My Trips</Text>
            <Text style={styles.subtitle}>Manage your travel plans</Text>
          </View>

          <Pressable
              onPress={() => router.push("/trips/create" as never)}
              style={styles.addButton}
          >
            <Ionicons name="add" size={24} color="#FFFFFF" />
          </Pressable>
        </View>

        {error ? (
            <View style={styles.errorBox}>
              <Ionicons name="alert-circle-outline" size={18} color={colors.error} />
              <Text style={styles.errorText}>{error}</Text>
            </View>
        ) : null}

        <FlatList
            data={trips}
            keyExtractor={(item, index) => String(item.tripId ?? index)}
            refreshControl={
              <RefreshControl refreshing={isRefreshing} onRefresh={handleRefresh} />
            }
            ListEmptyComponent={
              <View style={styles.emptyCard}>
                <View style={styles.emptyIcon}>
                  <Ionicons name="map-outline" size={34} color={colors.primary} />
                </View>
                <Text style={styles.emptyTitle}>No trips yet</Text>
                <Text style={styles.emptySubtitle}>
                  Create your first trip plan and it will appear here.
                </Text>
              </View>
            }
            renderItem={({ item }) => (
                <Pressable
                    style={styles.tripCard}
                    onPress={() => {
                      if (!item.tripId) {
                        Alert.alert("Missing trip ID", "This trip cannot be opened.");
                        return;
                      }

                      router.push({
                        pathname: "/trips/[tripId]" as never,
                        params: { tripId: String(item.tripId) } as never,
                      });
                    }}
                >
                  <View>
                    <Text style={styles.tripTitle}>
                      {item.tripName || "Untitled trip"}
                    </Text>
                    <Text style={styles.tripDestination}>
                      {item.destination || "No destinations"}
                    </Text>
                  </View>

                  <Ionicons name="chevron-forward" size={22} color={colors.mutedText} />
                </Pressable>
            )}
            contentContainerStyle={styles.listContent}
        />
      </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: spacing.lg,
    backgroundColor: colors.background,
  },
  centerContainer: {
    flex: 1,
    backgroundColor: colors.background,
    justifyContent: "center",
    alignItems: "center",
  },
  loadingText: {
    marginTop: spacing.sm,
    color: colors.mutedText,
    fontWeight: "600",
  },
  header: {
    marginTop: spacing.xl,
    marginBottom: spacing.lg,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  addButton: {
    width: 48,
    height: 48,
    borderRadius: 18,
    backgroundColor: colors.primary,
    alignItems: "center",
    justifyContent: "center",
    ...shadow.card,
  },
  title: {
    fontSize: 30,
    fontWeight: "900",
    color: colors.text,
  },
  subtitle: {
    marginTop: 4,
    fontSize: 15,
    color: colors.mutedText,
  },
  errorBox: {
    backgroundColor: "#FEF2F2",
    borderWidth: 1,
    borderColor: "#FECACA",
    borderRadius: radius.md,
    padding: spacing.md,
    flexDirection: "row",
    gap: spacing.sm,
    marginBottom: spacing.md,
  },
  errorText: {
    flex: 1,
    color: colors.error,
    fontWeight: "600",
  },
  listContent: {
    paddingBottom: 120,
  },
  emptyCard: {
    backgroundColor: colors.card,
    borderRadius: radius.xl,
    padding: spacing.xl,
    alignItems: "center",
    ...shadow.card,
  },
  emptyIcon: {
    width: 72,
    height: 72,
    borderRadius: 24,
    backgroundColor: colors.softBlue,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: spacing.md,
  },
  emptyTitle: {
    fontSize: 21,
    fontWeight: "900",
    color: colors.text,
    marginBottom: spacing.sm,
  },
  emptySubtitle: {
    textAlign: "center",
    color: colors.mutedText,
    lineHeight: 21,
  },
  tripCard: {
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    padding: spacing.md,
    marginBottom: spacing.md,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    ...shadow.card,
  },
  tripTitle: {
    fontSize: 17,
    fontWeight: "800",
    color: colors.text,
  },
  tripDestination: {
    marginTop: 4,
    fontSize: 14,
    color: colors.mutedText,
  },
});