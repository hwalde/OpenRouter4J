package de.entwicklertraining.openrouter4j.tools;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one entry of {@code native_support.models[]} on the
 * server-tool details (GET /tools/{name}, schema
 * {@code ServerToolNativeModel}): one model that runs this tool natively
 * on its provider's endpoints.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterServerToolNativeModel {

    private final JSONObject json;

    OpenRouterServerToolNativeModel(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw model row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code slug} - the model permaslug (e.g.
     * {@code anthropic/claude-4.5-sonnet}).
     *
     * @return the slug, or {@code null} when absent or not a string
     */
    public String slug() {
        Object value = json.opt("slug");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code providers[]} - the provider names whose endpoints of
     * this model run the tool natively (e.g. {@code Anthropic},
     * {@code Google}). Non-string entries are dropped.
     *
     * @return the provider names, empty when absent
     */
    public List<String> providers() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray providers = json.optJSONArray("providers");
            if (providers != null) {
                for (int i = 0; i < providers.length(); i++) {
                    Object value = providers.opt(i);
                    if (value instanceof String text) {
                        result.add(text);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}