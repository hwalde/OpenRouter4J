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

    @BeforeAll
    static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/embeddings", exchange -> {
            exchange.getResponseHeaders().add("Retry-After", "30");
            exchange.getResponseHeaders().add("X-RateLimit-Remaining", "0");
            byte[] body = ERROR_BODY.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(429, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/api/v1";

        server418 = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server418.createContext("/api/v1/embeddings", exchange -> {
            byte[] body = ("{\"error\":{\"message\":\"short and stout\",\"code\":418,"
                    + "\"metadata\":{\"reason\":\"timeout\",\"retryable\":true}}}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(418, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
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
}
