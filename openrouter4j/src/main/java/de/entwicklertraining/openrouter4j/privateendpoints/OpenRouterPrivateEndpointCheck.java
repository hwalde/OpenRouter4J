package de.entwicklertraining.openrouter4j.privateendpoints;

import org.json.JSONObject;

/**
 * A typed view of one check of a validation run (schema
 * {@code PrivateEndpointCheck}): its name, the verdict and, on failure, why
 * it failed and what the deployment answered.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterPrivateEndpointCheck {

    private final JSONObject json;

    OpenRouterPrivateEndpointCheck(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw check object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code name} - the check name (for example
     * {@code auth_ok}).
     *
     * @return the name, or {@code null} when absent or not a string
     */
    public String name() {
        Object value = json.opt("name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code passed} - the check verdict.
     *
     * @return the verdict, or {@code null} when absent or not a boolean
     */
    public Boolean passed() {
        Object value = json.opt("passed");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code reason} - why the check failed ({@code database_error},
     * {@code endpoint_limit_reached}, {@code invalid_base_url},
     * {@code model_not_found}, {@code provider_not_found},
     * {@code endpoint_not_found}, {@code workspace_not_found},
     * {@code no_byok_key}, {@code key_decryption_failed},
     * {@code upstream_error}, {@code invalid_response}, {@code invalid_stream},
     * {@code missing_usage}, {@code model_mismatch}, {@code not_validated},
     * {@code validation_stale}; unknown spellings pass through verbatim).
     *
     * @return the reason, or {@code null} when absent or not a string
     */
    public String reason() {
        Object value = json.opt("reason");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code upstream_message} - error message returned by your
     * deployment when the call failed.
     *
     * @return the message, or {@code null} when absent or not a string
     */
    public String upstreamMessage() {
        Object value = json.opt("upstream_message");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code upstream_status} - HTTP status returned by your
     * deployment when the call failed (400-599).
     *
     * @return the status, or {@code null} when absent or not a number
     */
    public Integer upstreamStatus() {
        Object value = json.opt("upstream_status");
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * JSON path: {@code actual_model} - deprecated and never returned:
     * validation no longer compares the model the upstream reports. Kept as
     * an accessor for tolerance of older payloads.
     *
     * @return always {@code null} on current payloads; {@code null} when
     *         absent or not a string
     */
    public String actualModel() {
        Object value = json.opt("actual_model");
        return value instanceof String text ? text : null;
    }
}
