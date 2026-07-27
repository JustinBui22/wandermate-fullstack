import type { Trip, TripSortOption, TripStatus } from "@/src/types/trip";
import { getApiErrorMessage } from "@/src/utils/apiWarningUtils";

export type TripStatusFilter = "ALL" | TripStatus;

export const DEFAULT_STATUS_FILTER: TripStatusFilter = "ALL";
export const DEFAULT_SORT_OPTION: TripSortOption = "MODIFIED_DATE_DESC";

export const SORT_OPTIONS: ReadonlyArray<{
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

export const STATUS_FILTERS: ReadonlyArray<{
    label: string;
    value: TripStatusFilter;
}> = [
    { label: "All", value: "ALL" },
    { label: "Planning", value: "PLANNING" },
    { label: "Ongoing", value: "ONGOING" },
    { label: "Finished", value: "FINISHED" },
];

export function getApiMessage(error: unknown) {
    return getApiErrorMessage(error, "Failed to load trips.");
}

export function formatDateRange(startDate?: string, endDate?: string) {
    if (!startDate && !endDate) return "Dates not set";
    if (startDate && !endDate) return `Starts ${formatDate(startDate)}`;
    if (!startDate && endDate) return `Ends ${formatDate(endDate)}`;

    return `${formatDate(startDate)} → ${formatDate(endDate)}`;
}

function parseCalendarDate(value?: string) {
    if (!value) return null;

    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
    if (!match) return null;

    const date = new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
    return Number.isNaN(date.getTime()) ? null : date;
}

export function formatDate(value?: string) {
    if (!value) return "Not set";

    const date = parseCalendarDate(value);

    if (!date) {
        return value;
    }

    return date.toLocaleDateString(undefined, {
        day: "2-digit",
        month: "short",
        year: "numeric",
    });
}

export function resolveTripStatusFromDates(startDate?: string, endDate?: string): TripStatus {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const start = parseCalendarDate(startDate);
    const end = parseCalendarDate(endDate);

    if (start && today < start) {
        return "PLANNING";
    }

    if (end && today > end) {
        return "FINISHED";
    }

    return "ONGOING";
}

export function getTripStatus(trip: Trip): TripStatus {
    return trip.tripStatus ?? resolveTripStatusFromDates(trip.startDate, trip.endDate);
}

export function getSortLabel(sortOption: TripSortOption) {
    return SORT_OPTIONS.find((option) => option.value === sortOption)?.label ?? "Updated newest";
}

export function getStatusFilterLabel(statusFilter: TripStatusFilter) {
    return STATUS_FILTERS.find((option) => option.value === statusFilter)?.label ?? "All";
}
