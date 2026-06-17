export type TripCollaborationRole = "OWNER" | "EDITOR" | "VIEWER";
export type TripCollaborationRequestType = "INVITATION" | "JOIN_REQUEST";
export type TripCollaborationRequestStatus = "PENDING" | "ACCEPTED" | "REJECTED" | "CANCELLED";

export type ApiResponse<T> = {
    code: string;
    message: string;
    flow?: string;
    body: T;
};

export type TripMember = {
    tripMemberId: number;
    tripId: number;
    userId: number;
    username: string;
    email?: string;
    role: TripCollaborationRole;
    createdDate?: string;
    modifiedDate?: string | null;
};

export type SendTripInvitationRequest = {
    username: string;
    role: Exclude<TripCollaborationRole, "OWNER">;
};

export type SendTripJoinRequest = {
    role: Exclude<TripCollaborationRole, "OWNER">;
};

export type UpdateTripMemberRoleRequest = {
    role: Exclude<TripCollaborationRole, "OWNER">;
};

export type TripCollaborationRequest = {
    requestId: number;
    tripId: number;
    tripName: string;
    destination?: string;
    tripStartDate?: string;
    tripEndDate?: string;
    requesterUserId: number;
    requesterUsername: string;
    targetUserId: number;
    targetUsername: string;
    requestedRole: Exclude<TripCollaborationRole, "OWNER">;
    requestType: TripCollaborationRequestType;
    status: TripCollaborationRequestStatus;
    createdDate?: string;
    modifiedDate?: string | null;
    respondedDate?: string | null;
};

export type MyTripOverlapWarning = {
    currentTripId: number;
    currentTripName: string;
    currentTripStartDate: string;
    currentTripEndDate: string;
    overlappingTripId: number;
    overlappingTripName: string;
    overlappingTripStartDate: string;
    overlappingTripEndDate: string;
    overlapStartDate: string;
    overlapEndDate: string;
    message: string;
};

export type TripCollaborationActionResponse = {
    request: TripCollaborationRequest;
    member: TripMember;
    overlapWarnings: MyTripOverlapWarning[];
};
