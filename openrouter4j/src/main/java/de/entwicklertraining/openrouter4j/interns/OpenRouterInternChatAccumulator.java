package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A streaming handler for the intern chat endpoint that forwards every raw
 * SSE chunk and <i>additionally</i> accumulates the pieces the interaction
 * loop needs, with typed accessors. Wrap (or replace) the user handler with
 * it:
 *
 * <pre>{@code
 * OpenRouterInternChatAccumulator turn = new OpenRouterInternChatAccumulator(null);
 * client.interns().chat(internId).addUserMessage("Summarize the PRs.").stream(turn).execute();
 * if (turn.isInteractionPending()) {
 *     // answer the openrouter.provide_input tool call:
 *     String interactionId = turn.toolCall().id();
 *     String sessionId = turn.sessionId();
 * }
 * }</pre>
 *
 * <p>Chunk grammar (schema {@code InternChatCompletionChunk}): content and
 * reasoning chunks carry a single choice; the interaction arrives as one
 * complete tool call named {@code openrouter.provide_input}; a failure after
 * the stream opened arrives as a chunk with {@code finish_reason: "error"}
 * and a top-level {@code error} object; every response ends with a final
 * empty-{@code choices} chunk carrying {@code session_id} and {@code usage}
 * ({@code null} unless the daemon reported usage, and always {@code null}
 * after {@code tool_calls}), followed by {@code [DONE]}.
 *
 * <p>Heartbeats arrive as SSE comment lines ({@code : ...}); the SSE
 * processor ignores them, so they never reach {@code onData}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 */
public final class OpenRouterInternChatAccumulator implements StreamingResponseHandler<String> {

    private final StreamingResponseHandler<String> userHandler;
    private final List<String> rawChunks = new ArrayList<>();
    private String finishReason;
    private String sessionId;
    private String completionId;
    private String model;
    private JSONObject error;
    private JSONObject usage;
    private JSONObject toolCall;
    private final StringBuilder contentBuilder = new StringBuilder();
    private final StringBuilder reasoningBuilder = new StringBuilder();

    /**
     * Creates an accumulator.
     *
     * @param userHandler an optional handler whose {@code onData} receives
     *                    every raw chunk as well (may be {@code null})
     */
    public OpenRouterInternChatAccumulator(StreamingResponseHandler<String> userHandler) {
        this.userHandler = userHandler;
    }

    @Override
    public void onStreamStart() {
        if (userHandler != null) {
            userHandler.onStreamStart();
        }
    }

