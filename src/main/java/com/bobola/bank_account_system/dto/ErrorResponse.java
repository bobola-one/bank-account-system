package com.bobola.bank_account_system.dto;

import java.time.LocalDateTime;

/**
 * Standard shape for error responses returned by the API.
 * <p>
 * Every exception the API handles gets converted into one of these,
 * so clients (Postman, the Swing GUI, etc.) always know what fields
 * to expect on failure, regardless of what actually went wrong.
 *
 * @param timestamp when the error occurred
 * @param status    the HTTP status code, e.g. 404
 * @param error     short name of the error, e.g. "Not Found"
 * @param message   human-readable detail about what went wrong
 * @param path      the request path that caused the error
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}