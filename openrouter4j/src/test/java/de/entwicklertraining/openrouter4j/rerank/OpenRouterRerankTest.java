package de.entwicklertraining.openrouter4j.rerank;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the rerank request shape and the response accessors against recorded
 * JSON shapes of POST /rerank.
 */
class OpenRouterRerankTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void requestUsesPostMethodOnRerankUrl() {
        OpenRouterRerankRequest request = new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("What is the capital of France?")
                .addDocument("Paris is the capital of France.")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/rerank");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void plainDocumentsAreEmittedAsStrings() {
        JSONObject body = new JSONObject(new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("What is the capital of France?")
                .addDocument("Paris is the capital of France.")
                .addDocument("Berlin is the capital of Germany.")
                .build()
                .getBody());

        assertThat(body.getString("model")).isEqualTo("cohere/rerank-v3.5");
        assertThat(body.getString("query")).isEqualTo("What is the capital of France?");
        assertThat(body.getJSONArray("documents").toList())
                .containsExactly("Paris is the capital of France.", "Berlin is the capital of Germany.");
        assertThat(body.has("top_n")).isFalse();
        assertThat(body.has("provider")).isFalse();
    }

    @Test
    void structuredDocumentsAreEmittedAsObjects() {
        JSONObject body = new JSONObject(new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("What is the capital of France?")
                .addDocument("Paris is the capital of France.", null)
                .addDocument(null, "https://example.com/image.png")
                .addDocument("text and image", "data:image/png;base64,AAAA")
                .build()
                .getBody());

        assertThat(body.getJSONArray("documents").length()).isEqualTo(3);
        assertThat(body.getJSONArray("documents").getJSONObject(0).toMap())
                .containsOnlyKeys("text");
        assertThat(body.getJSONArray("documents").getJSONObject(1).toMap())
                .containsOnlyKeys("image");
        assertThat(body.getJSONArray("documents").getJSONObject(2).toMap())
                .containsKeys("text", "image");
    }

    @Test
    void emptyTextNormalizesToImageOnlyDocument() {
        JSONObject body = new JSONObject(new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument("", "https://example.com/image.png")
                .build()
                .getBody());

        assertThat(body.getJSONArray("documents").getJSONObject(0).toMap())
                .containsOnlyKeys("image");
    }

    @Test
    void emptyPlainDocumentIsRejected() {
        assertThatThrownBy(() -> new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void structuredDocumentWithoutTextOrImageIsRejected() {
        assertThatThrownBy(() -> new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument(null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void topNIsEmittedOnlyWhenSet() {
        JSONObject body = new JSONObject(new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument("doc")
                .topN(3)
                .build()
                .getBody());

        assertThat(body.getInt("top_n")).isEqualTo(3);
    }

    @Test
    void providerObjectEmittedOnlyWhenAnyOptionSet() {
        JSONObject set = new JSONObject(new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument("doc")
                .providerOrder("cohere")
                .providerOnly("cohere")
                .providerIgnore("deepinfra")
                .requireParameters(true)
                .allowFallbacks(false)
                .build()
                .getBody());

        assertThat(set.getJSONObject("provider").getJSONArray("order").toList())
                .containsExactly("cohere");
        assertThat(set.getJSONObject("provider").getJSONArray("only").toList())
                .containsExactly("cohere");
        assertThat(set.getJSONObject("provider").getJSONArray("ignore").toList())
                .containsExactly("deepinfra");
        assertThat(set.getJSONObject("provider").getBoolean("require_parameters")).isTrue();
        assertThat(set.getJSONObject("provider").getBoolean("allow_fallbacks")).isFalse();
    }

    @Test
    void eachProviderOptionAloneTriggersTheProviderObject() {
        String[][] cases = {
                {"order", "providerOrder", "cohere"},
                {"only", "providerOnly", "cohere"},
                {"ignore", "providerIgnore", "deepinfra"}
        };
        for (String[] jsonKeyAndValue : cases) {
            OpenRouterRerankRequest.Builder builder = new OpenRouterRerankRequest.Builder(client())
                    .model("cohere/rerank-v3.5")
                    .query("q")
                    .addDocument("doc");
            switch (jsonKeyAndValue[1]) {
                case "providerOrder" -> builder.providerOrder(jsonKeyAndValue[2]);
                case "providerOnly" -> builder.providerOnly(jsonKeyAndValue[2]);
                case "providerIgnore" -> builder.providerIgnore(jsonKeyAndValue[2]);
                default -> throw new IllegalStateException(jsonKeyAndValue[1]);
            }
            JSONObject body = new JSONObject(builder.build().getBody());

            assertThat(body.has("provider")).isTrue();
            assertThat(body.getJSONObject("provider").toMap()).containsOnlyKeys(jsonKeyAndValue[0]);
        }

        JSONObject requireParameters = new JSONObject(new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument("doc")
                .requireParameters(true)
                .build()
                .getBody());
        assertThat(requireParameters.getJSONObject("provider").toMap())
                .containsOnlyKeys("require_parameters");

        JSONObject allowFallbacks = new JSONObject(new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument("doc")
                .allowFallbacks(false)
                .build()
                .getBody());
        assertThat(allowFallbacks.getJSONObject("provider").toMap())
                .containsOnlyKeys("allow_fallbacks");
    }

    @Test
    void providerOrderWithNoValuesEmitsNoProviderObject() {
        JSONObject body = new JSONObject(new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument("doc")
                .providerOrder()
                .build()
                .getBody());

        assertThat(body.has("provider")).isFalse();
    }

    @Test
    void buildRejectsMissingModelQueryAndDocuments() {
        assertThatThrownBy(() -> new OpenRouterRerankRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument("doc")
                .topN(0)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requestAccessorsReflectTheBuilderInput() {
        OpenRouterRerankRequest request = new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("capital of France?")
                .addDocument("plain")
                .addDocument("text", "image.png")
                .build();

        assertThat(request.model()).isEqualTo("cohere/rerank-v3.5");
        assertThat(request.query()).isEqualTo("capital of France?");
        assertThat(request.documents()).hasSize(2);
        assertThat(request.documents().get(0).text()).isEqualTo("plain");
        assertThat(request.documents().get(0).image()).isNull();
        assertThat(request.documents().get(1).text()).isEqualTo("text");
        assertThat(request.documents().get(1).image()).isEqualTo("image.png");
    }

    @Test
    void accessorsAreSurfaced() {
        OpenRouterRerankResponse response = responseOf("""
                {
                  "id": "gen-rerank-123",
                  "model": "cohere/rerank-v3.5",
                  "provider": "Cohere",
                  "results": [
                    {"index": 1, "relevance_score": 0.98, "document": {"text": "Berlin is the capital of Germany."}},
                    {"index": 0, "relevance_score": 0.87, "document": {"text": "Paris is the capital of France.", "image": "https://example.com/paris.png"}}
                  ],
                  "usage": {"total_tokens": 150, "search_units": 1, "cost": 0.001}
                }
                """);

        assertThat(response.id()).isEqualTo("gen-rerank-123");
        assertThat(response.model()).isEqualTo("cohere/rerank-v3.5");
        assertThat(response.provider()).isEqualTo("Cohere");
        assertThat(response.usage().toMap()).containsOnlyKeys("total_tokens", "search_units", "cost");
        assertThat(response.totalTokens()).isEqualTo(150L);
        assertThat(response.searchUnits()).isEqualTo(1L);
        assertThat(response.cost()).isEqualTo(0.001);

        assertThat(response.results()).hasSize(2);
        OpenRouterRerankResult first = response.results().get(0);
        assertThat(first.index()).isEqualTo(1);
        assertThat(first.relevanceScore()).isEqualTo(0.98);
        assertThat(first.documentText()).isEqualTo("Berlin is the capital of Germany.");
        assertThat(first.documentImage()).isNull();
        OpenRouterRerankResult second = response.results().get(1);
        assertThat(second.documentText()).isEqualTo("Paris is the capital of France.");
        assertThat(second.documentImage()).isEqualTo("https://example.com/paris.png");
    }

    @Test
    void accessorsReturnNullWhenAbsent() {
        OpenRouterRerankResponse response = responseOf("{}");

        assertThat(response.id()).isNull();
        assertThat(response.model()).isNull();
        assertThat(response.provider()).isNull();
        assertThat(response.usage()).isNull();
        assertThat(response.totalTokens()).isNull();
        assertThat(response.searchUnits()).isNull();
        assertThat(response.cost()).isNull();
        assertThat(response.results()).isEmpty();
    }

    @Test
    void accessorsSwallowMalformedBodies() {
        OpenRouterRerankResponse response = responseOf(
                "{\"results\": \"not-an-array\", \"usage\": \"not-an-object\", \"id\": 42}");

        assertThat(response.results()).isEmpty();
        assertThat(response.usage()).isNull();
        assertThat(response.totalTokens()).isNull();
        assertThat(response.searchUnits()).isNull();
        assertThat(response.cost()).isNull();
        assertThat(response.id()).isEqualTo("42");
    }

    @Test
    void resultAccessorsSwallowMalformedEntries() {
        OpenRouterRerankResponse response = responseOf("""
                {"results": [
                  {"index": "no-int", "relevance_score": "no-number", "document": "not-an-object"},
                  {"index": 2, "relevance_score": 0.5}
                ]}
                """);

        OpenRouterRerankResult malformed = response.results().get(0);
        assertThat(malformed.index()).isNull();
        assertThat(malformed.relevanceScore()).isNull();
        assertThat(malformed.document()).isNull();
        assertThat(malformed.documentText()).isNull();
        assertThat(malformed.documentImage()).isNull();

        OpenRouterRerankResult withoutDocument = response.results().get(1);
        assertThat(withoutDocument.index()).isEqualTo(2);
        assertThat(withoutDocument.relevanceScore()).isEqualTo(0.5);
        assertThat(withoutDocument.documentText()).isNull();
    }

    @Test
    void resultsSkipNonObjectEntries() {
        OpenRouterRerankResponse response = responseOf(
                "{\"results\": [\"not-an-object\", {\"index\": 0, \"relevance_score\": 0.5}]}");

        assertThat(response.results()).hasSize(1);
        assertThat(response.results().get(0).index()).isEqualTo(0);
    }

    @Test
    void resultJsonAccessorReturnsTheRawEntry() {
        OpenRouterRerankResponse response = responseOf(
                "{\"results\": [{\"index\": 0, \"relevance_score\": 0.5}]}");

        assertThat(response.results().get(0).json().toMap())
                .containsOnlyKeys("index", "relevance_score");
    }

    private OpenRouterRerankResponse responseOf(String json) {
        OpenRouterRerankRequest request = new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument("doc")
                .build();
        return request.createResponse(json);
    }
}
