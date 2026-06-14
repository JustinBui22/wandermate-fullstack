import { axiosClient } from "./axiosClient";
import type {ApiResponse, CreateTripRequest, Trip, UpdateTripRequest} from "../types/trip";
import { logger } from "../utils/logger";

export async function getMyTrips(): Promise<Trip[]> {
    const response = await axiosClient.get<ApiResponse<Trip[]>>("/api/v1/trips");

    logger.debug("Trips response:", response.data);

    return response.data.body;
}

export async function getTripById(tripId: number): Promise<Trip> {
    const response = await axiosClient.get<ApiResponse<Trip>>(
        `/api/v1/trips/${tripId}`
    );

    logger.debug("Trip detail response:", response.data);

    return response.data.body;
}

export async function createTrip(data: CreateTripRequest): Promise<Trip> {
    const response = await axiosClient.post<ApiResponse<Trip>>(
        "/api/v1/trips",
        data
    );

    logger.debug("Create trip response:", response.data);

    return response.data.body;
}

export async function updateTrip(
    tripId: number,
    data: UpdateTripRequest
): Promise<Trip> {
    const response = await axiosClient.put<ApiResponse<Trip>>(
        `/api/v1/trips/${tripId}`,
        data
    );

    logger.debug("Update trip response:", response.data);

    return response.data.body;
}

export async function deleteTrip(tripId: number): Promise<void> {
    const response = await axiosClient.delete(`/api/v1/trips/${tripId}`);
    logger.debug("Delete trip response:", response.data);
}