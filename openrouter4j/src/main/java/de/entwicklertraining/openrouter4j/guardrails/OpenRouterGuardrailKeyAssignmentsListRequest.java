package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the key assignments of one guardrail:
 * GET https://openrouter.ai/api/v1/guardrails/{id}/assignments/keys
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailKeyAssignmentsListRequest extends OpenRouterRequest<OpenRouterGuardrailKeyAssignmentsListResponse<OpenRouterGuardrailKeyAssignmentsListRequest>> {

    private final OpenRouterClient client;
    private final String id;
    private final Integer offset;
    private final Integer limit;

    private OpenRouterGuardrailKeyAssignmentsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.offset = builder.offset;
        this.limit = builder.limit;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/guardrails/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "/assignments/keys");
        boolean first = true;
        if (offset != null) {
            sb.append(first ? '?' : '&').append("offset=").append(offset);
            first = false;
        }
        if (limit != null) {
            sb.append(first ? '?' : '&').append("limit=").append(limit);
            first = false;
        }
        return sb.toString();
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    /**
     * GET requests carry no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }

    @Override
    public OpenRouterGuardrailKeyAssignmentsListResponse<OpenRouterGuardrailKeyAssignmentsListRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailKeyAssignmentsListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailKeyAssignmentsListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailKeyAssignmentsListRequest> {

        private final OpenRouterClient client;
    private final String id;
    private Integer offset;
    private Integer limit;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param id the id (UUID) of the guardrail
     */
    public Builder(OpenRouterClient client, String id) {
        super(client);
        this.client = client;
        this.id = id;
    }

    /**
     * Sets the query key {@code offset}.
     *
     * @param offset the value for the query key
     * @return this builder
     */
    public Builder offset(Integer offset) {
        this.offset = offset;
        return this;
    }
    /**
     * Sets the query key {@code limit}.
     *
     * @param limit the value for the query key
     * @return this builder
     */
    public Builder limit(Integer limit) {
        this.limit = limit;
        return this;
    }
        @Override
        public OpenRouterGuardrailKeyAssignmentsListRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
            return new OpenRouterGuardrailKeyAssignmentsListRequest(this);
        }


    @Override
    public OpenRouterGuardrailKeyAssignmentsListResponse<OpenRouterGuardrailKeyAssignmentsListRequest> execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterGuardrailKeyAssignmentsListResponse<OpenRouterGuardrailKeyAssignmentsListRequest> executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterGuardrailKeyAssignmentsListResponse<OpenRouterGuardrailKeyAssignmentsListRequest> executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
