package de.entwicklertraining.openrouter4j.generation;

import org.json.JSONObject;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import java.util.List;
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
        assertThat(response.origin()).isNull();
        assertThat(response.userAgent()).isNull();
        assertThat(response.httpReferer()).isNull();
        assertThat(response.workspaceId()).isNull();
        assertThat(response.nativeTokensCompletionImages()).isNull();
        assertThat(response.numFetches()).isNull();
        assertThat(response.numInputAudioPrompt()).isNull();
        assertThat(response.numMediaPrompt()).isNull();
        assertThat(response.numMediaCompletion()).isNull();
        assertThat(response.providerResponses()).isEmpty();
        assertThat(response.responseCacheSourceId()).isNull();
    }

    @Test
    void newerMetadataAccessorsAreSurfaced() {
        OpenRouterGenerationResponse response = responseOf("""
                {
                  "data": {
                    "origin": "https://example.com/app",
                    "user_agent": "MyClient/1.0",
                    "http_referer": "https://example.com/app",
                    "workspace_id": "660e8400-e29b-41d4-a716-446655440000",
                    "native_tokens_completion_images": 128,
                    "num_fetches": 3,
                    "num_input_audio_prompt": 2,
                    "num_media_prompt": 1,
                    "num_media_completion": 1,
                    "provider_responses": [
                      {"provider_name": "OpenAI", "id": "chatcmpl-1", "endpoint_id": "openai/gpt-4o",
                       "latency": 120, "status": 200, "model_permaslug": "openai/gpt-4o", "is_byok": false},
                      {"provider_name": "Azure", "id": "chatcmpl-2", "endpoint_id": "azure/gpt-4o",
                       "latency": 80, "status": 503, "model_permaslug": "openai/gpt-4o", "is_byok": true}
                    ],
                    "response_cache_source_id": "gen-cached123"
                  }
                }
                """);

        assertThat(response.origin()).isEqualTo("https://example.com/app");
        assertThat(response.userAgent()).isEqualTo("MyClient/1.0");
        assertThat(response.httpReferer()).isEqualTo("https://example.com/app");
        assertThat(response.workspaceId()).isEqualTo("660e8400-e29b-41d4-a716-446655440000");
        assertThat(response.nativeTokensCompletionImages()).isEqualTo(128L);
        assertThat(response.numFetches()).isEqualTo(3L);
        assertThat(response.numInputAudioPrompt()).isEqualTo(2L);
        assertThat(response.numMediaPrompt()).isEqualTo(1L);
        assertThat(response.numMediaCompletion()).isEqualTo(1L);
        assertThat(response.responseCacheSourceId()).isEqualTo("gen-cached123");

        List<JSONObject> attempts = response.providerResponses();
        assertThat(attempts).hasSize(2);
        assertThat(attempts.get(0).getString("provider_name")).isEqualTo("OpenAI");
        assertThat(attempts.get(0).getInt("status")).isEqualTo(200);
        assertThat(attempts.get(1).getString("provider_name")).isEqualTo("Azure");
        assertThat(attempts.get(1).getInt("status")).isEqualTo(503);
        assertThat(attempts.get(1).getBoolean("is_byok")).isTrue();
    }

    @Test
    void newerMetadataAccessorsReturnNullForFieldsMissingInsidePresentData() {
        OpenRouterGenerationResponse response = responseOf("""
                {"data": {"id": "gen-1", "model": "openai/gpt-4o"}}
                """);

        assertThat(response.id()).isEqualTo("gen-1");
        assertThat(response.origin()).isNull();
        assertThat(response.userAgent()).isNull();
        assertThat(response.httpReferer()).isNull();
        assertThat(response.workspaceId()).isNull();
        assertThat(response.nativeTokensCompletionImages()).isNull();
        assertThat(response.numFetches()).isNull();
        assertThat(response.numInputAudioPrompt()).isNull();
        assertThat(response.numMediaPrompt()).isNull();
        assertThat(response.numMediaCompletion()).isNull();
        assertThat(response.providerResponses()).isEmpty();
        assertThat(response.responseCacheSourceId()).isNull();
    }

    @Test
    void newerMetadataAccessorsSwallowMalformedShapes() {
        OpenRouterGenerationResponse response = responseOf("""
                {
                  "data": {
                    "origin": 42,
                    "user_agent": null,
                    "http_referer": true,
                    "workspace_id": ["w"],
                    "native_tokens_completion_images": "many",
                    "num_fetches": null,
                    "num_input_audio_prompt": "3",
                    "num_media_prompt": false,
                    "num_media_completion": {},
                    "provider_responses": ["flat", 7, null, {"provider_name": "kept"}],
                    "response_cache_source_id": 7
                  }
                }
                """);

        assertThat(response.origin()).isNull();
        assertThat(response.userAgent()).isNull();
        assertThat(response.httpReferer()).isNull();
        assertThat(response.workspaceId()).isNull();
        assertThat(response.nativeTokensCompletionImages()).isNull();
        assertThat(response.numFetches()).isNull();
        assertThat(response.numInputAudioPrompt()).isNull();
        assertThat(response.numMediaPrompt()).isNull();
        assertThat(response.numMediaCompletion()).isNull();
        assertThat(response.responseCacheSourceId()).isNull();
        assertThat(response.providerResponses()).hasSize(1);
        assertThat(response.providerResponses().get(0).getString("provider_name")).isEqualTo("kept");

        OpenRouterGenerationResponse notAnArray = responseOf("""
                {"data": {"provider_responses": {"id": "x"}}}
                """);
        assertThat(notAnArray.providerResponses()).isEmpty();
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
