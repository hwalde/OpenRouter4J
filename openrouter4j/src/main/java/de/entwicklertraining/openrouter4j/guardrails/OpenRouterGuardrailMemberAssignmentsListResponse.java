package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /guardrails/assignments/members and GET
 * /guardrails/{id}/assignments/members: the member assignments (global, or of
 * one guardrail).
 *
 * <p>Follows the swallow-and-return-empty convention.
 *
 * @param <T> the concrete request type this response belongs to
 */
public final class OpenRouterGuardrailMemberAssignmentsListResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {


    /**
     * Creates the typed response.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    OpenRouterGuardrailMemberAssignmentsListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data[]} - the member assignments as typed views, empty when
 * absent.
 */
    public List<OpenRouterGuardrailMemberAssignment> items() {
        List<OpenRouterGuardrailMemberAssignment> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterGuardrailMemberAssignment(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code total_count} - total number of member assignments.
 */
    public Integer totalCount() {
        if (!json.has("total_count") || json.isNull("total_count")) {
            return null;
        }
        return (int) json.optLong("total_count");
    }
}
