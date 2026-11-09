package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to change a private endpoint:
 * PATCH https://openrouter.ai/api/v1/private-endpoints/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * and the private endpoints entitlement for this route. Only the fields
 * present change; the answer is the whole endpoint
 * ({@code PrivateEndpointResponse}).
 *
 * <p>Traps:
 * <ul>
 *   <li>{@code base_url} and {@code upstream_model_id} re-point a draft and
 *       are accepted on a draft only: naming either on an active endpoint
 *       answers 409 {@code already_active} and writes nothing, even
 *       alongside other fields - delete and re-create instead. The data
 *       policy declarations {@code declared_zdr} and {@code declared_region}
 *       change on drafts and active endpoints alike (on an active endpoint
 *       the change keeps its validation and tested parameters).</li>
 *   <li>On a draft, omitted declarations are kept while the upstream is
 *       unchanged and cleared when it changes; any change to the upstream or
 *       the declarations of a draft clears earlier validation (and with it
 *       {@code tested_parameters}).</li>
 *   <li>{@code pricing} and {@code declared_parameters} replace their stored
 *       value; {@code settings} is itself a partial update where an omitted
 *       field keeps its stored value and an explicit JSON {@code null}
 *       clears a limit or restores the provider default (see
 *       {@link OpenRouterPrivateEndpointSettingsUpdate}).</li>
 *   <li>Every group is checked before anything is written: 400
 *       {@code parameters_invalid} when {@code tool_choice} is declared
 *       without {@code tools} tested or declared, 400
 *       {@code settings_invalid} when {@code max_prompt_tokens} exceeds the
 *       context length, both completion APIs are off, or
 *       {@code excluded_parameters} names a tested parameter, and 422
 *       {@code pricing_invalid} for rates the provider strategy cannot
 *       represent.</li>
 *   <li>Trap for the loud {@code tool_choice} rule: the endpoint's stored
 *       {@code tested_parameters} may already cover {@code tools}; the
 *       library cannot see stored state and rejects a declaration of
 *       {@code tool_choice} without {@code tools} anyway - declare
 *       {@code tools} alongside (tested parameters stay).</li>
 * </ul>
 *
 * <p>The deprecated {@code PUT /private-endpoints/{id}/parameters} and
 * {@code PUT /private-endpoints/{id}/pricing} routes behave exactly like the
 * corresponding PATCH calls and will be removed; this library deliberately
 * does not type them - use this request instead.
 *
 * <p>Not yet live-verified (management route; probing mutates account
 * state): the wire shapes follow the OpenAPI schema.
 */
