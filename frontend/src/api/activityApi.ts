import { axiosClient } from "./axiosClient";
import type {
    Activity,
    ActivityApiResponse,
    CreateActivityRequest,
    UpdateActivityRequest,
} from "../types/activity";
import { logger } from "../utils/logger";

export async function getActivitiesByDestination(
    tripId: number,
    destinationId: number
): Promise<Activity[]> {
    const response = await axiosClient.get<ActivityApiResponse<Activity[]>>(
        `/api/v1/trips/${tripId}/destinations/${destinationId}/activities`
    );

    logger.debug("Activities response:", response.data);

    return response.data.body;
}

export async function getActivityById(
    tripId: number,
    destinationId: number,
    activityId: number
): Promise<Activity> {
    const response = await axiosClient.get<ActivityApiResponse<Activity>>(
        `/api/v1/trips/${tripId}/destinations/${destinationId}/activities/${activityId}`
    );

    logger.debug("Activity detail response:", response.data);

    return response.data.body;
}

export async function createActivity(
    tripId: number,
    destinationId: number,
    data: CreateActivityRequest
): Promise<Activity> {
    const response = await axiosClient.post<ActivityApiResponse<Activity>>(
        `/api/v1/trips/${tripId}/destinations/${destinationId}/activities`,
        data
    );

    logger.debug("Create activity response:", response.data);

    return response.data.body;
}

export async function updateActivity(
    tripId: number,
    destinationId: number,
    activityId: number,
    data: UpdateActivityRequest
): Promise<Activity> {
    const response = await axiosClient.put<ActivityApiResponse<Activity>>(
        `/api/v1/trips/${tripId}/destinations/${destinationId}/activities/${activityId}`,
        data
    );

    logger.debug("Update activity response:", response.data);

    return response.data.body;
}

export async function deleteActivity(
    tripId: number,
    destinationId: number,
    activityId: number
): Promise<void> {
    await axiosClient.delete(
        `/api/v1/trips/${tripId}/destinations/${destinationId}/activities/${activityId}`
    );
}