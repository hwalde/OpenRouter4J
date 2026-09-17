package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the model-catalog request shapes (list, count, user, single model,
 * endpoints) and the response accessors against recorded JSON shapes of the
 * OpenRouter catalog responses.
 */
class OpenRouterModelsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void listRequestWithoutFiltersHasPlainUrl() {
        OpenRouterModelsListRequest request = new OpenRouterModelsListRequest.Builder(client()).build();

        assertThat(request.getRelativeUrl()).isEqualTo("/models");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.queryParams()).isEmpty();
    }

    @Test
    void listRequestEmitsTypedFiltersAsQueryParameters() {
        OpenRouterModelsListRequest request = new OpenRouterModelsListRequest.Builder(client())
                .limit(50)
                .offset(10)
                .q("gpt-4")
                .category("programming")
                .supportedParameters("temperature", "response_format")
                .inputModalities("text", "image")
                .outputModalities("text")
                .sort("pricing-low-to-high")
                .context(128000)
                .minPrice(0.5)
                .maxPrice(10.0)
                .minOutputPrice(0.0)
                .maxOutputPrice(2.5)
                .arch("GPT")
                .modelAuthors("openai", "anthropic")
                .providers("OpenAI")
                .distillable(true)
                .zdr(true)
                .region("eu")
                .build();

        Map<String, String> params = request.queryParams();
        assertThat(params.get("limit")).isEqualTo("50");
        assertThat(params.get("offset")).isEqualTo("10");
        assertThat(params.get("q")).isEqualTo("gpt-4");
        assertThat(params.get("category")).isEqualTo("programming");
        assertThat(params.get("supported_parameters")).isEqualTo("temperature,response_format");
        assertThat(params.get("input_modalities")).isEqualTo("text,image");
        assertThat(params.get("output_modalities")).isEqualTo("text");
        assertThat(params.get("sort")).isEqualTo("pricing-low-to-high");
        assertThat(params.get("context")).isEqualTo("128000");
        assertThat(params.get("min_price")).isEqualTo("0.5");
        assertThat(params.get("max_price")).isEqualTo("10.0");
        assertThat(params.get("min_output_price")).isEqualTo("0.0");
        assertThat(params.get("max_output_price")).isEqualTo("2.5");
        assertThat(params.get("arch")).isEqualTo("GPT");
        assertThat(params.get("model_authors")).isEqualTo("openai,anthropic");
        assertThat(params.get("providers")).isEqualTo("OpenAI");
        assertThat(params.get("distillable")).isEqualTo("true");
        assertThat(params.get("zdr")).isEqualTo("true");
        assertThat(params.get("region")).isEqualTo("eu");

        assertThat(request.getRelativeUrl())
                .startsWith("/models?")
                .contains("supported_parameters=temperature%2Cresponse_format")
                .contains("model_authors=openai%2Canthropic")
                .contains("q=gpt-4");
    }

    @Test
    void listRequestSupportsVerbatimEscapeHatchAndIgnoresNulls() {
        OpenRouterModelsListRequest request = new OpenRouterModelsListRequest.Builder(client())
                .queryParam("some_future_filter", "x")
                .queryParam("region", null)
                .build();

        assertThat(request.queryParams()).containsOnlyKeys("some_future_filter");
    }

    @Test
    void listRequestEmitsTheTenNewFiltersOnlyWhenSet() {
        OpenRouterModelsListRequest request = new OpenRouterModelsListRequest.Builder(client())
                .minAgeDays(30)
                .maxAgeDays(365)
                .minIntelligenceIndex(40.5)
                .maxIntelligenceIndex(60.0)
                .minCodingIndex(35.0)
                .maxCodingIndex(55.5)
                .minAgenticIndex(20.0)
                .maxAgenticIndex(45.0)
                .minToolSuccessRate(0.5)
                .maxToolSuccessRate(0.95)
                .build();

        Map<String, String> params = request.queryParams();
        assertThat(params).containsOnlyKeys(
                "min_age_days", "max_age_days",
                "min_intelligence_index", "max_intelligence_index",
                "min_coding_index", "max_coding_index",
                "min_agentic_index", "max_agentic_index",
                "min_tool_success_rate", "max_tool_success_rate");
        assertThat(params.get("min_age_days")).isEqualTo("30");
        assertThat(params.get("max_age_days")).isEqualTo("365");
        assertThat(params.get("min_intelligence_index")).isEqualTo("40.5");
        assertThat(params.get("max_intelligence_index")).isEqualTo("60.0");
        assertThat(params.get("min_coding_index")).isEqualTo("35.0");
        assertThat(params.get("max_coding_index")).isEqualTo("55.5");
        assertThat(params.get("min_agentic_index")).isEqualTo("20.0");
        assertThat(params.get("max_agentic_index")).isEqualTo("45.0");
        assertThat(params.get("min_tool_success_rate")).isEqualTo("0.5");
        assertThat(params.get("max_tool_success_rate")).isEqualTo("0.95");

        assertThat(request.getRelativeUrl())
                .startsWith("/models?")
                .contains("min_age_days=30")
                .contains("min_tool_success_rate=0.5");
    }

    @Test
    void listRequestOmitsUnsetNewFilters() {
        OpenRouterModelsListRequest request = new OpenRouterModelsListRequest.Builder(client()).build();

        assertThat(request.queryParams()).isEmpty();
        assertThat(request.getRelativeUrl()).isEqualTo("/models");
    }

    @Test
    void listResponseSurfacesTypedModelFields() {
        OpenRouterModelsListResponse response = listResponseOf("""
                {
                  "data": [
                    {
                      "id": "openai/gpt-4",
                      "canonical_slug": "openai/gpt-4",
                      "name": "GPT-4",
                      "created": 1692901234,
                      "description": "GPT-4 is a large multimodal model.",
                      "context_length": 8192,
                      "hugging_face_id": "microsoft/DialoGPT-medium",
                      "expiration_date": "2025-06-01",
                      "knowledge_cutoff": "2024-10-01",
                      "pricing": {"prompt": "0.00003", "completion": "0.00006", "request": "0", "image": "0",
                                  "input_cache_read": "0.0000025", "input_cache_write": "0.00000625"},
                      "architecture": {
                        "modality": "text->text",
                        "input_modalities": ["text"],
                        "output_modalities": ["text"],
                        "tokenizer": "GPT",
                        "instruct_type": "chatml"
                      },
                      "supported_parameters": ["temperature", "top_p", "max_tokens"],
                      "supported_voices": ["alloy", "echo"],
                      "top_provider": {"context_length": 8192, "max_completion_tokens": 4096, "is_moderated": true}
                    }
                  ]
                }
                """);

        List<OpenRouterModel> models = response.models();
        assertThat(models).hasSize(1);

        OpenRouterModel model = models.get(0);
        assertThat(model.id()).isEqualTo("openai/gpt-4");
        assertThat(model.name()).isEqualTo("GPT-4");
        assertThat(model.canonicalSlug()).isEqualTo("openai/gpt-4");
        assertThat(model.created()).isEqualTo(1692901234L);
        assertThat(model.description()).isEqualTo("GPT-4 is a large multimodal model.");
        assertThat(model.contextLength()).isEqualTo(8192L);
        assertThat(model.huggingFaceId()).isEqualTo("microsoft/DialoGPT-medium");
        assertThat(model.expirationDate()).isEqualTo("2025-06-01");
        assertThat(model.knowledgeCutoff()).isEqualTo("2024-10-01");
        assertThat(model.pricingPrompt()).isEqualTo("0.00003");
        assertThat(model.pricingCompletion()).isEqualTo("0.00006");
        assertThat(model.pricingRequest()).isEqualTo("0");
        assertThat(model.pricingImage()).isEqualTo("0");
        assertThat(model.pricingAudio()).isNull();
        assertThat(model.pricingInputCacheRead()).isEqualTo("0.0000025");
        assertThat(model.pricingInputCacheWrite()).isEqualTo("0.00000625");
        assertThat(model.pricing()).isNotNull();
        assertThat(model.pricing().optString("prompt")).isEqualTo("0.00003");
        assertThat(model.json()).isNotNull();
        assertThat(model.json().optString("id")).isEqualTo("openai/gpt-4");
        assertThat(model.modality()).isEqualTo("text->text");
        assertThat(model.inputModalities()).containsExactly("text");
        assertThat(model.outputModalities()).containsExactly("text");
        assertThat(model.tokenizer()).isEqualTo("GPT");
        assertThat(model.instructType()).isEqualTo("chatml");
        assertThat(model.supportedParameters()).containsExactly("temperature", "top_p", "max_tokens");
        assertThat(model.supportedVoices()).containsExactly("alloy", "echo");
        assertThat(model.topProviderContextLength()).isEqualTo(8192L);
        assertThat(model.topProviderMaxCompletionTokens()).isEqualTo(4096L);
        assertThat(model.topProviderIsModerated()).isTrue();

        assertThat(response.model("openai/gpt-4")).isNotNull();
        assertThat(response.model("openai/gpt-4").id()).isEqualTo("openai/gpt-4");
        assertThat(response.model("openai/not-there")).isNull();
    }

    @Test
    void listResponseReturnsEmptyWhenDataAbsent() {
        OpenRouterModelsListResponse response = listResponseOf("{}");

        assertThat(response.models()).isEmpty();
        assertThat(response.model("openai/gpt-4")).isNull();
    }

    @Test
    void listResponseSurfacesAliasLinksDefaultParametersPerRequestLimitsReasoningAndBenchmarks() {
        OpenRouterModelsListResponse response = listResponseOf("""
                {
                  "data": [
                    {
                      "id": "~anthropic/claude-sonnet-latest",
                      "canonical_slug": "anthropic/claude-sonnet-4.5",
                      "name": "Claude Sonnet (latest)",
                      "created": 1692901234,
                      "alias_target": {"slug": "anthropic/claude-sonnet-4.5", "name": "Claude Sonnet 4.5"},
                      "links": {"details": "/api/v1/models/anthropic/claude-sonnet-4.5/endpoints"},
                      "default_parameters": {"temperature": 0.7, "top_p": 0.9, "top_k": 0,
                                             "frequency_penalty": 0, "presence_penalty": 0,
                                             "repetition_penalty": 1},
                      "per_request_limits": {"prompt_tokens": 1000, "completion_tokens": 1000},
                      "reasoning": {"default_effort": "medium", "default_enabled": true,
                                    "mandatory": false, "supported_efforts": ["high", "medium", "low", "minimal"],
                                    "supports_max_tokens": true},
                      "benchmarks": {
                        "artificial_analysis": {"intelligence_index": 71.4, "coding_index": 63.2, "agentic_index": 55.8},
                        "design_arena": [
                          {"arena": "models", "category": "website", "elo": 1385.2, "rank": 5, "win_rate": 62.5}
                        ]
                      }
                    }
                  ]
                }
                """);

        List<OpenRouterModel> aliasModels = response.models();
        OpenRouterModel model = aliasModels.get(0);

        assertThat(model.aliasTargetSlug()).isEqualTo("anthropic/claude-sonnet-4.5");
        assertThat(model.aliasTargetName()).isEqualTo("Claude Sonnet 4.5");
        assertThat(model.aliasTarget()).isNotNull();
        assertThat(model.detailsLink())
                .isEqualTo("/api/v1/models/anthropic/claude-sonnet-4.5/endpoints");
        assertThat(model.links()).isNotNull();
        assertThat(model.defaultParameters().optDouble("temperature")).isEqualTo(0.7);
        assertThat(model.defaultParameters().optInt("top_k")).isEqualTo(0);
        assertThat(model.perRequestLimitPromptTokens()).isEqualTo(1000.0);
        assertThat(model.perRequestLimitCompletionTokens()).isEqualTo(1000.0);
        assertThat(model.perRequestLimits()).isNotNull();

        OpenRouterModel.Reasoning reasoning = model.reasoning();
        assertThat(reasoning).isNotNull();
        assertThat(reasoning.defaultEffort()).isEqualTo("medium");
        assertThat(reasoning.defaultEnabled()).isTrue();
        assertThat(reasoning.mandatory()).isFalse();
        assertThat(reasoning.supportedEfforts()).containsExactly("high", "medium", "low", "minimal");
        assertThat(reasoning.supportsMaxTokens()).isTrue();

        OpenRouterModel.Benchmarks benchmarks = model.benchmarks();
        assertThat(benchmarks).isNotNull();
        assertThat(benchmarks.intelligenceIndex()).isEqualTo(71.4);
        assertThat(benchmarks.codingIndex()).isEqualTo(63.2);
        assertThat(benchmarks.agenticIndex()).isEqualTo(55.8);
        assertThat(benchmarks.designArenaEntries()).hasSize(1);
        OpenRouterModel.DesignArenaEntry entry = benchmarks.designArenaEntries().get(0);
        assertThat(entry.arena()).isEqualTo("models");
        assertThat(entry.category()).isEqualTo("website");
        assertThat(entry.elo()).isEqualTo(1385.2);
        assertThat(entry.rank()).isEqualTo(5L);
        assertThat(entry.winRate()).isEqualTo(62.5);
    }

    @Test
    void newModelAccessorsReturnNullWhenFieldsAbsent() {
        OpenRouterModelsListResponse response = listResponseOf("""
                {
                  "data": [
                    {
                      "id": "openai/gpt-4",
                      "canonical_slug": "openai/gpt-4",
                      "name": "GPT-4",
                      "created": 1692901234,
                      "reasoning": {"mandatory": false},
                      "benchmarks": {"design_arena": []}
                    }
                  ]
                }
                """);

        List<OpenRouterModel> plainModels = response.models();
        OpenRouterModel model = plainModels.get(0);

        assertThat(model.aliasTarget()).isNull();
        assertThat(model.aliasTargetSlug()).isNull();
        assertThat(model.aliasTargetName()).isNull();
        assertThat(model.links()).isNull();
        assertThat(model.detailsLink()).isNull();
        assertThat(model.defaultParameters()).isNull();
        assertThat(model.perRequestLimits()).isNull();
        assertThat(model.perRequestLimitPromptTokens()).isNull();
        assertThat(model.perRequestLimitCompletionTokens()).isNull();

        assertThat(model.reasoning().defaultEffort()).isNull();
        assertThat(model.reasoning().defaultEnabled()).isNull();
        assertThat(model.reasoning().mandatory()).isFalse();
        assertThat(model.reasoning().supportedEfforts()).isEmpty();
        assertThat(model.reasoning().supportsMaxTokens()).isNull();

        assertThat(model.benchmarks().intelligenceIndex()).isNull();
        assertThat(model.benchmarks().codingIndex()).isNull();
        assertThat(model.benchmarks().agenticIndex()).isNull();
        assertThat(model.benchmarks().designArenaEntries()).isEmpty();
    }

    @Test
    void countRequestAndResponse() {
        OpenRouterModelsCountRequest request = new OpenRouterModelsCountRequest.Builder(client())
                .outputModalities("text", "image")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/models/count?output_modalities=text%2Cimage");
        assertThat(request.getHttpMethod()).isEqualTo("GET");

        OpenRouterModelsCountRequest plainRequest = new OpenRouterModelsCountRequest.Builder(client()).build();
        assertThat(plainRequest.getRelativeUrl()).isEqualTo("/models/count");

        // regression: filter values must arrive URL-encoded, not raw
        OpenRouterModelsCountRequest encoded = new OpenRouterModelsCountRequest.Builder(client())
                .outputModalities("image&limit=1")
                .build();
        assertThat(encoded.getRelativeUrl())
                .isEqualTo("/models/count?output_modalities=image%26limit%3D1");

        OpenRouterModelsCountResponse response = plainRequest.createResponse(
                "{\"data\": {\"count\": 150}}");
        assertThat(response.count()).isEqualTo(150L);

        assertThat(plainRequest.createResponse("{}").count()).isNull();
    }

    @Test
    void userModelsRequestEmitsPaginationParams() {
        OpenRouterUserModelsRequest request = new OpenRouterUserModelsRequest.Builder(client())
                .offset(20)
                .limit(30)
                .outputModalities("text")
                .build();

        assertThat(request.getRelativeUrl())
                .isEqualTo("/models/user?offset=20&limit=30&output_modalities=text");
        assertThat(request.getHttpMethod()).isEqualTo("GET");

        OpenRouterUserModelsRequest plainRequest = new OpenRouterUserModelsRequest.Builder(client()).build();
        assertThat(plainRequest.getRelativeUrl()).isEqualTo("/models/user");

        // regression: filter values must arrive URL-encoded, not raw
        OpenRouterUserModelsRequest encoded = new OpenRouterUserModelsRequest.Builder(client())
                .outputModalities("image&limit=1", "text x")
                .build();
        assertThat(encoded.getRelativeUrl())
                .isEqualTo("/models/user?output_modalities=image%26limit%3D1%2Ctext+x");

        OpenRouterModelsListResponse<OpenRouterUserModelsRequest> response = plainRequest.createResponse(
                "{\"data\": [{\"id\": \"m/1\", \"name\": \"M\"}]}");
        assertThat(response.models()).hasSize(1);
        assertThat(response.models().get(0).id()).isEqualTo("m/1");
    }

    @Test
    void singleModelRequestEncodesPathSegments() {
        OpenRouterModelRequest request = new OpenRouterModelRequest.Builder(client(), "openai", "gpt-4").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/model/openai/gpt-4");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.author()).isEqualTo("openai");
        assertThat(request.slug()).isEqualTo("gpt-4");

        assertThatThrownBy(() -> new OpenRouterModelRequest.Builder(client(), null, "gpt-4"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void singleModelResponseSurfacesTheEntry() {
        OpenRouterModelRequest request = new OpenRouterModelRequest.Builder(client(), "openai", "gpt-4").build();
        OpenRouterModelResponse response = request.createResponse("""
                {"data": {"id": "openai/gpt-4", "name": "GPT-4", "pricing": {"prompt": "0.00003"}}}
                """);

        assertThat(response.model()).isNotNull();
        assertThat(response.model().id()).isEqualTo("openai/gpt-4");
        assertThat(response.model().pricingPrompt()).isEqualTo("0.00003");

        assertThat(request.createResponse("{}").model()).isNull();
    }

    @Test
    void endpointsRequestAndResponse() {
        OpenRouterModelEndpointsRequest request =
                new OpenRouterModelEndpointsRequest.Builder(client(), "openai", "gpt-4").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/models/openai/gpt-4/endpoints");
        assertThat(request.getHttpMethod()).isEqualTo("GET");

        OpenRouterModelEndpointsResponse response = request.createResponse("""
                {
                  "data": {
                    "id": "openai/gpt-4",
                    "name": "GPT-4",
                    "endpoints": [
                      {"provider": "OpenAI", "model": "openai/gpt-4", "selected": true},
                      {"provider": "Azure", "model": "openai/gpt-4", "selected": false}
                    ]
                  }
                }
                """);

        assertThat(response.model()).isNotNull();
        assertThat(response.model().id()).isEqualTo("openai/gpt-4");
        assertThat(response.endpoints()).hasSize(2);
        assertThat(response.endpoints().get(0).provider()).isEqualTo("OpenAI");
        assertThat(response.endpoints().get(0).model()).isEqualTo("openai/gpt-4");
        assertThat(response.endpoints().get(0).selected()).isTrue();
        assertThat(response.endpoints().get(1).selected()).isFalse();

        assertThat(request.createResponse("{}").endpoints()).isEmpty();
        assertThat(request.createResponse("{}").model()).isNull();
    }

    private OpenRouterModelsListResponse<OpenRouterModelsListRequest> listResponseOf(String json) {
        OpenRouterModelsListRequest request = new OpenRouterModelsListRequest.Builder(client()).build();
        return request.createResponse(json);
    }
}
