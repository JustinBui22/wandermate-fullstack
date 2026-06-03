import { axiosClient } from "./axiosClient";
import type {
    CreateDestinationRequest,
    Destination,
    DestinationApiResponse,
    UpdateDestinationRequest,
} from "../types/destination";

export async function getDestinationsByTrip(tripId: number): Promise<Destination[]> {
    const response = await axiosClient.get<DestinationApiResponse<Destination[]>>(
        `/api/v1/trips/${tripId}/destinations`
    );

    console.log("Destinations response:", response.data);

    return response.data.body;
}

export async function getDestinationById(
    tripId: number,
    destinationId: number
): Promise<Destination> {
    const response = await axiosClient.get<DestinationApiResponse<Destination>>(
        `/api/v1/trips/${tripId}/destinations/${destinationId}`
    );

    console.log("Destination detail response:", response.data);

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

    console.log("Create destinations response:", response.data);

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

    console.log("Update destinations response:", response.data);

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