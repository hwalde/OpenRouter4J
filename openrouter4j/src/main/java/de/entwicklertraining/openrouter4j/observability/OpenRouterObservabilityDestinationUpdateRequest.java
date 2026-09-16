package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to update an observability destination:
 * PATCH https://openrouter.ai/api/v1/observability/destinations/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Only explicitly configured fields are sent; every field is optional. Masked
 * config values are ignored by the API; unset config fields keep their current
 * value. The {@code regions} field cannot be cleared.
 */
public final class OpenRouterObservabilityDestinationUpdateRequest extends OpenRouterRequest<OpenRouterObservabilityDestinationUpdateResponse> {

    private final OpenRouterClient client;
    private final String id;
    private final String type;
    private final String name;
    private final JSONObject config;
    private final JSONObject configOption;
    private final Boolean enabled;
    private final List<String> regions;
    private final Double samplingRate;
    private final Boolean privacyMode;
    private final JSONObject filterRules;
    private final List<String> apiKeyHashes;
    private final Boolean broadcastGenerationCost;
    private final Boolean broadcastGenerationIdentity;
    private final Boolean broadcastGenerationRequestContext;

    private OpenRouterObservabilityDestinationUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.config = builder.config;
        this.configOption = builder.configOption;
        this.enabled = builder.enabled;
        this.regions = builder.regions;
        this.samplingRate = builder.samplingRate;
        this.privacyMode = builder.privacyMode;
        this.filterRules = builder.filterRules;
        this.apiKeyHashes = builder.apiKeyHashes;
        this.broadcastGenerationCost = builder.broadcastGenerationCost;
        this.broadcastGenerationIdentity = builder.broadcastGenerationIdentity;
        this.broadcastGenerationRequestContext = builder.broadcastGenerationRequestContext;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/observability/destinations/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "";
    }

    @Override
    public String getHttpMethod() {
        return "PATCH";
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
        if (type != null) {
            body.put("type", type);
        }
        if (name != null) {
            body.put("name", name);
        }
        if (config != null) {
            body.put("config", config);
        }
        if (configOption != null) {
            body.put("None", configOption);
        }
        if (enabled != null) {
            body.put("enabled", enabled);
        }
        if (regions != null) {
            body.put("regions", new JSONArray(regions));
        }
        if (samplingRate != null) {
            body.put("sampling_rate", samplingRate);
        }
        if (privacyMode != null) {
            body.put("privacy_mode", privacyMode);
        }
        if (filterRules != null) {
            body.put("filter_rules", filterRules);
        }
        if (apiKeyHashes != null) {
            body.put("api_key_hashes", new JSONArray(apiKeyHashes));
        }
        if (broadcastGenerationCost != null) {
            body.put("broadcast_generation_cost", broadcastGenerationCost);
        }
        if (broadcastGenerationIdentity != null) {
            body.put("broadcast_generation_identity", broadcastGenerationIdentity);
        }
        if (broadcastGenerationRequestContext != null) {
            body.put("broadcast_generation_request_context", broadcastGenerationRequestContext);
        }
        return body.toString();
    }

    @Override
    public OpenRouterObservabilityDestinationUpdateResponse createResponse(String responseBody) {
        return new OpenRouterObservabilityDestinationUpdateResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterObservabilityDestinationUpdateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterObservabilityDestinationUpdateRequest> {

        private final OpenRouterClient client;
    private final String id;
    private String type;
    private String name;
    private JSONObject config;
    private JSONObject configOption;
    private Boolean enabled;
    private List<String> regions;
    private Double samplingRate;
    private Boolean privacyMode;
    private JSONObject filterRules;
    private List<String> apiKeyHashes;
    private Boolean broadcastGenerationCost;
    private Boolean broadcastGenerationIdentity;
    private Boolean broadcastGenerationRequestContext;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param id the id (UUID) of the observability destination
     */
    public Builder(OpenRouterClient client, String id) {
        super(client);
        this.client = client;
        this.id = id;
    }

    /**
 * Sets the body field {@code type} (optional on update) - changing the
 * destination type; the API validates the config against it.
 */
    public Builder type(String type) {
        this.type = type;
        return this;
    }
    /**
 * Sets the body field {@code name} (optional) - new human-readable name for
 * the destination.
 */
    public Builder name(String name) {
        this.name = name;
        return this;
    }
    /**
 * Sets the body field {@code config} (optional) - destination-specific
 * configuration fields to update as a raw JSON object; masked values are
 * ignored, unset fields keep their current value.
 */
    public Builder config(JSONObject config) {
        this.config = config;
        return this;
    }

    /**
     * Puts one key-value pair into the body field {@code config} (creating the
     * object lazily) - the escape hatch for destination-type-specific
     * configuration fields (e.g. {@code baseUrl}, {@code publicKey},
     * {@code secretKey} for langfuse); values are stored verbatim.
     *
     * @param key the config field name
     * @param value the config field value
     * @return this builder
     */
    public Builder configOption(String key, Object value) {
        if (this.config == null) {
            this.config = new JSONObject();
        }
        this.config.put(key, value);
        return this;
    }
    /**
 *
 */
    public Builder configOption(JSONObject configOption) {
        this.configOption = configOption;
        return this;
    }
    /**
 * Sets the body field {@code enabled} (optional) - whether the destination is
 * enabled.
 */
    public Builder enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    /**
 * Sets the body field {@code regions} (optional) - data regions this
 * destination applies to; cannot be cleared once set.
 */
    public Builder regions(List<String> regions) {
        this.regions = regions;
        return this;
    }
    /**
 * Sets the body field {@code sampling_rate} (optional) - sampling rate between
 * 0.0001 and 1 (1 = 100%).
 */
    public Builder samplingRate(Double samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }
    /**
 * Sets the body field {@code privacy_mode} (optional) - whether request/response
 * bodies are not forwarded, only metadata.
 */
    public Builder privacyMode(Boolean privacyMode) {
        this.privacyMode = privacyMode;
        return this;
    }
    /**
 * Sets the body field {@code filter_rules} (optional) - structured filter rules
 * controlling which events are forwarded; an explicit clearing of the rules is
 * not offered (an unset key keeps the current value).
 */
    public Builder filterRules(JSONObject filterRules) {
        this.filterRules = filterRules;
        return this;
    }
    /**
 * Sets the body field {@code api_key_hashes} (optional) - OpenRouter API key
 * hashes whose traffic is forwarded (at least one when set; omitting keeps the
 * current value).
 */
    public Builder apiKeyHashes(List<String> apiKeyHashes) {
        this.apiKeyHashes = apiKeyHashes;
        return this;
    }
    /**
 * Sets the body field {@code broadcast_generation_cost} (optional) - whether
 * cost and billing generation metadata are included.
 */
    public Builder broadcastGenerationCost(Boolean broadcastGenerationCost) {
        this.broadcastGenerationCost = broadcastGenerationCost;
        return this;
    }
    /**
 * Sets the body field {@code broadcast_generation_identity} (optional) -
 * whether identity generation metadata is included.
 */
    public Builder broadcastGenerationIdentity(Boolean broadcastGenerationIdentity) {
        this.broadcastGenerationIdentity = broadcastGenerationIdentity;
        return this;
    }
    /**
 * Sets the body field {@code broadcast_generation_request_context} (optional)
 * - whether request-context generation metadata is included.
 */
    public Builder broadcastGenerationRequestContext(Boolean broadcastGenerationRequestContext) {
        this.broadcastGenerationRequestContext = broadcastGenerationRequestContext;
        return this;
    }
        @Override
        public OpenRouterObservabilityDestinationUpdateRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
            return new OpenRouterObservabilityDestinationUpdateRequest(this);
        }


    @Override
    public OpenRouterObservabilityDestinationUpdateResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterObservabilityDestinationUpdateResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterObservabilityDestinationUpdateResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
