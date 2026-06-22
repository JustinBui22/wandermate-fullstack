import { axiosClient } from "./axiosClient";
import type {
    ApiResponse,
    GenerateTripShareCodeRequest,
    MyTripOverlapWarning,
    SendTripInvitationRequest,
    SendTripJoinRequest,
    TripCollaborationActionResponse,
    TripCollaborationRequest,
    TripShareCode,
    TripShareCodePreview,
    TripMember,
    UpdateTripMemberRoleRequest,
} from "../types/tripCollaboration";
import { logger } from "../utils/logger";

export async function getTripMembers(tripId: number): Promise<TripMember[]> {
    const response = await axiosClient.get<ApiResponse<TripMember[]>>(
        `/api/v1/trips/${tripId}/members`
    );

    logger.debug("Trip members response:", response.data);

    return response.data.body;
}

export async function updateTripMemberRole(
    tripId: number,
    tripMemberId: number,
    data: UpdateTripMemberRoleRequest
): Promise<TripMember> {
    const response = await axiosClient.patch<ApiResponse<TripMember>>(
        `/api/v1/trips/${tripId}/members/${tripMemberId}/role`,
        data
    );

    logger.debug("Update trip member role response:", response.data);

    return response.data.body;
}

export async function removeTripMember(
    tripId: number,
    tripMemberId: number
): Promise<void> {
    const response = await axiosClient.delete<ApiResponse<null>>(
        `/api/v1/trips/${tripId}/members/${tripMemberId}`
    );

    logger.debug("Remove trip member response:", response.data);
}

export async function sendTripInvitation(
    tripId: number,
    data: SendTripInvitationRequest
): Promise<TripCollaborationRequest> {
    const response = await axiosClient.post<ApiResponse<TripCollaborationRequest>>(
        `/api/v1/trips/${tripId}/invitations`,
        data
    );

    logger.debug("Send trip invitation response:", response.data);

    return response.data.body;
}

export async function getMyPendingInvitations(): Promise<TripCollaborationRequest[]> {
    const response = await axiosClient.get<ApiResponse<TripCollaborationRequest[]>>(
        "/api/v1/trips/invitations/received"
    );

    logger.debug("My pending invitations response:", response.data);

    return response.data.body;
}

export async function acceptInvitation(
    requestId: number
): Promise<TripCollaborationActionResponse> {
    const response = await axiosClient.patch<ApiResponse<TripCollaborationActionResponse>>(
        `/api/v1/trips/invitations/${requestId}/accept`
    );

    logger.debug("Accept invitation response:", response.data);

    return response.data.body;
}

export async function rejectInvitation(
    requestId: number
): Promise<TripCollaborationRequest> {
    const response = await axiosClient.patch<ApiResponse<TripCollaborationRequest>>(
        `/api/v1/trips/invitations/${requestId}/reject`
    );

    logger.debug("Reject invitation response:", response.data);

    return response.data.body;
}

export async function requestToJoinTrip(
    tripId: number,
    data: SendTripJoinRequest
): Promise<TripCollaborationRequest> {
    const response = await axiosClient.post<ApiResponse<TripCollaborationRequest>>(
        `/api/v1/trips/${tripId}/join-requests`,
        data
    );

    logger.debug("Request to join trip response:", response.data);

    return response.data.body;
}

export async function getPendingJoinRequests(
    tripId: number
): Promise<TripCollaborationRequest[]> {
    const response = await axiosClient.get<ApiResponse<TripCollaborationRequest[]>>(
        `/api/v1/trips/${tripId}/join-requests`
    );

    logger.debug("Pending join requests response:", response.data);

    return response.data.body;
}

export async function acceptJoinRequest(
    requestId: number
): Promise<TripCollaborationActionResponse> {
    const response = await axiosClient.patch<ApiResponse<TripCollaborationActionResponse>>(
        `/api/v1/trips/join-requests/${requestId}/accept`
    );

    logger.debug("Accept join request response:", response.data);

    return response.data.body;
}

export async function rejectJoinRequest(
    requestId: number
): Promise<TripCollaborationRequest> {
    const response = await axiosClient.patch<ApiResponse<TripCollaborationRequest>>(
        `/api/v1/trips/join-requests/${requestId}/reject`
    );

    logger.debug("Reject join request response:", response.data);

    return response.data.body;
}

export async function getMyOverlapWarnings(
    tripId: number
): Promise<MyTripOverlapWarning[]> {
    const response = await axiosClient.get<ApiResponse<MyTripOverlapWarning[]>>(
        `/api/v1/trips/${tripId}/my-overlap-warnings`
    );

    logger.debug("My overlap warnings response:", response.data);

    return response.data.body;
}

export async function regenerateTripShareCode(
    tripId: number,
    data?: GenerateTripShareCodeRequest
): Promise<TripShareCode> {
    const response = await axiosClient.post<ApiResponse<TripShareCode>>(
        `/api/v1/trips/${tripId}/share-codes/regenerate`,
        data ?? {}
    );

    logger.debug("Regenerate trip share code response:", response.data);

    return response.data.body;
}

export async function getActiveTripShareCode(
    tripId: number
): Promise<TripShareCode | null> {
    const response = await axiosClient.get<ApiResponse<TripShareCode | null>>(
        `/api/v1/trips/${tripId}/share-codes/active`
    );

    logger.debug("Active trip share code response:", response.data);
    return response.data.body ?? null;
}

export async function previewTripShareCode(
    code: string
): Promise<TripShareCodePreview> {
    const response = await axiosClient.get<ApiResponse<TripShareCodePreview>>(
        `/api/v1/trips/share-codes/${encodeURIComponent(code)}`
    );

    logger.debug("Preview trip share code response:", response.data);

    return response.data.body;
}

export async function requestToJoinByShareCode(
    code: string
): Promise<TripCollaborationRequest> {
    const response = await axiosClient.post<ApiResponse<TripCollaborationRequest>>(
        `/api/v1/trips/share-codes/${encodeURIComponent(code)}/join-requests`
    );

    logger.debug("Request to join by share code response:", response.data);

    return response.data.body;
}