    @Override
    public void onData(String rawChunk) {
        rawChunks.add(rawChunk);
        if (userHandler != null) {
            userHandler.onData(rawChunk);
        }
        try {
            JSONObject json = new JSONObject(rawChunk);

            if (json.has("id") && !json.isNull("id")) {
                completionId = json.optString("id", null);
            }
            if (json.has("model") && !json.isNull("model")) {
                // each chunk carries the model of the event behind it; it can
                // change within a stream - the last non-null wins
                model = json.optString("model", null);
            }
            if (json.has("session_id") && !json.isNull("session_id")) {
                sessionId = json.optString("session_id", null);
            }
            if (json.has("error") && !json.isNull("error")) {
                error = json.getJSONObject("error");
            }
            if (json.has("usage") && !json.isNull("usage")) {
                usage = json.getJSONObject("usage");
            }

            JSONArray choices = json.optJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return;
            }
            JSONObject choice = choices.getJSONObject(0);
            if (choice.has("finish_reason") && !choice.isNull("finish_reason")) {
                finishReason = choice.getString("finish_reason");
            }
            JSONObject delta = choice.optJSONObject("delta");
            if (delta == null) {
                return;
            }
            if (delta.has("content") && !delta.isNull("content")) {
                contentBuilder.append(delta.getString("content"));
            }
            if (delta.has("reasoning") && !delta.isNull("reasoning")) {
                reasoningBuilder.append(delta.getString("reasoning"));
            }
            JSONArray toolCalls = delta.optJSONArray("tool_calls");
            if (toolCalls != null && !toolCalls.isEmpty()) {
                JSONObject call = toolCalls.optJSONObject(0);
                if (call != null) {
                    toolCall = call;
                }
            }
        } catch (Exception e) {
            // a malformed chunk stays accessible via rawChunks(); typed
            // accessors follow the swallow convention
        }
    }

    @Override
    public void onComplete() {
        if (userHandler != null) {
            userHandler.onComplete();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (userHandler != null) {
            userHandler.onError(throwable);
        }
    }

    /**
     * @return {@code true} when the wrapped user handler wants to cancel
     */
    @Override
    public boolean shouldCancel() {
        return userHandler != null && userHandler.shouldCancel();
    }

    /**
     * @return every raw chunk this accumulator has seen, in arrival order
     */
    public List<String> rawChunks() {
        return List.copyOf(rawChunks);
    }

    /**
     * JSON path: {@code choices[0].finish_reason} - {@code null} while
     * streaming; {@code stop} when the run completed, {@code tool_calls}
     * when the run is paused waiting for the caller to answer the streamed
     * tool call, {@code error} on the terminal error chunk.
     *
     * @return the finish reason, or {@code null} when not seen yet
     */
    public String finishReason() {
        return finishReason;
    }

    /**
     * @return {@code true} when the run is paused waiting for the caller to
     *         answer the streamed {@code openrouter.provide_input} tool call
     *         ({@code finish_reason} is {@code tool_calls})
     */
    public boolean isInteractionPending() {
        return "tool_calls".equals(finishReason);
    }

    /**
     * @return {@code true} when the turn failed with a streamed error chunk
     *         ({@code finish_reason} is {@code error})
     */
    public boolean isFailed() {
        return "error".equals(finishReason);
    }

    /**
     * JSON path: {@code session_id} (final empty-choices chunk) - the daemon
     * session to continue with; send it as {@code session_id} on the next
     * request, including the {@code tool} reply to an interaction.
     * {@code null} when the run failed before the intern reported one.
     *
     * @return the session id, or {@code null} when absent
     */
    public String sessionId() {
        return sessionId;
    }

    /**
     * JSON path: {@code id} - the completion id, constant for the whole
     * response.
     *
     * @return the completion id, or {@code null} when absent
     */
    public String completionId() {
        return completionId;
    }

    /**
     * JSON path: {@code model} (last chunk that carried one) - the runtime's
     * identifier for the model the intern is running. It can change within a
     * stream and is not an OpenRouter model slug.
     *
     * @return the model identifier, or {@code null} when absent
     */
    public String model() {
        return model;
    }

    /**
     * @return the concatenated {@code delta.content} texts, empty when the
     *         turn streamed none
     */
    public String text() {
        return contentBuilder.toString();
    }

    /**
     * @return the concatenated {@code delta.reasoning} texts, empty when the
     *         turn streamed none
     */
    public String reasoning() {
        return reasoningBuilder.toString();
    }

    /**
     * JSON path: {@code choices[0].delta.tool_calls[0]} - the
     * {@code openrouter.provide_input} tool call of a paused interaction.
     *
     * @return the typed tool call view, or {@code null} when the turn
     *         streamed none
     */
    public OpenRouterInternToolCall toolCall() {
        return toolCall == null ? null : new OpenRouterInternToolCall(toolCall);
    }

    /**
     * JSON path: {@code error} - the failure of a chunk with
     * {@code finish_reason: "error"} ({@code code}, {@code message} and
     * {@code metadata.reason} / {@code metadata.retryable}).
     *
     * @return the raw error object, or {@code null} when the turn did not
     *         fail
     */
    public JSONObject error() {
        return error;
    }

    /**
     * JSON path: {@code error.code} - the HTTP status this failure would
     * have had before the stream opened.
     *
     * @return the code, or {@code null} when the turn did not fail
     */
    public Integer errorCode() {
        if (error == null || !error.has("code") || error.isNull("code")) {
            return null;
        }
        Object value = error.opt("code");
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * JSON path: {@code error.message}.
     *
     * @return the message, or {@code null} when the turn did not fail
     */
    public String errorMessage() {
        return error == null ? null : error.optString("message", null);
    }

    /**
     * JSON path: {@code error.metadata.reason} - a stable reason a client
     * can branch on (e.g. {@code interaction_not_pending},
     * {@code stream_severed}, {@code timeout}).
     *
     * @return the reason, or {@code null} when the turn did not fail
     */
    public String errorReason() {
        if (error == null) {
            return null;
        }
        JSONObject metadata = error.optJSONObject("metadata");
        return metadata == null ? null : metadata.optString("reason", null);
    }

    /**
     * JSON path: {@code error.metadata.retryable} - whether the same request
     * may be sent again unchanged. Branch on this field rather than on
     * {@link #errorReason()} when deciding whether to retry.
     *
     * @return the flag, or {@code null} when the turn did not fail or the
     *         metadata is absent
     */
    public Boolean errorRetryable() {
        if (error == null) {
            return null;
        }
        JSONObject metadata = error.optJSONObject("metadata");
        if (metadata == null || !metadata.has("retryable") || metadata.isNull("retryable")) {
            return null;
        }
        return metadata.optBoolean("retryable");
    }

    /**
     * JSON path: {@code usage} (final chunk) - token usage as the daemon
     * reported it. Always {@code null} after {@code tool_calls} because the
     * turn is not over.
     *
     * @return the raw usage object, or {@code null} when the daemon reported
     *         none
     */
    public JSONObject usage() {
        return usage;
    }

    /**
     * JSON path: {@code usage.prompt_tokens}.
     *
     * @return the prompt tokens, or {@code null} when absent
     */
    public Long promptTokens() {
        return optUsageLong("prompt_tokens");
    }

    /**
     * JSON path: {@code usage.completion_tokens}.
     *
     * @return the completion tokens, or {@code null} when absent
     */
    public Long completionTokens() {
        return optUsageLong("completion_tokens");
    }

    /**
     * JSON path: {@code usage.total_tokens}.
     *
     * @return the total tokens, or {@code null} when absent
     */
    public Long totalTokens() {
        return optUsageLong("total_tokens");
    }

    /**
     * JSON path: {@code usage.cost} - the cost of the run in USD, when the
     * daemon reported one.
     *
     * @return the cost, or {@code null} when absent
     */
    public Double cost() {
        if (usage == null || !usage.has("cost") || usage.isNull("cost")) {
            return null;
        }
        Object value = usage.opt("cost");
        return value instanceof Number number ? number.doubleValue() : null;
    }

    private Long optUsageLong(String key) {
        if (usage == null || !usage.has(key) || usage.isNull(key)) {
            return null;
        }
        Object value = usage.opt(key);
        return value instanceof Number number ? number.longValue() : null;
    }

    /**
     * A typed view of the streamed {@code openrouter.provide_input} tool
     * call (schema {@code InternChatToolCall}).
     */
    public static final class OpenRouterInternToolCall {

        private final JSONObject json;

        OpenRouterInternToolCall(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the raw JSON object behind this view
         */
        public JSONObject json() {
            return json;
        }

        /**
         * JSON path: {@code id} - the interaction id. Send it back as
         * {@code tool_call_id} on the {@code tool} message that answers it.
         *
         * @return the interaction id, or {@code null} when absent
         */
        public String id() {
            return json.optString("id", null);
        }

        /**
         * JSON path: {@code function.name} - always
         * {@code openrouter.provide_input}.
         *
         * @return the tool name, or {@code null} when absent
         */
        public String name() {
            JSONObject function = json.optJSONObject("function");
            return function == null ? null : function.optString("name", null);
        }

        /**
         * JSON path: {@code function.arguments} - a JSON object string
         * describing the interaction: a permission request is
         * {@code {"kind":"permission","operation":<tool name or null>,"options":[...]}}
         * and a question is
         * {@code {"kind":"elicitation","message":<question>,"fields":[...]}}.
         *
         * @return the arguments string, or {@code null} when absent
         */
        public String arguments() {
            JSONObject function = json.optJSONObject("function");
            return function == null ? null : function.optString("arguments", null);
        }

        /**
         * @return the arguments parsed as a JSON object, or {@code null}
         *         when absent or malformed
         */
        public JSONObject argumentsJson() {
            String args = arguments();
            if (args == null) {
                return null;
            }
            try {
                return new JSONObject(args);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
