package de.entwicklertraining.openrouter4j.providers;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /providers: the providers integrated on OpenRouter.
 *
 * <p>Follows the swallow-and-return-empty convention.
 */
public final class OpenRouterProvidersResponse extends OpenRouterResponse<OpenRouterProvidersRequest> {

    OpenRouterProvidersResponse(JSONObject json, OpenRouterProvidersRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - the providers.
     *
     * @return the providers as typed views, empty when absent
     */
    public List<OpenRouterProvider> items() {
        List<OpenRouterProvider> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterProvider(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}
