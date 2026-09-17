package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /containers/{container_id}/files: the files of one
 * code-execution container in lexicographic {@code path} order, with the
 * pagination envelope ({@code first_id}, {@code last_id}, {@code has_more}).
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention.
 *
 * @param <T> the concrete request type this response belongs to
 */
public class OpenRouterContainerFileListResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {

    /**
     * Creates a typed list response.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterContainerFileListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - every container file of the current page,
     * in lexicographic path order.
     *
     * @return the files as {@link OpenRouterContainerFile} views, empty when
     *         {@code data} is absent or not an array, never {@code null}
     */
    public List<OpenRouterContainerFile> files() {
        List<OpenRouterContainerFile> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterContainerFile(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code first_id} - the id of the first file of this page.
     *
     * @return the value, or {@code null} when absent
     */
    public String firstId() {
        return json.optString("first_id", null);
    }

    /**
     * JSON path: {@code last_id} - the id of the last file of this page;
     * pass it as {@code after} to fetch the next page.
     *
     * @return the value, or {@code null} when absent
     */
    public String lastId() {
        return json.optString("last_id", null);
    }

    /**
     * JSON path: {@code has_more} - {@code true} when another page can be
     * fetched by passing {@code after=lastId()} to the next request.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean hasMore() {
        if (!json.has("has_more") || json.isNull("has_more")) {
            return null;
        }
        return json.optBoolean("has_more");
    }

    /**
     * JSON path: {@code object} - always {@code "list"} for a listing.
     *
     * @return the value, or {@code null} when absent
     */
    public String objectType() {
        return json.optString("object", null);
    }
}
