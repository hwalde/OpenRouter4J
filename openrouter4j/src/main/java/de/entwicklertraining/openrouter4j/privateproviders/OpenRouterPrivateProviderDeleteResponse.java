package de.entwicklertraining.openrouter4j.privateproviders;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE /private-providers/{slug}: the deletion confirmation
 * ({@code {"data": {"slug": "..."}}}).
 *
 * <p>The response tolerates an empty body - {@link
 * OpenRouterPrivateProviderDeleteRequest#createResponse(String)} parses a
 * blank or {@code null} body as an empty object, so every accessor answers
 * {@code null} instead of throwing.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterPrivateProviderDeleteResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPrivateProviderDeleteResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data.slug} - the slug of the deleted provider.
     *
     * @return the deleted slug, or {@code null} when absent, not a string or
     *         the body was empty
     */
    public String slug() {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data == null) {
                return null;
            }
            Object value = data.opt("slug");
            return value instanceof String text ? text : null;
        } catch (Exception e) {
            return null;
        }
    }
}
