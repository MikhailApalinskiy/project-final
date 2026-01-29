package com.finalProjectLedZeppelin.common.error;

import java.time.Instant;

/**
 * Standard API error response.
 * <p>
 * Represents a structured error returned by the application
 * when a request cannot be processed successfully.
 *
 * @param timestamp time when the error occurred
 * @param status    HTTP status code
 * @param error     HTTP status reason phrase
 * @param message   detailed error message
 * @param path      request path that caused the error
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
