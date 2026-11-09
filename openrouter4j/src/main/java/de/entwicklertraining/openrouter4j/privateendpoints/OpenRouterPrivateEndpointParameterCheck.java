package de.entwicklertraining.openrouter4j.privateendpoints;

import org.json.JSONObject;

/**
 * A typed view of one parameter probe of a validation run (schema
 * {@code PrivateEndpointParameterCheck}): a request parameter the checks
 * proved the upstream accepts or drops.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterPrivateEndpointParameterCheck {

    private final JSONObject json;

    OpenRouterPrivateEndpointParameterCheck(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw parameter-check object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code parameter} - the probed request parameter (for
     * example {@code response_format}).
     *
     * @return the parameter name, or {@code null} when absent or not a
     *         string
     */
    public String parameter() {
        Object value = json.opt("parameter");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code supported} - whether the upstream honored the
     * parameter. Supported probes are recorded in the endpoint's
     * {@code tested_parameters}.
     *
     * @return the verdict, or {@code null} when absent or not a boolean
     */
    public Boolean supported() {
        Object value = json.opt("supported");
        return value instanceof Boolean flag ? flag : null;
    }
}
