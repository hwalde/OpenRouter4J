package de.entwicklertraining.openrouter4j.batches;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsRequest;
import de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesRequest;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A request to submit an asynchronous batch of inference requests:
 * POST https://openrouter.ai/api/v1/batches
 *
 * <p>Required top-level fields: {@code endpoint} (the API shape every item
 * follows), {@code model} (the batch-level model applied to every request)
 * and {@code requests} (a non-empty array of {@code {custom_id, body}} items,
 * {@link OpenRouterBatchItem}). Optional: {@code provider.only} (pinning - the
 * only provider preference the Batch API accepts; {@code order},
 * {@code sort}, {@code allow_fallbacks} and friends are rejected) and
 * {@code completion_window} (defaults to {@code 24h}, the only accepted
 * value).
 *
 * <p><b>Wire-order trap:</b> the API stream-parses the body so it can accept
 * very large {@code requests} arrays without buffering - {@code endpoint},
 * {@code model} and any {@code provider}/{@code completion_window} must be
 * serialized BEFORE {@code requests}, or the API answers {@code 400}. This
 * class emits the keys in that order on purpose; do not replace
 * {@link #getBody()} with a plain {@link JSONObject}, whose key order is a
 * hash detail.
 *
 * <p>Submission success is not request success: the answer is
 * {@code 202 Accepted} with {@code status: "validating"} and only means the
 * batch was persisted and queued for validation. A per-item body that sets
 * its own {@code model} and disagrees with the batch-level one is rejected
 * with the submission itself (this class rejects it at {@code build()}); an
 * {@code :online} model variant is rejected synchronously with 422. The
 * documented per-request restrictions are checked after the {@code 202} -
 * a violating request moves the whole batch to {@code failed} and its
 * {@code error} explains the rejection - and are worth checking before
 * submitting: URL-only multimodal input (base64 and {@code data:} URIs
 * rejected everywhere), no audio/video input parts, no non-text output via
 * {@code modalities}/{@code audio}/{@code image_config} on
 * {@code /v1/chat/completions}, no {@code stream: true}, no {@code speed},
 * no request without input, no max output token cap below 1, no Anthropic
 * beta-gated features, and no OpenRouter-orchestrated web search (the
 * {@code web} plugin, {@code web_search_options} outside OpenAI models that
 * execute it natively, and web search tools with an {@code engine} other
 * than {@code auto}/{@code native}). On Google models every request of a
 * batch must ask for the same {@code response_format} (all omitted, all
 * {@code json_object}, or all {@code json_schema} with the same schema) -
 * a mismatched batch fails validation and names the first conflicting
 * request, so send one batch per format and schema. A batch routes to
 * exactly one provider; {@code provider.only} is recommended for content not
 * every provider's batch API accepts. The model must have a {@code :batch}
 * endpoint variant (a submit without one answers 400; a {@code
 * provider.only} list matching none of them answers 404). Batch inference is
 * typically billed at ~50% of the standard per-token pricing; BYOK batches
 * route through the provider key automatically ({@code usage.is_byok: true}).
 */
public final class OpenRouterBatchSubmitRequest
        extends OpenRouterRequest<OpenRouterBatchResponse<OpenRouterBatchSubmitRequest>> {

    private final OpenRouterClient client;
    private final OpenRouterBatchEndpoint endpoint;
    private final String model;
    private final List<OpenRouterBatchItem> requests;
    private final List<String> providerOnly;
    private final String completionWindow;

    private OpenRouterBatchSubmitRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.endpoint = builder.endpoint;
        this.model = builder.model;
        this.requests = List.copyOf(builder.requests);
        this.providerOnly = builder.providerOnly == null
                ? null
                : List.copyOf(builder.providerOnly);
        this.completionWindow = builder.completionWindow;
    }

    /**
     * @return the API shape every item of this batch follows
     */
    public OpenRouterBatchEndpoint endpoint() {
        return endpoint;
    }

    /**
     * @return the batch-level model applied to every request
     */
    public String model() {
        return model;
    }

    /**
     * @return the submitted items, in submission order
     */
    public List<OpenRouterBatchItem> requests() {
        return requests;
    }

    /**
     * @return the provider slugs the batch is pinned to, or {@code null}
     *         when unset
     */
    public List<String> providerOnly() {
        return providerOnly;
    }

    /**
     * @return the completion window, or {@code null} when unset (the API
     *         default {@code 24h} applies)
     */
    public String completionWindow() {
        return completionWindow;
    }

    @Override
    public String getRelativeUrl() {
        return "/batches";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code endpoint}, {@code model}, the
     * optional {@code provider} object and {@code completion_window}, then
     * {@code requests} - in exactly that order (see the wire-order trap on
     * this class).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONStringer body = new JSONStringer();
        body.object();
        body.key("endpoint").value(endpoint.wireName());
        body.key("model").value(model);
        if (providerOnly != null && !providerOnly.isEmpty()) {
            JSONArray only = new JSONArray();
            for (String slug : providerOnly) {
                only.put(slug);
            }
            JSONObject provider = new JSONObject();
            provider.put("only", only);
            body.key("provider").value(provider);
        }
        if (completionWindow != null) {
            body.key("completion_window").value(completionWindow);
        }
        body.key("requests");
        body.array();
        for (OpenRouterBatchItem item : requests) {
            body.object();
            body.key("custom_id").value(item.customId());
            body.key("body").value(item.body());
            body.endObject();
        }
        body.endArray();
        body.endObject();
        return body.toString();
    }

    @Override
    public OpenRouterBatchResponse<OpenRouterBatchSubmitRequest> createResponse(String responseBody) {
        return new OpenRouterBatchResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterBatchSubmitRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterBatchSubmitRequest> {

        private final OpenRouterClient client;
        private OpenRouterBatchEndpoint endpoint;
        private String model;
        private final List<OpenRouterBatchItem> requests = new ArrayList<>();
        private List<String> providerOnly;
        private String completionWindow;

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
         * Sets the required {@code endpoint} - the API shape every item of
         * the batch follows ({@link OpenRouterBatchEndpoint#CHAT_COMPLETIONS},
         * {@link OpenRouterBatchEndpoint#RESPONSES},
         * {@link OpenRouterBatchEndpoint#MESSAGES} or
         * {@link OpenRouterBatchEndpoint#EMBEDDINGS}). All requests of one
         * batch use the same shape; to mix shapes, submit separate batches.
         *
         * @param endpoint the endpoint shape
         * @return this builder
         */
        public Builder endpoint(OpenRouterBatchEndpoint endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Sets the required {@code model} - the batch-level model applied to
         * every request (e.g. {@code openai/gpt-4o}). A request body may
         * omit {@code model} to inherit this value; a request body that sets
         * its own {@code model} must match it, or the submission is rejected
         * (checked loudly by {@link #build()}). The model must have a
         * {@code :batch} endpoint variant.
         *
         * @param model the model slug
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Adds one item to the {@code requests} array. The item's
         * {@code custom_id} must be unique within the batch (checked loudly
         * by {@link #build()}). Items are sent in the order they were added;
         * a later {@link #requests(List)} call replaces the whole array.
         *
         * @param item the item to submit
         * @return this builder
         */
        public Builder addRequest(OpenRouterBatchItem item) {
            if (item == null) {
                throw new IllegalArgumentException("item must not be null");
            }
            requests.add(item);
            return this;
        }

        /**
         * Adds one item whose body is taken verbatim from an existing
         * inference request ({@code getBody()}) - an
         * {@link OpenRouterChatCompletionRequest}, {@link OpenRouterMessagesRequest},
         * {@link OpenRouterResponsesRequest} or {@link OpenRouterEmbeddingsRequest}
         * in the shape of the batch's {@link #endpoint(OpenRouterBatchEndpoint)}.
         * See {@link OpenRouterBatchItem#fromRequest(String, OpenRouterRequest)}
         * for the model-matching rule.
         *
         * <p>Deliberately not an {@code addRequestBody} overload: the two
         * would make {@code addRequest(customId, null)} ambiguous at compile
         * time (the same reason {@code addInput} is not an
         * {@code addInputItem} overload).
         *
         * @param customId the caller-assigned id, unique within the batch
         * @param request the inference request to take the body from
         * @return this builder
         */
        public Builder addRequest(String customId, OpenRouterRequest<?> request) {
            return addRequest(OpenRouterBatchItem.fromRequest(customId, request));
        }

        /**
         * Adds one item from a verbatim request body:
         * {@code {custom_id, body}} with the body in the shape of the
         * batch's {@link #endpoint(OpenRouterBatchEndpoint)}. The body may
         * omit {@code model} to inherit the batch-level model.
         *
         * <p>Deliberately not an {@code addRequest(customId, body)} overload:
         * that would make the bare {@code null} body ambiguous at compile
         * time against {@link #addRequest(String, OpenRouterRequest)}.
         *
         * @param customId the caller-assigned id, unique within the batch
         * @param body the request body
         * @return this builder
         */
        public Builder addRequestBody(String customId, JSONObject body) {
            return addRequest(OpenRouterBatchItem.of(customId, body));
        }

        /**
         * Sets the {@code requests} array, replacing any items added before.
         *
         * @param requests the items to submit (must not be empty)
         * @return this builder
         */
        public Builder requests(List<OpenRouterBatchItem> requests) {
            this.requests.clear();
            if (requests != null) {
                for (OpenRouterBatchItem item : requests) {
                    addRequest(item);
                }
            }
            return this;
        }

        /**
         * Sets {@code provider.only} - pin the batch to specific providers
         * (the only provider preference the Batch API accepts; the other
         * provider-routing preferences of the sync API are rejected). One
         * provider runs the whole batch; if none of the listed providers has
         * an eligible {@code :batch} endpoint for the model, the submit
         * answers 404 instead of falling back. Replaces a previously set
         * list; {@code null}/empty unsets it so OpenRouter picks the
         * cheapest eligible batch endpoint itself.
         *
         * @param providerSlugs the provider slugs to pin
         * @return this builder
         */
        public Builder providerOnly(List<String> providerSlugs) {
            if (providerSlugs == null || providerSlugs.isEmpty()) {
                this.providerOnly = null;
                return this;
            }
            for (String slug : providerSlugs) {
                if (slug == null || slug.isBlank()) {
                    throw new IllegalArgumentException("provider slugs must not be blank");
                }
            }
            this.providerOnly = new ArrayList<>(providerSlugs);
            return this;
        }

        /**
         * Sets {@code provider.only} - see {@link #providerOnly(List)}.
         *
         * @param providerSlugs the provider slugs to pin
         * @return this builder
         */
        public Builder providerOnly(String... providerSlugs) {
            return providerOnly(providerSlugs == null ? null : java.util.Arrays.asList(providerSlugs));
        }

        /**
         * Sets {@code completion_window} - defaults to {@code 24h}, which is
         * the only accepted value (validated loudly). {@code null} unsets it
         * so the API default applies.
         *
         * @param completionWindow the completion window
         * @return this builder
         */
        public Builder completionWindow(String completionWindow) {
            if (completionWindow != null && !"24h".equals(completionWindow)) {
                throw new IllegalArgumentException(
                        "completion_window only accepts \"24h\", was: " + completionWindow);
            }
            this.completionWindow = completionWindow;
            return this;
        }

        @Override
        public OpenRouterBatchSubmitRequest build() {
            if (endpoint == null) {
                throw new IllegalStateException("endpoint is required to submit a batch");
            }
            if (model == null || model.isBlank()) {
                throw new IllegalStateException("model is required to submit a batch");
            }
            if (requests.isEmpty()) {
                throw new IllegalStateException("requests must not be empty to submit a batch");
            }
            LinkedHashSet<String> customIds = new LinkedHashSet<>();
            for (OpenRouterBatchItem item : requests) {
                if (!customIds.add(item.customId())) {
                    throw new IllegalStateException(
                            "custom_id must be unique within the batch: " + item.customId());
                }
                Object itemModel = item.body().opt("model");
                if (itemModel instanceof String s && !s.isBlank() && !s.equals(model)) {
                    throw new IllegalStateException(
                            "item " + item.customId() + " sets its own model (" + s
                                    + ") which must match the batch-level model (" + model + ")");
                }
            }
            return new OpenRouterBatchSubmitRequest(this);
        }

        @Override
        public OpenRouterBatchResponse<OpenRouterBatchSubmitRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterBatchResponse<OpenRouterBatchSubmitRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterBatchResponse<OpenRouterBatchSubmitRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
