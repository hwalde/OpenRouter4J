package de.entwicklertraining.openrouter4j.vault;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code github.apps[].member_links[]} in the
 * vault routing table: a member whose own account the vault would act as
 * on this App's accounts (GitHub still limits a member's token to what
 * that member can reach).
 *
 * <p>{@link #via()} names how the vault picks this link for the App, by
 * the same selection egress uses: {@code app_link}, {@code token} or
 * {@code any_link}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultGithubMemberLink {

    private final JSONObject json;

    OpenRouterVaultGithubMemberLink(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw member-link object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code login} - the GitHub login of the member.
     *
     * @return the login, or {@code null} when absent or not a string
     */
    public String login() {
        Object value = json.opt("login");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code via} - how the vault picks this link for the App:
     * {@code app_link}, {@code token} or {@code any_link}; unknown values
     * are passed through verbatim.
     *
     * @return the selection form, or {@code null} when absent or not a
     *         string
     */
    public String via() {
        Object value = json.opt("via");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code holds_token} - whether the vault holds a token for
     * this link.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean holdsToken() {
        Object value = json.opt("holds_token");
        return value instanceof Boolean flag ? flag : null;
    }
}
