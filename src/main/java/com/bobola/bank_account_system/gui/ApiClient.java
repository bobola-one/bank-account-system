package com.bobola.bank_account_system.gui;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.bobola.bank_account_system.dto.AccountResponse;
import com.bobola.bank_account_system.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Thin HTTP client that talks to the Bank Account Management System's REST
 * API on behalf of the Swing GUI.
 * <p>
 * This class knows nothing about Swing; it only knows how to send HTTP
 * requests and convert JSON responses into the same DTOs the backend uses.
 * The GUI layer calls these methods and never deals with HTTP or JSON directly.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/accounts";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the client with a reusable HTTP client and JSON mapper.
     */
    public ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Creates a new account.
     *
     * @param accountHolderName name of the account holder
     * @param initialBalance    starting balance
     * @return the newly created account
     * @throws ApiException if the server returns an error response
     * @throws IOException  if the request fails to send or the response fails to read
     * @throws InterruptedException if the request is interrupted
     */
    public AccountResponse createAccount(String accountHolderName, BigDecimal initialBalance)
            throws IOException, InterruptedException, ApiException {
        String body = objectMapper.writeValueAsString(
                new CreateAccountPayload(accountHolderName, initialBalance));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseOrThrow(response, AccountResponse.class);
    }

    /**
     * Retrieves a single account by id.
     *
     * @param id the account id
     * @return the matching account
     * @throws ApiException if the server returns an error response (e.g. not found)
     * @throws IOException  if the request fails to send or the response fails to read
     * @throws InterruptedException if the request is interrupted
     */
    public AccountResponse getAccount(Long id) throws IOException, InterruptedException , ApiException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseOrThrow(response, AccountResponse.class);
    }

    /**
     * Retrieves every account in the system.
     *
     * @return a list of all accounts
     * @throws ApiException if the server returns an error response
     * @throws IOException  if the request fails to send or the response fails to read
     * @throws InterruptedException if the request is interrupted
     */
    public List<AccountResponse> getAllAccounts() throws IOException, InterruptedException, ApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new ApiException(parseError(response));
        }
        return objectMapper.readValue(response.body(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, AccountResponse.class));
    }

    /**
     * Deposits an amount into an account.
     *
     * @param accountId the account to deposit into
     * @param amount    the amount to deposit
     * @return the updated account
     * @throws ApiException if the server returns an error response
     * @throws IOException  if the request fails to send or the response fails to read
     * @throws InterruptedException if the request is interrupted
     */
    public AccountResponse deposit(Long accountId, BigDecimal amount)
            throws IOException, InterruptedException, ApiException {
        return postAmount(accountId, amount, "deposit");
    }

    /**
     * Withdraws an amount from an account.
     *
     * @param accountId the account to withdraw from
     * @param amount    the amount to withdraw
     * @return the updated account
     * @throws ApiException if the server returns an error response (e.g. insufficient funds)
     * @throws IOException  if the request fails to send or the response fails to read
     * @throws InterruptedException if the request is interrupted
     */
    public AccountResponse withdraw(Long accountId, BigDecimal amount)
            throws IOException, InterruptedException, ApiException {
        return postAmount(accountId, amount, "withdraw");
    }

    /**
     * Shared implementation for deposit and withdraw, since both send the
     * same shape of request to a differently-named sub-path.
     *
     * @param accountId the account id
     * @param amount    the amount to send
     * @param action    either "deposit" or "withdraw"
     * @return the updated account
     */
    private AccountResponse postAmount(Long accountId, BigDecimal amount, String action)
            throws IOException, InterruptedException , ApiException{
        String body = objectMapper.writeValueAsString(new AmountPayload(amount));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + accountId + "/" + action))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseOrThrow(response, AccountResponse.class);
    }

    /**
     * Parses a successful response body into the given type, or throws
     * an {@link ApiException} if the server responded with an error status.
     *
     * @param response the raw HTTP response
     * @param type     the class to parse the body into
     * @param <T>      the type of the parsed result
     * @return the parsed object
     * @throws ApiException if the status code indicates an error
     */
    private <T> T parseOrThrow(HttpResponse<String> response, Class<T> type) throws IOException, ApiException {
        if (response.statusCode() >= 400) {
            throw new ApiException(parseError(response));
        }
        return objectMapper.readValue(response.body(), type);
    }

    /**
     * Attempts to parse the server's structured error body; falls back to
     * a generic message if parsing fails for any reason.
     *
     * @param response the raw HTTP response
     * @return the parsed error response, or a generic fallback
     */
    private ErrorResponse parseError(HttpResponse<String> response) {
        try {
            return objectMapper.readValue(response.body(), ErrorResponse.class);
        } catch (IOException e) {
            return new ErrorResponse(null, response.statusCode(), "Error", response.body(), null);
        }
    }

    /**
     * Request payload for account creation, matching the shape expected
     * by {@code AccountController.CreateAccountRequest}.
     *
     * @param accountHolderName name of the account holder
     * @param initialBalance    starting balance
     */
    private record CreateAccountPayload(String accountHolderName, BigDecimal initialBalance) {
    }

    /**
     * Request payload for deposit/withdraw, matching the shape expected
     * by {@code AccountController.AmountRequest}.
     *
     * @param amount the amount to send
     */
    private record AmountPayload(BigDecimal amount) {
    }
}