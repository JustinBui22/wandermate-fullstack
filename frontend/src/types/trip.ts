export type TripRole = "OWNER" | "EDITOR" | "VIEWER";

export type TripStatus = "PLANNING" | "ONGOING" | "FINISHED";

export type TripOwnershipFilter = "ALL" | "CREATED" | "JOINED";

export type TripSortOption =
    | "NAME_ASC"
    | "NAME_DESC"
    | "CREATED_DATE_ASC"
    | "CREATED_DATE_DESC"
    | "MODIFIED_DATE_ASC"
    | "MODIFIED_DATE_DESC";

export type Trip = {
    tripId: number;
    tripName: string;
    destination: string;
    startDate: string;
    endDate: string;
    username?: string;

    // V3 collaboration and filter fields returned by backend
    createdDate?: string;
    modifiedDate?: string;
    tripStatus?: TripStatus;
    currentUserRole?: TripRole;
};

export type GetTripsParams = {
    ownership?: TripOwnershipFilter;
    status?: "ALL" | TripStatus;
    sort?: TripSortOption;
};

export type CreateTripRequest = {
    tripName: string;
    destination: string;
    startDate: string;
    endDate: string;
    allowOverlap?: boolean;
    tripStatus?: TripStatus;
};

export type UpdateTripRequest = {
    tripName: string;
    destination: string;
    startDate: string;
    endDate: string;
    allowOverlap?: boolean;
    tripStatus?: TripStatus;
};

export type ApiResponse<T> = {
    code: string;
    message: string;
    flow: string;
    body: T;
};
