import { getTripMembers } from "../api/tripCollaborationApi";
import type { TripCollaborationRole, TripMember } from "../types/tripCollaboration";
import { getCurrentUsernameFromAccessToken } from "./authTokenUtils";

export function canEditTripPlan(role: TripCollaborationRole | null) {
    return role === "OWNER" || role === "EDITOR";
}

export function canManageTripMembers(role: TripCollaborationRole | null) {
    return role === "OWNER";
}

export function canDeleteTrip(role: TripCollaborationRole | null) {
    return role === "OWNER";
}

export function getRoleLabel(role: TripCollaborationRole | null) {
    if (role === "OWNER") return "Owner";
    if (role === "EDITOR") return "Editor";
    if (role === "VIEWER") return "Viewer";
    return "Member";
}

export async function getCurrentUserTripRole(tripId: number): Promise<{
    username: string | null;
    role: TripCollaborationRole | null;
    members: TripMember[];
}> {
    const [username, members] = await Promise.all([
        getCurrentUsernameFromAccessToken(),
        getTripMembers(tripId),
    ]);

    const currentMember = username
        ? members.find((member) => member.username === username)
        : null;

    return {
        username,
        role: currentMember?.role ?? null,
        members,
    };
}
