package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Request-JSON, URL and response-fixture level tests for the interns
 * endpoints, including the documented SSE shapes of the chat endpoint.
 */
class OpenRouterInternsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    private static final class NoopHandler
            implements de.entwicklertraining.api.base.streaming.StreamingResponseHandler<String> {
        @Override
        public void onData(String chunk) {
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable throwable) {
        }
    }

    private NoopHandler noop() {
        return new NoopHandler();
    }

    @Test
    void listRequestEmitsTypedQueryParametersOnlyWhenSet() {
        OpenRouterInternsListRequest without = client().interns().list().build();
        assertThat(without.getRelativeUrl()).isEqualTo("/interns");
        assertThat(without.getHttpMethod()).isEqualTo("GET");
        assertThat(without.getBody()).isNull();

        OpenRouterInternsListRequest with = client().interns().list()
                .limit(50)
                .status("running", "queued")
                .startingAfter("MjAyNi0wOS0xNg")
                .workspaceId("89f9f5b2-3f89-4eaf-83ca-5ceae149e8bb")
                .build();
        assertThat(with.getRelativeUrl()).isEqualTo("/interns?limit=50"
                + "&status=running%2Cqueued"
                + "&starting_after=MjAyNi0wOS0xNg"
                + "&workspace_id=89f9f5b2-3f89-4eaf-83ca-5ceae149e8bb");
    }

    @Test
    void listRequestCollapsesStatusRepeatsAndRejectsMoreThanEight() {
        OpenRouterInternsListRequest request = client().interns().list()
                .status("running", "running", "queued")
                .build();
        assertThat(request.queryParams()).containsEntry("status", "running,queued");

        assertThatThrownBy(() -> client().interns().list()
                .status("a", "b", "c", "d", "e", "f", "g", "h", "i").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8");
        assertThatThrownBy(() -> client().interns().list().limit(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit");
        assertThatThrownBy(() -> client().interns().list().limit(501).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void listResponseSurfacesTypedInterns() {
        OpenRouterInternsListRequest request = client().interns().list().build();
        OpenRouterInternsListResponse response = request.createResponse("""
                {
                  "data": [
                    {"id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                     "name": "research-assistant",
                     "description": "Researches customer questions",
                     "instructions": null,
                     "model": "openai/gpt-5.4",
                     "status": "running",
                     "last_failure_message": null,
                     "progress": {"step_number": 2, "total_steps": 5,
                                  "step_label": "Installing tools"},
                     "hostname": "research-assistant.openrouter.ai",
                     "workspace_id": "89f9f5b2-3f89-4eaf-83ca-5ceae149e8bb",
                     "vault_id": "b431c59d-6eed-41ac-bc89-9a89be79a121",
                     "attached_vault_id": null,
                     "created_at": "2026-09-16T08:30:00.000Z",
                     "updated_at": "2026-09-16T08:45:00.000Z"}
                  ],
                  "has_more": true,
                  "next_cursor": "MjAyNi0wOS0xNg"
                }
                """);

        assertThat(response.interns()).hasSize(1);
        OpenRouterIntern intern = response.interns().get(0);
        assertThat(intern.id()).isEqualTo("7c9e6679-7425-40de-944b-e07fc1f90ae7");
        assertThat(intern.name()).isEqualTo("research-assistant");
        assertThat(intern.model()).isEqualTo("openai/gpt-5.4");
        assertThat(intern.status()).isEqualTo("running");
        assertThat(intern.hostname()).isEqualTo("research-assistant.openrouter.ai");
        assertThat(intern.workspaceId()).isEqualTo("89f9f5b2-3f89-4eaf-83ca-5ceae149e8bb");
        assertThat(intern.vaultId()).isEqualTo("b431c59d-6eed-41ac-bc89-9a89be79a121");
        assertThat(intern.attachedVaultId()).isNull();
        assertThat(intern.progress()).isNotNull();
        assertThat(intern.progress().stepNumber()).isEqualTo(2);
        assertThat(intern.progress().totalSteps()).isEqualTo(5);
        assertThat(intern.progress().stepLabel()).isEqualTo("Installing tools");
        assertThat(response.hasMore()).isTrue();
        assertThat(response.nextCursor()).isEqualTo("MjAyNi0wOS0xNg");
    }

    @Test
    void createRequestEmitsNameAlwaysAndTheRestOnlyWhenSet() {
        OpenRouterInternCreateRequest minimal = client().interns().create("research-bot")
                .build();
        assertThat(minimal.getRelativeUrl()).isEqualTo("/interns");
        assertThat(minimal.getHttpMethod()).isEqualTo("POST");
        JSONObject body = new JSONObject(minimal.getBody());
        assertThat(body.keySet()).containsExactly("name");
        assertThat(body.getString("name")).isEqualTo("research-bot");

        OpenRouterInternCreateRequest full = client().interns().create("research-bot")
                .workspaceId("89f9f5b2-3f89-4eaf-83ca-5ceae149e8bb")
                .description("Researches customer questions")
                .instructions("Be concise.")
                .provision(true)
                .vaultId("b431c59d-6eed-41ac-bc89-9a89be79a121")
                .build();
        JSONObject fullBody = new JSONObject(full.getBody());
        assertThat(fullBody.keySet()).containsExactlyInAnyOrder(
                "name", "workspace_id", "description", "instructions", "provision", "vault_id");
        assertThat(fullBody.getBoolean("provision")).isTrue();
    }

    @Test
    void createRequestRejectsInvalidNames() {
        assertThatThrownBy(() -> client().interns().create("A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2 to 17");
        assertThatThrownBy(() -> client().interns().create("a".repeat(18)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2 to 17");
        assertThatThrownBy(() -> client().interns().create("ResearchAssistant"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must match");
        assertThatThrownBy(() -> client().interns().create("-bad"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must match");
        assertThatThrownBy(() -> client().interns().create("bad--name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must match");
        assertThatThrownBy(() -> client().interns().create("ok-name")
                .description("x".repeat(2001)).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2000");
        assertThatThrownBy(() -> client().interns().create("ok-name")
                .instructions("x".repeat(100_001)).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100000");
        assertThatThrownBy(() -> client().interns().create("ok-name")
                .idempotencyKey("").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idempotency");
        assertThatThrownBy(() -> client().interns().create("ok-name")
                .idempotencyKey("key\r\nInjected: 1").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CR or LF");
    }

    @Test
    void createRequestCarriesTheIdempotencyKeyHeader() {
        OpenRouterInternCreateRequest request = client().interns().create("research-bot")
                .idempotencyKey("create-research-bot-2026-09-16")
                .build();
        assertThat(request.getAdditionalHeaders())
                .containsEntry("Idempotency-Key", "create-research-bot-2026-09-16");
    }

    @Test
    void internResponseSurfacesTheIntern() {
        OpenRouterInternGetRequest request = client().interns().get("7c9e6679").build();
        assertThat(request.getRelativeUrl()).isEqualTo("/interns/7c9e6679");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.internId()).isEqualTo("7c9e6679");

        OpenRouterInternResponse<OpenRouterInternGetRequest> response =
                request.createResponse("""
                {"id": "7c9e6679", "name": "research-assistant", "status": "queued",
                 "description": null, "instructions": null, "model": null,
                 "last_failure_message": null, "progress": null, "hostname": null,
                 "workspace_id": "w1", "vault_id": null, "attached_vault_id": null,
                 "created_at": "2026-09-16T08:30:00.000Z",
                 "updated_at": "2026-09-16T08:30:00.000Z"}
                """);
        assertThat(response.intern()).isNotNull();
        assertThat(response.intern().status()).isEqualTo("queued");
        assertThat(response.intern().progress()).isNull();
    }

    @Test
    void updateRequestEmitsOnlyConfiguredFields() {
        OpenRouterInternUpdateRequest empty = client().interns().update("i1").build();
        assertThat(empty.getRelativeUrl()).isEqualTo("/interns/i1");
        assertThat(empty.getHttpMethod()).isEqualTo("PATCH");
        assertThat(new JSONObject(empty.getBody()).keySet()).isEmpty();

        OpenRouterInternUpdateRequest full = client().interns().update("i1")
                .name("renamed-assistant")
                .description("New description")
                .instructions("New instructions")
                .model("anthropic/claude-sonnet-4.5")
                .build();
        JSONObject body = new JSONObject(full.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "name", "description", "instructions", "model");
        assertThat(body.getString("model")).isEqualTo("anthropic/claude-sonnet-4.5");

        assertThatThrownBy(() -> client().interns().update("i1").name("Bad_Name").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must be");
    }

    @Test
    void deleteRequestEmitsTheConsentOnlyWhenSet() {
        OpenRouterInternDeleteRequest without = client().interns().delete("i1").build();
        assertThat(without.getRelativeUrl()).isEqualTo("/interns/i1");
        assertThat(without.getHttpMethod()).isEqualTo("DELETE");
        assertThat(without.getBody()).isNull();

        OpenRouterInternDeleteRequest with = client().interns().delete("i1")
                .acknowledgeWorkspaceLoss(true)
                .build();
        assertThat(new JSONObject(with.getBody()).getBoolean("acknowledge_workspace_loss"))
                .isTrue();
    }

    @Test
    void lifecycleActionsBuildScopedUrls() {
        assertThat(client().interns().provision("i1").build().getRelativeUrl())
                .isEqualTo("/interns/i1/provision");
        assertThat(client().interns().provision("i1").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().interns().suspend("i1").build().getRelativeUrl())
                .isEqualTo("/interns/i1/suspend");

        OpenRouterInternLifecycleResponse<OpenRouterInternDeleteRequest> deleteResponse =
                client().interns().delete("i1").build().createResponse("{\"deleting\":true}");
        assertThat(deleteResponse.deleting()).isTrue();
        assertThat(deleteResponse.provisioning()).isNull();
        assertThat(deleteResponse.suspended()).isNull();

        OpenRouterInternLifecycleResponse<OpenRouterInternProvisionRequest> provisionResponse =
                client().interns().provision("i1").build().createResponse("{\"provisioning\":true}");
        assertThat(provisionResponse.provisioning()).isTrue();

        OpenRouterInternLifecycleResponse<OpenRouterInternSuspendRequest> suspendResponse =
                client().interns().suspend("i1").build().createResponse("{\"suspended\":true}");
        assertThat(suspendResponse.suspended()).isTrue();
    }

    @Test
    void chatRequestRefusesNonStreamingAndEmptyMessages() {
        assertThatThrownBy(() -> client().interns().chat("i1")
                .addUserMessage("hello").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only streams");
        assertThatThrownBy(() -> client().interns().chat("i1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least one message");
    }

    @Test
    void chatRequestRefusesAToolReplyWithoutSessionId() {
        assertThatThrownBy(() -> client().interns().chat("i1")
                .addToolReply("interaction-1", "allow_once")
                .stream(noop())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("session_id");
    }

    @Test
    void chatRequestBodyCarriesStreamTrueAndOptionalFieldsOnlyWhenSet() {
        OpenRouterInternChatRequest request = client().interns().chat("i1")
                .addUserMessage("Summarize the open pull requests.")
                .stream(noop())
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/interns/i1/chat/completions");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        assertThat(request.isStreamingEnabled()).isTrue();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("messages", "stream");
        assertThat(body.getBoolean("stream")).isTrue();
        assertThat(body.getJSONArray("messages").length()).isEqualTo(1);
        JSONObject firstMessage = body.getJSONArray("messages").getJSONObject(0);
        assertThat(firstMessage.getString("role")).isEqualTo("user");
        assertThat(firstMessage.getString("content"))
                .isEqualTo("Summarize the open pull requests.");

        OpenRouterInternChatRequest full = client().interns().chat("i1")
                .addUserMessage("Continue")
                .approvalMode("manual")
                .sessionId("ses_7f3c9a")
                .stream(noop())
                .build();
        JSONObject fullBody = new JSONObject(full.getBody());
        assertThat(fullBody.keySet()).containsExactlyInAnyOrder(
                "messages", "stream", "approval_mode", "session_id");
    }

    @Test
    void chatRequestEmitsTheToolReplyAndEchoedAssistantForms() {
        OpenRouterInternChatRequest reply = client().interns().chat("i1")
                .addUserMessage("Summarize the open pull requests.")
                .addEchoedAssistantMessage(null, "15e90ad6-5320",
                        "{\"kind\":\"permission\",\"operation\":\"shell\"}")
                .addToolReply("15e90ad6-5320", "allow_once")
                .sessionId("ses_7f3c9a")
                .stream(noop())
                .build();
        org.json.JSONArray messages =
                new JSONObject(reply.getBody()).getJSONArray("messages");
        assertThat(messages.length()).isEqualTo(3);
        assertThat(messages.getJSONObject(1).getJSONArray("tool_calls")
                .getJSONObject(0).getJSONObject("function").getString("name"))
                .isEqualTo("openrouter.provide_input");
        JSONObject toolMessage = messages.getJSONObject(2);
        assertThat(toolMessage.getString("role")).isEqualTo("tool");
        assertThat(toolMessage.getString("tool_call_id")).isEqualTo("15e90ad6-5320");
        assertThat(toolMessage.getString("content")).isEqualTo("allow_once");

        OpenRouterInternChatRequest elicitation = client().interns().chat("i1")
                .addToolReply("15e90ad6-5320",
                        new JSONObject().put("action", "accept")
                                .put("content", new JSONObject().put("answer", "red")))
                .sessionId("ses_7f3c9a")
                .stream(noop())
                .build();
        assertThat(new JSONObject(elicitation.getBody()).getJSONArray("messages")
                .getJSONObject(0).getString("content"))
                .isEqualTo("{\"action\":\"accept\",\"content\":{\"answer\":\"red\"}}");
    }

    @Test
    void chatRequestRejectsAnEchoedToolCallWithoutIdOrArguments() {
        assertThatThrownBy(() -> client().interns().chat("i1")
                .addEchoedAssistantMessage(null, null, "{}").stream(noop()).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toolCallId");
        assertThatThrownBy(() -> client().interns().chat("i1")
                .addEchoedAssistantMessage(null, "call-1", null).stream(noop()).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toolCallArguments");
    }

    @Test
    void chatRequestRejectsInvalidRolesAndApprovalModes() {
        assertThatThrownBy(() -> client().interns().chat("i1")
                .addMessage("tool", "x").stream(noop()).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("role");
        assertThatThrownBy(() -> client().interns().chat("i1")
                .addToolReply(null, "x").stream(noop()).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toolCallId");
        assertThatThrownBy(() -> client().interns().chat("i1")
                .addUserMessage("x").approvalMode("").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("approvalMode");
    }

    @Test
    void chatResponseSurfacesTheSteeredAnswer() {
        OpenRouterInternChatRequest request = client().interns().chat("i1")
                .addUserMessage("hello").stream(noop()).build();
        OpenRouterInternChatResponse steered = request.createResponse(
                "{\"status\":\"steered\",\"session_id\":\"sess_01j9x0k4q7v8r2t3m6n5p8w9y1\"}");
        assertThat(steered.isSteered()).isTrue();
        assertThat(steered.status()).isEqualTo("steered");
        assertThat(steered.sessionId()).isEqualTo("sess_01j9x0k4q7v8r2t3m6n5p8w9y1");

        OpenRouterInternChatResponse other = request.createResponse("{}");
        assertThat(other.isSteered()).isFalse();
        assertThat(other.sessionId()).isNull();
    }

    @Test
    void chatAccumulatorSurfacesTextReasoningSessionIdAndStop() {
        OpenRouterInternChatAccumulator turn = new OpenRouterInternChatAccumulator(null);
        turn.onData("""
                {"id":"chatcmpl-f727","object":"chat.completion.chunk","created":1789537541,
                 "model":"openrouter/intern","choices":[{"index":0,
                 "delta":{"role":"assistant"},"finish_reason":null}]}
                """);
        turn.onData("""
                {"id":"chatcmpl-f727","object":"chat.completion.chunk","created":1789537541,
                 "model":"openrouter/intern","choices":[{"index":0,
                 "delta":{"content":"Working on it."},"finish_reason":null}]}
                """);
        turn.onData("""
                {"id":"chatcmpl-f727","object":"chat.completion.chunk","created":1789537541,
                 "model":"acme/worker-1","choices":[{"index":0,
                 "delta":{"reasoning":"planning"},"finish_reason":null}]}
                """);
        turn.onData("""
                {"id":"chatcmpl-f727","object":"chat.completion.chunk","created":1789537541,
                 "model":"acme/worker-1","choices":[{"index":0,"delta":{},
                 "finish_reason":"stop"}]}
                """);
        turn.onData("""
                {"id":"chatcmpl-f727","object":"chat.completion.chunk","created":1789537541,
                 "model":"openrouter/intern","choices":[],
                 "session_id":"ses_7f3c9a",
                 "usage":{"prompt_tokens":40,"completion_tokens":12,"total_tokens":52,
                          "cost":0.01,"prompt_tokens_details":{"cached_tokens":8}}}
                """);

        assertThat(turn.finishReason()).isEqualTo("stop");
        assertThat(turn.isInteractionPending()).isFalse();
        assertThat(turn.isFailed()).isFalse();
        assertThat(turn.text()).isEqualTo("Working on it.");
        assertThat(turn.reasoning()).isEqualTo("planning");
        assertThat(turn.sessionId()).isEqualTo("ses_7f3c9a");
        assertThat(turn.completionId()).isEqualTo("chatcmpl-f727");
        assertThat(turn.model()).isEqualTo("openrouter/intern");
        assertThat(turn.promptTokens()).isEqualTo(40L);
        assertThat(turn.completionTokens()).isEqualTo(12L);
        assertThat(turn.totalTokens()).isEqualTo(52L);
        assertThat(turn.cost()).isEqualTo(0.01);
        assertThat(turn.toolCall()).isNull();
        assertThat(turn.error()).isNull();
        assertThat(turn.rawChunks()).hasSize(5);
    }

    @Test
    void chatAccumulatorSurfacesTheProvideInputInteraction() {
        OpenRouterInternChatAccumulator turn = new OpenRouterInternChatAccumulator(null);
        turn.onData("""
                {"id":"chatcmpl-f727","object":"chat.completion.chunk","created":1789537541,
                 "model":"openrouter/intern","choices":[{"index":0,
                 "delta":{"tool_calls":[{"id":"15e90ad6-5320-4a59-af4f-b371428154fa",
                   "type":"function","index":0,"function":{"name":"openrouter.provide_input",
                   "arguments":"{\\"kind\\":\\"permission\\",\\"operation\\":\\"shell\\",\\"options\\":[\\"allow_once\\",\\"reject_once\\"]}"}}],
                   "finish_reason":null}}]}
                """);
        turn.onData("""
                {"id":"chatcmpl-f727","object":"chat.completion.chunk","created":1789537541,
                 "model":"openrouter/intern","choices":[{"index":0,"delta":{},
                 "finish_reason":"tool_calls"}]}
                """);
        turn.onData("""
                {"id":"chatcmpl-f727","object":"chat.completion.chunk","created":1789537541,
                 "model":"openrouter/intern","choices":[],"session_id":"ses_7f3c9a"}
                """);

        assertThat(turn.isInteractionPending()).isTrue();
        assertThat(turn.sessionId()).isEqualTo("ses_7f3c9a");
        assertThat(turn.toolCall()).isNotNull();
        assertThat(turn.toolCall().id()).isEqualTo("15e90ad6-5320-4a59-af4f-b371428154fa");
        assertThat(turn.toolCall().name()).isEqualTo("openrouter.provide_input");
        assertThat(turn.toolCall().argumentsJson().getString("kind")).isEqualTo("permission");
        assertThat(turn.toolCall().argumentsJson().getString("operation")).isEqualTo("shell");
        assertThat(turn.usage()).isNull();
    }

    @Test
    void chatAccumulatorSurfacesTheStreamedErrorChunk() {
        OpenRouterInternChatAccumulator turn = new OpenRouterInternChatAccumulator(null);
        turn.onData("""
                {"id":"chatcmpl-f727","object":"chat.completion.chunk","created":1789537541,
                 "model":"openrouter/intern","choices":[{"index":0,"delta":{},
                 "finish_reason":"error"}],
                 "error":{"code":502,"message":"The intern could not continue this run.",
                   "metadata":{"reason":"attachment_failed","retryable":false}}}
                """);

        assertThat(turn.isFailed()).isTrue();
        assertThat(turn.errorCode()).isEqualTo(502);
        assertThat(turn.errorMessage())
                .isEqualTo("The intern could not continue this run.");
        assertThat(turn.errorReason()).isEqualTo("attachment_failed");
        assertThat(turn.errorRetryable()).isFalse();
    }

    @Test
    void chatAccumulatorToleratesMalformedChunks() {
        OpenRouterInternChatAccumulator turn = new OpenRouterInternChatAccumulator(null);
        turn.onData("not json");
        assertThat(turn.rawChunks()).containsExactly("not json");
        assertThat(turn.finishReason()).isNull();
        assertThat(turn.text()).isEmpty();
    }

    @Test
    void invokeRequestEmitsInputAlwaysAndSessionIdOnlyWhenSet() {
        OpenRouterInternInvokeRequest minimal = client().interns()
                .invoke("7c9e6679", "Summarize the open pull requests.").build();
        assertThat(minimal.getRelativeUrl()).isEqualTo("/interns/7c9e6679/invoke");
        assertThat(minimal.getHttpMethod()).isEqualTo("POST");
        assertThat(minimal.internId()).isEqualTo("7c9e6679");
        assertThat(minimal.input()).isEqualTo("Summarize the open pull requests.");
        assertThat(minimal.sessionId()).isNull();
        JSONObject body = new JSONObject(minimal.getBody());
        assertThat(body.keySet()).containsExactly("input");
        assertThat(body.getString("input")).isEqualTo("Summarize the open pull requests.");

        OpenRouterInternInvokeRequest withSession = client().interns()
                .invoke("7c9e6679", "And the closed ones.")
                .sessionId("sess-42")
                .build();
        assertThat(withSession.sessionId()).isEqualTo("sess-42");
        JSONObject withSessionBody = new JSONObject(withSession.getBody());
        assertThat(withSessionBody.keySet()).containsExactly("input", "session_id");
        assertThat(withSessionBody.getString("session_id")).isEqualTo("sess-42");
    }

    @Test
    void invokeRequestValidatesInputAndSessionIdLoudlyAndUnsessionId() {
        assertThatThrownBy(() -> client().interns().invoke("7c9e6679", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("input");
        assertThatThrownBy(() -> client().interns().invoke("7c9e6679", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("input");
        assertThatThrownBy(() -> client().interns().invoke("7c9e6679", "x".repeat(32_001)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32,000");
        client().interns().invoke("7c9e6679", "x".repeat(32_000)).build();
        assertThatThrownBy(() -> client().interns().invoke("7c9e6679", "hi")
                .sessionId(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 to 256");
        assertThatThrownBy(() -> client().interns().invoke("7c9e6679", "hi")
                .sessionId("s".repeat(257)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 to 256");
        client().interns().invoke("7c9e6679", "hi").sessionId("s".repeat(256)).build();

        OpenRouterInternInvokeRequest unset = client().interns()
                .invoke("7c9e6679", "hi")
                .sessionId("sess-42")
                .sessionId(null)
                .build();
        assertThat(unset.sessionId()).isNull();
        assertThat(new JSONObject(unset.getBody()).keySet()).containsExactly("input");
    }

    @Test
    void invokeResponseSurfacesSessionAndStatus() {
        OpenRouterInternInvokeRequest request = client().interns()
                .invoke("7c9e6679", "Summarize the open pull requests.").build();

        OpenRouterInternInvokeResponse started = request.createResponse(
                "{\"session_id\": \"sess-42\", \"status\": \"started\"}");
        assertThat(started.sessionId()).isEqualTo("sess-42");
        assertThat(started.status()).isEqualTo("started");
        assertThat(started.isStarted()).isTrue();
        assertThat(started.isSteered()).isFalse();

        OpenRouterInternInvokeResponse steered = request.createResponse(
                "{\"session_id\": \"sess-42\", \"status\": \"steered\"}");
        assertThat(steered.isSteered()).isTrue();
        assertThat(steered.isStarted()).isFalse();

        OpenRouterInternInvokeResponse empty = request.createResponse("{}");
        assertThat(empty.sessionId()).isNull();
        assertThat(empty.status()).isNull();
        assertThat(empty.isStarted()).isFalse();
        assertThat(empty.isSteered()).isFalse();
    }

    @Test
    void daemonAccessRequestHitsTheDaemonAccessRoute() {
        OpenRouterInternDaemonAccessRequest request = client().interns()
                .daemonAccess("7c9e6679").build();
        assertThat(request.getRelativeUrl()).isEqualTo("/interns/7c9e6679/daemon-access");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.internId()).isEqualTo("7c9e6679");
    }

    @Test
    void invokeAndDaemonAccessUrlEncodeTheInternIdInThePath() {
        OpenRouterInternInvokeRequest invoke = client().interns()
                .invoke("id/with space", "hi").build();
        assertThat(invoke.getRelativeUrl()).isEqualTo("/interns/id%2Fwith+space/invoke");
        assertThat(invoke.internId()).isEqualTo("id%2Fwith+space");

        OpenRouterInternDaemonAccessRequest daemon = client().interns()
                .daemonAccess("id/with space").build();
        assertThat(daemon.getRelativeUrl())
                .isEqualTo("/interns/id%2Fwith+space/daemon-access");
        assertThat(daemon.internId()).isEqualTo("id%2Fwith+space");
    }

    @Test
    void daemonAccessResponseSurfacesOriginAndTokenButRedactsToString() {
        OpenRouterInternDaemonAccessRequest request = client().interns()
                .daemonAccess("7c9e6679").build();
        OpenRouterInternDaemonAccessResponse response = request.createResponse("""
                {"origin": "https://research-assistant-7c9e6679.or.bot",
                 "token": "daemon-token-value"}
                """);
        assertThat(response.origin())
                .isEqualTo("https://research-assistant-7c9e6679.or.bot");
        assertThat(response.token()).isEqualTo("daemon-token-value");
        assertThat(response.toString())
                .isEqualTo("OpenRouterInternDaemonAccessResponse{token=<redacted>}")
                .doesNotContain("daemon-token-value");

        OpenRouterInternDaemonAccessResponse empty = request.createResponse("{}");
        assertThat(empty.origin()).isNull();
        assertThat(empty.token()).isNull();
    }
}
