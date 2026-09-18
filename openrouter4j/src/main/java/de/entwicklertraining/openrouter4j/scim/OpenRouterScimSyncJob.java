package de.entwicklertraining.openrouter4j.scim;

import org.json.JSONObject;

/**
 * A shared view of a SCIM directory-sync job (schema {@code ScimSyncJob}).
 *
 * <p>All accessors follow the swallow-and-return-null convention; a running
 * job reports {@code null} for the completion fields.
 */
public final class OpenRouterScimSyncJob {

    private final JSONObject json;

    /**
     * Creates the view.
     *
     * @param json the raw sync-job object
     */
    public OpenRouterScimSyncJob(JSONObject json) {
        this.json = json;
    }

    /** @return the raw sync-job object */
    public JSONObject json() {
        return json;
    }

    /** @return the JSON field {@code id} (UUID) */
    public String id() {
        return json.optString("id", null);
    }

    /** @return the JSON field {@code status} ({@code queued}, {@code running}, {@code succeeded} or {@code failed}) */
    public String status() {
        return json.optString("status", null);
    }

    /** @return whether {@code status} is a terminal state ({@code succeeded} or {@code failed}) */
    public boolean isTerminal() {
        String s = status();
        return "succeeded".equals(s) || "failed".equals(s);
    }

    /** @return whether {@code status} is {@code succeeded} */
    public boolean isSucceeded() {
        return "succeeded".equals(status());
    }

    /** @return whether {@code status} is {@code failed} */
    public boolean isFailed() {
        return "failed".equals(status());
    }

    /** @return the JSON field {@code synced_groups}, or {@code null} while the job has not completed successfully */
    public Long syncedGroups() {
        return optLong("synced_groups");
    }

    /** @return the JSON field {@code deleted_groups}, or {@code null} while the job has not completed successfully */
    public Long deletedGroups() {
        return optLong("deleted_groups");
    }

    /** @return the JSON field {@code error_message}, or {@code null} when the job did not fail */
    public String errorMessage() {
        return json.optString("error_message", null);
    }

    /** @return the JSON field {@code created_at} */
    public String createdAt() {
        return json.optString("created_at", null);
    }

    /** @return the JSON field {@code started_at}, or {@code null} while not started */
    public String startedAt() {
        return json.optString("started_at", null);
    }

    /** @return the JSON field {@code finished_at}, or {@code null} while not finished */
    public String finishedAt() {
        return json.optString("finished_at", null);
    }

    private Long optLong(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        Object value = json.get(key);
        return value instanceof Number number ? number.longValue() : null;
    }
}
