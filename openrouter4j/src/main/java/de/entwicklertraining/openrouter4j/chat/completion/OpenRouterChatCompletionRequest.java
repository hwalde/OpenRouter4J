package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
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
    private final Integer thinkingBudget; // For reasoning models
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
            Integer thinkingBudget,
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
        this.thinkingBudget = thinkingBudget;
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

    public Integer thinkingBudget() {
        return thinkingBudget;
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
        if (providers != null && !providers.isEmpty()) {
            JSONObject providerObj = new JSONObject();
            JSONArray orderArr = new JSONArray();
            for (String p : providers) {
                orderArr.put(p);
            }
            providerObj.put("order", orderArr);
            root.put("provider", providerObj);
        }

        // Reasoning/thinking (if supported)
        if (thinkingBudget != null) {
            JSONObject reasoning = new JSONObject();
            reasoning.put("type", "enabled");
            reasoning.put("budget", thinkingBudget);
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
        private Integer thinkingBudget;
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
         */
        public Builder thinking(Integer budget) {
            this.thinkingBudget = budget;
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
                    thinkingBudget,
                    shouldStream
            );
        }

        @Override
        public OpenRouterChatCompletionResponse execute() {
            return new OpenRouterChatCompletionCallHandler(client).handleRequest(build(), false);
        }

        @Override
        public OpenRouterChatCompletionResponse executeWithExponentialBackoff() {
            return new OpenRouterChatCompletionCallHandler(client).handleRequest(build(), true);
        }
    }
}
