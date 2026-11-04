package de.entwicklertraining.openrouter4j.externalusers;

import org.json.JSONObject;

/**
 * A typed view of one external-user registration (schema
 * {@code ExternalUser}): the tracking string, its registration state and
 * the two timestamps.
 *
 * <p>A registration is created implicitly by sending {@code user} on
 * inference requests or {@code external.user} on API keys (for organization
 * keys outside HIPAA workspaces; the stored value has surrounding
 * whitespace trimmed) - the tracking string is then registered with
 * {@code is_active: true}.
 *
 * <p>{@link #isActive()} is registration state only - it is not an
 * inference authorization decision and does not block inference when
 * {@code false}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterExternalUser {

    private final JSONObject json;

    OpenRouterExternalUser(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw registration object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code user} - the immutable case-sensitive tracking
     * string supplied as {@code user} on inference requests or as
     * {@code external.user} on API keys (1-512 characters, well-formed
     * Unicode, no NUL, no surrounding whitespace).
     *
     * @return the tracking string, or {@code null} when absent or not a
     *         string
     */
    public String user() {
        Object value = json.opt("user");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code is_active} - whether this registration is active.
     * Registration state only; not an inference authorization decision.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isActive() {
        Object value = json.opt("is_active");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code created_at} - registration creation time (ISO 8601
     * date-time).
     *
     * @return the creation time, or {@code null} when absent or not a
     *         string
     */
    public String createdAt() {
        Object value = json.opt("created_at");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code updated_at} - last registration lifecycle update
     * time (ISO 8601 date-time).
     *
     * @return the update time, or {@code null} when absent or not a
     *         string
     */
    public String updatedAt() {
        Object value = json.opt("updated_at");
        return value instanceof String text ? text : null;
    }
}
