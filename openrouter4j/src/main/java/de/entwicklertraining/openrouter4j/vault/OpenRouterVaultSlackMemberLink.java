package de.entwicklertraining.openrouter4j.vault;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code slack.member_links[]} in the vault
 * routing table: a member whose own Slack account the vault can act as.
 * Whether the intern's own Slack app runs is a runtime fact the
 * {@code connections} tool reports, not part of this table.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultSlackMemberLink {

    private final JSONObject json;

    OpenRouterVaultSlackMemberLink(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw member-link object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code slack_user_id} - the Slack user id of the member.
     *
     * @return the user id, or {@code null} when absent or not a string
     */
    public String slackUserId() {
        Object value = json.opt("slack_user_id");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code holds_token} - whether the vault holds a token to
     * act as this member.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean holdsToken() {
        Object value = json.opt("holds_token");
        return value instanceof Boolean flag ? flag : null;
    }
}
