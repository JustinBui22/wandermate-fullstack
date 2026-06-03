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