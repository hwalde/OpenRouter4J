package de.entwicklertraining.openrouter4j;

import com.sun.net.httpserver.HttpServer;
import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiClientSettings;
import de.entwicklertraining.api.base.ApiErrorContext;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the typed error-body accessors ({@link OpenRouterErrorMetadata}) on the
 * enriched status-code exceptions: fixture-level accessors incl. the swallow paths, and
 * an error thrown through a real {@link OpenRouterClient} against a recorded error body.
 */
class OpenRouterErrorMetadataTest {

    private static final String ERROR_BODY =
            "{\"error\":{\"message\":\"Rate limit exceeded\",\"code\":429,"
                    + "\"metadata\":{\"reason\":\"in_flight_budget\",\"retryable\":true,"
                    + "\"error_type\":\"rate_limit\",\"provider_code\":\"upstream_429\","
                    + "\"limit_source\":\"openrouter_in_flight_budget\"}}}";

    private static HttpServer server;
    private static HttpServer server418;
    private static String baseUrl;
    private static String baseUrl418;
    private static final java.util.Map<String, java.util.concurrent.atomic.AtomicInteger> hits =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status,
                                String body, Map<String, String> headers) throws java.io.IOException {
        headers.forEach((k, v) -> exchange.getResponseHeaders().add(k, v));
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String errorBody(int code, String reason, boolean retryable) {
        return "{\"error\":{\"message\":\"m" + code + "\",\"code\":" + code + ","
                + "\"metadata\":{\"reason\":\"" + reason + "\",\"retryable\":" + retryable + "}}}";
    }

    /** Dispatches per request input: "NNN" answers status NNN with a metadata body. */
    private static void dispatch(com.sun.net.httpserver.HttpExchange exchange) throws java.io.IOException {
        String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String input = request.replaceAll(".*\"input\":\"([^\"]*)\".*", "$1");
        hits.computeIfAbsent(input, k -> new java.util.concurrent.atomic.AtomicInteger()).incrementAndGet();
        switch (input) {
            case "400" -> respond(exchange, 400, errorBody(400, "bad_request", false), Map.of());
            case "403" -> respond(exchange, 403, errorBody(403, "forbidden", false), Map.of());
            case "404" -> respond(exchange, 404, errorBody(404, "not_found", false), Map.of());
            case "500", "retry-500" -> respond(exchange, 500, errorBody(500, "server", true), Map.of());
            case "503" -> respond(exchange, 503, errorBody(503, "unavailable", true), Map.of());
            case "504" -> respond(exchange, 504, errorBody(504, "gateway_timeout", false), Map.of());
            case "retry-404" -> respond(exchange, 404, errorBody(404, "not_found", false), Map.of());
            default -> respond(exchange, 429, ERROR_BODY,
                    Map.of("Retry-After", "30", "X-RateLimit-Remaining", "0"));
        }
    }

    @BeforeAll
    static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/embeddings", OpenRouterErrorMetadataTest::dispatch);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/api/v1";

        server418 = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server418.createContext("/api/v1/embeddings", exchange ->
                respond(exchange, 418, errorBody(418, "timeout", true), Map.of()));
        server418.start();
        baseUrl418 = "http://127.0.0.1:" + server418.getAddress().getPort() + "/api/v1";
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
        server418.stop(0);
    }

    private static ApiErrorContext context(String body, Map<String, String> headers) {
        return new ApiErrorContext(429, body, headers);
    }

    // ---------------------------------------------------------------- fixture accessors

    @Test
    @DisplayName("every metadata accessor reads its field from a recorded error body")
    void accessorsReadTheDocumentedMetadata() {
        OpenRouterRateLimitOrQuotaException ex = new OpenRouterRateLimitOrQuotaException(
                "HTTP 429 (Rate Limited): " + ERROR_BODY,
                context(ERROR_BODY, Map.of("Retry-After", "30")));

        assertEquals("in_flight_budget", ex.errorReason());
        assertEquals(Boolean.TRUE, ex.isRetryable());
        assertEquals("rate_limit", ex.errorType());
        assertEquals("upstream_429", ex.providerCode());
        assertEquals("openrouter_in_flight_budget", ex.limitSource());
        assertNotNull(ex.errorMetadata());
        assertEquals("in_flight_budget", ex.errorMetadata().getString("reason"));
        assertEquals("30", ex.retryAfter());
        assertEquals(30L, ex.retryAfterSeconds());
        assertEquals(429, ex.getApiErrorContext().statusCode());
    }

    @Test
    @DisplayName("malformed error bodies look empty: every accessor swallows to null")
    void swallowPaths() {
        // body that is not JSON at all
        OpenRouterNotFoundException notJson = new OpenRouterNotFoundException("m",
                context("<html>gateway</html>", Map.of()));
        assertNull(notJson.errorMetadata());
        assertNull(notJson.errorReason());
        assertNull(notJson.isRetryable());
        assertNull(notJson.errorType());
        assertNull(notJson.providerCode());
        assertNull(notJson.limitSource());

        // no error object / no metadata object / metadata of the wrong shape
        assertNull(new OpenRouterNotFoundException("m", context("{}", Map.of())).errorMetadata());
        assertNull(new OpenRouterNotFoundException("m",
                context("{\"error\":{\"message\":\"x\"}}", Map.of())).errorMetadata());
        assertNull(new OpenRouterNotFoundException("m",
                context("{\"error\":{\"metadata\":42}}", Map.of())).errorMetadata());
        assertNull(new OpenRouterNotFoundException("m",
                context("{\"error\":\"not-an-object\"}", Map.of())).errorMetadata());

        // fields of the wrong type are swallowed individually
        String typed = "{\"error\":{\"metadata\":{\"reason\":7,\"retryable\":\"yes\","
                + "\"error_type\":false,\"provider_code\":{},\"limit_source\":[],"
                + "\"extra\":\"kept\"}}}";
        OpenRouterServerErrorException wrongTypes = new OpenRouterServerErrorException("m",
                context(typed, Map.of()));
        assertNull(wrongTypes.errorReason());
        assertNull(wrongTypes.isRetryable());
        assertNull(wrongTypes.errorType());
        assertNull(wrongTypes.providerCode());
        assertNull(wrongTypes.limitSource());
        assertEquals("kept", wrongTypes.errorMetadata().getString("extra"),
                "the raw metadata object survives malformed sibling fields");

        // no context at all, and an empty body
        OpenRouterServerTimeoutException noContext = new OpenRouterServerTimeoutException("m");
        assertNull(noContext.errorMetadata());
        assertNull(noContext.isRetryable());
        assertNull(noContext.retryAfter());
        assertNull(noContext.retryAfterSeconds());
        assertNull(new OpenRouterServerTimeoutException("m", context(null, null)).errorMetadata());
    }

    @Test
    @DisplayName("Retry-After parses both documented forms and swallows junk")
    void retryAfterForms() {
        assertEquals(30L, new OpenRouterServiceUnavailableException("m",
                context(ERROR_BODY, Map.of("Retry-After", "30"))).retryAfterSeconds());
        String future = DateTimeFormatter.RFC_1123_DATE_TIME
                .format(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(120));
        Long dateForm = new OpenRouterServiceUnavailableException("m",
                context(ERROR_BODY, Map.of("retry-after", future))).retryAfterSeconds();
        assertNotNull(dateForm);
        assertTrue(dateForm > 60 && dateForm <= 120, "HTTP-date answered as seconds until (got " + dateForm + ")");
        assertNull(new OpenRouterServiceUnavailableException("m",
                context(ERROR_BODY, Map.of("Retry-After", "soon"))).retryAfterSeconds());
        assertEquals("soon", new OpenRouterServiceUnavailableException("m",
                context(ERROR_BODY, Map.of("Retry-After", "soon"))).retryAfter());
    }

    @Test
    @DisplayName("OpenRouterErrorMetadata.of() reads any thrown error's context")
    void staticOfCoversUnregisteredStatuses() {
        ApiErrorContext ctx = new ApiErrorContext(402, ERROR_BODY,
                Map.of("Retry-After", "15"));
        ApiClient.ApiClientException plain = new ApiClient.ApiClientException("Unexpected HTTP status 402", ctx);

        OpenRouterErrorMetadata view = OpenRouterErrorMetadata.of(plain);
        assertNotNull(view);
        assertEquals("openrouter_in_flight_budget", view.limitSource());
        assertEquals(Boolean.TRUE, view.isRetryable());
        assertEquals(15L, view.retryAfterSeconds());

        assertNotNull(OpenRouterErrorMetadata.of(
                new OpenRouterRateLimitOrQuotaException("m", ctx)));
        assertNull(OpenRouterErrorMetadata.of(new RuntimeException("no context")));
        assertNull(OpenRouterErrorMetadata.of(null));
    }

    // -------------------------------------------------------- thrown through the client

    @Test
    @DisplayName("an error thrown through the client arrives enriched with its metadata")
    void thrownThroughTheClient() {
        OpenRouterClient client = new OpenRouterClient(
                ApiClientSettings.builder().maxRetries(1).initialDelayMs(10).build(),
                null, baseUrl);

        OpenRouterRateLimitOrQuotaException ex = assertThrows(OpenRouterRateLimitOrQuotaException.class,
                () -> client.embeddings().model("test-model").input("hello").execute());

        // still catchable as the api-base supertype
        assertThrows(ApiClient.HTTP_429_RateLimitOrQuotaException.class,
                () -> { throw ex; });

        assertEquals("HTTP 429 (Rate Limited): " + ERROR_BODY, ex.getMessage(),
                "the composed message is unchanged");
        assertEquals("in_flight_budget", ex.errorReason());
        assertEquals(Boolean.TRUE, ex.isRetryable());
        assertEquals("openrouter_in_flight_budget", ex.limitSource());
        assertEquals("30", ex.retryAfter());
        assertEquals(30L, ex.retryAfterSeconds());
        assertEquals("0", ex.getApiErrorContext().header("X-RateLimit-Remaining"),
                "the error response headers ride on the error context");
    }

    @Test
    @DisplayName("an unregistered status arrives as ApiClientException but still readable")
    void unregisteredStatusReadableThroughOf() {
        OpenRouterClient client = new OpenRouterClient(
                ApiClientSettings.builder().maxRetries(1).initialDelayMs(10).build(),
                null, baseUrl418);

        ApiClient.ApiClientException ex = assertThrows(ApiClient.ApiClientException.class,
                () -> client.embeddings().model("test-model").input("hello").execute());

        OpenRouterErrorMetadata view = OpenRouterErrorMetadata.of(ex);
        assertNotNull(view);
        assertEquals("timeout", view.errorReason());
        assertEquals(Boolean.TRUE, view.isRetryable());
        assertEquals(418, view.getApiErrorContext().statusCode());
    }

    // ------------------------------------------------- every registered status typed

    private static void assertEnriched(String input, Class<? extends RuntimeException> expected,
                                       Class<?> apiBaseType, String messagePrefix, String reason) {
        OpenRouterClient client = new OpenRouterClient(
                ApiClientSettings.builder().maxRetries(1).initialDelayMs(10).build(),
                null, baseUrl);
        RuntimeException ex = assertThrows(expected,
                () -> client.embeddings().model("test-model").input(input).execute());
        assertInstanceOf(apiBaseType, ex, "still catchable as the api-base supertype");
        assertTrue(ex.getMessage().startsWith(messagePrefix + ": "),
                "the composed message is unchanged, got: " + ex.getMessage());
        assertInstanceOf(OpenRouterErrorMetadata.class, ex);
        assertEquals(reason, ((OpenRouterErrorMetadata) ex).errorReason());
    }

    @Test
    @DisplayName("every registered status arrives as its enriched type through the client")
    void everyRegisteredStatusArrivesEnrichedThroughTheClient() {
        assertEnriched("400", OpenRouterRequestRejectedException.class,
                ApiClient.HTTP_400_RequestRejectedException.class, "HTTP 400 (Bad Request)", "bad_request");
        assertEnriched("403", OpenRouterPermissionDeniedException.class,
                ApiClient.HTTP_403_PermissionDeniedException.class, "HTTP 403 (Forbidden)", "forbidden");
        assertEnriched("404", OpenRouterNotFoundException.class,
                ApiClient.HTTP_404_NotFoundException.class, "HTTP 404 (Not Found)", "not_found");
        assertEnriched("500", OpenRouterServerErrorException.class,
                ApiClient.HTTP_500_ServerErrorException.class, "HTTP 500 (Internal Server Error)", "server");
        assertEnriched("503", OpenRouterServiceUnavailableException.class,
                ApiClient.HTTP_503_ServerUnavailableException.class, "HTTP 503 (Service Unavailable)", "unavailable");
        assertEnriched("504", OpenRouterServerTimeoutException.class,
                ApiClient.HTTP_504_ServerTimeoutException.class, "HTTP 504 (Gateway Timeout)", "gateway_timeout");
    }

    @Test
    @DisplayName("the per-status retry flags are unchanged: 500 retries, 404 does not")
    void retryFlagsUnchanged() {
        OpenRouterClient client = new OpenRouterClient(
                ApiClientSettings.builder().maxRetries(2).initialDelayMs(5).build(), null, baseUrl);

        ApiClient.ApiTimeoutException exhausted = assertThrows(ApiClient.ApiTimeoutException.class,
                () -> client.embeddings().model("test-model").input("retry-500").executeWithRetry());
        assertEquals(2, hits.get("retry-500").get(), "500 is registered retryable - two attempts");
        assertInstanceOf(OpenRouterServerErrorException.class, exhausted.getCause(),
                "the last status exception is the cause of the retries-exhausted error");
        assertNotNull(OpenRouterErrorMetadata.of(exhausted),
                "the cause chain still resolves the error metadata");

        assertThrows(OpenRouterNotFoundException.class,
                () -> client.embeddings().model("test-model").input("retry-404").executeWithRetry());
        assertEquals(1, hits.get("retry-404").get(), "404 is registered non-retryable - one attempt");
    }

    // ------------------------------------------------------------ remaining contracts

    @Test
    @DisplayName("isRetryable() answers FALSE, not null, when the body says false")
    void retryableFalseIsNotConfusedWithUndocumented() {
        OpenRouterNotFoundException ex = new OpenRouterNotFoundException("m",
                context("{\"error\":{\"metadata\":{\"retryable\":false}}}", Map.of()));
        assertEquals(Boolean.FALSE, ex.isRetryable());
    }

    @Test
    @DisplayName("the parse edges: negative delta-seconds swallow, a past HTTP-date is 0")
    void parseEdgesOnTheErrorSide() {
        assertNull(new OpenRouterServiceUnavailableException("m",
                context(ERROR_BODY, Map.of("Retry-After", "-5"))).retryAfterSeconds());
        String past = DateTimeFormatter.RFC_1123_DATE_TIME
                .format(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(60));
        assertEquals(0L, new OpenRouterServiceUnavailableException("m",
                context(ERROR_BODY, Map.of("Retry-After", past))).retryAfterSeconds());
    }

    @Test
    @DisplayName("a context with null body and headers swallows every accessor to null")
    void nullContextPartsSwallow() {
        OpenRouterServerTimeoutException ex = new OpenRouterServerTimeoutException("m", context(null, null));
        assertNull(ex.errorMetadata());
        assertNull(ex.retryAfter());
        assertNull(ex.retryAfterSeconds());
    }

    @Test
    @DisplayName("a deeply nested hostile body looks empty - the swallow contract holds")
    void deeplyNestedHostileBodyLooksEmpty() {
        // properly nested objects past the parser's depth guard. The pinned org.json
        // answers JSONException ("depth too large") for this shape - a RuntimeException
        // the swallow has always covered; the StackOverflowError arm in parseBody is the
        // belt for parsers without that guard (older/newer org.json), not what this
        // fixture reaches today.
        int depth = 20_000;
        String hostile = "{\"a\":".repeat(depth) + "1" + "}".repeat(depth);
        OpenRouterNotFoundException ex = new OpenRouterNotFoundException("m", context(hostile, Map.of()));
        assertDoesNotThrow(() -> {
            assertNull(ex.errorMetadata());
            assertNull(ex.errorReason());
        });
    }

    @Test
    @DisplayName("of() resolves the context through the streaming/async wrapper layers")
    void ofResolvesWrappedStreamingErrors() {
        OpenRouterRateLimitOrQuotaException inner = new OpenRouterRateLimitOrQuotaException("m",
                context(ERROR_BODY, Map.of("Retry-After", "30")));

        OpenRouterErrorMetadata viaStreaming = OpenRouterErrorMetadata.of(
                new ApiClient.StreamingException("Streaming execution failed", inner));
        assertNotNull(viaStreaming);
        assertEquals("in_flight_budget", viaStreaming.errorReason());
        assertEquals(30L, viaStreaming.retryAfterSeconds());

        OpenRouterErrorMetadata viaCompletion = OpenRouterErrorMetadata.of(
                new java.util.concurrent.CompletionException(inner));
        assertNotNull(viaCompletion);
        assertEquals("openrouter_in_flight_budget", viaCompletion.limitSource());
    }
}
