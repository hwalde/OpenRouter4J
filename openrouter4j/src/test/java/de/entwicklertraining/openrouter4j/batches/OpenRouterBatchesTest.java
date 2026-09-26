package de.entwicklertraining.openrouter4j.batches;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbedding;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsRequest;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsResponse;
import de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesRequest;
import de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesResponse;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesRequest;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the Batch API requests (URL shape, body emission and the documented
 * wire order, query parameters, loud validation) and the response views
 * against recorded JSON shapes of the OpenRouter /batches endpoints.
 */
class OpenRouterBatchesTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    private OpenRouterBatchSubmitRequest.Builder submitBuilder() {
        return new OpenRouterBatchSubmitRequest.Builder(client())
                .endpoint(OpenRouterBatchEndpoint.CHAT_COMPLETIONS)
                .model("openai/gpt-4o")
                .addRequestBody("req-0001", new JSONObject()
                        .put("messages", new JSONArray()
                                .put(new JSONObject().put("role", "user").put("content", "Hi"))));
    }

    @Test
    void submitRequestUsesPostMethodOnBatchesUrl() {
        OpenRouterBatchSubmitRequest request = submitBuilder().build();

        assertThat(request.getRelativeUrl()).isEqualTo("/batches");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void submitBodySerializesEndpointModelProviderAndCompletionWindowBeforeRequests() {
        OpenRouterBatchSubmitRequest request = submitBuilder()
                .providerOnly("google-vertex")
                .completionWindow("24h")
                .build();

        String body = request.getBody();
        int endpoint = body.indexOf("\"endpoint\"");
        int model = body.indexOf("\"model\"");
        int provider = body.indexOf("\"provider\"");
        int completionWindow = body.indexOf("\"completion_window\"");
        int requests = body.indexOf("\"requests\"");

        assertThat(endpoint).isNotNegative();
        assertThat(model).isGreaterThan(endpoint);
        assertThat(provider).isGreaterThan(model);
        assertThat(completionWindow).isGreaterThan(provider);
        assertThat(requests).isGreaterThan(completionWindow);
    }

    @Test
    void submitBodySerializesOptionalFieldsBeforeRequestsWithoutTheOptionalFields() {
        String body = submitBuilder().build().getBody();

        assertThat(body.indexOf("\"endpoint\""))
                .isLessThan(body.indexOf("\"requests\""));
        assertThat(body.indexOf("\"model\""))
                .isLessThan(body.indexOf("\"requests\""));
        assertThat(body).doesNotContain("\"provider\"");
        assertThat(body).doesNotContain("\"completion_window\"");
    }

    @Test
    void submitBodyEmitsRequiredFieldsAndItemsWithBodiesVerbatim() {
        OpenRouterBatchSubmitRequest request = submitBuilder().build();

        JSONObject json = new JSONObject(request.getBody());
        assertThat(json.keySet()).containsExactlyInAnyOrder("endpoint", "model", "requests");
        assertThat(json.getString("endpoint")).isEqualTo("/v1/chat/completions");
        assertThat(json.getString("model")).isEqualTo("openai/gpt-4o");

        JSONArray requests = json.getJSONArray("requests");
        assertThat(requests.toList()).hasSize(1);
        JSONObject item = requests.getJSONObject(0);
        assertThat(item.keySet()).containsExactlyInAnyOrder("custom_id", "body");
        assertThat(item.getString("custom_id")).isEqualTo("req-0001");
        assertThat(item.getJSONObject("body").getJSONArray("messages").getJSONObject(0)
                .getString("content")).isEqualTo("Hi");
    }

    @Test
    void submitBodyEmitsEveryEndpointShape() {
        for (OpenRouterBatchEndpoint endpoint : OpenRouterBatchEndpoint.values()) {
            JSONObject json = new JSONObject(new OpenRouterBatchSubmitRequest.Builder(client())
                    .endpoint(endpoint)
                    .model("openai/gpt-4o")
                    .addRequestBody("req-0001", new JSONObject().put("input", "Hi"))
                    .build()
                    .getBody());
            assertThat(json.getString("endpoint")).isEqualTo(endpoint.wireName());
        }
        assertThat(OpenRouterBatchEndpoint.CHAT_COMPLETIONS.wireName()).isEqualTo("/v1/chat/completions");
        assertThat(OpenRouterBatchEndpoint.RESPONSES.wireName()).isEqualTo("/v1/responses");
        assertThat(OpenRouterBatchEndpoint.MESSAGES.wireName()).isEqualTo("/v1/messages");
        assertThat(OpenRouterBatchEndpoint.EMBEDDINGS.wireName()).isEqualTo("/v1/embeddings");
    }

    @Test
    void submitBodyEmitsProviderOnlyObject() {
        JSONObject json = new JSONObject(submitBuilder()
                .providerOnly("openai", "anthropic")
                .build()
                .getBody());

        JSONObject provider = json.getJSONObject("provider");
        assertThat(provider.keySet()).containsExactly("only");
        assertThat(provider.getJSONArray("only").toList()).containsExactly("openai", "anthropic");
    }

    @Test
    void submitBodyEmitsCompletionWindowAndOmitsItWhenUnset() {
        JSONObject with = new JSONObject(submitBuilder().completionWindow("24h").build().getBody());
        assertThat(with.getString("completion_window")).isEqualTo("24h");

        JSONObject without = new JSONObject(submitBuilder().completionWindow(null).build().getBody());
        assertThat(without.has("completion_window")).isFalse();
    }

    @Test
    void submitRequestReadBackAccessorsReturnTheConfiguredValues() {
        OpenRouterBatchSubmitRequest request = submitBuilder()
                .providerOnly("google-vertex")
                .completionWindow("24h")
                .build();

        assertThat(request.endpoint()).isEqualTo(OpenRouterBatchEndpoint.CHAT_COMPLETIONS);
        assertThat(request.model()).isEqualTo("openai/gpt-4o");
        assertThat(request.requests()).hasSize(1);
        assertThat(request.requests().get(0).customId()).isEqualTo("req-0001");
        assertThat(request.providerOnly()).containsExactly("google-vertex");
        assertThat(request.completionWindow()).isEqualTo("24h");

        OpenRouterBatchSubmitRequest plain = submitBuilder().build();
        assertThat(plain.providerOnly()).isNull();
        assertThat(plain.completionWindow()).isNull();
    }

    @Test
    void submitBuilderRejectsMissingRequiredFieldsLoudly() {
        assertThatThrownBy(() -> new OpenRouterBatchSubmitRequest.Builder(client())
                .model("openai/gpt-4o")
                .addRequestBody("req-0001", new JSONObject())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("endpoint is required");

        assertThatThrownBy(() -> new OpenRouterBatchSubmitRequest.Builder(client())
                .endpoint(OpenRouterBatchEndpoint.CHAT_COMPLETIONS)
                .addRequestBody("req-0001", new JSONObject())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("model is required");

        assertThatThrownBy(() -> new OpenRouterBatchSubmitRequest.Builder(client())
                .endpoint(OpenRouterBatchEndpoint.CHAT_COMPLETIONS)
                .model(" ")
                .addRequestBody("req-0001", new JSONObject())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("model is required");

        assertThatThrownBy(() -> new OpenRouterBatchSubmitRequest.Builder(client())
                .endpoint(OpenRouterBatchEndpoint.CHAT_COMPLETIONS)
                .model("openai/gpt-4o")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requests must not be empty");
    }

    @Test
    void submitBuilderRejectsDuplicateCustomIdsLoudly() {
        assertThatThrownBy(() -> new OpenRouterBatchSubmitRequest.Builder(client())
                .endpoint(OpenRouterBatchEndpoint.CHAT_COMPLETIONS)
                .model("openai/gpt-4o")
                .addRequestBody("req-0001", new JSONObject().put("input", "a"))
                .addRequestBody("req-0001", new JSONObject().put("input", "b"))
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("custom_id must be unique")
                .hasMessageContaining("req-0001");
    }

    @Test
    void submitBuilderRejectsItemModelMismatchLoudlyButAcceptsMatchAndOmittedModel() {
        assertThatThrownBy(() -> new OpenRouterBatchSubmitRequest.Builder(client())
                .endpoint(OpenRouterBatchEndpoint.CHAT_COMPLETIONS)
                .model("openai/gpt-4o")
                .addRequestBody("req-0001", new JSONObject().put("model", "openai/gpt-4o-mini"))
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must match the batch-level model");

        JSONObject matched = new JSONObject(new OpenRouterBatchSubmitRequest.Builder(client())
                .endpoint(OpenRouterBatchEndpoint.CHAT_COMPLETIONS)
                .model("openai/gpt-4o")
                .addRequestBody("req-0001", new JSONObject().put("model", "openai/gpt-4o"))
                .addRequestBody("req-0002", new JSONObject().put("input", "no model"))
                .build()
                .getBody());
        assertThat(matched.getJSONArray("requests").toList()).hasSize(2);
    }

    @Test
    void submitBuilderValidatesCompletionWindowAndProviderSlugsLoudly() {
        assertThatThrownBy(() -> submitBuilder().completionWindow("1h"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("24h");

        assertThatThrownBy(() -> submitBuilder().providerOnly("openai", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");

        assertThat(submitBuilder().providerOnly().build().providerOnly()).isNull();
    }

    @Test
    void submitRequestsSetterReplacesItemsAndRejectsNullItems() {
        OpenRouterBatchSubmitRequest request = new OpenRouterBatchSubmitRequest.Builder(client())
                .endpoint(OpenRouterBatchEndpoint.EMBEDDINGS)
                .model("openai/text-embedding-3-small")
                .addRequestBody("req-0001", new JSONObject().put("input", "a"))
                .requests(List.of(OpenRouterBatchItem.of("req-0002", new JSONObject().put("input", "b"))))
                .build();

        assertThat(request.requests()).hasSize(1);
        assertThat(request.requests().get(0).customId()).isEqualTo("req-0002");

        assertThatThrownBy(() -> submitBuilder().addRequest((OpenRouterBatchItem) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("item must not be null");
    }

    @Test
    void batchItemFactoriesReuseTheInferenceRequestBodiesVerbatim() {
        OpenRouterChatCompletionRequest chat = new OpenRouterChatCompletionRequest.Builder(client())
                .model("openai/gpt-4o")
                .addMessage("user", "Hello")
                .build();
        OpenRouterMessagesRequest messages = new OpenRouterMessagesRequest.Builder(client())
                .model("anthropic/claude-sonnet-4")
                .maxTokens(1024)
                .addMessage("user", "Hello")
                .build();
        OpenRouterResponsesRequest responses = new OpenRouterResponsesRequest.Builder(client())
                .model("openai/gpt-4o")
                .input("Hello")
                .build();
        OpenRouterEmbeddingsRequest embeddings = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("Hello")
                .build();

        assertThat(OpenRouterBatchItem.fromRequest("a", chat).body().similar(new JSONObject(chat.getBody()))).isTrue();
        assertThat(OpenRouterBatchItem.fromRequest("b", messages).body().similar(new JSONObject(messages.getBody()))).isTrue();
        assertThat(OpenRouterBatchItem.fromRequest("c", responses).body().similar(new JSONObject(responses.getBody()))).isTrue();
        assertThat(OpenRouterBatchItem.fromRequest("d", embeddings).body().similar(new JSONObject(embeddings.getBody()))).isTrue();

        assertThat(OpenRouterBatchItem.fromRequest("a", chat).body().getJSONArray("messages").toList()).hasSize(1);
        assertThat(OpenRouterBatchItem.fromRequest("b", messages).body().getInt("max_tokens")).isEqualTo(1024);
        assertThat(OpenRouterBatchItem.fromRequest("c", responses).body().getString("input")).isEqualTo("Hello");
        assertThat(OpenRouterBatchItem.fromRequest("d", embeddings).body().getString("input")).isEqualTo("Hello");
        assertThat(OpenRouterBatchItem.fromRequest("a", chat).body().getString("model")).isEqualTo("openai/gpt-4o");

        JSONObject json = OpenRouterBatchItem.fromRequest("a", chat).toJson();
        assertThat(json.keySet()).containsExactlyInAnyOrder("custom_id", "body");
        assertThat(json.getString("custom_id")).isEqualTo("a");
    }

    @Test
    void batchItemFactoriesRejectNullAndBlankArguments() {
        assertThatThrownBy(() -> OpenRouterBatchItem.of(" ", new JSONObject()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("custom_id");
        assertThatThrownBy(() -> OpenRouterBatchItem.of("a", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("body must not be null");
        assertThatThrownBy(() -> OpenRouterBatchItem.fromRequest("a", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("request must not be null");
        assertThatThrownBy(() -> OpenRouterBatchItem.fromRequest("a",
                new OpenRouterBatchGetRequest.Builder(client(), "batch_1").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("request carries no body");
    }

    @Test
    void listRequestUsesGetMethodOnBatchesUrlWithoutBody() {
        OpenRouterBatchListRequest plain = new OpenRouterBatchListRequest.Builder(client()).build();

        assertThat(plain.getRelativeUrl()).isEqualTo("/batches");
        assertThat(plain.getHttpMethod()).isEqualTo("GET");
        assertThat(plain.getBody()).isNull();
    }

    @Test
    void listRequestEmitsTypedQueryParameters() {
        OpenRouterBatchListRequest request = new OpenRouterBatchListRequest.Builder(client())
                .limit(2)
                .after("batch_9f2c1e")
                .createdAfter(1787184000L)
                .createdBefore("2026-08-20T00:00:00Z")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/batches"
                + "?limit=2"
                + "&after=batch_9f2c1e"
                + "&created_after=1787184000"
                + "&created_before=2026-08-20T00%3A00%3A00Z");
        assertThat(request.queryParams().get("limit")).containsExactly("2");
    }

    @Test
    void listRequestRepeatsTheStatusFilterPerValue() {
        OpenRouterBatchListRequest request = new OpenRouterBatchListRequest.Builder(client())
                .status("completed", "failed")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/batches?status=completed&status=failed");
        assertThat(request.queryParams().get("status")).containsExactly("completed", "failed");
    }

    @Test
    void listRequestCollapsesDuplicateStatusesAndClearsOnEmpty() {
        OpenRouterBatchListRequest collapsed = new OpenRouterBatchListRequest.Builder(client())
                .status("completed", "completed", "failed")
                .build();
        assertThat(collapsed.getRelativeUrl()).isEqualTo("/batches?status=completed&status=failed");

        OpenRouterBatchListRequest cleared = new OpenRouterBatchListRequest.Builder(client())
                .status("completed")
                .status()
                .build();
        assertThat(cleared.getRelativeUrl()).isEqualTo("/batches");
    }

    @Test
    void listBuilderRejectsTransientStatusFiltersLoudly() {
        assertThatThrownBy(() -> new OpenRouterBatchListRequest.Builder(client()).status("finalizing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finalizing");
        assertThatThrownBy(() -> new OpenRouterBatchListRequest.Builder(client()).status("cancelling"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cancelling");
    }

    @Test
    void listBuilderValidatesLimitLoudly() {
        assertThatThrownBy(() -> new OpenRouterBatchListRequest.Builder(client()).limit(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 100");
        assertThatThrownBy(() -> new OpenRouterBatchListRequest.Builder(client()).limit(101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 100");
        assertThat(new OpenRouterBatchListRequest.Builder(client()).limit(1).build().getRelativeUrl())
                .isEqualTo("/batches?limit=1");
        assertThat(new OpenRouterBatchListRequest.Builder(client()).limit(100).build().getRelativeUrl())
                .isEqualTo("/batches?limit=100");
    }

    @Test
    void listQueryParamHatchSendsVerbatimAndNullRemoves() {
        OpenRouterBatchListRequest request = new OpenRouterBatchListRequest.Builder(client())
                .queryParam("workspace_id", "0df9e665-d932-5740-b2c7-b52af166bc11")
                .queryParam("custom filter", "a b")
                .queryParam("gone", null)
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/batches"
                + "?workspace_id=0df9e665-d932-5740-b2c7-b52af166bc11"
                + "&custom+filter=a+b");
    }

    @Test
    void getRequestPollsOneBatchById() {
        OpenRouterBatchGetRequest request = new OpenRouterBatchGetRequest.Builder(client(), "batch_123").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/batches/batch_123");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.batchId()).isEqualTo("batch_123");

        assertThatThrownBy(() -> new OpenRouterBatchGetRequest.Builder(client(), "").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("batchId is required");
    }

    @Test
    void deleteRequestDeletesOneBatchById() {
        OpenRouterBatchDeleteRequest request = new OpenRouterBatchDeleteRequest.Builder(client(), "batch_123").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/batches/batch_123");
        assertThat(request.getHttpMethod()).isEqualTo("DELETE");
        assertThat(request.getBody()).isNull();

        assertThatThrownBy(() -> new OpenRouterBatchDeleteRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("batchId is required");
    }

    @Test
    void batchViewSurfacesTheLifecycleAndTerminalStatuses() {
        OpenRouterBatch completed = batchOf("""
                {
                  "id": "batch_123",
                  "object": "batch",
                  "endpoint": "/v1/chat/completions",
                  "model": "openai/gpt-4o",
                  "completion_window": "24h",
                  "status": "completed",
                  "created_at": 1782097200,
                  "finalized_at": 1782100800
                }
                """);

        assertThat(completed.id()).isEqualTo("batch_123");
        assertThat(completed.object()).isEqualTo("batch");
        assertThat(completed.endpoint()).isEqualTo("/v1/chat/completions");
        assertThat(completed.model()).isEqualTo("openai/gpt-4o");
        assertThat(completed.completionWindow()).isEqualTo("24h");
        assertThat(completed.status()).isEqualTo("completed");
        assertThat(completed.createdAt()).isEqualTo(1782097200L);
        assertThat(completed.finalizedAt()).isEqualTo(1782100800L);
        assertThat(completed.isTerminal()).isTrue();

        for (String terminal : List.of("completed", "failed", "expired", "cancelled")) {
            assertThat(batchOf("{\"status\": \"" + terminal + "\"}").isTerminal()).isTrue();
        }
        for (String open : List.of("validating", "in_progress", "finalizing", "cancelling")) {
            assertThat(batchOf("{\"status\": \"" + open + "\"}").isTerminal()).isFalse();
        }
    }

    @Test
    void batchViewSurfacesCountsUsageAndError() {
        OpenRouterBatch batch = batchOf("""
                {
                  "id": "batch_123",
                  "request_counts": { "total": 100, "completed": 98, "failed": 2 },
                  "usage": {
                    "prompt_tokens": 51200,
                    "completion_tokens": 20480,
                    "total_tokens": 71680,
                    "cost": 0.000225,
                    "is_byok": true
                  },
                  "error": { "message": "request req-7 failed validation" }
                }
                """);

        assertThat(batch.requestCounts().total()).isEqualTo(100);
        assertThat(batch.requestCounts().completed()).isEqualTo(98);
        assertThat(batch.requestCounts().failed()).isEqualTo(2);
        assertThat(batch.usage().promptTokens()).isEqualTo(51200);
        assertThat(batch.usage().completionTokens()).isEqualTo(20480);
        assertThat(batch.usage().totalTokens()).isEqualTo(71680);
        assertThat(batch.usage().cost()).isEqualTo(0.000225);
        assertThat(batch.usage().isByok()).isTrue();
        assertThat(batch.error().getString("message")).contains("req-7");

        OpenRouterBatch empty = batchOf("{}");
        assertThat(empty.requestCounts()).isNull();
        assertThat(empty.usage()).isNull();
        assertThat(empty.error()).isNull();
        assertThat(empty.results()).isEmpty();
        assertThat(empty.result("req-0001")).isNull();
    }

    @Test
    void batchViewParsesResultsAndFindsThemByCustomId() {
        OpenRouterBatch batch = completedBatchOfChatShape();

        assertThat(batch.results()).hasSize(1);
        OpenRouterBatchResult result = batch.result("req-0001");
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("batch_req_123");
        assertThat(result.customId()).isEqualTo("req-0001");
        assertThat(batch.result("nope")).isNull();
        assertThat(batchOf("{\"results\": 7}").results()).isEmpty();
    }

    @Test
    void batchResultSurfacesResponseAndErrorMutuallyExclusive() {
        OpenRouterBatchResult ok = completedBatchOfChatShape().result("req-0001");
        assertThat(ok.hasResponse()).isTrue();
        assertThat(ok.hasError()).isFalse();
        assertThat(ok.statusCode()).isEqualTo(200);
        assertThat(ok.requestId()).isEqualTo("request_123");
        assertThat(ok.error()).isNull();

        OpenRouterBatchResult failed = batchOf("""
                {
                  "results": [
                    {
                      "id": "batch_req_9",
                      "custom_id": "req-0009",
                      "response": null,
                      "error": { "message": "model rejected" }
                    }
                  ]
                }
                """).result("req-0009");
        assertThat(failed.hasResponse()).isFalse();
        assertThat(failed.hasError()).isTrue();
        assertThat(failed.statusCode()).isNull();
        assertThat(failed.requestId()).isNull();
        assertThat(failed.body()).isNull();
        assertThat(failed.error().getString("message")).isEqualTo("model rejected");
    }

    @Test
    void batchResultReadsBodyAsChatCompletionResponse() {
        OpenRouterChatCompletionResponse response = completedBatchOfChatShape()
                .result("req-0001").chatCompletionResponse();

        assertThat(response).isNotNull();
        assertThat(response.assistantMessage()).isEqualTo("OpenRouter provides one API for many AI models.");
        assertThat(response.finishReason()).isEqualTo("stop");
    }

    @Test
    void batchResultReadsBodyAsMessagesResponse() {
        OpenRouterMessagesResponse response = batchOf("""
                {
                  "results": [
                    {
                      "custom_id": "req-1",
                      "response": {
                        "status_code": 200,
                        "request_id": "request_1",
                        "body": {
                          "id": "msg_01X",
                          "model": "anthropic/claude-sonnet-4",
                          "role": "assistant",
                          "stop_reason": "end_turn",
                          "content": [{ "type": "text", "text": "Hello." }]
                        }
                      },
                      "error": null
                    }
                  ]
                }
                """).result("req-1").messagesResponse();

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("msg_01X");
        assertThat(response.text()).isEqualTo("Hello.");
        assertThat(response.stopReason()).isEqualTo("end_turn");
    }

    @Test
    void batchResultReadsBodyAsResponsesResponse() {
        OpenRouterResponsesResponse response = batchOf("""
                {
                  "results": [
                    {
                      "custom_id": "req-1",
                      "response": {
                        "status_code": 200,
                        "body": {
                          "id": "resp_01X",
                          "object": "response",
                          "output_text": "Hi."
                        }
                      },
                      "error": null
                    }
                  ]
                }
                """).result("req-1").responsesResponse();

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("resp_01X");
        assertThat(response.outputText()).isEqualTo("Hi.");
    }

    @Test
    void batchResultReadsBodyAsEmbeddingsResponse() {
        OpenRouterEmbeddingsResponse response = batchOf("""
                {
                  "results": [
                    {
                      "custom_id": "emb-0001",
                      "response": {
                        "status_code": 200,
                        "body": {
                          "object": "list",
                          "data": [
                            { "object": "embedding", "embedding": [0.5, -0.25], "index": 0 },
                            { "object": "embedding", "embedding": [0.1, 0.2], "index": 1 }
                          ],
                          "model": "openai/text-embedding-3-small",
                          "usage": { "prompt_tokens": 18, "total_tokens": 18 }
                        }
                      },
                      "error": null
                    }
                  ]
                }
                """).result("emb-0001").embeddingsResponse();

        assertThat(response).isNotNull();
        List<OpenRouterEmbedding> embeddings = response.embeddings();
        assertThat(embeddings).hasSize(2);
        assertThat(embeddings.get(0).index()).isEqualTo(0);
    }

    @Test
    void batchResultTypedResponsesAreNullWithoutABody() {
        OpenRouterBatchResult failed = batchOf("""
                {
                  "results": [
                    { "custom_id": "req-9", "response": null, "error": { "message": "no" } }
                  ]
                }
                """).result("req-9");

        assertThat(failed.chatCompletionResponse()).isNull();
        assertThat(failed.messagesResponse()).isNull();
        assertThat(failed.responsesResponse()).isNull();
        assertThat(failed.embeddingsResponse()).isNull();
    }

    @Test
    void submitAndPollResponsesWrapTheBatchView() {
        OpenRouterBatchResponse<OpenRouterBatchSubmitRequest> submit =
                new OpenRouterBatchResponse<>(new JSONObject("""
                        { "id": "batch_123", "object": "batch", "status": "validating" }
                        """), submitBuilder().build());
        assertThat(submit.batch().id()).isEqualTo("batch_123");
        assertThat(submit.batch().status()).isEqualTo("validating");

        assertThat(new OpenRouterBatchResponse<>(new JSONObject("{}"), submitBuilder().build()).batch()).isNull();
    }

    @Test
    void listViewSurfacesPageMetadata() {
        OpenRouterBatchListResponse response = listResponseOf("""
                {
                  "object": "list",
                  "data": [
                    {
                      "id": "batch_9f2c1e",
                      "object": "batch",
                      "endpoint": "/v1/chat/completions",
                      "model": "openai/gpt-4o",
                      "status": "completed",
                      "results": null
                    }
                  ],
                  "first_id": "batch_9f2c1e",
                  "last_id": "batch_9f2c1e",
                  "has_more": true
                }
                """);

        assertThat(response.batches()).hasSize(1);
        assertThat(response.batches().get(0).id()).isEqualTo("batch_9f2c1e");
        assertThat(response.batches().get(0).results()).isEmpty();
        assertThat(response.firstId()).isEqualTo("batch_9f2c1e");
        assertThat(response.lastId()).isEqualTo("batch_9f2c1e");
        assertThat(response.hasMore()).isTrue();

        OpenRouterBatchListResponse empty = listResponseOf("{}");
        assertThat(empty.batches()).isEmpty();
        assertThat(empty.firstId()).isNull();
        assertThat(empty.hasMore()).isNull();
    }

    @Test
    void deleteResponseSurfacesTheDeletionOutcome() {
        OpenRouterBatchDeleteResponse response = deleteResponseOf("""
                {
                  "id": "batch_123",
                  "object": "batch",
                  "deletion": {
                    "openrouter": "deleted",
                    "upstream": { "provider": "Anthropic", "status": "deleted" }
                  }
                }
                """);

        assertThat(response.id()).isEqualTo("batch_123");
        assertThat(response.deletion().openrouter()).isEqualTo("deleted");
        assertThat(response.deletion().upstreamProvider()).isEqualTo("Anthropic");
        assertThat(response.deletion().upstreamStatus()).isEqualTo("deleted");

        OpenRouterBatchDeleteResponse noUpstream = deleteResponseOf("""
                {
                  "id": "batch_123",
                  "deletion": { "openrouter": "deleted" }
                }
                """);
        assertThat(noUpstream.deletion().upstreamProvider()).isNull();
        assertThat(noUpstream.deletion().upstreamStatus()).isNull();
        assertThat(deleteResponseOf("{}").deletion()).isNull();
    }

    private OpenRouterBatch batchOf(String json) {
        return new OpenRouterBatch(new JSONObject(json));
    }

    private OpenRouterBatch completedBatchOfChatShape() {
        return batchOf("""
                {
                  "id": "batch_123",
                  "object": "batch",
                  "endpoint": "/v1/chat/completions",
                  "model": "openai/gpt-4o",
                  "completion_window": "24h",
                  "status": "completed",
                  "created_at": 1782097200,
                  "finalized_at": 1782100800,
                  "request_counts": { "total": 1, "completed": 1, "failed": 0 },
                  "usage": {
                    "prompt_tokens": 20,
                    "completion_tokens": 40,
                    "total_tokens": 60,
                    "cost": 0.000225,
                    "is_byok": false
                  },
                  "results": [
                    {
                      "id": "batch_req_123",
                      "custom_id": "req-0001",
                      "response": {
                        "status_code": 200,
                        "request_id": "request_123",
                        "body": {
                          "id": "gen-batch-1782097200-a1b2c3d4e5f6a7b8c9d0",
                          "object": "chat.completion",
                          "created": 1782097200,
                          "model": "openai/gpt-4o",
                          "choices": [
                            {
                              "index": 0,
                              "message": {
                                "role": "assistant",
                                "content": "OpenRouter provides one API for many AI models."
                              },
                              "finish_reason": "stop"
                            }
                          ]
                        }
                      },
                      "error": null
                    }
                  ],
                  "error": null
                }
                """);
    }

    private OpenRouterBatchListResponse listResponseOf(String json) {
        return new OpenRouterBatchListResponse(new JSONObject(json),
                new OpenRouterBatchListRequest.Builder(client()).build());
    }

    private OpenRouterBatchDeleteResponse deleteResponseOf(String json) {
        return new OpenRouterBatchDeleteResponse(new JSONObject(json),
                new OpenRouterBatchDeleteRequest.Builder(client(), "batch_123").build());
    }
}
