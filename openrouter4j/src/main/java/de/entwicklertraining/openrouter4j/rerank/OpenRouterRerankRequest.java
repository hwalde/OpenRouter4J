package de.entwicklertraining.openrouter4j.rerank;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A request to rerank documents against a query:
 * POST https://openrouter.ai/api/v1/rerank
 *
 * <p>The {@code model}, {@code query} and {@code documents} are required.
 * Documents may be plain strings ({@link Builder#addDocument(String)}) or
 * structured objects with optional {@code text} and/or {@code image} content
 * for multimodal rerank models ({@link Builder#addDocument(String, String)});
 * at least one of text or image must be provided for a structured document.
 *
 * <p>The optional {@code provider} routing object is emitted only when at
 * least one of {@link Builder#providerOrder(String...)},
 * {@link Builder#providerOnly(String...)}, {@link Builder#providerIgnore(String...)},
 * {@link Builder#requireParameters(Boolean)} or {@link Builder#allowFallbacks(Boolean)}
 * is set - an unset option never appears in the JSON.
 */
public final class OpenRouterRerankRequest extends OpenRouterRequest<OpenRouterRerankResponse> {

    private final OpenRouterClient client;
    private final String model;
    private final String query;
    private final List<Document> documents;
    private final Integer topN;
    private final List<String> providerOrder;
    private final List<String> providerOnly;
    private final List<String> providerIgnore;
    private final Boolean requireParameters;
    private final Boolean allowFallbacks;

    /**
     * A structured document with optional text and/or image content. At least
     * one of the two must be provided (enforced by
     * {@link Builder#addDocument(String, String)}). A two-arg
     * {@code Document} is always emitted as a JSON object; the plain-string
     * form is reserved for {@link Builder#addDocument(String)}.
     */
    public static final class Document {

        private final String text;
        private final String image;
        private final boolean plainString;

        private Document(String text, String image, boolean plainString) {
            this.text = text;
            this.image = image;
            this.plainString = plainString;
        }

        private boolean isPlainString() {
            return plainString;
        }

        /**
         * @return the document text, or {@code null} when the document is image-only
         */
        public String text() {
            return text;
        }

        /**
         * @return the image (remote URL or {@code data:image/...} data URI),
         *         or {@code null} when the document is text-only
         */
        public String image() {
            return image;
        }
    }

    private OpenRouterRerankRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.model = builder.model;
        this.query = builder.query;
        this.documents = List.copyOf(builder.documents);
        this.topN = builder.topN;
        this.providerOrder = builder.providerOrder == null ? null : List.copyOf(builder.providerOrder);
        this.providerOnly = builder.providerOnly == null ? null : List.copyOf(builder.providerOnly);
        this.providerIgnore = builder.providerIgnore == null ? null : List.copyOf(builder.providerIgnore);
        this.requireParameters = builder.requireParameters;
        this.allowFallbacks = builder.allowFallbacks;
    }

    /**
     * @return the rerank model id (e.g. {@code cohere/rerank-v3.5})
     */
    public String model() {
        return model;
    }

    /**
     * @return the query the documents are reranked against
     */
    public String query() {
        return query;
    }

    /**
     * @return the documents to rerank, in input order; the response results
     *         reference them by this index
     */
    public List<Document> documents() {
        return documents;
    }

    @Override
    public String getRelativeUrl() {
        return "/rerank";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code model}, {@code query} and
     * {@code documents} (all required; documents are strings or
     * {@code {"text":..., "image":...}} objects), {@code top_n} (omitted when
     * unset) and the {@code provider} object (omitted unless any provider
     * routing option is set).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("model", model);
        root.put("query", query);
        JSONArray documentsArr = new JSONArray();
        for (Document document : documents) {
            if (document.isPlainString()) {
                documentsArr.put(document.text());
            } else {
                JSONObject documentObj = new JSONObject();
                if (document.text() != null) {
                    documentObj.put("text", document.text());
                }
                if (document.image() != null) {
                    documentObj.put("image", document.image());
                }
                documentsArr.put(documentObj);
            }
        }
        root.put("documents", documentsArr);
        if (topN != null) {
            root.put("top_n", topN);
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
    public OpenRouterRerankResponse createResponse(String responseBody) {
        return new OpenRouterRerankResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterRerankRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterRerankRequest> {

        private final OpenRouterClient client;
        private String model;
        private String query;
        private final List<Document> documents = new ArrayList<>();
        private Integer topN;
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
         * Sets the required JSON field {@code model} - the rerank model id
         * (e.g. {@code cohere/rerank-v3.5}).
         *
         * @param model the rerank model id
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the required JSON field {@code query} - the search query the
         * documents are reranked against.
         *
         * @param query the query text
         * @return this builder
         */
        public Builder query(String query) {
            this.query = query;
            return this;
        }

        /**
         * Adds a plain-string document to the required JSON field
         * {@code documents} (emitted as a bare JSON string). The response
         * results reference documents by their position in the input list.
         * Rejected loudly when the text is null or empty - use
         * {@link #addDocument(String, String)} for the structured form.
         *
         * @param text the document text
         * @return this builder
         */
        public Builder addDocument(String text) {
            if (text == null || text.isEmpty()) {
                throw new IllegalArgumentException(
                        "a plain rerank document needs non-empty text");
            }
            documents.add(new Document(text, null, true));
            return this;
        }

        /**
         * Adds a structured document (JSON object {@code {"text":...,
         * "image":...}}) to the required JSON field {@code documents}, for
         * multimodal rerank models. At least one of text or image must be
         * non-null and non-empty. Trap: the image must be a remote URL
         * (http/https) or a base64-encoded data URI ({@code data:image/...});
         * the API rejects other forms. An empty text with a valid image
         * normalizes to an image-only document, and vice versa.
         *
         * @param text the document text, or {@code null} for an image-only document
         * @param image the image URL or data URI, or {@code null} for a text-only document
         * @return this builder
         */
        public Builder addDocument(String text, String image) {
            boolean hasText = text != null && !text.isEmpty();
            boolean hasImage = image != null && !image.isEmpty();
            if (!hasText && !hasImage) {
                throw new IllegalArgumentException(
                        "a structured rerank document needs text or image");
            }
            documents.add(new Document(
                    hasText ? text : null,
                    hasImage ? image : null,
                    false));
            return this;
        }

        /**
         * Sets the JSON field {@code top_n} - the number of most relevant
         * documents to return (API schema minimum 1, validated loudly in
         * {@link #build()}). When unset, the API returns all documents
         * reranked.
         *
         * @param topN the number of results to return
         * @return this builder
         */
        public Builder topN(Integer topN) {
            this.topN = topN;
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
         * parameters of the request; otherwise an unsupported parameter may
         * be silently dropped by the serving provider.
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
        public OpenRouterRerankRequest build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("model is required for a rerank request");
            }
            if (query == null || query.isEmpty()) {
                throw new IllegalStateException("query is required for a rerank request");
            }
            if (documents.isEmpty()) {
                throw new IllegalStateException("at least one document is required for a rerank request");
            }
            if (topN != null && topN < 1) {
                throw new IllegalStateException("topN must be at least 1 (API schema minimum)");
            }
            return new OpenRouterRerankRequest(this);
        }

        @Override
        public OpenRouterRerankResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterRerankResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterRerankResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
