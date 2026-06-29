import type { ApiResponse } from "./trip";

export type Activity = {
    activityId: number;
    destinationId: number;
    tripId: number;
    activityName: string;
    location?: string | null;
    description?: string | null;
    startDateTime: string;
    endDateTime: string;
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

export type CreateActivityRequest = {
    activityName: string;
    location?: string | null;
    description?: string | null;
    startDateTime: string;
    endDateTime: string;
};

export type UpdateActivityRequest = {
    activityName: string;
    location?: string | null;
    description?: string | null;
    startDateTime: string;
    endDateTime: string;
};

export type ActivityApiResponse<T> = ApiResponse<T>;