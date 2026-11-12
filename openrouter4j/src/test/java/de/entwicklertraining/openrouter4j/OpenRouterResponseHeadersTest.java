package de.entwicklertraining.openrouter4j;

import com.sun.net.httpserver.HttpServer;
import de.entwicklertraining.api.base.ApiClientSettings;
import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
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
 * Tests for the response-header accessors on {@link OpenRouterResponse}:
 * {@code header()}, {@code generationIdHeader()}, {@code retryAfter()} /
 * {@code retryAfterSeconds()} - fixture-level, plus the synthesized response of the
 * streaming tool-call loop carrying the headers of its last HTTP turn.
 */
class OpenRouterResponseHeadersTest {

    private static OpenRouterChatCompletionRequest request() {
        OpenRouterClient client = new OpenRouterClient(
                ApiClientSettings.builder().maxRetries(1).build(), null, "http://127.0.0.1:1/api/v1");
        return client.chat().completion()
                .model("test-model")
                .addMessage("user", "hi")
                .build();
    }

    private static OpenRouterChatCompletionResponse response() {
        return new OpenRouterChatCompletionResponse(
                new JSONObject("{\"id\":\"gen-1\",\"choices\":[]}"), request());
    }

    @Test
    @DisplayName("every header accessor reads the recorded response headers")
    void headerAccessors() {
        OpenRouterChatCompletionResponse resp = response();
        resp.setResponseHeaders(Map.of(
                "X-Generation-Id", "gen_123",
                "Retry-After", "7"));

        assertEquals("gen_123", resp.header("X-Generation-Id"));
        assertEquals("gen_123", resp.header("x-generation-id"), "lookup is case-insensitive");
        assertEquals("gen_123", resp.generationIdHeader());
        assertEquals("7", resp.retryAfter());
        assertEquals(7L, resp.retryAfterSeconds());
    }

    @Test
    @DisplayName("absent and unparsable headers swallow to null")
    void swallowPaths() {
        OpenRouterChatCompletionResponse empty = response();
        assertNull(empty.header("X-Generation-Id"));
        assertNull(empty.generationIdHeader());
        assertNull(empty.retryAfter());
        assertNull(empty.retryAfterSeconds());
        assertNull(empty.header(null));

        OpenRouterChatCompletionResponse junk = response();
        junk.setResponseHeaders(Map.of("Retry-After", "soon"));
        assertNull(junk.retryAfterSeconds());
        assertEquals("soon", junk.retryAfter());

        junk.setResponseHeaders(null);
        assertNull(junk.retryAfter(), "a null header map records no headers");

        OpenRouterChatCompletionResponse dateForm = response();
        String future = DateTimeFormatter.RFC_1123_DATE_TIME
                .format(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(90));
        dateForm.setResponseHeaders(Map.of("Retry-After", future));
        Long seconds = dateForm.retryAfterSeconds();
        assertNotNull(seconds);
        assertTrue(seconds > 30 && seconds <= 90, "HTTP-date answered as seconds until (got " + seconds + ")");

        OpenRouterChatCompletionResponse edges = response();
        edges.setResponseHeaders(Map.of("Retry-After", "-5"));
        assertNull(edges.retryAfterSeconds(), "negative delta-seconds swallow");
        String past = DateTimeFormatter.RFC_1123_DATE_TIME
                .format(ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(60));
        edges.setResponseHeaders(Map.of("Retry-After", past));
        assertEquals(0L, edges.retryAfterSeconds(), "an HTTP-date in the past answers 0");
    }

    // ------------------------------------------- streaming tool-call loop (synthesized)

    private static HttpServer server;
    private static String baseUrl;
    private static final java.util.concurrent.atomic.AtomicInteger streamTurns =
            new java.util.concurrent.atomic.AtomicInteger();

    @BeforeAll
    static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/chat/completions", exchange -> {
            int turn = streamTurns.incrementAndGet();
            String body;
            if (turn == 1) {
                // first turn: a tool call - the loop will execute the tool and turn again
                exchange.getResponseHeaders().add("X-Generation-Id", "gen_turn1");
                body = "data: {\"id\":\"gen-t1\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\","
                        + "\"tool_calls\":[{\"index\":0,\"id\":\"call_1\",\"type\":\"function\","
                        + "\"function\":{\"name\":\"echo\",\"arguments\":\"{\\\"text\\\":\\\"hi\\\"}\"}}]}}]}\n\n"
                        + "data: {\"id\":\"gen-t1\",\"choices\":[{\"index\":0,\"delta\":{},"
                        + "\"finish_reason\":\"tool_calls\"}]}\n\n"
                        + "data: [DONE]\n\n";
            } else {
                // second turn: plain content
                exchange.getResponseHeaders().add("X-Generation-Id", "gen_turn2");
                body = "data: {\"id\":\"gen-t2\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\","
                        + "\"content\":\"done\"}}]}\n\n"
                        + "data: [DONE]\n\n";
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/api/v1";
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    @DisplayName("the synthesized streaming response carries the LAST turn's headers")
    void streamingSyntheticResponseCarriesHeaders() {
        OpenRouterClient client = new OpenRouterClient(
                ApiClientSettings.builder().maxRetries(1).build(), null, baseUrl);

        java.util.concurrent.atomic.AtomicBoolean toolExecuted =
                new java.util.concurrent.atomic.AtomicBoolean(false);
        OpenRouterToolDefinition echoTool = OpenRouterToolDefinition.builder("echo")
                .description("Echo the given text back")
                .parameter("text", OpenRouterJsonSchema.stringSchema("Text to echo"), true)
                .callback(ctx -> {
                    toolExecuted.set(true);
                    return OpenRouterToolResult.of(new JSONObject().put("echo", "x"));
                })
                .build();

        OpenRouterChatCompletionResponse resp = client.chat().completion()
                .model("test-model")
                .addMessage("user", "hi")
                .addTool(echoTool)
                .stream(new StreamingResponseHandler<String>() {
                    @Override
                    public void onData(String chunk) {
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                    }
                })
                .execute();

        assertTrue(toolExecuted.get(), "the tool-call loop ran two turns");
        assertEquals(2, streamTurns.get(), "the loop issued two HTTP turns");
        assertEquals("gen_turn2", resp.generationIdHeader(),
                "the synthesized response carries the last turn's headers");
        assertNotEquals("gen_turn1", resp.generationIdHeader(),
                "headers of earlier turns do not win");
        assertEquals("gen_turn2", resp.header("x-generation-id"));
    }
}
