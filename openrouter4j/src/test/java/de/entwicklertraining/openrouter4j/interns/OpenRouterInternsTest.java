package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultCustomOrigin;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultGithubApp;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultGithubInstallation;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultGithubToken;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultGoogleAccount;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultMcpOrigin;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultRoutingTable;
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

        OpenRouterInternConnectionsRequest connections = client().interns()
                .connections("id/with space").build();
        assertThat(connections.getRelativeUrl())
                .isEqualTo("/vault/interns/id%2Fwith+space/connections");
        assertThat(connections.internId()).isEqualTo("id%2Fwith+space");
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

    @Test
    void connectionsRequestHitsTheVaultConnectionsRoute() {
        OpenRouterInternConnectionsRequest request = client().interns()
                .connections("7c9e6679").build();
        assertThat(request.getRelativeUrl()).isEqualTo("/vault/interns/7c9e6679/connections");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.internId()).isEqualTo("7c9e6679");
    }

    @Test
    void connectionsResponseSurfacesKeysRoutingTableAndText() {
        OpenRouterInternConnectionsRequest request = client().interns()
                .connections("7c9e6679").build();
        OpenRouterInternConnectionsResponse response = request.createResponse("""
                {
                  "data": [{"key": "github"}, {"key": "linear"}, {"key": "mcp:api.example.com"}],
                  "routing": {
                    "agent_id": "7c9e6679",
                    "read_at": "2026-09-15T17:44:00.000Z",
                    "github": {
                      "apps": [
                        {
                          "app_id": "123456",
                          "app_slug": "acme-bot",
                          "scope": "workspace",
                          "installations": [
                            {
                              "credential_id": "cred-1",
                              "account": "acme-org",
                              "account_type": "organization",
                              "is_enabled": true,
                              "is_off_for_intern": false,
                              "holds_token": true,
                              "coverage": {
                                "selection": "selected",
                                "repository_count": 12,
                                "checked_at": "2026-09-15T17:40:00.000Z"
                              },
                              "shadowed_by": null
                            },
                            {
                              "credential_id": "cred-2",
                              "account": "acme-org",
                              "account_type": null,
                              "is_enabled": true,
                              "is_off_for_intern": true,
                              "holds_token": false,
                              "coverage": null,
                              "shadowed_by": "installation"
                            }
                          ],
                          "member_links": [
                            {"login": "lea", "via": "app_link", "holds_token": true}
                          ]
                        }
                      ],
                      "tokens": [
                        {"login": "lea", "owner": "acme-org", "repository_count": 3,
                         "scope": "intern", "holds_token": true}
                      ]
                    },
                    "google": {
                      "accounts": [
                        {"email": "lea@acme.example", "scope": "workspace",
                         "is_delegated_to_intern": true,
                         "delegated_capabilities": ["drive.readonly"],
                         "holds_grant": true}
                      ]
                    },
                    "slack": {
                      "member_links": [{"slack_user_id": "U123", "holds_token": true}]
                    },
                    "mcp_origins": [
                      {
                        "host": "mcp.linear.app",
                        "account_key": "linear",
                        "is_personal": false,
                        "routes": [{"host": "mcp.linear.app", "path_prefix": "/mcp"}],
                        "status": {"state": "usable", "failure": null}
                      },
                      {
                        "host": "mcp.notion.so",
                        "account_key": null,
                        "is_personal": true,
                        "routes": [],
                        "status": null
                      }
                    ],
                    "host_rules": [
                      {"host": "api.github.com", "secret_name": "github_token",
                       "holds_secret": true}
                    ],
                    "custom_origins": [
                      {"host": "api.acme.example", "path_prefix": "/v1",
                       "status": {"state": "failing", "failure": "expired"}}
                    ],
                    "secrets": [
                      {"name": "linear_api_key", "hosts": ["api.linear.app"],
                       "scope": "workspace", "created_at": "2026-09-15T17:44:00.000Z"},
                      {"name": "legacy_token", "hosts": null,
                       "scope": "future_scope", "created_at": "2026-08-01T09:30:00.000Z"}
                    ]
                  },
                  "routing_text": "## Connections\\n\\n..."
                }
                """);

        assertThat(response.connections()).hasSize(3);
        assertThat(response.connections().get(0).key()).isEqualTo("github");
        assertThat(response.connections().get(1).key()).isEqualTo("linear");
        assertThat(response.connections().get(2).key()).isEqualTo("mcp:api.example.com");
        assertThat(response.connections().get(0).json().has("key")).isTrue();
        assertThat(response.routingText()).isEqualTo("## Connections\n\n...");

        OpenRouterVaultRoutingTable routing = response.routing();
        assertThat(routing).isNotNull();
        assertThat(routing.agentId()).isEqualTo("7c9e6679");
        assertThat(routing.readAt()).isEqualTo("2026-09-15T17:44:00.000Z");
        assertThat(routing.json().has("github")).isTrue();

        assertThat(routing.githubApps()).hasSize(1);
        OpenRouterVaultGithubApp app = routing.githubApps().get(0);
        assertThat(app.appId()).isEqualTo("123456");
        assertThat(app.appSlug()).isEqualTo("acme-bot");
        assertThat(app.scope()).isEqualTo("workspace");
        assertThat(app.installations()).hasSize(2);
        OpenRouterVaultGithubInstallation installation = app.installations().get(0);
        assertThat(installation.credentialId()).isEqualTo("cred-1");
        assertThat(installation.account()).isEqualTo("acme-org");
        assertThat(installation.accountType()).isEqualTo("organization");
        assertThat(installation.isEnabled()).isTrue();
        assertThat(installation.isOffForIntern()).isFalse();
        assertThat(installation.holdsToken()).isTrue();
        assertThat(installation.shadowedBy()).isNull();
        assertThat(installation.coverage()).isNotNull();
        assertThat(installation.coverage().selection()).isEqualTo("selected");
        assertThat(installation.coverage().repositoryCount()).isEqualTo(12L);
        assertThat(installation.coverage().checkedAt()).isEqualTo("2026-09-15T17:40:00.000Z");
        assertThat(installation.coverage().json().has("selection")).isTrue();
        OpenRouterVaultGithubInstallation shadowed = app.installations().get(1);
        assertThat(shadowed.accountType()).isNull();
        assertThat(shadowed.coverage()).isNull();
        assertThat(shadowed.shadowedBy()).isEqualTo("installation");
        assertThat(app.memberLinks()).hasSize(1);
        assertThat(app.memberLinks().get(0).login()).isEqualTo("lea");
        assertThat(app.memberLinks().get(0).via()).isEqualTo("app_link");
        assertThat(app.memberLinks().get(0).holdsToken()).isTrue();

        assertThat(routing.githubTokens()).hasSize(1);
        OpenRouterVaultGithubToken token = routing.githubTokens().get(0);
        assertThat(token.login()).isEqualTo("lea");
        assertThat(token.owner()).isEqualTo("acme-org");
        assertThat(token.repositoryCount()).isEqualTo(3L);
        assertThat(token.scope()).isEqualTo("intern");
        assertThat(token.holdsToken()).isTrue();

        assertThat(routing.googleAccounts()).hasSize(1);
        OpenRouterVaultGoogleAccount account = routing.googleAccounts().get(0);
        assertThat(account.email()).isEqualTo("lea@acme.example");
        assertThat(account.scope()).isEqualTo("workspace");
        assertThat(account.isDelegatedToIntern()).isTrue();
        assertThat(account.delegatedCapabilities()).containsExactly("drive.readonly");
        assertThat(account.holdsGrant()).isTrue();

        assertThat(routing.slackMemberLinks()).hasSize(1);
        assertThat(routing.slackMemberLinks().get(0).slackUserId()).isEqualTo("U123");
        assertThat(routing.slackMemberLinks().get(0).holdsToken()).isTrue();

        assertThat(routing.mcpOrigins()).hasSize(2);
        OpenRouterVaultMcpOrigin origin = routing.mcpOrigins().get(0);
        assertThat(origin.host()).isEqualTo("mcp.linear.app");
        assertThat(origin.accountKey()).isEqualTo("linear");
        assertThat(origin.isPersonal()).isFalse();
        assertThat(origin.routes()).hasSize(1);
        assertThat(origin.routes().get(0).host()).isEqualTo("mcp.linear.app");
        assertThat(origin.routes().get(0).pathPrefix()).isEqualTo("/mcp");
        assertThat(origin.status()).isNotNull();
        assertThat(origin.status().state()).isEqualTo("usable");
        assertThat(origin.status().failure()).isNull();
        OpenRouterVaultMcpOrigin personal = routing.mcpOrigins().get(1);
        assertThat(personal.accountKey()).isNull();
        assertThat(personal.isPersonal()).isTrue();
        assertThat(personal.routes()).isEmpty();
        assertThat(personal.status()).isNull();

        assertThat(routing.hostRules()).hasSize(1);
        assertThat(routing.hostRules().get(0).host()).isEqualTo("api.github.com");
        assertThat(routing.hostRules().get(0).secretName()).isEqualTo("github_token");
        assertThat(routing.hostRules().get(0).holdsSecret()).isTrue();

        assertThat(routing.customOrigins()).hasSize(1);
        OpenRouterVaultCustomOrigin custom = routing.customOrigins().get(0);
        assertThat(custom.host()).isEqualTo("api.acme.example");
        assertThat(custom.pathPrefix()).isEqualTo("/v1");
        assertThat(custom.status().state()).isEqualTo("failing");
        assertThat(custom.status().failure()).isEqualTo("expired");

        assertThat(routing.secrets()).hasSize(2);
        assertThat(routing.secrets().get(0).name()).isEqualTo("linear_api_key");
        assertThat(routing.secrets().get(0).hosts()).containsExactly("api.linear.app");
        assertThat(routing.secrets().get(0).scope()).isEqualTo("workspace");
        assertThat(routing.secrets().get(0).createdAt()).isEqualTo("2026-09-15T17:44:00.000Z");
        // hosts: null = released on any request; unknown scope passes through
        assertThat(routing.secrets().get(1).hosts()).isNull();
        assertThat(routing.secrets().get(1).scope()).isEqualTo("future_scope");
    }

    @Test
    void connectionsResponseKeepsKeysWhenRoutingIsNullForAForeignCaller() {
        OpenRouterInternConnectionsRequest request = client().interns()
                .connections("7c9e6679").build();
        OpenRouterInternConnectionsResponse response = request.createResponse("""
                {"data": [{"key": "github"}], "routing": null, "routing_text": null}
                """);

        // the keys still list for a caller other than the intern
        assertThat(response.connections()).hasSize(1);
        assertThat(response.connections().get(0).key()).isEqualTo("github");
        assertThat(response.routing()).isNull();
        assertThat(response.routingText()).isNull();
    }

    @Test
    void connectionsResponseSwallowsMalformedShapes() {
        OpenRouterInternConnectionsRequest request = client().interns()
                .connections("7c9e6679").build();
        OpenRouterInternConnectionsResponse response = request.createResponse("""
                {
                  "data": [{"key": 7}, "not-an-object", {"key": "github"}],
                  "routing": "table",
                  "routing_text": 42
                }
                """);

        assertThat(response.connections()).hasSize(2);
        assertThat(response.connections().get(0).key()).isNull();
        assertThat(response.connections().get(1).key()).isEqualTo("github");
        assertThat(response.routing()).isNull();
        assertThat(response.routingText()).isNull();

        OpenRouterInternConnectionsResponse nested = request.createResponse("""
                {
                  "data": [],
                  "routing": {
                    "agent_id": 5,
                    "read_at": [],
                    "github": {"apps": ["nope", {"app_id": 7, "scope": true,
                                 "installations": [{"credential_id": 1,
                                  "coverage": {"selection": 9, "repository_count": "many"},
                                  "shadowed_by": 8}],
                                 "member_links": ["bad", {"login": 5, "via": 6,
                                                  "holds_token": "yes"}]}],
                               "tokens": ["bad", {"login": 5, "owner": 6,
                                          "repository_count": "n", "scope": 7,
                                          "holds_token": 8},
                                          {"login": "ok", "owner": null,
                                           "repository_count": 2, "scope": "intern",
                                           "holds_token": true}]},
                    "google": {"accounts": ["bad", {"email": 3, "scope": 4,
                                 "is_delegated_to_intern": "yes",
                                 "delegated_capabilities": ["ok", 4],
                                 "holds_grant": "yes"}]},
                    "slack": {"member_links": ["bad", {"slack_user_id": null,
                                      "holds_token": 1}]},
                    "mcp_origins": ["bad", {"host": 4, "account_key": 5,
                                    "is_personal": "no",
                                    "routes": [{"host": 6, "path_prefix": 7}],
                                    "status": {"state": 8, "failure": 9}}],
                    "host_rules": ["bad", {"host": 1, "secret_name": 2,
                                   "holds_secret": 3}],
                    "custom_origins": ["bad", {"host": 1, "path_prefix": 2,
                                       "status": "usable"}],
                    "secrets": ["bad", {"name": 1, "hosts": "x", "scope": 2,
                                "created_at": 3},
                                {"name": "ok", "hosts": ["a", 2], "scope": "intern",
                                 "created_at": "t"}]
                  },
                  "routing_text": "ok"
                }
                """);

        OpenRouterVaultRoutingTable routing = nested.routing();
        assertThat(routing).isNotNull();
        assertThat(routing.agentId()).isNull();
        assertThat(routing.readAt()).isNull();
        // non-object rows are dropped while good rows survive; wrong-typed
        // fields are swallowed to null at every level
        assertThat(routing.githubApps()).hasSize(1);
        OpenRouterVaultGithubApp app = routing.githubApps().get(0);
        assertThat(app.appId()).isNull();
        assertThat(app.scope()).isNull();
        assertThat(app.memberLinks()).hasSize(1);
        assertThat(app.memberLinks().get(0).login()).isNull();
        assertThat(app.memberLinks().get(0).via()).isNull();
        assertThat(app.memberLinks().get(0).holdsToken()).isNull();
        assertThat(app.installations()).hasSize(1);
        assertThat(app.installations().get(0).credentialId()).isNull();
        assertThat(app.installations().get(0).coverage()).isNotNull();
        assertThat(app.installations().get(0).coverage().selection()).isNull();
        assertThat(app.installations().get(0).coverage().repositoryCount()).isNull();
        assertThat(app.installations().get(0).shadowedBy()).isNull();
        assertThat(routing.githubTokens()).hasSize(2);
        assertThat(routing.githubTokens().get(0).login()).isNull();
        assertThat(routing.githubTokens().get(0).owner()).isNull();
        assertThat(routing.githubTokens().get(0).repositoryCount()).isNull();
        assertThat(routing.githubTokens().get(0).scope()).isNull();
        assertThat(routing.githubTokens().get(0).holdsToken()).isNull();
        assertThat(routing.githubTokens().get(1).login()).isEqualTo("ok");
        assertThat(routing.githubTokens().get(1).repositoryCount()).isEqualTo(2L);
        assertThat(routing.googleAccounts()).hasSize(1);
        assertThat(routing.googleAccounts().get(0).email()).isNull();
        assertThat(routing.googleAccounts().get(0).scope()).isNull();
        assertThat(routing.googleAccounts().get(0).isDelegatedToIntern()).isNull();
        assertThat(routing.googleAccounts().get(0).delegatedCapabilities()).containsExactly("ok");
        assertThat(routing.googleAccounts().get(0).holdsGrant()).isNull();
        assertThat(routing.slackMemberLinks()).hasSize(1);
        assertThat(routing.slackMemberLinks().get(0).slackUserId()).isNull();
        assertThat(routing.slackMemberLinks().get(0).holdsToken()).isNull();
        assertThat(routing.mcpOrigins()).hasSize(1);
        assertThat(routing.mcpOrigins().get(0).host()).isNull();
        assertThat(routing.mcpOrigins().get(0).accountKey()).isNull();
        assertThat(routing.mcpOrigins().get(0).isPersonal()).isNull();
        assertThat(routing.mcpOrigins().get(0).routes()).hasSize(1);
        assertThat(routing.mcpOrigins().get(0).routes().get(0).host()).isNull();
        assertThat(routing.mcpOrigins().get(0).routes().get(0).pathPrefix()).isNull();
        assertThat(routing.mcpOrigins().get(0).status()).isNotNull();
        assertThat(routing.mcpOrigins().get(0).status().state()).isNull();
        assertThat(routing.mcpOrigins().get(0).status().failure()).isNull();
        assertThat(routing.hostRules()).hasSize(1);
        assertThat(routing.hostRules().get(0).host()).isNull();
        assertThat(routing.hostRules().get(0).secretName()).isNull();
        assertThat(routing.hostRules().get(0).holdsSecret()).isNull();
        assertThat(routing.customOrigins()).hasSize(1);
        assertThat(routing.customOrigins().get(0).host()).isNull();
        assertThat(routing.customOrigins().get(0).pathPrefix()).isNull();
        assertThat(routing.customOrigins().get(0).status()).isNull();
        assertThat(routing.secrets()).hasSize(2);
        assertThat(routing.secrets().get(0).name()).isNull();
        assertThat(routing.secrets().get(0).hosts()).isNull();
        assertThat(routing.secrets().get(0).scope()).isNull();
        assertThat(routing.secrets().get(0).createdAt()).isNull();
        assertThat(routing.secrets().get(1).hosts()).containsExactly("a");
        assertThat(nested.routingText()).isEqualTo("ok");

        OpenRouterInternConnectionsResponse absent = request.createResponse("{}");
        assertThat(absent.connections()).isEmpty();
        assertThat(absent.routing()).isNull();
        assertThat(absent.routingText()).isNull();
    }
}
