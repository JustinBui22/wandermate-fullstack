export type PickerTarget = "startDate" | "startTime" | "endDate" | "endTime" | null;

export function pad(value: number) {
    return String(value).padStart(2, "0");
}

export function formatForBackend(date: Date) {
    const year = date.getFullYear();
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    const hour = pad(date.getHours());
    const minute = pad(date.getMinutes());
    const second = pad(date.getSeconds());

    return `${year}-${month}-${day}T${hour}:${minute}:${second}`;
}


export function formatDateForBackend(date: Date) {
    const year = date.getFullYear();
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());

    return `${year}-${month}-${day}`;
}

export function parseDateOnly(value: string | undefined, fallback: Date) {
    if (!value) return fallback;

    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
    if (!match) return fallback;

    const parsed = new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]));
    return Number.isNaN(parsed.getTime()) ? fallback : parsed;
}

export function formatDisplayDate(date: Date) {
    return date.toLocaleDateString(undefined, {
        weekday: "short",
        day: "2-digit",
        month: "short",
        year: "numeric",
    });
}

export function formatDisplayTime(date: Date) {
    return date.toLocaleTimeString(undefined, {
        hour: "2-digit",
        minute: "2-digit",
    });
}

export function updateDatePart(current: Date, selected: Date) {
    const updated = new Date(current);
    updated.setFullYear(selected.getFullYear());
    updated.setMonth(selected.getMonth());
    updated.setDate(selected.getDate());
    return updated;
}

export function updateTimePart(current: Date, selected: Date) {
    const updated = new Date(current);
    updated.setHours(selected.getHours());
    updated.setMinutes(selected.getMinutes());
    updated.setSeconds(0);
    updated.setMilliseconds(0);
    return updated;
}

export function getPickerTitle(activePicker: PickerTarget) {
    if (activePicker === "startDate") return "Choose start date";
    if (activePicker === "startTime") return "Choose start time";
    if (activePicker === "endDate") return "Choose end date";
    if (activePicker === "endTime") return "Choose end time";
    return "Choose date/time";
}

export function getPickerMode(activePicker: PickerTarget) {
    return activePicker === "startTime" || activePicker === "endTime" ? "time" : "date";
}

export function getPickerValue(
    activePicker: PickerTarget,
    startDateTime: Date,
    endDateTime: Date
) {
    return activePicker === "startDate" || activePicker === "startTime"
        ? startDateTime
        : endDateTime;
}