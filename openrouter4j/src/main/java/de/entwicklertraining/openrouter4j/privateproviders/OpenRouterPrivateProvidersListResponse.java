package de.entwicklertraining.openrouter4j.privateproviders;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /private-providers: the organization's private providers
 * wrapped in {@code data} ({@code {"data": [PrivateProvider]}}). The list
 * carries no pagination and no total count.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterPrivateProvidersListResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPrivateProvidersListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the private provider rows. Non-object entries
     * are dropped.
     *
     * @return the providers, empty when absent
     */
    public List<OpenRouterPrivateProvider> items() {
        List<OpenRouterPrivateProvider> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterPrivateProvider(row));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }
}
