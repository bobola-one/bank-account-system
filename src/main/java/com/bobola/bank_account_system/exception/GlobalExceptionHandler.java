package com.bobola.bank_account_system.exception;

import com.bobola.bank_account_system.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Centralized exception handling for the REST API.
 * <p>
 * Instead of each controller catching exceptions individually, this class
 * intercepts exceptions thrown anywhere in the request-handling process
 * (controllers or the services they call) and converts them into consistent
 * {@link ErrorResponse} JSON bodies with the correct HTTP status code.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles the case where a requested account does not exist.
     *
     * @param ex      the thrown exception
     * @param request the original HTTP request, used to report the path
     * @return a 404 Not Found response with error details
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(
            AccountNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles the case where a withdrawal exceeds the account's balance.
     *
     * @param ex      the thrown exception
     * @param request the original HTTP request, used to report the path
     * @return a 409 Conflict response with error details
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(
            InsufficientFundsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handles invalid input, such as a negative or missing amount.
     *
     * @param ex      the thrown exception
     * @param request the original HTTP request, used to report the path
     * @return a 400 Bad Request response with error details
     */
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmount(
            InvalidAmountException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Fallback handler for any exception not specifically handled above.
     * <p>
     * Prevents unexpected errors from leaking raw stack traces or Spring's
     * default generic error page to API clients.
     *
     * @param ex      the thrown exception
     * @param request the original HTTP request, used to report the path
     * @return a 500 Internal Server Error response with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    /**
     * Builds a consistent {@link ErrorResponse} wrapped in a {@link ResponseEntity}
     * with the given status.
     *
     * @param status  the HTTP status to return
     * @param message the error message to include
     * @param request the original HTTP request, used to report the path
     * @return the assembled response entity
     */
    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}