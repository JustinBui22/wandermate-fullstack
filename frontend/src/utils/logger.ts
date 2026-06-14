export const logger = {
    debug: (...args: unknown[]) => {
        if (__DEV__) {
            console.log(...args);
        }
    },

    error: (...args: unknown[]) => {
        if (__DEV__) {
            console.error(...args);
        }
    },
};