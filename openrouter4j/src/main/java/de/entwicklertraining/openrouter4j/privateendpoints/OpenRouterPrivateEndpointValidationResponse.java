package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /private-endpoints/{id}/validate (schema
 * {@code PrivateEndpointValidationResponse}, {@code {"data": {...}}}):
 * the checks of the live validation run.
 *
 * <p>Trap: a run with failed checks answers HTTP 200 with
 * {@code passed: false} - the verdict lives in the body, not in the status
 * code.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterPrivateEndpointValidationResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPrivateEndpointValidationResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the validation run.
     *
     * @return the validation as a typed view, or {@code null} when
     *         {@code data} is absent or not an object
     */
    public OpenRouterPrivateEndpointValidation data() {
        try {
            JSONObject data = json.optJSONObject("data");
            return data != null ? new OpenRouterPrivateEndpointValidation(data) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
