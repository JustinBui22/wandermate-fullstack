import { axiosClient } from "./axiosClient";
import type {
    CreateDestinationRequest,
    Destination,
    DestinationApiResponse,
    UpdateDestinationRequest,
} from "../types/destination";
import { logger } from "../utils/logger";

export async function getDestinationsByTrip(tripId: number): Promise<Destination[]> {
    const response = await axiosClient.get<DestinationApiResponse<Destination[]>>(
        `/api/v1/trips/${tripId}/destinations`
    );

    logger.debug("Destinations response:", response.data);

    return response.data.body;
}

export async function getDestinationById(
    tripId: number,
    destinationId: number
): Promise<Destination> {
    const response = await axiosClient.get<DestinationApiResponse<Destination>>(
        `/api/v1/trips/${tripId}/destinations/${destinationId}`
    );

    logger.debug("Destination detail response:", response.data);

    return response.data.body;
}

export async function createDestination(
    tripId: number,
    data: CreateDestinationRequest
): Promise<Destination> {
    const response = await axiosClient.post<DestinationApiResponse<Destination>>(
        `/api/v1/trips/${tripId}/destinations`,
        data
    );

    logger.debug("Create destinations response:", response.data);

    return response.data.body;
}

export async function updateDestination(
    tripId: number,
    destinationId: number,
    data: UpdateDestinationRequest
): Promise<Destination> {
    const response = await axiosClient.put<DestinationApiResponse<Destination>>(
        `/api/v1/trips/${tripId}/destinations/${destinationId}`,
        data
    );

    logger.debug("Update destinations response:", response.data);

    return response.data.body;
}

export async function deleteDestination(
    tripId: number,
    destinationId: number
): Promise<void> {
    await axiosClient.delete(
        `/api/v1/trips/${tripId}/destinations/${destinationId}`
    );
}