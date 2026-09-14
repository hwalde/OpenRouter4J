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
                .queryParam("min_intelligence_index", 50)
                .queryParam("region", null)
                .build();

        assertThat(request.queryParams()).containsOnlyKeys("min_intelligence_index");
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
                      "hugging_face_id": null,
                      "pricing": {"prompt": "0.00003", "completion": "0.00006", "request": "0", "image": "0"},
                      "architecture": {
                        "modality": "text->text",
                        "input_modalities": ["text"],
                        "output_modalities": ["text"],
                        "tokenizer": "GPT",
                        "instruct_type": "chatml"
                      },
                      "supported_parameters": ["temperature", "top_p", "max_tokens"],
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
        assertThat(model.pricingPrompt()).isEqualTo("0.00003");
        assertThat(model.pricingCompletion()).isEqualTo("0.00006");
        assertThat(model.pricingRequest()).isEqualTo("0");
        assertThat(model.pricingImage()).isEqualTo("0");
        assertThat(model.pricingAudio()).isNull();
        assertThat(model.modality()).isEqualTo("text->text");
        assertThat(model.inputModalities()).containsExactly("text");
        assertThat(model.outputModalities()).containsExactly("text");
        assertThat(model.tokenizer()).isEqualTo("GPT");
        assertThat(model.instructType()).isEqualTo("chatml");
        assertThat(model.supportedParameters()).containsExactly("temperature", "top_p", "max_tokens");
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
    void countRequestAndResponse() {
        OpenRouterModelsCountRequest request = new OpenRouterModelsCountRequest.Builder(client())
                .outputModalities("text", "image")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/models/count?output_modalities=text,image");
        assertThat(request.getHttpMethod()).isEqualTo("GET");

        OpenRouterModelsCountRequest plainRequest = new OpenRouterModelsCountRequest.Builder(client()).build();
        assertThat(plainRequest.getRelativeUrl()).isEqualTo("/models/count");

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
