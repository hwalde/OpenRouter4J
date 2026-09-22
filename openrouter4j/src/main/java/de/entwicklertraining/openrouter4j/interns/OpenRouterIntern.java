package de.entwicklertraining.openrouter4j.interns;

import org.json.JSONObject;

/**
 * A typed view of one intern (schema {@code Intern}): the public lifecycle
 * state and settings an intern endpoint returns.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterIntern {

    private final JSONObject json;

    OpenRouterIntern(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code id} - the intern id; the path segment of the
     * per-intern endpoints.
     *
     * @return the id, or {@code null} when absent
     */
    public String id() {
        return json.optString("id", null);
    }

    /**
     * JSON path: {@code name} - intern name, unique per creator within a
     * workspace (2-17 chars, lowercase start, digits and single hyphens).
     *
     * @return the name, or {@code null} when absent
     */
    public String name() {
        return json.optString("name", null);
    }

    /**
     * JSON path: {@code description} - free-form description.
     *
     * @return the description, or {@code null} when absent
     */
    public String description() {
        if (!json.has("description") || json.isNull("description")) {
            return null;
        }
        return json.optString("description", null);
    }

    /**
     * JSON path: {@code instructions} - the standing instructions the intern
     * boots with.
     *
     * @return the instructions, or {@code null} when absent
     */
    public String instructions() {
        if (!json.has("instructions") || json.isNull("instructions")) {
            return null;
        }
        return json.optString("instructions", null);
    }

    /**
     * JSON path: {@code model} - the OpenRouter model slug the intern runs,
     * or {@code null} for the workspace default. Change it via the update
     * endpoint.
     *
     * @return the model slug, or {@code null} when absent
     */
    public String model() {
        if (!json.has("model") || json.isNull("model")) {
            return null;
        }
        return json.optString("model", null);
    }

    /**
     * JSON path: {@code status} - the lifecycle status: {@code
     * awaiting_slack_install}, {@code queued}, {@code provisioning},
     * {@code running}, {@code failed}, {@code stopped}, {@code destroying}
     * or {@code destroy_failed} (unknown values are passed through
     * verbatim).
     *
     * @return the status, or {@code null} when absent
     */
    public String status() {
        return json.optString("status", null);
    }

    /**
     * JSON path: {@code last_failure_message} - why the last provisioning
     * attempt failed, when {@link #status()} is {@code failed}.
     *
     * @return the failure message, or {@code null} when absent
     */
    public String lastFailureMessage() {
        if (!json.has("last_failure_message") || json.isNull("last_failure_message")) {
            return null;
        }
        return json.optString("last_failure_message", null);
    }

    /**
     * JSON path: {@code progress} - the active provisioning step, or
     * {@code null} once provisioning has settled.
     *
     * @return the progress view, or {@code null} when absent
     */
    public OpenRouterInternProgress progress() {
        JSONObject progress = json.optJSONObject("progress");
        return progress == null ? null : new OpenRouterInternProgress(progress);
    }

    /**
     * JSON path: {@code hostname} - the public hostname the intern is
     * reachable at, or {@code null} until provisioning has assigned one.
     *
     * @return the hostname, or {@code null} when absent
     */
    public String hostname() {
        if (!json.has("hostname") || json.isNull("hostname")) {
            return null;
        }
        return json.optString("hostname", null);
    }

    /**
     * JSON path: {@code workspace_id} - the workspace that owns the intern
     * and scopes its secrets.
     *
     * @return the workspace id, or {@code null} when absent
     */
    public String workspaceId() {
        return json.optString("workspace_id", null);
    }

    /**
     * JSON path: {@code vault_id} - the vault the intern owns, or
     * {@code null} before it has been created.
     *
     * @return the owned vault id, or {@code null} when absent
     */
    public String vaultId() {
        if (!json.has("vault_id") || json.isNull("vault_id")) {
            return null;
        }
        return json.optString("vault_id", null);
    }

    /**
     * JSON path: {@code attached_vault_id} - the vault the intern borrows
     * from another intern, or {@code null} when it borrows none.
     *
     * @return the borrowed vault id, or {@code null} when absent
     */
    public String attachedVaultId() {
        if (!json.has("attached_vault_id") || json.isNull("attached_vault_id")) {
            return null;
        }
        return json.optString("attached_vault_id", null);
    }

    /**
     * JSON path: {@code created_at} - ISO 8601 creation time.
     *
     * @return the creation time, or {@code null} when absent
     */
    public String createdAt() {
        return json.optString("created_at", null);
    }

    /**
     * JSON path: {@code updated_at} - ISO 8601 last update time.
     *
     * @return the update time, or {@code null} when absent
     */
    public String updatedAt() {
        return json.optString("updated_at", null);
    }

    /**
     * A typed view of {@code progress} - the active provisioning step.
     */
    public static final class OpenRouterInternProgress {

        private final JSONObject json;

        OpenRouterInternProgress(JSONObject json) {
            this.json = json;
        }

        /**
         * JSON path: {@code progress.step_number} - one-based index of the
         * active step.
         *
         * @return the step number, or {@code null} when absent or not a
         *         number
         */
        public Integer stepNumber() {
            if (!json.has("step_number") || json.isNull("step_number")) {
                return null;
            }
            Object value = json.opt("step_number");
            return value instanceof Number number ? number.intValue() : null;
        }

        /**
         * JSON path: {@code progress.total_steps} - number of provisioning
         * steps.
         *
         * @return the total steps, or {@code null} when absent or not a
         *         number
         */
        public Integer totalSteps() {
            if (!json.has("total_steps") || json.isNull("total_steps")) {
                return null;
            }
            Object value = json.opt("total_steps");
            return value instanceof Number number ? number.intValue() : null;
        }

        /**
         * JSON path: {@code progress.step_label} - human-readable label of
         * the active provisioning step.
         *
         * @return the label, or {@code null} when absent
         */
        public String stepLabel() {
            return json.optString("step_label", null);
        }
    }
}
