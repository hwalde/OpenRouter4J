package de.entwicklertraining.openrouter4j.models;

import org.json.JSONObject;

/**
 * A typed view of one endpoint entry of GET
 * /models/{author}/{slug}/endpoints ({@code data.endpoints[]}).
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for the fields without a typed accessor (pricing,
 * quantization, supported parameters, tags, status and whatever OpenRouter
 * adds later).
 */
public final class OpenRouterModelEndpoint {

    private final JSONObject json;

    OpenRouterModelEndpoint(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw endpoint object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code provider} - name of the hosting provider.
     *
     * @return the value, or {@code null} when absent
     */
    public String provider() {
        return json.optString("provider", null);
    }

    /**
     * JSON path: {@code model} - the model id served by this endpoint.
     *
     * @return the value, or {@code null} when absent
     */
    public String model() {
        return json.optString("model", null);
    }

    /**
     * JSON path: {@code selected} - whether this endpoint is currently the
     * selected default for the model.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean selected() {
        if (!json.has("selected") || json.isNull("selected")) {
            return null;
        }
        return json.optBoolean("selected");
    }
}
