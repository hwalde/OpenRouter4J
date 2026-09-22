package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The shared response of the intern lifecycle actions
 * (DELETE /interns/{internId} - 202 {@code deleting},
 * POST /interns/{internId}/provision - 202 {@code provisioning},
 * POST /interns/{internId}/suspend - 200 {@code suspended}): a single
 * boolean confirmation flag.
 *
 * <p>Follows the swallow-and-return-null convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterInternLifecycleResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterInternLifecycleResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code deleting} - {@code true} when the teardown was
     * accepted (DELETE /interns/{internId}).
     *
     * @return the flag, or {@code null} when absent
     */
    public Boolean deleting() {
        return optBoolean("deleting");
    }

    /**
     * JSON path: {@code provisioning} - {@code true} when provisioning
     * (first boot or resume after suspension) was accepted
     * (POST /interns/{internId}/provision).
     *
     * @return the flag, or {@code null} when absent
     */
    public Boolean provisioning() {
        return optBoolean("provisioning");
    }

    /**
     * JSON path: {@code suspended} - {@code true} when the runtime was
     * stopped (POST /interns/{internId}/suspend; disk and configuration are
     * kept).
     *
     * @return the flag, or {@code null} when absent
     */
    public Boolean suspended() {
        return optBoolean("suspended");
    }

    private Boolean optBoolean(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        return json.optBoolean(key);
    }
}
