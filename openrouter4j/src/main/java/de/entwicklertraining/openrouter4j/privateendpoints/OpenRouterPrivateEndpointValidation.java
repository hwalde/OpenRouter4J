package de.entwicklertraining.openrouter4j.privateendpoints;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one validation run of a draft private endpoint (schema
 * {@code PrivateEndpointValidation}) - the {@code data} object of
 * POST /private-endpoints/{id}/validate.
 *
 * <p>Trap: a run with failed checks is <b>not</b> an error - the route
 * answers HTTP 200 with {@code passed: false} and the failed checks in
 * {@link #checks()}. {@link #parameters()} (the request parameters probed
 * after the checks passed) is absent on a failed run and for providers that
 * run no parameter probes, and empty when every probe group was already
 * proven; supported probes are recorded in the endpoint's
 * {@code tested_parameters}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterPrivateEndpointValidation {

    private final JSONObject json;

    OpenRouterPrivateEndpointValidation(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw validation object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code passed} - whether every check passed. A passing
     * validation is required before activation.
     *
     * @return the verdict, or {@code null} when absent or not a boolean
     */
    public Boolean passed() {
        Object value = json.opt("passed");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code checks} - the individual checks of the run. Non-object
     * entries are dropped.
     *
     * @return the checks, empty when absent or malformed
     */
    public List<OpenRouterPrivateEndpointCheck> checks() {
        List<OpenRouterPrivateEndpointCheck> result = new ArrayList<>();
        try {
            JSONArray checks = json.optJSONArray("checks");
            if (checks != null) {
                for (int i = 0; i < checks.length(); i++) {
                    JSONObject row = checks.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterPrivateEndpointCheck(row));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }

    /**
     * JSON path: {@code parameters} - request parameters probed after the
     * checks passed, each with whether the upstream honored it. Supported
     * ones are recorded in {@code tested_parameters}. Absent on a failed run
     * and for providers that run no parameter probes; empty when every probe
     * group was already proven. Non-object entries are dropped.
     *
     * @return the parameter checks, empty when absent or malformed
     */
    public List<OpenRouterPrivateEndpointParameterCheck> parameters() {
        List<OpenRouterPrivateEndpointParameterCheck> result = new ArrayList<>();
        try {
            JSONArray parameters = json.optJSONArray("parameters");
            if (parameters != null) {
                for (int i = 0; i < parameters.length(); i++) {
                    JSONObject row = parameters.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterPrivateEndpointParameterCheck(row));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }
}
