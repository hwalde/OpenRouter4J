package de.entwicklertraining.openrouter4j.vault;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code github.tokens[]} in the vault
 * routing table: a personal GitHub token the vault can inject.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultGithubToken {

    private final JSONObject json;

    OpenRouterVaultGithubToken(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw token entry behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code login} - the GitHub login the token belongs to.
     *
     * @return the login, or {@code null} when absent or not a string
     */
    public String login() {
        Object value = json.opt("login");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code owner} - the owner the token is pinned to.
     *
     * @return the owner, or {@code null} when absent, not a string or JSON
     *         {@code null}
     */
    public String owner() {
        Object value = json.opt("owner");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code repository_count} - repositories the token can
     * reach.
     *
     * @return the count, or {@code null} when absent, not a number or JSON
     *         {@code null}
     */
    public Long repositoryCount() {
        Object value = json.opt("repository_count");
        return value instanceof Number number ? number.longValue() : null;
    }

    /**
     * JSON path: {@code scope} - {@code workspace} applies to every intern
     * of the workspace; {@code intern} to this intern alone. Unknown values
     * are passed through verbatim.
     *
     * @return the scope, or {@code null} when absent or not a string
     */
    public String scope() {
        Object value = json.opt("scope");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code holds_token} - whether the vault holds a current
     * token to inject.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean holdsToken() {
        Object value = json.opt("holds_token");
        return value instanceof Boolean flag ? flag : null;
    }
}
