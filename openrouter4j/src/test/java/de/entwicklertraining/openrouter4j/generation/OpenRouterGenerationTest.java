package de.entwicklertraining.openrouter4j.generation;

import org.json.JSONObject;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the generation-metadata request shapes (metadata, content, feedback)
 * and the response accessors against recorded JSON shapes of the OpenRouter
 * generation endpoints.
 */
class OpenRouterGenerationTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void metadataRequestEncodesTheId() {
        OpenRouterGenerationRequest request = new OpenRouterGenerationRequest.Builder(client(), "gen-1234567890").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/generation?id=gen-1234567890");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.id()).isEqualTo("gen-1234567890");

        assertThatThrownBy(() -> new OpenRouterGenerationRequest.Builder(client(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void metadataAccessorsAreSurfaced() {
        OpenRouterGenerationResponse response = responseOf("""
                {
                  "data": {
                    "api_type": "completions",
                    "app_id": 12345,
                    "cache_discount": null,
                    "cancelled": false,
                    "created_at": "2024-07-15T23:33:19.433273+00:00",
                    "data_region": "global",
                    "external_user": "user-123",
                    "finish_reason": "stop",
                    "generation_time": 1200,
                    "id": "gen-3bhGkxlo4XFrqiabUM7NDtwDzWwG",
                    "is_byok": false,
                    "latency": 1250,
                    "model": "sao10k/l3-stheno-8b",
                    "moderation_latency": 50,
                    "native_finish_reason": "stop",
                    "native_tokens_cached": 3,
                    "native_tokens_completion": 25,
                    "native_tokens_prompt": 10,
                    "native_tokens_reasoning": 5,
                    "num_search_results": 5,
                    "preset_id": "a9e8d400-592a-494f-908c-375efa66cafd",
                    "provider_name": "Infermatic",
                    "request_id": "req-1727282430-aBcDeFgHiJkLmNoPqRsT",
                    "router": "openrouter/auto",
                    "service_tier": "priority",
                    "session_id": null,
                    "streamed": true,
                    "tokens_completion": 25,
                    "tokens_prompt": 10,
                    "total_cost": 0.0015,
                    "upstream_id": "chatcmpl-791bcf62-080e-4568-87d0-94c72e3b4946",
                    "upstream_inference_cost": 0.0012,
                    "usage": 0.0015,
                    "web_search_engine": "exa"
                  }
                }
                """);

        assertThat(response.id()).isEqualTo("gen-3bhGkxlo4XFrqiabUM7NDtwDzWwG");
        assertThat(response.model()).isEqualTo("sao10k/l3-stheno-8b");
        assertThat(response.providerName()).isEqualTo("Infermatic");
        assertThat(response.router()).isEqualTo("openrouter/auto");
        assertThat(response.apiType()).isEqualTo("completions");
        assertThat(response.finishReason()).isEqualTo("stop");
        assertThat(response.nativeFinishReason()).isEqualTo("stop");
        assertThat(response.tokensPrompt()).isEqualTo(10L);
        assertThat(response.tokensCompletion()).isEqualTo(25L);
        assertThat(response.nativeTokensPrompt()).isEqualTo(10L);
        assertThat(response.nativeTokensCompletion()).isEqualTo(25L);
        assertThat(response.nativeTokensReasoning()).isEqualTo(5L);
        assertThat(response.nativeTokensCached()).isEqualTo(3L);
        assertThat(response.totalCost()).isEqualTo(0.0015);
        assertThat(response.usage()).isEqualTo(0.0015);
        assertThat(response.upstreamInferenceCost()).isEqualTo(0.0012);
        assertThat(response.cacheDiscount()).isNull();
        assertThat(response.latency()).isEqualTo(1250L);
        assertThat(response.generationTime()).isEqualTo(1200L);
        assertThat(response.moderationLatency()).isEqualTo(50L);
        assertThat(response.streamed()).isTrue();
        assertThat(response.cancelled()).isFalse();
        assertThat(response.isByok()).isFalse();
        assertThat(response.createdAt()).isEqualTo("2024-07-15T23:33:19.433273+00:00");
        assertThat(response.dataRegion()).isEqualTo("global");
        assertThat(response.serviceTier()).isEqualTo("priority");
        assertThat(response.upstreamId()).isEqualTo("chatcmpl-791bcf62-080e-4568-87d0-94c72e3b4946");
        assertThat(response.requestId()).isEqualTo("req-1727282430-aBcDeFgHiJkLmNoPqRsT");
        assertThat(response.sessionId()).isNull();
        assertThat(response.presetId()).isEqualTo("a9e8d400-592a-494f-908c-375efa66cafd");
        assertThat(response.appId()).isEqualTo(12345L);
        assertThat(response.externalUser()).isEqualTo("user-123");
        assertThat(response.webSearchEngine()).isEqualTo("exa");
        assertThat(response.numSearchResults()).isEqualTo(5L);
    }

    @Test
    void metadataAccessorsReturnNullWhenAbsent() {
        OpenRouterGenerationResponse response = responseOf("{}");

        assertThat(response.model()).isNull();
        assertThat(response.providerName()).isNull();
        assertThat(response.nativeFinishReason()).isNull();
        assertThat(response.totalCost()).isNull();
        assertThat(response.data()).isNull();
    }

    @Test
    void contentRequestEncodesTheId() {
        OpenRouterGenerationContentRequest request =
                new OpenRouterGenerationContentRequest.Builder(client(), "gen-abc").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/generation/content?id=gen-abc");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.id()).isEqualTo("gen-abc");

        assertThatThrownBy(() -> new OpenRouterGenerationContentRequest.Builder(client(), ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void contentAccessorsAreSurfaced() {
        OpenRouterGenerationContentResponse response = contentResponseOf("""
                {
                  "data": {
                    "error": null,
                    "input": {
                      "messages": [
                        {"content": "What is the meaning of life?", "role": "user"}
                      ]
                    },
                    "output": {
                      "completion": "The meaning of life is a philosophical question...",
                      "reasoning": null
                    }
                  }
                }
                """);

        assertThat(response.inputPrompt()).isNull();
        assertThat(response.inputMessages()).hasSize(1);
        assertThat(response.inputMessages().get(0).getString("role")).isEqualTo("user");
        assertThat(response.outputCompletion()).isEqualTo("The meaning of life is a philosophical question...");
        assertThat(response.outputReasoning()).isNull();
        assertThat(response.error()).isNull();
        assertThat(response.errorStatus()).isNull();
        assertThat(response.errorMessage()).isNull();
        assertThat(response.errorProviderName()).isNull();
        assertThat(response.errorRaw()).isNull();
    }

    @Test
    void contentPromptFormAndFailureFormAreSurfaced() {
        OpenRouterGenerationContentResponse promptForm = contentResponseOf("""
                {
                  "data": {
                    "input": {"prompt": "What is 2+2?"},
                    "output": {"completion": "4"},
                    "error": null
                  }
                }
                """);
        assertThat(promptForm.inputPrompt()).isEqualTo("What is 2+2?");
        assertThat(promptForm.inputMessages()).isEmpty();

        OpenRouterGenerationContentResponse failure = contentResponseOf("""
                {
                  "data": {
                    "error": {
                      "status": 502,
                      "message": "Provider returned an error.",
                      "provider_name": "Infermatic",
                      "raw": "upstream connect error",
                      "previous_errors": []
                    }
                  }
                }
                """);
        assertThat(failure.errorStatus()).isEqualTo(502);
        assertThat(failure.errorMessage()).isEqualTo("Provider returned an error.");
        assertThat(failure.errorProviderName()).isEqualTo("Infermatic");
        assertThat(failure.errorRaw()).isEqualTo("upstream connect error");
    }

    @Test
    void contentAccessorsReturnNullWhenAbsent() {
        OpenRouterGenerationContentResponse response = contentResponseOf("{}");

        assertThat(response.inputPrompt()).isNull();
        assertThat(response.inputMessages()).isEmpty();
        assertThat(response.outputCompletion()).isNull();
        assertThat(response.outputReasoning()).isNull();
        assertThat(response.data()).isNull();
    }

    @Test
    void feedbackRequestEmitsTheSchemaBody() {
        OpenRouterGenerationFeedbackRequest request = new OpenRouterGenerationFeedbackRequest.Builder(client())
                .generationId("gen-3bhGkxlo4XFrqiabUM7NDtwDzWwG")
                .category("incorrect_response")
                .comment("The model repeated the same paragraph three times.")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/generation/feedback");
        assertThat(request.getHttpMethod()).isEqualTo("POST");

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("generation_id")).isEqualTo("gen-3bhGkxlo4XFrqiabUM7NDtwDzWwG");
        assertThat(body.getString("category")).isEqualTo("incorrect_response");
        assertThat(body.getString("comment")).isEqualTo("The model repeated the same paragraph three times.");
    }

    @Test
    void feedbackRequestOmitsUnsetComment() {
        OpenRouterGenerationFeedbackRequest request = new OpenRouterGenerationFeedbackRequest.Builder(client())
                .generationId("gen-abc")
                .category("latency")
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.has("comment")).isFalse();
        assertThat(request.comment()).isNull();
    }

    @Test
    void feedbackRequestRequiresGenerationIdAndCategory() {
        assertThatThrownBy(() -> new OpenRouterGenerationFeedbackRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class);

        assertThatCode(() -> new OpenRouterGenerationFeedbackRequest.Builder(client())
                .generationId("gen-abc")
                .category("other")
                .build())
                .doesNotThrowAnyException();
    }

    @Test
    void feedbackResponseSurfacesSuccess() {
        OpenRouterGenerationFeedbackRequest request = new OpenRouterGenerationFeedbackRequest.Builder(client())
                .generationId("gen-abc")
                .category("other")
                .build();

        OpenRouterGenerationFeedbackResponse response =
                request.createResponse("{\"data\": {\"success\": true}}");
        assertThat(response.success()).isTrue();

        assertThat(request.createResponse("{}").success()).isNull();
    }

    private OpenRouterGenerationResponse responseOf(String json) {
        OpenRouterGenerationRequest request =
                new OpenRouterGenerationRequest.Builder(client(), "gen-1234567890").build();
        return request.createResponse(json);
    }

    private OpenRouterGenerationContentResponse contentResponseOf(String json) {
        OpenRouterGenerationContentRequest request =
                new OpenRouterGenerationContentRequest.Builder(client(), "gen-1234567890").build();
        return request.createResponse(json);
    }
}
