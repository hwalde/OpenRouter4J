package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The answer of the policy assignment read routes: one
 * {@link OpenRouterGuardrailPolicyAssignmentState} under {@code data} - what
 * to pass as {@code expected_assignment} on the matching change request.
 *
 * <p>The response tolerates empty and {@code null} bodies - {@link #data()}
 * answers {@code null} then.
 *
 * @param <T> the request type
 */
public final class OpenRouterGuardrailPolicyAssignmentResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {

    OpenRouterGuardrailPolicyAssignmentResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the assignment state.
     *
     * @return the typed view, or {@code null} when absent or not an object
     */
    public OpenRouterGuardrailPolicyAssignmentState data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterGuardrailPolicyAssignmentState(data) : null;
    }
}
