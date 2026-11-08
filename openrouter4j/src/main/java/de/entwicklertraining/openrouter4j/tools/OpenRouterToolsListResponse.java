package de.entwicklertraining.openrouter4j.tools;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /tools (schema {@code ListToolsResponse}): one page of the
 * server-tool catalog plus the total count and the pagination link.
 *
 * <p>Pagination is offset-based: {@link #totalCount()} counts every tool the
 * request matches, and {@link #nextPageUrl()} is the URL of the next page or
 * {@code null} on the last page. When both {@code offset} and {@code limit}
 * are omitted the API returns the full list in one page.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterToolsListResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterToolsListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the catalog rows of this page.
     * Non-object entries are dropped.
     *
     * @return the catalog entries of this page, empty when absent
     */
    public List<OpenRouterServerToolCatalogEntry> items() {
        List<OpenRouterServerToolCatalogEntry> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterServerToolCatalogEntry(row));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code total_count} - the total number of tools matching
     * the request, before pagination.
     *
     * @return the count, or {@code null} when absent or not a number
     */
    public Long totalCount() {
        Object value = json.opt("total_count");
        return value instanceof Number number ? number.longValue() : null;
    }

    /**
     * JSON path: {@code links.next} - the URL of the next page of results,
     * or {@code null} on the last page. The API returns it as a relative
     * path (e.g. {@code /api/v1/tools?offset=50&limit=50}); page by raising
     * {@code offset} on the next request.
     *
     * @return the next-page URL, or {@code null} when absent, JSON
     *         {@code null} or not a string
     */
    public String nextPageUrl() {
        JSONObject links = json.optJSONObject("links");
        if (links == null) {
            return null;
        }
        if (!links.has("next") || links.isNull("next")) {
            return null;
        }
        Object value = links.opt("next");
        return value instanceof String text ? text : null;
    }
}