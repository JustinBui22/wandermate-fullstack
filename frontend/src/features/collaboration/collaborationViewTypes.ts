export type CollaborationAction =
    | "ACCEPT_INVITATION"
    | "REJECT_INVITATION"
    | "ACCEPT_JOIN_REQUEST"
    | "REJECT_JOIN_REQUEST";

export type LoadingAction = {
    requestId: number;
    action: CollaborationAction;
} | null;
