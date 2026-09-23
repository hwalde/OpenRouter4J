package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to update a BYOK provider credential:
 * PATCH https://openrouter.ai/api/v1/byok/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Only explicitly configured fields are sent; every field is optional. The
 * Secrets rule: the {@code key} field is a provider credential - it is
 * encrypted at rest, never returned in any response, and must never be
 * logged or placed in a {@code toString()}/error message.
 *  *
 * <p>Trap: {@code is_fallback} cannot be combined with {@code is_byok_only}.
 */
public final class OpenRouterByokUpdateRequest extends OpenRouterRequest<OpenRouterByokUpdateResponse> {

    private final OpenRouterClient client;
    private final String id;
    private final String name;
    private final String key;
    private final Boolean disabled;
    private final Boolean isFallback;
    private final Boolean isRequired;
    private final Boolean isByokOnly;
    private final Boolean declaredZdr;
    private final boolean declaredZdrSet;
    private final List<String> allowedModels;
    private final List<String> allowedApiKeyHashes;
    private final List<String> allowedUserIds;

    private OpenRouterByokUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.name = builder.name;
        this.key = builder.key;
        this.disabled = builder.disabled;
        this.isFallback = builder.isFallback;
        this.isRequired = builder.isRequired;
        this.isByokOnly = builder.isByokOnly;
        this.declaredZdr = builder.declaredZdr;
        this.declaredZdrSet = builder.declaredZdrSet;
        this.allowedModels = builder.allowedModels;
        this.allowedApiKeyHashes = builder.allowedApiKeyHashes;
        this.allowedUserIds = builder.allowedUserIds;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/byok/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "";
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
        if (name != null) {
            body.put("name", name);
        }
        if (key != null) {
            body.put("key", key);
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
        if (declaredZdrSet) {
            body.put("declared_zdr", declaredZdr == null ? JSONObject.NULL : declaredZdr);
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
        return body.toString();
    }

    @Override
    public OpenRouterByokUpdateResponse createResponse(String responseBody) {
        return new OpenRouterByokUpdateResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterByokUpdateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterByokUpdateRequest> {

        private final OpenRouterClient client;
    private final String id;
    private String name;
    private String key;
    private Boolean disabled;
    private Boolean isFallback;
    private Boolean isRequired;
    private Boolean isByokOnly;
    private Boolean declaredZdr;
    private boolean declaredZdrSet;
    private List<String> allowedModels;
    private List<String> allowedApiKeyHashes;
    private List<String> allowedUserIds;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param id the id (UUID) of the BYOK credential
     */
    public Builder(OpenRouterClient client, String id) {
        super(client);
        this.client = client;
        this.id = id;
    }

    /**
 * Sets the body field {@code name} (optional) - new human-readable name for
 * the credential (max 255 characters).
 */
    public Builder name(String name) {
        this.name = name;
        return this;
    }
    /**
 * Sets the body field {@code key} (optional) - a new raw provider API key to
 * rotate the credential in-place; the previous key material is overwritten and
 * the masked label is regenerated. Treat this value as a secret: it is never
 * returned in any response and must never be logged.
 */
    public Builder key(String key) {
        this.key = key;
        return this;
    }
    /**
 * Sets the body field {@code disabled} (optional) - whether the credential is
 * disabled.
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
 * applies to.
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
 * Sets the body field {@code declared_zdr} (optional) - your self-declaration
 * of whether the upstream provider account behind this credential has zero
 * data retention (ZDR). {@code true} declares the account ZDR so requests
 * that require ZDR (e.g. {@code zdr(true)} on the chat completions request -
 * the credential-side counterpart of that request-side routing flag) may
 * route to this credential even when the shared endpoint retains data;
 * {@code false} declares it non-ZDR so such requests never route to it.
 * {@code false} is a set option and is emitted.
 *
 * <p>Update-specific tri-state (differs from create): leaving this unset
 * omits the key and the stored value stays unchanged; {@code null} emits an
 * explicit JSON {@code null} and clears the declaration back to "inherit
 * OpenRouter's data policy for the provider's endpoint". Self-declared and
 * not verified by OpenRouter.
 */
    public Builder declaredZdr(Boolean declaredZdr) {
        this.declaredZdr = declaredZdr;
        this.declaredZdrSet = true;
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
        @Override
        public OpenRouterByokUpdateRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
            return new OpenRouterByokUpdateRequest(this);
        }


    @Override
    public OpenRouterByokUpdateResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterByokUpdateResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterByokUpdateResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
