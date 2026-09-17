package de.entwicklertraining.openrouter4j.messages;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.api.base.streaming.SSEStreamProcessor;
import de.entwicklertraining.api.base.streaming.StreamingFormat;
import de.entwicklertraining.api.base.streaming.StreamingInfo;
import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterContextManagementEdit;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.OpenRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterStopCondition;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A request to the Anthropic Messages API on OpenRouter:
 * POST https://openrouter.ai/api/v1/messages
 *
 * <p>Required by the API schema: {@code model} and {@code messages};
 * required by Anthropic semantics (loudly enforced here):
 * {@code max_tokens}. The {@code system} prompt travels as a top-level field
 * (not as a system message), the effort setting travels in the
 * {@code output_config} object.
 *
 * <p>Streaming: with the inherited {@code stream(StreamingResponseHandler)},
 * the body carries {@code stream: true} and the Anthropic SSE events arrive
 * as raw JSON strings on {@code onData} - the event {@code type} field
 * distinguishes {@code message_start}, {@code content_block_start},
 * {@code content_block_delta} (delta types {@code text_delta},
 * {@code input_json_delta}, {@code thinking_delta}, ...),
 * {@code content_block_stop}, {@code message_delta}, {@code message_stop},
 * {@code ping} and {@code error}.
 *
 * <p>An unset option never appears in the JSON; the {@code provider} object
 * is emitted only when at least one provider option is set.
 */
public final class OpenRouterMessagesRequest extends OpenRouterRequest<OpenRouterMessagesResponse> {

    private final OpenRouterClient client;
    private final String model;
    private final Integer maxTokens;
    private final String system;
    private final List<JSONObject> systemBlocks;
    private final List<Message> messages;
    private final Double temperature;
    private final Double topP;
    private final Integer topK;
    private final List<String> stopSequences;
    private final Boolean stream;
    private final Integer thinkingBudgetTokens;
    private final String thinkingMode;
    private final String effort;
    private final OpenRouterJsonSchema outputFormat;
    private final Integer taskBudgetTotal;
    private final Integer taskBudgetRemaining;
    private final String metadataUserId;
    private final List<String> models;
    private final List<String> fallbacks;
    private final String serviceTier;
    private final String speed;
    private final String sessionId;
    private final String user;
    private final String cacheControlTtl;
    private final List<OpenRouterAnthropicTool> tools;
    private final List<JSONObject> rawTools;
    private final String toolChoiceType;
    private final String toolChoiceName;
    private final Boolean toolChoiceDisableParallel;
    private final List<OpenRouterPlugin> plugins;
    private final List<OpenRouterStopCondition> stopServerToolsWhen;
    private final OpenRouterTraceConfig trace;
    private final List<OpenRouterContextManagementEdit> contextManagementEdits;
    private final List<String> providerOrder;
    private final List<String> providerOnly;
    private final List<String> providerIgnore;
    private final Boolean requireParameters;
    private final Boolean allowFallbacks;

    /** One conversation message: role plus string content or verbatim blocks. */
    public static final class Message {

        private final String role;
        private final String text;
        private final List<JSONObject> blocks;
        private final String cacheControlTtl;

        private Message(String role, String text, List<JSONObject> blocks, String cacheControlTtl) {
            this.role = role;
            this.text = text;
            this.blocks = blocks;
            this.cacheControlTtl = cacheControlTtl;
        }

        /** @return the message role ({@code user}, {@code assistant}, ...) */
        public String role() {
            return role;
        }

        /** @return the plain string content, or {@code null} when the message was built with blocks */
        public String text() {
            return text;
        }

        /** @return the verbatim content blocks, or {@code null} when the message was built with plain text */
        public List<JSONObject> blocks() {
            return blocks;
        }

        /** @return the cache_control TTL of the text block, or {@code null} when unset */
        public String cacheControlTtl() {
            return cacheControlTtl;
        }

        JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("role", role);
            boolean cached = cacheControlTtl != null;
            if (blocks != null) {
                JSONArray arr = new JSONArray();
                for (JSONObject block : blocks) {
                    arr.put(new JSONObject(block.toString()));
                }
                json.put("content", arr);
            } else if (cached) {
                JSONArray arr = new JSONArray();
                JSONObject textBlock = new JSONObject();
                textBlock.put("type", "text");
                textBlock.put("text", text);
                JSONObject cacheControl = new JSONObject();
                cacheControl.put("type", "ephemeral");
                if (!cacheControlTtl.isEmpty()) {
                    cacheControl.put("ttl", cacheControlTtl);
                }
                textBlock.put("cache_control", cacheControl);
                arr.put(textBlock);
                json.put("content", arr);
            } else {
                json.put("content", text);
            }
            return json;
        }
    }

    private OpenRouterMessagesRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.model = builder.model;
        this.maxTokens = builder.maxTokens;
        this.system = builder.system;
        this.systemBlocks = builder.systemBlocks == null ? null : List.copyOf(builder.systemBlocks);
        this.messages = builder.messages == null ? null : List.copyOf(builder.messages);
        this.temperature = builder.temperature;
        this.topP = builder.topP;
        this.topK = builder.topK;
        this.stopSequences = builder.stopSequences == null ? null : List.copyOf(builder.stopSequences);
        this.stream = builder.streamRequested();
        this.thinkingBudgetTokens = builder.thinkingBudgetTokens;
        this.thinkingMode = builder.thinkingMode;
        this.effort = builder.effort;
        this.outputFormat = builder.outputFormat;
        this.taskBudgetTotal = builder.taskBudgetTotal;
        this.taskBudgetRemaining = builder.taskBudgetRemaining;
        this.metadataUserId = builder.metadataUserId;
        this.models = builder.models == null ? null : List.copyOf(builder.models);
        this.fallbacks = builder.fallbacks == null ? null : List.copyOf(builder.fallbacks);
        this.serviceTier = builder.serviceTier;
        this.speed = builder.speed;
        this.sessionId = builder.sessionId;
        this.user = builder.user;
        this.cacheControlTtl = builder.cacheControlTtl;
        this.tools = builder.tools == null ? null : List.copyOf(builder.tools);
        this.rawTools = builder.rawTools == null ? null : List.copyOf(builder.rawTools);
        this.toolChoiceType = builder.toolChoiceType;
        this.toolChoiceName = builder.toolChoiceName;
        this.toolChoiceDisableParallel = builder.toolChoiceDisableParallel;
        this.plugins = builder.plugins == null ? null : List.copyOf(builder.plugins);
        this.stopServerToolsWhen = builder.stopServerToolsWhen.isEmpty()
                ? null : List.copyOf(builder.stopServerToolsWhen);
        this.trace = builder.trace;
        this.contextManagementEdits = builder.contextManagementEdits.isEmpty()
                ? null : List.copyOf(builder.contextManagementEdits);
        this.providerOrder = builder.providerOrder == null ? null : List.copyOf(builder.providerOrder);
        this.providerOnly = builder.providerOnly == null ? null : List.copyOf(builder.providerOnly);
        this.providerIgnore = builder.providerIgnore == null ? null : List.copyOf(builder.providerIgnore);
        this.requireParameters = builder.requireParameters;
        this.allowFallbacks = builder.allowFallbacks;
    }

    /** @return the model id (e.g. {@code anthropic/claude-sonnet-4}) */
    public String model() {
        return model;
    }

    /** @return the max output tokens (required by Anthropic semantics) */
    public Integer maxTokens() {
        return maxTokens;
    }

    /** @return the top-level system prompt, or {@code null} when unset */
    public String system() {
        return system;
    }

    /** @return whether the request body carries {@code stream: true} */
    public boolean streamRequested() {
        return stream;
    }

    /**
     * @return the stop conditions of the server-tool agent loop
     *         ({@code stop_server_tools_when}), empty when unset
     */
    public List<OpenRouterStopCondition> stopServerToolsWhen() {
        return stopServerToolsWhen == null ? List.of() : stopServerToolsWhen;
    }

    /**
     * @return the {@code context_management.edits} strategy entries, empty
     *         when unset
     */
    public List<OpenRouterContextManagementEdit> contextManagement() {
        return contextManagementEdits == null ? List.of() : contextManagementEdits;
    }

    @Override
    public String getRelativeUrl() {
        return "/messages";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code model} and {@code messages}
     * (required by the schema), {@code max_tokens} (required by Anthropic
     * semantics), the top-level {@code system} (string or text-block array),
     * {@code temperature}, {@code top_p}, {@code top_k},
     * {@code stop_sequences}, {@code stream}, {@code thinking},
     * {@code output_config} (effort, format, task_budget), {@code metadata},
     * {@code models}, {@code fallbacks} (mutually exclusive per the API),
     * {@code service_tier}, {@code speed}, {@code session_id}, {@code user},
     * {@code cache_control} (request-root), {@code tools},
     * {@code tool_choice}, {@code plugins}, {@code stop_server_tools_when},
     * {@code context_management} (the {@code edits} array, omitted when no
     * strategy is set), {@code trace} (all omitted when unset) and the
     * {@code provider} object (omitted unless any provider option is set).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("model", model);
        if (maxTokens != null) {
            root.put("max_tokens", maxTokens);
        }
        if (system != null) {
            root.put("system", system);
        } else if (systemBlocks != null && !systemBlocks.isEmpty()) {
            JSONArray arr = new JSONArray();
            for (JSONObject block : systemBlocks) {
                arr.put(new JSONObject(block.toString()));
            }
            root.put("system", arr);
        }
        if (messages != null && !messages.isEmpty()) {
            JSONArray arr = new JSONArray();
            for (Message message : messages) {
                arr.put(message.toJson());
            }
            root.put("messages", arr);
        }
        if (temperature != null) {
            root.put("temperature", temperature);
        }
        if (topP != null) {
            root.put("top_p", topP);
        }
        if (topK != null) {
            root.put("top_k", topK);
        }
        if (stopSequences != null && !stopSequences.isEmpty()) {
            JSONArray arr = new JSONArray();
            for (String s : stopSequences) {
                arr.put(s);
            }
            root.put("stop_sequences", arr);
        }
        if (stream) {
            root.put("stream", true);
        }
        if (thinkingBudgetTokens != null) {
            root.put("thinking", new JSONObject()
                    .put("type", "enabled").put("budget_tokens", thinkingBudgetTokens));
        } else if (thinkingMode != null) {
            JSONObject thinking = new JSONObject().put("type", thinkingMode);
            root.put("thinking", thinking);
        }
        if (effort != null || outputFormat != null || taskBudgetTotal != null) {
            JSONObject outputConfig = new JSONObject();
            if (effort != null) {
                outputConfig.put("effort", effort);
            }
            if (outputFormat != null) {
                outputConfig.put("format", new JSONObject()
                        .put("type", "json_schema")
                        .put("schema", outputFormat.toJson()));
            }
            if (taskBudgetTotal != null) {
                JSONObject taskBudget = new JSONObject()
                        .put("type", "tokens").put("total", taskBudgetTotal);
                if (taskBudgetRemaining != null) {
                    taskBudget.put("remaining", taskBudgetRemaining);
                }
                outputConfig.put("task_budget", taskBudget);
            }
            root.put("output_config", outputConfig);
        }
        if (metadataUserId != null) {
            root.put("metadata", new JSONObject().put("user_id", metadataUserId));
        }
        if (models != null && !models.isEmpty()) {
            JSONArray arr = new JSONArray();
            for (String m : models) {
                arr.put(m);
            }
            root.put("models", arr);
        }
        if (fallbacks != null && !fallbacks.isEmpty()) {
            JSONArray arr = new JSONArray();
            for (String m : fallbacks) {
                arr.put(new JSONObject().put("model", m));
            }
            root.put("fallbacks", arr);
        }
        if (serviceTier != null) {
            root.put("service_tier", serviceTier);
        }
        if (speed != null) {
            root.put("speed", speed);
        }
        if (sessionId != null) {
            root.put("session_id", sessionId);
        }
        if (user != null) {
            root.put("user", user);
        }
        if (cacheControlTtl != null) {
            JSONObject cacheControl = new JSONObject();
            cacheControl.put("type", "ephemeral");
            if (!cacheControlTtl.isEmpty()) {
                cacheControl.put("ttl", cacheControlTtl);
            }
            root.put("cache_control", cacheControl);
        }
        boolean hasTools = (tools != null && !tools.isEmpty()) || (rawTools != null && !rawTools.isEmpty());
        if (hasTools) {
            JSONArray arr = new JSONArray();
            if (tools != null) {
                for (OpenRouterAnthropicTool tool : tools) {
                    arr.put(tool.toJson());
                }
            }
            if (rawTools != null) {
                for (JSONObject tool : rawTools) {
                    arr.put(new JSONObject(tool.toString()));
                }
            }
            root.put("tools", arr);
        }
        if (toolChoiceType != null) {
            JSONObject toolChoice = new JSONObject().put("type", toolChoiceType);
            if (toolChoiceName != null) {
                toolChoice.put("name", toolChoiceName);
            }
            if (toolChoiceDisableParallel != null) {
                toolChoice.put("disable_parallel_tool_use", toolChoiceDisableParallel);
            }
            root.put("tool_choice", toolChoice);
        }
        if (plugins != null && !plugins.isEmpty()) {
            JSONArray arr = new JSONArray();
            for (OpenRouterPlugin plugin : plugins) {
                arr.put(plugin.toJson());
            }
            root.put("plugins", arr);
        }
        if (stopServerToolsWhen != null && !stopServerToolsWhen.isEmpty()) {
            JSONArray arr = new JSONArray();
            for (OpenRouterStopCondition condition : stopServerToolsWhen) {
                arr.put(condition.toJson());
            }
            root.put("stop_server_tools_when", arr);
        }
        if (trace != null) {
            root.put("trace", trace.toJson());
        }
        if (contextManagementEdits != null && !contextManagementEdits.isEmpty()) {
            JSONArray edits = new JSONArray();
            for (OpenRouterContextManagementEdit edit : contextManagementEdits) {
                edits.put(edit.toJson());
            }
            root.put("context_management", new JSONObject().put("edits", edits));
        }

        // The provider object is emitted whenever any provider option is set,
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
            if (hasOnly) {
                JSONArray onlyArr = new JSONArray();
                for (String p : providerOnly) {
                    onlyArr.put(p);
                }
                providerObj.put("only", onlyArr);
            }
            if (hasIgnore) {
                JSONArray ignoreArr = new JSONArray();
                for (String p : providerIgnore) {
                    ignoreArr.put(p);
                }
                providerObj.put("ignore", ignoreArr);
            }
            if (requireParameters != null) {
                providerObj.put("require_parameters", requireParameters);
            }
            if (allowFallbacks != null) {
                providerObj.put("allow_fallbacks", allowFallbacks);
            }
            root.put("provider", providerObj);
        }
        return root.toString();
    }

    @Override
    public OpenRouterMessagesResponse createResponse(String responseBody) {
        return new OpenRouterMessagesResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterMessagesRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterMessagesRequest> {

        private static final int MAX_FALLBACKS = 3;

        private final OpenRouterClient client;
        private String model;
        private Integer maxTokens;
        private String system;
        private List<JSONObject> systemBlocks;
        private List<Message> messages;
        private Double temperature;
        private Double topP;
        private Integer topK;
        private List<String> stopSequences;
        private boolean streamEnabled;
        private Integer thinkingBudgetTokens;
        private String thinkingMode;
        private String effort;
        private OpenRouterJsonSchema outputFormat;
        private Integer taskBudgetTotal;
        private Integer taskBudgetRemaining;
        private String metadataUserId;
        private List<String> models;
        private List<String> fallbacks;
        private String serviceTier;
        private String speed;
        private String sessionId;
        private String user;
        private String cacheControlTtl;
        private List<OpenRouterAnthropicTool> tools;
        private List<JSONObject> rawTools;
        private String toolChoiceType;
        private String toolChoiceName;
        private Boolean toolChoiceDisableParallel;
        private List<OpenRouterPlugin> plugins;
        private final List<OpenRouterStopCondition> stopServerToolsWhen = new ArrayList<>();
        private final List<OpenRouterContextManagementEdit> contextManagementEdits = new ArrayList<>();
        private OpenRouterTraceConfig trace;
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
         * Sets the required JSON field {@code model} - the Anthropic (or
         * other) model id (e.g. {@code anthropic/claude-sonnet-4}).
         *
         * @param model the model id
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets {@code max_tokens} - the maximum number of output tokens.
         * Not marked required by the OpenAPI schema, but required by
         * Anthropic semantics, so {@code build()} rejects the request
         * without it.
         *
         * @param maxTokens the maximum output tokens
         * @return this builder
         */
        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        /**
         * Sets the top-level {@code system} prompt as a plain string (the
         * Anthropic way, distinct from a system message). Clears a
         * previously set block form
         * ({@link #addSystemTextBlock(String)}).
         *
         * @param system the system prompt
         * @return this builder
         */
        public Builder system(String system) {
            this.system = system;
            this.systemBlocks = null;
            return this;
        }

        /**
         * Adds one {@code system} text block (the array form of the
         * top-level system prompt, the shape that carries per-block
         * {@code cache_control}); all added blocks are emitted as a JSON
         * array. Clears a previously set string form
         * ({@link #system(String)}).
         *
         * @param text the system prompt block text
         * @return this builder
         */
        public Builder addSystemTextBlock(String text) {
            if (text == null || text.isEmpty()) {
                throw new IllegalArgumentException("system block text must not be null or empty");
            }
            if (systemBlocks == null) {
                systemBlocks = new ArrayList<>();
            }
            system = null;
            JSONObject block = new JSONObject();
            block.put("type", "text");
            block.put("text", text);
            systemBlocks.add(block);
            return this;
        }

        /**
         * Adds one message with plain string content.
         *
         * @param role the message role ({@code user}, {@code assistant}, ...)
         * @param text the message content
         * @return this builder
         */
        public Builder addMessage(String role, String text) {
            if (role == null || role.isEmpty()) {
                throw new IllegalArgumentException("role must not be null or empty");
            }
            if (text == null) {
                throw new IllegalArgumentException("message text must not be null");
            }
            if (messages == null) {
                messages = new ArrayList<>();
            }
            messages.add(new Message(role, text, null, null));
            return this;
        }

        /**
         * Adds one message whose single text block carries an
         * Anthropic-style {@code cache_control} breakpoint
         * ({@code {"type":"ephemeral"}} - the default five-minute TTL when
         * the ttl is {@code null}). The message content is then emitted as a
         * block array, not a plain string.
         *
         * @param role the message role ({@code user}, {@code assistant}, ...)
         * @param text the message content
         * @param ttl {@code 5m} or {@code 1h}, or {@code null} for the default TTL
         * @return this builder
         */
        public Builder addCachedMessage(String role, String text, String ttl) {
            if (role == null || role.isEmpty()) {
                throw new IllegalArgumentException("role must not be null or empty");
            }
            if (text == null) {
                throw new IllegalArgumentException("message text must not be null");
            }
            if (messages == null) {
                messages = new ArrayList<>();
            }
            messages.add(new Message(role, text, null, ttl == null ? "" : ttl));
            return this;
        }

        /**
         * Adds one message with verbatim content blocks - the escape hatch
         * for the block forms the library does not type yet (tool_use,
         * tool_result, image, document, thinking, ...) and for blocks with
         * per-block {@code cache_control}. The blocks are carried verbatim;
         * an unset option never appears in the JSON anyway.
         *
         * @param role the message role ({@code user}, {@code assistant}, ...)
         * @param blocks the content block JSON objects
         * @return this builder
         */
        public Builder addMessageWithBlocks(String role, List<JSONObject> blocks) {
            if (role == null || role.isEmpty()) {
                throw new IllegalArgumentException("role must not be null or empty");
            }
            if (blocks == null || blocks.isEmpty()) {
                throw new IllegalArgumentException("content blocks must not be null or empty");
            }
            if (messages == null) {
                messages = new ArrayList<>();
            }
            messages.add(new Message(role, null, new ArrayList<>(blocks), null));
            return this;
        }

        /**
         * Sets the JSON field {@code temperature} - the sampling temperature.
         *
         * @param temperature the sampling temperature
         * @return this builder
         */
        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Sets the JSON field {@code top_p} - the nucleus sampling cutoff.
         *
         * @param topP the nucleus sampling cutoff
         * @return this builder
         */
        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        /**
         * Sets the JSON field {@code top_k} - the top-k sampling cutoff.
         *
         * @param topK the top-k sampling cutoff
         * @return this builder
         */
        public Builder topK(Integer topK) {
            this.topK = topK;
            return this;
        }

        /**
         * Sets the JSON field {@code stop_sequences} - sequences that end
         * the response.
         *
         * @param sequences the stop sequences
         * @return this builder
         */
        public Builder stopSequences(String... sequences) {
            List<String> list = new ArrayList<>();
            for (String s : sequences) {
                if (s != null && !s.isEmpty()) {
                    list.add(s);
                }
            }
            this.stopSequences = list;
            return this;
        }

        /**
         * Sets the JSON field {@code stream} - SSE streaming of the response.
         * Not needed when the handler-based
         * {@code stream(StreamingResponseHandler)} is used: streaming is
         * then enabled automatically and the {@code stream} flag is emitted
         * either way.
         *
         * @param stream true to request a streaming response
         * @return this builder
         */
        public Builder stream(boolean stream) {
            this.streamEnabled = stream;
            return this;
        }

        /**
         * Installs the streaming handler and enables SSE streaming - every
         * Anthropic Messages event arrives as its raw JSON string on
         * {@code onData}; the {@code type} field distinguishes
         * {@code message_start}, {@code content_block_start},
         * {@code content_block_delta}, {@code content_block_stop},
         * {@code message_delta}, {@code message_stop}, {@code ping} and
         * {@code error}. The body automatically carries {@code stream: true}.
         *
         * @param handler the streaming event handler
         * @return this builder
         */
        @Override
        public Builder stream(StreamingResponseHandler<?> handler) {
            SSEStreamProcessor<String> rawProcessor = new SSEStreamProcessor<>(
                    String.class, SSEStreamProcessor.CommonExtractors.RAW_JSON);
            this.streamingInfo = StreamingInfo.builder()
                    .format(StreamingFormat.SERVER_SENT_EVENTS)
                    .handler(handler)
                    .customProcessor(rawProcessor)
                    .build();
            return this;
        }

        private boolean streamRequested() {
            return streamEnabled || (streamingInfo != null && streamingInfo.isEnabled());
        }

        /**
         * Enables extended thinking with an explicit token budget - emits
         * {@code {"type":"enabled","budget_tokens":...}}. Trap: the budget
         * counts reasoning tokens only; a large budget cuts into
         * {@code max_tokens} (the response reports what remains).
         *
         * @param budgetTokens the reasoning token budget
         * @return this builder
         */
        public Builder thinking(Integer budgetTokens) {
            if (budgetTokens != null && budgetTokens < 1) {
                throw new IllegalArgumentException("thinking budget_tokens must be at least 1, got: " + budgetTokens);
            }
            this.thinkingBudgetTokens = budgetTokens;
            this.thinkingMode = null;
            return this;
        }

        /**
         * Emits {@code thinking} in a budget-free mode:
         * {@code disabled} turns thinking off; {@code adaptive} lets the
         * model decide how much to think (no budget field).
         *
         * @param mode {@code disabled} or {@code adaptive}
         * @return this builder
         */
        public Builder thinkingMode(String mode) {
            if (mode != null && !"disabled".equals(mode) && !"adaptive".equals(mode)) {
                throw new IllegalArgumentException(
                        "thinking mode must be \"disabled\" or \"adaptive\", got: " + mode);
            }
            this.thinkingMode = mode;
            this.thinkingBudgetTokens = null;
            return this;
        }

        /**
         * Sets the JSON field {@code output_config.effort} - how much effort
         * the model should put into its response: {@code low}, {@code medium},
         * {@code high}, {@code xhigh} or {@code max}. Higher effort may be
         * more thorough but takes longer.
         *
         * @param effort the effort level
         * @return this builder
         */
        public Builder effort(String effort) {
            this.effort = effort;
            return this;
        }

        /**
         * Sets the JSON field {@code output_config.format} - the structured
         * output schema ({@code {"type":"json_schema","schema":...}}).
         * Trap: like every structured output, the schema can be silently
         * ignored on endpoints without support - combine with
         * {@link #requireParameters(Boolean)} (true) to route only to
         * endpoints that support all parameters.
         *
         * @param outputFormat the structured output schema
         * @return this builder
         */
        public Builder outputFormat(OpenRouterJsonSchema outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        /**
         * Sets the JSON field {@code output_config.task_budget} - the token
         * budget for an agentic turn; the model sees a countdown of remaining
         * tokens and uses it to prioritize work and wind down gracefully.
         * Advisory - does not enforce a hard cap (total minimum 20000).
         *
         * @param total the total token budget (minimum 20000)
         * @return this builder
         */
        public Builder taskBudget(Integer total) {
            if (total != null && total < 20_000) {
                throw new IllegalArgumentException("task budget total must be at least 20000, got: " + total);
            }
            this.taskBudgetTotal = total;
            this.taskBudgetRemaining = null;
            return this;
        }

        /**
         * Sets the JSON field {@code output_config.task_budget} including the
         * remaining budget (minimum 0).
         *
         * @param total the total token budget (minimum 20000)
         * @param remaining the remaining token budget (minimum 0)
         * @return this builder
         */
        public Builder taskBudget(Integer total, Integer remaining) {
            if (total != null && total < 20_000) {
                throw new IllegalArgumentException("task budget total must be at least 20000, got: " + total);
            }
            if (remaining != null && remaining < 0) {
                throw new IllegalArgumentException("task budget remaining must be at least 0, got: " + remaining);
            }
            this.taskBudgetTotal = total;
            this.taskBudgetRemaining = remaining;
            return this;
        }

        /**
         * Sets the JSON field {@code metadata.user_id} - the end-user
         * identifier for abuse monitoring.
         *
         * @param userId the end-user identifier
         * @return this builder
         */
        public Builder metadataUserId(String userId) {
            this.metadataUserId = userId;
            return this;
        }

        /**
         * Sets the JSON field {@code models} - the fallback model list
         * (OpenRouter multi-model routing). Cannot be combined with
         * {@link #fallbacks(String...)}.
         *
         * @param models the fallback model ids in priority order
         * @return this builder
         */
        public Builder models(String... models) {
            List<String> list = new ArrayList<>();
            for (String m : models) {
                if (m != null && !m.isEmpty()) {
                    list.add(m);
                }
            }
            this.models = list;
            return this;
        }

        /**
         * Sets the JSON field {@code fallbacks} - fallback models to try if
         * the primary model fails or refuses, in order (max 3 entries; each
         * entry accepts only {@code model}). Handled by OpenRouter
         * multi-model routing; cannot be combined with
         * {@link #models(String...)}.
         *
         * @param models the fallback model ids in order
         * @return this builder
         */
        public Builder fallbacks(String... models) {
            List<String> list = new ArrayList<>();
            for (String m : models) {
                if (m != null && !m.isEmpty()) {
                    list.add(m);
                }
            }
            if (list.size() > MAX_FALLBACKS) {
                throw new IllegalArgumentException("at most " + MAX_FALLBACKS + " fallbacks are allowed, got: " + list.size());
            }
            this.fallbacks = list;
            return this;
        }

        /**
         * Sets the JSON field {@code service_tier}.
         *
         * @param serviceTier the service tier
         * @return this builder
         */
        public Builder serviceTier(String serviceTier) {
            this.serviceTier = serviceTier;
            return this;
        }

        /**
         * Sets the JSON field {@code speed} - {@code fast} uses a
         * higher-speed inference configuration at premium pricing; defaults
         * to {@code standard} server-side when omitted.
         *
         * @param speed {@code fast} or {@code standard}
         * @return this builder
         */
        public Builder speed(String speed) {
            this.speed = speed;
            return this;
        }

        /**
         * Sets the JSON field {@code session_id} - a unique identifier for
         * grouping related requests; OpenRouter uses it as the sticky routing
         * key.
         *
         * @param sessionId the session identifier
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Sets the JSON field {@code user} - the end-user identifier for
         * abuse reports.
         *
         * @param user the end-user identifier
         * @return this builder
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Emits the request-root {@code cache_control} object (the automatic
         * caching directive, Anthropic-style {@code {"type":"ephemeral"}} -
         * the default five-minute TTL when the ttl is {@code null}).
         *
         * @param ttl {@code 5m} or {@code 1h}, or {@code null} for the default TTL
         * @return this builder
         */
        public Builder cacheControl(String ttl) {
            this.cacheControlTtl = ttl == null ? "" : ttl;
            return this;
        }

        /**
         * Adds one custom tool to the JSON field {@code tools}.
         *
         * @param tool the Anthropic tool definition
         * @return this builder
         */
        public Builder addTool(OpenRouterAnthropicTool tool) {
            if (tool == null) {
                throw new IllegalArgumentException("tool must not be null");
            }
            if (tools == null) {
                tools = new ArrayList<>();
            }
            tools.add(tool);
            return this;
        }

        /**
         * Adds one verbatim tool definition to the JSON field {@code tools} -
         * the escape hatch for server tools the library does not type yet
         * (e.g. {@code {"type":"web_search_20250305","name":"web_search",...}}).
         *
         * @param toolJson the verbatim tool definition JSON
         * @return this builder
         */
        public Builder addTool(JSONObject toolJson) {
            if (toolJson == null) {
                throw new IllegalArgumentException("tool JSON must not be null");
            }
            if (rawTools == null) {
                rawTools = new ArrayList<>();
            }
            rawTools.add(new JSONObject(toolJson.toString()));
            return this;
        }

        /**
         * Emits {@code {"type":"auto","disable_parallel_tool_use":...}} -
         * the model decides whether to use tools; the flag optionally
         * disables parallel tool use.
         *
         * @param disableParallelToolUse true to disable parallel tool use,
         *                               {@code null} to omit the flag
         * @return this builder
         */
        public Builder toolChoiceAuto(Boolean disableParallelToolUse) {
            return toolChoice("auto", null, disableParallelToolUse);
        }

        /**
         * Emits {@code {"type":"any","disable_parallel_tool_use":...}} - the
         * model must use one of the tools.
         *
         * @param disableParallelToolUse true to disable parallel tool use,
         *                               {@code null} to omit the flag
         * @return this builder
         */
        public Builder toolChoiceAny(Boolean disableParallelToolUse) {
            return toolChoice("any", null, disableParallelToolUse);
        }

        /**
         * Emits {@code {"type":"tool","name":...,"disable_parallel_tool_use":...}}
         * - the model must use exactly this tool.
         *
         * @param name the required tool name
         * @param disableParallelToolUse true to disable parallel tool use,
         *                               {@code null} to omit the flag
         * @return this builder
         */
        public Builder toolChoiceTool(String name, Boolean disableParallelToolUse) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("tool name must not be null or empty");
            }
            return toolChoice("tool", name, disableParallelToolUse);
        }

        private Builder toolChoice(String type, String name, Boolean disableParallelToolUse) {
            this.toolChoiceType = type;
            this.toolChoiceName = name;
            this.toolChoiceDisableParallel = disableParallelToolUse;
            return this;
        }

        /**
         * Adds one OpenRouter plugin (e.g. {@code new OpenRouterWebSearchPlugin()})
         * to the JSON field {@code plugins} - the same plugin surface as on
         * chat completions.
         *
         * @param plugin the plugin
         * @return this builder
         */
        public Builder addPlugin(OpenRouterPlugin plugin) {
            if (plugin == null) {
                throw new IllegalArgumentException("plugin must not be null");
            }
            if (plugins == null) {
                plugins = new ArrayList<>();
            }
            plugins.add(plugin);
            return this;
        }

        /**
         * Sets the JSON field {@code stop_server_tools_when} - the stop
         * conditions of the server-side tool agent loop. The endpoint accepts
         * the same server tools via {@link #addTool(OpenRouterAnthropicTool)}
         * / {@link #addTool(JSONObject)} and {@link #addPlugin(OpenRouterPlugin)},
         * so it runs the same loop as chat completions and honours the same
         * conditions: any condition firing halts the loop (OR logic), the
         * array overrides {@code max_tool_calls} when present, and a firing
         * condition ends with one final turn whose tool calls are disabled.
         * Emits the array only when at least one condition is set.
         *
         * @param conditions the stop conditions
         * @return this builder
         */
        public Builder stopServerToolsWhen(OpenRouterStopCondition... conditions) {
            for (OpenRouterStopCondition condition : conditions) {
                if (condition == null) {
                    throw new IllegalArgumentException("stop condition must not be null");
                }
                stopServerToolsWhen.add(condition);
            }
            return this;
        }

        /**
         * List-based variant of
         * {@link #stopServerToolsWhen(OpenRouterStopCondition...)}.
         *
         * @param conditions the stop conditions
         * @return this builder
         */
        public Builder stopServerToolsWhen(List<OpenRouterStopCondition> conditions) {
            if (conditions == null) {
                throw new IllegalArgumentException("stop conditions must not be null");
            }
            return stopServerToolsWhen(conditions.toArray(new OpenRouterStopCondition[0]));
        }

        /**
         * Adds a single stop condition to {@code stop_server_tools_when} (see
         * {@link #stopServerToolsWhen(OpenRouterStopCondition...)}).
         *
         * @param condition the stop condition
         * @return this builder
         */
        public Builder addStopServerToolsWhen(OpenRouterStopCondition condition) {
            return stopServerToolsWhen(condition);
        }

        /**
         * Sets the {@code context_management.edits} array - Anthropic
         * server-side context editing for long agentic conversations. Each
         * entry is one strategy, discriminated by its {@code type} string:
         * {@code clear_tool_uses_20250919}
         * ({@link de.entwicklertraining.openrouter4j.OpenRouterClearToolUsesEdit}),
         * {@code clear_thinking_20251015}
         * ({@link de.entwicklertraining.openrouter4j.OpenRouterClearThinkingEdit}) and
         * {@code compact_20260112} ({@link de.entwicklertraining.openrouter4j.OpenRouterCompactEdit}); unknown
         * strategy types travel via
         * {@link de.entwicklertraining.openrouter4j.OpenRouterContextManagementEdit#raw(JSONObject)}. The array
         * is emitted only when at least one entry is set. Calling this
         * replaces a previously set list.
         *
         * @param edits the strategy entries
         * @return this builder
         */
        public Builder contextManagement(OpenRouterContextManagementEdit... edits) {
            if (edits == null) {
                throw new IllegalArgumentException("context management edits must not be null");
            }
            contextManagementEdits.clear();
            for (OpenRouterContextManagementEdit edit : edits) {
                if (edit == null) {
                    throw new IllegalArgumentException("context management edit must not be null");
                }
                contextManagementEdits.add(edit);
            }
            return this;
        }

        /**
         * List-based variant of
         * {@link #contextManagement(OpenRouterContextManagementEdit...)}.
         *
         * @param edits the strategy entries
         * @return this builder
         */
        public Builder contextManagement(List<OpenRouterContextManagementEdit> edits) {
            if (edits == null) {
                throw new IllegalArgumentException("context management edits must not be null");
            }
            return contextManagement(edits.toArray(new OpenRouterContextManagementEdit[0]));
        }

        /**
         * Adds a single strategy entry to {@code context_management.edits}
         * (see
         * {@link #contextManagement(OpenRouterContextManagementEdit...)}).
         * Unlike the setter, this accumulates: repeated calls append one
         * entry each, following the {@code addPlugin} / {@code addTool}
         * convention of this builder.
         *
         * @param edit the strategy entry
         * @return this builder
         */
        public Builder addContextManagement(OpenRouterContextManagementEdit edit) {
            if (edit == null) {
                throw new IllegalArgumentException("context management edit must not be null");
            }
            contextManagementEdits.add(edit);
            return this;
        }

        /**
         * Sets the JSON field {@code trace} - the observability/broadcast
         * config (Langfuse, Datadog, Weave, ...), the same surface as on
         * chat completions.
         *
         * @param trace the trace config
         * @return this builder
         */
        public Builder trace(OpenRouterTraceConfig trace) {
            this.trace = trace;
            return this;
        }

        /**
         * Sets the JSON field {@code provider.order} - the preferred serving
         * providers in priority order. Only emitted when at least one
         * provider option is set.
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
         * provider option is set.
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
         * option is set.
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
         * parameters of the request.
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
         * {@code false}, the request fails instead of routing to providers
         * outside the configured preferences.
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
        public OpenRouterMessagesRequest build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("model is required for a messages request");
            }
            if (maxTokens == null) {
                throw new IllegalStateException(
                        "maxTokens is required for a messages request (Anthropic semantics)");
            }
            if (messages == null || messages.isEmpty()) {
                throw new IllegalStateException("at least one message is required for a messages request");
            }
            if (models != null && !models.isEmpty() && fallbacks != null && !fallbacks.isEmpty()) {
                throw new IllegalStateException(
                        "models and fallbacks cannot be combined on a messages request");
            }
            return new OpenRouterMessagesRequest(this);
        }

        @Override
        public OpenRouterMessagesResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterMessagesResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterMessagesResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
