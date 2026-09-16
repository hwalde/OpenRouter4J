package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to create a BYOK provider credential:
 * POST https://openrouter.ai/api/v1/byok
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Secrets rule: the {@code key} field is a provider credential - it is
 * encrypted at rest, never returned in any response, and must never be
 * logged or placed in a {@code toString()}/error message.
 *  *
 * <p>Trap: {@code is_fallback} cannot be combined with {@code is_byok_only}.
 */
public final class OpenRouterByokCreateRequest extends OpenRouterRequest<OpenRouterByokCreateResponse> {

    private final OpenRouterClient client;
    private final String provider;
    private final String key;
    private final String name;
    private final Boolean disabled;
    private final Boolean isFallback;
    private final Boolean isRequired;
    private final Boolean isByokOnly;
    private final List<String> allowedModels;
    private final List<String> allowedApiKeyHashes;
    private final List<String> allowedUserIds;
    private final String workspaceId;

    private OpenRouterByokCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.provider = builder.provider;
        this.key = builder.key;
        this.name = builder.name;
        this.disabled = builder.disabled;
        this.isFallback = builder.isFallback;
        this.isRequired = builder.isRequired;
        this.isByokOnly = builder.isByokOnly;
        this.allowedModels = builder.allowedModels;
        this.allowedApiKeyHashes = builder.allowedApiKeyHashes;
        this.allowedUserIds = builder.allowedUserIds;
        this.workspaceId = builder.workspaceId;
    }


    @Override
    public String getRelativeUrl() {
        return "/byok";
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
        body.put("provider", provider);
        body.put("key", key);
        if (name != null) {
            body.put("name", name);
        }
        if (disabled != null) {
            body.put("disabled", disabled);
        }
        if (isFallback != null) {
            body.put("is_fallback", isFallback);
        }
        if (isRequired != null) {
            body.put("is_required", isRequired);
        }
        if (isByokOnly != null) {
            body.put("is_byok_only", isByokOnly);
        }
        if (allowedModels != null) {
            body.put("allowed_models", new JSONArray(allowedModels));
        }
        if (allowedApiKeyHashes != null) {
            body.put("allowed_api_key_hashes", new JSONArray(allowedApiKeyHashes));
        }
        if (allowedUserIds != null) {
            body.put("allowed_user_ids", new JSONArray(allowedUserIds));
        }
        if (workspaceId != null) {
            body.put("workspace_id", workspaceId);
        }
        return body.toString();
    }

    @Override
    public OpenRouterByokCreateResponse createResponse(String responseBody) {
        return new OpenRouterByokCreateResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterByokCreateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterByokCreateRequest> {

        private final OpenRouterClient client;
    private String provider;
    private String key;
    private String name;
    private Boolean disabled;
    private Boolean isFallback;
    private Boolean isRequired;
    private Boolean isByokOnly;
    private List<String> allowedModels;
    private List<String> allowedApiKeyHashes;
    private List<String> allowedUserIds;
    private String workspaceId;


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
 * Sets the body field {@code provider} (required) - the upstream provider
 * slug the credential authenticates against (e.g. {@code openai},
 * {@code anthropic}, {@code amazon-bedrock}).
 */
    public Builder provider(String provider) {
        this.provider = provider;
        return this;
    }
    /**
 * Sets the body field {@code key} (required) - the raw provider API key or
 * credential (never empty). Treat this value as a secret: it is encrypted at
 * rest, never returned in any response, and must never be logged.
 */
    public Builder key(String key) {
        this.key = key;
        return this;
    }
    /**
 * Sets the body field {@code name} (optional) - human-readable name for the
 * credential (max 255 characters).
 */
    public Builder name(String name) {
        this.name = name;
        return this;
    }
    /**
 * Sets the body field {@code disabled} (optional) - whether the credential is
 * created in a disabled state.
 */
    public Builder disabled(Boolean disabled) {
        this.disabled = disabled;
        return this;
    }
    /**
 * Sets the body field {@code is_fallback} (optional) - whether the credential
 * is used only after non-fallback keys for the same provider have been tried.
 * Cannot be combined with {@code is_byok_only}.
 */
    public Builder isFallback(Boolean isFallback) {
        this.isFallback = isFallback;
        return this;
    }
    /**
 * Sets the body field {@code is_required} (optional) - whether OpenRouter's
 * shared endpoints on this provider are removed for the models this credential
 * applies to; requests for those models run only on your own keys.
 */
    public Builder isRequired(Boolean isRequired) {
        this.isRequired = isRequired;
        return this;
    }
    /**
 * Sets the body field {@code is_byok_only} (optional) - whether the provider's
 * shared endpoints are removed for every model and the provider is skipped
 * instead of spending OpenRouter credits. Only valid on non-fallback
 * credentials.
 */
    public Builder isByokOnly(Boolean isByokOnly) {
        this.isByokOnly = isByokOnly;
        return this;
    }
    /**
 * Sets the body field {@code allowed_models} (optional) - model slugs this
 * credential may be used for (max 100; {@code null} on the wire means no
 * restriction, which is what leaving the field unset sends).
 */
    public Builder allowedModels(List<String> allowedModels) {
        this.allowedModels = allowedModels;
        return this;
    }
    /**
 * Sets the body field {@code allowed_api_key_hashes} (optional) - OpenRouter
 * API key hashes ({@code api_keys.hash}) that may use this credential (max 100,
 * at least one when set; hashes not belonging to your account are rejected with
 * 400).
 */
    public Builder allowedApiKeyHashes(List<String> allowedApiKeyHashes) {
        this.allowedApiKeyHashes = allowedApiKeyHashes;
        return this;
    }
    /**
 * Sets the body field {@code allowed_user_ids} (optional) - user ids that may
 * use this credential (max 100).
 */
    public Builder allowedUserIds(List<String> allowedUserIds) {
        this.allowedUserIds = allowedUserIds;
        return this;
    }
    /**
 * Sets the body field {@code workspace_id} (optional) - the workspace to scope
 * the credential to. When omitted, the credential is created in the account's
 * default workspace; if that default has been deleted, the request returns a 400
 * and the {@code workspace_id} must be passed explicitly.
 */
    public Builder workspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }
        @Override
        public OpenRouterByokCreateRequest build() {
        if (provider == null || provider.isEmpty()) {
            throw new IllegalStateException("provider is required for BYOK credential creation");
        }
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("key is required for BYOK credential creation");
        }
            return new OpenRouterByokCreateRequest(this);
        }


    @Override
    public OpenRouterByokCreateResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterByokCreateResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterByokCreateResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
