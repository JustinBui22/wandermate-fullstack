export const logger = {
    debug: (..._args: unknown[]) => {
        // Intentionally disabled. Keep this wrapper so old imports do not break.
    },

    error: (..._args: unknown[]) => {
        // Intentionally disabled. User-facing screens handle errors with clean messages.
    },
};