package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to create an observability destination:
 * POST https://openrouter.ai/api/v1/observability/destinations
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>The {@code config} object is destination-type-specific (e.g. {@code baseUrl},
 * {@code publicKey}, {@code secretKey} for langfuse) and validated server-side;
 * build it with {@link Builder#config(JSONObject)} or field by field with
 * {@link Builder#configOption(String, Object)}.
 */
public final class OpenRouterObservabilityDestinationCreateRequest extends OpenRouterRequest<OpenRouterObservabilityDestinationCreateResponse> {

    private final OpenRouterClient client;
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
    private final String workspaceId;
    private final Boolean broadcastGenerationCost;
    private final Boolean broadcastGenerationIdentity;
    private final Boolean broadcastGenerationRequestContext;

    private OpenRouterObservabilityDestinationCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
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
        this.workspaceId = builder.workspaceId;
        this.broadcastGenerationCost = builder.broadcastGenerationCost;
        this.broadcastGenerationIdentity = builder.broadcastGenerationIdentity;
        this.broadcastGenerationRequestContext = builder.broadcastGenerationRequestContext;
    }


    @Override
    public String getRelativeUrl() {
        return "/observability/destinations";
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
        body.put("type", type);
        body.put("name", name);
        body.put("config", config);
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
        if (workspaceId != null) {
            body.put("workspace_id", workspaceId);
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
    public OpenRouterObservabilityDestinationCreateResponse createResponse(String responseBody) {
        return new OpenRouterObservabilityDestinationCreateResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterObservabilityDestinationCreateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterObservabilityDestinationCreateRequest> {

        private final OpenRouterClient client;
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
    private String workspaceId;
    private Boolean broadcastGenerationCost;
    private Boolean broadcastGenerationIdentity;
    private Boolean broadcastGenerationRequestContext;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     */
    public Builder(OpenRouterClient client) {
        super(client);
        this.client = client;
    }

    /**
 * Sets the body field {@code type} (required) - the destination type; only
 * stable types are accepted ({@code arize}, {@code braintrust}, {@code clickhouse},
 * {@code datadog}, {@code grafana}, {@code langfuse}, {@code langsmith},
 * {@code newrelic}, {@code opik}, {@code otel-collector}, {@code posthog},
 * {@code ramp}, {@code s3}, {@code sentry}, {@code snowflake}, {@code weave},
 * {@code webhook}).
 */
    public Builder type(String type) {
        this.type = type;
        return this;
    }
    /**
 * Sets the body field {@code name} (required) - human-readable name for the
 * destination.
 */
    public Builder name(String name) {
        this.name = name;
        return this;
    }
    /**
 * Sets the body field {@code config} (required) - the destination-type-specific
 * configuration as a raw JSON object; the shape depends on {@code type} and is
 * validated server-side. Credentials inside (e.g. {@code secretKey}) are treated
 * as secrets by the destination service and masked in later responses.
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
 * enabled immediately (API default {@code true}).
 */
    public Builder enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    /**
 * Sets the body field {@code regions} (optional) - data regions this
 * destination applies to ({@code global}, {@code europe}, {@code us}; {@code eu}
 * is accepted as an alias and normalized); must be non-empty when set. Omitting
 * the field defaults to {@code [global]}.
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
 * bodies are not forwarded, only metadata (API default {@code false}).
 */
    public Builder privacyMode(Boolean privacyMode) {
        this.privacyMode = privacyMode;
        return this;
    }
    /**
 * Sets the body field {@code filter_rules} (optional) - structured filter rules
 * controlling which events are forwarded, as a raw JSON object ({@code enabled},
 * {@code groups}).
 */
    public Builder filterRules(JSONObject filterRules) {
        this.filterRules = filterRules;
        return this;
    }
    /**
 * Sets the body field {@code api_key_hashes} (optional) - OpenRouter API key
 * hashes whose traffic is forwarded (at least one when set; unset/null means all
 * keys).
 */
    public Builder apiKeyHashes(List<String> apiKeyHashes) {
        this.apiKeyHashes = apiKeyHashes;
        return this;
    }
    /**
 * Sets the body field {@code workspace_id} (optional) - the workspace the
 * destination belongs to; defaults to the authenticated entity's default
 * workspace.
 */
    public Builder workspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
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
        public OpenRouterObservabilityDestinationCreateRequest build() {
        if (type == null || type.isEmpty()) {
            throw new IllegalStateException("type is required for observability destination creation");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("name is required for observability destination creation");
        }
        if (config == null) {
            throw new IllegalStateException("config is required for observability destination creation");
        }
            return new OpenRouterObservabilityDestinationCreateRequest(this);
        }


    @Override
    public OpenRouterObservabilityDestinationCreateResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterObservabilityDestinationCreateResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterObservabilityDestinationCreateResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
