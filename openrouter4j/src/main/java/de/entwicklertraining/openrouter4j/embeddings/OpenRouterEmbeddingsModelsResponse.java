package de.entwicklertraining.openrouter4j.embeddings;

import de.entwicklertraining.openrouter4j.models.OpenRouterModelsListResponse;
import org.json.JSONObject;

/**
 * Response of GET /embeddings/models: the embedding models of the OpenRouter
 * catalog. The body has the same {@code data[]} shape as the general model
 * catalog, so {@link #models()} and {@link #model(String)} are inherited from
 * {@link OpenRouterModelsListResponse}.
 */
public final class OpenRouterEmbeddingsModelsResponse
        extends OpenRouterModelsListResponse<OpenRouterEmbeddingsModelsRequest> {

    /**
     * Creates the typed response.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterEmbeddingsModelsResponse(JSONObject json, OpenRouterEmbeddingsModelsRequest request) {
        super(json, request);
    }
}
