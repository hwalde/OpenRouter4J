package de.entwicklertraining.openrouter4j.rerank;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterPercentileCutoffs;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
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
    private final String user;
    private final String sessionId;
    private final OpenRouterTraceConfig trace;
    private final List<String> providerOrder;
    private final List<String> providerOnly;
    private final List<String> providerIgnore;
    private final Boolean requireParameters;
    private final Boolean allowFallbacks;
    private final String dataCollection;
    private final List<String> quantizations;
    private final String sort;
    private final String sortBy;
    private final String sortPartition;
    private final String maxPricePrompt;
    private final String maxPriceCompletion;
    private final String maxPriceImage;
    private final String maxPriceAudio;
    private final Double preferredMaxLatency;
    private final OpenRouterPercentileCutoffs preferredMaxLatencyCutoffs;
    private final Double preferredMinThroughput;
    private final OpenRouterPercentileCutoffs preferredMinThroughputCutoffs;
    private final Boolean enforceDistillableText;
    private final Boolean zdr;

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
        this.user = builder.user;
        this.sessionId = builder.sessionId;
        this.trace = builder.trace;
        this.providerOrder = builder.providerOrder == null ? null : List.copyOf(builder.providerOrder);
        this.providerOnly = builder.providerOnly == null ? null : List.copyOf(builder.providerOnly);
        this.providerIgnore = builder.providerIgnore == null ? null : List.copyOf(builder.providerIgnore);
        this.requireParameters = builder.requireParameters;
        this.allowFallbacks = builder.allowFallbacks;
        this.dataCollection = builder.dataCollection;
        this.quantizations = builder.quantizations.isEmpty() ? null : List.copyOf(builder.quantizations);
        this.sort = builder.sort;
        this.sortBy = builder.sortBy;
        this.sortPartition = builder.sortPartition;
        this.maxPricePrompt = builder.maxPricePrompt;
        this.maxPriceCompletion = builder.maxPriceCompletion;
        this.maxPriceImage = builder.maxPriceImage;
        this.maxPriceAudio = builder.maxPriceAudio;
        this.preferredMaxLatency = builder.preferredMaxLatency;
        this.preferredMaxLatencyCutoffs = builder.preferredMaxLatencyCutoffs;
        this.preferredMinThroughput = builder.preferredMinThroughput;
        this.preferredMinThroughputCutoffs = builder.preferredMinThroughputCutoffs;
        this.enforceDistillableText = builder.enforceDistillableText;
        this.zdr = builder.zdr;
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

    /**
     * The {@code user} end-user identifier, or {@code null} when unset (the
     * key is not sent). Used by OpenRouter for abuse monitoring and cost
     * isolation.
     *
     * @return the end-user identifier, or {@code null}
     */
    public String user() {
        return user;
    }

    /**
     * The {@code session_id} grouping key, or {@code null} when unset (the
     * key is not sent). Groups related requests (a conversation or agent
     * workflow) for observability grouping in Broadcast and private logging;
     * never sent to the provider.
     *
     * @return the session identifier, or {@code null}
     */
    public String sessionId() {
        return sessionId;
    }

    /**
     * The {@code trace} observability configuration, or {@code null} when
     * unset (the key is not sent). Forwarded to configured broadcast
     * destinations (Langfuse, Datadog, Weave, ...).
     *
     * @return the trace configuration, or {@code null}
     */
    public OpenRouterTraceConfig trace() {
        return trace;
    }

    /** @return {@code provider.data_collection}, or {@code null} when unset */
    public String dataCollection() {
        return dataCollection;
    }

    /** @return {@code provider.quantizations}, empty when unset (never {@code null}) */
    public List<String> quantizations() {
        return quantizations == null ? List.of() : quantizations;
    }

    /** @return {@code provider.sort} (plain string form), or {@code null} when unset */
    public String sort() {
        return sort;
    }

    /** @return {@code provider.sort.by} (object form), or {@code null} when unset */
    public String sortBy() {
        return sortBy;
    }

    /** @return {@code provider.sort.partition} (object form), or {@code null} when unset */
    public String sortPartition() {
        return sortPartition;
    }

    /** @return {@code provider.max_price.prompt}, or {@code null} when unset */
    public String maxPricePrompt() {
        return maxPricePrompt;
    }

    /** @return {@code provider.max_price.completion}, or {@code null} when unset */
    public String maxPriceCompletion() {
        return maxPriceCompletion;
    }

    /** @return {@code provider.max_price.image}, or {@code null} when unset */
    public String maxPriceImage() {
        return maxPriceImage;
    }

    /** @return {@code provider.max_price.audio}, or {@code null} when unset */
    public String maxPriceAudio() {
        return maxPriceAudio;
    }

    /** @return {@code provider.preferred_max_latency} (number form), or {@code null} when unset */
    public Double preferredMaxLatency() {
        return preferredMaxLatency;
    }

    /** @return {@code provider.preferred_max_latency} (object form), or {@code null} when unset */
    public OpenRouterPercentileCutoffs preferredMaxLatencyCutoffs() {
        return preferredMaxLatencyCutoffs;
    }

    /** @return {@code provider.preferred_min_throughput} (number form), or {@code null} when unset */
    public Double preferredMinThroughput() {
        return preferredMinThroughput;
    }

    /** @return {@code provider.preferred_min_throughput} (object form), or {@code null} when unset */
    public OpenRouterPercentileCutoffs preferredMinThroughputCutoffs() {
        return preferredMinThroughputCutoffs;
    }

    /** @return {@code provider.enforce_distillable_text}, or {@code null} when unset */
    public Boolean enforceDistillableText() {
        return enforceDistillableText;
    }

    /** @return {@code provider.zdr}, or {@code null} when unset */
    public Boolean zdr() {
        return zdr;
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
        if (user != null) {
            root.put("user", user);
        }
        if (sessionId != null) {
            root.put("session_id", sessionId);
        }
        if (trace != null) {
            root.put("trace", trace.toJson());
        }

        // The provider object is emitted whenever any provider routing option is set,
        // so an unset option never appears in the JSON.
        boolean hasOrder = providerOrder != null && !providerOrder.isEmpty();
        boolean hasOnly = providerOnly != null && !providerOnly.isEmpty();
        boolean hasIgnore = providerIgnore != null && !providerIgnore.isEmpty();
        if (hasOrder || hasOnly || hasIgnore
                || requireParameters != null || allowFallbacks != null
                || dataCollection != null
                || (quantizations != null && !quantizations.isEmpty())
                || sort != null || sortBy != null
                || maxPricePrompt != null || maxPriceCompletion != null
                || maxPriceImage != null || maxPriceAudio != null
                || preferredMaxLatency != null || preferredMaxLatencyCutoffs != null
                || preferredMinThroughput != null || preferredMinThroughputCutoffs != null
                || enforceDistillableText != null || zdr != null) {
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
            if (dataCollection != null) {
                providerObj.put("data_collection", dataCollection);
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
            if (maxPricePrompt != null || maxPriceCompletion != null
                    || maxPriceImage != null || maxPriceAudio != null) {
                JSONObject maxPriceObj = new JSONObject();
                if (maxPricePrompt != null) {
                    maxPriceObj.put("prompt", maxPricePrompt);
                }
                if (maxPriceCompletion != null) {
                    maxPriceObj.put("completion", maxPriceCompletion);
                }
                if (maxPriceImage != null) {
                    maxPriceObj.put("image", maxPriceImage);
                }
                if (maxPriceAudio != null) {
                    maxPriceObj.put("audio", maxPriceAudio);
                }
                providerObj.put("max_price", maxPriceObj);
            }
            if (quantizations != null && !quantizations.isEmpty()) {
                JSONArray quantArr = new JSONArray();
                for (String q : quantizations) {
                    quantArr.put(q);
                }
                providerObj.put("quantizations", quantArr);
            }
            if (sortBy != null) {
                JSONObject sortObj = new JSONObject();
                sortObj.put("by", sortBy);
                sortObj.put("partition", sortPartition);
                providerObj.put("sort", sortObj);
            } else if (sort != null) {
                providerObj.put("sort", sort);
            }
            if (enforceDistillableText != null) {
                providerObj.put("enforce_distillable_text", enforceDistillableText);
            }
            if (zdr != null) {
                providerObj.put("zdr", zdr);
            }
            if (preferredMaxLatency != null) {
                providerObj.put("preferred_max_latency", preferredMaxLatency);
            } else if (preferredMaxLatencyCutoffs != null) {
                providerObj.put("preferred_max_latency", preferredMaxLatencyCutoffs.toJson());
            }
            if (preferredMinThroughput != null) {
                providerObj.put("preferred_min_throughput", preferredMinThroughput);
            } else if (preferredMinThroughputCutoffs != null) {
                providerObj.put("preferred_min_throughput", preferredMinThroughputCutoffs.toJson());
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
        private String user;
        private String sessionId;
        private OpenRouterTraceConfig trace;
        private List<String> providerOrder;
        private List<String> providerOnly;
        private List<String> providerIgnore;
        private Boolean requireParameters;
        private Boolean allowFallbacks;
        private String dataCollection;
        private final List<String> quantizations = new ArrayList<>();
        private String sort;
        private String sortBy;
        private String sortPartition;
        private String maxPricePrompt;
        private String maxPriceCompletion;
        private String maxPriceImage;
        private String maxPriceAudio;
        private Double preferredMaxLatency;
        private OpenRouterPercentileCutoffs preferredMaxLatencyCutoffs;
        private Double preferredMinThroughput;
        private OpenRouterPercentileCutoffs preferredMinThroughputCutoffs;
        private Boolean enforceDistillableText;
        private Boolean zdr;

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
         * Sets the JSON field {@code user} - a unique identifier for the
         * end-user, used by OpenRouter for abuse monitoring. Omitted when
         * unset.
         *
         * @param user the end-user identifier
         * @return this builder
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the JSON field {@code session_id} - a unique identifier for
         * grouping related requests (a conversation or agent workflow). Used
         * for observability grouping in Broadcast and private logging; never
         * sent to the provider. Maximum 256 characters (API-side limit, not
         * validated here). Omitted when unset.
         *
         * @param sessionId the session grouping identifier
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Sets the JSON field {@code trace} - observability metadata that
         * OpenRouter forwards to configured broadcast destinations (Langfuse,
         * Datadog, Weave, ...). Build it with
         * {@link OpenRouterTraceConfig#builder()}. Omitted when unset.
         *
         * @param trace the trace configuration
         * @return this builder
         */
        public Builder trace(OpenRouterTraceConfig trace) {
            this.trace = trace;
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

        /**
         * Sets {@code provider.data_collection} - whether the request may be
         * routed to providers that train on prompts/completions. Documented
         * values: {@code "allow"} and {@code "deny"}; {@code "deny"} restricts
         * routing to providers that do not train on the data. JSON field:
         * {@code provider.data_collection}. Default: unset (the key is not
         * sent; the API applies no data-collection preference).
         *
         * @param policy {@code "allow"} or {@code "deny"}
         * @return this builder
         */
        public Builder dataCollection(String policy) {
            this.dataCollection = policy;
            return this;
        }

        /**
         * Replaces {@code provider.quantizations} - the quantization levels the
         * request may be served with (e.g. {@code int4}, {@code fp8}, {@code fp16}).
         * JSON field: {@code provider.quantizations}. Default: unset (the key
         * is not sent). Passing an empty list removes previously registered
         * levels and emits nothing.
         *
         * @param levels the accepted quantization levels
         * @return this builder
         */
        public Builder quantizations(String... levels) {
            return quantizations(java.util.Arrays.asList(levels));
        }

        /**
         * List-based variant of {@link #quantizations(String...)}. Passing an
         * empty list removes previously registered levels and emits nothing.
         * Default: unset.
         *
         * @param levels the accepted quantization levels
         * @return this builder
         */
        public Builder quantizations(List<String> levels) {
            this.quantizations.clear();
            if (levels != null) {
                this.quantizations.addAll(levels);
            }
            return this;
        }

        /**
         * Sets {@code provider.sort} (plain string form) - the criterion
         * providers are sorted by when routing. Documented values:
         * {@code "price"}, {@code "throughput"}, {@code "latency"}. JSON field:
         * {@code provider.sort}. Default: unset (the key is not sent).
         *
         * @param criterion the sort criterion
         * @return this builder
         */
        public Builder sort(String criterion) {
            this.sort = criterion;
            return this;
        }

        /**
         * Sets the object form of {@code provider.sort}:
         * {@code {"by": ..., "partition": ...}}. Wins over the plain string
         * form when both are set. JSON field: {@code provider.sort} (object
         * form). Default: unset (the key is not sent). Trap: a {@code null}
         * criterion makes the whole object form a silent no-op - no
         * {@code sort} key is emitted at all.
         *
         * @param criterion the sort criterion (e.g. {@code "price"})
         * @param partition {@code "model"} or {@code "none"}
         * @return this builder
         */
        public Builder sortBy(String criterion, String partition) {
            if (partition == null || partition.isBlank()) {
                throw new IllegalArgumentException(
                        "partition is required on the provider.sort object form (\"model\" or \"none\")");
            }
            this.sortBy = criterion;
            this.sortPartition = partition;
            return this;
        }

        /**
         * Sets {@code provider.max_price} caps for prompt and completion tokens.
         * Prices are strings of the token price in USD per million tokens
         * (e.g. {@code "0.5"}). JSON field: {@code provider.max_price}.
         * Default: unset (the key is not sent).
         *
         * @param prompt maximum prompt price, or {@code null} to leave it unset
         * @param completion maximum completion price, or {@code null} to leave it unset
         * @return this builder
         */
        public Builder maxPrice(String prompt, String completion) {
            return maxPrice(prompt, completion, null, null);
        }

        /**
         * Sets {@code provider.max_price} caps for all four price categories.
         * Prices are strings of the token price in USD per million tokens
         * (e.g. {@code "0.5"}); a {@code null} argument omits that key. JSON
         * field: {@code provider.max_price}. Default: unset.
         *
         * @param prompt maximum prompt price, or {@code null}
         * @param completion maximum completion price, or {@code null}
         * @param image maximum image price, or {@code null}
         * @param audio maximum audio price, or {@code null}
         * @return this builder
         */
        public Builder maxPrice(String prompt, String completion, String image, String audio) {
            this.maxPricePrompt = prompt;
            this.maxPriceCompletion = completion;
            this.maxPriceImage = image;
            this.maxPriceAudio = audio;
            return this;
        }

        /**
         * Sets {@code provider.preferred_max_latency} (plain number form) -
         * the maximum acceptable median (p50) end-to-end latency in seconds.
         * Endpoints beyond the threshold are deprioritized, not excluded.
         * Wins over the percentile cutoffs form when both are set. JSON field:
         * {@code provider.preferred_max_latency}. Default: unset.
         *
         * @param seconds maximum median latency in seconds
         * @return this builder
         */
        public Builder preferredMaxLatency(Double seconds) {
            this.preferredMaxLatency = seconds;
            return this;
        }

        /**
         * Percentile cutoffs form of {@link #preferredMaxLatency(Double)}.
         * The plain number form wins when both are set. Default: unset.
         *
         * @param cutoffs the percentile-specific latency cutoffs
         * @return this builder
         */
        public Builder preferredMaxLatency(OpenRouterPercentileCutoffs cutoffs) {
            this.preferredMaxLatencyCutoffs = cutoffs;
            return this;
        }

        /**
         * Sets {@code provider.preferred_min_throughput} (plain number form) -
         * the minimum acceptable median (p50) throughput in tokens per second.
         * Endpoints beyond the threshold are deprioritized, not excluded.
         * Wins over the percentile cutoffs form when both are set. JSON field:
         * {@code provider.preferred_min_throughput}. Default: unset.
         *
         * @param tokensPerSecond minimum median throughput in tokens/s
         * @return this builder
         */
        public Builder preferredMinThroughput(Double tokensPerSecond) {
            this.preferredMinThroughput = tokensPerSecond;
            return this;
        }

        /**
         * Percentile cutoffs form of {@link #preferredMinThroughput(Double)}.
         * The plain number form wins when both are set. Default: unset.
         *
         * @param cutoffs the percentile-specific throughput cutoffs
         * @return this builder
         */
        public Builder preferredMinThroughput(OpenRouterPercentileCutoffs cutoffs) {
            this.preferredMinThroughputCutoffs = cutoffs;
            return this;
        }

        /**
         * Sets {@code provider.enforce_distillable_text} - when {@code true},
         * only endpoints that support distillable text output are eligible.
         * JSON field: {@code provider.enforce_distillable_text}. Default: unset.
         *
         * @param enforce true to restrict routing to distillable-text endpoints
         * @return this builder
         */
        public Builder enforceDistillableText(Boolean enforce) {
            this.enforceDistillableText = enforce;
            return this;
        }

        /**
         * Sets {@code provider.zdr} - when {@code true}, routing is restricted
         * to Zero Data Retention endpoints. Stronger than
         * {@code dataCollection("deny")}. JSON field: {@code provider.zdr}.
         * Default: unset (the key is not sent).
         *
         * @param zdr {@code Boolean.TRUE} to restrict routing to ZDR endpoints
         * @return this builder
         */
        public Builder zdr(Boolean zdr) {
            this.zdr = zdr;
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
