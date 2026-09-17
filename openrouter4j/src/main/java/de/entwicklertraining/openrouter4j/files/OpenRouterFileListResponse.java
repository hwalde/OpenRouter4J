package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /files: a page of file documents plus the pagination
 * fields.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention.
 *
 * @param <T> the concrete request type this response belongs to
 */
public class OpenRouterFileListResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates a typed list response. Public so that other endpoint packages
     * can reuse this file listing shape.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterFileListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - the file documents of this page.
     *
     * @return every file of the response as {@link OpenRouterFile} views,
     *         empty when {@code data} is absent or not an array
     */
    public List<OpenRouterFile> files() {
        List<OpenRouterFile> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterFile(entry));
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
     * JSON path: {@code last_id} - the id of the last file of this page.
     *
     * @return the value, or {@code null} when absent
     */
    public String lastId() {
        return json.optString("last_id", null);
    }

    /**
     * JSON path: {@code has_more} - whether further pages exist.
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
     * JSON path: {@code cursor} - the OpenRouter-style pagination cursor to
     * pass to {@link OpenRouterFileListRequest.Builder#cursor(String)}.
     *
     * @return the value, or {@code null} when absent
     */
    public String cursor() {
        return json.optString("cursor", null);
    }

    /**
     * JSON path: {@code _shape} - the negotiated response shape
     * ({@code "openrouter"}, {@code "openai"} or {@code "anthropic"}).
     *
     * @return the value, or {@code null} when absent
     */
    public String shape() {
        return json.optString("_shape", null);
    }
}
