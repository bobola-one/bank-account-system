package com.bobola.bank_account_system.gui;

import com.bobola.bank_account_system.dto.ErrorResponse;

/**
 * Thrown by {@link ApiClient} when the server responds with an error status.
 * <p>
 * Carries the full {@link ErrorResponse} so the GUI can display the
 * server's actual message rather than a generic failure notice.
 */
public class ApiException extends Exception {

    private final ErrorResponse errorResponse;

    /**
     * Constructs the exception from the server's structured error response.
     *
     * @param errorResponse the parsed error body returned by the API
     */
    public ApiException(ErrorResponse errorResponse) {
        super(errorResponse.message());
        this.errorResponse = errorResponse;
    }

    /**
     * Returns the full structured error response from the server.
     *
     * @return the original error response
     */
    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}