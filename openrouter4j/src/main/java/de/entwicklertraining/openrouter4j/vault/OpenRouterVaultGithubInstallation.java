package de.entwicklertraining.openrouter4j.vault;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code github.apps[].installations[]} in
 * the vault routing table: a GitHub App installation the vault can inject
 * an installation token for.
 *
 * <p>Traps: {@link #isOffForIntern()} is the workspace turning this shared
 * installation off or hiding it for the intern; {@link #holdsToken()} is
 * whether the vault holds a current installation token to inject;
 * {@link #shadowedBy()} names what egress uses instead of this
 * installation for a request to its account (an earlier installation on
 * the same account, or a token pinned to the account) - {@code null} when
 * this installation answers.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultGithubInstallation {

    private final JSONObject json;

    OpenRouterVaultGithubInstallation(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw installation object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code credential_id} - the vault credential behind this
     * installation.
     *
     * @return the credential id, or {@code null} when absent or not a
     *         string
     */
    public String credentialId() {
        Object value = json.opt("credential_id");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code account} - the account the App is installed on, as
     * GitHub spells it.
     *
     * @return the account, or {@code null} when absent or not a string
     */
    public String account() {
        Object value = json.opt("account");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code account_type} - {@code organization} or
     * {@code user}; {@code null} when unknown, unknown values are passed
     * through verbatim.
     *
     * @return the account type, or {@code null} when absent, not a string
     *         or JSON {@code null}
     */
    public String accountType() {
        Object value = json.opt("account_type");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code is_enabled} - whether the installation is enabled.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isEnabled() {
        Object value = json.opt("is_enabled");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code is_off_for_intern} - the workspace turned this
     * shared installation off or hid it for this intern.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isOffForIntern() {
        Object value = json.opt("is_off_for_intern");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code holds_token} - whether the vault holds a current
     * installation token to inject.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean holdsToken() {
        Object value = json.opt("holds_token");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code coverage} - what GitHub says the installation
     * covers; {@code null} until read.
     *
     * @return the coverage view, or {@code null} when absent, not an object
     *         or JSON {@code null}
     */
    public OpenRouterVaultInstallationCoverage coverage() {
        JSONObject coverage = json.optJSONObject("coverage");
        return coverage != null ? new OpenRouterVaultInstallationCoverage(coverage) : null;
    }

    /**
     * JSON path: {@code shadowed_by} - what egress uses instead of this
     * installation for a request to its account: {@code installation} (an
     * earlier installation on the same account) or {@code token} (a token
     * pinned to the account); {@code null} when this installation answers.
     * Unknown values are passed through verbatim.
     *
     * @return the shadowing source, or {@code null} when absent, not a
     *         string or JSON {@code null}
     */
    public String shadowedBy() {
        Object value = json.opt("shadowed_by");
        return value instanceof String text ? text : null;
    }
}
