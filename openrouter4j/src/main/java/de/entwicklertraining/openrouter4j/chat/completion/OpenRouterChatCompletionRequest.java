package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.api.base.streaming.SSEStreamProcessor;
import de.entwicklertraining.api.base.streaming.StreamingFormat;
import de.entwicklertraining.api.base.streaming.StreamingInfo;
import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A request to call the OpenRouter chat completions endpoint:
 * POST https://openrouter.ai/api/v1/chat/completions
 *
 * OpenRouter follows OpenAI-compatible API format but adds provider selection and other features.
 */
public final class OpenRouterChatCompletionRequest extends OpenRouterRequest<OpenRouterChatCompletionResponse> {

    private final OpenRouterClient client;
    private final String model;
    private final Double temperature;
    private final Integer topK;
    private final Double topP;
    private final Integer maxTokens; // OpenRouter uses max_tokens (not maxOutputTokens)
    private final List<String> stopSequences;
    private final List<JSONObject> messages;
    private final List<OpenRouterToolDefinition> tools;
    private final String toolChoice; // "auto", "required", "none"
    private final Boolean parallelToolCalls;
    private final OpenRouterJsonSchema responseSchema;
    private final String responseMimeType;
    private final List<String> providers; // OpenRouter-specific: provider selection
    private final Boolean requireParameters; // OpenRouter-specific: provider.require_parameters
    private final Boolean allowFallbacks; // OpenRouter-specific: provider.allow_fallbacks
    private final String reasoningEffort; // reasoning.effort ("max", "xhigh", "high", "medium", "low", "minimal", "none")
    private final Integer reasoningMaxTokens; // reasoning.max_tokens (Anthropic-style reasoning budget)
    private final Boolean reasoningExclude; // reasoning.exclude - keep reasoning out of the response
    private final Boolean reasoningEnabled; // reasoning.enabled - explicit switch for reasoning
    private final boolean stream; // Enable streaming responses

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp", "heic", "heif");

    OpenRouterChatCompletionRequest(
            Builder builder,
            OpenRouterClient client,
            String model,
            Double temperature,
            Integer topK,
            Double topP,
            Integer maxTokens,
            List<String> stopSequences,
            List<JSONObject> messages,
            List<OpenRouterToolDefinition> tools,
            String toolChoice,
            Boolean parallelToolCalls,
            OpenRouterJsonSchema responseSchema,
            String responseMimeType,
            List<String> providers,
            Boolean requireParameters,
            Boolean allowFallbacks,
            String reasoningEffort,
            Integer reasoningMaxTokens,
            Boolean reasoningExclude,
            Boolean reasoningEnabled,
            boolean stream
    ) {
        super(builder);
        this.client = client;
        this.model = model;
        this.temperature = temperature;
        this.topK = topK;
        this.topP = topP;
        this.maxTokens = maxTokens;
        this.stopSequences = stopSequences;
        this.messages = messages;
        this.tools = tools;
        this.toolChoice = toolChoice;
        this.parallelToolCalls = parallelToolCalls;
        this.responseSchema = responseSchema;
        this.responseMimeType = responseMimeType;
        this.providers = providers;
        this.requireParameters = requireParameters;
        this.allowFallbacks = allowFallbacks;
        this.reasoningEffort = reasoningEffort;
        this.reasoningMaxTokens = reasoningMaxTokens;
        this.reasoningExclude = reasoningExclude;
        this.reasoningEnabled = reasoningEnabled;
        this.stream = stream;
    }

    public String model() {
        return model;
    }

    public Double temperature() {
        return temperature;
    }

    public Integer topK() {
        return topK;
    }

    public Double topP() {
        return topP;
    }

    public Integer maxTokens() {
        return maxTokens;
    }

    public List<String> stopSequences() {
        return stopSequences;
    }

    public List<JSONObject> messages() {
        return messages;
    }

    public List<OpenRouterToolDefinition> tools() {
        return tools;
    }

    public String toolChoice() {
        return toolChoice;
    }

    public Boolean parallelToolCalls() {
        return parallelToolCalls;
    }

    public OpenRouterJsonSchema responseSchema() {
        return responseSchema;
    }

    public String responseMimeType() {
        return responseMimeType;
    }

    public List<String> providers() {
        return providers;
    }

    public Boolean requireParameters() {
        return requireParameters;
    }

    public Boolean allowFallbacks() {
        return allowFallbacks;
    }

    /**
     * The reasoning effort hint for reasoning models, or {@code null} when unset.
     * One of "max", "xhigh", "high", "medium", "low", "minimal", "none".
     */
    public String reasoningEffort() {
        return reasoningEffort;
    }

