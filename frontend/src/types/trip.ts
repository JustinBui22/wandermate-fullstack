export type Trip = {
    tripId: number;
    tripName: string;
    destination: string;
    startDate: string;
    endDate: string;
    username?: string;
};

export type CreateTripRequest = {
    tripName: string;
    destination: string;
    startDate: string;
    endDate: string;
    allowOverlap?: boolean;
};

export type UpdateTripRequest = {
    tripName: string;
    destination: string;
    startDate: string;
    endDate: string;
    allowOverlap?: boolean;
};

export type ApiResponse<T> = {
    code: string;
    message: string;
    flow: string;
    body: T;
};