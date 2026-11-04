package de.entwicklertraining.openrouter4j.vault;

import org.json.JSONObject;

/**
 * A typed view of one GitHub installation's {@code coverage} object in the
 * vault routing table: what GitHub says the installation covers, read
 * through the installation token the vault holds and cached. {@code null}
 * until read.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultInstallationCoverage {

    private final JSONObject json;

    OpenRouterVaultInstallationCoverage(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw coverage object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code selection} - {@code all} or {@code selected};
     * unknown values are passed through verbatim.
     *
     * @return the selection, or {@code null} when absent or not a string
     */
    public String selection() {
        Object value = json.opt("selection");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code repository_count} - repositories the installation
     * covers; {@code null} when GitHub did not count them.
     *
     * @return the count, or {@code null} when absent, not a number or JSON
     *         {@code null}
     */
    public Long repositoryCount() {
        Object value = json.opt("repository_count");
        return value instanceof Number number ? number.longValue() : null;
    }

    /**
     * JSON path: {@code checked_at} - when GitHub answered, through the
     * installation token the vault holds.
     *
     * @return the check time, or {@code null} when absent or not a string
     */
    public String checkedAt() {
        Object value = json.opt("checked_at");
        return value instanceof String text ? text : null;
    }
}
