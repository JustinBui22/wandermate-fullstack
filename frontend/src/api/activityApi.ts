import { axiosClient } from "./axiosClient";
import type {
    Activity,
    ActivityApiResponse,
    CreateActivityRequest,
    UpdateActivityRequest,
} from "../types/activity";

export async function getActivitiesByDestination(
    tripId: number,
    destinationId: number
): Promise<Activity[]> {
    const response = await axiosClient.get<ActivityApiResponse<Activity[]>>(
        `/api/v1/trips/${tripId}/destinations/${destinationId}/activities`
    );

    console.log("Activities response:", response.data);

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

    console.log("Activity detail response:", response.data);

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

    console.log("Create activity response:", response.data);

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

    console.log("Update activity response:", response.data);

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