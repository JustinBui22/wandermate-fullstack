import { getTripById } from "../api/tripApi";
import { getTripMembers } from "../api/tripCollaborationApi";
import type { TripRole } from "../types/trip";
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

function toCollaborationRole(role?: TripRole | TripCollaborationRole | null): TripCollaborationRole | null {
    if (role === "OWNER" || role === "EDITOR" || role === "VIEWER") {
        return role;
    }

    return null;
}

export async function getCurrentUserTripRole(tripId: number): Promise<{
    username: string | null;
    role: TripCollaborationRole | null;
    members: TripMember[];
}> {
    const username = await getCurrentUsernameFromAccessToken();

    let members: TripMember[] = [];
    let memberRole: TripCollaborationRole | null = null;

    try {
        members = await getTripMembers(tripId);

        const currentMember = username
            ? members.find((member) => member.username === username)
            : null;

        memberRole = currentMember?.role ?? null;
    } catch {
        members = [];
    }

    if (memberRole) {
        return {
            username,
            role: memberRole,
            members,
        };
    }

    try {
        const trip = await getTripById(tripId);
        return {
            username,
            role: toCollaborationRole(trip.currentUserRole),
            members,
        };
    } catch {
        return {
            username,
            role: null,
            members,
        };
    }
}
