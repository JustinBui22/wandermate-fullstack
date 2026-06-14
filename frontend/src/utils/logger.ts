export const logger = {
    debug: (...args: unknown[]) => {
        if (__DEV__) {
            logger.debug(...args);
        }
    },

    error: (...args: unknown[]) => {
        if (__DEV__) {
            console.error(...args);
        }
    },
};