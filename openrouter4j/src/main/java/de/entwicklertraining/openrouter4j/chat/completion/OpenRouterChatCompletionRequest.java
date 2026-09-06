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
    private final Integer maxCompletionTokens; // max_completion_tokens - successor of the deprecated max_tokens
    private final List<String> stopSequences;
    private final List<JSONObject> messages;
    private final List<OpenRouterToolDefinition> tools;
    private final String toolChoice; // "auto", "required", "none"
    private final String toolChoiceFunction; // named form: forces this specific tool via {"type":"function","function":{"name":...}}
    private final String toolChoiceServerTool; // server-tool form: forces this server tool via {"type":"<server-tool-type>"}
    private final Boolean parallelToolCalls;
    private final OpenRouterJsonSchema responseSchema;
    private final String responseMimeType;
    private final List<OpenRouterServerTool> serverTools; // built-in OpenRouter server tools, mixed into the same tools array
    private final List<OpenRouterStopCondition> stopServerToolsWhen; // stop conditions for the server-tool agent loop
    private final List<String> providers; // OpenRouter-specific: provider selection
    private final Boolean requireParameters; // OpenRouter-specific: provider.require_parameters
    private final Boolean allowFallbacks; // OpenRouter-specific: provider.allow_fallbacks
    private final String reasoningEffort; // reasoning.effort ("max", "xhigh", "high", "medium", "low", "minimal", "none")
    private final Integer reasoningMaxTokens; // reasoning.max_tokens (Anthropic-style reasoning budget)
    private final Boolean reasoningExclude; // reasoning.exclude - keep reasoning out of the response
    private final Boolean reasoningEnabled; // reasoning.enabled - explicit switch for reasoning
    private final Double frequencyPenalty; // -2.0 to 2.0
    private final Double presencePenalty; // -2.0 to 2.0
    private final Double repetitionPenalty; // default 1.0
    private final Integer seed;
    private final Double minP;
    private final Double topA;
    private final Map<Integer, Double> logitBias; // token id -> bias (-100 to 100)
    private final Boolean logprobs;
    private final Integer topLogprobs; // 0-20
    private final List<String> models; // fallback candidates, tried in order when the primary model fails
    private final String httpReferer; // app-attribution header HTTP-Referer
    private final String appTitle; // app-attribution header X-OpenRouter-Title (legacy alias X-Title)
    private final List<String> appCategories; // app-attribution header X-OpenRouter-Categories (max 2)
    private final Map<String, String> metadata; // up to 16 string key/value pairs attached to the generation
    private final String user; // stable per-end-user identifier for abuse isolation
    private final String sessionId; // groups related requests; sticky-routing key for prompt-cache hits
    private final Boolean metadataInResponse; // opt-in: X-OpenRouter-Metadata header
    private final String dataCollection; // provider.data_collection ("allow" / "deny")
    private final List<String> ignoreProviders; // provider.ignore
    private final List<String> onlyProviders; // provider.only
    private final String maxPricePrompt; // provider.max_price.prompt
    private final String maxPriceCompletion; // provider.max_price.completion
    private final String maxPriceImage; // provider.max_price.image
    private final String maxPriceAudio; // provider.max_price.audio
    private final List<String> quantizations; // provider.quantizations
    private final String sort; // provider.sort
    private final Boolean enforceDistillableText; // provider.enforce_distillable_text
    private final List<OpenRouterPlugin> plugins; // OpenRouter server-side plugins
    private final List<String> modalities; // output modalities ("text", "image", "audio")
    private final OpenRouterImageConfig imageConfig; // provider-specific image generation options
    private final boolean stream; // Enable streaming responses

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp", "heic", "heif");

    private static final int METADATA_MAX_ENTRIES = 16;
    private static final int METADATA_MAX_KEY_LENGTH = 64;
    private static final int METADATA_MAX_VALUE_LENGTH = 512;

    /** Header carrying the app URL for attribution. */
    public static final String HEADER_HTTP_REFERER = "HTTP-Referer";
    /** Header carrying the app display name for attribution (legacy alias: {@code X-Title}). */
    public static final String HEADER_APP_TITLE = "X-OpenRouter-Title";
    /** Header carrying the comma-separated marketplace categories (max 2). */
    public static final String HEADER_APP_CATEGORIES = "X-OpenRouter-Categories";
    /** Header carrying the session identifier (the body field {@code session_id} wins). */
    public static final String HEADER_SESSION_ID = "x-session-id";
    /** Opt-in header to receive routing metadata under {@code openrouter_metadata}. */
    public static final String HEADER_METADATA = "X-OpenRouter-Metadata";

    OpenRouterChatCompletionRequest(
            Builder builder,
            OpenRouterClient client,
            String model,
            Double temperature,
            Integer topK,
            Double topP,
            Integer maxTokens,
            Integer maxCompletionTokens,
            List<String> stopSequences,
            List<JSONObject> messages,
            List<OpenRouterToolDefinition> tools,
            String toolChoice,
            String toolChoiceFunction,
            String toolChoiceServerTool,
            Boolean parallelToolCalls,
            OpenRouterJsonSchema responseSchema,
            String responseMimeType,
            List<OpenRouterServerTool> serverTools,
            List<OpenRouterStopCondition> stopServerToolsWhen,
            List<String> providers,
            Boolean requireParameters,
            Boolean allowFallbacks,
            String reasoningEffort,
            Integer reasoningMaxTokens,
            Boolean reasoningExclude,
            Boolean reasoningEnabled,
            Double frequencyPenalty,
            Double presencePenalty,
            Double repetitionPenalty,
            Integer seed,
            Double minP,
            Double topA,
            Map<Integer, Double> logitBias,
            Boolean logprobs,
            Integer topLogprobs,
            List<String> models,
            String httpReferer,
            String appTitle,
            List<String> appCategories,
            Map<String, String> metadata,
            String user,
            String sessionId,
            Boolean metadataInResponse,
            String dataCollection,
            List<String> ignoreProviders,
            List<String> onlyProviders,
            String maxPricePrompt,
            String maxPriceCompletion,
            String maxPriceImage,
            String maxPriceAudio,
            List<String> quantizations,
            String sort,
            Boolean enforceDistillableText,
            List<OpenRouterPlugin> plugins,
            List<String> modalities,
            OpenRouterImageConfig imageConfig,
            boolean stream
    ) {
        super(builder);
        this.client = client;
        this.model = model;
        this.temperature = temperature;
        this.topK = topK;
        this.topP = topP;
        this.maxTokens = maxTokens;
        this.maxCompletionTokens = maxCompletionTokens;
        this.stopSequences = stopSequences;
        this.messages = messages;
        this.tools = tools;
        this.toolChoice = toolChoice;
        this.toolChoiceFunction = toolChoiceFunction;
        this.toolChoiceServerTool = toolChoiceServerTool;
        this.parallelToolCalls = parallelToolCalls;
        this.responseSchema = responseSchema;
        this.responseMimeType = responseMimeType;
        this.serverTools = serverTools == null ? null : List.copyOf(serverTools);
        this.stopServerToolsWhen = stopServerToolsWhen == null ? null : List.copyOf(stopServerToolsWhen);
        this.providers = providers;
        this.requireParameters = requireParameters;
        this.allowFallbacks = allowFallbacks;
        this.reasoningEffort = reasoningEffort;
        this.reasoningMaxTokens = reasoningMaxTokens;
        this.reasoningExclude = reasoningExclude;
        this.reasoningEnabled = reasoningEnabled;
        this.frequencyPenalty = frequencyPenalty;
        this.presencePenalty = presencePenalty;
        this.repetitionPenalty = repetitionPenalty;
        this.seed = seed;
        this.minP = minP;
        this.topA = topA;
        this.logitBias = logitBias == null ? null : Map.copyOf(logitBias);
        this.logprobs = logprobs;
        this.topLogprobs = topLogprobs;
        this.models = models == null ? null : List.copyOf(models);
        this.httpReferer = httpReferer;
        this.appTitle = appTitle;
        this.appCategories = appCategories == null ? null : List.copyOf(appCategories);
        this.metadata = metadata == null ? null : Map.copyOf(metadata);
        this.user = user;
        this.sessionId = sessionId;
        this.metadataInResponse = metadataInResponse;
        this.dataCollection = dataCollection;
        this.ignoreProviders = ignoreProviders == null ? null : List.copyOf(ignoreProviders);
        this.onlyProviders = onlyProviders == null ? null : List.copyOf(onlyProviders);
        this.maxPricePrompt = maxPricePrompt;
        this.maxPriceCompletion = maxPriceCompletion;
        this.maxPriceImage = maxPriceImage;
        this.maxPriceAudio = maxPriceAudio;
        this.quantizations = quantizations == null ? null : List.copyOf(quantizations);
        this.sort = sort;
        this.enforceDistillableText = enforceDistillableText;
        this.plugins = plugins == null ? null : List.copyOf(plugins);
        this.modalities = modalities == null ? null : List.copyOf(modalities);
        this.imageConfig = imageConfig;
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

    /**
     * The completion token limit sent as {@code max_completion_tokens}, or {@code null} when unset.
     */
    public Integer maxCompletionTokens() {
        return maxCompletionTokens;
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

    /**
     * The name of the forced tool for the named {@code tool_choice} form, or {@code null} when unset.
     */
    public String toolChoiceFunction() {
        return toolChoiceFunction;
    }

    /**
     * The server-tool type forced via the server-tool {@code tool_choice} form
     * ({@code {"type":"<server-tool-type>"}}), or {@code null} when unset.
     */
    public String toolChoiceServerTool() {
        return toolChoiceServerTool;
    }

    /**
     * The built-in OpenRouter server tools mixed into the {@code tools} request array
     * alongside the function tools, empty when unset (never {@code null}).
     */
    public List<OpenRouterServerTool> serverTools() {
        return serverTools == null ? List.of() : serverTools;
    }

    /**
     * The stop conditions for the server-tool agent loop ({@code stop_server_tools_when}),
     * empty when unset (never {@code null}).
     */
    public List<OpenRouterStopCondition> stopServerToolsWhen() {
        return stopServerToolsWhen == null ? List.of() : stopServerToolsWhen;
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
     * The frequency penalty ({@code frequency_penalty}, -2.0 to 2.0), or {@code null} when unset.
     */
    public Double frequencyPenalty() {
        return frequencyPenalty;
    }

    /**
     * The presence penalty ({@code presence_penalty}, -2.0 to 2.0), or {@code null} when unset.
     */
    public Double presencePenalty() {
        return presencePenalty;
    }

    /**
     * The repetition penalty ({@code repetition_penalty}, default 1.0), or {@code null} when unset.
     */
    public Double repetitionPenalty() {
        return repetitionPenalty;
    }

    /**
     * The deterministic sampling seed ({@code seed}), or {@code null} when unset.
     */
    public Integer seed() {
        return seed;
    }

    /**
     * The {@code min_p} sampling threshold, or {@code null} when unset.
     */
    public Double minP() {
        return minP;
    }

    /**
     * The {@code top_a} sampling threshold, or {@code null} when unset.
     */
    public Double topA() {
        return topA;
    }

    /**
     * The logit bias map ({@code logit_bias}: token id to bias, -100 to 100), or {@code null} when unset.
     */
    public Map<Integer, Double> logitBias() {
        return logitBias;
    }

    /**
     * Whether token log probabilities are returned ({@code logprobs}), or {@code null} when unset.
     */
    public Boolean logprobs() {
        return logprobs;
    }

    /**
     * The number of top token log probabilities to return ({@code top_logprobs}, 0-20),
     * or {@code null} when unset.
     */
    public Integer topLogprobs() {
        return topLogprobs;
    }

    /**
     * The fallback model candidates ({@code models}, tried in order when the primary
     * model is unavailable), empty when unset (never {@code null}).
     */
    public List<String> models() {
        return models == null ? List.of() : models;
    }

    /**
     * The app URL sent as the {@code HTTP-Referer} attribution header, or {@code null} when unset.
     */
    public String httpReferer() {
        return httpReferer;
    }

    /**
     * The display name sent as the {@code X-OpenRouter-Title} attribution header
     * (the API still accepts the legacy {@code X-Title} alias), or {@code null} when unset.
     */
    public String appTitle() {
        return appTitle;
    }

    /**
     * The marketplace categories sent as the {@code X-OpenRouter-Categories}
     * attribution header (at most 2), empty when unset (never {@code null}).
     */
    public List<String> appCategories() {
        return appCategories == null ? List.of() : appCategories;
    }

    /**
     * The metadata key/value pairs ({@code metadata}, up to 16 entries) attached to the
     * generation, empty when unset (never {@code null}).
     */
    public Map<String, String> metadata() {
        return metadata == null ? Map.of() : metadata;
    }

    /**
     * The stable per-end-user identifier ({@code user}) for abuse isolation, or {@code null} when unset.
     */
    public String user() {
        return user;
    }

    /**
     * The session identifier ({@code session_id}) that groups related requests and serves
     * as sticky-routing key to maximise prompt-cache hits, or {@code null} when unset.
     */
    public String sessionId() {
        return sessionId;
    }

    /**
     * Whether the response should include routing metadata under {@code openrouter_metadata}
     * (sent as the {@code X-OpenRouter-Metadata: enabled} header), or {@code null} when unset.
     */
    public Boolean metadataInResponse() {
        return metadataInResponse;
    }

    /**
     * The {@code provider.data_collection} setting ({@code "allow"} or {@code "deny"}),
     * or {@code null} when unset.
     */
    public String dataCollection() {
        return dataCollection;
    }

    /**
     * The provider slugs to skip ({@code provider.ignore}), empty when unset (never {@code null}).
     */
    public List<String> ignoreProviders() {
        return ignoreProviders == null ? List.of() : ignoreProviders;
    }

    /**
     * The only provider slugs allowed ({@code provider.only}), empty when unset (never {@code null}).
     */
    public List<String> onlyProviders() {
        return onlyProviders == null ? List.of() : onlyProviders;
    }

    /**
     * The prompt price cap ({@code provider.max_price.prompt}), or {@code null} when unset.
     */
    public String maxPricePrompt() {
        return maxPricePrompt;
    }

    /**
     * The completion price cap ({@code provider.max_price.completion}), or {@code null} when unset.
     */
    public String maxPriceCompletion() {
        return maxPriceCompletion;
    }

    /**
     * The image price cap ({@code provider.max_price.image}), or {@code null} when unset.
     */
    public String maxPriceImage() {
        return maxPriceImage;
    }

    /**
     * The audio price cap ({@code provider.max_price.audio}), or {@code null} when unset.
     */
    public String maxPriceAudio() {
        return maxPriceAudio;
    }

    /**
     * The accepted quantizations ({@code provider.quantizations}, e.g. {@code int4}, {@code fp8}),
     * empty when unset (never {@code null}).
     */
    public List<String> quantizations() {
        return quantizations == null ? List.of() : quantizations;
    }

    /**
     * The provider sort criterion ({@code provider.sort}: {@code "price"}, {@code "throughput"}
     * or {@code "latency"}), or {@code null} when unset.
     */
    public String sort() {
        return sort;
    }

    /**
     * The {@code provider.enforce_distillable_text} flag, or {@code null} when unset.
     */
    public Boolean enforceDistillableText() {
        return enforceDistillableText;
    }

    /**
     * The server-side plugins attached to this request ({@code plugins}),
     * empty when unset (never {@code null}).
     */
    public List<OpenRouterPlugin> plugins() {
        return plugins == null ? List.of() : plugins;
    }

    /**
     * The requested output modalities ({@code modalities}: {@code "text"},
     * {@code "image"}, {@code "audio"}), empty when unset (never {@code null}).
     */
    public List<String> modalities() {
        return modalities == null ? List.of() : modalities;
    }

    /**
     * The provider-specific image generation configuration ({@code image_config}),
     * or {@code null} when unset.
     */
    public OpenRouterImageConfig imageConfig() {
        return imageConfig;
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

    /**
     * Creates the follow-up request for the next turn of the tool-call loop by copying
     * every option of this request and replacing only the messages (and the streaming
     * flag / accumulator where needed).
     * <p>
     * This is the single place where options are carried into the next turn - it reads
     * the private fields of this class directly, so a newly added option cannot be
     * forgotten here without the compiler noticing the unused field in the copy.
     *
     * @param updatedMessages the accumulated conversation messages for the next turn
     * @param stream whether the follow-up request should stream
     * @param accumulator the streaming tool-call accumulator for the streaming path,
     *        or {@code null} for the non-streaming path
     * @return the follow-up request
     */
    OpenRouterChatCompletionRequest copyForNextTurn(
            List<JSONObject> updatedMessages,
            boolean stream,
            StreamingToolCallAccumulator accumulator
    ) {
        Builder b = new Builder(client);
        b.model = model;
        b.temperature = temperature;
        b.topK = topK;
        b.topP = topP;
        b.maxTokens = maxTokens;
        b.maxCompletionTokens = maxCompletionTokens;
        b.stopSequences.addAll(stopSequences);
        b.messages.addAll(updatedMessages);
        b.tools.addAll(tools);
        b.toolChoice = toolChoice;
        b.toolChoiceFunction = toolChoiceFunction;
        b.toolChoiceServerTool = toolChoiceServerTool;
        b.parallelToolCalls = parallelToolCalls;
        b.responseSchema = responseSchema;
        b.responseMimeType = responseMimeType;
        if (serverTools != null) {
            b.serverTools.addAll(serverTools);
        }
        if (stopServerToolsWhen != null) {
            b.stopServerToolsWhen.addAll(stopServerToolsWhen);
        }
        b.providers.addAll(providers);
        b.requireParameters = requireParameters;
        b.allowFallbacks = allowFallbacks;
        b.reasoningEffort = reasoningEffort;
        b.reasoningMaxTokens = reasoningMaxTokens;
        b.reasoningExclude = reasoningExclude;
        b.reasoningEnabled = reasoningEnabled;
        b.frequencyPenalty = frequencyPenalty;
        b.presencePenalty = presencePenalty;
        b.repetitionPenalty = repetitionPenalty;
        b.seed = seed;
        b.minP = minP;
        b.topA = topA;
        if (logitBias != null) {
            b.logitBias.putAll(logitBias);
        }
        b.logprobs = logprobs;
        b.topLogprobs = topLogprobs;
        if (models != null) {
            b.models.addAll(models);
        }
        b.httpReferer = httpReferer;
        b.appTitle = appTitle;
        if (appCategories != null) {
            b.appCategories.addAll(appCategories);
        }
        if (metadata != null) {
            b.metadata.putAll(metadata);
        }
        b.user = user;
        b.sessionId = sessionId;
        b.metadataInResponse = metadataInResponse;
        b.dataCollection = dataCollection;
        if (ignoreProviders != null) {
            b.ignoreProviders.addAll(ignoreProviders);
        }
        if (onlyProviders != null) {
            b.onlyProviders.addAll(onlyProviders);
        }
        b.maxPricePrompt = maxPricePrompt;
        b.maxPriceCompletion = maxPriceCompletion;
        b.maxPriceImage = maxPriceImage;
        b.maxPriceAudio = maxPriceAudio;
        if (quantizations != null) {
            b.quantizations.addAll(quantizations);
        }
        b.sort = sort;
        b.enforceDistillableText = enforceDistillableText;
        if (plugins != null) {
            b.plugins.addAll(plugins);
        }
        if (modalities != null) {
            b.modalities.addAll(modalities);
        }
        b.imageConfig = imageConfig;
        b.streamEnabled = stream;

        // Execution settings of the original request
        b.maxExecutionTimeInSeconds(getMaxExecutionTimeInSeconds());
        b.setCancelSupplier(getIsCanceledSupplier());
        if (hasCaptureOnSuccess()) {
            b.captureOnSuccess(getCaptureOnSuccess());
        }
        if (hasCaptureOnError()) {
            b.captureOnError(getCaptureOnError());
        }

        if (accumulator != null) {
            b.setRawJsonStreaming(accumulator);
        }
        return b.build();
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
        if (maxCompletionTokens != null) {
            root.put("max_completion_tokens", maxCompletionTokens);
        }
        if (stopSequences != null && !stopSequences.isEmpty()) {
            JSONArray stopArr = new JSONArray();
            for (String s : stopSequences) {
                stopArr.put(s);
            }
            root.put("stop", stopArr);
        }

        // Sampling parameters - each emitted only when explicitly set
        if (frequencyPenalty != null) {
            root.put("frequency_penalty", frequencyPenalty);
        }
        if (presencePenalty != null) {
            root.put("presence_penalty", presencePenalty);
        }
        if (repetitionPenalty != null) {
            root.put("repetition_penalty", repetitionPenalty);
        }
        if (seed != null) {
            root.put("seed", seed);
        }
        if (minP != null) {
            root.put("min_p", minP);
        }
        if (topA != null) {
            root.put("top_a", topA);
        }
        if (logitBias != null && !logitBias.isEmpty()) {
            JSONObject logitBiasObj = new JSONObject();
            for (Map.Entry<Integer, Double> e : logitBias.entrySet()) {
                logitBiasObj.put(String.valueOf(e.getKey()), e.getValue());
            }
            root.put("logit_bias", logitBiasObj);
        }
        if (logprobs != null) {
            root.put("logprobs", logprobs);
        }
        if (topLogprobs != null) {
            root.put("top_logprobs", topLogprobs);
        }

        // Fallback model list (OpenRouter-specific): candidates tried in order when
        // the primary model cannot serve the request. The single "model" field is
        // always emitted; per API semantics the models list acts as fallback
        // candidates for it.
        if (models != null && !models.isEmpty()) {
            JSONArray modelsArr = new JSONArray();
            for (String m : models) {
                modelsArr.put(m);
            }
            root.put("models", modelsArr);
        }

        // Observability (OpenRouter-specific) - each emitted only when explicitly set
        if (metadata != null && !metadata.isEmpty()) {
            JSONObject metadataObj = new JSONObject();
            for (Map.Entry<String, String> e : metadata.entrySet()) {
                metadataObj.put(e.getKey(), e.getValue());
            }
            root.put("metadata", metadataObj);
        }
        if (user != null) {
            root.put("user", user);
        }
        if (sessionId != null) {
            root.put("session_id", sessionId);
        }

        // Tools: client-side function tools and built-in OpenRouter server tools
        // share the same tools array. tool_choice / parallel_tool_calls are emitted
        // only when the array is non-empty.
        boolean hasAnyTools = !tools.isEmpty() || (serverTools != null && !serverTools.isEmpty());
        if (hasAnyTools) {
            JSONArray toolsArr = new JSONArray();
            for (OpenRouterToolDefinition def : tools) {
                toolsArr.put(def.toJson());
            }
            if (serverTools != null) {
                for (OpenRouterServerTool serverTool : serverTools) {
                    toolsArr.put(serverTool.toJson());
                }
            }
            root.put("tools", toolsArr);

            // tool_choice - precedence: named function form > server-tool form > plain string
            if (toolChoiceFunction != null) {
                JSONObject functionChoice = new JSONObject();
                functionChoice.put("type", "function");
                JSONObject function = new JSONObject();
                function.put("name", toolChoiceFunction);
                functionChoice.put("function", function);
                root.put("tool_choice", functionChoice);
            } else if (toolChoiceServerTool != null) {
                JSONObject serverToolChoice = new JSONObject();
                serverToolChoice.put("type", toolChoiceServerTool);
                root.put("tool_choice", serverToolChoice);
            } else if (toolChoice != null) {
                root.put("tool_choice", toolChoice);
            }

            // parallel_tool_calls
            if (parallelToolCalls != null) {
                root.put("parallel_tool_calls", parallelToolCalls);
            }
        }

        // Stop conditions for the server-tool agent loop (OpenRouter-specific).
        // Emitted only when at least one condition is set. Per the API, any
        // condition firing halts the loop (OR logic); when set it overrides
        // max_tool_calls.
        if (stopServerToolsWhen != null && !stopServerToolsWhen.isEmpty()) {
            JSONArray stopArr = new JSONArray();
            for (OpenRouterStopCondition condition : stopServerToolsWhen) {
                stopArr.put(condition.toJson());
            }
            root.put("stop_server_tools_when", stopArr);
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
        if (hasProviderOrder || requireParameters != null || allowFallbacks != null
                || dataCollection != null
                || (ignoreProviders != null && !ignoreProviders.isEmpty())
                || (onlyProviders != null && !onlyProviders.isEmpty())
                || maxPricePrompt != null || maxPriceCompletion != null
                || maxPriceImage != null || maxPriceAudio != null
                || (quantizations != null && !quantizations.isEmpty())
                || sort != null || enforceDistillableText != null) {
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
            if (dataCollection != null) {
                providerObj.put("data_collection", dataCollection);
            }
            if (ignoreProviders != null && !ignoreProviders.isEmpty()) {
                JSONArray ignoreArr = new JSONArray();
                for (String p : ignoreProviders) {
                    ignoreArr.put(p);
                }
                providerObj.put("ignore", ignoreArr);
            }
            if (onlyProviders != null && !onlyProviders.isEmpty()) {
                JSONArray onlyArr = new JSONArray();
                for (String p : onlyProviders) {
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
            if (sort != null) {
                providerObj.put("sort", sort);
            }
            if (enforceDistillableText != null) {
                providerObj.put("enforce_distillable_text", enforceDistillableText);
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

        // Plugins (OpenRouter-specific): server-side plugins such as web search.
        // Emitted only when at least one plugin is set.
        if (plugins != null && !plugins.isEmpty()) {
            JSONArray pluginsArr = new JSONArray();
            for (OpenRouterPlugin plugin : plugins) {
                pluginsArr.put(plugin.toJson());
            }
            root.put("plugins", pluginsArr);
        }

        // Output modalities (OpenRouter-specific): "text", "image", "audio".
        // Emitted only when explicitly set - a multimodal-output model needs
        // e.g. modalities("text", "image") to actually produce images.
        if (modalities != null && !modalities.isEmpty()) {
            JSONArray modalitiesArr = new JSONArray();
            for (String modality : modalities) {
                modalitiesArr.put(modality);
            }
            root.put("modalities", modalitiesArr);
        }

        // Image generation configuration (OpenRouter-specific, provider-specific keys).
        if (imageConfig != null) {
            root.put("image_config", imageConfig.toJson());
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
        private Integer maxCompletionTokens;
        private final List<String> stopSequences = new ArrayList<>();
        private final List<JSONObject> messages = new ArrayList<>();
        private final List<OpenRouterToolDefinition> tools = new ArrayList<>();
        private final List<OpenRouterServerTool> serverTools = new ArrayList<>();
        private final List<OpenRouterStopCondition> stopServerToolsWhen = new ArrayList<>();
        private String toolChoice;
        private String toolChoiceFunction;
        private String toolChoiceServerTool;
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
        private Double frequencyPenalty;
        private Double presencePenalty;
        private Double repetitionPenalty;
        private Integer seed;
        private Double minP;
        private Double topA;
        private final Map<Integer, Double> logitBias = new LinkedHashMap<>();
        private Boolean logprobs;
        private Integer topLogprobs;
        private final List<String> models = new ArrayList<>();
        private String httpReferer;
        private String appTitle;
        private final List<String> appCategories = new ArrayList<>();
        private final Map<String, String> metadata = new LinkedHashMap<>();
        private String user;
        private String sessionId;
        private Boolean metadataInResponse;
        private String dataCollection;
        private final List<String> ignoreProviders = new ArrayList<>();
        private final List<String> onlyProviders = new ArrayList<>();
        private String maxPricePrompt;
        private String maxPriceCompletion;
        private String maxPriceImage;
        private String maxPriceAudio;
        private final List<String> quantizations = new ArrayList<>();
        private String sort;
        private Boolean enforceDistillableText;
        private final List<OpenRouterPlugin> plugins = new ArrayList<>();
        private final List<String> modalities = new ArrayList<>();
        private OpenRouterImageConfig imageConfig;
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
         * Sets the {@code max_tokens} parameter (limit on generated tokens).
         *
         * @deprecated OpenRouter has deprecated {@code max_tokens} in favour of
         * {@code max_completion_tokens}; use {@link #maxCompletionTokens(Integer)}
         * instead. This method keeps working and still emits {@code max_tokens}.
         */
        @Deprecated
        public Builder maxOutputTokens(Integer m) {
            this.maxTokens = m;
            return this;
        }

        /**
         * Sets the {@code max_completion_tokens} parameter (maximum tokens in the completion),
         * the non-deprecated successor of {@code max_tokens}.
         * <p>
         * JSON field: {@code max_completion_tokens}. Default: unset (the key is not sent).
         * Trap: if both {@link #maxCompletionTokens(Integer)} and the deprecated
         * {@link #maxOutputTokens(Integer)} are set, both keys are emitted verbatim and the
         * OpenRouter API decides which one takes precedence - do not rely on that combination.
         *
         * @param m maximum number of tokens in the completion
         * @return This builder instance
         */
        public Builder maxCompletionTokens(Integer m) {
            this.maxCompletionTokens = m;
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
         * Adds built-in OpenRouter <em>server tools</em> to the {@code tools}
         * request array, alongside (and freely mixed with) the function tools
         * registered via {@link #addTool(OpenRouterToolDefinition)}.
         * <p>
         * JSON field: {@code tools} (shared array; a server tool is emitted as
         * {@code {"type":"<server-tool-type>"[, "parameters":{...}]}}). Default:
         * unset (no server tool is sent).
         * <p>
         * Server tools are executed by OpenRouter itself; their results reach the
         * model as {@code server_tool_calls} and no client-side callback is
         * needed. {@link OpenRouterWebSearchServerTool}, {@link OpenRouterWebFetchServerTool}
         * and {@link OpenRouterDatetimeServerTool} are the typed implementations,
         * {@link OpenRouterServerTool#of(String)} the verbatim escape hatch for
         * every other (or future) server-tool type.
         * <p>
         * Trap: while server tools are present, OpenRouter may run a server-side
         * agent loop that calls them repeatedly. Cap the loop with
         * {@link #stopServerToolsWhen(OpenRouterStopCondition...)} where cost
         * matters.
         *
         * @param serverTools the server tools to enable for this request
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/features/server-tools">OpenRouter server tools</a>
         */
        public Builder serverTools(OpenRouterServerTool... serverTools) {
            this.serverTools.addAll(Arrays.asList(serverTools));
            return this;
        }

        /**
         * List-based variant of {@link #serverTools(OpenRouterServerTool...)}.
         *
         * @param serverTools the server tools to enable for this request
         * @return This builder instance
         */
        public Builder serverTools(List<OpenRouterServerTool> serverTools) {
            this.serverTools.addAll(serverTools);
            return this;
        }

        /**
         * Adds a single server tool to the {@code tools} array
         * (see {@link #serverTools(OpenRouterServerTool...)}).
         *
         * @param serverTool the server tool to enable for this request
         * @return This builder instance
         */
        public Builder addServerTool(OpenRouterServerTool serverTool) {
            this.serverTools.add(serverTool);
            return this;
        }

        /**
         * Sets the stop conditions for the server-tool agent loop
         * ({@code stop_server_tools_when}). Any condition firing halts the loop
         * (OR logic); when set it overrides {@code max_tool_calls}. When a
         * condition fires while the model is still emitting tool calls, the
         * pending tool calls are executed and one final turn is made with tool
         * calls disabled, so the answer ends in natural language.
         * <p>
         * JSON field: {@code stop_server_tools_when} (array). Default: unset (the
         * key is not sent). Build conditions via {@link OpenRouterStopCondition#stepCountIs(int)},
         * {@link OpenRouterStopCondition#hasToolCall(String)}, {@link OpenRouterStopCondition#maxTokensUsed(long)},
         * {@link OpenRouterStopCondition#maxCost(double)}, {@link OpenRouterStopCondition#finishReasonIs(String)}
         * or {@link OpenRouterStopCondition#raw(JSONObject)}.
         *
         * @param conditions the stop conditions (at least one for the key to be emitted)
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/features/server-tools">OpenRouter server tools</a>
         */
        public Builder stopServerToolsWhen(OpenRouterStopCondition... conditions) {
            this.stopServerToolsWhen.addAll(Arrays.asList(conditions));
            return this;
        }

        /**
         * List-based variant of {@link #stopServerToolsWhen(OpenRouterStopCondition...)}.
         *
         * @param conditions the stop conditions
         * @return This builder instance
         */
        public Builder stopServerToolsWhen(List<OpenRouterStopCondition> conditions) {
            this.stopServerToolsWhen.addAll(conditions);
            return this;
        }

        /**
         * Adds a single stop condition to {@code stop_server_tools_when}
         * (see {@link #stopServerToolsWhen(OpenRouterStopCondition...)}).
         *
         * @param condition the stop condition to add
         * @return This builder instance
         */
        public Builder addStopServerToolsWhen(OpenRouterStopCondition condition) {
            this.stopServerToolsWhen.add(condition);
            return this;
        }

        /**
         * Controls how the model uses tools: "auto", "required", "none"
         */
        public Builder toolChoice(String choice) {
            this.toolChoice = choice;
            return this;
        }

        /**
         * Forces the model to call one specific tool - the named {@code tool_choice}
         * object form {@code {"type":"function","function":{"name":...}}}.
         * <p>
         * JSON field: {@code tool_choice} (object form). Default: unset (the key is not sent).
         * The string keywords ("auto", "required", "none") via {@link #toolChoice(String)}
         * remain available; if both are set, the named form wins. The key is emitted only
         * when tools are present, and the named tool must be among them.
         *
         * @param functionName name of the tool definition to force (must match a registered tool)
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/features/tool-calling">Tool calling</a>
         */
        public Builder toolChoiceFunction(String functionName) {
            this.toolChoiceFunction = functionName;
            return this;
        }

        /**
         * Forces a built-in OpenRouter <em>server tool</em> directly via the
         * server-tool {@code tool_choice} form {@code {"type":"<server-tool-type>"}} -
         * e.g. {@code "openrouter:web_search"}, {@code "web_search"} or
         * {@code "web_search_preview"} - instead of wrapping it in the function
         * form. The type string is accepted verbatim: the API schema is
         * free-form here on purpose, so future server-tool types work without a
         * library update.
         * <p>
         * JSON field: {@code tool_choice} (server-tool object form). Default:
         * unset (the key is not sent). The key is emitted only when tools are
         * present. Precedence when several forms are set: the named function
         * form ({@link #toolChoiceFunction(String)}) wins, then this
         * server-tool form, then the plain string keywords
         * ({@link #toolChoice(String)}).
         *
         * @param serverToolType the server-tool type to force (e.g. {@code "openrouter:web_search"})
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/features/server-tools">OpenRouter server tools</a>
         */
        public Builder toolChoiceServerTool(String serverToolType) {
            this.toolChoiceServerTool = serverToolType;
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
         * Sets {@code frequency_penalty} (-2.0 to 2.0): positive values reduce the
         * likelihood of repeating tokens the model has already used.
         * <p>
         * JSON field: {@code frequency_penalty}. Default: unset (the key is not sent).
         *
         * @param penalty penalty value between -2.0 and 2.0
         * @return This builder instance
         */
        public Builder frequencyPenalty(Double penalty) {
            this.frequencyPenalty = penalty;
            return this;
        }

        /**
         * Sets {@code presence_penalty} (-2.0 to 2.0): positive values encourage the
         * model to introduce new topics.
         * <p>
         * JSON field: {@code presence_penalty}. Default: unset (the key is not sent).
         *
         * @param penalty penalty value between -2.0 and 2.0
         * @return This builder instance
         */
        public Builder presencePenalty(Double penalty) {
            this.presencePenalty = penalty;
            return this;
        }

        /**
         * Sets {@code repetition_penalty} (default 1.0): values above 1.0 penalise
         * repeated tokens.
         * <p>
         * JSON field: {@code repetition_penalty}. Default: unset (the key is not sent).
         * Trap: not every provider supports this OpenRouter extension; pair it with
         * {@link #requireParameters(boolean)} where it matters.
         *
         * @param penalty penalty value, typically around 1.0
         * @return This builder instance
         */
        public Builder repetitionPenalty(Double penalty) {
            this.repetitionPenalty = penalty;
            return this;
        }

        /**
         * Sets {@code seed} for (best-effort) deterministic sampling.
         * <p>
         * JSON field: {@code seed}. Default: unset (the key is not sent).
         *
         * @param seed the sampling seed
         * @return This builder instance
         */
        public Builder seed(Integer seed) {
            this.seed = seed;
            return this;
        }

        /**
         * Sets {@code min_p}: minimum probability for a token to be considered,
         * relative to the most likely token.
         * <p>
         * JSON field: {@code min_p}. Default: unset (the key is not sent).
         * Trap: not every provider supports this OpenRouter extension; pair it with
         * {@link #requireParameters(boolean)} where it matters.
         *
         * @param minP threshold between 0.0 and 1.0
         * @return This builder instance
         */
        public Builder minP(Double minP) {
            this.minP = minP;
            return this;
        }

        /**
         * Sets {@code top_a}: an alternative sampling threshold that scales with the
         * probability of the most likely token.
         * <p>
         * JSON field: {@code top_a}. Default: unset (the key is not sent).
         * Trap: not every provider supports this OpenRouter extension; pair it with
         * {@link #requireParameters(boolean)} where it matters.
         *
         * @param topA threshold value
         * @return This builder instance
         */
        public Builder topA(Double topA) {
            this.topA = topA;
            return this;
        }

        /**
         * Sets {@code logit_bias}: biases the likelihood of specific tokens
         * (token id to bias, -100 to 100; -100 bans the token).
         * <p>
         * JSON field: {@code logit_bias}. Default: unset (the key is not sent).
         * Calling this replaces any previously registered biases.
         *
         * @param bias map of token id to bias value
         * @return This builder instance
         */
        public Builder logitBias(Map<Integer, Double> bias) {
            this.logitBias.clear();
            if (bias != null) {
                this.logitBias.putAll(bias);
            }
            return this;
        }

        /**
         * Adds a single {@code logit_bias} entry (token id to bias, -100 to 100).
         *
         * @param tokenId the token id to bias
         * @param bias the bias value between -100 and 100
         * @return This builder instance
         * @see #logitBias(Map)
         */
        public Builder addLogitBias(Integer tokenId, Double bias) {
            this.logitBias.put(tokenId, bias);
            return this;
        }

        /**
         * Sets {@code logprobs}: when {@code true}, the response includes the log
         * probabilities of the output tokens.
         * <p>
         * JSON field: {@code logprobs}. Default: unset (the key is not sent).
         *
         * @param logprobs true to request token log probabilities
         * @return This builder instance
         */
        public Builder logprobs(Boolean logprobs) {
            this.logprobs = logprobs;
            return this;
        }

        /**
         * Sets {@code top_logprobs} (0-20): the number of most likely tokens for which
         * log probabilities are returned. Requires {@link #logprobs(Boolean)} to be true.
         * <p>
         * JSON field: {@code top_logprobs}. Default: unset (the key is not sent).
         *
         * @param topLogprobs number of top tokens (0-20)
         * @return This builder instance
         */
        public Builder topLogprobs(Integer topLogprobs) {
            this.topLogprobs = topLogprobs;
            return this;
        }

        /**
         * Sets the fallback model candidates ({@code models}) that OpenRouter tries in
         * order when the primary {@link #model(String)} cannot serve the request.
         * <p>
         * JSON field: {@code models}. Default: unset (the key is not sent).
         * Trap: the primary {@code model} key is always emitted alongside; per the API
         * semantics the {@code models} list acts as fallback candidates for {@code model},
         * it does not replace it. Passing an empty list removes previously registered
         * candidates and emits nothing.
         *
         * @param slugs the fallback model slugs in preference order
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/routing/model-fallbacks">Model fallbacks</a>
         */
        public Builder models(String... slugs) {
            return models(Arrays.asList(slugs));
        }

        /**
         * List-based variant of {@link #models(String...)}.
         * Passing an empty list removes previously registered candidates and emits nothing.
         *
         * @param slugs the fallback model slugs in preference order
         * @return This builder instance
         */
        public Builder models(List<String> slugs) {
            this.models.clear();
            if (slugs != null) {
                this.models.addAll(slugs);
            }
            return this;
        }

        /**
         * Sets the app URL sent as the {@code HTTP-Referer} attribution header - the
         * primary identifier for the OpenRouter leaderboards.
         * <p>
         * Header: {@code HTTP-Referer}. Default: unset, unless a client-level attribution
         * is configured via {@code OpenRouterClient.appAttribution(...)}; the per-request
         * value wins for this request.
         *
         * @param url the app URL
         * @return This builder instance
         */
        public Builder httpReferer(String url) {
            this.httpReferer = url;
            return this;
        }

        /**
         * Sets the display name sent as the {@code X-OpenRouter-Title} attribution header,
         * shown in rankings and analytics.
         * <p>
         * Header: {@code X-OpenRouter-Title}. Default: unset, unless a client-level
         * attribution is configured via {@code OpenRouterClient.appAttribution(...)};
         * the per-request value wins for this request. (The API still accepts the legacy
         * {@code X-Title} header as a documented alias, but this library always sends
         * {@code X-OpenRouter-Title}.)
         *
         * @param title the app display name
         * @return This builder instance
         */
        public Builder appTitle(String title) {
            this.appTitle = title;
            return this;
        }

        /**
         * Replaces the marketplace categories sent as the {@code X-OpenRouter-Categories}
         * attribution header.
         * <p>
         * Header: {@code X-OpenRouter-Categories}. Default: unset, unless a client-level
         * attribution is configured via {@code OpenRouterClient.appAttribution(...)}; the
         * per-request value wins for this request.
         * Trap: OpenRouter accepts at most 2 categories per request - more throw an
         * {@link IllegalArgumentException}.
         *
         * @param categories the marketplace categories (at most 2)
         * @return This builder instance
         */
        public Builder appCategories(String... categories) {
            return appCategories(Arrays.asList(categories));
        }

        /**
         * List-based variant of {@link #appCategories(String...)}.
         * Trap: OpenRouter accepts at most 2 categories per request - more throw an
         * {@link IllegalArgumentException}.
         *
         * @param categories the marketplace categories (at most 2)
         * @return This builder instance
         */
        public Builder appCategories(List<String> categories) {
            if (categories != null && categories.size() > OpenRouterAppAttribution.MAX_CATEGORIES) {
                throw new IllegalArgumentException(
                        "At most " + OpenRouterAppAttribution.MAX_CATEGORIES
                                + " categories are allowed per request, got " + categories.size());
            }
            this.appCategories.clear();
            if (categories != null) {
                this.appCategories.addAll(categories);
            }
            return this;
        }

        /**
         * Sets {@code metadata}: up to 16 string key/value pairs (keys at most 64 chars,
         * values at most 512 chars) attached to the generation and returned in
         * generation listings.
         * <p>
         * JSON field: {@code metadata}. Default: unset (the key is not sent).
         * Trap: OpenRouter rejects requests whose metadata exceeds the limits; this
         * builder validates the limits up front and throws an
         * {@link IllegalArgumentException} instead of letting the API fail later.
         *
         * @param keyValues the metadata entries
         * @return This builder instance
         */
        public Builder metadata(Map<String, String> keyValues) {
            validateMetadata(keyValues);
            this.metadata.clear();
            if (keyValues != null) {
                this.metadata.putAll(keyValues);
            }
            return this;
        }

        /**
         * Adds a single {@code metadata} entry (see {@link #metadata(Map)} for the limits).
         *
         * @param key the metadata key (at most 64 chars)
         * @param value the metadata value (at most 512 chars)
         * @return This builder instance
         */
        public Builder addMetadata(String key, String value) {
            validateMetadata(Map.of(key, value));
            if (this.metadata.size() >= METADATA_MAX_ENTRIES) {
                throw new IllegalArgumentException(
                        "At most " + METADATA_MAX_ENTRIES + " metadata entries are allowed");
            }
            this.metadata.put(key, value);
            return this;
        }

        /**
         * Sets {@code user}: a stable identifier of the end user this request belongs to,
         * used by OpenRouter for abuse isolation.
         * <p>
         * JSON field: {@code user}. Default: unset (the key is not sent).
         *
         * @param user the stable per-end-user identifier
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/limits/usage-accounting">Usage accounting</a>
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets {@code session_id}: groups related requests into one session. OpenRouter
         * uses it as sticky-routing key to maximise prompt-cache hits.
         * <p>
         * JSON field: {@code session_id}. Default: unset (the key is not sent).
         * Additionally, the {@code x-session-id} header is set to the same value;
         * per the API specification the body field takes precedence when both are present.
         *
         * @param sessionId the session identifier (the API accepts at most 256 chars;
         *        not validated by the builder)
         * @return This builder instance
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Opts in to receiving routing metadata on the response: when {@code true}, the
         * {@code X-OpenRouter-Metadata: enabled} header is sent and the response may
         * carry an {@code openrouter_metadata} object. (The legacy
         * {@code X-OpenRouter-Experimental-Metadata} header remains an accepted alias
         * in the API, but this library always sends the current one.)
         * <p>
         * Header: {@code X-OpenRouter-Metadata}. Default: unset (the header is not sent).
         *
         * @param enabled true to opt in
         * @return This builder instance
         */
        public Builder metadataInResponse(boolean enabled) {
            this.metadataInResponse = enabled;
            return this;
        }

        /**
         * Sets {@code provider.data_collection}: whether the request may be routed to
         * providers that train on prompts/completions.
         * <p>
         * JSON field: {@code provider.data_collection}. Default: unset (the key is not sent).
         * Documented values: {@code "allow"} and {@code "deny"}; {@code "deny"} restricts
         * routing to providers that do not train on the data - this can reduce the number
         * of eligible endpoints noticeably.
         *
         * @param policy {@code "allow"} or {@code "deny"}
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/routing/provider-selection">Provider selection</a>
         */
        public Builder dataCollection(String policy) {
            this.dataCollection = policy;
            return this;
        }

        /**
         * Replaces {@code provider.ignore}: provider slugs that must not serve the request.
         * <p>
         * JSON field: {@code provider.ignore}. Default: unset (the key is not sent).
         * Passing an empty list removes previously registered slugs and emits nothing.
         *
         * @param slugs provider slugs to skip
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/routing/provider-selection">Provider selection</a>
         */
        public Builder ignoreProviders(String... slugs) {
            return ignoreProviders(Arrays.asList(slugs));
        }

        /**
         * List-based variant of {@link #ignoreProviders(String...)}.
         * Passing an empty list removes previously registered slugs and emits nothing.
         *
         * @param slugs provider slugs to skip
         * @return This builder instance
         */
        public Builder ignoreProviders(List<String> slugs) {
            this.ignoreProviders.clear();
            if (slugs != null) {
                this.ignoreProviders.addAll(slugs);
            }
            return this;
        }

        /**
         * Adds a provider slug to {@code provider.ignore} (see {@link #ignoreProviders(String...)}).
         *
         * @param slug the provider slug to skip
         * @return This builder instance
         */
        public Builder addIgnoreProvider(String slug) {
            this.ignoreProviders.add(slug);
            return this;
        }

        /**
         * Replaces {@code provider.only}: restricts routing to exactly these provider slugs.
         * <p>
         * JSON field: {@code provider.only}. Default: unset (the key is not sent).
         * Trap: unlike the order list ({@link #provider(String...)}), no other provider
         * may serve the request; if none of the listed providers can, the call fails.
         * Passing an empty list removes previously registered slugs and emits nothing.
         *
         * @param slugs the only providers allowed to serve the request
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/routing/provider-selection">Provider selection</a>
         */
        public Builder onlyProviders(String... slugs) {
            return onlyProviders(Arrays.asList(slugs));
        }

        /**
         * List-based variant of {@link #onlyProviders(String...)}.
         * Trap: unlike the order list ({@link #provider(String...)}), no other provider
         * may serve the request; if none of the listed providers can, the call fails.
         * Passing an empty list removes previously registered slugs and emits nothing.
         *
         * @param slugs the only providers allowed to serve the request
         * @return This builder instance
         */
        public Builder onlyProviders(List<String> slugs) {
            this.onlyProviders.clear();
            if (slugs != null) {
                this.onlyProviders.addAll(slugs);
            }
            return this;
        }

        /**
         * Adds a provider slug to {@code provider.only} (see {@link #onlyProviders(String...)}).
         *
         * @param slug a provider slug allowed to serve the request
         * @return This builder instance
         */
        public Builder addOnlyProvider(String slug) {
            this.onlyProviders.add(slug);
            return this;
        }

        /**
         * Sets {@code provider.max_price} caps for prompt and completion tokens. Prices are
         * strings of the token price in USD per million tokens (e.g. {@code "0.5"}).
         * <p>
         * JSON field: {@code provider.max_price}. Default: unset (the key is not sent).
         * The two-argument form leaves {@code image}/{@code audio} caps unset.
         *
         * @param prompt maximum prompt price, or {@code null} to leave it unset
         * @param completion maximum completion price, or {@code null} to leave it unset
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/routing/provider-selection">Provider selection</a>
         */
        public Builder maxPrice(String prompt, String completion) {
            return maxPrice(prompt, completion, null, null);
        }

        /**
         * Sets {@code provider.max_price} caps for all four price categories.
         *
         * @param prompt maximum prompt price, or {@code null} to leave it unset
         * @param completion maximum completion price, or {@code null} to leave it unset
         * @param image maximum image price, or {@code null} to leave it unset
         * @param audio maximum audio price, or {@code null} to leave it unset
         * @return This builder instance
         */
        public Builder maxPrice(String prompt, String completion, String image, String audio) {
            this.maxPricePrompt = prompt;
            this.maxPriceCompletion = completion;
            this.maxPriceImage = image;
            this.maxPriceAudio = audio;
            return this;
        }

        /**
         * Replaces {@code provider.quantizations}: the quantization levels the request may
         * be served with (e.g. {@code int4}, {@code fp8}, {@code fp16}, {@code bf16},
         * {@code fp8_int8}).
         * <p>
         * JSON field: {@code provider.quantizations}. Default: unset (the key is not sent).
         * Passing an empty list removes previously registered levels and emits nothing.
         *
         * @param levels the accepted quantization levels
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/routing/provider-selection">Provider selection</a>
         */
        public Builder quantizations(String... levels) {
            return quantizations(Arrays.asList(levels));
        }

        /**
         * List-based variant of {@link #quantizations(String...)}.
         * Passing an empty list removes previously registered levels and emits nothing.
         *
         * @param levels the accepted quantization levels
         * @return This builder instance
         */
        public Builder quantizations(List<String> levels) {
            this.quantizations.clear();
            if (levels != null) {
                this.quantizations.addAll(levels);
            }
            return this;
        }

        /**
         * Sets {@code provider.sort}: the criterion providers are sorted by when routing.
         * <p>
         * JSON field: {@code provider.sort}. Default: unset (the key is not sent).
         * Documented values: {@code "price"}, {@code "throughput"} and {@code "latency"}.
         *
         * @param criterion one of the documented sort criteria
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/routing/provider-selection">Provider selection</a>
         */
        public Builder sort(String criterion) {
            this.sort = criterion;
            return this;
        }

        /**
         * Sets {@code provider.enforce_distillable_text}: when {@code true}, only endpoints
         * that support distillable text output are eligible.
         * <p>
         * JSON field: {@code provider.enforce_distillable_text}. Default: unset (the key
         * is not sent).
         *
         * @param enforce true to restrict routing to distillable-text endpoints
         * @return This builder instance
         */
        public Builder enforceDistillableText(Boolean enforce) {
            this.enforceDistillableText = enforce;
            return this;
        }

        private static String truncateForMessage(String value) {
            return value.length() <= 20 ? value : value.substring(0, 20) + "...";
        }

        /**
         * Replaces the server-side plugins attached to this request ({@code plugins}).
         * <p>
         * JSON field: {@code plugins} (array). Default: unset (the key is not sent).
         * Each plugin is emitted with its discriminator {@code id} plus its
         * configured fields; {@link OpenRouterWebSearchPlugin} is the typed
         * implementation of the {@code web} plugin, {@link OpenRouterPlugin#of(String)}
         * creates a generic escape hatch for every other plugin id.
         * <p>
         * Trap: web search results are delivered to the model as tool calls
         * ({@code server_tool_calls}) executed server-side by OpenRouter. Combining
         * plugins with client-side {@code tools} / {@code tool_choice} means two
         * independent tool-call flows in one request.
         * <p>
         * Passing an empty list removes previously registered plugins and emits nothing.
         *
         * @param plugins the plugins to enable for this request
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/features/plugins">OpenRouter plugins</a>
         */
        public Builder plugins(OpenRouterPlugin... plugins) {
            return plugins(Arrays.asList(plugins));
        }

        /**
         * List-based variant of {@link #plugins(OpenRouterPlugin...)}.
         * Passing an empty list removes previously registered plugins and emits nothing.
         *
         * @param plugins the plugins to enable for this request
         * @return This builder instance
         */
        public Builder plugins(List<OpenRouterPlugin> plugins) {
            this.plugins.clear();
            if (plugins != null) {
                this.plugins.addAll(plugins);
            }
            return this;
        }

        /**
         * Adds a single plugin to the {@code plugins} array
         * (see {@link #plugins(OpenRouterPlugin...)}).
         *
         * @param plugin the plugin to enable for this request
         * @return This builder instance
         */
        public Builder addPlugin(OpenRouterPlugin plugin) {
            this.plugins.add(plugin);
            return this;
        }

        /**
         * Sets the requested output modalities ({@code modalities}).
         * <p>
         * JSON field: {@code modalities} (array of {@code "text"}, {@code "image"},
         * {@code "audio"}). Default: unset (the key is not sent; the provider's
         * default - text only - applies).
         * <p>
         * Trap: multimodal-output models (e.g. image-generating Gemini models) do
         * not produce images unless {@code "image"} is requested here; combine with
         * {@link #imageConfig(OpenRouterImageConfig)} for image count, aspect ratio
         * or resolution. Not every provider supports non-text output - pair with
         * {@link #requireParameters(boolean)} where it matters.
         * <p>
         * Passing an empty list removes previously registered modalities and emits nothing.
         *
         * @param modalities the output modalities to request
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/overview/multimodal/image-generation">Image generation</a>
         */
        public Builder modalities(String... modalities) {
            return modalities(Arrays.asList(modalities));
        }

        /**
         * List-based variant of {@link #modalities(String...)}.
         * Passing an empty list removes previously registered modalities and emits nothing.
         *
         * @param modalities the output modalities to request
         * @return This builder instance
         */
        public Builder modalities(List<String> modalities) {
            this.modalities.clear();
            if (modalities != null) {
                this.modalities.addAll(modalities);
            }
            return this;
        }

        /**
         * Adds a single output modality to {@code modalities}
         * (see {@link #modalities(String...)}).
         *
         * @param modality one of {@code "text"}, {@code "image"}, {@code "audio"}
         * @return This builder instance
         */
        public Builder addModality(String modality) {
            this.modalities.add(modality);
            return this;
        }

        /**
         * Sets the provider-specific image generation configuration
         * ({@code image_config}): image count, aspect ratio, resolution and similar
         * provider-specific options.
         * <p>
         * JSON field: {@code image_config}. Default: unset (the key is not sent).
         * <p>
         * Trap: the key is only meaningful for multimodal-output models and must be
         * combined with {@link #modalities(String...)} containing {@code "image"} -
         * without that, providers ignore it.
         *
         * @param config the image generation configuration
         * @return This builder instance
         * @see <a href="https://openrouter.ai/docs/guides/overview/multimodal/image-generation">Image generation</a>
         */
        public Builder imageConfig(OpenRouterImageConfig config) {
            this.imageConfig = config;
            return this;
        }

        private static void validateMetadata(Map<String, String> keyValues) {
            if (keyValues == null) {
                return;
            }
            if (keyValues.size() > METADATA_MAX_ENTRIES) {
                throw new IllegalArgumentException(
                        "At most " + METADATA_MAX_ENTRIES + " metadata entries are allowed, got "
                                + keyValues.size());
            }
            for (Map.Entry<String, String> e : keyValues.entrySet()) {
                if (e.getKey() != null && e.getKey().length() > METADATA_MAX_KEY_LENGTH) {
                    throw new IllegalArgumentException(
                            "Metadata key '" + truncateForMessage(e.getKey()) + "' exceeds "
                                    + METADATA_MAX_KEY_LENGTH + " characters");
                }
                if (e.getValue() != null && e.getValue().length() > METADATA_MAX_VALUE_LENGTH) {
                    throw new IllegalArgumentException(
                            "Metadata value of key '" + truncateForMessage(e.getKey()) + "' exceeds "
                                    + METADATA_MAX_VALUE_LENGTH + " characters");
                }
            }
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

            OpenRouterChatCompletionRequest request = new OpenRouterChatCompletionRequest(
                    this,
                    client,
                    model,
                    temperature,
                    topK,
                    topP,
                    maxTokens,
                    maxCompletionTokens,
                    List.copyOf(stopSequences),
                    List.copyOf(messages),
                    List.copyOf(tools),
                    toolChoice,
                    toolChoiceFunction,
                    toolChoiceServerTool,
                    parallelToolCalls,
                    responseSchema,
                    responseMimeType,
                    serverTools.isEmpty() ? null : List.copyOf(serverTools),
                    stopServerToolsWhen.isEmpty() ? null : List.copyOf(stopServerToolsWhen),
                    List.copyOf(providers),
                    requireParameters,
                    allowFallbacks,
                    reasoningEffort,
                    reasoningMaxTokens,
                    reasoningExclude,
                    reasoningEnabled,
                    frequencyPenalty,
                    presencePenalty,
                    repetitionPenalty,
                    seed,
                    minP,
                    topA,
                    logitBias.isEmpty() ? null : Map.copyOf(logitBias),
                    logprobs,
                    topLogprobs,
                    models.isEmpty() ? null : List.copyOf(models),
                    httpReferer,
                    appTitle,
                    appCategories.isEmpty() ? null : List.copyOf(appCategories),
                    metadata.isEmpty() ? null : Map.copyOf(metadata),
                    user,
                    sessionId,
                    metadataInResponse,
                    dataCollection,
                    ignoreProviders.isEmpty() ? null : List.copyOf(ignoreProviders),
                    onlyProviders.isEmpty() ? null : List.copyOf(onlyProviders),
                    maxPricePrompt,
                    maxPriceCompletion,
                    maxPriceImage,
                    maxPriceAudio,
                    quantizations.isEmpty() ? null : List.copyOf(quantizations),
                    sort,
                    enforceDistillableText,
                    plugins.isEmpty() ? null : List.copyOf(plugins),
                    modalities.isEmpty() ? null : List.copyOf(modalities),
                    imageConfig,
                    shouldStream
            );
            applyHeaders(request);
            return request;
        }

        /**
         * Applies the derived HTTP headers to a freshly built request:
         * app attribution (per-request values win over the client-level default),
         * {@code x-session-id} when a session identifier is configured, and the
         * opt-in {@code X-OpenRouter-Metadata} header.
         */
        private void applyHeaders(OpenRouterChatCompletionRequest request) {
            OpenRouterAppAttribution clientAttribution =
                    client != null ? client.appAttribution() : null;

            String referer = httpReferer != null ? httpReferer
                    : clientAttribution != null ? clientAttribution.httpReferer() : null;
            if (referer != null) {
                request.setHeader(HEADER_HTTP_REFERER, referer);
            }

            String title = appTitle != null ? appTitle
                    : clientAttribution != null ? clientAttribution.appTitle() : null;
            if (title != null) {
                request.setHeader(HEADER_APP_TITLE, title);
            }

            List<String> categories = !appCategories.isEmpty() ? appCategories
                    : clientAttribution != null ? clientAttribution.categories() : List.of();
            if (!categories.isEmpty()) {
                request.setHeader(HEADER_APP_CATEGORIES, String.join(",", categories));
            }

            if (sessionId != null) {
                request.setHeader(HEADER_SESSION_ID, sessionId);
            }

            if (Boolean.TRUE.equals(metadataInResponse)) {
                request.setHeader(HEADER_METADATA, "enabled");
            }
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
