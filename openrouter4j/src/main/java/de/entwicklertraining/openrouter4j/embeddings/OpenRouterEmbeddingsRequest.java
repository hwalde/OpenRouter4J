package de.entwicklertraining.openrouter4j.embeddings;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A request to create embedding vectors:
 * POST https://openrouter.ai/api/v1/embeddings
 *
 * <p>The {@code model} and the {@code input} are required; the input may be a
 * single string ({@link Builder#input(String)}) or a list of strings
 * ({@link Builder#inputs(List)}), which is sent as a JSON array. Multimodal
 * inputs (token arrays, images, audio, video, files) are not typed here yet
 * and cannot be sent through this builder; wait for a library version with a
 * typed multimodal input or use the raw response/request escape hatches of
 * other surfaces.
 *
 * <p>The optional {@code provider} routing object is emitted only when at
 * least one of {@link Builder#providerOrder(String...)},
 * {@link Builder#providerOnly(String...)}, {@link Builder#providerIgnore(String...)},
 * {@link Builder#requireParameters(Boolean)} or {@link Builder#allowFallbacks(Boolean)}
 * is set - an unset option never appears in the JSON.
 */
public final class OpenRouterEmbeddingsRequest extends OpenRouterRequest<OpenRouterEmbeddingsResponse> {

    private final OpenRouterClient client;
    private final String model;
    private final String input;
    private final List<String> inputs;
    private final Integer dimensions;
    private final String encodingFormat;
    private final String inputType;
    private final String user;
    private final List<String> providerOrder;
    private final List<String> providerOnly;
    private final List<String> providerIgnore;
    private final Boolean requireParameters;
    private final Boolean allowFallbacks;

    private OpenRouterEmbeddingsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.model = builder.model;
        this.input = builder.input;
        this.inputs = builder.inputs == null ? null : List.copyOf(builder.inputs);
        this.dimensions = builder.dimensions;
        this.encodingFormat = builder.encodingFormat;
        this.inputType = builder.inputType;
        this.user = builder.user;
        this.providerOrder = builder.providerOrder == null ? null : List.copyOf(builder.providerOrder);
        this.providerOnly = builder.providerOnly == null ? null : List.copyOf(builder.providerOnly);
        this.providerIgnore = builder.providerIgnore == null ? null : List.copyOf(builder.providerIgnore);
        this.requireParameters = builder.requireParameters;
        this.allowFallbacks = builder.allowFallbacks;
    }

    /**
     * @return the embedding model id (e.g. {@code openai/text-embedding-3-small})
     */
    public String model() {
        return model;
    }

    /**
     * @return the single-string input, or {@code null} when the request was built with {@link #inputs()}
     */
    public String input() {
        return input;
    }

    /**
     * @return the list input, or {@code null} when the request was built with {@link Builder#input(String)}
     */
    public List<String> inputs() {
        return inputs;
    }

    @Override
    public String getRelativeUrl() {
        return "/embeddings";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code model} (required), {@code input}
     * (required; string or string array), {@code dimensions}, {@code encoding_format},
     * {@code input_type}, {@code user} (all omitted when unset) and the
     * {@code provider} object (omitted unless any provider routing option is set).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("model", model);
        if (input != null) {
            root.put("input", input);
        } else {
            JSONArray inputArr = new JSONArray();
            for (String s : inputs) {
                inputArr.put(s);
            }
            root.put("input", inputArr);
        }
        if (dimensions != null) {
            root.put("dimensions", dimensions);
        }
        if (encodingFormat != null) {
            root.put("encoding_format", encodingFormat);
        }
        if (inputType != null) {
            root.put("input_type", inputType);
        }
        if (user != null) {
            root.put("user", user);
        }

        // The provider object is emitted whenever any provider routing option is set,
        // so an unset option never appears in the JSON.
        boolean hasOrder = providerOrder != null && !providerOrder.isEmpty();
        boolean hasOnly = providerOnly != null && !providerOnly.isEmpty();
        boolean hasIgnore = providerIgnore != null && !providerIgnore.isEmpty();
        if (hasOrder || hasOnly || hasIgnore
                || requireParameters != null || allowFallbacks != null) {
            JSONObject providerObj = new JSONObject();
            if (hasOrder) {
                JSONArray orderArr = new JSONArray();
                for (String p : providerOrder) {
                    orderArr.put(p);
                }
                providerObj.put("order", orderArr);
            }
            if (requireParameters != null) {
                providerObj.put("require_parameters", requireParameters);
            }
            if (allowFallbacks != null) {
                providerObj.put("allow_fallbacks", allowFallbacks);
            }
            if (hasIgnore) {
                JSONArray ignoreArr = new JSONArray();
                for (String p : providerIgnore) {
                    ignoreArr.put(p);
                }
                providerObj.put("ignore", ignoreArr);
            }
            if (hasOnly) {
                JSONArray onlyArr = new JSONArray();
                for (String p : providerOnly) {
                    onlyArr.put(p);
                }
                providerObj.put("only", onlyArr);
            }
            root.put("provider", providerObj);
        }
        return root.toString();
    }

    @Override
    public OpenRouterEmbeddingsResponse createResponse(String responseBody) {
        return new OpenRouterEmbeddingsResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterEmbeddingsRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterEmbeddingsRequest> {

        private final OpenRouterClient client;
        private String model;
        private String input;
        private List<String> inputs;
        private Integer dimensions;
        private String encodingFormat;
        private String inputType;
        private String user;
        private List<String> providerOrder;
        private List<String> providerOnly;
        private List<String> providerIgnore;
        private Boolean requireParameters;
        private Boolean allowFallbacks;

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
         * Sets the required JSON field {@code model} - the embedding model id
         * (e.g. {@code openai/text-embedding-3-small}).
         *
         * @param model the embedding model id
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the required JSON field {@code input} to a single string.
         * Calling this clears a previously set {@link #inputs(List)}.
         *
         * @param input the text to embed
         * @return this builder
         */
        public Builder input(String input) {
            this.input = input;
            this.inputs = null;
            return this;
        }

        /**
         * Sets the required JSON field {@code input} to an array of strings
         * (batch embedding). Calling this clears a previously set
         * {@link #input(String)}. Trap: the response returns one embedding
         * per input, matched by {@code index}.
         *
         * @param inputs the texts to embed
         * @return this builder
         */
        public Builder inputs(List<String> inputs) {
            this.inputs = inputs;
            this.input = null;
            return this;
        }

        /**
         * Sets the JSON field {@code dimensions} - the number of dimensions
         * for the output embeddings. Only supported by models with
         * configurable dimensions (e.g. {@code openai/text-embedding-3-*});
         * other models reject the field with HTTP 400.
         *
         * @param dimensions the output dimension count
         * @return this builder
         */
        public Builder dimensions(Integer dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        /**
         * Sets the JSON field {@code encoding_format} - the format of the
         * output embeddings: {@code "float"} (default) or {@code "base64"}.
         * With {@code "base64"}, use
         * {@link OpenRouterEmbedding#embeddingBase64()} on the response.
         *
         * @param encodingFormat {@code "float"} or {@code "base64"}
         * @return this builder
         */
        public Builder encodingFormat(String encodingFormat) {
            this.encodingFormat = encodingFormat;
            return this;
        }

        /**
         * Sets the JSON field {@code input_type} - the type of input, e.g.
         * {@code search_query} or {@code search_document}. Used by models
         * that distinguish query and document embeddings (e.g. Cohere);
         * ignored by models that do not.
         *
         * @param inputType the input type
         * @return this builder
         */
        public Builder inputType(String inputType) {
            this.inputType = inputType;
            return this;
        }

        /**
         * Sets the JSON field {@code user} - a unique identifier for the
         * end-user, used by OpenRouter for abuse monitoring.
         *
         * @param user the end-user identifier
         * @return this builder
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the JSON field {@code provider.order} - the preferred serving
         * providers in priority order. Only emitted when at least one
         * provider routing option is set.
         *
         * @param providers the provider slugs in priority order
         * @return this builder
         */
        public Builder providerOrder(String... providers) {
            this.providerOrder = toNonEmptyList(providers);
            return this;
        }

        /**
         * Sets the JSON field {@code provider.only} - the only providers
         * allowed to serve the request. Only emitted when at least one
         * provider routing option is set.
         *
         * @param providers the provider slugs
         * @return this builder
         */
        public Builder providerOnly(String... providers) {
            this.providerOnly = toNonEmptyList(providers);
            return this;
        }

        /**
         * Sets the JSON field {@code provider.ignore} - providers excluded
         * from serving the request. Only emitted when at least one provider
         * routing option is set.
         *
         * @param providers the provider slugs to ignore
         * @return this builder
         */
        public Builder providerIgnore(String... providers) {
            this.providerIgnore = toNonEmptyList(providers);
            return this;
        }

        /**
         * Sets the JSON field {@code provider.require_parameters} - when
         * {@code true}, OpenRouter only routes to endpoints that support all
         * parameters of the request (e.g. {@code dimensions}); otherwise the
         * parameter may be silently dropped by providers that lack support.
         *
         * @param requireParameters the strict-parameter flag
         * @return this builder
         */
        public Builder requireParameters(Boolean requireParameters) {
            this.requireParameters = requireParameters;
            return this;
        }

        /**
         * Sets the JSON field {@code provider.allow_fallbacks} - when
         * {@code false}, OpenRouter fails the request instead of routing it
         * to a provider outside the configured preferences.
         *
         * @param allowFallbacks the fallback flag
         * @return this builder
         */
        public Builder allowFallbacks(Boolean allowFallbacks) {
            this.allowFallbacks = allowFallbacks;
            return this;
        }

        private List<String> toNonEmptyList(String... values) {
            List<String> list = new ArrayList<>();
            for (String v : values) {
                if (v != null && !v.isEmpty()) {
                    list.add(v);
                }
            }
            return list;
        }

        @Override
        public OpenRouterEmbeddingsRequest build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("model is required for an embeddings request");
            }
            if (input == null && (inputs == null || inputs.isEmpty())) {
                throw new IllegalStateException("input is required for an embeddings request");
            }
            return new OpenRouterEmbeddingsRequest(this);
        }

        @Override
        public OpenRouterEmbeddingsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterEmbeddingsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterEmbeddingsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
