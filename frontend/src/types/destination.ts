import type { ApiResponse } from "./trip";

export type Destination = {
    destinationId: number;
    destinationName: string;
    startDate: string;
    endDate: string;
    destinationOrder?: number | null;
    notes?: string | null;
    tripId: number;
    createdDate?: string;
    modifiedDate?: string | null;
    createdByUserId?: number | null;
    createdByUsername?: string | null;
    createdByDisplayName?: string | null;
    createdByProfileImageUrl?: string | null;
    modifiedByUserId?: number | null;
    modifiedByUsername?: string | null;
    modifiedByDisplayName?: string | null;
    modifiedByProfileImageUrl?: string | null;
};

export type CreateDestinationRequest = {
    destinationName: string;
    startDate: string;
    endDate: string;
    destinationOrder?: number | null;
    notes?: string | null;
    allowOverlap?: boolean;
};

export type UpdateDestinationRequest = {
    destinationName: string;
    startDate: string;
    endDate: string;
    destinationOrder?: number | null;
    notes?: string | null;
    allowOverlap?: boolean;
};

export type DestinationApiResponse<T> = ApiResponse<T>;