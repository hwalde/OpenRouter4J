package de.entwicklertraining.openrouter4j.embeddings;

import de.entwicklertraining.openrouter4j.OpenRouterPercentileCutoffs;
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
    void responseCachingHeadersAndCustomHeaderAreEmitted() {
        OpenRouterEmbeddingsRequest request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .responseCache(true)
                .build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache", "true");

        request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .responseCache(false)
                .build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache", "false");

        request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("hello").responseCacheClear(true).build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache-Clear", "true");

        request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("hello").responseCacheTtl(7).build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache-TTL", "7");

        request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("hello").responseCacheTtl(1).build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache-TTL", "1");

        request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("hello").responseCacheTtl(86400).build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache-TTL", "86400");

        request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("hello").header("X-Custom-Header", "v").build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-Custom-Header", "v");

        OpenRouterEmbeddingsRequest bare = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .build();
        assertThat(bare.getAdditionalHeaders())
                .doesNotContainKey("X-OpenRouter-Cache")
                .doesNotContainKey("X-OpenRouter-Cache-Clear")
                .doesNotContainKey("X-OpenRouter-Cache-TTL")
                .doesNotContainKey("X-Custom-Header");

        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").responseCache(true).responseCache(null).build().getAdditionalHeaders())
                .doesNotContainKey("X-OpenRouter-Cache");
        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").responseCacheClear(true).responseCacheClear(false).build().getAdditionalHeaders())
                .doesNotContainKey("X-OpenRouter-Cache-Clear");
        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").responseCacheTtl(600).responseCacheTtl(null).build().getAdditionalHeaders())
                .doesNotContainKey("X-OpenRouter-Cache-TTL");
        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header("X-C", "v").header("X-C", null).build().getAdditionalHeaders())
                .doesNotContainKey("X-C");

        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").responseCache(true)
                .header("X-OpenRouter-Cache", "custom").build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache", "custom");
        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header("X-OpenRouter-Cache", "x")
                .responseCache(null).build().getAdditionalHeaders())
                .doesNotContainKey("X-OpenRouter-Cache");
        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").responseCacheTtl(600)
                .header("X-OpenRouter-Cache-TTL", "120").build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache-TTL", "120");
        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header("X-OpenRouter-Cache", "x")
                .responseCache(true).build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache", "true");
        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header("X-OpenRouter-Cache-TTL", "120")
                .responseCacheTtl(600).build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache-TTL", "600");
        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").responseCacheClear(true)
                .header("X-OpenRouter-Cache-Clear", "x").build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache-Clear", "x");
        assertThat(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header("X-OpenRouter-Cache-Clear", "x")
                .responseCacheClear(true).build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache-Clear", "true");

        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").responseCacheTtl(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").responseCacheTtl(86401))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void headerEscapeHatchValidatesNamesAndValues() {
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header(null, "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header("  ", "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header("X-Bad\rName", "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header("X-Bad\nName", "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header("X-Name", "bad\rvalue"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("x").header("X-Name", "bad\nvalue"))
                .isInstanceOf(IllegalArgumentException.class);
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
        assertThat(response.embedding(1).index()).isEqualTo(1);
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
    void itemObjectFormEmitsTypedItems() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .addInput(OpenRouterEmbeddingsInputItem.text("a caption"))
                .addInput(OpenRouterEmbeddingsInputItem.imageUrl("https://example.com/img.png"))
                .build()
                .getBody());

        org.json.JSONArray input = body.getJSONArray("input");
        assertThat(input.length()).isEqualTo(2);
        JSONObject textItem = input.getJSONObject(0);
        assertThat(textItem.keySet()).containsExactlyInAnyOrder("content");
        JSONObject textPart = textItem.getJSONArray("content").getJSONObject(0);
        assertThat(textPart.keySet()).containsExactlyInAnyOrder("type", "text");
        assertThat(textPart.getString("type")).isEqualTo("text");
        assertThat(textPart.getString("text")).isEqualTo("a caption");
        JSONObject imageItem = input.getJSONObject(1);
        JSONObject imagePart = imageItem.getJSONArray("content").getJSONObject(0);
        assertThat(imagePart.keySet()).containsExactlyInAnyOrder("type", "image_url");
        assertThat(imagePart.getJSONObject("image_url").keySet()).containsExactlyInAnyOrder("url");
        assertThat(imagePart.getJSONObject("image_url").getString("url"))
                .isEqualTo("https://example.com/img.png");
    }

    @Test
    void multiPartItemComposesContentPartsInOrder() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(OpenRouterEmbeddingsInputItem.of(
                        OpenRouterEmbeddingsContentPart.imageUrl("https://example.com/img.png"),
                        OpenRouterEmbeddingsContentPart.text("what is shown?")))
                .build()
                .getBody());

        org.json.JSONArray content = body.getJSONArray("input")
                .getJSONObject(0).getJSONArray("content");
        assertThat(content.length()).isEqualTo(2);
        assertThat(content.getJSONObject(0).getString("type")).isEqualTo("image_url");
        assertThat(content.getJSONObject(1).getString("type")).isEqualTo("text");
        assertThat(content.getJSONObject(1).getString("text")).isEqualTo("what is shown?");

        JSONObject listForm = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(OpenRouterEmbeddingsInputItem.of(List.of(
                        OpenRouterEmbeddingsContentPart.text("a"),
                        OpenRouterEmbeddingsContentPart.text("b"))))
                .build()
                .getBody());
        org.json.JSONArray listContent = listForm.getJSONArray("input")
                .getJSONObject(0).getJSONArray("content");
        assertThat(listContent.length()).isEqualTo(2);
        assertThat(listContent.getJSONObject(0).getString("text")).isEqualTo("a");
        assertThat(listContent.getJSONObject(1).getString("text")).isEqualTo("b");
    }

    @Test
    void inputItemEscapeHatchPassesMultimodalPartsVerbatim() {
        JSONObject rawItem = new JSONObject("""
                {"content": [
                  {"type": "input_audio", "input_audio": {"data": "data:audio/wav;base64,AAAA", "format": "wav"}}
                ]}
                """);
        OpenRouterEmbeddingsRequest request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInputItem(rawItem)
                .build();
        assertThat(new JSONObject(request.getBody()).getJSONArray("input").getJSONObject(0).toString())
                .isEqualTo(rawItem.toString());

        // the hatch copies: mutating the caller's object afterwards cannot
        // change what the built request sends
        rawItem.getJSONArray("content").getJSONObject(0).put("type", "text");
        assertThat(new JSONObject(request.getBody()).getJSONArray("input").getJSONObject(0)
                .getJSONArray("content").getJSONObject(0).getString("type")).isEqualTo("input_audio");

        // and the read-back accessor hands out detached copies too
        request.inputItems().get(0).getJSONArray("content").getJSONObject(0).put("type", "mutated");
        assertThat(new JSONObject(request.getBody()).getJSONArray("input").getJSONObject(0)
                .getJSONArray("content").getJSONObject(0).getString("type")).isEqualTo("input_audio");

        // the raw() item factory covers the same shapes under the typed addInput path
        JSONObject typedItem = new JSONObject("""
                {"content": [
                  {"type": "input_audio", "input_audio": {"data": "data:audio/wav;base64,AAAA", "format": "wav"}}
                ]}
                """);
        OpenRouterEmbeddingsRequest rawTyped = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(OpenRouterEmbeddingsInputItem.raw(typedItem))
                .build();
        assertThat(new JSONObject(rawTyped.getBody()).getJSONArray("input").getJSONObject(0).toString())
                .isEqualTo(typedItem.toString());
        typedItem.getJSONArray("content").getJSONObject(0).put("type", "text");
        assertThat(new JSONObject(rawTyped.getBody()).getJSONArray("input").getJSONObject(0)
                .getJSONArray("content").getJSONObject(0).getString("type")).isEqualTo("input_audio");

        // and the raw() content-part factory covers one part inside a typed item
        JSONObject rawPart = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(OpenRouterEmbeddingsInputItem.of(OpenRouterEmbeddingsContentPart.raw(
                        new JSONObject("{\"type\":\"input_file\",\"input_file\":{\"data\":\"data:application/pdf;base64,AAA\",\"format\":\"pdf\"}}"))))
                .build()
                .getBody());
        assertThat(rawPart.getJSONArray("input").getJSONObject(0)
                .getJSONArray("content").getJSONObject(0).getString("type")).isEqualTo("input_file");
    }

    @Test
    void tokenArrayFormEmitsSingleNumberArray() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .inputTokenArray(1, 2, 3)
                .build()
                .getBody());

        assertThat(body.getJSONArray("input").toList()).containsExactly(1, 2, 3);
    }

    @Test
    void tokenArraysFormEmitsNestedNumberArrays() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .inputTokens(new int[]{1, 2}, new int[]{3, 4})
                .build()
                .getBody());

        org.json.JSONArray input = body.getJSONArray("input");
        assertThat(input.length()).isEqualTo(2);
        assertThat(input.getJSONArray(0).toList()).containsExactly(1, 2);
        assertThat(input.getJSONArray(1).toList()).containsExactly(3, 4);
    }

    @Test
    void lastInputFormSetWinsAcrossAllTransitions() {
        // item form clears the string forms ...
        OpenRouterEmbeddingsRequest itemsAfterStrings = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .inputs(List.of("a"))
                .addInput(OpenRouterEmbeddingsInputItem.text("i"))
                .build();
        assertThat(new JSONObject(itemsAfterStrings.getBody()).getJSONArray("input").length())
                .isEqualTo(1);
        assertThat(itemsAfterStrings.input()).isNull();
        assertThat(itemsAfterStrings.inputs()).isNull();

        // ... and every later form clears the accumulated items
        OpenRouterEmbeddingsRequest stringAfterItems = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(OpenRouterEmbeddingsInputItem.text("i"))
                .input("plain")
                .build();
        assertThat(new JSONObject(stringAfterItems.getBody()).getString("input")).isEqualTo("plain");
        assertThat(stringAfterItems.inputItems()).isNull();

        OpenRouterEmbeddingsRequest tokensAfterItems = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(OpenRouterEmbeddingsInputItem.text("i"))
                .inputTokenArray(7)
                .build();
        assertThat(new JSONObject(tokensAfterItems.getBody()).getJSONArray("input").toList())
                .containsExactly(7);
        assertThat(tokensAfterItems.inputItems()).isNull();

        OpenRouterEmbeddingsRequest rowsAfterTokens = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .inputTokenArray(7)
                .inputTokens(new int[]{5})
                .build();
        assertThat(new JSONObject(rowsAfterTokens.getBody()).getJSONArray("input")
                .getJSONArray(0).toList()).containsExactly(5);
        assertThat(rowsAfterTokens.inputTokenArray()).isNull();

        OpenRouterEmbeddingsRequest listAfterRows = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .inputTokens(new int[]{5})
                .inputs(List.of("a", "b"))
                .build();
        assertThat(new JSONObject(listAfterRows.getBody()).getJSONArray("input").toList())
                .containsExactly("a", "b");
        assertThat(listAfterRows.inputTokens()).isNull();

        OpenRouterEmbeddingsRequest tokenArrayAfterList = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .inputs(List.of("a"))
                .inputTokenArray(9)
                .build();
        assertThat(new JSONObject(tokenArrayAfterList.getBody()).getJSONArray("input").toList())
                .containsExactly(9);
        assertThat(tokenArrayAfterList.inputs()).isNull();

        // addInput appends to the item form instead of replacing it
        JSONObject twoItems = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(OpenRouterEmbeddingsInputItem.text("one"))
                .addInput(OpenRouterEmbeddingsInputItem.text("two"))
                .build()
                .getBody());
        assertThat(twoItems.getJSONArray("input").length()).isEqualTo(2);

        // a later addInput switches back to the item form and drops the string
        // set in between (the last form set wins, also across add-calls)
        OpenRouterEmbeddingsRequest chained = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(OpenRouterEmbeddingsInputItem.text("x"))
                .input("p")
                .addInput(OpenRouterEmbeddingsInputItem.text("y"))
                .build();
        org.json.JSONArray chainedInput = new JSONObject(chained.getBody()).getJSONArray("input");
        assertThat(chainedInput.length()).isEqualTo(1);
        assertThat(chainedInput.getJSONObject(0).getJSONArray("content")
                .getJSONObject(0).getString("text")).isEqualTo("y");
        assertThat(chained.input()).isNull();

        // typed and hatch items append to one shared list, in call order
        JSONObject mixed = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(OpenRouterEmbeddingsInputItem.text("typed"))
                .addInputItem(new JSONObject("{\"content\": [{\"type\": \"text\", \"text\": \"raw\"}]}"))
                .build()
                .getBody());
        org.json.JSONArray mixedInput = mixed.getJSONArray("input");
        assertThat(mixedInput.length()).isEqualTo(2);
        assertThat(mixedInput.getJSONObject(0).getJSONArray("content")
                .getJSONObject(0).getString("text")).isEqualTo("typed");
        assertThat(mixedInput.getJSONObject(1).getJSONArray("content")
                .getJSONObject(0).getString("text")).isEqualTo("raw");

        JSONObject mixedReverse = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInputItem(new JSONObject("{\"content\": [{\"type\": \"text\", \"text\": \"raw\"}]}"))
                .addInput(OpenRouterEmbeddingsInputItem.text("typed"))
                .build()
                .getBody());
        org.json.JSONArray mixedReverseInput = mixedReverse.getJSONArray("input");
        assertThat(mixedReverseInput.length()).isEqualTo(2);
        assertThat(mixedReverseInput.getJSONObject(0).getJSONArray("content")
                .getJSONObject(0).getString("text")).isEqualTo("raw");
        assertThat(mixedReverseInput.getJSONObject(1).getJSONArray("content")
                .getJSONObject(0).getString("text")).isEqualTo("typed");
    }

    @Test
    void inputReadBackAccessorsPerForm() {
        OpenRouterEmbeddingsRequest stringForm = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("hello").build();
        assertThat(stringForm.input()).isEqualTo("hello");
        assertThat(stringForm.inputs()).isNull();
        assertThat(stringForm.inputItems()).isNull();
        assertThat(stringForm.inputTokenArray()).isNull();
        assertThat(stringForm.inputTokens()).isNull();

        OpenRouterEmbeddingsRequest listForm = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").inputs(List.of("a", "b")).build();
        assertThat(listForm.input()).isNull();
        assertThat(listForm.inputs()).containsExactly("a", "b");

        OpenRouterEmbeddingsRequest itemForm = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(OpenRouterEmbeddingsInputItem.text("i"))
                .build();
        assertThat(itemForm.input()).isNull();
        assertThat(itemForm.inputs()).isNull();
        assertThat(itemForm.inputItems()).hasSize(1);
        assertThat(itemForm.inputItems().get(0).getJSONArray("content")
                .getJSONObject(0).getString("text")).isEqualTo("i");
        assertThat(itemForm.inputTokenArray()).isNull();
        assertThat(itemForm.inputTokens()).isNull();

        OpenRouterEmbeddingsRequest tokenForm = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").inputTokenArray(1, 2).build();
        assertThat(tokenForm.inputTokenArray()).containsExactly(1, 2);
        assertThat(tokenForm.inputTokens()).isNull();

        OpenRouterEmbeddingsRequest rowsForm = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").inputTokens(new int[]{1, 2}, new int[]{3}).build();
        assertThat(rowsForm.inputTokens()).hasSize(2);
        assertThat(rowsForm.inputTokens().get(0)).containsExactly(1, 2);
        assertThat(rowsForm.inputTokens().get(1)).containsExactly(3);
        assertThat(rowsForm.inputTokenArray()).isNull();
    }

    @Test
    void inputFactoriesValidateAndStayImmutable() {
        assertThatThrownBy(() -> OpenRouterEmbeddingsContentPart.text(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterEmbeddingsContentPart.text(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterEmbeddingsContentPart.imageUrl(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterEmbeddingsContentPart.raw(new JSONObject()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterEmbeddingsContentPart.raw(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> OpenRouterEmbeddingsInputItem.of())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterEmbeddingsInputItem.of(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterEmbeddingsInputItem.of(
                (OpenRouterEmbeddingsContentPart) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> OpenRouterEmbeddingsInputItem.raw(new JSONObject()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterEmbeddingsInputItem.raw(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").addInput(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").addInputItem(null))
                .isInstanceOf(IllegalArgumentException.class);

        // toJson() hands out detached copies - the value class stays immutable
        OpenRouterEmbeddingsInputItem item = OpenRouterEmbeddingsInputItem.text("t");
        JSONObject copy = item.toJson();
        copy.getJSONArray("content").getJSONObject(0).put("text", "mutated");
        assertThat(item.toJson().getJSONArray("content").getJSONObject(0).getString("text"))
                .isEqualTo("t");
        OpenRouterEmbeddingsContentPart part = OpenRouterEmbeddingsContentPart.text("p");
        JSONObject partCopy = part.toJson();
        partCopy.put("text", "mutated");
        assertThat(part.toJson().getString("text")).isEqualTo("p");
    }

    @Test
    void emptyTokenFormsAreRejectedAtBuild() {
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").inputTokenArray().build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").inputTokens().build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").inputTokens((int[]) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").inputTokenArray((int[]) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").inputTokens(new int[]{1}, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> OpenRouterEmbeddingsContentPart.imageUrl(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void inputItemsAreSnapshottedAtAddTimeAndBuildTime() {
        // add-time snapshot: mutating the caller's item between addInputItem
        // and build cannot change what the request sends (a shared item
        // template must not collapse every entry to its last state)
        JSONObject shared = new JSONObject("""
                {"content": [
                  {"type": "input_audio", "input_audio": {"data": "data:audio/wav;base64,AAAA", "format": "wav"}}
                ]}
                """);
        OpenRouterEmbeddingsRequest.Builder hatchBuilder = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInputItem(shared);
        shared.getJSONArray("content").getJSONObject(0).put("type", "text");
        OpenRouterEmbeddingsRequest built = hatchBuilder.build();
        assertThat(new JSONObject(built.getBody()).getJSONArray("input").getJSONObject(0)
                .getJSONArray("content").getJSONObject(0).getString("type")).isEqualTo("input_audio");

        // the typed raw() factories copy at creation: mutating their source
        // BEFORE the item is added (and before build) changes nothing
        JSONObject rawSource = new JSONObject("""
                {"content": [
                  {"type": "input_audio", "input_audio": {"data": "data:audio/wav;base64,AAAA", "format": "wav"}}
                ]}
                """);
        OpenRouterEmbeddingsInputItem rawItem = OpenRouterEmbeddingsInputItem.raw(rawSource);
        rawSource.getJSONArray("content").getJSONObject(0).put("type", "text");
        assertThat(rawItem.toJson().getJSONArray("content").getJSONObject(0).getString("type"))
                .isEqualTo("input_audio");
        OpenRouterEmbeddingsRequest typedBuilt = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInput(rawItem)
                .build();
        assertThat(new JSONObject(typedBuilt.getBody()).getJSONArray("input").getJSONObject(0)
                .getJSONArray("content").getJSONObject(0).getString("type")).isEqualTo("input_audio");

        JSONObject rawPartSource = new JSONObject(
                "{\"type\": \"input_audio\", \"input_audio\": {\"data\": \"data:audio/wav;base64,AAAA\", \"format\": \"wav\"}}");
        OpenRouterEmbeddingsContentPart rawPart = OpenRouterEmbeddingsContentPart.raw(rawPartSource);
        rawPartSource.put("type", "text");
        assertThat(rawPart.toJson().getString("type")).isEqualTo("input_audio");

        // build-time snapshot: reusing the builder afterwards cannot rewrite
        // an already-built request
        OpenRouterEmbeddingsRequest.Builder reused = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m")
                .addInputItem(new JSONObject("{\"content\": [{\"type\": \"text\", \"text\": \"one\"}]}"));
        OpenRouterEmbeddingsRequest first = reused.build();
        reused.addInputItem(new JSONObject("{\"content\": [{\"type\": \"text\", \"text\": \"two\"}]}"));
        assertThat(new JSONObject(first.getBody()).getJSONArray("input").length()).isEqualTo(1);
        assertThat(first.inputItems()).hasSize(1);
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

        OpenRouterEmbeddingsModelsRequest offsetOnly =
                new OpenRouterEmbeddingsModelsRequest.Builder(client())
                        .offset(10)
                        .build();

        assertThat(offsetOnly.getRelativeUrl()).isEqualTo("/embeddings/models?offset=10");
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

    @Test
    void providerNewFieldsAreEmittedWhenSet() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .dataCollection("deny")
                .quantizations("int4", "fp8")
                .sortBy("price", "none")
                .maxPrice("0.5", "1.0", "2.0", "0.1")
                .preferredMaxLatency(2.5)
                .preferredMinThroughput(OpenRouterPercentileCutoffs.builder().p50(100.0).build())
                .enforceDistillableText(true)
                .zdr(true)
                .build()
                .getBody());
        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getString("data_collection")).isEqualTo("deny");
        assertThat(provider.getJSONArray("quantizations").toList()).containsExactly("int4", "fp8");
        JSONObject sort = provider.getJSONObject("sort");
        assertThat(sort.getString("by")).isEqualTo("price");
        assertThat(sort.getString("partition")).isEqualTo("none");
        JSONObject maxPrice = provider.getJSONObject("max_price");
        assertThat(maxPrice.getString("prompt")).isEqualTo("0.5");
        assertThat(maxPrice.getString("completion")).isEqualTo("1.0");
        assertThat(maxPrice.getString("image")).isEqualTo("2.0");
        assertThat(maxPrice.getString("audio")).isEqualTo("0.1");
        assertThat(provider.getDouble("preferred_max_latency")).isEqualTo(2.5);
        assertThat(provider.getJSONObject("preferred_min_throughput").getDouble("p50")).isEqualTo(100.0);
        assertThat(provider.getBoolean("enforce_distillable_text")).isTrue();
        assertThat(provider.getBoolean("zdr")).isTrue();
    }

    @Test
    void providerNewFieldsAreAbsentWhenUnset() {
        JSONObject bare = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .build()
                .getBody());
        assertThat(bare.has("provider")).isFalse();

        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .requireParameters(true)
                .build()
                .getBody());
        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.has("data_collection")).isFalse();
        assertThat(provider.has("quantizations")).isFalse();
        assertThat(provider.has("sort")).isFalse();
        assertThat(provider.has("max_price")).isFalse();
        assertThat(provider.has("preferred_max_latency")).isFalse();
        assertThat(provider.has("preferred_min_throughput")).isFalse();
        assertThat(provider.has("enforce_distillable_text")).isFalse();
        assertThat(provider.has("zdr")).isFalse();
    }

    @Test
    void providerObjectEmittedWhenOnlyNewFieldSet() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .zdr(true)
                .build()
                .getBody());
        assertThat(body.has("provider")).isTrue();
        assertThat(body.getJSONObject("provider").getBoolean("zdr")).isTrue();
    }

    @Test
    void providerSortPlainFormAndMaxPriceTwoArgAreEmitted() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .sort("latency")
                .maxPrice("0.5", "1.0")
                .preferredMinThroughput(50.0)
                .build()
                .getBody());
        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getString("sort")).isEqualTo("latency");
        JSONObject maxPrice = provider.getJSONObject("max_price");
        assertThat(maxPrice.keySet()).containsOnly("prompt", "completion");
        assertThat(provider.getDouble("preferred_min_throughput")).isEqualTo(50.0);
    }

    @Test
    void providerSortByObjectFormWinsOverPlainForm() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .sort("price")
                .sortBy("latency", "none")
                .build()
                .getBody());
        JSONObject sort = body.getJSONObject("provider").getJSONObject("sort");
        assertThat(sort.getString("by")).isEqualTo("latency");
    }

    @Test
    void providerPreferredLatencyCutoffsFormIsEmitted() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .preferredMaxLatency(OpenRouterPercentileCutoffs.builder().p50(1.0).p90(3.5).build())
                .build()
                .getBody());
        JSONObject cutoffs = body.getJSONObject("provider").getJSONObject("preferred_max_latency");
        assertThat(cutoffs.getDouble("p50")).isEqualTo(1.0);
        assertThat(cutoffs.getDouble("p90")).isEqualTo(3.5);
        assertThat(cutoffs.has("p75")).isFalse();
    }

    @Test
    void providerSortByRejectsBlankPartition() {
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("i").sortBy("price", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("partition");
        assertThatThrownBy(() -> new OpenRouterEmbeddingsRequest.Builder(client())
                .model("m").input("i").sortBy("price", "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void providerQuantizationsEmptyListClears() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .quantizations("int4", "fp8")
                .quantizations(List.of())
                .requireParameters(true)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").has("quantizations")).isFalse();
    }

    @Test
    void providerPreferredMinThroughputNumberWinsOverCutoffs() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .preferredMinThroughput(OpenRouterPercentileCutoffs.builder().p50(10.0).build())
                .preferredMinThroughput(30.0)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").getDouble("preferred_min_throughput")).isEqualTo(30.0);
    }

    @Test
    void providerPreferredLatencyNumberWinsOverCutoffs() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .preferredMaxLatency(OpenRouterPercentileCutoffs.builder().p50(1.0).build())
                .preferredMaxLatency(2.5)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").getDouble("preferred_max_latency")).isEqualTo(2.5);
    }

    @Test
    void providerAccessorsRoundTrip() {
        var request = new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .dataCollection("deny")
                .zdr(true)
                .sort("price")
                .enforceDistillableText(true)
                .build();
        assertThat(request.dataCollection()).isEqualTo("deny");
        assertThat(request.zdr()).isTrue();
        assertThat(request.sort()).isEqualTo("price");
        assertThat(request.enforceDistillableText()).isTrue();
        assertThat(request.quantizations()).isEmpty();
        assertThat(request.sortBy()).isNull();
        assertThat(request.maxPricePrompt()).isNull();
        assertThat(request.preferredMaxLatency()).isNull();
    }

    @Test
    void providerSortByObjectWinsRegardlessOfCallOrder() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .sortBy("latency", "none")
                .sort("price")
                .build()
                .getBody());
        JSONObject sort = body.getJSONObject("provider").getJSONObject("sort");
        assertThat(sort.getString("by")).isEqualTo("latency");
    }

    @Test
    void providerPreferredLatencyNumberWinsRegardlessOfCallOrder() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .preferredMaxLatency(2.5)
                .preferredMaxLatency(OpenRouterPercentileCutoffs.builder().p50(1.0).build())
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").getDouble("preferred_max_latency")).isEqualTo(2.5);
    }

    @Test
    void providerSortByNullCriterionEmitsNoSortKey() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .sortBy(null, "none")
                .requireParameters(true)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").has("sort")).isFalse();
    }
}
