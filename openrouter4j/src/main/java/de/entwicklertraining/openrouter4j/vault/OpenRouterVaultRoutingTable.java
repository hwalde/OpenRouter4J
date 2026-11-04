package de.entwicklertraining.openrouter4j.vault;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of the {@code VaultRoutingTable} object: the vault's
 * routing table for one intern - the credentials it holds and what it
 * would inject each for, from the same lookups the egress proxy picks a
 * credential with (GitHub repository coverage read through the
 * installation token and cached). Returned as the {@code routing} field of
 * GET /vault/interns/{internId}/connections.
 *
 * <p><b>The intern's own key only:</b> {@code routing} is {@code null} for
 * any other caller and when the vault could not build the table in time -
 * the connection keys still list. Never credentials: the table carries
 * metadata about them, not their values.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} / -empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultRoutingTable {

    private final JSONObject json;

    /**
     * Wraps one routing-table object. A {@code null} argument is read as an
     * empty object - every accessor answers {@code null}/empty.
     *
     * @param json the {@code VaultRoutingTable} object
     */
    public OpenRouterVaultRoutingTable(JSONObject json) {
        this.json = json != null ? json : new JSONObject();
    }

    /**
     * @return the raw routing table behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code agent_id} - the intern this table was built for.
     *
     * @return the agent id, or {@code null} when absent or not a string
     */
    public String agentId() {
        Object value = json.opt("agent_id");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code read_at} - when the vault read this table
     * (date-time).
     *
     * @return the read time, or {@code null} when absent or not a string
     */
    public String readAt() {
        Object value = json.opt("read_at");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code github.apps[]} - the GitHub Apps the vault can act
     * as. Non-object entries are dropped.
     *
     * @return the apps, empty when absent or not an array
     */
    public List<OpenRouterVaultGithubApp> githubApps() {
        List<OpenRouterVaultGithubApp> result = new ArrayList<>();
        JSONObject github = json.optJSONObject("github");
        JSONArray apps = github != null ? github.optJSONArray("apps") : null;
        if (apps == null) {
            return result;
        }
        for (int i = 0; i < apps.length(); i++) {
            JSONObject entry = apps.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultGithubApp(entry));
            }
        }
        return result;
    }

    /**
     * JSON path: {@code github.tokens[]} - the personal GitHub tokens the
     * vault can inject. Non-object entries are dropped.
     *
     * @return the tokens, empty when absent or not an array
     */
    public List<OpenRouterVaultGithubToken> githubTokens() {
        List<OpenRouterVaultGithubToken> result = new ArrayList<>();
        JSONObject github = json.optJSONObject("github");
        JSONArray tokens = github != null ? github.optJSONArray("tokens") : null;
        if (tokens == null) {
            return result;
        }
        for (int i = 0; i < tokens.length(); i++) {
            JSONObject entry = tokens.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultGithubToken(entry));
            }
        }
        return result;
    }

    /**
     * JSON path: {@code google.accounts[]} - the Google accounts the vault
     * can act as. Non-object entries are dropped.
     *
     * @return the accounts, empty when absent or not an array
     */
    public List<OpenRouterVaultGoogleAccount> googleAccounts() {
        List<OpenRouterVaultGoogleAccount> result = new ArrayList<>();
        JSONObject google = json.optJSONObject("google");
        JSONArray accounts = google != null ? google.optJSONArray("accounts") : null;
        if (accounts == null) {
            return result;
        }
        for (int i = 0; i < accounts.length(); i++) {
            JSONObject entry = accounts.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultGoogleAccount(entry));
            }
        }
        return result;
    }

    /**
     * JSON path: {@code slack.member_links[]} - members whose own Slack
     * account the vault can act as. Non-object entries are dropped.
     *
     * @return the member links, empty when absent or not an array
     */
    public List<OpenRouterVaultSlackMemberLink> slackMemberLinks() {
        List<OpenRouterVaultSlackMemberLink> result = new ArrayList<>();
        JSONObject slack = json.optJSONObject("slack");
        JSONArray links = slack != null ? slack.optJSONArray("member_links") : null;
        if (links == null) {
            return result;
        }
        for (int i = 0; i < links.length(); i++) {
            JSONObject entry = links.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultSlackMemberLink(entry));
            }
        }
        return result;
    }

    /**
     * JSON path: {@code mcp_origins[]} - the MCP server origins the vault
     * injects for. Non-object entries are dropped.
     *
     * @return the MCP origins, empty when absent or not an array
     */
    public List<OpenRouterVaultMcpOrigin> mcpOrigins() {
        List<OpenRouterVaultMcpOrigin> result = new ArrayList<>();
        JSONArray origins = json.optJSONArray("mcp_origins");
        if (origins == null) {
            return result;
        }
        for (int i = 0; i < origins.length(); i++) {
            JSONObject entry = origins.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultMcpOrigin(entry));
            }
        }
        return result;
    }

    /**
     * JSON path: {@code host_rules[]} - built-in hosts the vault injects a
     * named secret for, and whether that secret is stored. Non-object
     * entries are dropped.
     *
     * @return the host rules, empty when absent or not an array
     */
    public List<OpenRouterVaultHostRule> hostRules() {
        List<OpenRouterVaultHostRule> result = new ArrayList<>();
        JSONArray rules = json.optJSONArray("host_rules");
        if (rules == null) {
            return result;
        }
        for (int i = 0; i < rules.length(); i++) {
            JSONObject entry = rules.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultHostRule(entry));
            }
        }
        return result;
    }

    /**
     * JSON path: {@code custom_origins[]} - custom destinations the vault
     * injects for. Non-object entries are dropped.
     *
     * @return the custom origins, empty when absent or not an array
     */
    public List<OpenRouterVaultCustomOrigin> customOrigins() {
        List<OpenRouterVaultCustomOrigin> result = new ArrayList<>();
        JSONArray origins = json.optJSONArray("custom_origins");
        if (origins == null) {
            return result;
        }
        for (int i = 0; i < origins.length(); i++) {
            JSONObject entry = origins.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultCustomOrigin(entry));
            }
        }
        return result;
    }

    /**
     * JSON path: {@code secrets[]} - the secrets the vault would inject, by
     * name and scope (metadata only). Non-object entries are dropped.
     *
     * @return the secret entries, empty when absent or not an array
     */
    public List<OpenRouterVaultRoutingSecret> secrets() {
        List<OpenRouterVaultRoutingSecret> result = new ArrayList<>();
        JSONArray secrets = json.optJSONArray("secrets");
        if (secrets == null) {
            return result;
        }
        for (int i = 0; i < secrets.length(); i++) {
            JSONObject entry = secrets.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultRoutingSecret(entry));
            }
        }
        return result;
    }
}
