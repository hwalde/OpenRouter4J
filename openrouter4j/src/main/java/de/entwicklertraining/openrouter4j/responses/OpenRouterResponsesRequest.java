package de.entwicklertraining.openrouter4j.responses;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.api.base.streaming.SSEStreamProcessor;
import de.entwicklertraining.api.base.streaming.StreamingFormat;
import de.entwicklertraining.api.base.streaming.StreamingInfo;
import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterImageConfig;
import de.entwicklertraining.openrouter4j.OpenRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterPercentileCutoffs;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterStopCondition;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A request to the OpenAI Responses API on OpenRouter:
 * POST https://openrouter.ai/api/v1/responses
 *
 * <p>The Responses API is the successor surface for several features that
 * only exist there (e.g. full {@code openrouter:tool_search} support,
 * {@code openrouter:apply_patch}, subagent function inheritance). Required
 * by the API: a {@code model} (or {@code models}) and an {@code input} -
 * {@code build()} rejects the request without both.
 *
 * <p>Streaming: with the inherited {@code stream(StreamingResponseHandler)},
 * the body carries {@code stream: true} and the Responses-API SSE events
 * arrive as raw JSON strings on {@code onData} - the event {@code type}
 * field distinguishes {@code response.created}, {@code response.in_progress},
 * {@code response.output_item.added}, {@code response.output_text.delta},
 * {@code response.output_text.done}, {@code response.content_part.added},
 * {@code response.function_call_arguments.delta},
 * {@code response.output_item.done}, {@code response.completed},
 * {@code response.failed}, {@code response.incomplete} and {@code error},
 * terminated by the {@code [DONE]} sentinel.
 *
 * <p>Traps: the API is stateless - {@code previous_response_id} is not
 * supported (rejected with 400); send the full conversation history in
 * {@code input} instead. The typed surface covers the documented schema
 * subset below; unknown or future fields travel verbatim through
 * {@link Builder#addInputItem(JSONObject)} (input items) and
 * {@link Builder#addTool(JSONObject)} (tools, e.g. server tools such as
 * {@code openrouter:web_search}).
 *
 * <p>An unset option never appears in the JSON; the {@code provider} object
 * is emitted only when at least one provider option is set.
 */
public final class OpenRouterResponsesRequest extends OpenRouterRequest<OpenRouterResponsesResponse> {

    private final OpenRouterClient client;
    private final String inputText;
    private final List<JSONObject> inputItems;
    private final String model;
    private final List<String> models;
    private final String instructions;
    private final Integer maxOutputTokens;
    private final Integer maxToolCalls;
    private final Double temperature;
    private final Double topP;
    private final Integer topK;
    private final Integer topLogprobs;
    private final Double frequencyPenalty;
    private final Double presencePenalty;
    private final List<JSONObject> tools;
    private final String toolChoice;
    private final String toolChoiceFunctionName;
    private final String toolChoiceAllowedToolsMode;
    private final List<JSONObject> toolChoiceAllowedTools;
    private final String toolChoiceType;
    private final Boolean parallelToolCalls;
    private final String reasoningEffort;
    private final Integer reasoningMaxTokens;
    private final String reasoningSummary;
    private final Boolean reasoningEnabled;
    private final List<String> modalities;
    private final List<String> include;
    private final Boolean background;
    private final Boolean store;
    private final JSONObject metadata;
    private final String serviceTier;
    private final String sessionId;
    private final String safetyIdentifier;
    private final String user;
    private final String promptCacheKey;
    private final String promptCacheOptionsMode;
    private final String promptCacheOptionsTtl;
    private final String truncation;
    private final String cacheControlTtl;
    private final List<OpenRouterPlugin> plugins;
    private final OpenRouterTraceConfig trace;
    private final List<OpenRouterStopCondition> stopServerToolsWhen;
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
    private final Boolean stream;
    private final String promptId;
    private final JSONObject promptVariables;
    private final OpenRouterImageConfig imageConfig;
    private final Boolean debugEchoUpstreamBody;
    private final JSONObject textFormat;
    private final String textVerbosity;
    private final JSONObject textVerbatim;

    private OpenRouterResponsesRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.inputText = builder.inputText;
        this.inputItems = builder.inputItems == null ? null : List.copyOf(builder.inputItems);
        this.model = builder.model;
        this.models = builder.models == null ? null : List.copyOf(builder.models);
        this.instructions = builder.instructions;
        this.maxOutputTokens = builder.maxOutputTokens;
        this.maxToolCalls = builder.maxToolCalls;
        this.temperature = builder.temperature;
        this.topP = builder.topP;
        this.topK = builder.topK;
        this.topLogprobs = builder.topLogprobs;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.presencePenalty = builder.presencePenalty;
        this.tools = builder.tools == null ? null : List.copyOf(builder.tools);
        this.toolChoice = builder.toolChoice;
        this.toolChoiceFunctionName = builder.toolChoiceFunctionName;
        this.toolChoiceAllowedToolsMode = builder.toolChoiceAllowedToolsMode;
        this.toolChoiceAllowedTools = builder.toolChoiceAllowedTools == null
                ? null : List.copyOf(builder.toolChoiceAllowedTools);
        this.toolChoiceType = builder.toolChoiceType;
        this.parallelToolCalls = builder.parallelToolCalls;
        this.reasoningEffort = builder.reasoningEffort;
        this.reasoningMaxTokens = builder.reasoningMaxTokens;
        this.reasoningSummary = builder.reasoningSummary;
        this.reasoningEnabled = builder.reasoningEnabled;
        this.modalities = builder.modalities == null ? null : List.copyOf(builder.modalities);
        this.include = builder.include == null ? null : List.copyOf(builder.include);
        this.background = builder.background;
        this.store = builder.store;
        this.metadata = builder.metadata;
        this.serviceTier = builder.serviceTier;
        this.sessionId = builder.sessionId;
        this.safetyIdentifier = builder.safetyIdentifier;
        this.user = builder.user;
        this.promptCacheKey = builder.promptCacheKey;
        this.promptCacheOptionsMode = builder.promptCacheOptionsMode;
        this.promptCacheOptionsTtl = builder.promptCacheOptionsTtl;
        this.truncation = builder.truncation;
        this.cacheControlTtl = builder.cacheControlTtl;
        this.plugins = builder.plugins == null ? null : List.copyOf(builder.plugins);
        this.trace = builder.trace;
        this.stopServerToolsWhen = builder.stopServerToolsWhen == null ? null : List.copyOf(builder.stopServerToolsWhen);
        this.providerOrder = builder.providerOrder == null ? null : List.copyOf(builder.providerOrder);
        this.providerOnly = builder.providerOnly == null ? null : List.copyOf(builder.providerOnly);
        this.providerIgnore = builder.providerIgnore == null ? null : List.copyOf(builder.providerIgnore);
        this.requireParameters = builder.requireParameters;
        this.allowFallbacks = builder.allowFallbacks;
        this.dataCollection = builder.dataCollection;
        this.quantizations = builder.quantizations == null || builder.quantizations.isEmpty() ? null : List.copyOf(builder.quantizations);
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
        builder.customHeaders.forEach(this::setHeader);
        this.stream = builder.streamRequested();
        this.promptId = builder.promptId;
        this.promptVariables = builder.promptVariables == null ? null : new JSONObject(builder.promptVariables.toString());
        this.imageConfig = builder.imageConfig;
        this.debugEchoUpstreamBody = builder.debugEchoUpstreamBody;
        this.textFormat = builder.textFormat == null ? null : new JSONObject(builder.textFormat.toString());
        this.textVerbosity = builder.textVerbosity;
        this.textVerbatim = builder.textVerbatim == null ? null : new JSONObject(builder.textVerbatim.toString());
    }

    /** @return the plain-string input, or {@code null} when the input was built with items */
    public String inputText() {
        return inputText;
    }

    /** @return the verbatim input items, empty when the input is a plain string or unset */
    public List<JSONObject> inputItems() {
        return inputItems == null ? List.of() : inputItems;
    }

    /** @return the JSON field {@code model}, or {@code null} when unset */
    public String model() {
        return model;
    }

    /** @return the JSON field {@code models}, empty when unset */
    public List<String> models() {
        return models == null ? List.of() : models;
    }

    /** @return whether the request body carries {@code stream: true} */
    public boolean streamRequested() {
        return stream;
    }

    /**
     * The {@code prompt.id} of the stored prompt template to run this request
     * through, or {@code null} when unset (the key is not sent).
     *
     * @return the stored prompt template id, or {@code null}
     */
    public String promptId() {
        return promptId;
    }

    /**
     * The {@code prompt.variables} map for the stored prompt template, or
     * {@code null} when none were set.
     *
     * @return the prompt template variables, or {@code null}
     */
    public JSONObject promptVariables() {
        return promptVariables;
    }

    /**
     * The provider-specific image generation configuration
     * ({@code image_config}), or {@code null} when unset (the key is not
     * sent). The same type the chat-completions request uses.
     *
     * @return the image configuration, or {@code null}
     */
    public OpenRouterImageConfig imageConfig() {
        return imageConfig;
    }

    /**
     * The {@code debug.echo_upstream_body} flag, or {@code null} when unset
     * (the key is not sent).
     *
     * @return the debug echo flag, or {@code null}
     */
    public Boolean debugEchoUpstreamBody() {
        return debugEchoUpstreamBody;
    }

    /**
     * The {@code text.format} configuration object, or {@code null} when
     * unset (the key is not sent).
     *
     * @return the text format configuration, or {@code null}
     */
    public JSONObject textFormat() {
        return textFormat;
    }

    /**
     * The {@code text.verbosity} enum value, or {@code null} when unset (the
     * key is not sent).
     *
     * @return the verbosity value, or {@code null}
     */
    public String textVerbosity() {
        return textVerbosity;
    }

    /**
     * The verbatim {@code text} object set through
     * {@link Builder#text(JSONObject)}, or {@code null}. When set it replaces
     * the composed {@code format} / {@code verbosity} form.
     *
     * @return the verbatim text object, or {@code null}
     */
    public JSONObject textVerbatim() {
        return textVerbatim;
    }

    /**
     * JSON field: {@code top_logprobs} - the number of top log probabilities
     * to return per output token, or {@code null} when unset (the key is not
     * sent).
     *
     * @return the top-logprobs count, or {@code null}
     */
    public Integer topLogprobs() {
        return topLogprobs;
    }

    /**
     * JSON field: {@code prompt_cache_options.mode} - the explicit-cache
     * configuration mode, or {@code null} when unset (the key is not sent).
     *
     * @return the prompt-cache mode, or {@code null}
     */
    public String promptCacheOptionsMode() {
        return promptCacheOptionsMode;
    }

    /**
     * JSON field: {@code prompt_cache_options.ttl} - the explicit-cache
     * time-to-live, or {@code null} when unset (the key is not sent).
     *
     * @return the prompt-cache TTL, or {@code null}
     */
    public String promptCacheOptionsTtl() {
        return promptCacheOptionsTtl;
    }

    /**
     * JSON field: {@code tool_choice} (string form) - the configured plain
     * keyword, or {@code null} when {@link Builder#toolChoice(String)} was
     * never called. Note this reports the raw setting: when an object form is
     * also configured, {@link #getBody()} emits the object form (see the
     * precedence on {@link Builder#toolChoiceFunction(String)}) but this
     * accessor still returns the keyword.
     *
     * @return the configured tool-choice keyword, or {@code null}
     */
    public String toolChoice() {
        return toolChoice;
    }

    /**
     * JSON field: {@code tool_choice.name} of the named function form, or
     * {@code null} when that form is not configured.
     *
     * @return the forced function name, or {@code null}
     */
    public String toolChoiceFunctionName() {
        return toolChoiceFunctionName;
    }

    /**
     * JSON field: {@code tool_choice.mode} of the allowed_tools form, or
     * {@code null} when that form is not configured.
     *
     * @return the allowed-tools mode, or {@code null}
     */
    public String toolChoiceAllowedToolsMode() {
        return toolChoiceAllowedToolsMode;
    }

    /**
     * JSON field: {@code tool_choice.tools} of the allowed_tools form, empty
     * when that form is not configured.
     *
     * @return the tool refs, empty when unset
     */
    public List<JSONObject> toolChoiceAllowedTools() {
        return toolChoiceAllowedTools == null ? List.of() : toolChoiceAllowedTools;
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

    /**
     * JSON field: {@code tool_choice.type} of the tool-type form, or
     * {@code null} when that form is not configured.
     *
     * @return the forced tool type, or {@code null}
     */
    public String toolChoiceType() {
        return toolChoiceType;
    }

    @Override
    public String getRelativeUrl() {
        return "/responses";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code input} (string or item array),
     * {@code model} / {@code models}, {@code prompt}, {@code image_config},
     * {@code debug}, {@code text}, {@code instructions},
     * {@code max_output_tokens}, {@code max_tool_calls}, {@code temperature},
     * {@code top_p}, {@code top_k}, {@code top_logprobs},
     * {@code frequency_penalty},
     * {@code presence_penalty}, {@code tools}, {@code tool_choice}
     * (string keyword or one of the object forms), {@code parallel_tool_calls},
     * {@code reasoning}, {@code modalities},
     * {@code include}, {@code background}, {@code store}, {@code metadata},
     * {@code service_tier}, {@code session_id}, {@code safety_identifier},
     * {@code user}, {@code prompt_cache_key}, {@code prompt_cache_options},
     * {@code truncation},
     * {@code cache_control}, {@code plugins}, {@code trace},
     * {@code stop_server_tools_when}, {@code stream} (all omitted when unset)
     * and the {@code provider} object (omitted unless any provider option is
     * set).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        if (inputText != null) {
            root.put("input", inputText);
        } else if (inputItems != null) {
            JSONArray array = new JSONArray();
            for (JSONObject item : inputItems) {
                array.put(item);
            }
            root.put("input", array);
        }
        if (model != null) {
            root.put("model", model);
        }
        if (models != null && !models.isEmpty()) {
            root.put("models", new JSONArray(models));
        }
        if (promptId != null) {
            JSONObject prompt = new JSONObject();
            prompt.put("id", promptId);
            if (promptVariables != null && promptVariables.length() > 0) {
                prompt.put("variables", new JSONObject(promptVariables.toString()));
            }
            root.put("prompt", prompt);
        }
        if (imageConfig != null) {
            root.put("image_config", imageConfig.toJson());
        }
        if (debugEchoUpstreamBody != null) {
            root.put("debug", new JSONObject().put("echo_upstream_body", debugEchoUpstreamBody));
        }
        if (textVerbatim != null) {
            root.put("text", new JSONObject(textVerbatim.toString()));
        } else if (textFormat != null || textVerbosity != null) {
            JSONObject text = new JSONObject();
            if (textFormat != null) {
                text.put("format", new JSONObject(textFormat.toString()));
            }
            if (textVerbosity != null) {
                text.put("verbosity", textVerbosity);
            }
            root.put("text", text);
        }
        if (instructions != null) {
            root.put("instructions", instructions);
        }
        if (maxOutputTokens != null) {
            root.put("max_output_tokens", maxOutputTokens);
        }
        if (maxToolCalls != null) {
            root.put("max_tool_calls", maxToolCalls);
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
        if (topLogprobs != null) {
            root.put("top_logprobs", topLogprobs);
        }
        if (frequencyPenalty != null) {
            root.put("frequency_penalty", frequencyPenalty);
        }
        if (presencePenalty != null) {
            root.put("presence_penalty", presencePenalty);
        }
        if (tools != null && !tools.isEmpty()) {
            JSONArray array = new JSONArray();
            for (JSONObject tool : tools) {
                array.put(tool);
            }
            root.put("tools", array);
        }
        if (toolChoiceFunctionName != null) {
            JSONObject functionChoice = new JSONObject();
            functionChoice.put("type", "function");
            functionChoice.put("name", toolChoiceFunctionName);
            root.put("tool_choice", functionChoice);
        } else if (toolChoiceAllowedToolsMode != null) {
            JSONObject allowedToolsChoice = new JSONObject();
            allowedToolsChoice.put("type", "allowed_tools");
            allowedToolsChoice.put("mode", toolChoiceAllowedToolsMode);
            JSONArray allowedToolsArr = new JSONArray();
            for (JSONObject toolRef : toolChoiceAllowedTools) {
                allowedToolsArr.put(toolRef);
            }
            allowedToolsChoice.put("tools", allowedToolsArr);
            root.put("tool_choice", allowedToolsChoice);
        } else if (toolChoiceType != null) {
            JSONObject typeChoice = new JSONObject();
            typeChoice.put("type", toolChoiceType);
            root.put("tool_choice", typeChoice);
        } else if (toolChoice != null) {
            root.put("tool_choice", toolChoice);
        }
        if (parallelToolCalls != null) {
            root.put("parallel_tool_calls", parallelToolCalls);
        }
        if (reasoningEffort != null || reasoningMaxTokens != null
                || reasoningSummary != null || reasoningEnabled != null) {
            JSONObject reasoning = new JSONObject();
            if (reasoningEnabled != null) {
                reasoning.put("enabled", reasoningEnabled);
            }
            if (reasoningEffort != null) {
                reasoning.put("effort", reasoningEffort);
            }
            if (reasoningMaxTokens != null) {
                reasoning.put("max_tokens", reasoningMaxTokens);
            }
            if (reasoningSummary != null) {
                reasoning.put("summary", reasoningSummary);
            }
            root.put("reasoning", reasoning);
        }
        if (modalities != null && !modalities.isEmpty()) {
            root.put("modalities", new JSONArray(modalities));
        }
        if (include != null && !include.isEmpty()) {
            root.put("include", new JSONArray(include));
        }
        if (background != null) {
            root.put("background", background);
        }
        if (store != null) {
            root.put("store", store);
        }
        if (metadata != null) {
            root.put("metadata", metadata);
        }
        if (serviceTier != null) {
            root.put("service_tier", serviceTier);
        }
        if (sessionId != null) {
            root.put("session_id", sessionId);
        }
        if (safetyIdentifier != null) {
            root.put("safety_identifier", safetyIdentifier);
        }
        if (user != null) {
            root.put("user", user);
        }
        if (promptCacheKey != null) {
            root.put("prompt_cache_key", promptCacheKey);
        }
        if (promptCacheOptionsMode != null) {
            JSONObject promptCacheOptions = new JSONObject();
            promptCacheOptions.put("mode", promptCacheOptionsMode);
            if (promptCacheOptionsTtl != null) {
                promptCacheOptions.put("ttl", promptCacheOptionsTtl);
            }
            root.put("prompt_cache_options", promptCacheOptions);
        }
        if (truncation != null) {
            root.put("truncation", truncation);
        }
        if (cacheControlTtl != null) {
            JSONObject cacheControl = new JSONObject();
            cacheControl.put("type", "ephemeral");
            cacheControl.put("ttl", cacheControlTtl);
            root.put("cache_control", cacheControl);
        }
        if (plugins != null && !plugins.isEmpty()) {
            JSONArray array = new JSONArray();
            for (OpenRouterPlugin plugin : plugins) {
                array.put(plugin.toJson());
            }
            root.put("plugins", array);
        }
        if (trace != null) {
            root.put("trace", trace.toJson());
        }
        if (stopServerToolsWhen != null && !stopServerToolsWhen.isEmpty()) {
            JSONArray array = new JSONArray();
            for (OpenRouterStopCondition condition : stopServerToolsWhen) {
                array.put(condition.toJson());
            }
            root.put("stop_server_tools_when", array);
        }
        if (providerOrder != null || providerOnly != null || providerIgnore != null
                || requireParameters != null || allowFallbacks != null
                || dataCollection != null
                || (quantizations != null && !quantizations.isEmpty())
                || sort != null || sortBy != null
                || maxPricePrompt != null || maxPriceCompletion != null
                || maxPriceImage != null || maxPriceAudio != null
                || preferredMaxLatency != null || preferredMaxLatencyCutoffs != null
                || preferredMinThroughput != null || preferredMinThroughputCutoffs != null
                || enforceDistillableText != null || zdr != null) {
            JSONObject provider = new JSONObject();
            if (providerOrder != null) {
                provider.put("order", new JSONArray(providerOrder));
            }
            if (providerOnly != null) {
                provider.put("allow_fallbacks", false);
                provider.put("only", new JSONArray(providerOnly));
            }
            if (providerIgnore != null) {
                provider.put("ignore", new JSONArray(providerIgnore));
            }
            if (requireParameters != null) {
                provider.put("require_parameters", requireParameters);
            }
            if (allowFallbacks != null) {
                provider.put("allow_fallbacks", allowFallbacks);
            }
            if (dataCollection != null) {
                provider.put("data_collection", dataCollection);
            }
            if (maxPricePrompt != null || maxPriceCompletion != null
                    || maxPriceImage != null || maxPriceAudio != null) {
                JSONObject maxPriceObj = new JSONObject();
                if (maxPricePrompt != null) { maxPriceObj.put("prompt", maxPricePrompt); }
                if (maxPriceCompletion != null) { maxPriceObj.put("completion", maxPriceCompletion); }
                if (maxPriceImage != null) { maxPriceObj.put("image", maxPriceImage); }
                if (maxPriceAudio != null) { maxPriceObj.put("audio", maxPriceAudio); }
                provider.put("max_price", maxPriceObj);
            }
            if (quantizations != null && !quantizations.isEmpty()) {
                provider.put("quantizations", new JSONArray(quantizations));
            }
            if (sortBy != null) {
                JSONObject sortObj = new JSONObject();
                sortObj.put("by", sortBy);
                sortObj.put("partition", sortPartition);
                provider.put("sort", sortObj);
            } else if (sort != null) {
                provider.put("sort", sort);
            }
            if (enforceDistillableText != null) {
                provider.put("enforce_distillable_text", enforceDistillableText);
            }
            if (zdr != null) {
                provider.put("zdr", zdr);
            }
            if (preferredMaxLatency != null) {
                provider.put("preferred_max_latency", preferredMaxLatency);
            } else if (preferredMaxLatencyCutoffs != null) {
                provider.put("preferred_max_latency", preferredMaxLatencyCutoffs.toJson());
            }
            if (preferredMinThroughput != null) {
                provider.put("preferred_min_throughput", preferredMinThroughput);
            } else if (preferredMinThroughputCutoffs != null) {
                provider.put("preferred_min_throughput", preferredMinThroughputCutoffs.toJson());
            }
            root.put("provider", provider);
        }
        if (stream) {
            root.put("stream", true);
        }
        return root.toString();
    }

    @Override
    public OpenRouterResponsesResponse createResponse(String responseBody) {
        return new OpenRouterResponsesResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterResponsesRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterResponsesRequest> {

        private final OpenRouterClient client;
        private String inputText;
        private List<JSONObject> inputItems;
        private String model;
        private List<String> models;
        private String instructions;
        private Integer maxOutputTokens;
        private Integer maxToolCalls;
        private Double temperature;
        private Double topP;
        private Integer topK;
        private Integer topLogprobs;
        private Double frequencyPenalty;
        private Double presencePenalty;
        private List<JSONObject> tools;
        private String toolChoice;
        private String toolChoiceFunctionName;
        private String toolChoiceAllowedToolsMode;
        private List<JSONObject> toolChoiceAllowedTools;
        private String toolChoiceType;
        private Boolean parallelToolCalls;
        private String reasoningEffort;
        private Integer reasoningMaxTokens;
        private String reasoningSummary;
        private Boolean reasoningEnabled;
        private List<String> modalities;
        private List<String> include;
        private Boolean background;
        private Boolean store;
        private JSONObject metadata;
        private String serviceTier;
        private String sessionId;
        private String safetyIdentifier;
        private String user;
        private String promptCacheKey;
        private String promptCacheOptionsMode;
        private String promptCacheOptionsTtl;
        private String truncation;
        private String cacheControlTtl;
        private final Map<String, String> customHeaders = new LinkedHashMap<>();
        private List<OpenRouterPlugin> plugins;
        private OpenRouterTraceConfig trace;
        private List<OpenRouterStopCondition> stopServerToolsWhen;
        private List<String> providerOrder;
        private List<String> providerOnly;
        private List<String> providerIgnore;
        private Boolean requireParameters;
        private Boolean allowFallbacks;
        private String dataCollection;
        private List<String> quantizations;
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
        private boolean streamEnabled;
        private StreamingInfo streamingInfo;
        private String promptId;
        private JSONObject promptVariables;
        private OpenRouterImageConfig imageConfig;
        private Boolean debugEchoUpstreamBody;
        private JSONObject textFormat;
        private String textVerbosity;
        private JSONObject textVerbatim;

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
         * Sets the JSON field {@code input} as a plain string (the simple
         * form). Mutually exclusive with the item-array form built by
         * {@link #addMessage(String, String)} / {@link #addInputItem(JSONObject)};
         * the last form set wins.
         *
         * @param input the plain input text
         * @return this builder
         */
        public Builder input(String input) {
            this.inputText = input;
            this.inputItems = null;
            return this;
        }

        /**
         * Adds an easy-input message item to the {@code input} array:
         * {@code {"type":"message","role":...,"content":...}}. Switches the
         * input to the item-array form.
         *
         * @param role the message role ({@code user}, {@code assistant}, {@code system}, ...)
         * @param text the message content
         * @return this builder
         */
        public Builder addMessage(String role, String text) {
            if (role == null || role.isEmpty()) {
                throw new IllegalArgumentException("role is required for a Responses input message");
            }
            if (text == null) {
                throw new IllegalArgumentException("text is required for a Responses input message");
            }
            ensureInputItems();
            JSONObject item = new JSONObject();
            item.put("type", "message");
            item.put("role", role);
            item.put("content", text);
            this.inputItems.add(item);
            return this;
        }

        /**
         * Adds a verbatim item to the {@code input} array - the escape hatch
         * for the item forms the library does not type (server-tool output
         * items, shell/apply-patch round trips, compaction markers, MCP items,
         * ...). The tool-call round-trip basics are typed on
         * {@link OpenRouterInputItem} and appended via {@link #addInput(OpenRouterInputItem)}.
         *
         * @param item the raw input item
         * @return this builder
         */
        public Builder addInputItem(JSONObject item) {
            if (item == null) {
                throw new IllegalArgumentException("input item must not be null");
            }
            ensureInputItems();
            this.inputItems.add(item);
            return this;
        }

        /**
         * Adds a typed item to the {@code input} array - see
         * {@link OpenRouterInputItem} for the factories ({@code functionCall},
         * {@code functionCallOutput}, {@code itemReference},
         * {@code outputMessage}, {@code reasoning}, {@code raw}). Switches the
         * input to the item-array form like {@link #addMessage(String, String)}.
         * Deliberately not an {@code addInputItem} overload: that would make
         * existing {@code addInputItem(null)} calls ambiguous at compile time.
         *
         * @param item the typed input item
         * @return this builder
         */
        public Builder addInput(OpenRouterInputItem item) {
            if (item == null) {
                throw new IllegalArgumentException("input item must not be null");
            }
            ensureInputItems();
            this.inputItems.add(item.toJson());
            return this;
        }

        private void ensureInputItems() {
            if (this.inputItems == null) {
                this.inputItems = new ArrayList<>();
            }
            this.inputText = null;
        }

        /**
         * Sets the JSON field {@code model} (required unless
         * {@link #models(List)} is used).
         *
         * @param model the model id
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the JSON field {@code models} - the model fallback list.
         *
         * @param models the model ids in priority order
         * @return this builder
         */
        public Builder models(List<String> models) {
            this.models = models;
            return this;
        }

        /**
         * Sets the JSON field {@code models} - the model fallback list.
         *
         * @param models the model ids in priority order
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
         * Sets the JSON field {@code prompt.id} - the id of a stored prompt
         * template (the Responses counterpart of the chat-completions
         * {@code preset} field). The template runs the request; template
         * variables ride along via {@link #promptVariable(String, Object)}.
         * The {@code prompt} object is emitted only when an id is set.
         *
         * @param id the stored prompt template id
         * @return this builder
         */
        public Builder prompt(String id) {
            this.promptId = id;
            return this;
        }

        /**
         * Adds a JSON field {@code prompt.variables[key]} entry for the
         * stored prompt template set via {@link #prompt(String)}. The value
         * may be any JSON-representable object (string, number, boolean,
         * {@code JSONObject}); {@code null} values are rejected. Trap:
         * variables without a preceding {@code prompt(String)} id are
         * rejected loudly in {@link #build()}.
         *
         * @param key the variable name
         * @param value the variable value
         * @return this builder
         */
        public Builder promptVariable(String key, Object value) {
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("prompt variable key must not be null or empty");
            }
            if (value == null) {
                throw new IllegalArgumentException("prompt variable value must not be null");
            }
            if (promptVariables == null) {
                promptVariables = new JSONObject();
            }
            promptVariables.put(key, value);
            return this;
        }

        /**
         * Sets the JSON field {@code image_config} - the provider-specific
         * image generation options (image count, aspect ratio, resolution,
         * quality and similar provider-specific keys). The Responses API can
         * generate images; without this object the model-dependent defaults
         * apply. Build it with {@link OpenRouterImageConfig#builder()} - the
         * same type the chat-completions request uses. Emitted only when set.
         *
         * @param config the image configuration
         * @return this builder
         */
        public Builder imageConfig(OpenRouterImageConfig config) {
            this.imageConfig = config;
            return this;
        }

        /**
         * Sets the JSON field {@code debug.echo_upstream_body} - when
         * {@code true}, OpenRouter echoes the upstream provider request body
         * back in the debug response metadata. Mainly useful for diagnosing
         * provider-facing issues. Emitted only when set.
         *
         * @param echoUpstreamBody whether to echo the upstream body
         * @return this builder
         */
        public Builder debug(Boolean echoUpstreamBody) {
            this.debugEchoUpstreamBody = echoUpstreamBody;
            return this;
        }

        /**
         * Sets the JSON field {@code text.format} - the text output format
         * configuration (the {@code Formats} form, e.g.
         * {@code new JSONObject().put("type", "text")}). One of the two typed
         * {@code text} keys alongside {@link #textVerbosity(String)}; both
         * are composed into the {@code text} object, each emitted only when
         * set. Replaced wholesale by {@link #text(JSONObject)}.
         *
         * @param format the format configuration object
         * @return this builder
         */
        public Builder textFormat(JSONObject format) {
            this.textFormat = format;
            return this;
        }

        /**
         * Sets the JSON field {@code text.verbosity} - how verbose the text
         * output should be: {@code low}, {@code medium}, {@code high},
         * {@code xhigh} or {@code max} (validated loudly). One of the two
         * typed {@code text} keys alongside {@link #textFormat(JSONObject)}.
         *
         * @param verbosity the verbosity value
         * @return this builder
         */
        public Builder textVerbosity(String verbosity) {
            if (verbosity != null && !List.of("low", "medium", "high", "xhigh", "max").contains(verbosity)) {
                throw new IllegalArgumentException(
                        "text verbosity must be one of low, medium, high, xhigh, max, got: " + verbosity);
            }
            this.textVerbosity = verbosity;
            return this;
        }

        /**
         * Sets the JSON field {@code text} verbatim - the escape hatch for
         * future {@code Formats} variants the typed
         * {@link #textFormat(JSONObject)} / {@link #textVerbosity(String)}
         * methods do not cover yet. Replaces the composed form entirely.
         *
         * @param text the verbatim text configuration object
         * @return this builder
         */
        public Builder text(JSONObject text) {
            this.textVerbatim = text;
            return this;
        }

        /**
         * Sets the JSON field {@code instructions} - system-style
         * instructions for the response.
         *
         * @param instructions the instructions text
         * @return this builder
         */
        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        /**
         * Sets the JSON field {@code max_output_tokens} - the upper bound
         * for the generated output.
         *
         * @param maxOutputTokens the token limit
         * @return this builder
         */
        public Builder maxOutputTokens(Integer maxOutputTokens) {
            this.maxOutputTokens = maxOutputTokens;
            return this;
        }

        /**
         * Sets the JSON field {@code max_tool_calls} - the maximum number of
         * server-tool agent steps (default and maximum 30 per the schema;
         * ignored when {@code stop_server_tools_when} is set).
         *
         * @param maxToolCalls the step limit
         * @return this builder
         */
        public Builder maxToolCalls(Integer maxToolCalls) {
            this.maxToolCalls = maxToolCalls;
            return this;
        }

        /**
         * Sets the JSON field {@code temperature}.
         *
         * @param temperature the sampling temperature
         * @return this builder
         */
        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Sets the JSON field {@code top_p}.
         *
         * @param topP the nucleus-sampling cutoff
         * @return this builder
         */
        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        /**
         * Sets the JSON field {@code top_k}.
         *
         * @param topK the top-k cutoff
         * @return this builder
         */
        public Builder topK(Integer topK) {
            this.topK = topK;
            return this;
        }

        /**
         * Sets the JSON field {@code top_logprobs} - the number of top log
         * probabilities to return per output token (the same semantics as the
         * chat-completions {@code topLogprobs(n)}; the logprob entries ride
         * on the output content parts). Emitted only when set.
         *
         * @param topLogprobs the number of top log probabilities per output token
         * @return this builder
         */
        public Builder topLogprobs(Integer topLogprobs) {
            this.topLogprobs = topLogprobs;
            return this;
        }

        /**
         * Sets the JSON field {@code frequency_penalty}.
         *
         * @param frequencyPenalty the frequency penalty
         * @return this builder
         */
        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        /**
         * Sets the JSON field {@code presence_penalty}.
         *
         * @param presencePenalty the presence penalty
         * @return this builder
         */
        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        /**
         * Adds a function tool to the {@code tools} array:
         * {@code {"type":"function","name":...,"description":...,"parameters":...}}.
         *
         * @param name the tool name
         * @param description the tool description
         * @param parameters the JSON-schema parameters object
         * @return this builder
         */
        public Builder addFunctionTool(String name, String description, JSONObject parameters) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name is required for a function tool");
            }
            ensureTools();
            JSONObject tool = new JSONObject();
            tool.put("type", "function");
            tool.put("name", name);
            if (description != null) {
                tool.put("description", description);
            }
            if (parameters != null) {
                tool.put("parameters", parameters);
            }
            this.tools.add(tool);
            return this;
        }

        /**
         * Adds a verbatim tool to the {@code tools} array - the escape hatch
         * for server tools (e.g. {@code openrouter:tool_search},
         * {@code openrouter:web_search}) and future tool forms. The typed
         * server-tool classes ship {@code toJson()} and can be passed here.
         *
         * @param tool the raw tool object
         * @return this builder
         */
        public Builder addTool(JSONObject tool) {
            if (tool == null) {
                throw new IllegalArgumentException("tool must not be null");
            }
            ensureTools();
            this.tools.add(tool);
            return this;
        }

        private void ensureTools() {
            if (this.tools == null) {
                this.tools = new ArrayList<>();
            }
        }

        /**
         * Sets the JSON field {@code tool_choice} to one of the plain string
         * keywords ({@code "auto"}, {@code "none"}, {@code "required"}).
         *
         * <p>JSON field: {@code tool_choice} (string form). Default: unset
         * (the key is not sent). Unlike the chat builder, the key is emitted
         * whenever a form is set, even when {@code tools} is empty. The object
         * forms ({@link #toolChoiceFunction(String)},
         * {@link #toolChoiceAllowedTools(String, Object...)},
         * {@link #toolChoiceType(String)}) win when several forms are set -
         * see the precedence documented there.
         *
         * <p>Trap: when deferred tools (e.g. {@code openrouter:tool_search}
         * with top-level {@code defer_loading} on the tool entries) are in
         * play, {@code tool_choice} must be omitted, left at {@code "auto"},
         * or set to the {@code allowed_tools} form
         * ({@link #toolChoiceAllowedTools(String, Object...)}) - any other
         * setting makes the request fail (see
         * {@link de.entwicklertraining.openrouter4j.OpenRouterToolSearchServerTool}).
         *
         * @param toolChoice the tool choice keyword
         * @return this builder
         */
        public Builder toolChoice(String toolChoice) {
            this.toolChoice = toolChoice;
            return this;
        }

        /**
         * Forces the model to call one specific tool - the named object form
         * {@code {"type":"function","name":<name>}} (flat {@code name}, not
         * the chat-completions nested {@code function.name} shape).
         *
         * <p>JSON field: {@code tool_choice} (named function form). Default:
         * unset (the key is not sent). Unlike the chat builder, the key is
         * emitted whenever a form is set, even when {@code tools} is empty -
         * but the named tool must still be among them at the API.
         * Precedence when several forms are set: this named function form
         * wins, then {@link #toolChoiceAllowedTools(String, Object...)}, then
         * {@link #toolChoiceType(String)}, then the plain string keywords
         * ({@link #toolChoice(String)}).
         *
         * <p>Trap: when deferred tools (e.g. {@code openrouter:tool_search}
         * with top-level {@code defer_loading} on the tool entries) are in
         * play, {@code tool_choice} must be omitted, left at {@code "auto"},
         * or set to the {@code allowed_tools} form
         * ({@link #toolChoiceAllowedTools(String, Object...)}) - any other
         * setting makes the request fail (see
         * {@link de.entwicklertraining.openrouter4j.OpenRouterToolSearchServerTool}).
         *
         * @param name name of the tool definition to force (must match a tool in {@code tools})
         * @return this builder
         * @throws IllegalArgumentException when {@code name} is null or empty
         */
        public Builder toolChoiceFunction(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("tool_choice function name is required");
            }
            this.toolChoiceFunctionName = name;
            return this;
        }

        /**
         * Constrains the model to a pre-defined set of allowed tools - the
         * {@code {"type":"allowed_tools","mode":...,"tools":[...]}} form.
         *
         * <p>Each tool ref is either a {@link String} (emitted as
         * {@code {"type":"function","name":...}}) or a verbatim
         * {@link JSONObject} used as-is. JSON field: {@code tool_choice}
         * (allowed_tools form). Default: unset (the key is not sent). Unlike
         * the chat builder, the key is emitted whenever a form is set, even
         * when {@code tools} is empty. Precedence when several forms are set:
         * {@link #toolChoiceFunction(String)} wins, then this allowed_tools
         * form, then {@link #toolChoiceType(String)}, then the plain string
         * keywords ({@link #toolChoice(String)}). A later call replaces the
         * whole allowed_tools form (mode and refs), it does not append.
         *
         * <p>Trap: this is the one object form the docs accept alongside
         * deferred tools (e.g. {@code openrouter:tool_search} with top-level
         * {@code defer_loading} on the tool entries); there any <em>other</em>
         * {@code tool_choice} setting must stay at {@code "auto"} or unset
         * (see {@link de.entwicklertraining.openrouter4j.OpenRouterToolSearchServerTool}).
         *
         * @param mode {@code "auto"} or {@code "required"}
         * @param toolRefs at least one tool ref ({@link String} function name or verbatim {@link JSONObject})
         * @return this builder
         * @throws IllegalArgumentException when {@code mode} is not {@code "auto"}/{@code "required"}, no tool ref is given or a ref is neither a non-empty {@link String} nor a {@link JSONObject}
         */
        public Builder toolChoiceAllowedTools(String mode, Object... toolRefs) {
            return toolChoiceAllowedTools(mode, toolRefs == null ? null : java.util.Arrays.asList(toolRefs));
        }

        /**
         * Collection form of {@link #toolChoiceAllowedTools(String, Object...)}:
         * same semantics (JSON field {@code tool_choice} allowed_tools form,
         * default unset, a later call replaces the whole form, and this is the
         * one object form the docs accept alongside deferred tools).
         *
         * @param mode {@code "auto"} or {@code "required"}
         * @param toolRefs at least one tool ref ({@link String} function name or verbatim {@link JSONObject})
         * @return this builder
         * @throws IllegalArgumentException when {@code mode} is not {@code "auto"}/{@code "required"}, no tool ref is given or a ref is neither a non-empty {@link String} nor a {@link JSONObject}
         */
        public Builder toolChoiceAllowedTools(String mode, Collection<?> toolRefs) {
            if (!"auto".equals(mode) && !"required".equals(mode)) {
                throw new IllegalArgumentException(
                        "tool_choice allowed_tools mode must be auto or required, got " + mode);
            }
            if (toolRefs == null || toolRefs.isEmpty()) {
                throw new IllegalArgumentException("tool_choice allowed_tools needs at least one tool ref");
            }
            List<JSONObject> refs = new ArrayList<>();
            for (Object toolRef : toolRefs) {
                if (toolRef instanceof JSONObject) {
                    refs.add((JSONObject) toolRef);
                } else if (toolRef instanceof String && !((String) toolRef).isEmpty()) {
                    JSONObject functionRef = new JSONObject();
                    functionRef.put("type", "function");
                    functionRef.put("name", toolRef);
                    refs.add(functionRef);
                } else {
                    throw new IllegalArgumentException(
                            "tool_choice allowed_tools tool refs must be non-empty strings or JSONObjects");
                }
            }
            this.toolChoiceAllowedToolsMode = mode;
            this.toolChoiceAllowedTools = refs;
            return this;
        }

        /**
         * Forces a tool-type shorthand object - {@code {"type":"<type>"}}.
         * {@code tool_choice.type} takes the bare documented variants
         * {@code web_search_preview}, {@code web_search_preview_2025_03_11},
         * {@code apply_patch} and {@code shell}. Pairing trap:
         * {@code apply_patch} / {@code shell} force the tool entries whose
         * {@code tools[].type} is
         * {@link de.entwicklertraining.openrouter4j.OpenRouterApplyPatchServerTool#TOOL_TYPE} /
         * {@link de.entwicklertraining.openrouter4j.OpenRouterShellServerTool#TOOL_TYPE},
         * while {@code web_search_preview} / {@code web_search_preview_2025_03_11}
         * force a {@code tools[]} entry whose {@code tools[].type} is that same
         * bare variant (an OpenAI-style built-in, add it verbatim via
         * {@code addTool}) - they do <em>not</em> pair with
         * {@link de.entwicklertraining.openrouter4j.OpenRouterWebSearchServerTool},
         * whose {@code tools[].type} is {@code openrouter:web_search}. The
         * type string is accepted verbatim, so an unknown variant travels as
         * an escape hatch - the schema union is closed, so a value outside the
         * four documented ones is up to the API to accept or reject.
         *
         * <p>JSON field: {@code tool_choice} (tool-type form). Default: unset
         * (the key is not sent). Unlike the chat builder, the key is emitted
         * whenever a form is set, even when {@code tools} is empty.
         * Precedence when several forms are set:
         * {@link #toolChoiceFunction(String)} wins, then
         * {@link #toolChoiceAllowedTools(String, Object...)}, then this
         * tool-type form, then the plain string keywords
         * ({@link #toolChoice(String)}).
         *
         * <p>Trap: when deferred tools (e.g. {@code openrouter:tool_search}
         * with top-level {@code defer_loading} on the tool entries) are in
         * play, {@code tool_choice} must be omitted, left at {@code "auto"},
         * or set to the {@code allowed_tools} form
         * ({@link #toolChoiceAllowedTools(String, Object...)}) - any other
         * setting makes the request fail (see
         * {@link de.entwicklertraining.openrouter4j.OpenRouterToolSearchServerTool}).
         *
         * @param type the tool type to force (e.g. {@code "apply_patch"} or {@code "web_search_preview"})
         * @return this builder
         * @throws IllegalArgumentException when {@code type} is null or empty
         */
        public Builder toolChoiceType(String type) {
            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException("tool_choice type is required");
            }
            this.toolChoiceType = type;
            return this;
        }

        /**
         * Sets the JSON field {@code parallel_tool_calls}.
         *
         * @param parallelToolCalls whether tool calls may run in parallel
         * @return this builder
         */
        public Builder parallelToolCalls(Boolean parallelToolCalls) {
            this.parallelToolCalls = parallelToolCalls;
            return this;
        }

        /**
         * Sets {@code reasoning.enabled} inside the {@code reasoning} object.
         *
         * @param enabled whether reasoning is enabled
         * @return this builder
         */
        public Builder reasoningEnabled(Boolean enabled) {
            this.reasoningEnabled = enabled;
            return this;
        }

        /**
         * Sets {@code reasoning.effort} inside the {@code reasoning} object
         * (e.g. {@code low}, {@code medium}, {@code high}).
         *
         * @param effort the reasoning effort
         * @return this builder
         */
        public Builder reasoningEffort(String effort) {
            this.reasoningEffort = effort;
            return this;
        }

        /**
         * Sets {@code reasoning.max_tokens} inside the {@code reasoning}
         * object - the reasoning token budget.
         *
         * @param maxTokens the reasoning token budget
         * @return this builder
         */
        public Builder reasoningMaxTokens(Integer maxTokens) {
            this.reasoningMaxTokens = maxTokens;
            return this;
        }

        /**
         * Sets {@code reasoning.summary} inside the {@code reasoning} object
         * (e.g. {@code auto}, {@code concise}, {@code detailed}).
         *
         * @param summary the reasoning summary verbosity
         * @return this builder
         */
        public Builder reasoningSummary(String summary) {
            this.reasoningSummary = summary;
            return this;
        }

        /**
         * Sets the JSON field {@code modalities} - the output modalities
         * ({@code "text"}, {@code "image"}).
         *
         * @param modalities the output modalities
         * @return this builder
         */
        public Builder modalities(List<String> modalities) {
            this.modalities = modalities;
            return this;
        }

        /**
         * Sets the JSON field {@code include} - extra output data to include
         * (e.g. {@code reasoning.encrypted_content},
         * {@code file_search_call.results}).
         *
         * @param include the include entries
         * @return this builder
         */
        public Builder include(List<String> include) {
            this.include = include;
            return this;
        }

        /**
         * Adds one entry to the JSON field {@code include}.
         *
         * @param includeEntry the include entry
         * @return this builder
         */
        public Builder addInclude(String includeEntry) {
            if (includeEntry == null || includeEntry.isEmpty()) {
                throw new IllegalArgumentException("include entry must not be empty");
            }
            if (this.include == null) {
                this.include = new ArrayList<>();
            }
            this.include.add(includeEntry);
            return this;
        }

        /**
         * Sets the JSON field {@code background} - run the response in the
         * background.
         *
         * @param background true for background execution
         * @return this builder
         */
        public Builder background(Boolean background) {
            this.background = background;
            return this;
        }

        /**
         * Sets the JSON field {@code store} - whether the response is stored
         * (the Responses API on OpenRouter is stateless; the schema pins this
         * to {@code false}).
         *
         * @param store whether to store the response
         * @return this builder
         */
        public Builder store(Boolean store) {
            this.store = store;
            return this;
        }

        /**
         * Sets the JSON field {@code metadata} verbatim.
         *
         * @param metadata the metadata object
         * @return this builder
         */
        public Builder metadata(JSONObject metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Sets the JSON field {@code service_tier} ({@code auto},
         * {@code default}, {@code fast}, {@code flex}, {@code priority},
         * {@code scale}; {@code fast} is accepted as an alias for
         * {@code priority}).
         *
         * @param serviceTier the service tier
         * @return this builder
         */
        public Builder serviceTier(String serviceTier) {
            this.serviceTier = serviceTier;
            return this;
        }

        /**
         * Sets the JSON field {@code session_id} - the sticky-routing key
         * for related requests (max 256 characters).
         *
         * @param sessionId the session id
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Sets the JSON field {@code safety_identifier} - the per-end-user
         * abuse-isolation identifier.
         *
         * @param safetyIdentifier the stable end-user identifier
         * @return this builder
         */
        public Builder safetyIdentifier(String safetyIdentifier) {
            this.safetyIdentifier = safetyIdentifier;
            return this;
        }

        /**
         * Sets the JSON field {@code user}.
         *
         * @param user the user identifier
         * @return this builder
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the JSON field {@code prompt_cache_key}.
         *
         * @param promptCacheKey the prompt-cache key
         * @return this builder
         */
        public Builder promptCacheKey(String promptCacheKey) {
            this.promptCacheKey = promptCacheKey;
            return this;
        }

        /**
         * Sets {@code prompt_cache_options} with an explicit mode
         * ({@code {"mode":<mode>}}) - the same explicit-cache configuration
         * the chat-completions builder exposes. Documented mode:
         * {@code "explicit"} - disables OpenAI-managed breakpoints so only
         * blocks marked with {@code prompt_cache_breakpoint} participate in
         * caching (OpenAI GPT-5.6+). Emitted only when set.
         *
         * @param mode the caching mode, e.g. {@code "explicit"}
         * @return this builder
         */
        public Builder promptCacheOptions(String mode) {
            this.promptCacheOptionsMode = mode;
            this.promptCacheOptionsTtl = null;
            return this;
        }

        /**
         * Sets {@code prompt_cache_options} with an explicit mode and TTL
         * ({@code {"mode":<mode>,"ttl":<ttl>}}) - see
         * {@link #promptCacheOptions(String)} for the mode semantics.
         * Emitted only when set. Trap: the object is emitted only when the
         * mode is non-null - a TTL passed with a {@code null} mode is
         * silently not sent, matching the "emitted only when set" convention.
         *
         * @param mode the caching mode, e.g. {@code "explicit"}
         * @param ttl the cache time-to-live, e.g. {@code "30m"}
         * @return this builder
         */
        public Builder promptCacheOptions(String mode, String ttl) {
            this.promptCacheOptionsMode = mode;
            this.promptCacheOptionsTtl = ttl;
            return this;
        }

        /**
         * Sets the JSON field {@code truncation} ({@code "auto"} or
         * {@code "disabled"}).
         *
         * @param truncation the truncation mode
         * @return this builder
         */
        public Builder truncation(String truncation) {
            this.truncation = truncation;
            return this;
        }

        /**
         * Sets the JSON field {@code cache_control} as the Anthropic-style
         * directive {@code {"type":"ephemeral","ttl":...}} - the same shape
         * the chat-completions request-root {@code cacheControl(ttl)} emits.
         *
         * @param ttl the cache TTL
         * @return this builder
         */
        public Builder cacheControl(String ttl) {
            this.cacheControlTtl = ttl;
            return this;
        }

        /**
         * Sets the JSON field {@code plugins} - the OpenRouter plugins, using
         * the same typed {@link OpenRouterPlugin} implementations as the
         * chat-completions builder.
         *
         * @param plugins the plugins
         * @return this builder
         */
        public Builder plugins(List<OpenRouterPlugin> plugins) {
            this.plugins = plugins;
            return this;
        }

        /**
         * Adds one plugin to the JSON field {@code plugins}.
         *
         * @param plugin the plugin
         * @return this builder
         */
        public Builder addPlugin(OpenRouterPlugin plugin) {
            if (plugin == null) {
                throw new IllegalArgumentException("plugin must not be null");
            }
            if (this.plugins == null) {
                this.plugins = new ArrayList<>();
            }
            this.plugins.add(plugin);
            return this;
        }

        /**
         * Sets the JSON field {@code trace} - the observability trace config.
         *
         * @param trace the trace config
         * @return this builder
         */
        public Builder trace(OpenRouterTraceConfig trace) {
            this.trace = trace;
            return this;
        }

        /**
         * Sets the JSON field {@code stop_server_tools_when} - conditions
         * that end the server-tool agent loop early. When set,
         * {@code max_tool_calls} is ignored by the API.
         *
         * @param conditions the stop conditions
         * @return this builder
         */
        public Builder stopServerToolsWhen(List<OpenRouterStopCondition> conditions) {
            this.stopServerToolsWhen = conditions;
            return this;
        }

        /**
         * Sets the {@code provider.order} field - the preferred provider
         * slugs in priority order.
         *
         * @param order the provider order
         * @return this builder
         */
        public Builder providerOrder(String... order) {
            List<String> list = new ArrayList<>();
            for (String p : order) {
                if (p != null && !p.isEmpty()) {
                    list.add(p);
                }
            }
            this.providerOrder = list;
            return this;
        }

        /**
         * Sets the {@code provider.only} field - restricts routing to these
         * providers and disables fallbacks.
         *
         * @param only the allowed provider slugs
         * @return this builder
         */
        public Builder providerOnly(String... only) {
            List<String> list = new ArrayList<>();
            for (String p : only) {
                if (p != null && !p.isEmpty()) {
                    list.add(p);
                }
            }
            this.providerOnly = list;
            return this;
        }

        /**
         * Sets the {@code provider.ignore} field - providers to exclude from
         * routing.
         *
         * @param ignore the excluded provider slugs
         * @return this builder
         */
        public Builder providerIgnore(String... ignore) {
            List<String> list = new ArrayList<>();
            for (String p : ignore) {
                if (p != null && !p.isEmpty()) {
                    list.add(p);
                }
            }
            this.providerIgnore = list;
            return this;
        }

        /**
         * Sets the {@code provider.require_parameters} field - only route to
         * endpoints that support all request parameters (see the
         * {@code requireParameters} trap: without it, constrained outputs can
         * be silently dropped).
         *
         * @param requireParameters whether parameter support is required
         * @return this builder
         */
        public Builder requireParameters(Boolean requireParameters) {
            this.requireParameters = requireParameters;
            return this;
        }

        /**
         * Sets the {@code provider.allow_fallbacks} field.
         *
         * @param allowFallbacks whether fallback providers are allowed
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
            this.quantizations = levels == null ? null : new java.util.ArrayList<>(levels);
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
         * criterion makes the object form a silent no-op - if the plain
         * {@code sort(...)} is also set, that plain value is what gets sent
         * (the object form only wins with a non-null criterion); a
         * {@code null}/{@code blank} partition is rejected with
         * {@code IllegalArgumentException}.
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

        /**
         * Sends an arbitrary HTTP header with this request - the escape hatch for
         * headers the library does not type. A {@code null} value removes a
         * previously set header of that name again. Names and values must not
         * contain CR or LF (header injection) - rejected with
         * {@link IllegalArgumentException}, like a blank name. The
         * {@link #responseCache(Boolean)} family shares this header map, so for
         * the {@code X-OpenRouter-Cache*} names the <em>last</em> builder call
         * wins. A value sent here gets none of the typed methods' validation -
         * e.g. a raw {@code X-OpenRouter-Cache-TTL} is parsed leniently by the
         * API ({@code 60abc} becomes 60, {@code 1.5} becomes 1, unparseable
         * values fall through to the preset or default TTL).
         *
         * @param name the header name (non-blank, no CR/LF)
         * @param value the header value (no CR/LF), or {@code null} to remove the
         *        header again
         * @return this builder
         */
        public Builder header(String name, String value) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Header name must not be blank");
            }
            if (name.indexOf('\r') >= 0 || name.indexOf('\n') >= 0) {
                throw new IllegalArgumentException("Header name must not contain CR or LF");
            }
            if (value == null) {
                customHeaders.remove(name);
                return this;
            }
            if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
                throw new IllegalArgumentException("Header value must not contain CR or LF");
            }
            customHeaders.put(name, value);
            return this;
        }

        /**
         * Enables or disables OpenRouter-level <b>response caching</b> for this
         * request: identical requests (same API key, model, endpoint type,
         * streaming mode and request body) are answered from cache with zeroed
         * usage counters and no billing.
         * <p>
         * Header: {@code X-OpenRouter-Cache}. {@code true} enables caching,
         * {@code false} forces it off for this request even when a preset enables
         * it, {@code null} (default) sends no header.
         * <p>
         * Traps: the cache key covers API key, model, endpoint type, streaming
         * mode and a SHA-256 of the request body - the JSON <em>property order</em>
         * is significant (whitespace is not) and omitting an optional field is not
         * the same as sending its default. Cached responses are returned verbatim
         * regardless of stochastic parameters - use {@link #responseCacheClear(Boolean)}
         * or a short {@link #responseCacheTtl(Integer)} when you need fresh answers.
         * On a cache HIT the response's {@code id} / {@code created} (and the
         * {@code X-Generation-Id} header) reflect the new cache-hit generation
         * record. A preset that sets {@code cache_enabled: false} wins over this
         * header. Caching is unavailable when account-level zero-data-retention is
         * enforced (per-request {@code zdr(true)} does not affect cache
         * eligibility). Attribution headers are not part of the cache key.
         * Supported on chat completions, Responses, Anthropic Messages and
         * Embeddings.
         *
         * @param enabled {@code true} to enable caching, {@code false} to force it
         *        off, {@code null} to send no header
         * @return this builder
         * @see <a href="https://openrouter.ai/docs/guides/features/response-caching">Response caching</a>
         */
        public Builder responseCache(Boolean enabled) {
            if (enabled == null) {
                customHeaders.remove(HEADER_RESPONSE_CACHE);
            } else {
                customHeaders.put(HEADER_RESPONSE_CACHE, enabled.toString());
            }
            return this;
        }

        /**
         * Forces a cache refresh for this request: the cache entry for this
         * request's cache key is deleted and a fresh response is fetched and
         * cached again. Has no effect unless caching is enabled for the request.
         * <p>
         * Header: {@code X-OpenRouter-Cache-Clear}. {@code true} clears;
         * {@code false} and {@code null} (default) send no header.
         *
         * @param clear {@code true} to force a cache refresh
         * @return this builder
         * @see <a href="https://openrouter.ai/docs/guides/features/response-caching">Response caching</a>
         */
        public Builder responseCacheClear(Boolean clear) {
            if (Boolean.TRUE.equals(clear)) {
                customHeaders.put(HEADER_RESPONSE_CACHE_CLEAR, "true");
            } else {
                customHeaders.remove(HEADER_RESPONSE_CACHE_CLEAR);
            }
            return this;
        }

        /**
         * Sets the cache lifetime for this request in seconds (1-86400, API
         * default 300). Overrides a preset's {@code cache_ttl_seconds}. The
         * documented range is validated loudly here - the API would instead
         * clamp to {@code [1, 86400]} and parse leniently (see
         * {@link #header(String, String)}); {@code null} sends no header and
         * removes a previously set value again.
         * <p>
         * Header: {@code X-OpenRouter-Cache-TTL}.
         *
         * @param seconds the TTL in seconds (1-86400)
         * @return this builder
         * @see <a href="https://openrouter.ai/docs/guides/features/response-caching">Response caching</a>
         */
        public Builder responseCacheTtl(Integer seconds) {
            if (seconds == null) {
                customHeaders.remove(HEADER_RESPONSE_CACHE_TTL);
                return this;
            }
            if (seconds < 1 || seconds > 86400) {
                throw new IllegalArgumentException(
                        "Cache TTL must be between 1 and 86400 seconds, was " + seconds);
            }
            customHeaders.put(HEADER_RESPONSE_CACHE_TTL, seconds.toString());
            return this;
        }


        /**
         * Sets the JSON field {@code stream} - SSE streaming of the response.
         * Not needed when the handler-based
         * {@code stream(StreamingResponseHandler)} is used: streaming is then
         * enabled automatically and the {@code stream} flag is emitted either
         * way.
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
         * Responses-API event arrives as its raw JSON string on
         * {@code onData}; the {@code type} field distinguishes
         * {@code response.created}, {@code response.in_progress},
         * {@code response.output_item.added}, {@code response.output_text.delta},
         * {@code response.output_item.done}, {@code response.completed},
         * {@code response.failed}, {@code response.incomplete} and
         * {@code error}, terminated by the {@code [DONE]} sentinel. The body
         * automatically carries {@code stream: true}.
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

        @Override
        public OpenRouterResponsesRequest build() {
            if ((model == null || model.isEmpty()) && (models == null || models.isEmpty())) {
                throw new IllegalStateException("model (or models) is required for a Responses request");
            }
            if (inputText == null && (inputItems == null || inputItems.isEmpty())) {
                throw new IllegalStateException("input is required for a Responses request");
            }
            if (promptVariables != null && promptVariables.length() > 0 && promptId == null) {
                throw new IllegalStateException(
                        "prompt variables require a prompt id - call prompt(String) first");
            }
            return new OpenRouterResponsesRequest(this);
        }

        @Override
        public OpenRouterResponsesResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterResponsesResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterResponsesResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
