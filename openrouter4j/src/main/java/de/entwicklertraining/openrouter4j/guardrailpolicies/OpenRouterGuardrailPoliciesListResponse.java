package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The typed list answer of the budget / model-availability / moderation
 * policy list routes: {@code data[]} rows of
 * {@link OpenRouterGuardrailPolicySummary} plus {@code total_count} (the
 * count before pagination; page with {@code offset}). Generic over the
 * request type because the three list routes share the shape.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/-empty
 * convention; the response tolerates empty and {@code null} bodies.
 *
 * @param <T> the request type
 */
public final class OpenRouterGuardrailPoliciesListResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {

    OpenRouterGuardrailPoliciesListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - the policies as typed views, empty when
     * absent (non-object entries dropped while good ones survive).
     */
    public List<OpenRouterGuardrailPolicySummary> items() {
        List<OpenRouterGuardrailPolicySummary> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("data");
            if (arr == null) {
                return result;
            }
            for (int i = 0; i < arr.length(); i++) {
                JSONObject row = arr.optJSONObject(i);
                if (row != null) {
                    result.add(new OpenRouterGuardrailPolicySummary(row));
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code total_count} - the total number of policies.
     *
     * @return the count, or {@code null} when absent or not a number
     */
    public Integer totalCount() {
        Object value = json.opt("total_count");
        return value instanceof Number n ? n.intValue() : null;
    }
}
