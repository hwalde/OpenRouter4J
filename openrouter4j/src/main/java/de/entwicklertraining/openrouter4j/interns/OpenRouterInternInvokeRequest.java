package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to start an intern run without waiting for it:
 * POST https://openrouter.ai/api/v1/interns/{internId}/invoke
 * (schema {@code InternInvokeRequest}).
 *
 * <p>Body: {@code input} (required, 1-32,000 characters; validated loudly
 * here) plus the optional {@code session_id} (1-256 characters). Send the
 * {@code session_id} from an earlier 202 to continue that conversation;
 * omit it to start a new session.
 *
 * <p>Answers <b>202</b> with {@code session_id} and {@code status}:
 * {@code started} (a new run was started) or {@code steered} (the session
 * already had a run going and the prompt was delivered into it). The run
 * continues on the intern after the response - progress never comes back on
 * this request; the intern reports through its own tools (e.g. Slack). Runs
 * started here self-drive: the intern consents to its own tool approvals and
 * answers its own questions; a run ends when the intern finishes or after
 * its execution deadline (1 hour by default).
 *
 * <p>Documented traps: a {@code session_id} is accepted only from the caller
 * it was issued to, on the same intern - any other is refused with
 * {@code 404} ({@code error.metadata.reason: "not_found"}), so session ids
 * from Slack or from the chat endpoint cannot be replayed here;
 * {@code 409 intern_not_ready} / {@code busy} (a {@code busy} refusal carries
 * {@code Retry-After} - note that response headers are not accessible yet,
 * see the api-base response-headers finding), {@code 413 payload_too_large}
 * (body cap 1 MiB), {@code 429 rate_limited} and {@code 503 busy} (both
 * carry {@code Retry-After}), {@code 504 timeout}; every {@code /interns}
 * path answers {@code 404} for keys outside the interns programme.
 */
public final class OpenRouterInternInvokeRequest
        extends OpenRouterRequest<OpenRouterInternInvokeResponse> {

    private final OpenRouterClient client;
    private final String internId;
    private final String input;
    private final String sessionId;

    private OpenRouterInternInvokeRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.internId = builder.internId;
        this.input = builder.input;
        this.sessionId = builder.sessionId;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String internId() {
        return URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    /**
     * @return the required prompt text (1-32,000 characters)
     */
    public String input() {
        return input;
    }

    /**
     * @return the session id, or {@code null} when unset (a new session is
     *         started)
     */
    public String sessionId() {
        return sessionId;
    }

    @Override
    public String getRelativeUrl() {
        return "/interns/"
                + URLEncoder.encode(internId, StandardCharsets.UTF_8)
                + "/invoke";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * The JSON body: {@code input} always, {@code session_id} only when set.
     *
     * @return the JSON body string
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("input", input);
        if (sessionId != null) {
            body.put("session_id", sessionId);
        }
        return body.toString();
    }

    @Override
    public OpenRouterInternInvokeResponse createResponse(String responseBody) {
        return new OpenRouterInternInvokeResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterInternInvokeRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternInvokeRequest> {

        private final OpenRouterClient client;
        private final String internId;
        private final String input;
        private String sessionId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param internId the id (UUID) of the intern
         * @param input the prompt text (1-32,000 characters; validated
         *              loudly)
         */
        public Builder(OpenRouterClient client, String internId, String input) {
            super(client);
            if (input == null || input.isEmpty()) {
                throw new IllegalArgumentException("input must not be null or empty");
            }
            if (input.length() > 32_000) {
                throw new IllegalArgumentException(
                        "input must be at most 32,000 characters (was " + input.length() + ")");
            }
            this.client = client;
            this.internId = internId;
            this.input = input;
        }

        /**
         * Sets the JSON field {@code session_id} - the session to continue,
         * as returned by an earlier invoke answer. Omit it (or pass
         * {@code null}) to start a new session. Trap: the id is accepted
         * only from the caller it was issued to, on the same intern - any
         * other is refused with {@code 404}, so a mistyped id does not fork
         * the conversation the way the chat endpoint's {@code session_id}
         * does.
         *
         * @param sessionId the session id (1-256 characters; {@code null}
         *                  unsets it)
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            if (sessionId != null && (sessionId.isEmpty() || sessionId.length() > 256)) {
                throw new IllegalArgumentException("sessionId must be 1 to 256 characters");
            }
            this.sessionId = sessionId;
            return this;
        }

        @Override
        public OpenRouterInternInvokeRequest build() {
            return new OpenRouterInternInvokeRequest(this);
        }

        @Override
        public OpenRouterInternInvokeResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternInvokeResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternInvokeResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
