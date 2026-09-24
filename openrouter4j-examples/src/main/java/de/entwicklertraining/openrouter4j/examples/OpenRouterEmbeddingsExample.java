package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbedding;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsModelsResponse;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsResponse;

import java.util.List;

/**
 * Demonstrates POST /embeddings - creating embedding vectors for one or more
 * inputs - and GET /embeddings/models, the embedding-model listing.
 *
 * <p>Batch requests return one embedding per input, matched by the
 * {@code index} of each entry. With {@code encodingFormat("base64")} the
 * vector travels base64-encoded and is decoded by
 * {@link OpenRouterEmbedding#vectorFromBase64()}.
 */
public class OpenRouterEmbeddingsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Which embedding models exist? (GET /embeddings/models)
        OpenRouterEmbeddingsModelsResponse catalog = client.embeddingsModels().limit(5).execute();
        catalog.models().forEach(model ->
                System.out.println("Embedding model: " + model.id()));

        // One embedding per input, matched by index (POST /embeddings)
        OpenRouterEmbeddingsResponse response = client.embeddings()
                .model("openai/text-embedding-3-small")
                .inputs(List.of(
                        "Paris is the capital of France.",
                        "Berlin is the capital of Germany."))
                .inputType("search_document")
                // Broadcast trace metadata (the embeddings request also has user(...)).
                .trace(OpenRouterTraceConfig.builder().traceId("embedding-jobs").build())
                // Observability grouping key - never sent to the provider.
                .sessionId("embedding-session-42")
                .execute();

        System.out.println("Model:            " + response.model());
        System.out.println("Prompt tokens:    " + response.promptTokens());
        for (OpenRouterEmbedding embedding : response.embeddings()) {
            List<Float> vector = embedding.vectorOrDecoded();
            System.out.println("index=" + embedding.index()
                    + " dimensions=" + (vector == null ? 0 : vector.size())
                    + " first=" + (vector == null || vector.isEmpty() ? null : vector.get(0)));
        }

        // Full provider preferences (the same 13-field ProviderPreferences
        // object the chat completions request types): data collection, ZDR,
        // quantizations, price caps, latency/throughput preferences and sort.
        OpenRouterEmbeddingsResponse routed = client.embeddings()
                .model("openai/text-embedding-3-small")
                .input("hello")
                .dataCollection("deny")
                .zdr(true)
                .quantizations("int4", "fp8")
                .maxPrice("0.5", "1.0")
                .preferredMaxLatency(2.0)
                .preferredMinThroughput(100.0)
                .sortBy("price", "none")
                .enforceDistillableText(true)
                .execute();
        System.out.println("Routed embedding dimensions: "
                + routed.embeddings().get(0).vectorOrDecoded().size());
    }
}
