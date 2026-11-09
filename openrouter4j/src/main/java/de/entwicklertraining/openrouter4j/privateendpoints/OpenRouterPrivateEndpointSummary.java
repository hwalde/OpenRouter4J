package de.entwicklertraining.openrouter4j.privateendpoints;

import org.json.JSONObject;

/**
 * A typed view of one private-endpoint list row (schema
 * {@code PrivateEndpointSummary}): identity, lifecycle status, the model it
 * serves and its provider display name.
 *
 * <p>Not final on purpose: {@link OpenRouterManagedPrivateEndpoint} extends
 * this view with the three fields the managed routes add. The full endpoint
 * view ({@link OpenRouterPrivateEndpoint}) is separate - it carries no
 * {@code created_at} and adds upstream configuration, pricing and settings.
 *
 * <p>{@code status} is exposed as {@link String}: {@code draft}, {@code active}
 * and {@code disabled} are the documented spellings of
 * {@code PrivateEndpointStatus} (unknown values pass through verbatim - the
 * API rejects undocumented ones, the library never does).
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public class OpenRouterPrivateEndpointSummary {

    private final JSONObject json;

    OpenRouterPrivateEndpointSummary(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw summary object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code id} - stable identifier of the private endpoint
     * (UUID).
     *
     * @return the id, or {@code null} when absent or not a string
     */
    public String id() {
        Object value = json.opt("id");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code status} - lifecycle state: {@code draft} (not
     * routable until validated and activated), {@code active} or
     * {@code disabled} (activated but temporarily not routable). Unknown
     * spellings pass through verbatim.
     *
     * @return the status, or {@code null} when absent or not a string
     */
    public String status() {
        Object value = json.opt("status");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code model_permaslug} - permanent slug of the model this
     * endpoint serves.
     *
     * @return the permaslug, or {@code null} when absent or not a string
     */
    public String modelPermaslug() {
        Object value = json.opt("model_permaslug");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code model_slug} - public model slug, or {@code null}
     * when the model is no longer listed (or when the field is absent or not
     * a string).
     *
     * @return the model slug, or {@code null}
     */
    public String modelSlug() {
        Object value = json.opt("model_slug");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code model_name} - display name of the model, or
     * {@code null} when the model is no longer listed (or when the field is
     * absent or not a string).
     *
     * @return the model display name, or {@code null}
     */
    public String modelName() {
        Object value = json.opt("model_name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code provider_name} - display name of the upstream
     * provider.
     *
     * @return the provider display name, or {@code null} when absent or not
     *         a string
     */
    public String providerName() {
        Object value = json.opt("provider_name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code created_at} - ISO timestamp of when the endpoint was
     * created. Only the summary shape carries this field; the full endpoint
     * view ({@link OpenRouterPrivateEndpoint}) does not.
     *
     * @return the creation timestamp, or {@code null} when absent or not a
     *         string
     */
    public String createdAt() {
        Object value = json.opt("created_at");
        return value instanceof String text ? text : null;
    }
}
