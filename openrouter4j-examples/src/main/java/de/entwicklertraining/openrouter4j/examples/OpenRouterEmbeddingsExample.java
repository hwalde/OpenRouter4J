package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbedding;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsContentPart;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsInputItem;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsModelsResponse;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsResponse;
import org.json.JSONObject;

import java.util.List;

/**
 * Demonstrates POST /embeddings - creating embedding vectors for one or more
 * inputs - and GET /embeddings/models, the embedding-model listing.
 *
 * <p>Batch requests return one embedding per input, matched by the
 * {@code index} of each entry. With {@code encodingFormat("base64")} the
 * vector travels base64-encoded and is decoded by
 * {@link OpenRouterEmbedding#vectorFromBase64()}.
 *
 * <p>The {@code input} union carries five wire forms (string, string array,
 * token-id array, token-id array batch, multimodal item objects) - the last
 * form set wins, exactly one {@code input} value is emitted.
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

        // Multimodal input - the item-object form of `input`: one item per
        // input to embed, each carrying content parts (text / image_url typed;
        // input_audio / input_video / input_file via the verbatim hatch).
        // Trap: only embedding models that declare the matching input
        // modality accept media parts - check client.embeddingsModels() /
        // OpenRouterModel.inputModalities(); a text-only embedding model
        // answers HTTP 400.
        OpenRouterEmbeddingsResponse multimodal = client.embeddings()
                .model("google/embeddinggemma-300m")
                .addInput(OpenRouterEmbeddingsInputItem.of(
                        OpenRouterEmbeddingsContentPart.imageUrl("https://example.com/photo.png"),
                        OpenRouterEmbeddingsContentPart.text("what is shown in this image?")))
                .addInput(OpenRouterEmbeddingsInputItem.text("a plain text item alongside"))
                .execute();
        System.out.println("Multimodal embeddings: " + multimodal.embeddings().size());

        // Models that embed pre-tokenized input take token ids instead of
        // text: one token array ...
        OpenRouterEmbeddingsResponse tokens = client.embeddings()
                .model("openai/text-embedding-3-small")
                .inputTokenArray(15496, 11, 262, 5764)
                .execute();
        System.out.println("Token-array embedding dimensions: "
                + tokens.embeddings().get(0).vectorOrDecoded().size());

        // ... or a batch of token arrays (one embedding per row, matched by
        // index, exactly like the string batch).
        OpenRouterEmbeddingsResponse tokenBatch = client.embeddings()
                .model("openai/text-embedding-3-small")
                .inputTokens(new int[]{15496, 11}, new int[]{262, 5764})
                .execute();
        System.out.println("Token-batch embeddings: " + tokenBatch.embeddings().size());

        // The value-class hatch covers part shapes without a typed factory
        // (e.g. input_video) inside an otherwise typed item.
        OpenRouterEmbeddingsResponse videoItem = client.embeddings()
                .model("google/embeddinggemma-300m")
                .addInput(OpenRouterEmbeddingsInputItem.of(
                        OpenRouterEmbeddingsContentPart.text("summarize this clip"),
                        OpenRouterEmbeddingsContentPart.raw(new JSONObject("""
                                {"type": "input_video",
                                 "input_video": {"data": "data:video/mp4;base64,AAAAGGZ0eXBtcDQy", "format": "mp4"}}
                                """))))
                .execute();
        System.out.println("Video-item embedding dimensions: "
                + videoItem.embeddings().get(0).vectorOrDecoded().size());

        // Everything the schema declares that has no typed factory goes
        // through the verbatim item hatch - e.g. an input_audio part.
        OpenRouterEmbeddingsResponse audioItem = client.embeddings()
                .model("google/embeddinggemma-300m")
                .addInputItem(new JSONObject("""
                        {"content": [
                          {"type": "input_audio",
                           "input_audio": {"data": "data:audio/wav;base64,UklGRiQAAABXQVZF", "format": "wav"}}
                        ]}
                        """))
                .execute();
        System.out.println("Audio-item embedding dimensions: "
                + audioItem.embeddings().get(0).vectorOrDecoded().size());
    }
}
