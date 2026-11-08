package de.entwicklertraining.openrouter4j.tools;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of the {@code native_support} object of a server-tool catalog
 * entry (schemas {@code ServerToolNativeSupport} / {@code ServerToolDetails}):
 * the endpoints whose provider runs this tool itself during inference - zero
 * for tools no provider runs natively.
 *
 * <p>{@link #models()} is required only on the details shape (GET
 * /tools/{name}); the list rows of GET /tools carry the two counts alone and
 * {@link #models()} answers empty there.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterServerToolNativeSupport {

    private final JSONObject json;

    OpenRouterServerToolNativeSupport(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw {@code native_support} object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code endpoint_count} - how many endpoints run this tool
     * natively.
     *
     * @return the count, or {@code null} when absent or not a number
     */
    public Long endpointCount() {
        Object value = json.opt("endpoint_count");
        return value instanceof Number number ? number.longValue() : null;
    }

    /**
     * JSON path: {@code model_count} - how many models run this tool
     * natively.
     *
     * @return the count, or {@code null} when absent or not a number
     */
    public Long modelCount() {
        Object value = json.opt("model_count");
        return value instanceof Number number ? number.longValue() : null;
    }

    /**
     * JSON path: {@code models[]} - the models that run this tool natively.
     * Required on the details shape (GET /tools/{name}); absent on the list
     * rows of GET /tools, which answer empty. Non-object entries are
     * dropped.
     *
     * @return the native models, empty when absent
     */
    public List<OpenRouterServerToolNativeModel> models() {
        List<OpenRouterServerToolNativeModel> result = new ArrayList<>();
        try {
            JSONArray models = json.optJSONArray("models");
            if (models != null) {
                for (int i = 0; i < models.length(); i++) {
                    JSONObject row = models.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterServerToolNativeModel(row));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}