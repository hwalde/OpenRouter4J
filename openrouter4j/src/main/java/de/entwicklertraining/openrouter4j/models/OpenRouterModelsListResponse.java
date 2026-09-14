package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of the model-catalog listing endpoints (GET /models and
 * GET /models/user): the list of catalog entries.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention.
 *
 * @param <T> the concrete request type this response belongs to
 */
public class OpenRouterModelsListResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {

    OpenRouterModelsListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * @return every catalog entry of the response as {@link OpenRouterModel}
     *         views, empty when {@code data} is absent or not an array
     */
    public List<OpenRouterModel> models() {
        List<OpenRouterModel> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterModel(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * Convenience lookup by model id (e.g. {@code openai/gpt-4}).
     *
     * @param id the model id to find
     * @return the matching entry, or {@code null} when no entry has that id
     */
    public OpenRouterModel model(String id) {
        for (OpenRouterModel model : models()) {
            if (id != null && id.equals(model.id())) {
                return model;
            }
        }
        return null;
    }
}
