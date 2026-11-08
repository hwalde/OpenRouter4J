package de.entwicklertraining.openrouter4j.tools;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /tools/{name} (schema {@code GetToolResponse}): one
 * server tool by canonical name or any accepted alias.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterToolGetResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterToolGetResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the tool, with the models that run it
     * natively.
     *
     * @return the tool view, or {@code null} when absent or not an object
     */
    public OpenRouterServerToolCatalogDetails data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterServerToolCatalogDetails(data) : null;
    }
}