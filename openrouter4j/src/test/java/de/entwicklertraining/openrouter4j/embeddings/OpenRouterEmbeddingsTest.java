package de.entwicklertraining.openrouter4j.embeddings;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the embeddings request shape and the response accessors against
 * recorded JSON shapes of POST /embeddings and GET /embeddings/models.
 */
class OpenRouterEmbeddingsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void requestUsesPostMethodOnEmbeddingsUrl() {
        OpenRouterEmbeddingsRequest request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/embeddings");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void singleInputEmitsStringAndRequiredModel() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .build()
                .getBody());

        assertThat(body.getString("model")).isEqualTo("openai/text-embedding-3-small");
        assertThat(body.getString("input")).isEqualTo("hello");
        assertThat(body.has("dimensions")).isFalse();
        assertThat(body.has("encoding_format")).isFalse();
        assertThat(body.has("input_type")).isFalse();
        assertThat(body.has("user")).isFalse();
        assertThat(body.has("provider")).isFalse();
    }

    @Test
    void multipleInputsEmitArray() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .inputs(List.of("a", "b"))
                .build()
                .getBody());

        assertThat(body.getJSONArray("input").toList()).containsExactly("a", "b");
    }

    @Test
    void optionalFieldsAreEmittedOnlyWhenSet() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .dimensions(512)
                .encodingFormat("base64")
                .inputType("search_query")
                .user("user-1234")
                .build()
                .getBody());

        assertThat(body.getInt("dimensions")).isEqualTo(512);
        assertThat(body.getString("encoding_format")).isEqualTo("base64");
        assertThat(body.getString("input_type")).isEqualTo("search_query");
        assertThat(body.getString("user")).isEqualTo("user-1234");
    }

    @Test
    void providerObjectEmittedOnlyWhenAnyOptionSet() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .providerOrder("openai", "azure")
                .requireParameters(true)
                .providerIgnore("deepinfra")
                .build()
                .getBody());

        assertThat(body.getJSONObject("provider").getJSONArray("order").toList())
                .containsExactly("openai", "azure");
        assertThat(body.getJSONObject("provider").getBoolean("require_parameters")).isTrue();
        assertThat(body.getJSONObject("provider").getJSONArray("ignore").toList())
                .containsExactly("deepinfra");
        assertThat(body.getJSONObject("provider").has("only")).isFalse();
        assertThat(body.getJSONObject("provider").has("allow_fallbacks")).isFalse();
    }

    @Test
    void providerOnlyAndAllowFallbacksAreEmitted() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .providerOnly("openai")
                .allowFallbacks(false)
                .build()
                .getBody());

        assertThat(body.getJSONObject("provider").getJSONArray("only").toList())
                .containsExactly("openai");
        // the false half matters: it is the only value that distinguishes the
        // option from an unset boolean
        assertThat(body.getJSONObject("provider").getBoolean("allow_fallbacks")).isFalse();
    }

    @Test
    void providerOrderWithNoValuesEmitsNoProviderObject() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .providerOrder()
                .build()
                .getBody());

        assertThat(body.has("provider")).isFalse();
    }

    @Test
    void inputAndInputsClearEachOther() {
        JSONObject afterInput = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .inputs(List.of("a", "b"))
                .input("x")
                .build()
                .getBody());
        assertThat(afterInput.getString("input")).isEqualTo("x");

        JSONObject afterInputs = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("x")
                .inputs(List.of("a", "b"))
                .build()
                .getBody());
        assertThat(afterInputs.getJSONArray("input").toList()).containsExactly("a", "b");
    }

    @Test
    void buildRejectsMissingModelAndInput() {
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .input("hello")
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void accessorsAreSurfaced() {
        OpenRouterEmbeddingsResponse response = responseOf("""
                {
                  "id": "embd-123",
                  "object": "list",
                  "model": "openai/text-embedding-3-small",
                  "data": [
                    {"object": "embedding", "index": 0, "embedding": [0.5, -0.25]},
                    {"object": "embedding", "index": 1, "embedding": [0.1, 0.2]}
                  ],
                  "usage": {"prompt_tokens": 8, "total_tokens": 8}
                }
                """);

        assertThat(response.id()).isEqualTo("embd-123");
        assertThat(response.model()).isEqualTo("openai/text-embedding-3-small");
        assertThat(response.object()).isEqualTo("list");
        assertThat(response.promptTokens()).isEqualTo(8L);
        assertThat(response.totalTokens()).isEqualTo(8L);
        assertThat(response.embeddings()).hasSize(2);
        assertThat(response.embedding(1).vector()).containsExactly(0.1f, 0.2f);
        assertThat(response.embedding(1).object()).isEqualTo("embedding");
        assertThat(response.embedding(1).vectorOrDecoded()).containsExactly(0.1f, 0.2f);
        assertThat(response.embedding(5)).isNull();
    }

    @Test
    void base64VectorIsDecoded() {
        // float32 little-endian of 0.5 and -0.25, base64-encoded
        String base64 = java.util.Base64.getEncoder().encodeToString(
                new byte[]{
                        0x00, 0x00, 0x00, 0x3F, // 0.5f
                        0x00, 0x00, (byte) 0x80, (byte) 0xBE  // -0.25f
                });

        OpenRouterEmbeddingsResponse response = responseOf("""
                {"data": [{"object": "embedding", "index": 0, "embedding": "%s"}]}
                """.formatted(base64));

        OpenRouterEmbedding embedding = response.embedding(0);
        assertThat(embedding.vector()).isNull();
        assertThat(embedding.embeddingBase64()).isEqualTo(base64);
        assertThat(embedding.vectorFromBase64()).containsExactly(0.5f, -0.25f);
        assertThat(embedding.vectorOrDecoded()).containsExactly(0.5f, -0.25f);
    }

    @Test
    void accessorsReturnNullWhenAbsent() {
        OpenRouterEmbeddingsResponse response = responseOf("{}");

        assertThat(response.id()).isNull();
        assertThat(response.model()).isNull();
        assertThat(response.object()).isNull();
        assertThat(response.usage()).isNull();
        assertThat(response.promptTokens()).isNull();
        assertThat(response.totalTokens()).isNull();
        assertThat(response.embeddings()).isEmpty();
    }

    @Test
    void accessorsSwallowMalformedBodies() {
        OpenRouterEmbeddingsResponse response = responseOf(
                "{\"data\": \"not-an-array\", \"usage\": \"not-an-object\", \"id\": 42}");

        assertThat(response.embeddings()).isEmpty();
        assertThat(response.usage()).isNull();
        assertThat(response.promptTokens()).isNull();
        assertThat(response.totalTokens()).isNull();
        // the coerced string form still documents the swallow behaviour of
        // the string accessors on a malformed body
        assertThat(response.id()).isEqualTo("42");
    }

    @Test
    void modelsRequestUsesGetMethodAndOptionalPagination() {
        OpenRouterEmbeddingsModelsRequest request =
                new OpenRouterEmbeddingsModelsRequest.Builder(client()).build();

        assertThat(request.getRelativeUrl()).isEqualTo("/embeddings/models");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();

        OpenRouterEmbeddingsModelsRequest paged =
                new OpenRouterEmbeddingsModelsRequest.Builder(client())
                        .offset(10)
                        .limit(20)
                        .build();

        assertThat(paged.getRelativeUrl()).isEqualTo("/embeddings/models?offset=10&limit=20");

        OpenRouterEmbeddingsModelsRequest limitOnly =
                new OpenRouterEmbeddingsModelsRequest.Builder(client())
                        .limit(20)
                        .build();

        assertThat(limitOnly.getRelativeUrl()).isEqualTo("/embeddings/models?limit=20");
    }

    @Test
    void modelsResponseInheritsCatalogAccessors() {
        OpenRouterEmbeddingsModelsRequest request =
                new OpenRouterEmbeddingsModelsRequest.Builder(client()).build();
        OpenRouterEmbeddingsModelsResponse response = new OpenRouterEmbeddingsModelsResponse(
                new JSONObject("""
                        {
                          "data": [
                            {"id": "openai/text-embedding-3-small", "name": "OpenAI Text Embedding 3 Small"}
                          ]
                        }
                        """),
                request);

        assertThat(response.models()).hasSize(1);
        assertThat(response.model("openai/text-embedding-3-small").name())
                .isEqualTo("OpenAI Text Embedding 3 Small");
        assertThat(response.model("unknown")).isNull();
    }

    private OpenRouterEmbeddingsResponse responseOf(String json) {
        OpenRouterEmbeddingsRequest request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .build();
        return request.createResponse(json);
    }
}
