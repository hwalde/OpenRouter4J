package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The answer of the workspace-wide (account-level) policy create and update
 * routes: one {@link OpenRouterGuardrailAccountPolicy} under {@code data}.
 *
 * <p>The response tolerates empty and {@code null} bodies - {@link #data()}
 * answers {@code null} then.
 *
 * @param <T> the request type
 */
public final class OpenRouterGuardrailAccountPolicyResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {

    OpenRouterGuardrailAccountPolicyResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the policy.
     *
     * @return the typed view, or {@code null} when absent or not an object
     */
    public OpenRouterGuardrailAccountPolicy data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterGuardrailAccountPolicy(data) : null;
    }
}
