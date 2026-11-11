package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The answer of the typed policy create and update routes: one
 * {@link OpenRouterGuardrailPolicy} under {@code data} (the spec's
 * {@code BudgetGuardrailPolicy} / {@code ModelAvailabilityGuardrailPolicy} /
 * {@code ModerationGuardrailPolicy} - {@code id}, {@code name},
 * {@code policy_type} and {@code config}).
 *
 * <p>The response tolerates empty and {@code null} bodies - {@link #data()}
 * answers {@code null} then.
 *
 * @param <T> the request type
 */
public final class OpenRouterGuardrailPolicyResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {

    OpenRouterGuardrailPolicyResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the policy.
     *
     * @return the typed view, or {@code null} when absent or not an object
     */
    public OpenRouterGuardrailPolicy data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterGuardrailPolicy(data) : null;
    }
}
