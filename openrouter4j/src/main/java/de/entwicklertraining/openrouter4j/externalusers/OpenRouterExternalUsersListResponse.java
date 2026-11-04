package de.entwicklertraining.openrouter4j.externalusers;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /external-users (schema
 * {@code ListExternalUsersResponse}): one page of the organization's
 * external-user registrations plus the total count.
 *
 * <p>Inactive registrations are excluded unless the request set
 * {@code include_inactive}. {@link #totalCount()} counts the registrations
 * matching the filters before pagination - page with {@code offset}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterExternalUsersListResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterExternalUsersListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the registration rows of this page.
     * Non-object entries are dropped.
     *
     * @return the registrations of this page, empty when absent
     */
    public List<OpenRouterExternalUser> items() {
        List<OpenRouterExternalUser> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterExternalUser(row));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }

    /**
     * JSON path: {@code total_count} - the number of registrations matching
     * the filters before pagination.
     *
     * @return the count, or {@code null} when absent or not a number
     */
    public Long totalCount() {
        Object value = json.opt("total_count");
        return value instanceof Number number ? number.longValue() : null;
    }
}
