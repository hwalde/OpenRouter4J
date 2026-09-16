package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to unassign API keys from a guardrail:
 * POST https://openrouter.ai/api/v1/guardrails/{id}/assignments/keys/remove
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailKeysUnassignRequest extends OpenRouterRequest<OpenRouterGuardrailKeysUnassignResponse> {

    private final OpenRouterClient client;
    private final String id;
    private final List<String> keyHashes;

    private OpenRouterGuardrailKeysUnassignRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.keyHashes = builder.keyHashes;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/guardrails/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "/assignments/keys/remove";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * Builds the JSON body with only the explicitly configured fields
     * (an unset option never appears in the JSON).
     *
     * @return the JSON body
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("key_hashes", new JSONArray(keyHashes));
        return body.toString();
    }

    @Override
    public OpenRouterGuardrailKeysUnassignResponse createResponse(String responseBody) {
        return new OpenRouterGuardrailKeysUnassignResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailKeysUnassignRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailKeysUnassignRequest> {

        private final OpenRouterClient client;
    private final String id;
    private List<String> keyHashes;


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
 * Adds an API key hash to the body field {@code key_hashes} (required, at
 * least one) - the hash of an API key to release from this guardrail.
 */
    public Builder keyHashes(List<String> keyHashes) {
        this.keyHashes = keyHashes;
        return this;
    }
    /**
     * Adds one API key hash to the list.
     *
     * <p>Adds one API key hash to the body field {@code key_hashes} (required, at
least one) - the hash of an API key to release from this guardrail.
     *
     * @param addKeyHash the API key hash to add
     * @return this builder
     */
    public Builder addKeyHash(String addKeyHash) {
        if (this.keyHashes == null) {
            this.keyHashes = new ArrayList<>();
        }
        this.keyHashes.add(addKeyHash);
        return this;
    }

        @Override
        public OpenRouterGuardrailKeysUnassignRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
        if (keyHashes == null || keyHashes.isEmpty()) {
            throw new IllegalStateException("keyHashes must contain at least one API key hash");
        }
            return new OpenRouterGuardrailKeysUnassignRequest(this);
        }


    @Override
    public OpenRouterGuardrailKeysUnassignResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterGuardrailKeysUnassignResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterGuardrailKeysUnassignResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