public final class OpenRouterPrivateEndpointUpdateRequest
        extends OpenRouterRequest<OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointUpdateRequest>> {

    private final OpenRouterClient client;
    private final String id;
    private final String baseUrl;
    private final String upstreamModelId;
    private final Boolean declaredZdr;
    private final boolean declaredZdrSet;
    private final String declaredRegion;
    private final boolean declaredRegionSet;
    private final List<String> declaredParameters;
    private final OpenRouterPrivateEndpointPricing pricing;
    private final OpenRouterPrivateEndpointSettingsUpdate settings;
    private final Integer maxCompletionTokens;
    private final boolean maxCompletionTokensSet;

    private OpenRouterPrivateEndpointUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.baseUrl = builder.baseUrl;
        this.upstreamModelId = builder.upstreamModelId;
        this.declaredZdr = builder.declaredZdr;
        this.declaredZdrSet = builder.declaredZdrSet;
        this.declaredRegion = builder.declaredRegion;
        this.declaredRegionSet = builder.declaredRegionSet;
        this.declaredParameters = builder.declaredParameters;
        this.pricing = builder.pricing;
        this.settings = builder.settings;
        this.maxCompletionTokens = builder.maxCompletionTokens;
        this.maxCompletionTokensSet = builder.maxCompletionTokensSet;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    /**
     * @return the new base URL, or {@code null} when unset
     */
    public String baseUrl() {
        return baseUrl;
    }

    /**
     * @return the new upstream model or deployment id, or {@code null} when
     *         unset
     */
    public String upstreamModelId() {
        return upstreamModelId;
    }

    /**
     * @return the value of {@code declared_zdr}, or {@code null} when the
     *         attestation is unset or was explicitly set to {@code null} (on
     *         update unset keeps the stored attestation - except that it is
     *         cleared when the same PATCH changes the upstream - while
     *         explicit {@code null} clears it back to undeclared; the
     *         difference is in the body, not this accessor)
     */
    public Boolean declaredZdr() {
        return declaredZdr;
    }

    /**
     * @return the value of {@code declared_region}, or {@code null} when the
     *         attestation is unset or was explicitly set to {@code null}
     *         (same keep/clear distinction as {@link #declaredZdr()})
     */
    public String declaredRegion() {
        return declaredRegion;
    }

    /**
     * @return the declared parameter names replacing the stored declaration,
     *         or {@code null} when unset
     */
    public List<String> declaredParameters() {
        return declaredParameters;
    }

    /**
     * @return the pricing value replacing the stored pricing, or
     *         {@code null} when unset
     */
    public OpenRouterPrivateEndpointPricing pricing() {
        return pricing;
    }

    /**
     * @return the settings partial update, or {@code null} when unset
     */
    public OpenRouterPrivateEndpointSettingsUpdate settings() {
        return settings;
    }

    /**
     * @return the completion cap, or {@code null} when unset or explicitly
     *         set to {@code null} (emitted as {@code settings.max_completion_tokens})
     */
    public Integer maxCompletionTokens() {
        return maxCompletionTokens;
    }

    @Override
    public String getRelativeUrl() {
        return "/private-endpoints/" + URLEncoder.encode(id, StandardCharsets.UTF_8);
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
        if (baseUrl != null) {
            body.put("base_url", baseUrl);
        }
        if (upstreamModelId != null) {
            body.put("upstream_model_id", upstreamModelId);
        }
        if (declaredZdrSet) {
            body.put("declared_zdr", declaredZdr == null ? JSONObject.NULL : declaredZdr);
        }
        if (declaredRegionSet) {
            body.put("declared_region", declaredRegion == null ? JSONObject.NULL : declaredRegion);
        }
        if (declaredParameters != null) {
            body.put("declared_parameters", new JSONArray(declaredParameters));
        }
        if (maxCompletionTokensSet || settings != null) {
            JSONObject settingsJson = settings != null
                    ? new JSONObject(settings.json().toString())
                    : new JSONObject();
            // a max_completion_tokens named inside settings(...) wins over the
            // shorthand setter
            if (maxCompletionTokensSet && !settingsJson.has("max_completion_tokens")) {
                settingsJson.put("max_completion_tokens",
                        maxCompletionTokens == null ? JSONObject.NULL : maxCompletionTokens);
            }
            if (!settingsJson.isEmpty()) {
                body.put("settings", settingsJson);
            }
        }
        if (pricing != null) {
            body.put("pricing", pricing.json());
        }
        return body.toString();
    }

    @Override
    public OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointUpdateRequest> createResponse(
            String responseBody) {
        return new OpenRouterPrivateEndpointResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateEndpointUpdateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateEndpointUpdateRequest> {

        private final OpenRouterClient client;
        private final String id;
        private String baseUrl;
        private String upstreamModelId;
        private Boolean declaredZdr;
        private boolean declaredZdrSet;
        private String declaredRegion;
        private boolean declaredRegionSet;
        private List<String> declaredParameters;
        private OpenRouterPrivateEndpointPricing pricing;
        private OpenRouterPrivateEndpointSettingsUpdate settings;
        private Integer maxCompletionTokens;
        private boolean maxCompletionTokensSet;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param id the id (UUID) of the private endpoint (validated loudly;
         *        URL-encoded into the path)
         */
        public Builder(OpenRouterClient client, String id) {
            super(client);
            this.client = client;
            OpenRouterPrivateEndpointRules.requirePathSegment(id, "id");
            this.id = id;
        }

        /**
         * Sets the body field {@code base_url} (optional) - HTTPS base URL of
         * the deployment. Draft-only: naming it on an active endpoint answers
         * 409 {@code already_active} and writes nothing. Never empty
         * ({@code minLength: 1}, validated loudly).
         *
         * @param baseUrl the deployment base URL, {@code null} to unset
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            if (baseUrl != null && baseUrl.isEmpty()) {
                throw new IllegalArgumentException("base_url must not be empty (minLength 1)");
            }
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the body field {@code upstream_model_id} (optional) - model or
         * deployment identifier sent to the upstream provider. Draft-only
         * like {@link #baseUrl(String)} (409 {@code already_active} on an
         * active endpoint). Changing it re-points the draft and clears
         * earlier validation and {@code tested_parameters}.
         *
         * @param upstreamModelId the upstream model or deployment id,
         *        {@code null} to unset
         * @return this builder
         */
        public Builder upstreamModelId(String upstreamModelId) {
            this.upstreamModelId = upstreamModelId;
            return this;
        }

        /**
         * Sets the body field {@code declared_zdr} (optional) - attest that
         * this deployment retains no prompt or completion data. Accepted on
         * drafts and active endpoints alike; on an active endpoint the change
         * keeps its validation and tested parameters. Self-declared and not
         * verified by OpenRouter.
         *
         * <p>Update-specific tri-state: leaving this unset omits the key and
         * the stored attestation stays - except that it is cleared when the
         * same PATCH changes the upstream; a value is sent ({@code false} is
         * a set option and is emitted); {@code null} emits an explicit JSON
         * {@code null} and clears the attestation back to undeclared.
         *
         * @param declaredZdr the attestation, {@code null} to clear
         * @return this builder
         */
        public Builder declaredZdr(Boolean declaredZdr) {
            this.declaredZdr = declaredZdr;
            this.declaredZdrSet = true;
            return this;
        }

        /**
         * Sets the body field {@code declared_region} (optional) - attest
         * where this deployment processes data: {@code global} (not
         * regional), {@code europe} or {@code us}; unknown spellings pass
         * through verbatim. Accepted on drafts and active endpoints alike
         * like {@link #declaredZdr(Boolean)}.
         *
         * <p>Update-specific tri-state like {@link #declaredZdr(Boolean)}:
         * unset keeps the stored attestation (cleared when the same PATCH
         * changes the upstream), a value is sent verbatim, {@code null}
         * clears it back to undeclared.
         *
         * @param declaredRegion the attestation, {@code null} to clear
         * @return this builder
         */
        public Builder declaredRegion(String declaredRegion) {
            this.declaredRegion = declaredRegion;
            this.declaredRegionSet = true;
            return this;
        }

        /**
         * Sets the body field {@code declared_parameters} (optional) -
         * replaces the stored declaration entirely (tested parameters stay).
         * Plain parameter-name strings as on create (see
         * {@code OpenRouterPrivateEndpointCreateRequest.Builder#declaredParameters(List)});
         * unknown spellings pass through verbatim; at most 25 entries
         * (validated loudly).
         *
         * <p>Trap: {@code tool_choice} requires {@code tools} to be tested
         * or declared. When the list names {@code tool_choice} without
         * {@code tools} the library rejects it loudly (400
         * {@code parameters_invalid}) even when the stored
         * {@code tested_parameters} already cover {@code tools} - declare
         * {@code tools} alongside, which is harmless (tested parameters
         * stay).
         *
         * @param declaredParameters the declared parameter names,
         *        {@code null} to unset
         * @return this builder
         * @throws IllegalArgumentException when the list exceeds 25 entries
         *         or names {@code tool_choice} without {@code tools}
         */
        public Builder declaredParameters(List<String> declaredParameters) {
            if (declaredParameters != null && declaredParameters.size() > 25) {
                throw new IllegalArgumentException(
                        "declared_parameters holds at most 25 entries, got " + declaredParameters.size());
            }
            OpenRouterPrivateEndpointRules.requireToolChoiceCovered(declaredParameters);
            this.declaredParameters = declaredParameters;
            return this;
        }

        /**
         * Sets the body field {@code pricing} (optional) - replaces the whole
         * pricing object (schema {@code PrivateEndpointPricing}; build one
         * with {@link OpenRouterPrivateEndpointPricing#builder()}). Omitting
         * {@code cache_read} or {@code cache_write} inside it reverts that
         * rate to the pricing strategy default. Trap: rates the provider
         * strategy cannot represent answer 422 {@code pricing_invalid}.
         *
         * @param pricing the pricing value, {@code null} to unset
         * @return this builder
         */
        public Builder pricing(OpenRouterPrivateEndpointPricing pricing) {
            this.pricing = pricing;
            return this;
        }

        /**
         * Sets the body field {@code settings} (optional) - partial update
         * over the stored capabilities and limits; only the fields it names
         * change and an explicit JSON {@code null} clears a limit or restores
         * the provider default (schema
         * {@code UpdatePrivateEndpointSettingsRequest}; build one with
         * {@link OpenRouterPrivateEndpointSettingsUpdate#builder()}).
         *
         * @param settings the settings value, {@code null} to unset
         * @return this builder
         */
        public Builder settings(OpenRouterPrivateEndpointSettingsUpdate settings) {
            this.settings = settings;
            return this;
        }

        /**
         * Sets the endpoint's completion cap. JSON field:
         * {@code settings.max_completion_tokens} (minimum 1, validated
         * loudly) - the update body has no top-level
         * {@code max_completion_tokens} field; the response views surface
         * their own top-level {@code max_completion_tokens}.
         *
         * <p>Tri-state like the settings fields: unset omits the key (the
         * stored value stays); a value is sent; {@code null} is sent as
         * explicit JSON {@code null} and keeps the model default. A
         * {@code max_completion_tokens} named inside
         * {@link #settings(OpenRouterPrivateEndpointSettingsUpdate)} wins
         * over this shorthand.
         *
         * @param maxCompletionTokens the completion cap, {@code null} to
         *        keep the model default
         * @return this builder
         */
        public Builder maxCompletionTokens(Integer maxCompletionTokens) {
            if (maxCompletionTokens != null && maxCompletionTokens < 1) {
                throw new IllegalArgumentException("max_completion_tokens must be at least 1");
            }
            this.maxCompletionTokens = maxCompletionTokens;
            this.maxCompletionTokensSet = true;
            return this;
        }

        @Override
        public OpenRouterPrivateEndpointUpdateRequest build() {
            return new OpenRouterPrivateEndpointUpdateRequest(this);
        }

        @Override
        public OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointUpdateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointUpdateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointUpdateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
