package de.entwicklertraining.openrouter4j.extractions;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A request to submit an Extractions request to the extraction router:
 * POST https://openrouter.ai/api/alpha/extractions - extract, compute,
 * estimate or forecast typed fields from a state string and image assets
 * using an extraction-style System One model (ScaleDown Extract family).
 *
 * <p><b>Alpha endpoint.</b> The path lives outside the {@code /api/v1}
 * namespace, so {@link OpenRouterClient#extractions()} routes this request
 * through an internal client whose base URL is the API host's
 * {@code /api/alpha} namespace (derived from the client's base URL). The
 * route's own description is "Alpha: the shape is not stable" - the surface
 * may change or disappear without a major version of the API.
 *
 * <p>Required: {@code model}, {@code state} (the text to read from; may be
 * empty only when the request carries image assets) and {@code fields} (a
 * map of field name to typed field spec, see
 * {@link OpenRouterExtractionField}). Optional: {@code assets} (images and
 * PDFs keyed by reference string, see {@link OpenRouterExtractionAsset}),
 * {@code provider} (routing preferences), {@code reference_date},
 * {@code session_id}, {@code trace} and {@code user}.
 *
 * <p>Traps:</p>
 * <ul>
 *   <li><b>File-only state:</b> write an asset's reference string wherever
 *   the asset belongs in {@code state} or a field {@code instructions};
 *   every asset must be referenced at least once. A {@code state} that is
 *   exactly the sole asset's key carries no text of its own and is read as
 *   file-only. A model that reads either text or a document per request,
 *   not both (ScaleDown Extract with a PDF), needs that form - any other
 *   text around the key is state text and is rejected with 400.</li>
 *   <li><b>Unsupported field types are rejected with 400:</b> which field
 *   types a model answers is model-specific.</li>
 *   <li>{@code session_id} groups related requests for observability in
 *   Broadcast and private logging; it is never sent to the provider. If
 *   provided both in the body and in the {@code x-session-id} header, the
 *   body value takes precedence. Maximum 256 characters (validated
 *   loudly).</li>
 *   <li>{@code reference_date} is an ISO date ({@code YYYY-MM-DD}) that
 *   anchors dates written without a year.</li>
 *   <li>{@code null_threshold} on a field spec is the probability of null
 *   above which the answer's {@code value} is null (default 0.5).</li>
 * </ul>
 *
 * <p>Spec-derived: this surface is derived from the published OpenAPI spec;
 * no live response has been recorded for it yet, so field names and shapes
 * are as documented there.
 */
public final class OpenRouterExtractionsRequest
        extends OpenRouterRequest<OpenRouterExtractionsResponse> {

    private static final Pattern ISO_DATE =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private final OpenRouterClient client;
    private final String model;
    private final String state;
    private final Map<String, OpenRouterExtractionField> fields;
    private final Map<String, OpenRouterExtractionAsset> assets;
    private final OpenRouterExtractionProviderPreferences provider;
    private final String referenceDate;
    private final String sessionId;
    private final String user;
    private final OpenRouterTraceConfig trace;

    private OpenRouterExtractionsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.model = builder.model;
        this.state = builder.state;
        this.fields = builder.fields == null ? null : new LinkedHashMap<>(builder.fields);
        this.assets = builder.assets == null ? null : new LinkedHashMap<>(builder.assets);
        this.provider = builder.provider;
        this.referenceDate = builder.referenceDate;
        this.sessionId = builder.sessionId;
        this.user = builder.user;
        this.trace = builder.trace;
    }

    /**
     * @return the extraction model id (e.g. {@code cloudflare/clairic})
     */
    public String model() {
        return model;
    }

    /**
     * @return the state text to read from (may be empty when the request
     *         carries image assets)
     */
    public String state() {
        return state;
    }

    /**
     * @return the configured field specs keyed as they will be emitted,
     *         empty when none were set
     */
    public Map<String, OpenRouterExtractionField> fields() {
        return fields == null ? Map.of() : Collections.unmodifiableMap(fields);
    }

    /**
     * @return the configured assets keyed by reference string, empty when
     *         none were set
     */
    public Map<String, OpenRouterExtractionAsset> assets() {
        return assets == null ? Map.of() : Collections.unmodifiableMap(assets);
    }

    /**
     * @return the provider routing preferences, or {@code null} when unset
     *         (the {@code provider} key is not sent)
     */
    public OpenRouterExtractionProviderPreferences provider() {
        return provider;
    }

    /**
     * @return the ISO reference date anchoring dates written without a
     *         year, or {@code null} when unset
     */
    public String referenceDate() {
        return referenceDate;
    }

    /**
     * The {@code session_id} grouping key, or {@code null} when unset (the
     * key is not sent). Groups related requests for observability in
     * Broadcast and private logging; never sent to the provider. When set
     * both here and in the {@code x-session-id} header, this body value
     * takes precedence.
     *
     * @return the session identifier, or {@code null}
     */
    public String sessionId() {
        return sessionId;
    }

    /**
     * @return the end-user identifier, or {@code null} when unset
     */
    public String user() {
        return user;
    }

    /**
     * @return the trace observability configuration, or {@code null} when
     *         unset
     */
    public OpenRouterTraceConfig trace() {
        return trace;
    }

    @Override
    public String getRelativeUrl() {
        return "/extractions";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code model}, {@code state} and
     * {@code fields} (all required), {@code assets} (omitted when empty),
     * {@code provider} (omitted unless at least one routing option is set),
     * {@code reference_date}, {@code session_id}, {@code user} and
     * {@code trace} (all omitted when unset).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("model", model);
        root.put("state", state);
        JSONObject fieldsObj = new JSONObject();
        for (Map.Entry<String, OpenRouterExtractionField> entry : fields.entrySet()) {
            fieldsObj.put(entry.getKey(), entry.getValue().toJson());
        }
        root.put("fields", fieldsObj);
        if (assets != null && !assets.isEmpty()) {
            JSONObject assetsObj = new JSONObject();
            for (Map.Entry<String, OpenRouterExtractionAsset> entry : assets.entrySet()) {
                assetsObj.put(entry.getKey(), entry.getValue().toJson());
            }
            root.put("assets", assetsObj);
        }
        // The provider object is emitted whenever any provider routing
        // option is set, so an unset option never appears in the JSON.
        if (provider != null && !provider.toJson().isEmpty()) {
            root.put("provider", provider.toJson());
        }
        if (referenceDate != null) {
            root.put("reference_date", referenceDate);
        }
        if (sessionId != null) {
            root.put("session_id", sessionId);
        }
        if (user != null) {
            root.put("user", user);
        }
        if (trace != null) {
            root.put("trace", trace.toJson());
        }
        return root.toString();
    }

    @Override
    public OpenRouterExtractionsResponse createResponse(String responseBody) {
        return new OpenRouterExtractionsResponse(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterExtractionsRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterExtractionsRequest> {

        private final OpenRouterClient client;
        private String model;
        private String state;
        private Map<String, OpenRouterExtractionField> fields;
        private Map<String, OpenRouterExtractionAsset> assets;
        private OpenRouterExtractionProviderPreferences provider;
        private String referenceDate;
        private String sessionId;
        private String user;
        private OpenRouterTraceConfig trace;

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
         * Sets the required JSON field {@code model} - the extraction model
         * (an extraction-style System One model of the ScaleDown Extract
         * family, e.g. {@code cloudflare/clairic}).
         *
         * @param model the extraction model id
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the required JSON field {@code state} - the text to read
         * from. May be empty only when the request carries image assets.
         * Write asset reference strings wherever the asset belongs; a state
         * that is exactly the sole asset's key is read as file-only (see
         * the class traps).
         *
         * @param state the state text
         * @return this builder
         */
        public Builder state(String state) {
            this.state = state;
            return this;
        }

        /**
         * Adds a typed field spec under a caller-chosen name. Repeated calls
         * with the same name replace the previous spec (JSON objects are
         * unordered, the name is the identity). Build the spec with the
         * {@link OpenRouterExtractionField} factories.
         *
         * @param name the caller-chosen field name (echoed in the answers)
         * @param field the typed field spec
         * @return this builder
         */
        public Builder field(String name, OpenRouterExtractionField field) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("field name must not be null or empty");
            }
            if (field == null) {
                throw new IllegalArgumentException("field spec must not be null");
            }
            ensureFields().put(name, field);
            return this;
        }

        /**
         * Adds a verbatim field spec under a caller-chosen name - the escape
         * hatch for field shapes the typed factories do not cover. The
         * object must carry a {@code type} field and is emitted as written.
         *
         * @param name the caller-chosen field name (echoed in the answers)
         * @param rawField the verbatim field spec object
         * @return this builder
         */
        public Builder field(String name, JSONObject rawField) {
            return field(name, OpenRouterExtractionField.raw(rawField));
        }

        /**
         * Adds every entry of the given map to the {@code fields} object
         * (entries with the same name replace previous ones).
         *
         * @param fields the ordered name-to-spec map
         * @return this builder
         */
        public Builder fields(Map<String, OpenRouterExtractionField> fields) {
            if (fields == null) {
                throw new IllegalArgumentException("fields map must not be null");
            }
            for (Map.Entry<String, OpenRouterExtractionField> entry : fields.entrySet()) {
                field(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Adds an asset under a caller-chosen reference string. Write that
         * reference wherever the asset belongs in {@code state} or a field
         * {@code instructions}; every asset must be referenced at least
         * once. Repeated calls with the same reference replace the previous
         * asset.
         *
         * @param reference the caller-chosen reference string
         * @param asset the asset (build with {@link OpenRouterExtractionAsset#of})
         * @return this builder
         */
        public Builder asset(String reference, OpenRouterExtractionAsset asset) {
            if (reference == null || reference.isEmpty()) {
                throw new IllegalArgumentException("asset reference must not be null or empty");
            }
            if (asset == null) {
                throw new IllegalArgumentException("asset must not be null");
            }
            ensureAssets().put(reference, asset);
            return this;
        }

        /**
         * Adds every entry of the given map to the {@code assets} object
         * (entries with the same reference replace previous ones).
         *
         * @param assets the ordered reference-to-asset map
         * @return this builder
         */
        public Builder assets(Map<String, OpenRouterExtractionAsset> assets) {
            if (assets == null) {
                throw new IllegalArgumentException("assets map must not be null");
            }
            for (Map.Entry<String, OpenRouterExtractionAsset> entry : assets.entrySet()) {
                asset(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Sets the JSON field {@code provider} - provider routing
         * preferences (the full {@code ProviderPreferences} set; build with
         * {@link OpenRouterExtractionProviderPreferences#builder()}). The
         * object is emitted only when at least one routing option is set.
         *
         * @param provider the routing preferences, or {@code null} to unset
         * @return this builder
         */
        public Builder provider(OpenRouterExtractionProviderPreferences provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Sets the JSON field {@code reference_date} - the ISO date
         * ({@code YYYY-MM-DD}) that anchors dates written without a year.
         * Validated loudly against that pattern. Omitted when unset
         * ({@code null}).
         *
         * @param referenceDate the ISO reference date
         * @return this builder
         */
        public Builder referenceDate(String referenceDate) {
            if (referenceDate != null && !ISO_DATE.matcher(referenceDate).matches()) {
                throw new IllegalArgumentException(
                        "reference_date must be an ISO date (YYYY-MM-DD)");
            }
            this.referenceDate = referenceDate;
            return this;
        }

        /**
         * Sets the JSON field {@code session_id} - a unique identifier for
         * grouping related requests. Used for observability grouping in
         * Broadcast and private logging; never sent to the provider. If
         * provided both here and in the {@code x-session-id} header, the
         * body value takes precedence. Maximum 256 characters (validated
         * loudly). Omitted when unset ({@code null}).
         *
         * @param sessionId the session grouping identifier
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            requireWithin256(sessionId, "session_id");
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Sets the JSON field {@code user} - a unique identifier for the
         * end-user, used by OpenRouter for abuse monitoring and cost
         * isolation. Maximum 256 characters (validated loudly). Omitted
         * when unset ({@code null}).
         *
         * @param user the end-user identifier
         * @return this builder
         */
        public Builder user(String user) {
            requireWithin256(user, "user");
            this.user = user;
            return this;
        }

        /**
         * Sets the JSON field {@code trace} - observability metadata that
         * OpenRouter forwards to configured broadcast destinations. Build
         * it with {@link OpenRouterTraceConfig#builder()}. Omitted when
         * unset ({@code null}).
         *
         * @param trace the trace configuration
         * @return this builder
         */
        public Builder trace(OpenRouterTraceConfig trace) {
            this.trace = trace;
            return this;
        }

        private Map<String, OpenRouterExtractionField> ensureFields() {
            if (fields == null) {
                fields = new LinkedHashMap<>();
            }
            return fields;
        }

        private Map<String, OpenRouterExtractionAsset> ensureAssets() {
            if (assets == null) {
                assets = new LinkedHashMap<>();
            }
            return assets;
        }

        private static void requireWithin256(String value, String jsonField) {
            if (value != null && value.codePointCount(0, value.length()) > 256) {
                throw new IllegalArgumentException(jsonField + " must be at most 256 characters");
            }
        }

        @Override
        public OpenRouterExtractionsRequest build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("model is required for an Extractions request");
            }
            if (state == null) {
                throw new IllegalStateException("state is required for an Extractions request");
            }
            if (fields == null || fields.isEmpty()) {
                throw new IllegalStateException(
                        "at least one field is required for an Extractions request - use field(name, ...)");
            }
            return new OpenRouterExtractionsRequest(this);
        }

        @Override
        public OpenRouterExtractionsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterExtractionsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterExtractionsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
