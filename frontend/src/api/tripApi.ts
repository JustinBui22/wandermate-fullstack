import { axiosClient } from "./axiosClient";
import type {
    ApiResponse,
    CreateTripRequest,
    GetTripsParams,
    Trip,
    UpdateTripRequest,
} from "../types/trip";
import { logger } from "../utils/logger";

function cleanGetTripsParams(params?: GetTripsParams) {
    if (!params) {
        return undefined;
    }

    return {
        ownership: params.ownership ?? "ALL",
        status: params.status ?? "ALL",
        sort: params.sort ?? "MODIFIED_DATE_DESC",
    };
}

export async function getMyTrips(params?: GetTripsParams): Promise<Trip[]> {
    const response = await axiosClient.get<ApiResponse<Trip[]>>("/api/v1/trips", {
        params: cleanGetTripsParams(params),
    });

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
