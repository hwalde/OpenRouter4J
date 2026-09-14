package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
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
                .execute();

        System.out.println("Model:            " + response.model());
        System.out.println("Prompt tokens:    " + response.promptTokens());
        for (OpenRouterEmbedding embedding : response.embeddings()) {
            List<Float> vector = embedding.vectorOrDecoded();
            System.out.println("index=" + embedding.index()
                    + " dimensions=" + (vector == null ? 0 : vector.size())
                    + " first=" + (vector == null || vector.isEmpty() ? null : vector.get(0)));
        }
    }
}
