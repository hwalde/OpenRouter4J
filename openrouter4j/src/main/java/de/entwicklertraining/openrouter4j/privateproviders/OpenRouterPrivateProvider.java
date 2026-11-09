package de.entwicklertraining.openrouter4j.privateproviders;

import org.json.JSONObject;

/**
 * A typed view of one private provider (schema {@code PrivateProvider}): the
 * immutable identity pair, the editable presentation fields and the creation
 * time.
 *
 * <p>A private provider is a bring-your-own-key container visible only to
 * your organization; private endpoints are created under it (the {@code slug}
 * is the {@code provider_slug} of {@code POST /private-endpoints} and of BYOK
 * keys, and may be used in {@code provider.order}, {@code only} and
 * {@code ignore}).
 *
 * <p>Traps: {@code name} and {@code slug} cannot be changed after creation -
 * {@code PATCH /private-providers/{slug}} edits only {@code display_name} and
 * {@code base_url}. Neither the name nor its slug may match a public provider
 * name or slug (case-insensitive); the server rejects the collision, while
 * other customers may use the same name. The data policy is fixed by
 * OpenRouter: a private provider serves only your organization, so its
 * endpoints are recorded as retaining no prompts, training on none and
 * publishing none, and pass every data-policy filter your account or
 * guardrails apply - there is no caller-declared policy and none appears in
 * this object. {@code base_url} is the HTTPS base URL of an OpenAI-compatible
 * API used by private endpoints under this provider that set no URL of their
 * own; private, internal, metadata and OpenRouter hosts are rejected.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterPrivateProvider {

    private final JSONObject json;

    OpenRouterPrivateProvider(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw provider object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code name} - the provider name shown in your activity,
     * logs and Broadcast output (2-64 letters, numbers, spaces, dots,
     * underscores or hyphens). Cannot be changed later; neither the name nor
     * its slug may match a public provider name or slug (case-insensitive).
     *
     * @return the name, or {@code null} when absent or not a string
     */
    public String name() {
        Object value = json.opt("name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code slug} - the slug you chose, unique within your
     * account. Use it as {@code provider_slug} when creating private
     * endpoints and BYOK keys, and in {@code provider.order}, {@code only}
     * and {@code ignore}. Cannot be changed later.
     *
     * @return the slug, or {@code null} when absent or not a string
     */
    public String slug() {
        Object value = json.opt("slug");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code display_name} - the human-readable provider name
     * shown in the UI (defaults to {@code name}).
     *
     * @return the display name, or {@code null} when absent or not a string
     */
    public String displayName() {
        Object value = json.opt("display_name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code base_url} - the HTTPS base URL of an
     * OpenAI-compatible API, used by private endpoints under this provider
     * that set no URL of their own. The field is nullable in the schema:
     * {@code null} means the provider carries no URL and every endpoint must
     * set its own.
     *
     * @return the base URL, or {@code null} when absent, JSON {@code null} or
     *         not a string
     */
    public String baseUrl() {
        Object value = json.opt("base_url");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code created_at} - provider creation time (ISO 8601
     * date-time).
     *
     * @return the creation time, or {@code null} when absent or not a string
     */
    public String createdAt() {
        Object value = json.opt("created_at");
        return value instanceof String text ? text : null;
    }
}
