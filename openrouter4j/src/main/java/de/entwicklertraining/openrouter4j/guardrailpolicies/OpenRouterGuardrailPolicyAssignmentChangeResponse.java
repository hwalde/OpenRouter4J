package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The answer of the policy assignment change routes (spec
 * {@code ChangeGuardrailPolicyAssignmentResponse}): {@code data} is the
 * boolean confirmation that the assignment changed.
 *
 * <p>The response tolerates empty and {@code null} bodies - {@link #changed()}
 * answers {@code null} then.
 *
 * @param <T> the request type
 */
public final class OpenRouterGuardrailPolicyAssignmentChangeResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {

    OpenRouterGuardrailPolicyAssignmentChangeResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - whether the assignment changed.
     *
     * @return the confirmation, or {@code null} when absent or not a boolean
     */
    public Boolean changed() {
        Object value = json.opt("data");
        return value instanceof Boolean b ? b : null;
    }
}
