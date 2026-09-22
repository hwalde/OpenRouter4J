package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.api.base.streaming.SSEStreamProcessor;
import de.entwicklertraining.api.base.streaming.StreamingFormat;
import de.entwicklertraining.api.base.streaming.StreamingInfo;
import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to stream one turn with an intern:
 * POST https://openrouter.ai/api/v1/interns/{internId}/chat/completions
 * (OpenAI chat-completions SSE format; schema
 * {@code InternChatCompletionRequest}).
 *
 * <p>The endpoint <b>only streams</b>: the builder refuses to produce a
 * request without an installed streaming handler (the API answers 400 with
 * reason {@code bad_request} for a missing or {@code false} {@code stream}
 * field). The body automatically carries {@code stream: true}.
 *
 * <p>Messages: only the <b>last</b> message is read. A last {@code user}
 * message starts a run; a last {@code tool} message answers an interaction
 * (requires {@code session_id} - validated loudly). Earlier messages are
 * accepted so ordinary clients can resend history. {@code system} and
 * {@code developer} messages are accepted for client compatibility and not
 * forwarded. There is deliberately no {@code model} builder method: the
 * field is accepted by the API for OpenAI compatibility but never used - the
 * intern runs the model configured on it (change it via the update
 * endpoint).
 *
 * <p>Interaction loop: when the intern needs input it pauses the run and
 * ends the stream with one tool call named {@code openrouter.provide_input}
 * ({@code finish_reason: "tool_calls"}); the final empty-{@code choices}
 * chunk carries the {@code session_id}. Answer with a follow-up request
 * whose last message is a {@code tool} message with the matching
 * {@code tool_call_id} (see {@link Builder#addToolReply(String, String)}).
 * Use {@link OpenRouterInternChatAccumulator} as the handler to get typed
 * accessors to these pieces.
 *
 * <p>Documented traps: a paused run waits 5 minutes - afterwards answering
 * is rejected with {@code 409 interaction_not_pending}; closing an active
 * stream cancels the run; streamed failures arrive as a
 * {@code finish_reason: "error"} chunk with an {@code error.metadata.reason}
 * (afterwards the final chunk and {@code [DONE]} follow); heartbeats arrive
 * as SSE comment lines ({@code : ...}) which the SSE processor ignores;
 * every path answers 404 for keys outside the interns programme; a new user
 * message on a session with a turn already running can be answered with a
 * non-streamed 202 {@code {"status":"steered"}} body (exposed by
 * {@link OpenRouterInternChatResponse}).
 */
public final class OpenRouterInternChatRequest
        extends OpenRouterRequest<OpenRouterInternChatResponse> {

    private static final Set<String> ROLES = Set.of("system", "developer", "user", "assistant");

    private final OpenRouterClient client;
    private final String internId;
    private final List<JSONObject> messages;
    private final String approvalMode;
    private final String sessionId;

    private OpenRouterInternChatRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.internId = builder.internId;
        this.messages = List.copyOf(builder.messages);
        this.approvalMode = builder.approvalMode;
        this.sessionId = builder.sessionId;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String internId() {
        return URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/interns/"
                + URLEncoder.encode(internId, StandardCharsets.UTF_8)
                + "/chat/completions";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * The JSON body: {@code messages} and {@code stream: true} always, plus
     * {@code approval_mode} and {@code session_id} only when set.
     *
     * @return the JSON body string
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("messages", new JSONArray(messages));
        body.put("stream", true);
        if (approvalMode != null) {
            body.put("approval_mode", approvalMode);
        }
        if (sessionId != null) {
            body.put("session_id", sessionId);
        }
        return body.toString();
    }

    @Override
    public OpenRouterInternChatResponse createResponse(String responseBody) {
        return new OpenRouterInternChatResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterInternChatRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternChatRequest> {

        private final OpenRouterClient client;
        private final String internId;
        private final List<JSONObject> messages = new ArrayList<>();
        private String approvalMode;
        private String sessionId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param internId the id (UUID) of the intern
         */
        public Builder(OpenRouterClient client, String internId) {
            super(client);
            this.client = client;
            this.internId = internId;
        }

        /**
         * Adds a message with a plain string content. Only the last message
         * is read: a last {@code user} message starts a run, a last
         * {@code assistant} message echoes history. Roles
         * {@code system}/{@code developer} are accepted for compatibility
         * and not forwarded. Valid roles: {@code system}, {@code developer},
         * {@code user}, {@code assistant} (use
         * {@link #addToolReply(String, String)} for the {@code tool} role).
         *
         * @param role the message role
         * @param content the message text
         * @return this builder
         */
        public Builder addMessage(String role, String content) {
            if (role == null || !ROLES.contains(role)) {
                throw new IllegalArgumentException(
                        "role must be one of " + ROLES
                                + " - use addToolReply for the tool role");
            }
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("content must not be null or empty");
            }
            messages.add(new JSONObject().put("role", role).put("content", content));
            return this;
        }

        /**
         * Adds a {@code user} message - when it is the last message its text
         * is the prompt for a new run (at most 32,000 characters).
         *
         * @param content the prompt text
         * @return this builder
         */
        public Builder addUserMessage(String content) {
            return addMessage("user", content);
        }

        /**
         * Adds an {@code assistant} message echoing history. When answering
         * an interaction, echo the streamed {@code tool_calls} here before
         * the {@code tool} message - use
         * {@link #addEchoedAssistantMessage(String, String, String)} for
         * that form.
         *
         * @param content the assistant text, or {@code null} for a
         *                content-less tool-call echo
         * @return this builder
         */
        public Builder addAssistantMessage(String content) {
            JSONObject message = new JSONObject().put("role", "assistant");
            if (content != null && !content.isEmpty()) {
                message.put("content", content);
            }
            messages.add(message);
            return this;
        }

        /**
         * Adds an {@code assistant} message echoing the streamed
         * {@code openrouter.provide_input} tool call, as documented for
         * answering an interaction: the tool call verbatim (its {@code id}
         * is the interaction the following {@code tool} message answers).
         *
         * @param content the assistant text, or {@code null} for none
         * @param toolCallId the streamed tool call id
         * @param toolCallArguments the streamed arguments JSON string
         * @return this builder
         */
        public Builder addEchoedAssistantMessage(String content, String toolCallId,
                String toolCallArguments) {
            JSONObject message = new JSONObject().put("role", "assistant");
            if (content != null && !content.isEmpty()) {
                message.put("content", content);
            }
            JSONObject function = new JSONObject()
                    .put("name", "openrouter.provide_input")
                    .put("arguments", toolCallArguments);
            JSONObject toolCall = new JSONObject()
                    .put("id", toolCallId)
                    .put("function", function);
            message.put("tool_calls", new JSONArray().put(toolCall));
            messages.add(message);
            return this;
        }

        /**
         * Adds the {@code tool} message answering an interaction:
         * {@code {"role":"tool","tool_call_id":...,"content":...}}. The
         * {@code tool_call_id} is the streamed tool call id; the request
         * then requires {@code session_id} (validated loudly in
         * {@link #build()}). For a permission interaction the content is one
         * of the offered option kinds ({@code allow_once},
         * {@code allow_always}, {@code reject_once}, {@code reject_always})
         * or {@code cancel}; for a question (elicitation) it is a JSON
         * object string - use {@link #addToolReply(String, JSONObject)} for
         * that form.
         *
         * @param toolCallId the interaction id from the streamed tool call
         * @param content the answer text
         * @return this builder
         */
        public Builder addToolReply(String toolCallId, String content) {
            return toolReply(toolCallId, content);
        }

        /**
         * Adds the {@code tool} message answering an elicitation
         * interaction: the content travels as the JSON object string the API
         * documents - {@code {"action":"accept","content":{...}}},
         * {@code {"action":"decline"}} or {@code {"action":"cancel"}}.
         *
         * @param toolCallId the interaction id from the streamed tool call
         * @param content the answer as a JSON object (serialized verbatim
         *                into the string content)
         * @return this builder
         */
        public Builder addToolReply(String toolCallId, JSONObject content) {
            if (content == null) {
                throw new IllegalArgumentException("content must not be null");
            }
            return toolReply(toolCallId, content.toString());
        }

        private Builder toolReply(String toolCallId, String content) {
            if (toolCallId == null || toolCallId.isEmpty()) {
                throw new IllegalArgumentException("toolCallId must not be null or empty");
            }
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("content must not be null or empty");
            }
            messages.add(new JSONObject()
                    .put("role", "tool")
                    .put("tool_call_id", toolCallId)
                    .put("content", content));
            return this;
        }

        /**
         * Sets the JSON field {@code approval_mode} - how the run started by
         * this prompt handles tool approvals. Documented values:
         * {@code self-drive} (the default when omitted; consents on your
         * behalf and runs the shell unsandboxed) and {@code manual} (asks
         * before an approval-bearing tool runs, as an
         * {@code openrouter.provide_input} permission request). The mode
         * applies to the run this prompt starts and is not remembered by the
         * session; a {@code tool} reply continues the run under the mode it
         * started with. Unknown values are passed through verbatim.
         *
         * @param approvalMode the approval mode
         * @return this builder
         */
        public Builder approvalMode(String approvalMode) {
            if (approvalMode == null || approvalMode.isEmpty()) {
                throw new IllegalArgumentException("approvalMode must not be null or empty");
            }
            this.approvalMode = approvalMode;
            return this;
        }

        /**
         * Sets the JSON field {@code session_id} - the daemon session to
         * continue, as returned on the final chunk of an earlier response.
         * Omit it to start a new session. Trap: an id the intern has not
         * seen before is not an error - it starts a new session under that
         * id, so a mistyped id forks the conversation. Required when the
         * last message has role {@code tool} (validated loudly in
         * {@link #build()}).
         *
         * @param sessionId the session id (1-256 characters)
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            if (sessionId != null && (sessionId.isEmpty() || sessionId.length() > 256)) {
                throw new IllegalArgumentException("sessionId must be 1 to 256 characters");
            }
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Installs the streaming handler and enables SSE streaming - every
         * chat-completions chunk arrives as its raw JSON string on
         * {@code onData}, terminated by the {@code [DONE]} sentinel. The
         * body automatically carries {@code stream: true}. Wrap the handler
         * in an {@link OpenRouterInternChatAccumulator} for typed access to
         * {@code finish_reason}, the {@code openrouter.provide_input} tool
         * call and the {@code session_id}.
         *
         * @param handler the streaming event handler
         * @return this builder
         */
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
            return streamingInfo != null && streamingInfo.isEnabled();
        }

        @Override
        public OpenRouterInternChatRequest build() {
            if (messages.isEmpty()) {
                throw new IllegalStateException(
                        "at least one message is required (only the last one is read)");
            }
            if (!streamRequested()) {
                throw new IllegalStateException(
                        "the intern chat endpoint only streams - install a handler via"
                                + " stream(handler); the API refuses a missing or false"
                                + " stream field with 400 (reason bad_request)");
            }
            String lastRole = messages.get(messages.size() - 1).optString("role");
            if ("tool".equals(lastRole) && sessionId == null) {
                throw new IllegalStateException(
                        "a tool reply requires session_id - send the session_id returned"
                                + " on the final chunk of the interaction's stream");
            }
            return new OpenRouterInternChatRequest(this);
        }

        @Override
        public OpenRouterInternChatResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternChatResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternChatResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
