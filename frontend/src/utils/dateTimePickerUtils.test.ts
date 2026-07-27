import { describe, expect, it } from "vitest";

import {
    formatDateForBackend,
    parseDateOnly,
} from "@/src/utils/dateTimePickerUtils";


describe("date-only picker utilities", () => {
    it("formats a calendar date without adding a timezone or time component", () => {
        const date = new Date(2027, 3, 5, 23, 45, 30);

        expect(formatDateForBackend(date)).toBe("2027-04-05");
    });

    it("parses an ISO calendar date into the same local year, month and day", () => {
        const parsed = parseDateOnly("2027-04-05", new Date(2000, 0, 1));

        expect(parsed.getFullYear()).toBe(2027);
        expect(parsed.getMonth()).toBe(3);
        expect(parsed.getDate()).toBe(5);
    });

    it("uses the fallback when the backend value is not a calendar date", () => {
        const fallback = new Date(2000, 0, 1);

        expect(parseDateOnly("2027-04-05T00:00:00", fallback)).toBe(fallback);
    });
});
