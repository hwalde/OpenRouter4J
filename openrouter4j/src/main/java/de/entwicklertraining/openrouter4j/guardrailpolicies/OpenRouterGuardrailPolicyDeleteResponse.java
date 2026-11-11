package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The answer of the policy delete routes (spec
 * {@code DeleteGuardrailPolicyResponse}): {@code data} is the boolean
 * confirmation that the policy was deleted.
 *
 * <p>The response tolerates empty and {@code null} bodies - {@link #deleted()}
 * answers {@code null} then.
 *
 * @param <T> the request type
 */
public final class OpenRouterGuardrailPolicyDeleteResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {

    OpenRouterGuardrailPolicyDeleteResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - whether the policy was deleted.
     *
     * @return the confirmation, or {@code null} when absent or not a boolean
     */
    public Boolean deleted() {
        Object value = json.opt("data");
        return value instanceof Boolean b ? b : null;
    }
}