    /**
     * The reasoning token budget (Anthropic-style {@code reasoning.max_tokens}), or {@code null} when unset.
     */
    public Integer reasoningMaxTokens() {
        return reasoningMaxTokens;
    }

    /**
     * Whether reasoning should be excluded from the response, or {@code null} when unset.
     */
    public Boolean reasoningExclude() {
        return reasoningExclude;
    }

    /**
     * The explicit reasoning switch ({@code reasoning.enabled}), or {@code null} when unset.
     */
    public Boolean reasoningEnabled() {
        return reasoningEnabled;
    }

    /**
     * @deprecated Legacy alias for {@link #reasoningMaxTokens()}. The old
     * {@code "reasoning": {"type": "enabled", "budget": N}} wire format no longer
     * exists in the OpenRouter API; the budget is now sent as {@code reasoning.max_tokens}.
     */
    @Deprecated
    public Integer thinkingBudget() {
        return reasoningMaxTokens;
    }

    /**
     * Whether streaming is enabled for this request.
     */
    public boolean stream() {
        return stream;
    }

    @Override
    public String getRelativeUrl() {
        return "/chat/completions";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    @Override
    public String getBody() {
        JSONObject root = new JSONObject();

        // Required: model
        root.put("model", model);

        // Required: messages
        JSONArray messagesArr = new JSONArray();
        for (JSONObject msg : messages) {
            messagesArr.put(msg);
        }
        root.put("messages", messagesArr);

        // Optional parameters
        if (temperature != null) {
            root.put("temperature", temperature);
        }
        if (topK != null) {
            root.put("top_k", topK);
        }
        if (topP != null) {
            root.put("top_p", topP);
        }
        if (maxTokens != null) {
            root.put("max_tokens", maxTokens);
        }
        if (stopSequences != null && !stopSequences.isEmpty()) {
            JSONArray stopArr = new JSONArray();
            for (String s : stopSequences) {
                stopArr.put(s);
            }
            root.put("stop", stopArr);
        }

        // Tools
        if (!tools.isEmpty()) {
            JSONArray toolsArr = new JSONArray();
            for (OpenRouterToolDefinition def : tools) {
                toolsArr.put(def.toJson());
            }
            root.put("tools", toolsArr);

            // tool_choice
            if (toolChoice != null) {
                root.put("tool_choice", toolChoice);
            }

            // parallel_tool_calls
            if (parallelToolCalls != null) {
                root.put("parallel_tool_calls", parallelToolCalls);
            }
        }

        // Response format
        if (responseSchema != null) {
            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_schema");
            JSONObject jsonSchema = new JSONObject();
            jsonSchema.put("name", "response_schema");
            jsonSchema.put("strict", true);
            jsonSchema.put("schema", responseSchema.toJson());
            responseFormat.put("json_schema", jsonSchema);
            root.put("response_format", responseFormat);
        } else if (responseMimeType != null) {
            JSONObject responseFormat = new JSONObject();
            if (responseMimeType.contains("json")) {
                responseFormat.put("type", "json_object");
            } else {
                responseFormat.put("type", "text");
            }
            root.put("response_format", responseFormat);
        }

        // Provider selection (OpenRouter-specific)
        // The provider object is emitted whenever any provider routing option is set,
        // not only when an order list is present.
        boolean hasProviderOrder = providers != null && !providers.isEmpty();
        if (hasProviderOrder || requireParameters != null || allowFallbacks != null) {
            JSONObject providerObj = new JSONObject();
            if (hasProviderOrder) {
                JSONArray orderArr = new JSONArray();
                for (String p : providers) {
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
            root.put("provider", providerObj);
        }

        // Reasoning configuration (OpenRouter-specific, current API format).
        // Emitted only when at least one reasoning option is set - an unset
        // reasoning configuration must not appear in the JSON at all.
        if (reasoningEffort != null || reasoningMaxTokens != null
                || reasoningExclude != null || reasoningEnabled != null) {
            JSONObject reasoning = new JSONObject();
            if (reasoningEffort != null) {
                reasoning.put("effort", reasoningEffort);
            }
            if (reasoningMaxTokens != null) {
                reasoning.put("max_tokens", reasoningMaxTokens);
            }
            if (reasoningExclude != null) {
                reasoning.put("exclude", reasoningExclude);
            }
            if (reasoningEnabled != null) {
                reasoning.put("enabled", reasoningEnabled);
            }
            root.put("reasoning", reasoning);
        }

        // Streaming
        if (stream) {
            root.put("stream", true);
        }

        return root.toString();
    }

    @Override
    public OpenRouterChatCompletionResponse createResponse(String responseBody) {
        return new OpenRouterChatCompletionResponse(new JSONObject(responseBody), this);
    }

    public static Builder builder(OpenRouterClient client) {
        return new Builder(client);
    }

    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterChatCompletionRequest> {
        private final OpenRouterClient client;
        private String model = "google/gemini-2.5-flash";
        private Double temperature;
        private Integer topK;
        private Double topP;
        private Integer maxTokens;
        private final List<String> stopSequences = new ArrayList<>();
        private final List<JSONObject> messages = new ArrayList<>();
        private final List<OpenRouterToolDefinition> tools = new ArrayList<>();
        private String toolChoice;
        private Boolean parallelToolCalls;
        private OpenRouterJsonSchema responseSchema;
        private String responseMimeType;
        private final List<String> providers = new ArrayList<>();
        private Boolean requireParameters;
        private Boolean allowFallbacks;
        private String reasoningEffort;
        private Integer reasoningMaxTokens;
        private Boolean reasoningExclude;
        private Boolean reasoningEnabled;
        private boolean streamEnabled;

        public Builder(OpenRouterClient client) {
            super(client); // Pass client to parent for execute() methods
            this.client = client;
        }

        public Builder model(String m) {
            this.model = m;
            return this;
        }

        public Builder temperature(Double t) {
            this.temperature = t;
            return this;
        }

        public Builder topK(Integer k) {
            this.topK = k;
            return this;
        }

        public Builder topP(Double p) {
            this.topP = p;
            return this;
        }

        /**
         * Sets max_tokens parameter.
         */
        public Builder maxOutputTokens(Integer m) {
            this.maxTokens = m;
            return this;
        }

        public Builder stopSequences(List<String> stops) {
            this.stopSequences.addAll(stops);
            return this;
        }

        public Builder addStopSequence(String stop) {
            this.stopSequences.add(stop);
            return this;
        }

        /**
         * Adds a message to the conversation.
         * For OpenRouter, roles are: system, user, assistant, tool
         */
        public Builder addMessage(String role, String text) {
            JSONObject msg = new JSONObject();
            msg.put("role", role);
            msg.put("content", text);
            messages.add(msg);
            return this;
        }

        public Builder addAllMessages(List<JSONObject> msgList) {
            this.messages.addAll(msgList);
            return this;
        }

        public Builder tools(List<OpenRouterToolDefinition> t) {
            this.tools.addAll(t);
            return this;
        }

        public Builder addTool(OpenRouterToolDefinition t) {
            this.tools.add(t);
            return this;
        }

        /**
         * Controls how the model uses tools: "auto", "required", "none"
         */
        public Builder toolChoice(String choice) {
            this.toolChoice = choice;
            return this;
        }

        public Builder parallelToolCalls(Boolean allow) {
            this.parallelToolCalls = allow;
            return this;
        }

        public Builder responseSchema(OpenRouterJsonSchema schema) {
            this.responseSchema = schema;
            return this;
        }

        public Builder responseMimeType(String mime) {
            this.responseMimeType = mime;
            return this;
        }

        /**
         * Sets system instruction by adding it as the first message with role "system".
         * If a system message already exists, it will be replaced.
         */
        public Builder systemInstruction(String instruction) {
            // Remove existing system messages
            messages.removeIf(msg -> "system".equals(msg.optString("role")));

            // Add new system message at the beginning
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", instruction);
            messages.add(0, systemMsg);
            return this;
        }

        /**
         * Sets the thinking budget for reasoning models.
         *
         * @deprecated Use {@link #reasoningMaxTokens(Integer)} instead. The legacy
         * {@code "reasoning": {"type": "enabled", "budget": N}} wire format is no longer
         * accepted by the OpenRouter API; this method now produces the current
         * {@code reasoning.max_tokens} form.
         */
        @Deprecated
        public Builder thinking(Integer budget) {
            this.reasoningMaxTokens = budget;
            return this;
        }

        /**
         * Sets the reasoning token budget ({@code reasoning.max_tokens}) for reasoning models.
         * <p>
         * JSON field: {@code reasoning.max_tokens}. Default: unset (the key is not sent).
         * Trap: not every model supports explicit reasoning budgets; when in doubt use
         * {@link #reasoningEffort(String)} instead, or combine the two - the API decides
         * precedence when both are present.
         *
         * @param maxTokens maximum number of tokens the model may spend on reasoning
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/best-practices/reasoning-tokens">Reasoning tokens</a>
         */
        public Builder reasoningMaxTokens(Integer maxTokens) {
            this.reasoningMaxTokens = maxTokens;
            return this;
        }

        /**
         * Sets the reasoning effort hint ({@code reasoning.effort}) for reasoning models.
         * <p>
         * JSON field: {@code reasoning.effort}. Default: unset (the key is not sent).
         * Documented values: {@code "max"}, {@code "xhigh"}, {@code "high"},
         * {@code "medium"}, {@code "low"}, {@code "minimal"}, {@code "none"}.
         *
         * @param effort one of the documented effort levels
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/best-practices/reasoning-tokens">Reasoning tokens</a>
         */
        public Builder reasoningEffort(String effort) {
            this.reasoningEffort = effort;
            return this;
        }

        /**
         * Sets {@code reasoning.exclude}: when {@code true}, the model still reasons but the
         * reasoning output is kept out of the response.
         * <p>
         * JSON field: {@code reasoning.exclude}. Default: unset (the key is not sent).
         *
         * @param exclude true to suppress reasoning output in the response
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/best-practices/reasoning-tokens">Reasoning tokens</a>
         */
        public Builder reasoningExclude(Boolean exclude) {
            this.reasoningExclude = exclude;
            return this;
        }

        /**
         * Sets {@code reasoning.enabled} as an explicit on/off switch for reasoning.
         * <p>
         * JSON field: {@code reasoning.enabled}. Default: unset (the key is not sent and
         * the provider default applies).
         *
         * @param enabled true to explicitly enable, false to explicitly disable reasoning
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/best-practices/reasoning-tokens">Reasoning tokens</a>
         */
        public Builder reasoningEnabled(Boolean enabled) {
            this.reasoningEnabled = enabled;
            return this;
        }

        /**
         * Enables or disables streaming for this request.
         * When enabled, partial message deltas will be sent as server-sent events.
         *
         * @param enableStream true to enable streaming, false to disable
         * @return This builder instance
         */
        public Builder stream(boolean enableStream) {
            this.streamEnabled = enableStream;
            return this;
        }

        void setRawJsonStreaming(StreamingResponseHandler<String> handler) {
            SSEStreamProcessor<String> rawProcessor = new SSEStreamProcessor<>(
                    String.class, SSEStreamProcessor.CommonExtractors.RAW_JSON
            );
            this.streamingInfo = StreamingInfo.builder()
                    .format(StreamingFormat.SERVER_SENT_EVENTS)
                    .handler(handler)
                    .customProcessor(rawProcessor)
                    .build();
        }

        /**
         * Sets provider preference order (OpenRouter-specific).
         * Example: provider("google-ai-studio", "openai")
         */
        public Builder provider(String... providerNames) {
            this.providers.clear();
            this.providers.addAll(Arrays.asList(providerNames));
            return this;
        }

        /**
         * Sets provider.require_parameters (OpenRouter-specific).
         * <p>
         * When {@code true}, OpenRouter only routes the request to endpoints that support
         * ALL parameters of the request (e.g. structured outputs via response_format,
         * tools, ...). This matters because by default OpenRouter silently ignores
         * unsupported parameters: with a responseSchema set, the request may be routed
         * to an endpoint without structured_outputs support and the schema is then
         * dropped without any error - the model answers with free-form text instead.
         * <p>
         * Note: if no endpoint of the model supports all requested parameters,
         * OpenRouter responds with HTTP 404 ("No endpoints found that can handle the
         * requested parameters") instead of silently degrading.
         *
         * @param require true to restrict routing to fully compatible endpoints
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/routing/provider-selection">Provider selection</a>
         */
        public Builder requireParameters(boolean require) {
            this.requireParameters = require;
            return this;
        }

        /**
         * Sets provider.allow_fallbacks (OpenRouter-specific).
         * <p>
         * By default ({@code true}), OpenRouter may fall back to other providers when
         * the preferred providers (see {@link #provider(String...)}) are unavailable or
         * fail. Set to {@code false} to pin the request strictly to the providers given
         * in the order list - if none of them can serve the request, OpenRouter returns
         * an error instead of routing elsewhere.
         *
         * @param allow false to disable fallbacks to providers outside the order list
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/routing/provider-selection">Provider selection</a>
         */
        public Builder allowFallbacks(boolean allow) {
            this.allowFallbacks = allow;
            return this;
        }

        /**
         * Adds an image via external URL. Validates supported file extensions.
         * OpenRouter supports multimodal inputs similar to OpenAI.
         */
        public Builder addImageByUrl(String url) {
            Objects.requireNonNull(url, "url must not be null");

            String fileExt = extractExtension(url).toLowerCase(Locale.ROOT);
            if (!ALLOWED_EXTENSIONS.contains(fileExt)) {
                throw new IllegalArgumentException(
                        "Unsupported file extension: " + fileExt + ". Allowed: " + ALLOWED_EXTENSIONS
                );
            }

            // For OpenRouter, we can use the image URL directly in content array
            JSONObject msg = new JSONObject();
            msg.put("role", "user");

            JSONArray contentArr = new JSONArray();
            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", url);
            imageContent.put("image_url", imageUrl);
            contentArr.put(imageContent);

            msg.put("content", contentArr);
            messages.add(msg);

            return this;
        }

        /**
         * Reads a local image file, base64-encodes it, and adds it as a user message.
         */
        public Builder addImageByBase64(Path filePath) {
            Objects.requireNonNull(filePath, "filePath must not be null");

            String fileName = filePath.getFileName().toString().toLowerCase(Locale.ROOT);
            String ext = extractExtension(fileName);
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new IllegalArgumentException(
                        "Unsupported file extension: " + ext + ". Allowed: " + ALLOWED_EXTENSIONS
                );
            }

            String mimeType = extensionToMime(ext);

            byte[] fileBytes;
            try {
                fileBytes = Files.readAllBytes(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + filePath + " => " + e.getMessage(), e);
            }
            String base64Data = Base64.getEncoder().encodeToString(fileBytes);

            // Create message with base64 image
            JSONObject msg = new JSONObject();
            msg.put("role", "user");

            JSONArray contentArr = new JSONArray();
            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", "data:" + mimeType + ";base64," + base64Data);
            imageContent.put("image_url", imageUrl);
            contentArr.put(imageContent);

            msg.put("content", contentArr);
            messages.add(msg);

            return this;
        }

        private static String extractExtension(String path) {
            int dotIdx = path.lastIndexOf('.');
            if (dotIdx < 0) {
                return "";
            }
            String raw = path.substring(dotIdx + 1).toLowerCase(Locale.ROOT);
            // strip query params if any
            int qMark = raw.indexOf('?');
            return (qMark >= 0) ? raw.substring(0, qMark) : raw;
        }

        private static String extensionToMime(String ext) {
            return switch (ext) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "webp" -> "image/webp";
                case "heic" -> "image/heic";
                case "heif" -> "image/heif";
                default -> throw new IllegalArgumentException("Unsupported extension (mime lookup) " + ext);
            };
        }

        public OpenRouterChatCompletionRequest build() {
            // If streaming is enabled via api-base StreamingInfo, also set the stream flag
            boolean shouldStream = streamEnabled || (getStreamingInfo() != null && getStreamingInfo().isEnabled());

            return new OpenRouterChatCompletionRequest(
                    this,
                    client,
                    model,
                    temperature,
                    topK,
                    topP,
                    maxTokens,
                    List.copyOf(stopSequences),
                    List.copyOf(messages),
                    List.copyOf(tools),
                    toolChoice,
                    parallelToolCalls,
                    responseSchema,
                    responseMimeType,
                    List.copyOf(providers),
                    requireParameters,
                    allowFallbacks,
                    reasoningEffort,
                    reasoningMaxTokens,
                    reasoningExclude,
                    reasoningEnabled,
                    shouldStream
            );
        }

        @Override
        public OpenRouterChatCompletionResponse execute() {
            OpenRouterChatCompletionRequest req = build();
            var handler = new OpenRouterChatCompletionCallHandler(client);
            if (!req.tools().isEmpty() && req.isStreamingEnabled()) {
                return handler.handleStreamingRequest(req, extractStreamingHandler(), false).join();
            }
            return handler.handleRequest(req, false);
        }

        @Override
        public OpenRouterChatCompletionResponse executeWithExponentialBackoff() {
            OpenRouterChatCompletionRequest req = build();
            var handler = new OpenRouterChatCompletionCallHandler(client);
            if (!req.tools().isEmpty() && req.isStreamingEnabled()) {
                return handler.handleStreamingRequest(req, extractStreamingHandler(), true).join();
            }
            return handler.handleRequest(req, true);
        }

        @SuppressWarnings("unchecked")
        private StreamingResponseHandler<String> extractStreamingHandler() {
            var info = getStreamingInfo();
            if (info != null && info.isEnabled() && info.getHandler() != null) {
                return (StreamingResponseHandler<String>) info.getHandler();
            }
            throw new IllegalStateException("Streaming handler is required for streaming + tool calling");
        }
    }
}
